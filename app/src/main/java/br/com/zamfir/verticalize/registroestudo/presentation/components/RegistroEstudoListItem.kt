package br.com.zamfir.verticalize.registroestudo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.zamfir.verticalize.core.presentation.components.InfoChip
import br.com.zamfir.verticalize.core.presentation.components.SwipeEditDeleteBox
import br.com.zamfir.verticalize.core.presentation.components.VerticalizeCard
import br.com.zamfir.verticalize.registroestudo.presentation.RegistroEstudoUi
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme

@Composable
fun RegistroEstudoListItem(
    registro: RegistroEstudoUi,
    onEditRequested: () -> Unit,
    onDeleteRequested: () -> Unit,
    modifier: Modifier = Modifier,
    onTapped: () -> Unit = {},
    onLongPress: () -> Unit = {},
    emSelecao: Boolean = false,
    selecionado: Boolean = false
) {
    val colors = MaterialTheme.colorScheme

    // Sem swipe durante a seleção, para um arrasto não editar/excluir um item avulso no meio dela.
    SwipeEditDeleteBox(
        onEditRequested = onEditRequested,
        onDeleteRequested = onDeleteRequested,
        modifier = modifier,
        enabled = !emSelecao
    ) {
        VerticalizeCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onTapped,
            onLongClick = onLongPress,
            containerColor = if (selecionado) colors.primaryContainer else colors.surfaceContainerLow,
            borderColor = if (selecionado) colors.primary else colors.outlineVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (emSelecao) {
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Checkbox(checked = selecionado, onCheckedChange = { onTapped() })
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = registro.materia,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = registro.dataFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                InfoChip(
                    text = registro.duracaoFormatted,
                    icon = Icons.Filled.Timer,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegistroEstudoListItemPreview() {
    VerticalizeTheme {
        RegistroEstudoListItem(
            registro = RegistroEstudoUi(
                id = 1L,
                materia = "Direito Constitucional",
                dataFormatted = "17/09/2026",
                duracaoFormatted = "2h 30min"
            ),
            onEditRequested = {},
            onDeleteRequested = {}
        )
    }
}
