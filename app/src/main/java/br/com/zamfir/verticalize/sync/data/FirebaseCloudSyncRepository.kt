package br.com.zamfir.verticalize.sync.data

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import br.com.zamfir.verticalize.auth.domain.AuthRepository
import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.sync.domain.AcaoSyncInicial
import br.com.zamfir.verticalize.sync.domain.CloudSyncRepository
import br.com.zamfir.verticalize.sync.domain.EscolhaConflito
import br.com.zamfir.verticalize.sync.domain.ResultadoSyncInicial
import br.com.zamfir.verticalize.sync.domain.SyncError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "CloudSync"
private const val KEY_UID_ATIVO = "uid_ativo"

/** Agrupa alterações em sequência (ex.: importação, cronômetro) num único envio. */
private const val ENVIO_DEBOUNCE_MILLIS = 500L

/** Registros por operação de escrita na nuvem. */
private const val ENVIO_LOTE = 200

/**
 * Sem conexão, a gravação no Realtime Database fica na fila local do SDK e só conclui quando a rede
 * volta; as pendências continuam no Room e o envio é tentado de novo depois de [ESPERA_MAXIMA_MILLIS].
 */
private const val ENVIO_TIMEOUT_MILLIS = 30_000L
private const val ESPERA_INICIAL_MILLIS = 5_000L
private const val ESPERA_MAXIMA_MILLIS = 60_000L

