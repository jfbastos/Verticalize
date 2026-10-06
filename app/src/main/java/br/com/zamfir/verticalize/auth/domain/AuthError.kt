package br.com.zamfir.verticalize.auth.domain

import br.com.zamfir.verticalize.core.domain.Error

enum class AuthError : Error {
    CANCELADO,

    /** Nenhuma conta Google disponível no aparelho. */
    SEM_CONTA,
    SEM_CONEXAO,

    /** Web Client ID ausente em auth_config.xml: o login com Google ainda não foi configurado. */
    NAO_CONFIGURADO,
    DESCONHECIDO
}
