package br.com.zamfir.verticalize.registroestudo.data

import br.com.zamfir.verticalize.core.domain.DataError
import br.com.zamfir.verticalize.core.domain.EmptyResult
import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.registroestudo.domain.RegistroEstudo
import br.com.zamfir.verticalize.registroestudo.domain.RegistroEstudoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRegistroEstudoRepository : RegistroEstudoRepository {
    private val registrosFlow = MutableStateFlow<List<RegistroEstudo>>(emptyList())
    var shouldReturnError: Boolean = false

    fun setRegistros(registros: List<RegistroEstudo>) {
        registrosFlow.value = registros
    }

    override fun observeRegistrosByConcursoId(concursoId: Long): Flow<List<RegistroEstudo>> =
        registrosFlow.map { registros -> registros.filter { it.concursoId == concursoId } }

    override suspend fun getRegistroEstudoById(id: Long): Result<RegistroEstudo, DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        val registro = registrosFlow.value.find { it.id == id }
            ?: return Result.Error(DataError.Local.NOT_FOUND)
        return Result.Success(registro)
    }

    override suspend fun upsertRegistroEstudo(registro: RegistroEstudo): Result<Long, DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        val id = if (registro.id == 0L) registrosFlow.value.size + 1L else registro.id
        val updated = registro.copy(id = id)
        registrosFlow.value = registrosFlow.value
            .filterNot { it.id == id }
            .plus(updated)
        return Result.Success(id)
    }

    override suspend fun deleteRegistroEstudo(id: Long): EmptyResult<DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        registrosFlow.value = registrosFlow.value.filterNot { it.id == id }
        return Result.Success(Unit)
    }

    override suspend fun deleteRegistrosEstudo(ids: Set<Long>): EmptyResult<DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        registrosFlow.value = registrosFlow.value.filterNot { it.id in ids }
        return Result.Success(Unit)
    }
}
