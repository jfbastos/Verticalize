package br.com.zamfir.verticalize.auth.domain

data class AuthUser(
    val id: String,
    val nome: String?,
    val email: String?,
    val fotoUrl: String?
)
