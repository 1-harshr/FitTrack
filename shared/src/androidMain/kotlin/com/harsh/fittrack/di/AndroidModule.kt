package com.harsh.fittrack.di

import android.content.Context
import com.harsh.fittrack.data.local.DatabaseFactory
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun androidModule(
    context: Context,
    apiBaseUrl: String = "http://10.0.2.2:8080",
): Module = module {
    single { DatabaseFactory(context.applicationContext) }
    single<String>(qualifier = named("apiBaseUrl")) { apiBaseUrl }
}
