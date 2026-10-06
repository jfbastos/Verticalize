package br.com.zamfir.verticalize.conteudo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.concurso.presentation.ConcursoDetailAction
import br.com.zamfir.verticalize.concurso.presentation.ConcursoDetailState
import br.com.zamfir.verticalize.conteudo.presentation.ConteudoAgrupamento

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConteudoAgrupamentoBottomSheet(
    state: ConcursoDetailState,
    onAction: (ConcursoDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = { onAction(ConcursoDetailAction.OnConteudoAgrupamentoDismiss) },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.conteudo_agrupamento_titulo),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            ConteudoAgrupamento.entries.forEach { opcao ->
                val selected = opcao == state.conteudoAgrupamento
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(
                            if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                        )
                        .clickable { onAction(ConcursoDetailAction.OnConteudoAgrupamentoSelected(opcao)) }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selected,
                        onClick = { onAction(ConcursoDetailAction.OnConteudoAgrupamentoSelected(opcao)) }
                    )
                    Text(
                        text = stringResource(opcao.labelRes),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}
