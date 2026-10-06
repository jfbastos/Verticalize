package br.com.zamfir.verticalize.concurso.data

import br.com.zamfir.verticalize.concurso.domain.Concurso
import br.com.zamfir.verticalize.concurso.domain.ConcursoRepository
import br.com.zamfir.verticalize.core.data.IdGenerator
import br.com.zamfir.verticalize.core.domain.DataError
import br.com.zamfir.verticalize.core.domain.EmptyResult
import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.core.domain.safeCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomConcursoRepository(
    private val dao: ConcursoDao
) : ConcursoRepository {

    override fun observeConcursos(): Flow<List<Concurso>> {
        return dao.observeAll().map { entities -> entities.map { it.toConcurso() } }
    }

    override suspend fun getConcursoById(id: Long): Result<Concurso, DataError.Local> {
        return when (val result = safeCall { dao.getById(id) }) {
            is Result.Error -> result
            is Result.Success -> result.data?.toConcurso()?.let { Result.Success(it) }
                ?: Result.Error(DataError.Local.NOT_FOUND)
        }
    }

    override suspend fun upsertConcurso(concurso: Concurso): Result<Long, DataError.Local> {
        val entity = concurso.toConcursoEntity().let { it.copy(id = IdGenerator.idOuNovo(it.id)) }
        return safeCall {
            dao.upsert(entity)
            entity.id
        }
    }

    override suspend fun deleteConcurso(id: Long): EmptyResult<DataError.Local> {
        return safeCall { dao.deleteById(id) }
    }

    override suspend fun updatePercentualCompletude(id: Long, percentual: Int): EmptyResult<DataError.Local> {
        return safeCall { dao.updatePercentualCompletude(id, percentual) }
    }

    override suspend fun updateHorasEstudadas(id: Long, horas: Int): EmptyResult<DataError.Local> {
        return safeCall { dao.updateHorasEstudadas(id, horas) }
    }
}
