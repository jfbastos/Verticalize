package br.com.zamfir.verticalize.concurso.domain

import br.com.zamfir.verticalize.core.domain.DataError
import br.com.zamfir.verticalize.core.domain.EmptyResult
import br.com.zamfir.verticalize.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface ConcursoRepository {
    fun observeConcursos(): Flow<List<Concurso>>
    suspend fun getConcursoById(id: Long): Result<Concurso, DataError.Local>
    suspend fun upsertConcurso(concurso: Concurso): Result<Long, DataError.Local>
    suspend fun deleteConcurso(id: Long): EmptyResult<DataError.Local>
    suspend fun updatePercentualCompletude(id: Long, percentual: Int): EmptyResult<DataError.Local>
    suspend fun updateHorasEstudadas(id: Long, horas: Int): EmptyResult<DataError.Local>
}
