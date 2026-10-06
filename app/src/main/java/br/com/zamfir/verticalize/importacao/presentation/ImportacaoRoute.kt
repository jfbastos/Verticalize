package br.com.zamfir.verticalize.importacao.presentation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** [fileUri] é o `Uri` (como texto) do arquivo escolhido pelo usuário no seletor de documentos. */
@Serializable
data class ImportacaoRoute(val fileUri: String) : NavKey
