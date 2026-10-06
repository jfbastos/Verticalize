package br.com.zamfir.verticalize.conteudo.data

import br.com.zamfir.verticalize.conteudo.domain.Conteudo
import br.com.zamfir.verticalize.conteudo.domain.ConteudoRepository
import br.com.zamfir.verticalize.core.domain.DataError
import br.com.zamfir.verticalize.core.domain.EmptyResult
import br.com.zamfir.verticalize.core.domain.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeConteudoRepository : ConteudoRepository {
    private val conteudosFlow = MutableStateFlow<List<Conteudo>>(emptyList())
    var shouldReturnError: Boolean = false

    fun setConteudos(conteudos: List<Conteudo>) {
        conteudosFlow.value = conteudos
    }

    override fun observeConteudosByConcursoId(concursoId: Long): Flow<List<Conteudo>> =
        conteudosFlow.map { conteudos -> conteudos.filter { it.concursoId == concursoId } }

    override suspend fun getConteudoById(id: Long): Result<Conteudo, DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        val conteudo = conteudosFlow.value.find { it.id == id }
            ?: return Result.Error(DataError.Local.NOT_FOUND)
        return Result.Success(conteudo)
    }

    override suspend fun upsertConteudo(conteudo: Conteudo): Result<Long, DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        val id = if (conteudo.id == 0L) conteudosFlow.value.size + 1L else conteudo.id
        val updated = conteudo.copy(id = id)
        conteudosFlow.value = conteudosFlow.value
            .filterNot { it.id == id }
            .plus(updated)
        return Result.Success(id)
    }

    override suspend fun setConcluido(id: Long, concluido: Boolean): EmptyResult<DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        conteudosFlow.value = conteudosFlow.value.map {
            if (it.id == id) it.copy(concluido = concluido) else it
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteConteudos(ids: Set<Long>): EmptyResult<DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        conteudosFlow.value = conteudosFlow.value.filterNot { it.id in ids }
        return Result.Success(Unit)
    }
}