class FirebaseCloudSyncRepository(
    private val dao: SyncDao,
    private val remote: FirebaseBackupDataSource,
    private val authRepository: AuthRepository,
    private val prefs: SharedPreferences
) : CloudSyncRepository {

    /** Usuário cujos dados já foram conciliados com a nuvem; persiste entre aberturas do app. */
    private val uidAtivo = MutableStateFlow(prefs.getString(KEY_UID_ATIVO, null))

    /** Conciliação e envios nunca rodam ao mesmo tempo, para um não atropelar o outro. */
    private val mutex = Mutex()

    override fun isSincronizacaoAtiva(uid: String): Boolean = uidAtivo.value == uid

    override suspend fun prepararSincronizacao(uid: String): Result<ResultadoSyncInicial, SyncError> =
        mutex.withLock {
            val local = when (val r = lerLocal()) {
                is Result.Error -> return@withLock r
                is Result.Success -> r.data
            }
            val remoto = when (val r = baixar(uid)) {
                is Result.Error -> return@withLock r
                is Result.Success -> r.data
            }

            when {
                remoto == null || remoto.snapshot.isEmpty -> {
                    if (local.isEmpty) {
                        ativar(uid).toConcluida(AcaoSyncInicial.NADA_A_FAZER)
                    } else {
                        substituirNuvemEAtivar(uid, local).toConcluida(AcaoSyncInicial.ENVIOU_PARA_NUVEM)
                    }
                }

                local.isEmpty ->
                    substituirLocalEAtivar(uid, remoto.snapshot).toConcluida(AcaoSyncInicial.BAIXOU_DA_NUVEM)

                local == remoto.snapshot -> ativar(uid).toConcluida(AcaoSyncInicial.NADA_A_FAZER)

                else -> Result.Success(
                    ResultadoSyncInicial.Conflito(
                        aparelho = local.resumo(),
                        nuvem = remoto.snapshot.resumo(remoto.atualizadoEm)
                    )
                )
            }
        }

    override suspend fun resolverConflito(
        uid: String,
        escolha: EscolhaConflito
    ): Result<AcaoSyncInicial, SyncError> = mutex.withLock {
        when (escolha) {
            EscolhaConflito.USAR_NUVEM -> {
                // Baixa de novo em vez de reaproveitar o que foi lido antes do diálogo: a nuvem pode
                // ter mudado (outro aparelho) enquanto o usuário decidia.
                when (val r = baixar(uid)) {
                    is Result.Error -> r
                    is Result.Success -> {
                        val snapshot = r.data?.snapshot ?: BackupSnapshot(emptyList(), emptyList(), emptyList())
                        substituirLocalEAtivar(uid, snapshot).toAcao(AcaoSyncInicial.BAIXOU_DA_NUVEM)
                    }
                }
            }

            EscolhaConflito.MANTER_APARELHO -> when (val r = lerLocal()) {
                is Result.Error -> r
                is Result.Success -> substituirNuvemEAtivar(uid, r.data).toAcao(AcaoSyncInicial.ENVIOU_PARA_NUVEM)
            }
        }
    }

    override fun desativar() {
        uidAtivo.value = null
        prefs.edit { remove(KEY_UID_ATIVO) }
    }

    /**
     * Mantém aparelho e nuvem sincronizados nos dois sentidos enquanto houver um usuário logado com a
     * sincronização ativa: envia as alterações locais e aplica as que chegam de outros aparelhos.
     * Deve ser chamado uma única vez, com um escopo que dure o processo inteiro.
     */
    fun iniciarSincronizacaoContinua(scope: CoroutineScope) {
        val uidSincronizado: Flow<String?> =
            combine(authRepository.observeCurrentUser(), uidAtivo) { user, ativo ->
                user?.id?.takeIf { it == ativo }
            }.distinctUntilChanged()

        scope.launch { enviarAlteracoesLocais(uidSincronizado) }
        scope.launch {
            uidSincronizado.collectLatest { uid -> if (uid != null) receberAlteracoesRemotas(uid) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private suspend fun enviarAlteracoesLocais(uidSincronizado: Flow<String?>) {
        uidSincronizado
            .flatMapLatest { uid ->
                if (uid == null) emptyFlow() else dao.observePendencias().debounce(ENVIO_DEBOUNCE_MILLIS).map { uid }
            }
            .collectLatest { uid ->
                // Sem rede, tenta de novo com espera crescente; uma nova alteração local reinicia o ciclo.
                var espera = ESPERA_INICIAL_MILLIS
                while (!enviarPendencias(uid)) {
                    delay(espera)
                    espera = (espera * 2).coerceAtMost(ESPERA_MAXIMA_MILLIS)
                }
            }
    }

    private suspend fun receberAlteracoesRemotas(uid: String) {
        var espera = ESPERA_INICIAL_MILLIS
        remote.observarDados(uid)
            .retryWhen { causa, _ ->
                if (causa is CancellationException) return@retryWhen false
                Log.w(TAG, "Falha ao observar a nuvem; nova tentativa em ${espera / 1000}s", causa)
                delay(espera.milliseconds)
                espera = (espera * 2).coerceAtMost(ESPERA_MAXIMA_MILLIS)
                true
            }
            .collect { remoto ->
                espera = ESPERA_INICIAL_MILLIS
                try {
                    dao.aplicarRemoto(remoto)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Falha ao aplicar no aparelho as alterações da nuvem", e)
                }
            }
    }

    /** true quando não sobrou nada a enviar (ou a sincronização foi desligada no meio). */
    private suspend fun enviarPendencias(uid: String): Boolean = mutex.withLock {
        if (!isSincronizacaoAtiva(uid)) return@withLock true
        try {
            for (lote in dao.getPendencias().chunked(ENVIO_LOTE)) {
                val alteracoes = lote.associate { pendencia ->
                    caminhoDoRegistro(pendencia.tabela, pendencia.registroId) to registroParaEnvio(pendencia)
                }
                val concluiu = withTimeoutOrNull(ENVIO_TIMEOUT_MILLIS.milliseconds) {
                    remote.enviarAlteracoes(uid, alteracoes)
                } != null
                if (!concluiu) {
                    Log.w(TAG, "Envio para a nuvem não confirmado a tempo (sem conexão?)")
                    return@withLock false
                }
                dao.confirmarEnvio(lote)
            }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao enviar alterações para a nuvem", e)
            false
        }
    }

    /** Estado atual do registro na forma da nuvem, ou null para excluí-lo de lá. */
    private suspend fun registroParaEnvio(pendencia: SyncPendenciaEntity): Map<String, Any?>? {
        if (pendencia.excluido) return null
        val id = pendencia.registroId
        // Lê o estado atual (e não o do momento da alteração): se o registro sumiu desde então, exclui.
        return when (pendencia.tabela) {
            TABELA_CONCURSOS -> dao.getConcurso(id)?.toFirebaseMap()
            TABELA_CONTEUDOS -> dao.getConteudo(id)?.toFirebaseMap()
            TABELA_ESTUDOS -> dao.getEstudo(id)?.toFirebaseMap()
            else -> null
        }
    }

    private suspend fun substituirNuvemEAtivar(uid: String, local: BackupSnapshot): Result<Unit, SyncError> {
        return try {
            val concluiu = withTimeoutOrNull(ENVIO_TIMEOUT_MILLIS.milliseconds) { remote.substituirTudo(uid, local) } != null
            if (concluiu) {
                ativar(uid)
            } else {
                Log.w(TAG, "Envio para a nuvem não confirmado a tempo (sem conexão?)")
                Result.Error(SyncError.FALHA_NUVEM)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao enviar os dados para a nuvem", e)
            Result.Error(SyncError.FALHA_NUVEM)
        }
    }

    private suspend fun substituirLocalEAtivar(uid: String, snapshot: BackupSnapshot): Result<Unit, SyncError> {
        return try {
            dao.substituirTudo(snapshot)
            ativar(uid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao gravar no aparelho os dados baixados da nuvem", e)
            Result.Error(SyncError.FALHA_LOCAL)
        }
    }

    /**
     * Liga a sincronização contínua. As pendências acumuladas até aqui (inclusive as de antes do login)
     * são descartadas: a conciliação acabou de deixar aparelho e nuvem iguais.
     */
    private suspend fun ativar(uid: String): Result<Unit, SyncError> {
        return try {
            dao.limparPendencias()
            uidAtivo.value = uid
            prefs.edit { putString(KEY_UID_ATIVO, uid) }
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao preparar a sincronização no aparelho", e)
            Result.Error(SyncError.FALHA_LOCAL)
        }
    }

    private suspend fun lerLocal(): Result<BackupSnapshot, SyncError> {
        return try {
            Result.Success(dao.getSnapshot().normalizado())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao ler o banco local", e)
            Result.Error(SyncError.FALHA_LOCAL)
        }
    }

    private suspend fun baixar(uid: String): Result<BackupRemoto?, SyncError> {
        return try {
            Result.Success(remote.baixar(uid))
        } catch (e: CancellationException) {
            throw e
        } catch (e: BackupInvalidoException) {
            Log.w(TAG, "Dados da nuvem em formato inválido", e)
            Result.Error(SyncError.DADOS_INVALIDOS)
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao baixar os dados da nuvem", e)
            Result.Error(SyncError.FALHA_NUVEM)
        }
    }
}

private fun Result<Unit, SyncError>.toAcao(acao: AcaoSyncInicial): Result<AcaoSyncInicial, SyncError> =
    when (this) {
        is Result.Error -> this
        is Result.Success -> Result.Success(acao)
    }

private fun Result<Unit, SyncError>.toConcluida(acao: AcaoSyncInicial): Result<ResultadoSyncInicial, SyncError> =
    when (this) {
        is Result.Error -> this
        is Result.Success -> Result.Success(ResultadoSyncInicial.Concluida(acao))
    }
