package br.com.zamfir.verticalize.concurso.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.core.presentation.components.VerticalizeDeleteDialog

@Composable
fun ConcursoDeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    VerticalizeDeleteDialog(
        title = stringResource(R.string.concurso_delete_confirmation_title),
        message = stringResource(R.string.concurso_delete_confirmation_message),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
