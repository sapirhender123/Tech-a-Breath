package com.example.tech_a_breath.auth

/**
 * Minimal representation of a signed-in Google user.
 */
data class GoogleUser(
    val idToken: String,
    val email: String,
    val displayName: String?,
)
