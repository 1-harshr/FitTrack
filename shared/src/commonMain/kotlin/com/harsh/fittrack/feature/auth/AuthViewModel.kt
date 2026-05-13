package com.harsh.fittrack.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harsh.fittrack.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _state.value = if (user != null) AuthState.SignedIn(user) else AuthState.SignedOut
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            authRepository.signInWithGoogle()
                .onFailure { _state.value = AuthState.Error(it.message ?: "Sign-in failed") }
        }
    }

    fun signInWithApple() {
        viewModelScope.launch {
            authRepository.signInWithApple()
                .onFailure { _state.value = AuthState.Error(it.message ?: "Sign-in failed") }
        }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
