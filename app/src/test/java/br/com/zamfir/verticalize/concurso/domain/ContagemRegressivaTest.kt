package br.com.zamfir.verticalize.concurso.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneOffset

class ContagemRegressivaTest {

    private val hoje = LocalDate.of(2027, 1, 1)

    private fun millis(data: LocalDate) = data.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    @Test
    fun `prova futura retorna dias positivos`() {
        val dataProva = millis(hoje.plusDays(42))

        assertThat(calcularDiasParaProva(dataProva, hoje)).isEqualTo(42L)
    }

    @Test
    fun `prova hoje retorna zero`() {
        val dataProva = millis(hoje)

        assertThat(calcularDiasParaProva(dataProva, hoje)).isEqualTo(0L)
    }

    @Test
    fun `prova passada retorna dias negativos`() {
        val dataProva = millis(hoje.minusDays(10))

        assertThat(calcularDiasParaProva(dataProva, hoje)).isEqualTo(-10L)
    }
}
