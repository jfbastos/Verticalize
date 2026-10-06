package br.com.zamfir.verticalize.importacao.data

import android.content.ContentResolver
import androidx.core.net.toUri
import br.com.zamfir.verticalize.core.domain.Result
import br.com.zamfir.verticalize.importacao.domain.ImportFileError
import br.com.zamfir.verticalize.importacao.domain.ImportFileReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

internal const val MAX_IMPORT_FILE_BYTES = 2 * 1024 * 1024
private const val READ_BUFFER_BYTES = 8 * 1024

class ContentResolverImportFileReader(
    private val contentResolver: ContentResolver
) : ImportFileReader {

    override suspend fun readText(uri: String): Result<String, ImportFileError> = withContext(Dispatchers.IO) {
        try {
            val input = contentResolver.openInputStream(uri.toUri())
                ?: return@withContext Result.Error(ImportFileError.ARQUIVO_ILEGIVEL)

            input.use { stream ->
                val saida = ByteArrayOutputStream()
                val buffer = ByteArray(READ_BUFFER_BYTES)
                var total = 0
                while (true) {
                    val lidos = stream.read(buffer)
                    if (lidos < 0) break
                    total += lidos
                    if (total > MAX_IMPORT_FILE_BYTES) {
                        return@withContext Result.Error(ImportFileError.ARQUIVO_MUITO_GRANDE)
                    }
                    saida.write(buffer, 0, lidos)
                }
                Result.Success(decodeImportText(saida.toByteArray()))
            }
        } catch (_: IOException) {
            Result.Error(ImportFileError.ARQUIVO_ILEGIVEL)
        } catch (_: SecurityException) {
            // Permissão de leitura do documento perdida (ex.: processo recriado depois de escolher o arquivo).
            Result.Error(ImportFileError.ARQUIVO_ILEGIVEL)
        } catch (_: IllegalArgumentException) {
            Result.Error(ImportFileError.ARQUIVO_ILEGIVEL)
        }
    }
}
