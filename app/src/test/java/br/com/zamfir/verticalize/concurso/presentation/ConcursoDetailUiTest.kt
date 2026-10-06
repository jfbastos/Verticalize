package br.com.zamfir.verticalize.concurso.presentation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import br.com.zamfir.verticalize.concurso.domain.Concurso
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.core.domain.formatDate
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneOffset

class ConcursoDetailUiTest {

    @Test
    fun `sem data da prova o formatado e os dias faltantes ficam nulos`() {
        val concurso = Concurso(id = 1L, nome = "Concurso", nivel = Nivel.MEDIO, dataProva = null)

        val ui = concurso.toConcursoDetailUi()

        assertThat(ui.dataProvaFormatted).isNull()
        assertThat(ui.diasParaProva).isNull()
    }

    @Test
    fun `com data da prova o formatado e os dias faltantes sao calculados`() {
        val daquiA10Dias = LocalDate.now(ZoneOffset.UTC).plusDays(10)
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val concurso = Concurso(id = 1L, nome = "Concurso", nivel = Nivel.MEDIO, dataProva = daquiA10Dias)

        val ui = concurso.toConcursoDetailUi()

        assertThat(ui.dataProvaFormatted).isEqualTo(formatDate(daquiA10Dias))
        assertThat(ui.diasParaProva).isEqualTo(10L)
    }
}
