package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.local.SessionManager
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import com.example.nhathuoc.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository for authentication operations
 * Handles API calls and local session management
 */
class AuthRepository(
    private val sessionManager: SessionManager
) {
    private val apiService: ApiService by lazy {
        RetrofitClient.createService<ApiService>()
    }

    // Register new user
    suspend fun register(
        fullName: String,
        phone: String,
        email: String,
        password: String,
        gender: Int? = null, // Added gender parameter
        dateOfBirth: String? = null // Added dateOfBirth parameter
    ): NetworkResult<AuthResponse> {
        return try {
            val request = RegisterRequest(
                fullName = fullName,
                phone = phone,
                email = email,
                password = password,
                gender = gender,
                dateOfBirth = dateOfBirth
            )

            val response = apiService.register(request)

            if (response.isSuccessful) {
                val authResponse = response.body()!!

                // Save session data
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
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                NetworkResult.Error(response.code(), errorMessage)
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    // Login user
    suspend fun login(
        phone: String,
        password: String
    ): NetworkResult<AuthResponse> {
        return try {
            val request = LoginRequest(phone = phone, password = password)
            val response = apiService.login(request)

            if (response.isSuccessful) {
                val authResponse = response.body()!!

                // Save session data
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
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                NetworkResult.Error(response.code(), errorMessage)
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    // Logout user
    suspend fun logout(): NetworkResult<String> {
        return try {
            val response = apiService.logout()

            // Clear local session regardless of server response
            sessionManager.clearSession()

            if (response.isSuccessful) {
                NetworkResult.Success("Đăng xuất thành công")
            } else {
                // Still success locally even if server call fails
                NetworkResult.Success("Đăng xuất thành công")
            }
        } catch (e: Exception) {
            // Clear session even if network fails
            sessionManager.clearSession()
            NetworkResult.Success("Đăng xuất thành công")
        }
    }

    // Get current user info
    suspend fun getCurrentUser(): NetworkResult<UserResponse> {
        return try {
            val response = apiService.getMe()

            if (response.isSuccessful) {
                val user = response.body()!!

                // Update local user info
                sessionManager.updateUserInfo(
                    fullName = user.fullName,
                    phone = user.phone,
                    email = user.email
                )

                NetworkResult.Success(user)
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                NetworkResult.Error(response.code(), errorMessage)
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    // Update user profile
    suspend fun updateProfile(
        fullName: String,
        email: String
    ): NetworkResult<UpdateUserResponse> {
        return try {
            val request = UpdateUserRequest(fullName = fullName, email = email)
            val response = apiService.updateUser(request)

            if (response.isSuccessful) {
                val updateResponse = response.body()!!

                // Update local session data
                sessionManager.updateUserInfo(
                    fullName = updateResponse.user.fullName,
                    phone = updateResponse.user.phone,
                    email = updateResponse.user.email
                )

                NetworkResult.Success(updateResponse)
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                NetworkResult.Error(response.code(), errorMessage)
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    // Check if user is logged in
    fun isLoggedIn(): Flow<Boolean> = sessionManager.isLoggedIn

    // Get user session data flows
    fun getUserFullName(): Flow<String?> = sessionManager.userFullName
    fun getUserPhone(): Flow<String?> = sessionManager.userPhone
    fun getUserEmail(): Flow<String?> = sessionManager.userEmail
    fun getUserRole(): Flow<String?> = sessionManager.userRole

    // Get user ID synchronously (for repository operations)
    suspend fun getUserId(): String? = sessionManager.getUserId()

    // Helper function to parse error messages
    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            if (errorBody != null) {
                // Try to parse as ApiError
                kotlinx.serialization.json.Json.decodeFromString<ApiError>(errorBody).message
            } else {
                "Có lỗi xảy ra, vui lòng thử lại"
            }
        } catch (e: Exception) {
            errorBody ?: "Có lỗi xảy ra, vui lòng thử lại"
        }
    }
}