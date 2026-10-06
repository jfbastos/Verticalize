package br.com.zamfir.verticalize.registroestudo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.core.presentation.components.VerticalizeCard
import br.com.zamfir.verticalize.registroestudo.timer.EstudoTimerSnapshot
import br.com.zamfir.verticalize.registroestudo.timer.EstudoTimerStatus
import br.com.zamfir.verticalize.registroestudo.timer.elapsedMillis
import br.com.zamfir.verticalize.registroestudo.timer.formatElapsedClock
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme
import kotlinx.coroutines.delay

/** Card do cronômetro ativo nesta tela: tempo decorrido em tempo real e os controles de pausar/retomar/registrar. */
@Composable
fun EstudoTimerCard(
    snapshot: EstudoTimerSnapshot,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onRegistrarClick: () -> Unit,
    onDescartarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(snapshot.status) {
        while (snapshot.status == EstudoTimerStatus.RUNNING) {
            delay(1000)
            now = System.currentTimeMillis()
        }
    }
    val elapsedFormatted = remember(snapshot, now) { formatElapsedClock(snapshot.elapsedMillis(now)) }

    VerticalizeCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = snapshot.materia,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (snapshot.status == EstudoTimerStatus.PAUSED) {
                            stringResource(R.string.estudo_timer_card_pausado)
                        } else {
                            stringResource(R.string.estudo_timer_card_em_andamento)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = elapsedFormatted,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (snapshot.status == EstudoTimerStatus.RUNNING) {
                    OutlinedButton(onClick = onPauseClick, modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Filled.Pause, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.estudo_timer_acao_pausar))
                    }
                } else {
                    OutlinedButton(onClick = onResumeClick, modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.estudo_timer_acao_retomar))
                    }
                }
                Button(onClick = onRegistrarClick, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.estudo_timer_acao_registrar))
                }
            }

            TextButton(
                onClick = onDescartarClick,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.estudo_timer_acao_descartar))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EstudoTimerCardPreview() {
    VerticalizeTheme {
        EstudoTimerCard(
            snapshot = EstudoTimerSnapshot(
                status = EstudoTimerStatus.RUNNING,
                materia = "Direito Constitucional",
                segmentStartEpochMillis = System.currentTimeMillis() - 65_000L
            ),
            onPauseClick = {},
            onResumeClick = {},
            onRegistrarClick = {},
            onDescartarClick = {}
        )
    }
}
