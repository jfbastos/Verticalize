package br.com.zamfir.verticalize.auth.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.auth.presentation.ContaUi
import br.com.zamfir.verticalize.ui.theme.VerticalizeTheme
import coil3.compose.AsyncImage

@Composable
fun ContaActionButton(
    conta: ContaUi?,
    isSigningIn: Boolean,
    isMenuExpanded: Boolean,
    onClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val signingInDescription = stringResource(R.string.conta_entrando_cd)

    Box(modifier = modifier) {
        IconButton(onClick = onClick, enabled = !isSigningIn) {
            when {
                isSigningIn -> CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp)
                        .semantics { contentDescription = signingInDescription },
                    strokeWidth = 2.dp
                )

                conta == null -> Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = stringResource(R.string.conta_entrar_cd)
                )

                else -> ContaAvatar(
                    fotoUrl = conta.fotoUrl,
                    inicial = conta.inicial,
                    contentDescription = stringResource(R.string.conta_menu_cd)
                )
            }
        }

        if (conta != null) {
            DropdownMenu(expanded = isMenuExpanded, onDismissRequest = onMenuDismiss) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    conta.nome?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    conta.email?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.conta_sair)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    },
                    onClick = onSignOutClick
                )
            }
        }
    }
}

/**
 * Foto da conta Google; enquanto carrega, ou se a conta não tem foto ou a carga falha, mostra a
 * inicial do nome no lugar.
 */
@Composable
private fun ContaAvatar(
    fotoUrl: String?,
    inicial: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = inicial,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        if (fotoUrl != null) {
            AsyncImage(
                model = fotoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ContaActionButtonLoggedOutPreview() {
    VerticalizeTheme {
        ContaActionButton(
            conta = null,
            isSigningIn = false,
            isMenuExpanded = false,
            onClick = {},
            onMenuDismiss = {},
            onSignOutClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ContaActionButtonLoggedInPreview() {
    VerticalizeTheme {
        ContaActionButton(
            conta = ContaUi(nome = "Maria Souza", email = "maria@gmail.com"),
            isSigningIn = false,
            isMenuExpanded = false,
            onClick = {},
            onMenuDismiss = {},
            onSignOutClick = {}
        )
    }
}
