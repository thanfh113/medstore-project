package com.example.nhathuoc.data.repository

import android.util.Log
import com.example.nhathuoc.data.local.SessionManager
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val sessionManager: SessionManager,
    private val apiService: ApiService
) {
    suspend fun register(
        fullName: String,
        phone: String,
        email: String,
        password: String,
        gender: Int? = null,
        dateOfBirth: String? = null
    ): NetworkResult<AuthResponse> {
        return try {
            val request = RegisterRequest(fullName, phone, email, password, gender, dateOfBirth)
            Log.d("AuthRepo", "Sending register request: phone=$phone, email=$email")
            val response = apiService.register(request)
            if (response.isSuccessful) {
                val authResponse = response.body()!!
                sessionManager.saveAuthData(
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    userId = authResponse.user.id,
                    fullName = authResponse.user.fullName,
                    phone = authResponse.user.phone,
                    email = authResponse.user.email,
                    role = authResponse.user.role
                )
                NetworkResult.Success(authResponse)
            } else {
                NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun login(credential: String, password: String): NetworkResult<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(credential, password))
            if (response.isSuccessful) {
                val authResponse = response.body()!!
                sessionManager.saveAuthData(
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    userId = authResponse.user.id,
                    fullName = authResponse.user.fullName,
                    phone = authResponse.user.phone,
                    email = authResponse.user.email,
                    role = authResponse.user.role
                )
                sessionManager.updateUserInfo(
                    fullName = authResponse.user.fullName,
                    phone = authResponse.user.phone,
                    email = authResponse.user.email,
                    gender = authResponse.user.gender,
                    dateOfBirth = authResponse.user.dateOfBirth
                )
                sessionManager.saveAvatarUri(authResponse.user.avatarUrl)
                NetworkResult.Success(authResponse)
            } else {
                NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun logout(): NetworkResult<String> {
        return try {
            apiService.logout()
            sessionManager.clearSession()
            NetworkResult.Success("Dang xuat thanh cong")
        } catch (e: Exception) {
            sessionManager.clearSession()
            NetworkResult.Success("Dang xuat thanh cong")
        }
    }

    suspend fun getCurrentUser(): NetworkResult<UserResponse> {
        return try {
            val response = apiService.getMe()
            if (response.isSuccessful) {
                val user = response.body()!!
                sessionManager.updateUserInfo(user.fullName, user.phone, user.email, user.gender, user.dateOfBirth)
                NetworkResult.Success(user)
            } else {
                NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun uploadAvatar(file: File): NetworkResult<String> {
        return try {
            val body = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, body)
            val response = apiService.uploadFile(part, "AVATAR")
            if (response.isSuccessful) {
                NetworkResult.Success(response.body()!!.url)
            } else {
                NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun updateProfile(
        fullName: String,
        email: String,
        gender: Int? = null,
        dateOfBirth: String? = null,
        avatarUrl: String? = null
    ): NetworkResult<UpdateUserResponse> {
        return try {
            val response = apiService.updateUser(UpdateUserRequest(
                fullName = fullName,
                email = email,
                gender = gender,
                dateOfBirth = dateOfBirth,
                avatarUrl = avatarUrl
            ))
            if (response.isSuccessful) {
                val updateResponse = response.body()!!
                sessionManager.updateUserInfo(
                    fullName = updateResponse.user.fullName,
                    phone = updateResponse.user.phone,
                    email = updateResponse.user.email,
                    gender = updateResponse.user.gender,
                    dateOfBirth = updateResponse.user.dateOfBirth
                )
                NetworkResult.Success(updateResponse)
            } else {
                NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun forgotPassword(email: String): NetworkResult<String> {
        return try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                NetworkResult.Success(response.body()?.message ?: "OTP đã được gửi")
            } else {
                NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun resetPassword(email: String, otp: String, newPassword: String): NetworkResult<String> {
        return try {
            val response = apiService.resetPassword(ResetPasswordRequest(email, otp, newPassword))
            if (response.isSuccessful) {
                NetworkResult.Success(response.body()?.message ?: "Đặt lại mật khẩu thành công")
            } else {
                NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): NetworkResult<String> {
        return try {
            val response = apiService.changePassword(ChangePasswordRequest(currentPassword, newPassword))
            if (response.isSuccessful) {
                NetworkResult.Success(response.body()?.message ?: "Đổi mật khẩu thành công")
            } else {
                NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    fun isLoggedIn(): Flow<Boolean> = sessionManager.isLoggedIn
    fun getUserFullName(): Flow<String?> = sessionManager.userFullName
    fun getUserPhone(): Flow<String?> = sessionManager.userPhone
    fun getUserEmail(): Flow<String?> = sessionManager.userEmail
    fun getUserRole(): Flow<String?> = sessionManager.userRole
    fun getUserAvatarUri(): Flow<String?> = sessionManager.userAvatarUri
    fun getUserGender(): Flow<Int?> = sessionManager.userGender
    fun getUserDateOfBirth(): Flow<String?> = sessionManager.userDateOfBirth
    suspend fun getUserId(): String? = sessionManager.getUserId()
    suspend fun saveAvatarUri(uri: String?) = sessionManager.saveAvatarUri(uri)

    private fun parseErrorMessage(errorBody: String?): String = parseErrorBody(errorBody)
}
