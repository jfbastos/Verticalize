package br.com.zamfir.verticalize.importacao.domain

import br.com.zamfir.verticalize.core.domain.DataError
import br.com.zamfir.verticalize.core.domain.Error
import br.com.zamfir.verticalize.core.domain.Result

enum class ExportError : Error {
    SEM_DADOS,
    FALHA_AO_LER,
    FALHA_AO_GRAVAR
}

data class ExportSummary(
    val concursos: Int,
    val conteudos: Int,
    val estudos: Int = 0
)

interface ExportRepository {
    /**
     * Todos os concursos com seus conteúdos, na forma do arquivo (o mesmo modelo que a importação lê),
     * ordenados por data da prova.
     */
    suspend fun carregarConcursos(): Result<List<ImportedConcurso>, DataError.Local>

    /** Um único concurso com seus conteúdos; [DataError.Local.NOT_FOUND] se ele não existir mais. */
    suspend fun carregarConcurso(concursoId: Long): Result<ImportedConcurso, DataError.Local>
}

interface ExportFileWriter {
    /** Grava [text] no arquivo apontado por [uri] (um `content://` escolhido pelo usuário), substituindo o conteúdo. */
    suspend fun writeText(uri: String, text: String): Result<Unit, ExportError>
}
