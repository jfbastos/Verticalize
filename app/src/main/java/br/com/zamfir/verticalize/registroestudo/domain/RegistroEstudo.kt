package br.com.zamfir.verticalize.registroestudo.domain

data class RegistroEstudo(
    val id: Long = 0L,
    val concursoId: Long,
    val materia: String,
    val data: Long,
    val horaInicioMinutos: Int,
    val horaFimMinutos: Int
)
