package br.com.zamfir.verticalize.importacao.domain

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Garante que os arquivos em `docs/` (usados no teste manual) continuam válidos para o parser
 * e que os erros propositais do arquivo "com-erros" são exatamente os esperados.
 */
class ArquivosDeExemploTest {

    private val parser = TxtSectionsParser()

    // O diretório de trabalho dos testes unitários é o módulo (app/); docs/ fica na raiz do projeto.
    private fun lerExemplo(nome: String): String {
        val arquivo = listOf(File("docs/$nome"), File("../docs/$nome")).first { it.exists() }
        return arquivo.readText(Charsets.UTF_8)
    }

    @Test
    fun `exemplo-importacao txt e valido e traz dois concursos`() {
        val parsed = parser.parse(lerExemplo("exemplo-importacao.txt"))

        assertThat(parsed.errors).isEmpty()
        assertThat(parsed.concursos).hasSize(2)
        assertThat(parsed.concursos.map { it.conteudos.size }).isEqualTo(listOf(6, 3))
        assertThat(parsed.concursos.map { it.estudos.size }).isEqualTo(listOf(4, 0))
        assertThat(parsed.concursos[1].valorInscricaoCentavos).isEqualTo(120050L)
    }

    @Test
    fun `exemplo-importacao-com-erros txt reporta exatamente as linhas com erro proposital`() {
        val parsed = parser.parse(lerExemplo("exemplo-importacao-com-erros.txt"))

        assertThat(parsed.concursos.map { it.nome })
            .isEqualTo(listOf("Concurso de Teste", "Concurso com horários de estudo"))
        assertThat(parsed.concursos[0].conteudos.map { it.descricao }).isEqualTo(listOf("Teoria do crime"))
        assertThat(parsed.concursos[1].estudos.map { it.materia }).isEqualTo(listOf("Direito Penal"))
        assertThat(parsed.errors.map { it.linha to it.motivo }).isEqualTo(
            listOf(
                12 to ImportLineReason.EIXO_INVALIDO,
                13 to ImportLineReason.DESCRICAO_OBRIGATORIA,
                14 to ImportLineReason.REVISAO_INVALIDA,
                15 to ImportLineReason.CONCLUIDO_INVALIDO,
                16 to ImportLineReason.PRIORIDADE_INVALIDA,
                17 to ImportLineReason.MUITOS_CAMPOS,
                22 to ImportLineReason.DATA_PROVA_INVALIDA,
                35 to ImportLineReason.ESTUDO_INTERVALO_INVALIDO,
                36 to ImportLineReason.ESTUDO_DATA_INVALIDA,
                37 to ImportLineReason.ESTUDO_HORA_INICIO_INVALIDA,
                38 to ImportLineReason.ESTUDO_MATERIA_OBRIGATORIA
            )
        )
    }
}
