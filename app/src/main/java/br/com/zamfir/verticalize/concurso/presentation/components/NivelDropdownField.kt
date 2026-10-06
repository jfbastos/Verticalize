package br.com.zamfir.verticalize.concurso.presentation.components

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.concurso.domain.Nivel
import br.com.zamfir.verticalize.concurso.presentation.labelRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NivelDropdownField(
    selected: Nivel,
    expanded: Boolean,
    onExpandedChange: () -> Unit,
    onNivelSelected: (Nivel) -> Unit,
    modifier: Modifier = Modifier
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange() },
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            value = stringResource(selected.labelRes()),
            onValueChange = {},
            label = { Text(stringResource(R.string.label_nivel)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = onExpandedChange
        ) {
            Nivel.entries.forEach { nivel ->
                DropdownMenuItem(
                    text = { Text(stringResource(nivel.labelRes())) },
                    onClick = { onNivelSelected(nivel) }
                )
            }
        }
    }
}
