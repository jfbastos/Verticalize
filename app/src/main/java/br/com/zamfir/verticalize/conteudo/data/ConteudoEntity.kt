package br.com.zamfir.verticalize.conteudo.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.zamfir.verticalize.concurso.data.ConcursoEntity

@Entity(
    tableName = "conteudos",
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
data class ConteudoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val concursoId: Long,
    val materia: String = "",
    val descricao: String,
    val eixo: Int?,
    val bloco: Int?,
    val quantidadeAulas: Int = 0,
    val tempoMedioAulaMinutos: Int = 0,
    val concluido: Boolean = false,
    val dataUltimaRevisao: Long?,
    val quantidadeQuestoesRealizadas: Int = 0,
    val prioridade: String = "MEDIA"
)
