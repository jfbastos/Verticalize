package br.com.zamfir.verticalize.registroestudo.data

import br.com.zamfir.verticalize.core.data.IdGenerator
import br.com.zamfir.verticalize.core.domain.DataError
import br.com.zamfir.verticalize.core.domain.EmptyResult
import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.core.domain.safeCall
import br.com.zamfir.verticalize.registroestudo.domain.RegistroEstudo
import br.com.zamfir.verticalize.registroestudo.domain.RegistroEstudoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomRegistroEstudoRepository(
    private val dao: RegistroEstudoDao
) : RegistroEstudoRepository {

    override fun observeRegistrosByConcursoId(concursoId: Long): Flow<List<RegistroEstudo>> {
        return dao.observeByConcursoId(concursoId).map { entities -> entities.map { it.toRegistroEstudo() } }
    }

    override suspend fun getRegistroEstudoById(id: Long): Result<RegistroEstudo, DataError.Local> {
        return when (val result = safeCall { dao.getById(id) }) {
            is Result.Error -> result
            is Result.Success -> result.data?.toRegistroEstudo()?.let { Result.Success(it) }
                ?: Result.Error(DataError.Local.NOT_FOUND)
        }
    }

    override suspend fun upsertRegistroEstudo(registro: RegistroEstudo): Result<Long, DataError.Local> {
        val entity = registro.toRegistroEstudoEntity().let { it.copy(id = IdGenerator.idOuNovo(it.id)) }
        return safeCall {
            dao.upsert(entity)
            entity.id
        }
    }

    override suspend fun deleteRegistroEstudo(id: Long): EmptyResult<DataError.Local> {
        return safeCall { dao.deleteById(id) }
    }

    override suspend fun deleteRegistrosEstudo(ids: Set<Long>): EmptyResult<DataError.Local> {
        return safeCall { dao.deleteByIds(ids) }
    }
}
