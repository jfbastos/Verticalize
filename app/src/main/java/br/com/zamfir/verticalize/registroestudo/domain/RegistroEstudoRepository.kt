package br.com.zamfir.verticalize.registroestudo.domain

import br.com.zamfir.verticalize.core.domain.DataError
import br.com.zamfir.verticalize.core.domain.EmptyResult
import br.com.zamfir.verticalize.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface RegistroEstudoRepository {
    fun observeRegistrosByConcursoId(concursoId: Long): Flow<List<RegistroEstudo>>
    suspend fun getRegistroEstudoById(id: Long): Result<RegistroEstudo, DataError.Local>
    suspend fun upsertRegistroEstudo(registro: RegistroEstudo): Result<Long, DataError.Local>
    suspend fun deleteRegistroEstudo(id: Long): EmptyResult<DataError.Local>
    suspend fun deleteRegistrosEstudo(ids: Set<Long>): EmptyResult<DataError.Local>
}
