package br.com.zamfir.verticalize.importacao.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import br.com.zamfir.verticalize.concurso.data.ConcursoEntity
import br.com.zamfir.verticalize.conteudo.data.ConteudoEntity
import br.com.zamfir.verticalize.core.data.IdGenerator
import br.com.zamfir.verticalize.registroestudo.data.RegistroEstudoEntity

/**
 * O que gravar para um concurso do arquivo. Se [existingId] não for nulo o concurso já existe e só
 * recebe [conteudos] e [estudos] (com `concursoId` ignorado, preenchido na gravação); senão [concurso]
 * é inserido antes. Em [concurso] vêm o percentual e as horas já calculados pelo planner.
 */
class ConcursoImportEntities(
    val existingId: Long?,
    val concurso: ConcursoEntity,
    val conteudos: List<ConteudoEntity>,
    val estudos: List<RegistroEstudoEntity> = emptyList()
)

/** Consultas e gravações em lote usadas pela importação e pela exportação de concursos. */
@Dao
abstract class ImportDao {
    @Query("SELECT * FROM concursos")
    abstract suspend fun getAllConcursos(): List<ConcursoEntity>

    @Query("SELECT * FROM conteudos")
    abstract suspend fun getAllConteudos(): List<ConteudoEntity>

    @Query("SELECT * FROM registros_estudo")
    abstract suspend fun getAllEstudos(): List<RegistroEstudoEntity>

    @Query("SELECT * FROM concursos WHERE id = :id")
    abstract suspend fun getConcursoById(id: Long): ConcursoEntity?

    @Query("SELECT * FROM conteudos WHERE concursoId = :concursoId ORDER BY id ASC")
    abstract suspend fun getConteudosByConcursoId(concursoId: Long): List<ConteudoEntity>

    @Query("SELECT * FROM registros_estudo WHERE concursoId = :concursoId ORDER BY data ASC, horaInicioMinutos ASC")
    abstract suspend fun getEstudosByConcursoId(concursoId: Long): List<RegistroEstudoEntity>

    @Insert
    abstract suspend fun insertConcurso(entity: ConcursoEntity): Long

    @Insert
    abstract suspend fun insertConteudos(entities: List<ConteudoEntity>)

    @Insert
    abstract suspend fun insertEstudos(entities: List<RegistroEstudoEntity>)

    @Query("UPDATE concursos SET percentualCompletude = :percentual WHERE id = :id")
    abstract suspend fun updatePercentualCompletude(id: Long, percentual: Int)

    @Query("UPDATE concursos SET horasEstudadas = :horas WHERE id = :id")
    abstract suspend fun updateHorasEstudadas(id: Long, horas: Int)

    /** Tudo numa única transação: se qualquer inserção falhar, nada do arquivo é gravado. */
    @Transaction
    open suspend fun aplicar(itens: List<ConcursoImportEntities>) {
        // Ids gerados aqui (e não pelo autoincremento) para serem únicos entre aparelhos; ver IdGenerator.
        for (item in itens) {
            val concursoId = item.existingId
                ?: insertConcurso(item.concurso.copy(id = IdGenerator.idOuNovo(item.concurso.id)))

            if (item.conteudos.isNotEmpty()) {
                insertConteudos(
                    item.conteudos.map { it.copy(id = IdGenerator.idOuNovo(it.id), concursoId = concursoId) }
                )
                if (item.existingId != null) {
                    updatePercentualCompletude(concursoId, item.concurso.percentualCompletude)
                }
            }
            if (item.estudos.isNotEmpty()) {
                insertEstudos(
                    item.estudos.map { it.copy(id = IdGenerator.idOuNovo(it.id), concursoId = concursoId) }
                )
                if (item.existingId != null) {
                    updateHorasEstudadas(concursoId, item.concurso.horasEstudadas)
                }
            }
        }
    }
}
