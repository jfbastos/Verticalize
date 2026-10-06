package br.com.zamfir.verticalize.auth.presentation

import androidx.annotation.StringRes
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.auth.domain.AuthError

@StringRes
fun AuthError.messageRes(): Int? {
    return when (this) {
        AuthError.CANCELADO -> null
        AuthError.SEM_CONTA -> R.string.auth_error_sem_conta
        AuthError.SEM_CONEXAO -> R.string.auth_error_sem_conexao
        AuthError.NAO_CONFIGURADO -> R.string.auth_error_nao_configurado
        AuthError.DESCONHECIDO -> R.string.auth_error_desconhecido
    }
}
