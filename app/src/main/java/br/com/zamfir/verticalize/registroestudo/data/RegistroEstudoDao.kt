package br.com.zamfir.verticalize.registroestudo.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// Abaixo do limite de parâmetros por query do SQLite (999 nas versões mais antigas).
private const val EXCLUSAO_LOTE = 500

@Dao
interface RegistroEstudoDao {
    @Query("SELECT * FROM registros_estudo WHERE concursoId = :concursoId ORDER BY data DESC, horaInicioMinutos DESC")
    fun observeByConcursoId(concursoId: Long): Flow<List<RegistroEstudoEntity>>

    @Query("SELECT * FROM registros_estudo WHERE id = :id")
    suspend fun getById(id: Long): RegistroEstudoEntity?

    @Upsert
    suspend fun upsert(entity: RegistroEstudoEntity): Long

    @Query("DELETE FROM registros_estudo WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM registros_estudo WHERE id IN (:ids)")
    suspend fun deleteByIdsLote(ids: List<Long>)

    /** Em lotes (o SQLite limita a quantidade de parâmetros por query), mas tudo ou nada. */
    @Transaction
    suspend fun deleteByIds(ids: Collection<Long>) {
        ids.chunked(EXCLUSAO_LOTE).forEach { deleteByIdsLote(it) }
    }
}
