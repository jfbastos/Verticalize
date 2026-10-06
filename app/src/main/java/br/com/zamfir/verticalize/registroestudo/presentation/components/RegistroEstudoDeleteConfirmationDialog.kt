package br.com.zamfir.verticalize.registroestudo.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.core.presentation.components.VerticalizeDeleteDialog

@Composable
fun RegistroEstudoDeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    VerticalizeDeleteDialog(
        title = stringResource(R.string.registro_estudo_delete_confirmation_title),
        message = stringResource(R.string.registro_estudo_delete_confirmation_message),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
