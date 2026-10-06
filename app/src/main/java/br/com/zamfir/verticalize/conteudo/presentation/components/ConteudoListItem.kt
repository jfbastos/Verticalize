package br.com.zamfir.verticalize.conteudo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.conteudo.domain.Prioridade
import br.com.zamfir.verticalize.conteudo.presentation.ConteudoUi
import br.com.zamfir.verticalize.conteudo.presentation.labelRes
import br.com.zamfir.verticalize.conteudo.presentation.toHorasMinutosFormatted
import br.com.zamfir.verticalize.core.presentation.components.InfoChip
import br.com.zamfir.verticalize.core.presentation.components.VerticalizeCard
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConteudoListItem(
    conteudo: ConteudoUi,
    onTapped: () -> Unit,
    onConcluidoToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onLongPress: () -> Unit = {},
    emSelecao: Boolean = false,
    selecionado: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    val concluido = conteudo.concluido

    VerticalizeCard(
        modifier = modifier.fillMaxWidth(),
        onClick = dropUnlessResumed { onTapped() },
        onLongClick = onLongPress,
        containerColor = when {
            selecionado -> colors.primaryContainer
            concluido -> colors.tertiaryContainer.copy(alpha = 0.4f)
            else -> colors.surfaceContainerLow
        },
        borderColor = when {
            selecionado -> colors.primary
            concluido -> colors.tertiary.copy(alpha = 0.3f)
            else -> colors.outlineVariant
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (conteudo.materia.isNotBlank()) {
                    Text(
                        text = conteudo.materia,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (concluido) colors.tertiary else colors.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = conteudo.descricao,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                val totalMinutosAula = conteudo.quantidadeAulas * conteudo.tempoMedioAulaMinutos

                FlowRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (concluido) {
                        InfoChip(
                            text = stringResource(R.string.conteudo_concluido_label),
                            icon = Icons.Filled.CheckCircle,
                            containerColor = colors.tertiaryContainer,
                            contentColor = colors.onTertiaryContainer
                        )
                    }
                    InfoChip(
                        text = stringResource(conteudo.prioridade.labelRes()),
                        containerColor = conteudo.prioridade.containerColor(colors),
                        contentColor = conteudo.prioridade.contentColor(colors)
                    )
                    conteudo.eixo?.let {
                        InfoChip(text = stringResource(R.string.eixo_label_format, it))
                    }
                    conteudo.bloco?.let {
                        InfoChip(text = stringResource(R.string.bloco_label_format, it))
                    }
                    if (totalMinutosAula > 0) {
                        InfoChip(
                            text = stringResource(
                                R.string.conteudo_horas_aula_format,
                                totalMinutosAula.toHorasMinutosFormatted()
                            ),
                            icon = Icons.Filled.Schedule
                        )
                    }
                    conteudo.dataUltimaRevisaoFormatted?.let { data ->
                        InfoChip(
                            text = stringResource(R.string.conteudo_ultima_revisao_format, data),
                            icon = Icons.Filled.History
                        )
                    }
                    if (conteudo.quantidadeQuestoes > 0) {
                        InfoChip(
                            text = stringResource(
                                R.string.conteudo_questoes_format,
                                conteudo.quantidadeQuestoes
                            ),
                            icon = Icons.Filled.Quiz
                        )
                    }
                }
            }

            // Durante a seleção o controle à direita passa a marcar o item, não a conclusão.
            if (emSelecao) {
                Checkbox(checked = selecionado, onCheckedChange = { onTapped() })
            } else {
                Switch(checked = concluido, onCheckedChange = onConcluidoToggle)
            }
        }
    }
}

/** Quanto mais urgente a prioridade, mais forte a cor do chip; opcional fica discreto. */
private fun Prioridade.containerColor(colors: ColorScheme): Color = when (this) {
    Prioridade.ALTA -> colors.errorContainer
    Prioridade.MEDIA -> colors.primaryContainer
    Prioridade.BAIXA -> colors.secondaryContainer
    Prioridade.OPCIONAL -> colors.surfaceVariant
}

private fun Prioridade.contentColor(colors: ColorScheme): Color = when (this) {
    Prioridade.ALTA -> colors.onErrorContainer
    Prioridade.MEDIA -> colors.onPrimaryContainer
    Prioridade.BAIXA -> colors.onSecondaryContainer
    Prioridade.OPCIONAL -> colors.onSurfaceVariant
}

@Preview(showBackground = true)
@Composable
private fun ConteudoListItemPreview() {
    VerticalizeTheme {
        ConteudoListItem(
            conteudo = ConteudoUi(
                id = 1L,
                materia = "Direito Administrativo",
                descricao = "Princípios administrativos e regime jurídico-administrativo",
                eixo = 1,
                bloco = 2,
                concluido = false,
                quantidadeAulas = 5,
                tempoMedioAulaMinutos = 50,
                dataUltimaRevisaoFormatted = "10/09/2026",
                dataUltimaRevisaoMillis = 1_757_500_000_000L,
                quantidadeQuestoes = 0,
                prioridade = Prioridade.ALTA
            ),
            onTapped = {},
            onConcluidoToggle = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConteudoListItemConcluidoPreview() {
    VerticalizeTheme {
        ConteudoListItem(
            conteudo = ConteudoUi(
                id = 1L,
                materia = "Direito Administrativo",
                descricao = "Princípios administrativos",
                eixo = 1,
                bloco = 2,
                concluido = true,
                quantidadeAulas = 5,
                tempoMedioAulaMinutos = 50,
                dataUltimaRevisaoFormatted = "10/09/2026",
                dataUltimaRevisaoMillis = 1_757_500_000_000L,
                quantidadeQuestoes = 10,
                prioridade = Prioridade.OPCIONAL
            ),
            onTapped = {},
            onConcluidoToggle = {}
        )
    }
}
