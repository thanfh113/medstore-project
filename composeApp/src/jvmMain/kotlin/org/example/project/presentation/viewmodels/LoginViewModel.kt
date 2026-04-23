package org.example.project.presentation.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.repositories.AuthRepository


data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val token: String? = null,
    val userRole: String? = null,
    val displayName: String? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String, expectedRole: String, onSuccess: (String) -> Unit) {
        val credential = username.trim()
        val normalizedRole = expectedRole.trim().uppercase()
        if (credential.isBlank() || password.isBlank()) {
                _uiState.update { it.copy(error = "Vui lòng nhập đầy đủ email/số điện thoại và mật khẩu") }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.login(credential, password)
            result.onSuccess { auth ->
                val role = auth.user.role.uppercase()
                if (role != "ADMIN" && role != "EMPLOYEE") {
                    sessionManager.clear()
                    authRepository.clearTokens()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                        error = "Desktop chỉ cho phép tài khoản ADMIN hoặc EMPLOYEE",
                            token = null,
                            userRole = null,
                            displayName = null
                        )
                    }
                    return@onSuccess
                }

                if (role != normalizedRole) {
                    sessionManager.clear()
                    authRepository.clearTokens()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                        error = "Bạn đang đăng nhập bằng tài khoản $role nhưng đang chọn vai trò $normalizedRole",
                            token = null,
                            userRole = null,
                            displayName = null
                        )
                    }
                    return@onSuccess
                }

                sessionManager.save(auth)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        token = auth.accessToken,
                        userRole = role,
                        displayName = auth.user.fullName ?: auth.user.email ?: auth.user.phone,
                        error = null
                    )
                }
                onSuccess(role)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                    error = throwable.message ?: "Đăng nhập thất bại"
                    )
                }
            }
        }
    }

    fun logout() {
        sessionManager.clear()
        authRepository.clearTokens()
        _uiState.value = LoginUiState()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
