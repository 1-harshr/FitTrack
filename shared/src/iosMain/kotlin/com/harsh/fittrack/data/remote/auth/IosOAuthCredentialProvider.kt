package com.harsh.fittrack.data.remote.auth

/**
 * iOS implementation. Two pieces still to wire up:
 *
 *  1. Google Sign-In — add the GoogleSignIn Swift Package to iosApp, call
 *     `GIDSignIn.sharedInstance.signIn(withPresenting:)` from a Swift bridge,
 *     return the resulting ID token here.
 *
 *  2. Sign in with Apple — use `ASAuthorizationAppleIDProvider` with a fresh
 *     SHA-256 nonce, return the identity token + raw nonce so Firebase can
 *     verify it via `OAuthProvider.credential("apple.com", idToken, rawNonce)`.
 *
 * Deferred until [GoogleService-Info.plist] lands in iosApp and Firebase is
 * configured in `iOSApp.swift` via `FirebaseApp.configure()`.
 */
class IosOAuthCredentialProvider : OAuthCredentialProvider {
    override suspend fun getGoogleIdToken(): Result<IdTokenCredential> =
        Result.failure(NotImplementedError("Google Sign-In on iOS — pending Swift bridge wiring."))

    override suspend fun getAppleIdToken(): Result<AppleIdCredential> =
        Result.failure(NotImplementedError("Sign in with Apple — pending ASAuthorization bridge."))
}
