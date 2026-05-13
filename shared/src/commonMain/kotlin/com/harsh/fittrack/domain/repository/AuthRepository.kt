package com.harsh.fittrack.domain.repository

import com.harsh.fittrack.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Authentication facade over Firebase Auth.
 *
 * The actual OAuth credential acquisition (Google ID token, Apple identity token) is
 * platform-specific and lives behind [com.harsh.fittrack.data.remote.auth.OAuthCredentialProvider].
 * This interface stays provider-agnostic — callers don't know whether sign-in came from
 * Credential Manager on Android or ASAuthorization on iOS.
 */
interface AuthRepository {
    /** Emits the current authenticated user, or null if signed out. Hot — Firebase auth-state-listener backed. */
    val currentUser: Flow<User?>

    /** Synchronous accessor for use during splash routing. */
    suspend fun isSignedIn(): Boolean

    suspend fun signInWithGoogle(): Result<User>
    suspend fun signInWithApple(): Result<User>
    suspend fun signOut()
}
