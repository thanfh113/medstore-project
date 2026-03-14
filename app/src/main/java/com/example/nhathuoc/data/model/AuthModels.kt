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
    val role: String = "user"
)

@Serializable
data class LoginRequest(
    val phone: String,
    val password: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class AuthResponse(
    val message: String,
    val user: UserResponse,
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class UserResponse(
    val id: Int,
    val fullName: String,
    val phone: String,
    val email: String,
    val role: String,
    val createdAt: String,
    val updatedAt: String
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
    val email: String?
)

@Serializable
data class UpdateUserResponse(
    val message: String,
    val user: UserResponse
)

// Address related
@Serializable
data class UserAddress(
    val id: Int,
    val userId: Int,
    val type: String, // "home", "work", "other"
    val recipientName: String,
    val phone: String,
    val address: String,
    val ward: String,
    val district: String,
    val province: String,
    val isDefault: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class AddAddressRequest(
    val type: String,
    val recipientName: String,
    val phone: String,
    val address: String,
    val ward: String,
    val district: String,
    val province: String,
    val isDefault: Boolean = false
)

@Serializable
data class AddAddressResponse(
    val message: String,
    val address: UserAddress
)