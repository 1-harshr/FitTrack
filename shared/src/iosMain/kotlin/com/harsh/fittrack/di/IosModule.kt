package com.harsh.fittrack.di

import com.harsh.fittrack.data.local.DatabaseFactory
import com.harsh.fittrack.data.remote.auth.IosOAuthCredentialProvider
import com.harsh.fittrack.data.remote.auth.OAuthCredentialProvider
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS platform bindings. Call from `iOSApp.swift` via a small KMP entry point:
 *
 * ```
 * initKoin { modules(iosModule()) }
 * ```
 */
fun iosModule(): Module = module {
    single { DatabaseFactory() }
    single<OAuthCredentialProvider> { IosOAuthCredentialProvider() }
}
