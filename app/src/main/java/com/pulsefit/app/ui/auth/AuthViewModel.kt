package com.pulsefit.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.pulsefit.app.data.model.User
import com.pulsefit.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val user: User? = null) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(private val repo: AuthRepository = AuthRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Enter your email and password.")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repo.loginWithEmail(email.trim(), password)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success() },
                onFailure = { AuthUiState.Error(it.message ?: "Login failed. Please try again.") }
            )
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        weightKg: Double,
        heightCm: Double,
        goal: String,
        termsAccepted: Boolean
    ) {
        when {
            name.isBlank() || email.isBlank() || password.isBlank() ->
                _uiState.value = AuthUiState.Error("Please fill in all required fields.")
            password != confirmPassword ->
                _uiState.value = AuthUiState.Error("Passwords do not match.")
            password.length < 6 ->
                _uiState.value = AuthUiState.Error("Password must be at least 6 characters.")
            !termsAccepted ->
                _uiState.value = AuthUiState.Error("Please accept the Terms and Privacy Policy.")
            else -> {
                _uiState.value = AuthUiState.Loading
                viewModelScope.launch {
                    val result = repo.registerWithEmail(name.trim(), email.trim(), password, heightCm, weightKg, goal)
                    _uiState.value = result.fold(
                        onSuccess = { AuthUiState.Success(it) },
                        onFailure = { AuthUiState.Error(it.message ?: "Registration failed. Please try again.") }
                    )
                }
            }
        }
    }

    fun signInWithGoogle(credential: AuthCredential) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repo.signInWithGoogleCredential(credential)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success(it) },
                onFailure = { AuthUiState.Error(it.message ?: "Google sign-in failed.") }
            )
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
