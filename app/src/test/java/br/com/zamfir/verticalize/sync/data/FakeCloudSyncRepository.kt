package br.com.zamfir.verticalize.sync.data

import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.sync.domain.AcaoSyncInicial
import br.com.zamfir.verticalize.sync.domain.CloudSyncRepository
import br.com.zamfir.verticalize.sync.domain.EscolhaConflito
import br.com.zamfir.verticalize.sync.domain.ResultadoSyncInicial
import br.com.zamfir.verticalize.sync.domain.SyncError

class FakeCloudSyncRepository : CloudSyncRepository {
    var uidAtivo: String? = null
    var resultadoPreparar: Result<ResultadoSyncInicial, SyncError> =
        Result.Success(ResultadoSyncInicial.Concluida(AcaoSyncInicial.NADA_A_FAZER))
    var prepararChamadas = 0
    var ultimaEscolha: EscolhaConflito? = null

    override fun isSincronizacaoAtiva(uid: String): Boolean = uidAtivo == uid

    override suspend fun prepararSincronizacao(uid: String): Result<ResultadoSyncInicial, SyncError> {
        prepararChamadas++
        if (resultadoPreparar is Result.Success &&
            (resultadoPreparar as Result.Success).data is ResultadoSyncInicial.Concluida
        ) {
            uidAtivo = uid
        }
        return resultadoPreparar
    }

    override suspend fun resolverConflito(
        uid: String,
        escolha: EscolhaConflito
    ): Result<AcaoSyncInicial, SyncError> {
        ultimaEscolha = escolha
        uidAtivo = uid
        return Result.Success(
            when (escolha) {
                EscolhaConflito.USAR_NUVEM -> AcaoSyncInicial.BAIXOU_DA_NUVEM
                EscolhaConflito.MANTER_APARELHO -> AcaoSyncInicial.ENVIOU_PARA_NUVEM
            }
        )
    }

    override fun desativar() {
        uidAtivo = null
    }
}
