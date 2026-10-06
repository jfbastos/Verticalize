package br.com.zamfir.verticalize.importacao.domain

import br.com.zamfir.verticalize.core.domain.Result

/** Lê os concursos, formata no mesmo formato da importação e grava no arquivo escolhido. */
class ConcursoExporter(
    private val repository: ExportRepository,
    private val formatter: TxtSectionsFormatter,
    private val fileWriter: ExportFileWriter
) {

    /** Exporta todos os concursos cadastrados. */
    suspend fun exportar(uri: String): Result<ExportSummary, ExportError> {
        val concursos = when (val resultado = repository.carregarConcursos()) {
            is Result.Error -> return Result.Error(ExportError.FALHA_AO_LER)
            is Result.Success -> resultado.data
        }
        if (concursos.isEmpty()) return Result.Error(ExportError.SEM_DADOS)

        return gravar(concursos, uri)
    }

    /** Exporta apenas o concurso [concursoId], no mesmo formato (um único bloco `[CONCURSO]`). */
    suspend fun exportarConcurso(concursoId: Long, uri: String): Result<ExportSummary, ExportError> {
        val concurso = when (val resultado = repository.carregarConcurso(concursoId)) {
            is Result.Error -> return Result.Error(ExportError.FALHA_AO_LER)
            is Result.Success -> resultado.data
        }

        return gravar(listOf(concurso), uri)
    }

    private suspend fun gravar(concursos: List<ImportedConcurso>, uri: String): Result<ExportSummary, ExportError> {
        return when (val gravacao = fileWriter.writeText(uri, formatter.format(concursos))) {
            is Result.Error -> Result.Error(gravacao.error)
            is Result.Success -> Result.Success(
                ExportSummary(
                    concursos = concursos.size,
                    conteudos = concursos.sumOf { it.conteudos.size },
                    estudos = concursos.sumOf { it.estudos.size }
                )
            )
        }
    }
}
