package br.com.zamfir.verticalize.registroestudo.timer.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** `<receiver>` real registrado no Manifest; só expõe a instância do widget Glance. */
class EstudoTimerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EstudoTimerGlanceWidget()
}
