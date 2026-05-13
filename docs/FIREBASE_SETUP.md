# Firebase Auth — Setup Guide

This is the manual setup needed before the Firebase Auth code in this repo will actually authenticate anyone. The code is wired up; these are the human steps.

---

## 1. Firebase Console

1. **Create a Firebase project** at <https://console.firebase.google.com>.
2. **Add an Android app** to the project. Use the package name `com.harsh.fittrack` (must match `android.namespace` in `composeApp/build.gradle.kts`).
3. **Add an iOS app**. Bundle ID matches whatever is set in `iosApp/iosApp.xcodeproj`.
4. **Authentication → Sign-in method** — enable **Google** and **Apple** providers.

For Google, take note of the auto-generated **Web client ID** under *Project settings → General → Your apps → Web SDK configuration*. Credential Manager on Android needs this exact value (not the Android client ID).

For Apple, you'll need to register a Services ID in your Apple Developer account and configure it as a return URL — Apple's instructions in the Firebase Console walk through this when you enable the provider.

---

## 2. Drop the config files into the repo

| Platform | File | Location |
|---|---|---|
| Android | `google-services.json` | `composeApp/google-services.json` |
| iOS | `GoogleService-Info.plist` | `iosApp/iosApp/GoogleService-Info.plist` (also add to the Xcode target) |

Both files are auto-generated and downloadable from the Firebase Console once each platform app is registered.

> `composeApp/google-services.json` is automatically picked up by the `com.google.gms.google-services` Gradle plugin during the Android build — no further wiring needed.

---

## 3. Web client ID

In `composeApp/src/androidMain/res/values/strings.xml`, replace:

```xml
<string name="firebase_web_client_id" translatable="false">REPLACE_WITH_FIREBASE_WEB_CLIENT_ID</string>
```

with the Web client ID copied from the Firebase Console (step 1).

Do **not** commit this string to a public repo. It isn't a secret per se (Android apps embed it openly), but rotating is annoying. Consider moving it to `local.properties` and exposing via `BuildConfig` if the repo will be public.

---

## 4. SHA-1 / SHA-256 fingerprints (Android)

Google Sign-In via Credential Manager **requires** your app's signing certificate fingerprint to be registered with the Firebase Android app.

```bash
# Debug fingerprint (every developer needs to add their own)
keytool -list -v -keystore ~/.android/debug.keystore \
  -alias androiddebugkey -storepass android -keypass android | grep SHA
```

Copy both **SHA-1** and **SHA-256** into *Firebase Console → Project settings → Your apps → Android app → Add fingerprint*. Re-download `google-services.json` after adding fingerprints — they get baked in.

For release builds, add your release keystore's fingerprints too.

---

## 5. iOS — additional steps

1. Add the **Firebase iOS SDK** via Swift Package Manager in Xcode (`File → Add Package Dependencies → https://github.com/firebase/firebase-ios-sdk`). Select `FirebaseAuth`.
2. Add **GoogleSignIn-iOS** Swift Package (`https://github.com/google/GoogleSignIn-iOS`).
3. In `iOSApp.swift`:
   ```swift
   import FirebaseCore

   @main
   struct iOSApp: App {
       init() {
           FirebaseApp.configure()
       }
       // ...
   }
   ```
4. Add your reversed client ID (from `GoogleService-Info.plist` → `REVERSED_CLIENT_ID`) to **Info.plist → URL Types → URL Schemes**.
5. Enable **Sign in with Apple** capability on the iOS target (Xcode → target → Signing & Capabilities → + Capability).
6. Implement the Swift bridge for `IosOAuthCredentialProvider` — see the TODO in `shared/src/iosMain/.../IosOAuthCredentialProvider.kt`. The bridge needs to:
   - Call `GIDSignIn.sharedInstance.signIn(withPresenting:)` and return the resulting `idToken`.
   - Run an `ASAuthorizationAppleIDProvider` request with a fresh SHA-256 nonce and return `identityToken` + the raw nonce.

---

## 6. Verify

After all of the above:

```bash
./gradlew :composeApp:assembleDebug
```

The build should succeed. Once you run the Android app, tapping "Continue with Google" on the Login screen should pop the system account chooser and route to Home on success.

If you see `DEVELOPER_ERROR` or `API_NOT_AVAILABLE` from Credential Manager, the most common cause is a missing or mismatched SHA fingerprint in the Firebase Console (step 4).
