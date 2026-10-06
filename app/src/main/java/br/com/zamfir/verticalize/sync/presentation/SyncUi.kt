package br.com.zamfir.verticalize.sync.presentation

import androidx.annotation.StringRes
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.sync.domain.AcaoSyncInicial
import br.com.zamfir.verticalize.sync.domain.ResumoDados
import br.com.zamfir.verticalize.sync.domain.SyncError
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Aparelho e nuvem com dados diferentes no login, aguardando o usuário escolher qual manter. */
data class ConflitoSyncUi(
    val aparelho: ResumoDados,
    val nuvem: ResumoDados
)

@StringRes
fun SyncError.messageRes(): Int {
    return when (this) {
        SyncError.FALHA_NUVEM -> R.string.sync_error_falha_nuvem
        SyncError.FALHA_LOCAL -> R.string.sync_error_falha_local
        SyncError.DADOS_INVALIDOS -> R.string.sync_error_dados_invalidos
    }
}

/** null quando não há o que avisar (os dois lados já estavam iguais). */
@StringRes
fun AcaoSyncInicial.messageRes(): Int? {
    return when (this) {
        AcaoSyncInicial.BAIXOU_DA_NUVEM -> R.string.sync_baixou_da_nuvem
        AcaoSyncInicial.ENVIOU_PARA_NUVEM -> R.string.sync_enviou_para_nuvem
        AcaoSyncInicial.NADA_A_FAZER -> null
    }
}

// Diferente de formatDate (datas do app, sempre meia-noite UTC): aqui é um instante real, no fuso do aparelho.
private val atualizadoEmFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")

fun formatAtualizadoEm(epochMillis: Long): String {
    return atualizadoEmFormatter.format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))
}
