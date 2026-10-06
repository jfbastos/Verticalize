package br.com.zamfir.verticalize.registroestudo.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.zamfir.verticalize.concurso.data.ConcursoEntity

@Entity(
    tableName = "registros_estudo",
    foreignKeys = [
        ForeignKey(
            entity = ConcursoEntity::class,
            parentColumns = ["id"],
            childColumns = ["concursoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("concursoId")]
)
data class RegistroEstudoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val concursoId: Long,
    val materia: String,
    val data: Long,
    val horaInicioMinutos: Int,
    val horaFimMinutos: Int
)
