package br.com.zamfir.verticalize.importacao.presentation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.core.presentation.UiText
import br.com.zamfir.verticalize.importacao.domain.ExportError
import br.com.zamfir.verticalize.importacao.domain.ExportSummary
import org.junit.jupiter.api.Test

class ExportFeedbackTest {

    private fun UiText.resId() = (this as UiText.StringResource).id

    @Test
    fun `sucesso usa a mensagem escolhida pela tela com o resumo`() {
        var resumoRecebido: ExportSummary? = null

        val feedback = Result.Success(ExportSummary(concursos = 2, conteudos = 5, estudos = 3)).toFeedback { resumo ->
            resumoRecebido = resumo
            UiText.StringResource(R.string.export_success_format)
        }

        assertThat(feedback).isInstanceOf(ExportFeedback.Success::class)
        assertThat(feedback.message.resId()).isEqualTo(R.string.export_success_format)
        assertThat(resumoRecebido).isEqualTo(ExportSummary(concursos = 2, conteudos = 5, estudos = 3))
    }

    @Test
    fun `falha usa a mensagem do proprio erro e nao chama a mensagem de sucesso`() {
        var chamou = false

        val feedback = Result.Error(ExportError.FALHA_AO_GRAVAR).toFeedback {
            chamou = true
            UiText.StringResource(R.string.export_success_format)
        }

        assertThat(feedback).isInstanceOf(ExportFeedback.Failure::class)
        assertThat(feedback.message.resId()).isEqualTo(R.string.export_error_falha_gravar)
        assertThat(chamou).isEqualTo(false)
    }

    @Test
    fun `cada erro de exportacao tem sua mensagem`() {
        assertThat(ExportError.SEM_DADOS.messageRes()).isEqualTo(R.string.export_error_sem_dados)
        assertThat(ExportError.FALHA_AO_LER.messageRes()).isEqualTo(R.string.export_error_falha_ler)
        assertThat(ExportError.FALHA_AO_GRAVAR.messageRes()).isEqualTo(R.string.export_error_falha_gravar)
    }
}
