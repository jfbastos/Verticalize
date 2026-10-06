package br.com.zamfir.verticalize.importacao.data

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

private val WINDOWS_1252: Charset = Charset.forName("windows-1252")

/**
 * Decodifica os bytes do arquivo como UTF-8 (o padrão dos editores atuais). Se houver bytes que não
 * formam UTF-8 válido, assume Windows-1252, típico de .txt/.csv salvos pelo Bloco de Notas/Excel no Windows.
 * O BOM, se existir, é removido pelo parser.
 */
internal fun decodeImportText(bytes: ByteArray): String {
    val utf8 = Charsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT)

    return try {
        utf8.decode(ByteBuffer.wrap(bytes)).toString()
    } catch (_: CharacterCodingException) {
        String(bytes, WINDOWS_1252)
    }
}
