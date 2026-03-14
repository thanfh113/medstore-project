package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.local.SessionManager
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication operations
 * Handles login, register, logout and user session state
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // UI State for registration
    private val _registerState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val registerState: StateFlow<UiState<AuthResponse>> = _registerState.asStateFlow()

    // UI State for login
    private val _loginState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val loginState: StateFlow<UiState<AuthResponse>> = _loginState.asStateFlow()

    // UI State for logout
    private val _logoutState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val logoutState: StateFlow<UiState<String>> = _logoutState.asStateFlow()

    // UI State for user profile
    private val _userState = MutableStateFlow<UiState<UserResponse>>(UiState.Idle)
    val userState: StateFlow<UiState<UserResponse>> = _userState.asStateFlow()

    // Session data flows from repository
    val isLoggedIn: Flow<Boolean> = authRepository.isLoggedIn()
    val userFullName: Flow<String?> = authRepository.getUserFullName()
    val userPhone: Flow<String?> = authRepository.getUserPhone()
    val userEmail: Flow<String?> = authRepository.getUserEmail()
    val userRole: Flow<String?> = authRepository.getUserRole()

    // Register new user
    fun register(
        fullName: String,
        phone: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading

            when (val result = authRepository.register(fullName, phone, email, password)) {
                is NetworkResult.Success -> {
                    _registerState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _registerState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _registerState.value = UiState.Error(
                        result.e.message ?: "Có lỗi xảy ra, vui lòng thử lại"
                    )
                }
            }
        }
    }

    // Login user
    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading

            when (val result = authRepository.login(phone, password)) {
                is NetworkResult.Success -> {
                    _loginState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _loginState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _loginState.value = UiState.Error(
                        result.e.message ?: "Có lỗi xảy ra, vui lòng thử lại"
                    )
                }
            }
        }
    }

    // Logout user
    fun logout() {
        viewModelScope.launch {
            _logoutState.value = UiState.Loading

            when (val result = authRepository.logout()) {
                is NetworkResult.Success -> {
                    _logoutState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _logoutState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _logoutState.value = UiState.Error(
                        result.e.message ?: "Có lỗi xảy ra, vui lòng thử lại"
                    )
                }
            }
        }
    }

    // Get current user info
    fun getCurrentUser() {
        viewModelScope.launch {
            _userState.value = UiState.Loading

            when (val result = authRepository.getCurrentUser()) {
                is NetworkResult.Success -> {
                    _userState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _userState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _userState.value = UiState.Error(
                        result.e.message ?: "Có lỗi xảy ra, vui lòng thử lại"
                    )
                }
            }
        }
    }

    // Update user profile
    fun updateProfile(fullName: String, email: String) {
        viewModelScope.launch {
            _userState.value = UiState.Loading

            when (val result = authRepository.updateProfile(fullName, email)) {
                is NetworkResult.Success -> {
                    _userState.value = UiState.Success(result.data.user)
                }
                is NetworkResult.Error -> {
                    _userState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _userState.value = UiState.Error(
                        result.e.message ?: "Có lỗi xảy ra, vui lòng thử lại"
                    )
                }
            }
        }
    }

    // Clear login state (for UI flow)
    fun clearLoginState() {
        _loginState.value = UiState.Idle
    }

    // Clear register state (for UI flow)
    fun clearRegisterState() {
        _registerState.value = UiState.Idle
    }

    // Clear logout state (for UI flow)
    fun clearLogoutState() {
        _logoutState.value = UiState.Idle
    }

    // Clear user state
    fun clearUserState() {
        _userState.value = UiState.Idle
    }

    // Validation helpers
    fun isValidPhone(phone: String): Boolean {
        return phone.matches(Regex("^(0[3,5,7,8,9])+([0-9]{8})$"))
    }

    fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidFullName(fullName: String): Boolean {
        return fullName.trim().length >= 2
    }
}

/**
 * ViewModelFactory for AuthViewModel
 */
class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}