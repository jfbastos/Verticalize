package br.com.zamfir.verticalize.importacao.presentation

import br.com.zamfir.verticalize.core.presentation.UiText

sealed interface ImportEvent {
    data class ShowError(val message: UiText) : ImportEvent
    data object NavigateBack : ImportEvent
}
