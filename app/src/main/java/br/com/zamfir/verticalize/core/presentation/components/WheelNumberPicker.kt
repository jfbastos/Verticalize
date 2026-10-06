package br.com.zamfir.verticalize.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

private const val WHEEL_VIRTUAL_ITEM_COUNT = Int.MAX_VALUE

/**
 * Seletor de número com efeito de scroll infinito (estilo relógio/despertador).
 * O valor inicial nasce já centralizado no viewport, sem scroll perceptível no primeiro frame.
 */
@Composable
fun WheelNumberPicker(
    range: IntRange,
    initialValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleItemsCount: Int = 3,
    itemHeight: Dp = 40.dp
) {
    require(visibleItemsCount % 2 == 1) { "visibleItemsCount deve ser ímpar para existir um item central" }
    require(initialValue in range) { "initialValue deve estar dentro de range" }

    val itemCount = range.count()

    val initialIndex = remember(range, initialValue) {
        val virtualMiddle = WHEEL_VIRTUAL_ITEM_COUNT / 2
        val alignedToRangeStart = virtualMiddle - (virtualMiddle % itemCount)
        alignedToRangeStart + (initialValue - range.first)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val centerItemIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isEmpty()) {
                initialIndex
            } else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                layoutInfo.visibleItemsInfo
                    .minBy { item -> abs((item.offset + item.size / 2) - viewportCenter) }
                    .index
            }
        }
    }

    LaunchedEffect(centerItemIndex) {
        val normalized = ((centerItemIndex % itemCount) + itemCount) % itemCount
        onValueChange(range.first + normalized)
    }

    Box(
        modifier = modifier.height(itemHeight * visibleItemsCount),
        contentAlignment = Alignment.Center
    ) {
        // Faixa de destaque atrás do item central, para o valor selecionado ficar evidente.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        )
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight * (visibleItemsCount / 2)),
            modifier = Modifier.fillMaxHeight()
        ) {
            items(count = WHEEL_VIRTUAL_ITEM_COUNT) { index ->
                val value = range.first + (index % itemCount)
                val distance = abs(index - centerItemIndex)
                val isCentered = distance == 0

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .graphicsLayer {
                            val fade = 1f - distance.coerceAtMost(2) * 0.35f
                            alpha = fade.coerceIn(0.3f, 1f)
                            scaleX = if (isCentered) 1f else 0.85f
                            scaleY = scaleX
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "%02d".format(value),
                        style = if (isCentered) {
                            MaterialTheme.typography.headlineSmall
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        color = if (isCentered) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
