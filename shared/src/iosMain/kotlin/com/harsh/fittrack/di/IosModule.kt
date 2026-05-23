package com.harsh.fittrack.di

import com.harsh.fittrack.data.local.DatabaseFactory
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun iosModule(
    apiBaseUrl: String = "http://localhost:8080",
): Module = module {
    single { DatabaseFactory() }
    single<String>(qualifier = named("apiBaseUrl")) { apiBaseUrl }
}
