package br.com.zamfir.verticalize.sync.domain

import br.com.zamfir.verticalize.core.domain.Error
import br.com.zamfir.verticalize.core.domain.Result

enum class SyncError : Error {
    /** Falha ao ler ou gravar no Realtime Database (sem conexão, regras de acesso, banco não criado...). */
    FALHA_NUVEM,

    /** Falha ao ler ou gravar no banco local. */
    FALHA_LOCAL,

    /** O backup da nuvem existe, mas não está no formato esperado. */
    DADOS_INVALIDOS
}

/** Quantidade de dados de um lado da sincronização, exibida no diálogo de conflito. */
data class ResumoDados(
    val concursos: Int,
    val conteudos: Int,
    val estudos: Int,
    /** Quando o backup foi gravado na nuvem; null para os dados do aparelho. */
    val atualizadoEm: Long? = null
)

sealed interface ResultadoSyncInicial {
    /** A sincronização ficou ativa sem precisar perguntar nada ao usuário. */
    data class Concluida(val acao: AcaoSyncInicial) : ResultadoSyncInicial

    /** Aparelho e nuvem têm dados diferentes: o usuário precisa escolher qual lado manter. */
    data class Conflito(val aparelho: ResumoDados, val nuvem: ResumoDados) : ResultadoSyncInicial
}

enum class AcaoSyncInicial {
    /** Os dados da nuvem substituíram os do aparelho. */
    BAIXOU_DA_NUVEM,

    /** Os dados do aparelho foram enviados para a nuvem. */
    ENVIOU_PARA_NUVEM,

    /** Os dois lados já estavam iguais (ou vazios). */
    NADA_A_FAZER
}

enum class EscolhaConflito {
    USAR_NUVEM,
    MANTER_APARELHO
}

/**
 * Mantém uma cópia dos dados locais no Realtime Database, por usuário.
 *
 * Depois do login, [prepararSincronizacao] concilia aparelho e nuvem (baixando os dados salvos) e só
 * então a sincronização fica ativa para aquele usuário; a partir daí toda alteração local é enviada
 * automaticamente para a nuvem.
 */
interface CloudSyncRepository {
    /** Se aparelho e nuvem já foram conciliados para [uid] (envio automático ligado). */
    fun isSincronizacaoAtiva(uid: String): Boolean

    suspend fun prepararSincronizacao(uid: String): Result<ResultadoSyncInicial, SyncError>

    suspend fun resolverConflito(uid: String, escolha: EscolhaConflito): Result<AcaoSyncInicial, SyncError>

    /** Para o envio automático (logout). Os dados locais continuam no aparelho. */
    fun desativar()
}
