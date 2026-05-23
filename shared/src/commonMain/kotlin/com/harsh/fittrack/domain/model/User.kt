package com.harsh.fittrack.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val units: Units = Units.KG,
)

enum class Units { KG, LBS }
