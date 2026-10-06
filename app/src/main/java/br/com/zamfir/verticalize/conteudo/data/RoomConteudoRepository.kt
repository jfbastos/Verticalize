package br.com.zamfir.verticalize.conteudo.data

import br.com.zamfir.verticalize.conteudo.domain.Conteudo
import br.com.zamfir.verticalize.conteudo.domain.ConteudoRepository
import br.com.zamfir.verticalize.core.data.IdGenerator
import br.com.zamfir.verticalize.core.domain.DataError
import br.com.zamfir.verticalize.core.domain.EmptyResult
import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.core.domain.safeCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomConteudoRepository(
    private val dao: ConteudoDao
) : ConteudoRepository {

    override fun observeConteudosByConcursoId(concursoId: Long): Flow<List<Conteudo>> {
        return dao.observeByConcursoId(concursoId).map { entities -> entities.map { it.toConteudo() } }
    }

    override suspend fun getConteudoById(id: Long): Result<Conteudo, DataError.Local> {
        return when (val result = safeCall { dao.getById(id) }) {
            is Result.Error -> result
            is Result.Success -> result.data?.toConteudo()?.let { Result.Success(it) }
                ?: Result.Error(DataError.Local.NOT_FOUND)
        }
    }

    override suspend fun upsertConteudo(conteudo: Conteudo): Result<Long, DataError.Local> {
        val entity = conteudo.toConteudoEntity().let { it.copy(id = IdGenerator.idOuNovo(it.id)) }
        return safeCall {
            dao.upsert(entity)
            entity.id
        }
    }

    override suspend fun setConcluido(id: Long, concluido: Boolean): EmptyResult<DataError.Local> {
        return safeCall { dao.updateConcluido(id, concluido) }
    }

    override suspend fun deleteConteudos(ids: Set<Long>): EmptyResult<DataError.Local> {
        return safeCall { dao.deleteByIds(ids) }
    }
}
