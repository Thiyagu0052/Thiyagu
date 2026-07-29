package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class User(
    val email: String,
    val name: String,
    val role: String
)

class AuthViewModel : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(
        User(email = "admin@silvererp.com", name = "Wholesale Admin", role = "Admin Owner")
    )
    val currentUser: StateFlow<User?> = _currentUser

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun login(email: String, pass: String): Boolean {
        if (email.isNotBlank() && pass.isNotBlank()) {
            _currentUser.value = User(
                email = email,
                name = if (email.contains("@")) email.substringBefore("@").replaceFirstChar { it.uppercase() } else "User",
                role = "Store Manager"
            )
            _isLoggedIn.value = true
            return true
        }
        return false
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUser.value = null
    }
}
