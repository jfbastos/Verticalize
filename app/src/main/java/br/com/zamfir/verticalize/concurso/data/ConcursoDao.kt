package br.com.zamfir.verticalize.concurso.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ConcursoDao {
    @Query("SELECT * FROM concursos ORDER BY dataProva IS NULL ASC, dataProva ASC")
    fun observeAll(): Flow<List<ConcursoEntity>>

    @Query("SELECT * FROM concursos WHERE id = :id")
    suspend fun getById(id: Long): ConcursoEntity?

    @Upsert
    suspend fun upsert(entity: ConcursoEntity): Long

    @Query("DELETE FROM concursos WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE concursos SET percentualCompletude = :percentual WHERE id = :id")
    suspend fun updatePercentualCompletude(id: Long, percentual: Int)

    @Query("UPDATE concursos SET horasEstudadas = :horas WHERE id = :id")
    suspend fun updateHorasEstudadas(id: Long, horas: Int)
}
