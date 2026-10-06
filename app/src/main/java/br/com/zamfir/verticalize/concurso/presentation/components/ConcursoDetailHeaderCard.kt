package br.com.zamfir.verticalize.concurso.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.concurso.presentation.ConcursoDetailUi
import br.com.zamfir.verticalize.concurso.presentation.EixoUi
import br.com.zamfir.verticalize.concurso.presentation.labelRes
import br.com.zamfir.verticalize.conteudo.presentation.toHorasMinutosFormatted
import br.com.zamfir.verticalize.core.presentation.components.InfoChip
import br.com.zamfir.verticalize.core.presentation.components.LabeledProgressBar
import br.com.zamfir.verticalize.core.presentation.components.ProgressRing
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme

@Composable
fun ConcursoDetailHeaderCard(
    concurso: ConcursoDetailUi,
    eixos: List<EixoUi>,
    totalMinutosAula: Int,
    totalMinutosAulaAssistidos: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(gradient)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = concurso.nome,
                        style = MaterialTheme.typography.headlineSmall,
                        color = contentColor,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    InfoChip(
                        text = stringResource(concurso.nivel.labelRes()),
                        containerColor = contentColor.copy(alpha = 0.12f),
                        contentColor = contentColor
                    )
                }
                ProgressRing(
                    percentual = concurso.percentualCompletude,
                    size = 88.dp,
                    strokeWidth = 10.dp,
                    progressColor = MaterialTheme.colorScheme.primary,
                    trackColor = contentColor.copy(alpha = 0.15f),
                    textColor = contentColor,
                    textStyle = MaterialTheme.typography.titleMedium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DataProvaTile(
                    label = stringResource(R.string.concurso_stat_data_prova),
                    value = concurso.dataProvaFormatted ?: stringResource(R.string.concurso_data_prova_a_definir),
                    caption = concurso.diasParaProva?.let { diasRestantesCaption(it) },
                    contentColor = contentColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.concurso_detail_detalhes_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = contentColor
                )
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = stringResource(
                            if (isExpanded) {
                                R.string.concurso_detail_collapse_cd
                            } else {
                                R.string.concurso_detail_expand_cd
                            }
                        ),
                        tint = contentColor
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HeaderInfoRow(
                            label = stringResource(R.string.concurso_stat_horas_estudadas),
                            value = "${concurso.horasEstudadas}h",
                            contentColor = contentColor
                        )
                        HeaderInfoRow(
                            label = stringResource(R.string.concurso_stat_total_horas_aula),
                            value = totalMinutosAula.toHorasMinutosFormatted(),
                            contentColor = contentColor
                        )
                        if (totalMinutosAulaAssistidos > 0) {
                            HeaderInfoRow(
                                label = stringResource(R.string.concurso_stat_horas_assistidas),
                                value = totalMinutosAulaAssistidos.toHorasMinutosFormatted(),
                                contentColor = contentColor
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = stringResource(R.string.concurso_detail_eixos_title),
                            style = MaterialTheme.typography.labelLarge,
                            color = contentColor.copy(alpha = 0.85f)
                        )
                        if (eixos.isEmpty()) {
                            Text(
                                text = stringResource(R.string.concurso_detail_eixos_empty_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = contentColor.copy(alpha = 0.8f)
                            )
                        } else {
                            eixos.forEach { eixo ->
                                LabeledProgressBar(
                                    label = stringResource(R.string.eixo_label_format, eixo.numero),
                                    percentual = eixo.percentualCompletude,
                                    trackColor = contentColor.copy(alpha = 0.15f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderInfoRow(label: String, value: String, contentColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor.copy(alpha = 0.85f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = contentColor
        )
    }
}

@Composable
private fun DataProvaTile(
    label: String,
    value: String,
    contentColor: Color,
    modifier: Modifier = Modifier,
    caption: String? = null
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = contentColor.copy(alpha = 0.08f),
        contentColor = contentColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (caption != null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun diasRestantesCaption(dias: Long): String? {
    return when {
        dias > 0 -> {
            val dia = dias.toInt()
            val semanas = (dias / 7).toInt()
            pluralStringResource(R.plurals.concurso_dias_restantes_format, dia, dia) +
                " (" + pluralStringResource(R.plurals.concurso_semanas_restantes_format, semanas, semanas) + ")"
        }
        dias == 0L -> stringResource(R.string.concurso_prova_hoje)
        else -> null
    }
}

@Preview(showBackground = true)
@Composable
private fun ConcursoDetailHeaderCardCollapsedPreview() {
    VerticalizeTheme {
        ConcursoDetailHeaderCard(
            concurso = ConcursoDetailUi(
                id = 1L,
                nome = "Concurso TRT 2ª Região",
                nivel = Nivel.SUPERIOR,
                dataProvaFormatted = "15/03/2027",
                diasParaProva = 42L,
                horasEstudadas = 12,
                percentualCompletude = 35
            ),
            eixos = listOf(EixoUi(numero = 1, percentualCompletude = 40), EixoUi(numero = 2, percentualCompletude = 65)),
            totalMinutosAula = 600,
            totalMinutosAulaAssistidos = 180,
            isExpanded = false,
            onToggleExpand = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConcursoDetailHeaderCardExpandedPreview() {
    VerticalizeTheme {
        ConcursoDetailHeaderCard(
            concurso = ConcursoDetailUi(
                id = 1L,
                nome = "Concurso TRT 2ª Região",
                nivel = Nivel.SUPERIOR,
                dataProvaFormatted = null,
                diasParaProva = null,
                horasEstudadas = 12,
                percentualCompletude = 35
            ),
            eixos = listOf(EixoUi(numero = 1, percentualCompletude = 40), EixoUi(numero = 2, percentualCompletude = 65)),
            totalMinutosAula = 600,
            totalMinutosAulaAssistidos = 180,
            isExpanded = true,
            onToggleExpand = {}
        )
    }
}
