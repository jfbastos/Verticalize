package br.com.zamfir.verticalize.concurso.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.concurso.presentation.ConcursoUi
import br.com.zamfir.verticalize.concurso.presentation.labelRes
import br.com.zamfir.verticalize.core.presentation.components.InfoChip
import br.com.zamfir.verticalize.core.presentation.components.ProgressRing
import br.com.zamfir.verticalize.core.presentation.components.SwipeEditDeleteBox
import br.com.zamfir.verticalize.core.presentation.components.VerticalizeCard
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConcursoListItem(
    concurso: ConcursoUi,
    onEditRequested: () -> Unit,
    onDeleteRequested: () -> Unit,
    onDetailRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    SwipeEditDeleteBox(
        onEditRequested = onEditRequested,
        onDeleteRequested = onDeleteRequested,
        modifier = modifier
    ) {
        VerticalizeCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = dropUnlessResumed { onDetailRequested() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = concurso.nome,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoChip(
                            text = stringResource(concurso.nivel.labelRes()),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        InfoChip(
                            text = stringResource(R.string.concurso_horas_estudadas_format, concurso.horasEstudadas),
                            icon = Icons.Filled.Schedule
                        )
                    }
                }
                ProgressRing(percentual = concurso.percentualCompletude)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConcursoListItemPreview() {
    VerticalizeTheme {
        ConcursoListItem(
            concurso = ConcursoUi(
                id = 1L,
                nome = "Concurso TRT 2ª Região",
                nivel = Nivel.SUPERIOR,
                horasEstudadas = 12,
                percentualCompletude = 35
            ),
            onEditRequested = {},
            onDeleteRequested = {},
            onDetailRequested = {}
        )
    }
}
