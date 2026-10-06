package br.com.zamfir.verticalize.core.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.coroutines.cancellation.CancellationException

class SafeCallTest {

    @Test
    fun `devolve sucesso com o valor da acao`() = runTest {
        assertThat(safeCall { 42 }).isEqualTo(Result.Success(42))
    }

    @Test
    fun `excecao comum vira erro desconhecido`() = runTest {
        val resultado = safeCall<Int> { throw IllegalStateException("falhou") }

        assertThat(resultado).isEqualTo(Result.Error(DataError.Local.UNKNOWN))
    }

    @Test
    fun `cancelamento e repassado e nao vira erro`() = runTest {
        var repassou = false

        try {
            safeCall<Int> { throw CancellationException("cancelado") }
        } catch (_: CancellationException) {
            repassou = true
        }

        assertThat(repassou).isTrue()
    }
}
