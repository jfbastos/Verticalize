package br.com.zamfir.verticalize.registroestudo.timer.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import br.com.zamfir.verticalize.registroestudo.timer.EstudoTimerService

/**
 * Cada ação só repassa para o [EstudoTimerService] (dono real do estado); quem atualiza o widget
 * depois é o próprio serviço, no momento em que o estado muda de fato — ver seus pontos de chamada
 * a [EstudoTimerGlanceWidget.updateAll]. Atualizar aqui seria prematuro, já que o serviço processa a
 * ação de forma assíncrona.
 */
class EstudoTimerWidgetPauseAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        EstudoTimerService.pause(context)
    }
}

class EstudoTimerWidgetResumeAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        EstudoTimerService.resume(context)
    }
}

class EstudoTimerWidgetRegistrarAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        EstudoTimerService.registrar(context)
    }
}

class EstudoTimerWidgetDescartarAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        EstudoTimerService.descartar(context)
    }
}
