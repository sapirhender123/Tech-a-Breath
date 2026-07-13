package com.example.tech_a_breath.auth

/** Sealed result returned by [GoogleAuthClient.signIn]. */
sealed class SignInResult {
    /** The user completed the flow and we have a valid credential. */
    data class Success(val user: GoogleUser) : SignInResult()

    /** The user dismissed the bottom sheet or pressed back. */
    object Cancelled : SignInResult()

    /** An unexpected error occurred; [message] is safe to show in the UI. */
    data class Failure(val message: String) : SignInResult()
}
