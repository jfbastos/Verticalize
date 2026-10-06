package br.com.zamfir.verticalize.auth.presentation

import br.com.zamfir.verticalize.auth.domain.AuthUser

data class ContaUi(
    val nome: String?,
    val email: String?,
    val fotoUrl: String? = null
) {
    /** Letra exibida no avatar: inicial do nome, ou do e-mail quando a conta não tem nome. */
    val inicial: String
        get() = (nome?.takeIf { it.isNotBlank() } ?: email)
            ?.trim()
            ?.firstOrNull()
            ?.uppercase()
            ?: "?"
}

fun AuthUser.toContaUi(): ContaUi = ContaUi(nome = nome, email = email, fotoUrl = fotoUrl)
