package br.com.zamfir.verticalize.registroestudo.timer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.glance.appwidget.updateAll
import br.com.zamfir.verticalize.MainActivity
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.registroestudo.domain.RegistroEstudo
import br.com.zamfir.verticalize.registroestudo.domain.RegistroEstudoRepository
import br.com.zamfir.verticalize.registroestudo.timer.widget.EstudoTimerGlanceWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

private const val MINUTOS_POR_DIA = 24 * 60
private const val NOTIFICATION_ID = 1001

private const val ACTION_START = "br.com.zamfir.verticalize.registroestudo.timer.START"
private const val ACTION_PAUSE = "br.com.zamfir.verticalize.registroestudo.timer.PAUSE"
private const val ACTION_RESUME = "br.com.zamfir.verticalize.registroestudo.timer.RESUME"
private const val ACTION_REGISTRAR = "br.com.zamfir.verticalize.registroestudo.timer.REGISTRAR"
private const val ACTION_DESCARTAR = "br.com.zamfir.verticalize.registroestudo.timer.DESCARTAR"

private const val EXTRA_CONCURSO_ID = "concursoId"
private const val EXTRA_MATERIA = "materia"

/**
 * Foreground Service do cronômetro de estudo. O estado real mora em [EstudoTimerState]; este
 * serviço só reage às ações (start/pause/resume/registrar/descartar), mantém a notificação em dia
 * e, ao registrar, grava o [RegistroEstudo] resultante. Ficar em primeiro plano é o que garante que
 * a contagem continua mesmo com a Activity fechada — só termina quando o próprio serviço for parado.
 */
class EstudoTimerService : Service() {

