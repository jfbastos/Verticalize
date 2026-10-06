package br.com.zamfir.verticalize.importacao.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThanOrEqualTo
import org.junit.jupiter.api.Test

class ExportFileNameTest {

    @Test
    fun `remove acentos simbolos e espacos`() {
        assertThat(nomeDoArquivoDeExportacao("TRT 2ª Região - Analista Judiciário"))
            .isEqualTo("verticalize-trt-2-regiao-analista-judiciario.txt")
    }

    @Test
    fun `nome so com simbolos usa o nome padrao`() {
        assertThat(nomeDoArquivoDeExportacao("¿¡?!")).isEqualTo("verticalize-concurso.txt")
        assertThat(nomeDoArquivoDeExportacao("   ")).isEqualTo("verticalize-concurso.txt")
    }

    @Test
    fun `nomes longos sao truncados sem terminar em hifen`() {
        val nome = nomeDoArquivoDeExportacao("Concurso Público de Provas e Títulos para o Cargo de Analista Judiciário Área Judiciária")

        val slug = nome.removePrefix("verticalize-").removeSuffix(".txt")
        assertThat(slug.length).isLessThanOrEqualTo(40)
        assertThat(slug.endsWith("-")).isEqualTo(false)
        assertThat(slug.startsWith("concurso-publico-de-provas-e-titulos")).isEqualTo(true)
    }
}
