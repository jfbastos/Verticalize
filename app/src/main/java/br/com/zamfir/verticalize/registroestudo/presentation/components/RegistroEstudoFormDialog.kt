package br.com.zamfir.verticalize.registroestudo.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.concurso.presentation.components.ConcursoDatePickerDialog
import br.com.zamfir.verticalize.core.presentation.components.VerticalizeFormDialog
import br.com.zamfir.verticalize.core.presentation.components.WheelTimePicker
import br.com.zamfir.verticalize.registroestudo.presentation.RegistroEstudoListAction
import br.com.zamfir.verticalize.registroestudo.presentation.RegistroEstudoListState
import br.com.zamfir.verticalize.registroestudo.presentation.isSaveEnabled

@Composable
fun RegistroEstudoFormDialog(
    state: RegistroEstudoListState,
    onAction: (RegistroEstudoListAction) -> Unit
) {
    VerticalizeFormDialog(
        title = if (state.editingId == null) {
            stringResource(R.string.registro_estudo_dialog_title)
        } else {
            stringResource(R.string.registro_estudo_dialog_title_edit)
        },
        icon = Icons.Filled.Schedule,
        onDismiss = { onAction(RegistroEstudoListAction.OnDismissDialog) },
        onConfirm = { onAction(RegistroEstudoListAction.OnSaveClick) },
        confirmEnabled = state.isSaveEnabled
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.materia,
            onValueChange = { onAction(RegistroEstudoListAction.OnMateriaChanged(it)) },
            label = { Text(stringResource(R.string.label_materia)) },
            singleLine = true
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAction(RegistroEstudoListAction.OnDataFieldClick) }
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.dataFormatted,
                onValueChange = {},
                enabled = false,
                readOnly = true,
                label = { Text(stringResource(R.string.label_data)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = stringResource(R.string.cd_calendar_icon)
                    )
                }
            )
        }

        Text(
            text = stringResource(R.string.registro_estudo_hora_inicio_label),
            style = MaterialTheme.typography.labelMedium
        )
        WheelTimePicker(
            initialMinutosDoDia = state.horaInicioMinutos,
            onMinutosDoDiaChange = { onAction(RegistroEstudoListAction.OnHoraInicioChanged(it)) }
        )

        Text(
            text = stringResource(R.string.registro_estudo_hora_fim_label),
            style = MaterialTheme.typography.labelMedium
        )
        WheelTimePicker(
            initialMinutosDoDia = state.horaFimMinutos,
            onMinutosDoDiaChange = { onAction(RegistroEstudoListAction.OnHoraFimChanged(it)) }
        )
    }

    if (state.isDatePickerVisible) {
        ConcursoDatePickerDialog(
            initialSelectedDateMillis = state.dataMillis,
            onConfirm = { onAction(RegistroEstudoListAction.OnDataPickerConfirm(it)) },
            onDismiss = { onAction(RegistroEstudoListAction.OnDataPickerDismiss) }
        )
    }
}
