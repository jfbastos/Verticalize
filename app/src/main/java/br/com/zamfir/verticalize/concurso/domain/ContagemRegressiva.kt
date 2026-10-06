package br.com.zamfir.verticalize.concurso.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/**
 * Dias corridos entre [hoje] e [dataProvaMillis] (meia-noite UTC, como todas as datas do app).
 * Positivo se a prova ainda vai acontecer, zero se é hoje, negativo se já passou.
 */
fun calcularDiasParaProva(dataProvaMillis: Long, hoje: LocalDate = LocalDate.now(ZoneOffset.UTC)): Long {
    val dataDaProva = Instant.ofEpochMilli(dataProvaMillis).atZone(ZoneOffset.UTC).toLocalDate()
    return ChronoUnit.DAYS.between(hoje, dataDaProva)
}
