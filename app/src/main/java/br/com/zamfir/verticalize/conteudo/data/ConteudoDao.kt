package br.com.zamfir.verticalize.conteudo.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// Abaixo do limite de parâmetros por query do SQLite (999 nas versões mais antigas).
private const val EXCLUSAO_LOTE = 500

@Dao
interface ConteudoDao {
    @Query("SELECT * FROM conteudos WHERE concursoId = :concursoId ORDER BY id ASC")
    fun observeByConcursoId(concursoId: Long): Flow<List<ConteudoEntity>>

    @Query("SELECT * FROM conteudos WHERE id = :id")
    suspend fun getById(id: Long): ConteudoEntity?

    @Upsert
    suspend fun upsert(entity: ConteudoEntity): Long

    @Query("UPDATE conteudos SET concluido = :concluido WHERE id = :id")
    suspend fun updateConcluido(id: Long, concluido: Boolean)

    @Query("DELETE FROM conteudos WHERE id IN (:ids)")
    suspend fun deleteByIdsLote(ids: List<Long>)

    /** Em lotes (o SQLite limita a quantidade de parâmetros por query), mas tudo ou nada. */
    @Transaction
    suspend fun deleteByIds(ids: Collection<Long>) {
        ids.chunked(EXCLUSAO_LOTE).forEach { deleteByIdsLote(it) }
    }
}
