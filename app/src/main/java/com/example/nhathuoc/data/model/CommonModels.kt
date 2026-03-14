package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable

/**
 * Common data transfer objects shared across the app
 */

@Serializable
data class ApiError(
    val status: Int,
    val message: String,
    val details: String? = null,
    val timestamp: String,
    val path: String
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val error: ApiError? = null
)

@Serializable
data class PaginationInfo(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrev: Boolean
)

@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val pagination: PaginationInfo
)

@Serializable
data class UploadResponse(
    val message: String,
    val fileUrl: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String
)

@Serializable
data class MessageResponse(
    val message: String,
    val timestamp: String = ""
)

// UI State for ViewModels
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val exception: Throwable? = null) : UiState<Nothing>()
}

// Network result wrapper
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>()
    data class Exception(val e: Throwable) : NetworkResult<Nothing>()
}

// Common filter/sort options
enum class SortOrder {
    ASC, DESC
}

@Serializable
data class DateRange(
    val startDate: String,
    val endDate: String
)

@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)