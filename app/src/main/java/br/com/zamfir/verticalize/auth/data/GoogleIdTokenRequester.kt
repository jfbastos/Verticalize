package br.com.zamfir.verticalize.auth.data

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import br.com.zamfir.verticalize.R
import br.com.zamfir.verticalize.auth.domain.AuthError
import br.com.zamfir.verticalize.core.domain.Result
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

class GoogleIdTokenRequester {

    suspend fun requestIdToken(activityContext: Context): Result<String, AuthError> {
        val serverClientId = activityContext.getString(R.string.google_web_client_id)
        if (!serverClientId.endsWith(WEB_CLIENT_ID_SUFFIX)) {
            Log.w(TAG, "google_web_client_id não configurado em auth_config.xml")
            return Result.Error(AuthError.NAO_CONFIGURADO)
        }

        val option = GetSignInWithGoogleOption
            .Builder(serverClientId = serverClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        return try {
            val credential = CredentialManager.create(activityContext)
                .getCredential(activityContext, request)
                .credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                Result.Success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
            } else {
                Result.Error(AuthError.DESCONHECIDO)
            }
        } catch (_: GetCredentialCancellationException) {
            Result.Error(AuthError.CANCELADO)
        } catch (_: NoCredentialException) {
            Result.Error(AuthError.SEM_CONTA)
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Falha ao obter credencial Google (${e.type})", e)
            Result.Error(AuthError.DESCONHECIDO)
        } catch (e: GoogleIdTokenParsingException) {
            Log.w(TAG, "ID token do Google inválido", e)
            Result.Error(AuthError.DESCONHECIDO)
        }
    }

    private companion object {
        const val TAG = "GoogleIdTokenRequester"
        const val WEB_CLIENT_ID_SUFFIX = ".apps.googleusercontent.com"
    }
}
