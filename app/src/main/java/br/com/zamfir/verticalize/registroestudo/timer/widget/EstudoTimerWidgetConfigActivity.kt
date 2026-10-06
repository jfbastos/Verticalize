package br.com.zamfir.verticalize.registroestudo.timer.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.core.presentation.ObserveAsEvents
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Tela leve aberta pelo widget quando não há cronômetro em andamento: escolher concurso + matéria
 * e iniciar, sem precisar abrir o app. Fica num scrim com um card central (não um tema de diálogo
 * de janela) para não depender de flags de tema translúcido.
 */
class EstudoTimerWidgetConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VerticalizeTheme {
                EstudoTimerWidgetConfigRoot(onFinished = { finish() })
            }
        }
    }
}

@Composable
private fun EstudoTimerWidgetConfigRoot(
    onFinished: () -> Unit,
    viewModel: EstudoTimerWidgetConfigViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            EstudoTimerWidgetConfigEvent.Finish -> onFinished()
        }
    }

    EstudoTimerWidgetConfigScreen(state = state, onAction = viewModel::onAction)
}

@Composable
private fun EstudoTimerWidgetConfigScreen(
    state: EstudoTimerWidgetConfigState,
    onAction: (EstudoTimerWidgetConfigAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
            .clickable { onAction(EstudoTimerWidgetConfigAction.OnCancelClick) },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                // Consome o clique para não "vazar" pro scrim por trás e fechar a tela sem querer.
                .clickable(enabled = false) {},
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = stringResource(R.string.estudo_timer_dialog_titulo), style = MaterialTheme.typography.titleLarge)

                when {
                    state.isLoading -> CircularProgressIndicator()
                    state.concursos.isEmpty() -> Text(stringResource(R.string.estudo_timer_widget_sem_concursos))
                    else -> {
                        var menuExpandido by remember { mutableStateOf(false) }
                        val concursoSelecionado = state.concursos.find { it.id == state.concursoIdSelecionado }

                        Box {
                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { menuExpandido = true },
                                value = concursoSelecionado?.nome.orEmpty(),
                                onValueChange = {},
                                enabled = false,
                                readOnly = true,
                                label = { Text(stringResource(R.string.label_concurso)) }
                            )
                            DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }) {
                                state.concursos.forEach { concurso ->
                                    DropdownMenuItem(
                                        text = { Text(concurso.nome) },
                                        onClick = {
                                            onAction(EstudoTimerWidgetConfigAction.OnConcursoSelected(concurso.id))
                                            menuExpandido = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.materia,
                            onValueChange = { onAction(EstudoTimerWidgetConfigAction.OnMateriaChanged(it)) },
                            label = { Text(stringResource(R.string.label_materia)) },
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { onAction(EstudoTimerWidgetConfigAction.OnCancelClick) }) {
                                Text(stringResource(R.string.action_cancel))
                            }
                            Button(
                                onClick = { onAction(EstudoTimerWidgetConfigAction.OnStartClick) },
                                enabled = state.isStartEnabled
                            ) {
                                Text(stringResource(R.string.estudo_timer_acao_iniciar))
                            }
                        }
                    }
                }
            }
        }
    }
}
