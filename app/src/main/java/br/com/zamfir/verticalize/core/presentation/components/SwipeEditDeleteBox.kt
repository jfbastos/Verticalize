package br.com.zamfir.verticalize.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.zamfir.verticalize.R
import kotlinx.coroutines.launch

/**
 * Item de lista com gestos de swipe: para a direita pede a edição, para a esquerda pede a exclusão.
 * O item volta sozinho à posição original depois do gesto; quem chama decide o que fazer (ex.: confirmar a exclusão).
 * Com [enabled] falso os gestos ficam desligados (ex.: durante a seleção múltipla).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeEditDeleteBox(
    onEditRequested: () -> Unit,
    onDeleteRequested: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.fillMaxWidth(),
        enableDismissFromStartToEnd = enabled,
        enableDismissFromEndToStart = enabled,
        onDismiss = {
            when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> onEditRequested()
                SwipeToDismissBoxValue.EndToStart -> onDeleteRequested()
                SwipeToDismissBoxValue.Settled -> Unit
            }
            scope.launch { dismissState.reset() }
        },
        backgroundContent = { SwipeBackground(dismissState.dismissDirection) }
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(direction: SwipeToDismissBoxValue) {
    val backgroundColor = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
        // Transparente em repouso para não aparecer uma "sombra" nos cantos arredondados do card.
        SwipeToDismissBoxValue.Settled -> Color.Transparent
    }
    val contentColor = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.onPrimaryContainer
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onErrorContainer
        SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val alignment = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        SwipeToDismissBoxValue.Settled -> Alignment.Center
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.large)
            .background(backgroundColor)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment
    ) {
        when (direction) {
            SwipeToDismissBoxValue.StartToEnd -> SwipeActionLabel(
                icon = Icons.Filled.Edit,
                label = stringResource(R.string.action_edit),
                tint = contentColor
            )
            SwipeToDismissBoxValue.EndToStart -> SwipeActionLabel(
                icon = Icons.Filled.Delete,
                label = stringResource(R.string.action_delete),
                tint = contentColor
            )
            SwipeToDismissBoxValue.Settled -> Unit
        }
    }
}

@Composable
private fun SwipeActionLabel(icon: ImageVector, label: String, tint: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = tint)
    }
}
