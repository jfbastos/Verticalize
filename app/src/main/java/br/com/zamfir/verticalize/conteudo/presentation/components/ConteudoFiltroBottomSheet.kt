package br.com.zamfir.verticalize.conteudo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.concurso.presentation.ConcursoDetailAction
import br.com.zamfir.verticalize.concurso.presentation.ConcursoDetailState
import br.com.zamfir.verticalize.concurso.presentation.blocosDisponiveisParaFiltro
import br.com.zamfir.verticalize.concurso.presentation.eixosDisponiveisParaFiltro
import br.com.zamfir.verticalize.concurso.presentation.isConteudoFiltroAtivo
import br.com.zamfir.verticalize.conteudo.domain.Prioridade
import br.com.zamfir.verticalize.conteudo.presentation.labelRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConteudoFiltroBottomSheet(
    state: ConcursoDetailState,
    onAction: (ConcursoDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = { onAction(ConcursoDetailAction.OnConteudoFiltroDismiss) },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.conteudo_filtro_titulo),
                style = MaterialTheme.typography.titleLarge
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
                            selected = prioridade in state.conteudoFiltroPrioridadesSelecionados,
                            onClick = { onAction(ConcursoDetailAction.OnConteudoFiltroPrioridadeToggle(prioridade)) },
                            label = { Text(stringResource(prioridade.labelRes())) }
                        )
                    }
                }
            }

            if (state.eixosDisponiveisParaFiltro.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.label_eixo),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.eixosDisponiveisParaFiltro.forEach { eixo ->
                            FilterChip(
                                selected = eixo in state.conteudoFiltroEixosSelecionados,
                                onClick = { onAction(ConcursoDetailAction.OnConteudoFiltroEixoToggle(eixo)) },
                                label = { Text(stringResource(R.string.eixo_label_format, eixo)) }
                            )
                        }
                    }
                }
            }

            if (state.blocosDisponiveisParaFiltro.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.label_bloco),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.blocosDisponiveisParaFiltro.forEach { bloco ->
                            FilterChip(
                                selected = bloco in state.conteudoFiltroBlocosSelecionados,
                                onClick = { onAction(ConcursoDetailAction.OnConteudoFiltroBlocoToggle(bloco)) },
                                label = { Text(stringResource(R.string.bloco_label_format, bloco)) }
                            )
                        }
                    }
                }
            }

            TextButton(
                onClick = { onAction(ConcursoDetailAction.OnConteudoFiltroLimparClick) },
                enabled = state.isConteudoFiltroAtivo
            ) {
                Text(stringResource(R.string.conteudo_filtro_limpar))
            }
        }
    }
}
