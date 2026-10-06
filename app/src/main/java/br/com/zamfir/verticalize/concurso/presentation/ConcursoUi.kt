package br.com.zamfir.verticalize.concurso.presentation

import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.concurso.domain.Concurso
import br.com.zamfir.verticalize.concurso.domain.Nivel

data class ConcursoUi(
    val id: Long,
    val nome: String,
    val nivel: Nivel,
    val horasEstudadas: Int,
    val percentualCompletude: Int
)

fun Concurso.toConcursoUi(): ConcursoUi {
    return ConcursoUi(
        id = id,
        nome = nome,
        nivel = nivel,
        horasEstudadas = horasEstudadas,
        percentualCompletude = percentualCompletude
    )
}

fun Nivel.labelRes(): Int {
    return when (this) {
        Nivel.MEDIO -> R.string.nivel_medio
        Nivel.SUPERIOR -> R.string.nivel_superior
    }
}
