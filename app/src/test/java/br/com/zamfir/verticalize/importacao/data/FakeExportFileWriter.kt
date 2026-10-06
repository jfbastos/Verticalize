package br.com.zamfir.verticalize.importacao.data

import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.importacao.domain.ExportError
import br.com.zamfir.verticalize.importacao.domain.ExportFileWriter

class FakeExportFileWriter : ExportFileWriter {
    var shouldFail: Boolean = false

    var lastUri: String? = null
        private set
    var lastText: String? = null
        private set

    override suspend fun writeText(uri: String, text: String): Result<Unit, ExportError> {
        if (shouldFail) return Result.Error(ExportError.FALHA_AO_GRAVAR)
        lastUri = uri
        lastText = text
        return Result.Success(Unit)
    }
}
