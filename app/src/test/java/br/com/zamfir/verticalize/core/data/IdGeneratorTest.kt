package br.com.zamfir.verticalize.core.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isLessThan
import org.junit.jupiter.api.Test
import kotlin.random.Random

class IdGeneratorTest {

    @Test
    fun `ids are strictly increasing even within the same millisecond`() {
        val gerador = IdGenerator()
        val ids = List(1_000) { gerador.novoId(nowMillis = 1_800_000_000_000L) }

        ids.zipWithNext().forEach { (anterior, proximo) -> assertThat(proximo).isGreaterThan(anterior) }
    }

    @Test
    fun `ids stay below 2 to the 53 so the realtime database keeps them exact`() {
        // Ano 2100, com folga para a vida útil do app.
        val id = IdGenerator().novoId(nowMillis = 4_102_444_800_000L)

        assertThat(id).isLessThan(1L shl 53)
    }

    @Test
    fun `ids carry the creation time in the high bits`() {
        val agora = 1_800_000_000_000L
        val aparelhoA = IdGenerator().novoId(nowMillis = agora, random = Random(1))
        val aparelhoB = IdGenerator().novoId(nowMillis = agora + 1_000, random = Random(2))

        assertThat(aparelhoB shr 10).isEqualTo(agora + 1_000)
        assertThat(aparelhoA shr 10).isEqualTo(agora)
    }

    @Test
    fun `existing ids are kept and new records get a generated id`() {
        assertThat(IdGenerator.idOuNovo(42L)).isEqualTo(42L)
        assertThat(IdGenerator.idOuNovo(0L)).isGreaterThan(0L)
    }
}
