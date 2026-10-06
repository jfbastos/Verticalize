package br.com.zamfir.verticalize.conteudo.presentation

import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.conteudo.domain.Conteudo
import br.com.zamfir.verticalize.conteudo.domain.Prioridade
import br.com.zamfir.verticalize.core.domain.formatDate

data class ConteudoUi(
    val id: Long,
    val materia: String,
    val descricao: String,
    val eixo: Int?,
    val bloco: Int?,
    val concluido: Boolean,
    val quantidadeAulas: Int,
    val quantidadeQuestoes: Int,
    val tempoMedioAulaMinutos: Int,
    val dataUltimaRevisaoFormatted: String?,
    val dataUltimaRevisaoMillis: Long? = null,
    val prioridade: Prioridade = Prioridade.MEDIA
)

fun Conteudo.toConteudoUi(): ConteudoUi {
    return ConteudoUi(
        id = id,
        materia = materia,
        descricao = descricao,
        eixo = eixo,
        bloco = bloco,
        concluido = concluido,
        quantidadeAulas = quantidadeAulas,
        tempoMedioAulaMinutos = tempoMedioAulaMinutos,
        quantidadeQuestoes = quantidadeQuestoesRealizadas,
        dataUltimaRevisaoFormatted = dataUltimaRevisao?.let(::formatDate),
        dataUltimaRevisaoMillis = dataUltimaRevisao,
        prioridade = prioridade
    )
}

fun Prioridade.labelRes(): Int {
    return when (this) {
        Prioridade.ALTA -> R.string.prioridade_alta
        Prioridade.MEDIA -> R.string.prioridade_media
        Prioridade.BAIXA -> R.string.prioridade_baixa
        Prioridade.OPCIONAL -> R.string.prioridade_opcional
    }
}

fun Int.toHorasMinutosFormatted(): String {
    val horas = this / 60
    val minutos = this % 60
    return if (minutos == 0) "${horas}h" else "${horas}h${minutos}min"
}
