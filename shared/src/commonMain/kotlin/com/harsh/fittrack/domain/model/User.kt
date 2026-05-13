package com.harsh.fittrack.domain.model

/**
 * Authenticated user. Identity comes from Firebase OAuth (Google / Apple).
 * Firebase UID is the stable identifier used to key local and future remote data.
 * No password is stored.
 */
data class User(
    val id: String,          // Firebase UID
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val units: Units = Units.KG,
)

enum class Units { KG, LBS }
