package com.example.nhathuoc.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Utilities for image upload to Cloudinary
 */
object ImageUploadUtils {

    /**
     * Create URI for camera intent
     */
    fun createImageUri(context: Context): Uri {
        val imagesDir = File(context.cacheDir, "images")
        imagesDir.mkdirs()
        val imageFile = File(imagesDir, "temp_image_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    /**
     * Get image file from URI (for gallery pick)
     */
    fun getImagePath(context: Context, imageUri: Uri): String {
        return if (imageUri.scheme == "content") {
            val cursor = context.contentResolver.query(imageUri, null, null, null, null)
            cursor?.use {
                val columnIndex = it.getColumnIndexOrThrow("_data")
                it.moveToFirst()
                it.getString(columnIndex)
            } ?: ""
        } else {
            imageUri.path ?: ""
        }
    }

    /**
     * Cloudinary configuration for uploads
     */
    class CloudinaryConfig {
        companion object {
            const val CLOUD_NAME = "your_cloudinary_name"
            const val UPLOAD_PRESET = "pharmacy_app"
            const val API_KEY = "your_api_key"
            const val BASE_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

            fun getUploadUrl(): String {
                return BASE_URL
            }
        }
    }

    /**
     * Data class for upload response
     */
    data class UploadResult(
        val success: Boolean,
        val imageUrl: String? = null,
        val publicId: String? = null,
        val error: String? = null
    )
}

/**
 * Extended Price utilities
 */
object PriceUtils {

    fun formatPrice(price: Double): String {
        return if (price >= 1_000_000) {
            String.format("%.1fM", price / 1_000_000).replace(Regex("\\.0M$"), "M") + "đ"
        } else if (price >= 1_000) {
            String.format("%.0fK", price / 1_000) + "đ"
        } else {
            String.format("%.0f", price) + "đ"
        }
    }

    fun calculateDiscount(price: Double, originalPrice: Double): Double {
        return if (originalPrice > 0) {
            ((originalPrice - price) / originalPrice) * 100
        } else {
            0.0
        }
    }

    fun calculateTotalFromItems(items: List<Any>): Double {
        // This would need to work with your actual item type
        return 0.0
    }
}

/**
 * API Error Handler for various error scenarios
 */
object ApiErrorHandler {

    data class ErrorResponse(
        val code: Int,
        val message: String,
        val userFriendlyMessage: String
    )

    fun handleError(code: Int, message: String): ErrorResponse {
        val userFriendlyMessage = when (code) {
            400 -> "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại."
            401 -> "Phiên làm việc hết hạn. Vui lòng đăng nhập lại."
            403 -> "Bạn không có quyền truy cập."
            404 -> "Không tìm thấy dữ liệu."
            409 -> "Xung đột dữ liệu. Vui lòng thử lại."
            422 -> "Dữ liệu nhập không đúng định dạng."
            429 -> "Quá nhiều yêu cầu. Vui lòng chờ một chút."
            500 -> "Lỗi máy chủ. Vui lòng thử lại sau."
            503 -> "Dịch vụ tạm thời không khả dụng."
            else -> message
        }

        return ErrorResponse(code, message, userFriendlyMessage)
    }

    fun getUserFriendlyMessage(code: Int, message: String): String {
        return handleError(code, message).userFriendlyMessage
    }

    suspend fun <T> retryWithBackoff(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 10000,
        backoffMultiplier: Float = 2f,
        block: suspend () -> T
    ): T {
        var delayMs = initialDelayMs
        var lastException: Exception? = null

        repeat(maxRetries) {
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                if (it < maxRetries - 1) {
                    kotlinx.coroutines.delay(delayMs)
                    delayMs = (delayMs * backoffMultiplier).toLong().coerceAtMost(maxDelayMs)
                }
            }
        }

        throw lastException ?: Exception("Failed after $maxRetries retries")
    }
}
