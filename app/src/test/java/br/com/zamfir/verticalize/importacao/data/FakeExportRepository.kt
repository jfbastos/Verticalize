package br.com.zamfir.verticalize.importacao.data

import br.com.zamfir.verticalize.core.domain.DataError
import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.importacao.domain.ExportRepository
import br.com.zamfir.verticalize.importacao.domain.ImportedConcurso

class FakeExportRepository : ExportRepository {
    var concursos: List<ImportedConcurso> = emptyList()
    var concursosPorId: Map<Long, ImportedConcurso> = emptyMap()
    var shouldReturnError: Boolean = false

    override suspend fun carregarConcursos(): Result<List<ImportedConcurso>, DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        return Result.Success(concursos)
    }

    override suspend fun carregarConcurso(concursoId: Long): Result<ImportedConcurso, DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        val concurso = concursosPorId[concursoId] ?: return Result.Error(DataError.Local.NOT_FOUND)
        return Result.Success(concurso)
    }
}
