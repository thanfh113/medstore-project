package com.example.nhathuoc.data.repository

import android.util.Log
import com.example.nhathuoc.data.local.SessionManager
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
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
                sessionManager.updateUserInfo(user.fullName, user.phone, user.email)
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

    suspend fun updateProfile(fullName: String, email: String): NetworkResult<UpdateUserResponse> {
        return try {
            val response = apiService.updateUser(UpdateUserRequest(fullName = fullName, email = email))
            if (response.isSuccessful) {
                val updateResponse = response.body()!!
                sessionManager.updateUserInfo(
                    fullName = updateResponse.user.fullName,
                    phone = updateResponse.user.phone,
                    email = updateResponse.user.email
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

    fun isLoggedIn(): Flow<Boolean> = sessionManager.isLoggedIn
    fun getUserFullName(): Flow<String?> = sessionManager.userFullName
    fun getUserPhone(): Flow<String?> = sessionManager.userPhone
    fun getUserEmail(): Flow<String?> = sessionManager.userEmail
    fun getUserRole(): Flow<String?> = sessionManager.userRole
    suspend fun getUserId(): String? = sessionManager.getUserId()

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            if (errorBody != null) kotlinx.serialization.json.Json.decodeFromString<ApiError>(errorBody).message else "Co loi xay ra, vui long thu lai"
        } catch (e: Exception) {
            errorBody ?: "Co loi xay ra, vui long thu lai"
        }
    }
}
