package br.com.zamfir.verticalize.sync.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.sync.domain.ResumoDados
import br.com.zamfir.verticalize.sync.presentation.ConflitoSyncUi
import br.com.zamfir.verticalize.sync.presentation.formatAtualizadoEm
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme

/**
 * Pergunta qual lado manter quando aparelho e nuvem têm dados diferentes no login. Não fecha ao tocar
 * fora: a sincronização só começa depois da escolha, e o lado não escolhido é substituído.
 */
@Composable
fun SyncConflitoDialog(
    conflito: ConflitoSyncUi,
    onUsarNuvem: () -> Unit,
    onManterAparelho: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        icon = { Icon(imageVector = Icons.Filled.CloudSync, contentDescription = null) },
        title = { Text(stringResource(R.string.sync_conflito_titulo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.sync_conflito_mensagem))
                ResumoLado(
                    titulo = conflito.nuvem.atualizadoEm
                        ?.let { stringResource(R.string.sync_conflito_nuvem_data_format, formatAtualizadoEm(it)) }
                        ?: stringResource(R.string.sync_conflito_nuvem),
                    resumo = conflito.nuvem
                )
                ResumoLado(titulo = stringResource(R.string.sync_conflito_aparelho), resumo = conflito.aparelho)
            }
        },
        confirmButton = {
            Button(onClick = onUsarNuvem) {
                Text(stringResource(R.string.sync_conflito_usar_nuvem))
            }
        },
        dismissButton = {
            TextButton(onClick = onManterAparelho) {
                Text(stringResource(R.string.sync_conflito_manter_aparelho))
            }
        }
    )
}

@Composable
private fun ResumoLado(titulo: String, resumo: ResumoDados) {
    Column {
        Text(text = titulo, style = MaterialTheme.typography.titleSmall)
        Text(
            text = stringResource(
                R.string.sync_conflito_resumo_format,
                resumo.concursos,
                resumo.conteudos,
                resumo.estudos
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun SyncConflitoDialogPreview() {
    VerticalizeTheme {
        SyncConflitoDialog(
            conflito = ConflitoSyncUi(
                aparelho = ResumoDados(concursos = 1, conteudos = 12, estudos = 3),
                nuvem = ResumoDados(concursos = 3, conteudos = 85, estudos = 40, atualizadoEm = 1_790_000_000_000L)
            ),
            onUsarNuvem = {},
            onManterAparelho = {}
        )
    }
}
