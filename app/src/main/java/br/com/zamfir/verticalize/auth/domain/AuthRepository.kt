package br.com.zamfir.verticalize.auth.domain

import br.com.zamfir.verticalize.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeCurrentUser(): Flow<AuthUser?>

    suspend fun signInWithGoogle(idToken: String): Result<AuthUser, AuthError>

    suspend fun signOut()
}
