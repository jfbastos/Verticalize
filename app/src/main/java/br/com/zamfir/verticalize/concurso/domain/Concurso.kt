package br.com.zamfir.verticalize.concurso.domain

data class Concurso(
    val id: Long = 0L,
    val nome: String,
    val nivel: Nivel,
    val dataProva: Long? = null,
    val valorInscricaoCentavos: Long = 0L,
    val banca: String = "",
    val horasEstudadas: Int = 0,
    val percentualCompletude: Int = 0
)
