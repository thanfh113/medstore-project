package com.example.nhathuoc.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _registerState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val registerState: StateFlow<UiState<AuthResponse>> = _registerState.asStateFlow()

    private val _loginState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val loginState: StateFlow<UiState<AuthResponse>> = _loginState.asStateFlow()

    private val _logoutState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val logoutState: StateFlow<UiState<String>> = _logoutState.asStateFlow()

    private val _userState = MutableStateFlow<UiState<UserResponse>>(UiState.Idle)
    val userState: StateFlow<UiState<UserResponse>> = _userState.asStateFlow()

    val isLoggedIn: Flow<Boolean> = authRepository.isLoggedIn()
    val userFullName: Flow<String?> = authRepository.getUserFullName()
    val userPhone: Flow<String?> = authRepository.getUserPhone()
    val userEmail: Flow<String?> = authRepository.getUserEmail()
    val userRole: Flow<String?> = authRepository.getUserRole()

    fun register(fullName: String, phone: String, email: String, password: String) {
        Log.d("AuthViewModel", "register() called with phone=$phone, email=$email")
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            when (val result = authRepository.register(fullName, phone, email, password)) {
                is NetworkResult.Success -> _registerState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _registerState.value = UiState.Error(result.message)
                is NetworkResult.Exception -> _registerState.value = UiState.Error(result.e.message ?: "Co loi xay ra")
            }
        }
    }

    fun login(phone: String, password: String) {
        Log.d("AuthViewModel", "login() called with credential=$phone")
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            when (val result = authRepository.login(phone, password)) {
                is NetworkResult.Success -> _loginState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _loginState.value = UiState.Error(result.message)
                is NetworkResult.Exception -> _loginState.value = UiState.Error(result.e.message ?: "Co loi xay ra")
            }
        }
    }

    fun loginWithEmail(email: String, password: String) = login(email, password)

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = UiState.Loading
            when (val result = authRepository.logout()) {
                is NetworkResult.Success -> _logoutState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _logoutState.value = UiState.Error(result.message)
                is NetworkResult.Exception -> _logoutState.value = UiState.Error(result.e.message ?: "Co loi xay ra")
            }
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            _userState.value = UiState.Loading
            when (val result = authRepository.getCurrentUser()) {
                is NetworkResult.Success -> _userState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _userState.value = UiState.Error(result.message)
                is NetworkResult.Exception -> _userState.value = UiState.Error(result.e.message ?: "Co loi xay ra")
            }
        }
    }

    fun updateProfile(fullName: String, email: String) {
        viewModelScope.launch {
            _userState.value = UiState.Loading
            when (val result = authRepository.updateProfile(fullName, email)) {
                is NetworkResult.Success -> _userState.value = UiState.Success(result.data.user)
                is NetworkResult.Error -> _userState.value = UiState.Error(result.message)
                is NetworkResult.Exception -> _userState.value = UiState.Error(result.e.message ?: "Co loi xay ra")
            }
        }
    }

    fun clearLoginState() { _loginState.value = UiState.Idle }
    fun clearRegisterState() { _registerState.value = UiState.Idle }
    fun clearLogoutState() { _logoutState.value = UiState.Idle }
    fun clearUserState() { _userState.value = UiState.Idle }

    fun isValidPhone(phone: String): Boolean = phone.matches(Regex("^0[0-9]{9}$"))
    fun isValidEmail(email: String): Boolean =
        email.trim().matches(Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"))
    fun isValidPassword(password: String): Boolean = password.length >= 6
    fun isValidFullName(fullName: String): Boolean = fullName.trim().length >= 2
}
