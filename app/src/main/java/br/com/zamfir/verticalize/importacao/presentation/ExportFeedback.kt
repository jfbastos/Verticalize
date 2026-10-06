package br.com.zamfir.verticalize.importacao.presentation

import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.core.presentation.UiText
import br.com.zamfir.verticalize.importacao.domain.ExportError
import br.com.zamfir.verticalize.importacao.domain.ExportSummary

/** Resultado de uma exportação já convertido em texto para a tela mostrar: sucesso ou falha. */
sealed interface ExportFeedback {
    val message: UiText

    data class Success(override val message: UiText) : ExportFeedback
    data class Failure(override val message: UiText) : ExportFeedback
}

/** Cada tela escolhe a mensagem de sucesso; a mensagem de falha vem sempre do [ExportError]. */
fun Result<ExportSummary, ExportError>.toFeedback(successMessage: (ExportSummary) -> UiText): ExportFeedback {
    return when (this) {
        is Result.Success -> ExportFeedback.Success(successMessage(data))
        is Result.Error -> ExportFeedback.Failure(UiText.StringResource(error.messageRes()))
    }
}
