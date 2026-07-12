package com.example.tech_a_breath.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Wraps the Jetpack Credential Manager "Sign in with Google" flow.
 *
 * Replace [WEB_CLIENT_ID] with the OAuth 2.0 Web client ID from your
 * Google Cloud Console (the same one used for Firebase / backend verification).
 */
class GoogleAuthClient(private val context: Context) {

    companion object {
        // TODO: replace with your actual Web client ID from Google Cloud Console.
        private const val WEB_CLIENT_ID =
            "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"
    }

    private val credentialManager = CredentialManager.create(context)

    /**
     * Launches the Credential Manager bottom sheet and suspends until the user
     * completes, cancels, or an error occurs.
     */
    suspend fun signIn(): SignInResult {
        val googleIdOption = GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                request = request,
                context = context,
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
            SignInResult.Failure(e.message ?: "Sign-in failed")
        }
    }
}
