package br.com.zamfir.verticalize.concurso.data

import br.com.zamfir.verticalize.concurso.domain.Concurso
import br.com.zamfir.verticalize.concurso.domain.ConcursoRepository
import br.com.zamfir.verticalize.core.domain.DataError
import br.com.zamfir.verticalize.core.domain.EmptyResult
import br.com.zamfir.verticalize.core.domain.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeConcursoRepository : ConcursoRepository {
    private val concursosFlow = MutableStateFlow<List<Concurso>>(emptyList())
    var shouldReturnError: Boolean = false

    fun setConcursos(concursos: List<Concurso>) {
        concursosFlow.value = concursos
    }

    override fun observeConcursos(): Flow<List<Concurso>> = concursosFlow

    override suspend fun getConcursoById(id: Long): Result<Concurso, DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        val concurso = concursosFlow.value.find { it.id == id }
            ?: return Result.Error(DataError.Local.NOT_FOUND)
        return Result.Success(concurso)
    }

    override suspend fun upsertConcurso(concurso: Concurso): Result<Long, DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        val id = if (concurso.id == 0L) concursosFlow.value.size + 1L else concurso.id
        val updated = concurso.copy(id = id)
        concursosFlow.value = concursosFlow.value
            .filterNot { it.id == id }
            .plus(updated)
        return Result.Success(id)
    }

    override suspend fun deleteConcurso(id: Long): EmptyResult<DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        concursosFlow.value = concursosFlow.value.filterNot { it.id == id }
        return Result.Success(Unit)
    }

    override suspend fun updatePercentualCompletude(id: Long, percentual: Int): EmptyResult<DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        concursosFlow.value = concursosFlow.value.map {
            if (it.id == id) it.copy(percentualCompletude = percentual) else it
        }
        return Result.Success(Unit)
    }

    override suspend fun updateHorasEstudadas(id: Long, horas: Int): EmptyResult<DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        concursosFlow.value = concursosFlow.value.map {
            if (it.id == id) it.copy(horasEstudadas = horas) else it
        }
        return Result.Success(Unit)
    }
}
