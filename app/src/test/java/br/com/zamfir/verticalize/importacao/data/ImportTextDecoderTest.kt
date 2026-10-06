package br.com.zamfir.verticalize.importacao.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class ImportTextDecoderTest {

    @Test
    fun `decodifica UTF-8 valido`() {
        val bytes = "Concurso: ação e coração".toByteArray(Charsets.UTF_8)

        assertThat(decodeImportText(bytes)).isEqualTo("Concurso: ação e coração")
    }

    @Test
    fun `cai para Windows-1252 quando os bytes nao sao UTF-8 valido`() {
        val bytes = "Concurso: ação e coração".toByteArray(charset("windows-1252"))

        assertThat(decodeImportText(bytes)).isEqualTo("Concurso: ação e coração")
    }

    @Test
    fun `texto ASCII e igual nas duas codificacoes`() {
        assertThat(decodeImportText("nome: TRT".toByteArray())).isEqualTo("nome: TRT")
    }
}
