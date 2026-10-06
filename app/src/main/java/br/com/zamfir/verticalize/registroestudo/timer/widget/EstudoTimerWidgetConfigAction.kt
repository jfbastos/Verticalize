package br.com.zamfir.verticalize.registroestudo.timer.widget

sealed interface EstudoTimerWidgetConfigAction {
    data class OnConcursoSelected(val concursoId: Long) : EstudoTimerWidgetConfigAction
    data class OnMateriaChanged(val value: String) : EstudoTimerWidgetConfigAction
    data object OnStartClick : EstudoTimerWidgetConfigAction
    data object OnCancelClick : EstudoTimerWidgetConfigAction
}
