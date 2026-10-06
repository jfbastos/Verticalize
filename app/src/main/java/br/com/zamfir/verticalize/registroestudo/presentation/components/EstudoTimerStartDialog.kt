package br.com.zamfir.verticalize.registroestudo.presentation.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.registroestudo.presentation.RegistroEstudoListAction
import br.com.zamfir.verticalize.registroestudo.presentation.RegistroEstudoListState
import br.com.zamfir.verticalize.registroestudo.presentation.isTimerStartSaveEnabled
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme

/** Pede a matéria antes de iniciar o cronômetro; ela aparece na notificação do Foreground Service. */
@Composable
fun EstudoTimerStartDialog(
    state: RegistroEstudoListState,
    onAction: (RegistroEstudoListAction) -> Unit
) {
    val context = LocalContext.current
    // O cronômetro inicia independente do resultado; sem a permissão, só a notificação não aparece.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    AlertDialog(
        onDismissRequest = { onAction(RegistroEstudoListAction.OnTimerStartDismiss) },
        icon = { Icon(imageVector = Icons.Filled.Timer, contentDescription = null) },
        title = { Text(stringResource(R.string.estudo_timer_dialog_titulo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.timerMateriaInput,
                    onValueChange = { onAction(RegistroEstudoListAction.OnTimerMateriaChanged(it)) },
                    label = { Text(stringResource(R.string.label_materia)) },
                    singleLine = true
                )
                Text(
                    text = stringResource(R.string.estudo_timer_dialog_aviso),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                enabled = state.isTimerStartSaveEnabled,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    onAction(RegistroEstudoListAction.OnTimerStartConfirm)
                }
            ) {
                Text(stringResource(R.string.estudo_timer_acao_iniciar))
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(RegistroEstudoListAction.OnTimerStartDismiss) }) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun EstudoTimerStartDialogPreview() {
    VerticalizeTheme {
        EstudoTimerStartDialog(state = RegistroEstudoListState(), onAction = {})
    }
}
