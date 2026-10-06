package br.com.zamfir.verticalize.concurso.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.concurso.presentation.ConcursoListAction
import br.com.zamfir.verticalize.concurso.presentation.ConcursoListState
import br.com.zamfir.verticalize.concurso.presentation.isSaveEnabled
import br.com.zamfir.verticalize.core.presentation.BrlCurrencyVisualTransformation
import br.com.zamfir.verticalize.core.presentation.components.VerticalizeFormDialog

@Composable
fun ConcursoFormDialog(
    state: ConcursoListState,
    onAction: (ConcursoListAction) -> Unit
) {
    VerticalizeFormDialog(
        title = if (state.editingId == null) {
            stringResource(R.string.concurso_dialog_title_new)
        } else {
            stringResource(R.string.concurso_dialog_title_edit)
        },
        icon = Icons.Filled.School,
        onDismiss = { onAction(ConcursoListAction.OnDismissDialog) },
        onConfirm = { onAction(ConcursoListAction.OnSaveClick) },
        confirmEnabled = state.isSaveEnabled
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.nome,
            onValueChange = { onAction(ConcursoListAction.OnNomeChanged(it)) },
            label = { Text(stringResource(R.string.label_nome)) },
            singleLine = true
        )

        NivelDropdownField(
            modifier = Modifier.fillMaxWidth(),
            selected = state.nivel,
            expanded = state.isNivelDropdownExpanded,
            onExpandedChange = { onAction(ConcursoListAction.OnNivelDropdownToggle) },
            onNivelSelected = { onAction(ConcursoListAction.OnNivelSelected(it)) }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAction(ConcursoListAction.OnDataProvaFieldClick) }
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.dataProvaFormatted,
                onValueChange = {},
                enabled = false,
                readOnly = true,
                label = { Text(stringResource(R.string.label_data_prova)) },
                trailingIcon = {
                    if (state.dataProvaMillis != null) {
                        IconButton(onClick = { onAction(ConcursoListAction.OnDataProvaClearClick) }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.concurso_data_prova_remover_cd)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = stringResource(R.string.cd_calendar_icon)
                        )
                    }
                }
            )
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.valorInscricaoInput,
            onValueChange = { onAction(ConcursoListAction.OnValorInscricaoChanged(it)) },
            label = { Text(stringResource(R.string.label_valor_inscricao)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = BrlCurrencyVisualTransformation()
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.banca,
            onValueChange = { onAction(ConcursoListAction.OnBancaChanged(it)) },
            label = { Text(stringResource(R.string.label_banca)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )
    }

    if (state.isDatePickerVisible) {
        ConcursoDatePickerDialog(
            initialSelectedDateMillis = state.dataProvaMillis,
            onConfirm = { onAction(ConcursoListAction.OnDatePickerConfirm(it)) },
            onDismiss = { onAction(ConcursoListAction.OnDatePickerDismiss) }
        )
    }
}
