package br.com.zamfir.verticalize.conteudo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.conteudo.domain.Prioridade
import br.com.zamfir.verticalize.conteudo.presentation.ConteudoUi
import br.com.zamfir.verticalize.conteudo.presentation.labelRes
import br.com.zamfir.verticalize.conteudo.presentation.toHorasMinutosFormatted
import br.com.zamfir.verticalize.core.presentation.components.InfoChip
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme

/**
 * Detalhes de um conteúdo, só para leitura. O lápis no título leva à edição.
 * Matéria e descrição podem ser longas, por isso ficam abaixo das demais informações, com o texto completo.
 */
@Composable
fun ConteudoDetailDialog(
    conteudo: ConteudoUi,
    onEdit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.conteudo_detalhe_title),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.conteudo_detalhe_edit_cd)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(label = stringResource(R.string.label_situacao)) {
                    if (conteudo.concluido) {
                        InfoChip(
                            text = stringResource(R.string.conteudo_concluido_label),
                            icon = Icons.Filled.CheckCircle,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    } else {
                        InfoChip(text = stringResource(R.string.conteudo_pendente_label))
                    }
                }
                InfoTextRow(
                    label = stringResource(R.string.label_prioridade),
                    value = stringResource(conteudo.prioridade.labelRes())
                )
                InfoTextRow(
                    label = stringResource(R.string.label_eixo),
                    value = conteudo.eixo?.toString()
                )
                InfoTextRow(
                    label = stringResource(R.string.label_bloco),
                    value = conteudo.bloco?.toString()
                )
                InfoTextRow(
                    label = stringResource(R.string.label_quantidade_aulas),
                    value = conteudo.quantidadeAulas.toString()
                )
                InfoTextRow(
                    label = stringResource(R.string.conteudo_detalhe_tempo_medio),
                    value = stringResource(R.string.conteudo_detalhe_minutos_format, conteudo.tempoMedioAulaMinutos)
                )
                InfoTextRow(
                    label = stringResource(R.string.conteudo_detalhe_tempo_total),
                    value = (conteudo.quantidadeAulas * conteudo.tempoMedioAulaMinutos).toHorasMinutosFormatted()
                )
                InfoTextRow(
                    label = stringResource(R.string.label_data_ultima_revisao),
                    value = conteudo.dataUltimaRevisaoFormatted
                )
                InfoTextRow(
                    label = stringResource(R.string.label_quantidade_questoes),
                    value = conteudo.quantidadeQuestoes.toString()
                )

                HorizontalDivider()

                LongTextSection(
                    label = stringResource(R.string.label_materia),
                    text = conteudo.materia
                )
                LongTextSection(
                    label = stringResource(R.string.label_descricao),
                    text = conteudo.descricao
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

/** Uma informação curta: rótulo à esquerda e o valor (qualquer conteúdo) à direita. */
@Composable
private fun InfoRow(
    label: String,
    value: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        value()
    }
}

@Composable
private fun InfoTextRow(label: String, value: String?) {
    InfoRow(label = label) {
        Text(
            text = value ?: stringResource(R.string.valor_nao_informado),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.End
        )
    }
}

/** Texto potencialmente longo (matéria/descrição): rótulo pequeno e o texto completo embaixo, sem cortar. */
@Composable
private fun LongTextSection(label: String, text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text.ifBlank { stringResource(R.string.valor_nao_informado) },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConteudoDetailDialogPreview() {
    VerticalizeTheme {
        ConteudoDetailDialog(
            conteudo = ConteudoUi(
                id = 1L,
                materia = "Direito Administrativo",
                descricao = "Princípios administrativos",
                eixo = 1,
                bloco = 2,
                concluido = false,
                quantidadeAulas = 5,
                quantidadeQuestoes = 10,
                tempoMedioAulaMinutos = 50,
                dataUltimaRevisaoFormatted = "10/09/2026",
                prioridade = Prioridade.ALTA
            ),
            onEdit = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConteudoDetailDialogLongTextPreview() {
    VerticalizeTheme {
        ConteudoDetailDialog(
            conteudo = ConteudoUi(
                id = 2L,
                materia = "Direito Constitucional e Direitos Humanos — Teoria Geral dos Direitos Fundamentais",
                descricao = "Controle de constitucionalidade: sistemas difuso e concentrado, ação direta de " +
                    "inconstitucionalidade, ação declaratória de constitucionalidade, arguição de descumprimento " +
                    "de preceito fundamental, súmula vinculante e efeitos da decisão no tempo e no espaço.",
                eixo = null,
                bloco = null,
                concluido = true,
                quantidadeAulas = 12,
                quantidadeQuestoes = 0,
                tempoMedioAulaMinutos = 45,
                dataUltimaRevisaoFormatted = null,
                prioridade = Prioridade.OPCIONAL
            ),
            onEdit = {},
            onDismiss = {}
        )
    }
}
