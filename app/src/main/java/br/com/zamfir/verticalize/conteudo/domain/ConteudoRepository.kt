package br.com.zamfir.verticalize.conteudo.domain

import br.com.zamfir.verticalize.core.domain.DataError
import br.com.zamfir.verticalize.core.domain.EmptyResult
import br.com.zamfir.verticalize.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface ConteudoRepository {
    fun observeConteudosByConcursoId(concursoId: Long): Flow<List<Conteudo>>
    suspend fun getConteudoById(id: Long): Result<Conteudo, DataError.Local>
    suspend fun upsertConteudo(conteudo: Conteudo): Result<Long, DataError.Local>
    suspend fun setConcluido(id: Long, concluido: Boolean): EmptyResult<DataError.Local>
    suspend fun deleteConteudos(ids: Set<Long>): EmptyResult<DataError.Local>
}
