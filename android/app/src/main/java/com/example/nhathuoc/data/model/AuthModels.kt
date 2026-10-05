package com.example.nhathuoc.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Authentication related data transfer objects
 * Synchronized with Backend AuthRoutes.kt
 */

@Serializable
data class RegisterRequest(
    val fullName: String,
    val phone: String,
    val email: String,
    val password: String,
    val gender: Int? = null, // 1:Male, 2:Female, 3:Other
    val dateOfBirth: String? = null,
    val role: String = "USER"
)

@Serializable
data class LoginRequest(
    val credential: String,  // Email or Phone
    val password: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class AuthResponse(
    val user: UserResponse,
    val accessToken: String,
    val refreshToken: String,
    val message: String = ""  // Move to end with default - API may not always include
)

@Serializable
data class UserResponse(
    val id: String, // UUID from backend
    val fullName: String? = null,  // Make nullable - API can return null
    val phone: String,
    val email: String? = null,  // Make nullable - API can return null
    val avatarUrl: String? = null,
    val gender: Int? = null, // 1:Male, 2:Female, 3:Other
    val dateOfBirth: String? = null,
    val role: String,
    val isActive: Boolean = true,
    val createdAt: String = "",  // Add default
    val updatedAt: String = "",  // Add default
    val deletedAt: String? = null
)

@Serializable
data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class LogoutResponse(
    val message: String
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class ForgotPasswordRequest(val email: String)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)

// User profile update
@Serializable
data class UpdateUserRequest(
    val fullName: String?,
    val email: String?,
    val avatarUrl: String? = null,
    val gender: Int? = null, // 1:Male, 2:Female, 3:Other
    val dateOfBirth: String? = null
)

@Serializable
data class UpdateUserResponse(
    val message: String,
    val user: UserResponse
)

// Address related
@Serializable
data class UserAddress(
    val id: String,
    val userId: String = "",
    val label: String? = null,
    val recipientName: String? = null,
    @SerialName("phone") val recipientPhone: String? = null,
    val address: String = "",           // backend's primary street address (always present)
    val fullAddress: String? = null,    // backend's optional composed full address
    val ward: String? = null,
    val wardCode: String? = null,
    val district: String? = null,
    val province: String? = null,
    val provinceCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationSource: String = "MANUAL",
    val isDefault: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @Transient val type: String = "home"  // UI-only chip state, not serialized
)

@Serializable
data class AddressListResponse(
    val data: List<UserAddress>,
    val message: String = ""
)

@Serializable
data class AddAddressRequest(
    val type: String = "home",
    val recipientName: String,
    @SerialName("phone") val recipientPhone: String,
    @SerialName("address") val fullAddress: String,
    val ward: String,
    val wardCode: String? = null,
    val district: String,
    val province: String,
    val provinceCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationSource: String = "MANUAL",
    val isDefault: Boolean = false
)

@Serializable
data class AddAddressResponse(
    val message: String = "",
    val data: AddressIdData? = null
)

@Serializable
data class AddressIdData(val addressId: String? = null)
