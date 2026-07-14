package com.example.tech_a_breath.auth

/** Sealed result returned by [GoogleAuthClient.signIn]. */
sealed class SignInResult {
    data class Success(val user: GoogleUser) : SignInResult()
    object Cancelled : SignInResult()
    data class Failure(val message: String) : SignInResult()
}
