package br.com.zamfir.verticalize.auth.data

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import br.com.zamfir.verticalize.auth.domain.AuthError
import br.com.zamfir.verticalize.auth.domain.AuthRepository
import br.com.zamfir.verticalize.auth.domain.AuthUser
import br.com.zamfir.verticalize.core.domain.Result
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val context: Context
) : AuthRepository {

    override fun observeCurrentUser(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser, AuthError> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val user = firebaseAuth.signInWithCredential(credential).await().user
                ?: return Result.Error(AuthError.DESCONHECIDO)
            Result.Success(user.toAuthUser())
        } catch (e: CancellationException) {
            throw e
        } catch (_: FirebaseNetworkException) {
            Result.Error(AuthError.SEM_CONEXAO)
        } catch (e: Exception) {
            Log.w("FirebaseAuthRepository", "Falha ao autenticar no Firebase com a conta Google", e)
            Result.Error(AuthError.DESCONHECIDO)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        try {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // O logout no Firebase já aconteceu; falhar aqui só faz o seletor pular a escolha da conta.
        }
    }
}

private fun FirebaseUser.toAuthUser(): AuthUser = AuthUser(
    id = uid,
    nome = displayName,
    email = email,
    fotoUrl = photoUrl?.toString()
)
