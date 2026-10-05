package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable

/**
 * Common data transfer objects shared across the app
 */

@Serializable
data class ApiError(
    val error: String? = null,
    val message: String? = null,
    val details: String? = null,
    val status: Int? = null,
    val timestamp: String? = null,
    val path: String? = null
) {
    fun userMessage(): String = message ?: error ?: "Có lỗi xảy ra, vui lòng thử lại"
}

fun parseErrorBody(errorBody: String?): String {
    if (errorBody == null) return "Có lỗi xảy ra, vui lòng thử lại"
    return try {
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        json.decodeFromString<ApiError>(errorBody).userMessage()
    } catch (_: Exception) {
        errorBody
    }
}

fun Throwable.toUserMessage(): String = when (this) {
    is java.net.UnknownHostException -> "Không có kết nối internet. Vui lòng kiểm tra mạng."
    is java.net.SocketTimeoutException -> "Kết nối quá chậm, vui lòng thử lại."
    is java.net.ConnectException -> "Không thể kết nối đến máy chủ. Vui lòng thử lại sau."
    is java.io.IOException -> "Lỗi mạng, vui lòng thử lại."
    else -> message?.let { msg ->
        when {
            msg.contains("timeout", ignoreCase = true) -> "Kết nối quá chậm, vui lòng thử lại."
            msg.contains("Unable to resolve host", ignoreCase = true) -> "Không có kết nối internet."
            msg.contains("failed to connect", ignoreCase = true) -> "Không thể kết nối đến máy chủ."
            else -> msg
        }
    } ?: "Có lỗi xảy ra, vui lòng thử lại."
}

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val error: ApiError? = null
)

@Serializable
data class DataMessageResponse<T>(
    val data: T,
    val message: String
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
    val url: String = "",
    val publicId: String = "",
    val format: String = "",
    val resourceType: String = "",
    val bytes: Long = 0,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Double? = null,
    val message: String? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val mimeType: String? = null
)

@Serializable
data class MessageResponse(
    val message: String,
    val timestamp: String = ""
)

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val exception: Throwable? = null) : UiState<Nothing>()
}

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>()
    data class Exception(val e: Throwable) : NetworkResult<Nothing>()
}

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