    private val repository: RegistroEstudoRepository by inject()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            if (EstudoTimerState.snapshot.value.status == EstudoTimerStatus.IDLE) stopSelf()
            return START_STICKY
        }

        when (intent.action) {
            ACTION_START -> onStart(
                concursoId = intent.getLongExtra(EXTRA_CONCURSO_ID, 0L),
                materia = intent.getStringExtra(EXTRA_MATERIA).orEmpty()
            )
            ACTION_PAUSE -> onPause()
            ACTION_RESUME -> onResume()
            ACTION_REGISTRAR -> onRegistrar()
            ACTION_DESCARTAR -> onDescartar()
        }
        return START_STICKY
    }

    private fun onStart(concursoId: Long, materia: String) {
        if (EstudoTimerState.snapshot.value.status != EstudoTimerStatus.IDLE) return

        val agora = System.currentTimeMillis()
        EstudoTimerState.update {
            EstudoTimerSnapshot(
                status = EstudoTimerStatus.RUNNING,
                concursoId = concursoId,
                materia = materia,
                startEpochMillis = agora,
                accumulatedMillis = 0L,
                segmentStartEpochMillis = agora
            )
        }
        postNotification()
    }

    private fun onPause() {
        EstudoTimerState.update { snapshot ->
            if (snapshot.status != EstudoTimerStatus.RUNNING) return@update snapshot
            snapshot.copy(status = EstudoTimerStatus.PAUSED, accumulatedMillis = snapshot.elapsedMillis())
        }
        postNotification()
    }

    private fun onResume() {
        EstudoTimerState.update { snapshot ->
            if (snapshot.status != EstudoTimerStatus.PAUSED) return@update snapshot
            snapshot.copy(status = EstudoTimerStatus.RUNNING, segmentStartEpochMillis = System.currentTimeMillis())
        }
        postNotification()
    }

    private fun onRegistrar() {
        val snapshot = EstudoTimerState.snapshot.value
        if (snapshot.status == EstudoTimerStatus.IDLE) {
            pararServico()
            return
        }

        val elapsedMinutos = (snapshot.elapsedMillis() / 60_000L).toInt().coerceAtLeast(1)
        val inicioLocal = Instant.ofEpochMilli(snapshot.startEpochMillis).atZone(ZoneId.systemDefault())
        val horaInicioMinutos = inicioLocal.hour * 60 + inicioLocal.minute
        val horaFimMinutos = (horaInicioMinutos + elapsedMinutos).coerceAtMost(MINUTOS_POR_DIA)
        // A data do registro é a mesma convenção do resto do app: meia-noite UTC do dia (local) em que o cronômetro começou.
        val dataMeiaNoiteUtc = inicioLocal.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        val registro = RegistroEstudo(
            concursoId = snapshot.concursoId,
            materia = snapshot.materia,
            data = dataMeiaNoiteUtc,
            horaInicioMinutos = horaInicioMinutos,
            horaFimMinutos = horaFimMinutos
        )

        serviceScope.launch {
            repository.upsertRegistroEstudo(registro)
            pararServico()
        }
    }

    private fun onDescartar() {
        pararServico()
    }

    private fun pararServico() {
        EstudoTimerState.update { EstudoTimerSnapshot() }
        stopForeground(STOP_FOREGROUND_REMOVE)
        atualizarWidget()
        stopSelf()
    }

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }

    private fun postNotification() {
        val snapshot = EstudoTimerState.snapshot.value
        if (snapshot.status == EstudoTimerStatus.IDLE) return
        startForeground(NOTIFICATION_ID, buildNotification(snapshot))
        atualizarWidget()
    }

    // O widget (se adicionado à tela inicial) reflete o mesmo EstudoTimerState que a notificação;
    // atualizar nos mesmos pontos mantém os dois sempre sincronizados, sem precisar de tick próprio.
    private fun atualizarWidget() {
        serviceScope.launch {
            EstudoTimerGlanceWidget().updateAll(this@EstudoTimerService)
        }
    }

    private fun buildNotification(snapshot: EstudoTimerSnapshot): Notification {
        val tapIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, ESTUDO_TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_timer)
            .setContentTitle(snapshot.materia.ifBlank { getString(R.string.estudo_timer_notificacao_titulo) })
            .setContentIntent(tapIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            // PRIORITY_DEFAULT (não LOW): no Android 7 e em skins como a MIUI, esse campo legado ainda
            // pesa na decisão de mostrar a notificação condensada (sem ações) ou já expandida.
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val textoConteudo: String
        when (snapshot.status) {
            EstudoTimerStatus.RUNNING -> {
                textoConteudo = getString(R.string.estudo_timer_notificacao_em_andamento)
                builder
                    .setUsesChronometer(true)
                    .setWhen(snapshot.segmentStartEpochMillis - snapshot.accumulatedMillis)
                    .setContentText(textoConteudo)
                    .addAction(acaoPausar())
            }

            EstudoTimerStatus.PAUSED -> {
                textoConteudo = getString(
                    R.string.estudo_timer_notificacao_pausado_format,
                    formatElapsedClock(snapshot.elapsedMillis())
                )
                builder
                    .setUsesChronometer(false)
                    .setContentText(textoConteudo)
                    .addAction(acaoRetomar())
            }

            EstudoTimerStatus.IDLE -> textoConteudo = ""
        }
        builder.addAction(acaoRegistrar())
        builder.addAction(acaoDescartar())
        // BigTextStyle tende a fazer a MIUI preferir o layout expandido (com as ações) por padrão,
        // em vez do formato condensado que só mostra título/texto até pressionar e segurar.
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(textoConteudo))
        return builder.build()
    }

    private fun acaoPausar() = acao(R.string.estudo_timer_acao_pausar, ACTION_PAUSE, requestCode = 1)
    private fun acaoRetomar() = acao(R.string.estudo_timer_acao_retomar, ACTION_RESUME, requestCode = 2)
    private fun acaoRegistrar() = acao(R.string.estudo_timer_acao_registrar, ACTION_REGISTRAR, requestCode = 3)
    private fun acaoDescartar() = acao(R.string.estudo_timer_acao_descartar, ACTION_DESCARTAR, requestCode = 4)

    private fun acao(titleRes: Int, action: String, requestCode: Int): NotificationCompat.Action {
        val intent = Intent(this, EstudoTimerService::class.java).setAction(action)
        val pendingIntent = PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            IconCompat.createWithResource(this, R.drawable.ic_stat_timer),
            getString(titleRes),
            pendingIntent
        ).build()
    }

    companion object {
        /**
         * Id do canal de notificação; o canal é criado uma vez em [br.com.zamfir.verticalize.VerticalizeApp].
         * O sufixo "_v2" existe porque o Android não deixa alterar a importância de um canal já criado
         * no aparelho — mudar o id força a criação de um canal novo com a importância corrigida.
         */
        const val ESTUDO_TIMER_CHANNEL_ID = "cronometro_estudo_v2"

        fun start(context: Context, concursoId: Long, materia: String) {
            val intent = Intent(context, EstudoTimerService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_CONCURSO_ID, concursoId)
                .putExtra(EXTRA_MATERIA, materia)
            ContextCompat.startForegroundService(context, intent)
        }

        fun pause(context: Context) = sendAction(context, ACTION_PAUSE)
        fun resume(context: Context) = sendAction(context, ACTION_RESUME)
        fun registrar(context: Context) = sendAction(context, ACTION_REGISTRAR)
        fun descartar(context: Context) = sendAction(context, ACTION_DESCARTAR)

        private fun sendAction(context: Context, action: String) {
            val intent = Intent(context, EstudoTimerService::class.java).setAction(action)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
