package br.com.zamfir.verticalize.registroestudo.presentation

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class RegistroEstudoListStateTest {

    @Test
    fun `isSaveEnabled false com materia em branco`() {
        val state = RegistroEstudoListState(
            materia = "  ",
            dataMillis = 1_700_000_000_000L,
            horaInicioMinutos = 480,
            horaFimMinutos = 600
        )

        assertThat(state.isSaveEnabled).isFalse()
    }

    @Test
    fun `isSaveEnabled false com data nula`() {
        val state = RegistroEstudoListState(
            materia = "Direito Administrativo",
            dataMillis = null,
            horaInicioMinutos = 480,
            horaFimMinutos = 600
        )

        assertThat(state.isSaveEnabled).isFalse()
    }

    @Test
    fun `isSaveEnabled false quando hora fim igual ou menor que hora inicio`() {
        val igual = RegistroEstudoListState(
            materia = "Direito Administrativo",
            dataMillis = 1_700_000_000_000L,
            horaInicioMinutos = 600,
            horaFimMinutos = 600
        )
        val menor = igual.copy(horaFimMinutos = 500)

        assertThat(igual.isSaveEnabled).isFalse()
        assertThat(menor.isSaveEnabled).isFalse()
    }

    @Test
    fun `isSaveEnabled true com todos os campos validos`() {
        val state = RegistroEstudoListState(
            materia = "Direito Administrativo",
            dataMillis = 1_700_000_000_000L,
            horaInicioMinutos = 480,
            horaFimMinutos = 600
        )

        assertThat(state.isSaveEnabled).isTrue()
    }
}
