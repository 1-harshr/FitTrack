plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
    application
}

group = "com.harsh.fittrack"
version = "1.0.0"

application {
    mainClass.set("com.harsh.fittrack.ApplicationKt")
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    // Ktor server
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverAuth)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serverStatusPages)
    implementation(libs.ktor.serverCallLogging)
    implementation(libs.ktor.serverCors)
    implementation(libs.ktor.serializationKotlinxJson)

    // Database
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.postgresql.jdbc)
    implementation(libs.hikari)

    // JWT auth + password hashing
    implementation(libs.ktor.serverAuthJwt)
    implementation(libs.jbcrypt)

    // DI
    implementation(libs.koin.ktor)

    // Ktor client (for outbound Claude API calls)
    implementation(libs.ktor.clientCio)
    implementation(libs.ktor.clientContentNegotiation)
    implementation(libs.ktor.clientKotlinxJson)

    // Serialization + logging
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.logback)

    // Test
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.ktor.clientContentNegotiation)
    testImplementation(libs.ktor.clientKotlinxJson)
    testImplementation(libs.kotlin.testJunit)
}