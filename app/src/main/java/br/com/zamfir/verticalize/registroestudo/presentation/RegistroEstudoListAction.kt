package br.com.zamfir.verticalize.registroestudo.presentation

sealed interface RegistroEstudoListAction {
    data object OnFabClick : RegistroEstudoListAction
    data object OnDismissDialog : RegistroEstudoListAction
    data class OnMateriaChanged(val value: String) : RegistroEstudoListAction
    data object OnDataFieldClick : RegistroEstudoListAction
    data class OnDataPickerConfirm(val millis: Long) : RegistroEstudoListAction
    data object OnDataPickerDismiss : RegistroEstudoListAction
    data class OnHoraInicioChanged(val minutos: Int) : RegistroEstudoListAction
    data class OnHoraFimChanged(val minutos: Int) : RegistroEstudoListAction
    data object OnSaveClick : RegistroEstudoListAction

    data class OnEditSwipe(val id: Long) : RegistroEstudoListAction
    data class OnDeleteSwipe(val id: Long) : RegistroEstudoListAction
    data object OnDeleteConfirm : RegistroEstudoListAction
    data object OnDeleteDismiss : RegistroEstudoListAction

    data class OnRegistroTapped(val id: Long) : RegistroEstudoListAction
    data class OnRegistroLongPress(val id: Long) : RegistroEstudoListAction
    data object OnSelecaoCancelar : RegistroEstudoListAction
    data object OnSelecionarTodosClick : RegistroEstudoListAction
    data object OnExcluirSelecionadosClick : RegistroEstudoListAction
    data object OnExcluirSelecionadosConfirm : RegistroEstudoListAction
    data object OnExcluirSelecionadosDismiss : RegistroEstudoListAction

    data class OnViewModeChanged(val viewMode: RegistroEstudoViewMode) : RegistroEstudoListAction
    data object OnCalendarPreviousMonthClick : RegistroEstudoListAction
    data object OnCalendarNextMonthClick : RegistroEstudoListAction

    data object OnRegistroOpcaoDismiss : RegistroEstudoListAction
    data object OnRegistroManualOptionClick : RegistroEstudoListAction
    data object OnRegistroCronometroOptionClick : RegistroEstudoListAction

    data class OnTimerMateriaChanged(val value: String) : RegistroEstudoListAction
    data object OnTimerStartDismiss : RegistroEstudoListAction
    data object OnTimerStartConfirm : RegistroEstudoListAction
    data object OnTimerPauseClick : RegistroEstudoListAction
    data object OnTimerResumeClick : RegistroEstudoListAction
    data object OnTimerRegistrarClick : RegistroEstudoListAction
    data object OnTimerDescartarClick : RegistroEstudoListAction
}
