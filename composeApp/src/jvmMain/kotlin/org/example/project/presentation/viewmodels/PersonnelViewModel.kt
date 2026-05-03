package org.example.project.presentation.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.repositories.PersonnelEmployeeProfileRequest
import org.example.project.data.repositories.CreatePersonnelUserRequest
import org.example.project.data.repositories.PersonnelRepository
import org.example.project.data.repositories.PersonnelUserDto
import org.example.project.data.repositories.UpdatePersonnelUserRequest
import java.io.File

data class PersonnelUiState(
    val isLoading: Boolean = false,
    val users: List<PersonnelUserDto> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val creating: Boolean = false,
    val processingUserId: String? = null
)

class PersonnelViewModel(
    private val repository: PersonnelRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _uiState = MutableStateFlow(PersonnelUiState())
    val uiState: StateFlow<PersonnelUiState> = _uiState.asStateFlow()

    fun loadUsers() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getUsers().fold(
                onSuccess = { users ->
                    _uiState.update { it.copy(isLoading = false, users = users) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "Khong the tai nhan su") }
                }
            )
        }
    }

    fun createUser(
        fullName: String,
        phone: String,
        email: String,
        password: String,
        role: String,
        employeeProfile: PersonnelEmployeeProfileRequest? = null,
        qualificationDocumentFile: File? = null
    ) {
        scope.launch {
            _uiState.update { it.copy(creating = true, error = null) }
            val preparedProfile = prepareEmployeeProfile(employeeProfile, qualificationDocumentFile)
                .getOrElse { error ->
                    _uiState.update {
                        it.copy(
                            creating = false,
                            error = error.message ?: "Khong the upload minh chung chuyen mon"
                        )
                    }
                    return@launch
                }

            repository.createUser(
                CreatePersonnelUserRequest(
                    fullName = fullName.ifBlank { null },
                    phone = phone,
                    email = email.ifBlank { null },
                    password = password,
                    role = role,
                    employeeProfile = preparedProfile
                )
            ).fold(
                onSuccess = {
                    _uiState.update { it.copy(creating = false, successMessage = "Đã tạo tài khoản") }
                    loadUsers()
                },
                onFailure = { error -> _uiState.update { it.copy(creating = false, error = error.message ?: "Không thể tạo tài khoản") } }
            )
        }
    }

    fun updateUser(
        userId: String,
        fullName: String,
        phone: String,
        email: String,
        role: String,
        employeeProfile: PersonnelEmployeeProfileRequest? = null,
        qualificationDocumentFile: File? = null
    ) {
        scope.launch {
            _uiState.update { it.copy(processingUserId = userId, error = null) }
            val preparedProfile = prepareEmployeeProfile(employeeProfile, qualificationDocumentFile)
                .getOrElse { error ->
                    _uiState.update {
                        it.copy(
                            processingUserId = null,
                            error = error.message ?: "Khong the upload minh chung chuyen mon"
                        )
                    }
                    return@launch
                }

            repository.updateUser(
                userId,
                UpdatePersonnelUserRequest(
                    fullName = fullName.ifBlank { null },
                    phone = phone.ifBlank { null },
                    email = email.ifBlank { null },
                    role = role,
                    employeeProfile = preparedProfile
                )
            ).fold(
                onSuccess = {
                    _uiState.update { it.copy(processingUserId = null, successMessage = "Đã cập nhật tài khoản") }
                    loadUsers()
                },
                onFailure = { error -> _uiState.update { it.copy(processingUserId = null, error = error.message ?: "Không thể cập nhật") } }
            )
        }
    }

    fun updateEmployeeProfile(
        user: PersonnelUserDto,
        employeeProfile: PersonnelEmployeeProfileRequest,
        qualificationDocumentFile: File? = null
    ) {
        updateUser(
            userId = user.id,
            fullName = user.fullName.orEmpty(),
            phone = user.phone,
            email = user.email.orEmpty(),
            role = user.role,
            employeeProfile = employeeProfile,
            qualificationDocumentFile = qualificationDocumentFile
        )
    }

    fun toggleLock(userId: String) {
        scope.launch {
            _uiState.update { it.copy(processingUserId = userId, error = null) }
            repository.toggleLock(userId).fold(
                onSuccess = {
                    _uiState.update { it.copy(processingUserId = null, successMessage = "Da cap nhat trang thai tai khoan") }
                    loadUsers()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(processingUserId = null, error = error.message ?: "Khong the khoa/mo khoa") }
                }
            )
        }
    }

    fun resetPassword(userId: String, newPassword: String) {
        scope.launch {
            _uiState.update { it.copy(processingUserId = userId, error = null) }
            repository.resetPassword(userId, newPassword).fold(
                onSuccess = {
                    _uiState.update { it.copy(processingUserId = null, successMessage = "Da reset mat khau") }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(processingUserId = null, error = error.message ?: "Khong the reset mat khau") }
                }
            )
        }
    }

    fun deleteUser(userId: String) {
        scope.launch {
            _uiState.update { it.copy(processingUserId = userId, error = null) }
            repository.deleteUser(userId).fold(
                onSuccess = {
                    _uiState.update { it.copy(processingUserId = null, successMessage = "Da xoa tai khoan") }
                    loadUsers()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(processingUserId = null, error = error.message ?: "Khong the xoa tai khoan") }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    private suspend fun prepareEmployeeProfile(
        employeeProfile: PersonnelEmployeeProfileRequest?,
        qualificationDocumentFile: File?
    ): Result<PersonnelEmployeeProfileRequest?> {
        if (qualificationDocumentFile == null) return Result.success(employeeProfile)

        return repository.uploadQualificationDocument(qualificationDocumentFile).map { uploaded ->
            val profile = employeeProfile ?: PersonnelEmployeeProfileRequest()
            profile.copy(
                qualificationDocumentUrl = uploaded.url,
                qualificationDocumentPublicId = uploaded.publicId,
                qualificationDocumentType = uploaded.fileType,
                qualificationDocumentResourceType = uploaded.resourceType
            )
        }
    }
}

