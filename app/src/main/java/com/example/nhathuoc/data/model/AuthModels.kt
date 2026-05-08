package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable

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
    val id: String, // UUID from backend
    val userId: String, // UUID from backend
    val type: String, // "home", "work", "other"
    val recipientName: String,
    val recipientPhone: String, // Updated to match schema
    val fullAddress: String, // Updated to match schema
    val ward: String,
    val wardCode: String? = null,
    val district: String,
    val province: String,
    val provinceCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationSource: String = "MANUAL",
    val isDefault: Boolean,
    val createdAt: String,
    val updatedAt: String,
    // Compatibility fields for legacy Android screens until Phase 2 rewrite.
    val label: String? = null,
    val phone: String? = recipientPhone,
    val address: String = fullAddress
)

@Serializable
data class AddAddressRequest(
    val type: String,
    val recipientName: String,
    val recipientPhone: String, // Updated to match schema
    val fullAddress: String, // Updated to match schema
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
    val message: String,
    val address: UserAddress
)
