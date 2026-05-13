package com.harsh.fittrack.data.remote.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CancellationException

/**
 * Android implementation that uses Credential Manager to surface the modern
 * Google account chooser sheet and returns the resulting Google ID token.
 *
 * Construct with [webClientId] — the OAuth 2.0 **Web** client ID generated for your
 * Firebase project (Firebase Console → Project settings → General → Web SDK
 * configuration → Web client ID). It is NOT the Android client ID.
 *
 * Apple Sign-In is not required on Android per the PRD; the call returns failure.
 */
class AndroidOAuthCredentialProvider(
    private val context: Context,
    private val webClientId: String,
) : OAuthCredentialProvider {

    private val credentialManager: CredentialManager = CredentialManager.create(context)

    override suspend fun getGoogleIdToken(): Result<IdTokenCredential> = runCatching {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            // Allow account picker even when no accounts are authorized for this app yet.
            .setFilterByAuthorizedAccounts(false)
            // Auto-select when only one Google account is on the device and previously authorized.
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val response = try {
            credentialManager.getCredential(context = context, request = request)
        } catch (e: NoCredentialException) {
            throw IllegalStateException(
                "No Google account found on this device. " +
                "Please add a Google account in Settings → Accounts.",
                e,
            )
        } catch (e: GetCredentialException) {
            throw IllegalStateException("Google sign-in was cancelled or failed: ${e.message}", e)
        }

        val credential = response.credential
        if (credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            error("Unexpected credential type: ${credential.type}")
        }
        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
        IdTokenCredential(idToken = googleCredential.idToken)
    }.also { result ->
        // runCatching swallows CancellationException — ensure coroutines can still be cancelled.
        result.exceptionOrNull()
            ?.takeIf { it is CancellationException }
            ?.let { throw it }
    }

    override suspend fun getAppleIdToken(): Result<AppleIdCredential> =
        Result.failure(UnsupportedOperationException("Apple Sign-In is iOS-only in this build."))
}
