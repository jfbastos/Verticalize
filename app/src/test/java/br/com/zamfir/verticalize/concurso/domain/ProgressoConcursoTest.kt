package br.com.zamfir.verticalize.concurso.domain

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class ProgressoConcursoTest {

    @Test
    fun `percentual por eixo e ordenado pelo numero do eixo e ignora conteudos sem eixo`() {
        val resultado = percentualPorEixo(
            listOf(
                3 to true,
                1 to true,
                1 to false,
                2 to false,
                null to true
            )
        )

        assertThat(resultado.entries.map { it.key to it.value }).isEqualTo(listOf(1 to 50, 2 to 0, 3 to 100))
    }

    @Test
    fun `sem eixos nao ha percentuais`() {
        assertThat(percentualPorEixo(emptyList())).isEmpty()
        assertThat(percentualPorEixo(listOf(null to true))).isEmpty()
    }

    @Test
    fun `percentual de um eixo usa divisao inteira`() {
        // 1 de 3 concluídos = 33% (33,33 truncado), como na tela de detalhe.
        assertThat(percentualPorEixo(listOf(1 to true, 1 to false, 1 to false))[1]).isEqualTo(33)
    }

    @Test
    fun `percentual medio arredonda e vale zero sem itens`() {
        assertThat(percentualMedio(emptyList())).isEqualTo(0)
        assertThat(percentualMedio(listOf(50, 100))).isEqualTo(75)
        assertThat(percentualMedio(listOf(33, 34))).isEqualTo(34) // 33,5 arredonda para 34
    }

    @Test
    fun `completude do concurso e a media dos percentuais por eixo`() {
        val itens = listOf(1 to true, 1 to false, 2 to true, null to true)

        assertThat(calcularPercentualCompletude(itens)).isEqualTo(75)
        assertThat(calcularPercentualCompletude(emptyList())).isEqualTo(0)
        assertThat(calcularPercentualCompletude(listOf(null to true))).isEqualTo(0)
    }

    @Test
    fun `horas estudadas arredondam os minutos totais`() {
        assertThat(calcularHorasEstudadas(0)).isEqualTo(0)
        assertThat(calcularHorasEstudadas(89)).isEqualTo(1)
        assertThat(calcularHorasEstudadas(90)).isEqualTo(2)
        assertThat(calcularHorasEstudadas(240)).isEqualTo(4)
    }
}
