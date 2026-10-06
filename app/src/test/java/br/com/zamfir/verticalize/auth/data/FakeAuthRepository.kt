package br.com.zamfir.verticalize.auth.data

import br.com.zamfir.verticalize.auth.domain.AuthError
import br.com.zamfir.verticalize.auth.domain.AuthRepository
import br.com.zamfir.verticalize.auth.domain.AuthUser
import br.com.zamfir.verticalize.core.domain.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository : AuthRepository {
    private val userFlow = MutableStateFlow<AuthUser?>(null)
    var signInError: AuthError? = null
    var userToSignIn = AuthUser(id = "uid-1", nome = "Maria Souza", email = "maria@gmail.com", fotoUrl = null)
    var lastIdToken: String? = null

    fun setCurrentUser(user: AuthUser?) {
        userFlow.value = user
    }

    override fun observeCurrentUser(): Flow<AuthUser?> = userFlow

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser, AuthError> {
        lastIdToken = idToken
        signInError?.let { return Result.Error(it) }
        userFlow.value = userToSignIn
        return Result.Success(userToSignIn)
    }

    override suspend fun signOut() {
        userFlow.value = null
    }
}
