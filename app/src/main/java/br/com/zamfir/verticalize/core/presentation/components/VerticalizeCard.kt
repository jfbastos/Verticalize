package br.com.zamfir.verticalize.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme

/**
 * Card "flat" do app: cantos grandes, superfície tonal e borda fina em vez de elevação.
 * Passe [onClick] para uma versão clicável (com ripple e semântica de botão) e [onLongClick] para
 * também reagir ao toque longo (ex.: entrar no modo de seleção).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerticalizeCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.large
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    val border = BorderStroke(width = 1.dp, color = borderColor)

    if (onLongClick != null) {
        // O Card clicável do Material não tem toque longo; o clip mantém o ripple dentro dos cantos.
        Card(
            modifier = modifier
                .clip(shape)
                .combinedClickable(onClick = onClick ?: {}, onLongClick = onLongClick),
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = content
        )
    } else if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = content
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VerticalizeCardPreview() {
    VerticalizeTheme {
        VerticalizeCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Direito Constitucional", style = MaterialTheme.typography.titleMedium)
                Text(text = "12h estudadas", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
