package com.harsh.fittrack.data.remote.auth

/**
 * Acquires an OAuth credential from the platform's native sign-in flow.
 * Android implementation uses Credential Manager + Google ID Helper.
 * iOS implementation uses ASAuthorization (Sign in with Apple) and Google Sign-In SDK.
 *
 * Returned [IdTokenCredential] is then exchanged with Firebase by AuthRepositoryImpl.
 */
interface OAuthCredentialProvider {
    /** Launches the native Google account chooser and returns the resulting ID token. */
    suspend fun getGoogleIdToken(): Result<IdTokenCredential>

    /** Launches the native Sign in with Apple sheet and returns the resulting identity token + nonce. */
    suspend fun getAppleIdToken(): Result<AppleIdCredential>
}

data class IdTokenCredential(
    val idToken: String,
    /** Server-side access token, if the provider returns one. Usually null for Firebase flows. */
    val accessToken: String? = null,
)

data class AppleIdCredential(
    val idToken: String,
    val rawNonce: String,
)
