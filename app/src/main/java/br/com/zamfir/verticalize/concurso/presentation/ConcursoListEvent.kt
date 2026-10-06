package br.com.zamfir.verticalize.concurso.presentation

import br.com.zamfir.verticalize.core.presentation.UiText

sealed interface ConcursoListEvent {
    data class ShowError(val message: UiText) : ConcursoListEvent
    data class ShowMessage(val message: UiText) : ConcursoListEvent
    data class NavigateToDetail(val concursoId: Long) : ConcursoListEvent
    data object OpenImportPicker : ConcursoListEvent

    /** Abre o seletor de destino do Android, já sugerindo [fileName] como nome do arquivo. */
    data class OpenExportPicker(val fileName: String) : ConcursoListEvent

    /** Abre o seletor de contas Google do sistema, que precisa do contexto da Activity. */
    data object LaunchGoogleSignIn : ConcursoListEvent
}
