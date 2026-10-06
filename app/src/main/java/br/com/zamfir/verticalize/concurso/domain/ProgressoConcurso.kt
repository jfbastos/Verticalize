package br.com.zamfir.verticalize.concurso.domain

import kotlin.math.roundToInt

fun percentualPorEixo(itens: List<Pair<Int?, Boolean>>): Map<Int, Int> {
    return itens
        .mapNotNull { (eixo, concluido) -> eixo?.let { it to concluido } }
        .groupBy({ it.first }, { it.second })
        .toSortedMap()
        .mapValues { (_, doEixo) -> doEixo.count { it } * 100 / doEixo.size }
}

fun percentualMedio(percentuais: Collection<Int>): Int {
    return if (percentuais.isEmpty()) 0 else (percentuais.sum().toDouble() / percentuais.size).roundToInt()
}

fun calcularPercentualCompletude(itens: List<Pair<Int?, Boolean>>): Int {
    return percentualMedio(percentualPorEixo(itens).values)
}

fun calcularHorasEstudadas(totalMinutos: Int): Int = (totalMinutos / 60.0).roundToInt()
