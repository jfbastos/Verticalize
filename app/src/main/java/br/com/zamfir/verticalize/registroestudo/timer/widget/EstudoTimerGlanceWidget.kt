package br.com.zamfir.verticalize.registroestudo.timer.widget

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.registroestudo.timer.EstudoTimerSnapshot
import br.com.zamfir.verticalize.registroestudo.timer.EstudoTimerState
import br.com.zamfir.verticalize.registroestudo.timer.EstudoTimerStatus
import br.com.zamfir.verticalize.registroestudo.timer.elapsedMillis
import br.com.zamfir.verticalize.registroestudo.timer.formatElapsedClock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")

private val CorFundo = Color.Black.copy(alpha = 0.6f)
private val CorTextoPrincipal = ColorProvider(Color.White)
private val CorTextoEmAndamento = ColorProvider(Color(0xFF69F0AE))
private val CorTextoPausado = ColorProvider(Color(0xFFFFD54F))
private val CorBotaoFundo = ColorProvider(Color.White)
private val CorBotaoConteudo = ColorProvider(Color.Black)

private val TAMANHO_BOTAO = 36.dp
private val TAMANHO_ICONE = 21.dp

/**
 * Widget de tela inicial do cronômetro de estudo. Só reflete o mesmo [EstudoTimerState] global que
 * a notificação e o card dentro do app usam — qualquer ação (daqui, da notificação ou do app) deixa
 * os três sincronizados. Sem contagem ao vivo por segundo: o valor exibido só atualiza quando o
 * serviço muda de estado (ver chamadas a [updateAll] em EstudoTimerService), para não precisar de um
 * worker periódico só para isso.
 *
 * Tamanho fixo (ver estudo_timer_widget_info.xml): uma faixa fina em que o fundo envolve só o
 * conteúdo, centralizada na célula — o que sobra da célula fica transparente.
 */
class EstudoTimerGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent()
        }
    }

    @Composable
    private fun WidgetContent() {
        val snapshot by EstudoTimerState.snapshot.collectAsState()
        WidgetBody(snapshot)
    }
}

/** Separado do estado global para poder ser renderizado nos previews com snapshots fixos. */
@Composable
private fun WidgetBody(snapshot: EstudoTimerSnapshot) {
    val context = LocalContext.current

    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(
            modifier = GlanceModifier
                .cornerRadius(16.dp)
                .background(CorFundo)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (snapshot.status) {
                EstudoTimerStatus.IDLE -> {
                    Text(
                        text = glanceString(R.string.estudo_timer_widget_nenhum_em_andamento),
                        style = TextStyle(color = CorTextoPrincipal, fontSize = 21.sp, fontWeight = FontWeight.Medium),
                        maxLines = 1
                    )

                    Spacer(modifier = GlanceModifier.width(24.dp))

                    BotaoIcone(
                        R.drawable.ic_widget_play,
                        R.string.estudo_timer_acao_iniciar,
                        actionStartActivity(Intent(context, EstudoTimerWidgetConfigActivity::class.java))
                    )
                }

                EstudoTimerStatus.RUNNING -> {
                    val inicioFormatado = Instant
                        .ofEpochMilli(snapshot.segmentStartEpochMillis - snapshot.accumulatedMillis)
                        .atZone(ZoneId.systemDefault())
                        .format(HORA_FORMATTER)

                    TextosEmCurso(
                        materia = snapshot.materia,
                        status = glanceString(R.string.estudo_timer_widget_em_andamento_desde_format, inicioFormatado),
                        corStatus = CorTextoEmAndamento
                    )

                    Spacer(modifier = GlanceModifier.width(24.dp))

                    BotaoIcone(
                        R.drawable.ic_widget_pause,
                        R.string.estudo_timer_acao_pausar,
                        actionRunCallback<EstudoTimerWidgetPauseAction>()
                    )

                    Spacer(modifier = GlanceModifier.width(6.dp))

                    BotaoIcone(
                        R.drawable.ic_widget_check,
                        R.string.estudo_timer_acao_registrar,
                        actionRunCallback<EstudoTimerWidgetRegistrarAction>()
                    )
                }

                EstudoTimerStatus.PAUSED -> {
                    TextosEmCurso(
                        materia = snapshot.materia,
                        status = glanceString(
                            R.string.estudo_timer_notificacao_pausado_format,
                            formatElapsedClock(snapshot.elapsedMillis())
                        ),
                        corStatus = CorTextoPausado
                    )
                    Spacer(modifier = GlanceModifier.width(24.dp))
                    BotaoIcone(
                        R.drawable.ic_widget_play,
                        R.string.estudo_timer_acao_retomar,
                        actionRunCallback<EstudoTimerWidgetResumeAction>()
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    BotaoIcone(
                        R.drawable.ic_widget_check,
                        R.string.estudo_timer_acao_registrar,
                        actionRunCallback<EstudoTimerWidgetRegistrarAction>()
                    )
                }
            }
        }
    }
}

@Composable
private fun TextosEmCurso(materia: String, status: String, corStatus: ColorProvider) {
    Column {
        Text(
            text = materia,
            style = TextStyle(color = CorTextoPrincipal, fontSize = 26.sp, fontWeight = FontWeight.Bold),
            maxLines = 1
        )
        Text(
            text = status,
            style = TextStyle(color = corStatus, fontSize = 16.sp, fontWeight = FontWeight.Medium),
            maxLines = 1
        )
    }
}

@Composable
private fun BotaoIcone(@DrawableRes icone: Int, @StringRes descricao: Int, acao: Action) {
    Box(
        modifier = GlanceModifier
            .size(TAMANHO_BOTAO)
            .cornerRadius(TAMANHO_BOTAO / 2)
            .background(CorBotaoFundo)
            .clickable(acao),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(icone),
            contentDescription = glanceString(descricao),
            modifier = GlanceModifier.size(TAMANHO_ICONE),
            colorFilter = ColorFilter.tint(CorBotaoConteudo)
        )
    }
}

@Composable
private fun glanceString(@StringRes resId: Int, vararg formatArgs: Any): String =
    LocalContext.current.getString(resId, *formatArgs)

// Tamanho aproximado de 3x1 células (ver estudo_timer_widget_info.xml).
private const val PREVIEW_LARGURA = 500
private const val PREVIEW_ALTURA = 120

private const val PREVIEW_DURACAO = 25 * 60 * 1000L
private val PREVIEW_INICIO = System.currentTimeMillis() - PREVIEW_DURACAO

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = PREVIEW_LARGURA, heightDp = PREVIEW_ALTURA)
@Composable
private fun PreviewOcioso() {
    WidgetBody(EstudoTimerSnapshot(status = EstudoTimerStatus.IDLE))
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = PREVIEW_LARGURA, heightDp = PREVIEW_ALTURA)
@Composable
private fun PreviewEmAndamento() {
    WidgetBody(
        EstudoTimerSnapshot(
            status = EstudoTimerStatus.RUNNING,
            materia = "Direito Constitucional",
            startEpochMillis = PREVIEW_INICIO,
            segmentStartEpochMillis = PREVIEW_INICIO
        )
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = PREVIEW_LARGURA, heightDp = PREVIEW_ALTURA)
@Composable
private fun PreviewPausado() {
    WidgetBody(
        EstudoTimerSnapshot(
            status = EstudoTimerStatus.PAUSED,
            materia = "Direito Constitucional",
            startEpochMillis = PREVIEW_INICIO,
            accumulatedMillis = PREVIEW_DURACAO
        )
    )
}