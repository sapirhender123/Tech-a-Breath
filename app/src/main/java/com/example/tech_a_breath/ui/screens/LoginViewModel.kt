package com.example.tech_a_breath.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tech_a_breath.auth.GoogleAuthClient
import com.example.tech_a_breath.auth.GoogleUser
import com.example.tech_a_breath.auth.SignInResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val signedInUser: GoogleUser? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authClient = GoogleAuthClient(application.applicationContext)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onGoogleSignInClick() {
        if (_uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = authClient.signIn()) {
                is SignInResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, signedInUser = result.user)
                    }
                    // TODO: send result.user.idToken to your backend (or
                    // Firebase Auth's signInWithCredential) to establish a
                    // verified session before treating the user as signed in.
                }
                is SignInResult.Failure -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                is SignInResult.Cancelled -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
