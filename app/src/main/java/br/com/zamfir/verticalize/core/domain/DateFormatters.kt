package br.com.zamfir.verticalize.core.domain

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val dateFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("dd/MM/yyyy")
    .withZone(ZoneOffset.UTC)

/** Data no formato dd/MM/yyyy, em UTC (as datas do app são sempre meia-noite UTC, como as do DatePicker). */
fun formatDate(epochMillis: Long): String {
    return dateFormatter.format(Instant.ofEpochMilli(epochMillis))
}
