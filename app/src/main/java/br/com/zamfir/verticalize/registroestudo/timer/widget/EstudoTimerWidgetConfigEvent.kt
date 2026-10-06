package br.com.zamfir.verticalize.registroestudo.timer.widget

sealed interface EstudoTimerWidgetConfigEvent {
    data object Finish : EstudoTimerWidgetConfigEvent
}
