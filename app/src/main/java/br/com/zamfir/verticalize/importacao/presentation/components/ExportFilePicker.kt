package br.com.zamfir.verticalize.importacao.presentation.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private const val EXPORT_FILE_MIME_TYPE = "text/plain"

/**
 * Devolve uma função que abre o seletor de destino do Android sugerindo o nome do arquivo.
 * O `Uri` escolhido pelo usuário chega em [onFileSelected]; se ele cancelar, nada é chamado.
 */
@Composable
fun rememberExportFilePicker(onFileSelected: (uri: String) -> Unit): (fileName: String) -> Unit {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(EXPORT_FILE_MIME_TYPE)
    ) { uri ->
        if (uri != null) onFileSelected(uri.toString())
    }
    return remember(launcher) { { fileName -> launcher.launch(fileName) } }
}
