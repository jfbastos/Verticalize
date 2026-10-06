package br.com.zamfir.verticalize.registroestudo.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.core.presentation.components.EmptyState
import br.com.zamfir.verticalize.registroestudo.presentation.components.EstudoTimerCard
import br.com.zamfir.verticalize.registroestudo.presentation.components.RegistroEstudoCalendarView
import br.com.zamfir.verticalize.registroestudo.presentation.components.RegistroEstudoListItem
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme

/** Conteúdo da aba "Horas de estudo" dos detalhes do concurso; não tem Scaffold/TopAppBar/FAB próprios. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroEstudoListContent(
    state: RegistroEstudoListState,
    onAction: (RegistroEstudoListAction) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    Column(modifier = modifier.fillMaxSize()) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = contentPadding.calculateTopPadding() + 8.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 8.dp
                )
        ) {
            SegmentedButton(
                selected = state.viewMode == RegistroEstudoViewMode.LISTA,
                onClick = { onAction(RegistroEstudoListAction.OnViewModeChanged(RegistroEstudoViewMode.LISTA)) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text(stringResource(R.string.registro_estudo_view_lista))
            }
            SegmentedButton(
                selected = state.viewMode == RegistroEstudoViewMode.CALENDARIO,
                onClick = { onAction(RegistroEstudoListAction.OnViewModeChanged(RegistroEstudoViewMode.CALENDARIO)) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text(stringResource(R.string.registro_estudo_view_calendario))
            }
        }

        if (state.isTimerAtivoNesteConcurso) {
            EstudoTimerCard(
                snapshot = state.timerSnapshot,
                onPauseClick = { onAction(RegistroEstudoListAction.OnTimerPauseClick) },
                onResumeClick = { onAction(RegistroEstudoListAction.OnTimerResumeClick) },
                onRegistrarClick = { onAction(RegistroEstudoListAction.OnTimerRegistrarClick) },
                onDescartarClick = { onAction(RegistroEstudoListAction.OnTimerDescartarClick) },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            )
        } else if (state.isTimerAtivoEmOutroConcurso) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.estudo_timer_aviso_outro_concurso),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val bodyPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding())

        when (state.viewMode) {
            RegistroEstudoViewMode.CALENDARIO -> RegistroEstudoCalendarView(
                registros = state.registros,
                month = state.calendarMonth,
                onPreviousMonthClick = { onAction(RegistroEstudoListAction.OnCalendarPreviousMonthClick) },
                onNextMonthClick = { onAction(RegistroEstudoListAction.OnCalendarNextMonthClick) },
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = bodyPadding.calculateBottomPadding() + 88.dp
                ),
                modifier = Modifier.fillMaxSize()
            )

            RegistroEstudoViewMode.LISTA -> if (state.registros.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bodyPadding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Filled.Schedule,
                        title = stringResource(R.string.registro_estudo_empty_state_message),
                        message = stringResource(R.string.registro_estudo_empty_state_hint)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(
                        top = 0.dp,
                        // Espaço extra embaixo para o último card não ficar escondido atrás do FAB.
                        bottom = bodyPadding.calculateBottomPadding() + 88.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.registros, key = { it.id }) { registro ->
                        RegistroEstudoListItem(
                            registro = registro,
                            onEditRequested = { onAction(RegistroEstudoListAction.OnEditSwipe(registro.id)) },
                            onDeleteRequested = { onAction(RegistroEstudoListAction.OnDeleteSwipe(registro.id)) },
                            onTapped = { onAction(RegistroEstudoListAction.OnRegistroTapped(registro.id)) },
                            onLongPress = { onAction(RegistroEstudoListAction.OnRegistroLongPress(registro.id)) },
                            emSelecao = state.isSelecaoAtiva,
                            selecionado = registro.id in state.registrosSelecionados,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegistroEstudoListContentPreview() {
    VerticalizeTheme {
        RegistroEstudoListContent(
            state = RegistroEstudoListState(
                registros = listOf(
                    RegistroEstudoUi(id = 1L, materia = "Direito Constitucional", dataFormatted = "17/09/2026", duracaoFormatted = "12h")
                )
            ),
            onAction = {},
            contentPadding = PaddingValues()
        )
    }
}
