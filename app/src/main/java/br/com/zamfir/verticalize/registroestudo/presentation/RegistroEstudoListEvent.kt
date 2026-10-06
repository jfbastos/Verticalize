package br.com.zamfir.verticalize.registroestudo.presentation

import br.com.zamfir.verticalize.core.presentation.UiText

sealed interface RegistroEstudoListEvent {
    data class ShowError(val message: UiText) : RegistroEstudoListEvent
    data class ShowMessage(val message: UiText) : RegistroEstudoListEvent
}
