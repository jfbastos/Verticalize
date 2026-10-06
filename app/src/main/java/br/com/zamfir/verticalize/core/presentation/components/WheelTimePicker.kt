package br.com.zamfir.verticalize.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Seletor de horário (HH:mm) combinando dois [WheelNumberPicker]. O valor inicial só é
 * aplicado na primeira composição — reabrir com um novo default requer descartar e
 * recompor este composable (ex.: dialog sendo remontado do zero).
 */
@Composable
fun WheelTimePicker(
    initialMinutosDoDia: Int,
    onMinutosDoDiaChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    require(initialMinutosDoDia in 0..1439) { "initialMinutosDoDia deve estar entre 0 e 1439" }

    var hora by remember { mutableIntStateOf(initialMinutosDoDia / 60) }
    var minuto by remember { mutableIntStateOf(initialMinutosDoDia % 60) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        WheelNumberPicker(
            range = 0..23,
            initialValue = hora,
            onValueChange = {
                hora = it
                onMinutosDoDiaChange(hora * 60 + minuto)
            },
            modifier = Modifier.weight(1f)
        )
        Text(
            text = ":",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        WheelNumberPicker(
            range = 0..59,
            initialValue = minuto,
            onValueChange = {
                minuto = it
                onMinutosDoDiaChange(hora * 60 + minuto)
            },
            modifier = Modifier.weight(1f)
        )
    }
}
