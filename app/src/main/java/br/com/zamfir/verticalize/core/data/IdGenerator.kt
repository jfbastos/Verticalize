package br.com.zamfir.verticalize.core.data

import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

/**
 * Gera ids de registros únicos entre aparelhos, para a sincronização com a nuvem não misturar o
 * concurso "5" de um aparelho com o "5" de outro (o autoincremento do SQLite é por aparelho).
 *
 * Formato: `(epochMillis << 10) | 10 bits aleatórios`. Os ids crescem com o tempo, então `ORDER BY id`
 * continua sendo a ordem de criação, e ficam abaixo de 2^53 (o Realtime Database guarda números como
 * double). Dois aparelhos só colidem criando no mesmo milissegundo com os mesmos 10 bits sorteados.
 *
 * O app usa a instância compartilhada pelo companion ([IdGenerator.idOuNovo]); instâncias próprias
 * existem para os testes não dividirem estado.
 */
class IdGenerator {
    private val ultimo = AtomicLong(0L)

    fun novoId(nowMillis: Long = System.currentTimeMillis(), random: Random = Random.Default): Long {
        val candidato = (nowMillis shl BITS_ALEATORIOS) or random.nextLong(1L shl BITS_ALEATORIOS)
        // Garante ids estritamente crescentes mesmo para vários registros criados no mesmo milissegundo
        // (ex.: importação), preservando a ordem de inserção.
        return ultimo.updateAndGet { anterior -> maxOf(candidato, anterior + 1) }
    }

    companion object {
        private const val BITS_ALEATORIOS = 10
        private val compartilhado = IdGenerator()

        /** O próprio [id] se o registro já existe; um id novo se ainda vai ser criado (id 0). */
        fun idOuNovo(id: Long): Long = if (id == 0L) compartilhado.novoId() else id
    }
}
