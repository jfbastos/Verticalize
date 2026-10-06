package br.com.zamfir.verticalize.importacao.data

import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.importacao.domain.ImportFileError
import br.com.zamfir.verticalize.importacao.domain.ImportFileReader

class FakeImportFileReader : ImportFileReader {
    var text: String = ""
    var error: ImportFileError? = null

    override suspend fun readText(uri: String): Result<String, ImportFileError> {
        return error?.let { Result.Error(it) } ?: Result.Success(text)
    }
}
