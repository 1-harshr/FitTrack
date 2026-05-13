package com.harsh.fittrack.feature.auth

import com.harsh.fittrack.domain.model.User

sealed interface AuthState {
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val user: User) : AuthState
    data class Error(val message: String) : AuthState
}
