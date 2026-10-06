package br.com.zamfir.verticalize.concurso.presentation

import br.com.zamfir.verticalize.concurso.domain.Concurso
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.concurso.domain.calcularDiasParaProva
import br.com.zamfir.verticalize.concurso.domain.percentualMedio
import br.com.zamfir.verticalize.concurso.domain.percentualPorEixo
import br.com.zamfir.verticalize.conteudo.domain.Conteudo
import br.com.zamfir.verticalize.core.domain.formatDate

data class ConcursoDetailUi(
    val id: Long,
    val nome: String,
    val nivel: Nivel,
    val dataProvaFormatted: String?,
    val diasParaProva: Long?,
    val horasEstudadas: Int,
    val percentualCompletude: Int
)

fun Concurso.toConcursoDetailUi(): ConcursoDetailUi {
    return ConcursoDetailUi(
        id = id,
        nome = nome,
        nivel = nivel,
        dataProvaFormatted = dataProva?.let(::formatDate),
        diasParaProva = dataProva?.let(::calcularDiasParaProva),
        horasEstudadas = horasEstudadas,
        percentualCompletude = percentualCompletude
    )
}

data class EixoUi(
    val numero: Int,
    val percentualCompletude: Int
)

fun List<Conteudo>.toEixoUiList(): List<EixoUi> {
    return percentualPorEixo(map { it.eixo to it.concluido })
        .map { (numero, percentual) -> EixoUi(numero = numero, percentualCompletude = percentual) }
}

fun List<EixoUi>.averagePercentual(): Int = percentualMedio(map { it.percentualCompletude })
