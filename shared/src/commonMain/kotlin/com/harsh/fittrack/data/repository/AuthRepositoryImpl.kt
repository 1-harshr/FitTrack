package com.harsh.fittrack.data.repository

import com.harsh.fittrack.data.remote.auth.OAuthCredentialProvider
import com.harsh.fittrack.domain.model.User
import com.harsh.fittrack.domain.repository.AuthRepository
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val credentials: OAuthCredentialProvider,
) : AuthRepository {

    override val currentUser: Flow<User?> =
        firebaseAuth.authStateChanged.map { it?.toUser() }

    override suspend fun isSignedIn(): Boolean = firebaseAuth.currentUser != null

    override suspend fun signInWithGoogle(): Result<User> = runCatching {
        val token = credentials.getGoogleIdToken().getOrThrow()
        val credential = GoogleAuthProvider.credential(token.idToken, token.accessToken)
        firebaseAuth.signInWithCredential(credential).user?.toUser()
            ?: error("Firebase returned a null user after Google sign-in")
    }.reThrowCancellation()

    override suspend fun signInWithApple(): Result<User> = runCatching {
        val token = credentials.getAppleIdToken().getOrThrow()
        val credential = OAuthProvider.credential(
            providerId = "apple.com",
            idToken = token.idToken,
            rawNonce = token.rawNonce,
        )
        firebaseAuth.signInWithCredential(credential).user?.toUser()
            ?: error("Firebase returned a null user after Apple sign-in")
    }.reThrowCancellation()

    private fun <T> Result<T>.reThrowCancellation(): Result<T> = also {
        it.exceptionOrNull()?.takeIf { e -> e is CancellationException }?.let { e -> throw e }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    private fun FirebaseUser.toUser(): User = User(
        id = uid,
        name = displayName.orEmpty(),
        email = email.orEmpty(),
        photoUrl = photoURL,
        // Units default to KG here; the real value is layered in by UserRepository
        // from the local settings store, which keys off the Firebase UID.
    )
}
