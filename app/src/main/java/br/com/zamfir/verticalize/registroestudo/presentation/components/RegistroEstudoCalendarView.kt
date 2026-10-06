package br.com.zamfir.verticalize.registroestudo.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.registroestudo.presentation.RegistroEstudoUi
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val WEEKDAY_LABELS = listOf("D", "S", "T", "Q", "Q", "S", "S")
private val MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale("pt", "BR"))

/** Grade mensal (domingo a sábado) com a matéria e o tempo de estudo de cada registro no seu dia. */
@Composable
fun RegistroEstudoCalendarView(
    registros: List<RegistroEstudoUi>,
    month: YearMonth,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val registrosPorDia = remember(registros) {
        registros.groupBy { Instant.ofEpochMilli(it.data).atZone(ZoneOffset.UTC).toLocalDate() }
    }
    val weeks = remember(month) { month.toCalendarWeeks() }
    val monthLabel = remember(month) {
        month.atDay(1).format(MONTH_LABEL_FORMATTER).replaceFirstChar { it.uppercase() }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonthClick) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.registro_estudo_calendario_mes_anterior_cd)
                )
            }
            Text(text = monthLabel, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onNextMonthClick) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.registro_estudo_calendario_proximo_mes_cd)
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            WEEKDAY_LABELS.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        weeks.forEach { week ->
            // IntrinsicSize.Min faz a linha assumir a altura da célula mais alta, e o fillMaxHeight()
            // de cada célula estica as demais até lá — todas as células da semana ficam com a mesma altura.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(top = 4.dp)
            ) {
                week.forEach { day ->
                    RegistroEstudoCalendarDayCell(
                        day = day,
                        registros = day?.let { registrosPorDia[it] }.orEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}

/** Divide o mês em semanas de domingo a sábado; `null` marca as células vazias antes/depois do mês. */
private fun YearMonth.toCalendarWeeks(): List<List<LocalDate?>> {
    val firstDay = atDay(1)
    // DayOfWeek.value é 1=segunda..7=domingo (ISO); "% 7" traz domingo para 0, a primeira coluna.
    val leadingBlanks = firstDay.dayOfWeek.value % 7
    val dias = (1..lengthOfMonth()).map { atDay(it) }
    val trailingBlanks = (7 - (leadingBlanks + dias.size) % 7) % 7
    val celulas = List<LocalDate?>(leadingBlanks) { null } + dias + List<LocalDate?>(trailingBlanks) { null }
    return celulas.chunked(7)
}

@Composable
private fun RegistroEstudoCalendarDayCell(
    day: LocalDate?,
    registros: List<RegistroEstudoUi>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 64.dp)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (day != null) {
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            registros.forEachIndexed { index, registro ->
                // Separa visualmente uma matéria da outra quando há mais de um registro no mesmo dia.
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = registro.materia,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = registro.duracaoFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegistroEstudoCalendarViewPreview() {
    VerticalizeTheme {
        RegistroEstudoCalendarView(
            registros = listOf(
                RegistroEstudoUi(
                    id = 1L,
                    materia = "Direito Constitucional",
                    dataFormatted = "17/09/2026",
                    duracaoFormatted = "2h30min",
                    data = YearMonth.now(ZoneOffset.UTC).atDay(5).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                )
            ),
            month = YearMonth.now(ZoneOffset.UTC),
            onPreviousMonthClick = {},
            onNextMonthClick = {},
            contentPadding = PaddingValues(16.dp)
        )
    }
}
