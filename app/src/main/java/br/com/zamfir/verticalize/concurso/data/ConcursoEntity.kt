package br.com.zamfir.verticalize.concurso.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "concursos")
data class ConcursoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nome: String,
    val nivel: String,
    val dataProva: Long? = null,
    val valorInscricaoCentavos: Long = 0L,
    val banca: String = "",
    val horasEstudadas: Int = 0,
    val percentualCompletude: Int = 0
)
