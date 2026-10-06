package br.com.zamfir.verticalize.core.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme

private const val PROGRESS_ANIMATION_MILLIS = 700
private const val RING_START_ANGLE = -90f
private const val RING_FULL_SWEEP = 360f

/**
 * Anel de progresso com o percentual no centro. O valor animado só é lido na fase de desenho
 * (dentro de [drawBehind]), então a animação não causa recomposição.
 */
@Composable
fun ProgressRing(
    percentual: Int,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    strokeWidth: Dp = 6.dp,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium
) {
    val fraction = percentual.coerceIn(0, 100) / 100f
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = PROGRESS_ANIMATION_MILLIS),
        label = "progressRing"
    )

    Box(
        modifier = modifier
            .size(size)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f) }
            .drawBehind {
                val strokePx = strokeWidth.toPx()
                val diameter = this.size.minDimension - strokePx
                val topLeft = Offset(strokePx / 2f, strokePx / 2f)
                val arcSize = Size(diameter, diameter)
                val stroke = Stroke(width = strokePx, cap = StrokeCap.Round)

                drawArc(
                    color = trackColor,
                    startAngle = RING_START_ANGLE,
                    sweepAngle = RING_FULL_SWEEP,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )
                if (animatedFraction > 0f) {
                    drawArc(
                        color = progressColor,
                        startAngle = RING_START_ANGLE,
                        sweepAngle = RING_FULL_SWEEP * animatedFraction,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.percentual_format, percentual),
            style = textStyle,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressRingPreview() {
    VerticalizeTheme {
        ProgressRing(percentual = 35)
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressRingLargePreview() {
    VerticalizeTheme {
        ProgressRing(
            percentual = 72,
            size = 96.dp,
            strokeWidth = 10.dp,
            textStyle = MaterialTheme.typography.titleLarge
        )
    }
}
