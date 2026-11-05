package com.dormfix.web.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dormfix.web.api.ApiClient
import com.dormfix.web.api.LoginResponse

sealed interface LoginLoadState {
    data object Idle : LoginLoadState
    data object Loading : LoginLoadState
    data class Error(val message: String) : LoginLoadState
}

class LoginState {
    var email: String by mutableStateOf("")
    var password: String by mutableStateOf("")
    var emailError: String? by mutableStateOf(null)
    var loadState: LoginLoadState by mutableStateOf(LoginLoadState.Idle)

    fun validateEmail(): Boolean {
        emailError = if (!email.contains("@")) "Invalid email" else null
        return emailError == null
    }

    suspend fun login(): LoginResponse? {
        if (!validateEmail()) return null
        loadState = LoginLoadState.Loading
        return try {
            val response = ApiClient.login(email, password)
            loadState = LoginLoadState.Idle
            response
        } catch (e: Exception) {
            loadState = LoginLoadState.Error(e.message ?: "Login failed")
            null
        }
    }
}
