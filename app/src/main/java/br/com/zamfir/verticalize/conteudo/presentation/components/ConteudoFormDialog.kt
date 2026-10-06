package br.com.zamfir.verticalize.conteudo.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.concurso.presentation.ConcursoDetailAction
import br.com.zamfir.verticalize.concurso.presentation.ConcursoDetailState
import br.com.zamfir.verticalize.concurso.presentation.components.ConcursoDatePickerDialog
import br.com.zamfir.verticalize.concurso.presentation.isConteudoSaveEnabled
import br.com.zamfir.verticalize.conteudo.domain.Prioridade
import br.com.zamfir.verticalize.conteudo.presentation.labelRes
import br.com.zamfir.verticalize.core.presentation.components.VerticalizeFormDialog

@Composable
fun ConteudoFormDialog(
    state: ConcursoDetailState,
    onAction: (ConcursoDetailAction) -> Unit
) {
    VerticalizeFormDialog(
        title = if (state.editingConteudoId == null) {
            stringResource(R.string.conteudo_dialog_title_new)
        } else {
            stringResource(R.string.conteudo_dialog_title_edit)
        },
        icon = Icons.AutoMirrored.Filled.MenuBook,
        onDismiss = { onAction(ConcursoDetailAction.OnConteudoDismissDialog) },
        onConfirm = { onAction(ConcursoDetailAction.OnConteudoSaveClick) },
        confirmEnabled = state.isConteudoSaveEnabled
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.conteudoMateria,
            onValueChange = { onAction(ConcursoDetailAction.OnConteudoMateriaChanged(it)) },
            label = { Text(stringResource(R.string.label_materia)) },
            singleLine = true
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.conteudoDescricao,
            onValueChange = { onAction(ConcursoDetailAction.OnConteudoDescricaoChanged(it)) },
            label = { Text(stringResource(R.string.label_descricao)) },
            singleLine = true
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.label_prioridade),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Prioridade.entries.forEach { prioridade ->
                    FilterChip(
                        selected = prioridade == state.conteudoPrioridade,
                        onClick = { onAction(ConcursoDetailAction.OnConteudoPrioridadeChanged(prioridade)) },
                        label = { Text(stringResource(prioridade.labelRes())) }
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = state.conteudoEixoInput,
                onValueChange = { onAction(ConcursoDetailAction.OnConteudoEixoChanged(it)) },
                label = { Text(stringResource(R.string.label_eixo)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = state.conteudoBlocoInput,
                onValueChange = { onAction(ConcursoDetailAction.OnConteudoBlocoChanged(it)) },
                label = { Text(stringResource(R.string.label_bloco)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.conteudoQuantidadeAulasInput,
            onValueChange = { onAction(ConcursoDetailAction.OnConteudoQuantidadeAulasChanged(it)) },
            label = { Text(stringResource(R.string.label_quantidade_aulas)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.conteudoTempoMedioAulaInput,
            onValueChange = { onAction(ConcursoDetailAction.OnConteudoTempoMedioAulaChanged(it)) },
            label = { Text(stringResource(R.string.label_tempo_medio_aula)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAction(ConcursoDetailAction.OnConteudoDataRevisaoFieldClick) }
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.conteudoDataUltimaRevisaoFormatted,
                onValueChange = {},
                enabled = false,
                readOnly = true,
                label = { Text(stringResource(R.string.label_data_ultima_revisao)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = stringResource(R.string.cd_calendar_icon)
                    )
                }
            )
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.conteudoQuantidadeQuestoesInput,
            onValueChange = { onAction(ConcursoDetailAction.OnConteudoQuantidadeQuestoesChanged(it)) },
            label = { Text(stringResource(R.string.label_quantidade_questoes)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }

    if (state.isConteudoDatePickerVisible) {
        ConcursoDatePickerDialog(
            initialSelectedDateMillis = state.conteudoDataUltimaRevisaoMillis,
            onConfirm = { onAction(ConcursoDetailAction.OnConteudoDataRevisaoPickerConfirm(it)) },
            onDismiss = { onAction(ConcursoDetailAction.OnConteudoDataRevisaoPickerDismiss) }
        )
    }
}
