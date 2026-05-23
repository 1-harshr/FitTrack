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

    fun login(email: String, password: String) {
        _state.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.login(email.trim(), password)
                .onFailure { _state.value = AuthState.Error(it.message ?: "Login failed") }
        }
    }

    fun register(name: String, email: String, password: String) {
        _state.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.register(name.trim(), email.trim(), password)
                .onFailure { _state.value = AuthState.Error(it.message ?: "Registration failed") }
        }
    }

    fun clearError() {
        if (_state.value is AuthState.Error) _state.value = AuthState.SignedOut
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
