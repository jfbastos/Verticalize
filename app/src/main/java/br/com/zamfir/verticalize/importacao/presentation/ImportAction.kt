package br.com.zamfir.verticalize.importacao.presentation

sealed interface ImportAction {
    data object OnConfirmClick : ImportAction
    data object OnCancelClick : ImportAction
    data object OnDoneClick : ImportAction
}
