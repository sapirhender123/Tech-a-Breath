package com.example.tech_a_breath.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class GoogleAuthClient(private val context: Context) {

    companion object {
        private const val WEB_CLIENT_ID =
            "56698945375-ognmudb43pt4e99c3m44dc1akfg64acg.apps.googleusercontent.com"
    }

    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(activityContext: Context): SignInResult {
        val googleIdOption = GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                context = activityContext,
                request = request,
            )
            val googleCredential = GoogleIdTokenCredential
                .createFrom(result.credential.data)

            SignInResult.Success(
                GoogleUser(
                    idToken = googleCredential.idToken,
                    email = googleCredential.id,
                    displayName = googleCredential.displayName,
                )
            )
        } catch (e: GetCredentialCancellationException) {
            SignInResult.Cancelled
        } catch (e: Exception) {
            SignInResult.Failure(e.javaClass.simpleName + ": " + (e.message ?: "No message"))
        }
    }
}
