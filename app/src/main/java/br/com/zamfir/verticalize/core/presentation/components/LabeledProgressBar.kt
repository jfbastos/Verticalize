package br.com.zamfir.verticalize.core.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme
import kotlin.math.max

private const val PROGRESS_ANIMATION_MILLIS = 700

/** Barra de progresso arredondada com rótulo à esquerda e percentual à direita. */
@Composable
fun LabeledProgressBar(
    label: String,
    percentual: Int,
    modifier: Modifier = Modifier,
    barHeight: Dp = 8.dp,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest
) {
    val fraction = percentual.coerceIn(0, 100) / 100f
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = PROGRESS_ANIMATION_MILLIS),
        label = "labeledProgressBar"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.percentual_format, percentual),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(CircleShape)
                .background(trackColor)
                .semantics { progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f) }
                .drawBehind {
                    if (animatedFraction > 0f) {
                        // Largura mínima igual à altura para o preenchimento sempre parecer uma "pílula".
                        val fillWidth = max(size.width * animatedFraction, size.height)
                        drawRoundRect(
                            color = progressColor,
                            size = Size(fillWidth, size.height),
                            cornerRadius = CornerRadius(size.height / 2f)
                        )
                    }
                }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LabeledProgressBarPreview() {
    VerticalizeTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LabeledProgressBar(label = "Eixo 1", percentual = 40)
            LabeledProgressBar(label = "Eixo 2", percentual = 100)
            LabeledProgressBar(label = "Eixo 3", percentual = 0)
        }
    }
}
