package br.com.zamfir.verticalize.registroestudo.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme

/** Diálogo do FAB de "Horas de estudo": escolher entre registrar manualmente ou iniciar o cronômetro. */
@Composable
fun RegistroOpcaoDialog(
    onManualClick: () -> Unit,
    onCronometroClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.registro_estudo_opcao_titulo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                RegistroOpcaoItem(
                    icon = Icons.Filled.Edit,
                    title = stringResource(R.string.registro_estudo_opcao_manual_titulo),
                    subtitle = stringResource(R.string.registro_estudo_opcao_manual_subtitulo),
                    onClick = onManualClick
                )
                RegistroOpcaoItem(
                    icon = Icons.Filled.Timer,
                    title = stringResource(R.string.registro_estudo_opcao_cronometro_titulo),
                    subtitle = stringResource(R.string.registro_estudo_opcao_cronometro_subtitulo),
                    onClick = onCronometroClick
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun RegistroOpcaoItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegistroOpcaoDialogPreview() {
    VerticalizeTheme {
        RegistroOpcaoDialog(onManualClick = {}, onCronometroClick = {}, onDismiss = {})
    }
}
