package com.example.tech_a_breath.auth

/**
 * Minimal representation of a signed-in Google user.
 *
 * @param idToken  Raw ID token from Google — pass this to your backend or to
 *                 Firebase Auth's [com.google.firebase.auth.GoogleAuthProvider]
 *                 to establish a verified server-side session.
 * @param email    The user's Google email address.
 * @param displayName  Display name as set in the Google account (may be null).
 */
data class GoogleUser(
    val idToken: String,
    val email: String,
    val displayName: String?,
)
