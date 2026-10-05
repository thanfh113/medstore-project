package com.example.nhathuoc.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.nhathuoc.data.model.ApiError
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.model.parseErrorBody
import com.example.nhathuoc.data.remote.ApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

data class UploadedAttachment(
    val fileUrl: String,
    val fileType: String,
    val publicId: String?
)

@Singleton
class FileUploadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService
) {
    suspend fun uploadEvidenceFiles(uris: List<Uri>): NetworkResult<List<UploadedAttachment>> {
        return try {
            val uploaded = uris.mapIndexed { index, uri ->
                val name = getDisplayName(uri).ifBlank { "attachment_$index" }
                val mimeType = context.contentResolver.getType(uri)
                    ?: guessMimeType(name)
                    ?: "application/octet-stream"
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return NetworkResult.Error(0, "Không thể đọc file: $name")
                val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", name, body)
                val response = apiService.uploadFile(part, "EVIDENCE")

                if (!response.isSuccessful) {
                    return NetworkResult.Error(
                        response.code(),
                        parseErrorMessage(response.errorBody()?.string())
                    )
                }

                val result = response.body()
                    ?: return NetworkResult.Error(response.code(), "Upload file that bai: $name")
                val fileUrl = result.url.ifBlank { result.fileUrl.orEmpty() }
                if (fileUrl.isBlank()) {
                    return NetworkResult.Error(response.code(), "Upload thanh cong nhung thieu URL file: $name")
                }

                UploadedAttachment(
                    fileUrl = fileUrl,
                    fileType = detectFileType(mimeType, name),
                    publicId = result.publicId.ifBlank { null }
                )
            }
            NetworkResult.Success(uploaded)
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun uploadReviewImages(uris: List<Uri>): NetworkResult<List<UploadedAttachment>> {
        return try {
            val uploaded = uris.mapIndexed { index, uri ->
                val name = getDisplayName(uri).ifBlank { "review_image_$index" }
                val mimeType = context.contentResolver.getType(uri)
                    ?: guessMimeType(name)
                    ?: "application/octet-stream"
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return NetworkResult.Error(0, "Không thể đọc file: $name")
                val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", name, body)
                val response = apiService.uploadFile(part, "REVIEW_IMAGE")

                if (!response.isSuccessful) {
                    return NetworkResult.Error(
                        response.code(),
                        parseErrorMessage(response.errorBody()?.string())
                    )
                }

                val result = response.body()
                    ?: return NetworkResult.Error(response.code(), "Upload file thất bại: $name")
                val fileUrl = result.url.ifBlank { result.fileUrl.orEmpty() }
                if (fileUrl.isBlank()) {
                    return NetworkResult.Error(response.code(), "Upload thành công nhưng thiếu URL file: $name")
                }

                UploadedAttachment(
                    fileUrl = fileUrl,
                    fileType = detectFileType(mimeType, name),
                    publicId = result.publicId.ifBlank { null }
                )
            }
            NetworkResult.Success(uploaded)
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    private fun getDisplayName(uri: Uri): String {
        return context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) cursor.getString(index).orEmpty() else ""
                } else {
                    ""
                }
            }
            .orEmpty()
            .ifBlank { uri.lastPathSegment.orEmpty().substringAfterLast('/') }
    }

    private fun guessMimeType(name: String): String? {
        return when (name.substringAfterLast('.', "").lowercase()) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "heic" -> "image/heic"
            else -> null
        }
    }

    private fun detectFileType(mimeType: String?, name: String): String {
        val lowerName = name.lowercase()
        return when {
            mimeType == "application/pdf" || lowerName.endsWith(".pdf") -> "PDF"
            mimeType?.startsWith("image/") == true -> "IMAGE"
            lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
                lowerName.endsWith(".png") || lowerName.endsWith(".webp") ||
                lowerName.endsWith(".heic") -> "IMAGE"
            else -> "OTHER"
        }
    }

    private fun parseErrorMessage(errorBody: String?): String = parseErrorBody(errorBody)
}
