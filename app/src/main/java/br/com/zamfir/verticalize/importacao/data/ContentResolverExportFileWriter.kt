package br.com.zamfir.verticalize.importacao.data

import android.content.ContentResolver
import androidx.core.net.toUri
import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.importacao.domain.ExportError
import br.com.zamfir.verticalize.importacao.domain.ExportFileWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class ContentResolverExportFileWriter(
    private val contentResolver: ContentResolver
) : ExportFileWriter {

    override suspend fun writeText(uri: String, text: String): Result<Unit, ExportError> = withContext(Dispatchers.IO) {
        try {
            // "wt" = escrita truncando: se o arquivo já existir, o conteúdo antigo não sobra no final.
            val output = contentResolver.openOutputStream(uri.toUri(), "wt")
                ?: return@withContext Result.Error(ExportError.FALHA_AO_GRAVAR)

            output.use { it.write(text.toByteArray(Charsets.UTF_8)) }
            Result.Success(Unit)
        } catch (_: IOException) {
            Result.Error(ExportError.FALHA_AO_GRAVAR)
        } catch (_: SecurityException) {
            Result.Error(ExportError.FALHA_AO_GRAVAR)
        } catch (_: IllegalArgumentException) {
            Result.Error(ExportError.FALHA_AO_GRAVAR)
        }
    }
}
