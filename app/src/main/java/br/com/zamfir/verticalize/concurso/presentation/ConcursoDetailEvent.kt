package br.com.zamfir.verticalize.concurso.presentation

import br.com.zamfir.verticalize.core.presentation.UiText

sealed interface ConcursoDetailEvent {
    data class ShowError(val message: UiText) : ConcursoDetailEvent
    data class ShowMessage(val message: UiText) : ConcursoDetailEvent

    data class OpenExportPicker(val fileName: String) : ConcursoDetailEvent
}
