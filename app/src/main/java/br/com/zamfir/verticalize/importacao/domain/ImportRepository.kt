package br.com.zamfir.verticalize.importacao.domain

import br.com.zamfir.verticalize.core.domain.DataError
import br.com.zamfir.verticalize.core.domain.Result

interface ImportRepository {
    /** Cruza o que foi lido do arquivo com o que já existe no app, sem gravar nada. */
    suspend fun analisar(parsed: ParsedImport): Result<ImportPlan, DataError.Local>

    /** Grava o [plan] de forma atômica: se algo falhar, nada é gravado. */
    suspend fun aplicar(plan: ImportPlan): Result<ImportSummary, DataError.Local>
}

interface ImportFileReader {
    /** Lê o arquivo apontado por [uri] (um `content://` ou `file://`) como texto. */
    suspend fun readText(uri: String): Result<String, ImportFileError>
}
