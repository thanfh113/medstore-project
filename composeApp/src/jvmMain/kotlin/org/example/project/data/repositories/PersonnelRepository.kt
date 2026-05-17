package org.example.project.data.repositories

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

@Serializable
data class PersonnelEmployeeProfileDto(
    val id: String,
    val qualificationTitle: String,
    val qualificationSpecialty: String? = null,
    val qualificationInstitution: String? = null,
    val qualificationDocumentUrl: String? = null,
    val qualificationDocumentPublicId: String? = null,
    val qualificationDocumentType: String? = null,
    val qualificationDocumentResourceType: String? = null,
    val qualificationVerified: Boolean = false,
    val qualificationSubmittedAt: String? = null,
    val qualificationVerifiedBy: String? = null,
    val qualificationVerifiedAt: String? = null,
    val qualificationNote: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class PersonnelEmployeeProfileRequest(
    val qualificationTitle: String? = null,
    val qualificationSpecialty: String? = null,
    val qualificationInstitution: String? = null,
    val qualificationDocumentUrl: String? = null,
    val qualificationDocumentPublicId: String? = null,
    val qualificationDocumentType: String? = null,
    val qualificationDocumentResourceType: String? = null,
    val qualificationVerified: Boolean? = null,
    val qualificationNote: String? = null
)

data class PersonnelUploadedDocument(
    val url: String,
    val publicId: String,
    val fileType: String,
    val resourceType: String? = null
)

@Serializable
data class PersonnelUserDto(
    val id: String,
    val fullName: String? = null,
    val phone: String,
    val email: String? = null,
    val role: String,
    val isActive: Boolean,
    val failedLoginAttempts: Int = 0,
    val createdAt: String,
    val employeeProfile: PersonnelEmployeeProfileDto? = null
)

@Serializable
private data class PersonnelEnvelope<T>(
    val data: T,
    val message: String
)

@Serializable
private data class PersonnelUploadResponse(
    val url: String,
    val publicId: String,
    val resourceType: String? = null,
    val format: String? = null,
    val bytes: Int? = null
)

@Serializable
data class CreatePersonnelUserRequest(
    val fullName: String? = null,
    val phone: String,
    val email: String? = null,
    val password: String,
    val role: String,
    val employeeProfile: PersonnelEmployeeProfileRequest? = null
)

@Serializable
data class UpdatePersonnelUserRequest(
    val fullName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val role: String? = null,
    val employeeProfile: PersonnelEmployeeProfileRequest? = null
)

@Serializable
data class ResetPersonnelPasswordRequest(
    val newPassword: String
)

class PersonnelRepository(private val client: HttpClient) {
    private val apiBaseUrl = "http://localhost:8080/api/v1"
    private val baseUrl = "http://localhost:8080/api/v1/admin"
    private val json = Json { ignoreUnknownKeys = true }
    private var authToken: String? = null
    private var authRetryHandler: AuthRetryHandler? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun setAuthRetryHandler(handler: AuthRetryHandler?) {
        authRetryHandler = handler
    }

    suspend fun getUsers(): Result<List<PersonnelUserDto>> = try {
        val response = executeAuthorized { token ->
            client.get("$baseUrl/users") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        }
        Result.success(response.body<PersonnelEnvelope<List<PersonnelUserDto>>>().data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tai nhan su"))
    }

    suspend fun createUser(request: CreatePersonnelUserRequest): Result<Unit> = try {
        val response = executeAuthorized { token ->
            client.post("$baseUrl/users") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tao tai khoan"))
    }

    suspend fun updateUser(userId: String, request: UpdatePersonnelUserRequest): Result<Unit> = try {
        val response = executeAuthorized { token ->
            client.put("$baseUrl/users/$userId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the cap nhat tai khoan"))
    }

    suspend fun toggleLock(userId: String): Result<Unit> = try {
        val response = executeAuthorized { token ->
            client.put("$baseUrl/users/$userId/ban") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the khoa/mo khoa tai khoan"))
    }

    suspend fun resetPassword(userId: String, newPassword: String): Result<Unit> = try {
        val response = executeAuthorized { token ->
            client.post("$baseUrl/users/$userId/reset-password") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(ResetPersonnelPasswordRequest(newPassword))
            }
        }
        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the reset mat khau"))
    }

    suspend fun deleteUser(userId: String): Result<Unit> = try {
        val response = executeAuthorized { token ->
            client.delete("$baseUrl/users/$userId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the xoa tai khoan"))
    }

    suspend fun uploadQualificationDocument(file: File): Result<PersonnelUploadedDocument> = try {
        val response = executeAuthorized { token ->
            client.submitFormWithBinaryData(
                url = "$apiBaseUrl/upload?type=CERTIFICATE",
                formData = formData {
                    append("file", file.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, getContentType(file.extension))
                        append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                    })
                }
            ) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        }

        val upload = response.body<PersonnelUploadResponse>()
        Result.success(
            PersonnelUploadedDocument(
                url = upload.url,
                publicId = upload.publicId,
                fileType = detectUploadedFileType(upload),
                resourceType = upload.resourceType
            )
        )
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the upload minh chung chuyen mon"))
    }

    private suspend fun executeAuthorized(request: suspend (String) -> HttpResponse): HttpResponse {
        val firstToken = authToken?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Chua dang nhap")

        val firstResponse = request(firstToken)
        if (firstResponse.status != HttpStatusCode.Unauthorized) return firstResponse

        val refreshedToken = authRetryHandler?.refreshAccessToken()
        if (refreshedToken.isNullOrBlank()) {
            authRetryHandler?.onAuthFailed()
            return firstResponse
        }

        authToken = refreshedToken
        val retryResponse = request(refreshedToken)
        if (retryResponse.status == HttpStatusCode.Unauthorized) {
            authRetryHandler?.onAuthFailed()
        }
        return retryResponse
    }

    private fun extractError(raw: String): String {
        return runCatching {
            val obj = json.parseToJsonElement(raw).jsonObject
            obj["error"]?.jsonPrimitive?.content
                ?: obj["message"]?.jsonPrimitive?.content
                ?: raw
        }.getOrElse { raw.ifBlank { "Request failed" } }
    }

    private fun getContentType(extension: String): String {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "heic" -> "image/heic"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
    }

    private fun detectUploadedFileType(upload: PersonnelUploadResponse): String {
        return when {
            upload.resourceType.equals("raw", ignoreCase = true) -> "PDF"
            upload.format.equals("pdf", ignoreCase = true) -> "PDF"
            else -> "IMAGE"
        }
    }
}

