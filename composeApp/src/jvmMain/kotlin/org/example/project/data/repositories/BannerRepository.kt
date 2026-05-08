package org.example.project.data.repositories

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
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
private data class BannerEnvelope<T>(
    val data: T,
    val message: String? = null
)

@Serializable
data class DesktopBannerDto(
    val id: String,
    val imageUrl: String,
    val linkUrl: String? = null,
    val title: String? = null,
    val description: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val startDt: String? = null,
    val endDt: String? = null,
    val createdAt: String? = null
)

@Serializable
data class DesktopBannerUpsertRequest(
    val imageUrl: String,
    val linkUrl: String? = null,
    val title: String? = null,
    val description: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val startDt: String? = null,
    val endDt: String? = null
)

@Serializable
private data class BannerUploadResponse(
    val url: String,
    val publicId: String,
    val format: String,
    val resourceType: String,
    val bytes: Int
)

class BannerRepository(private val client: HttpClient) {
    private val baseUrl = "http://localhost:8080/api/v1"
    private val adminUrl = "$baseUrl/admin/banners"
    private val json = Json { ignoreUnknownKeys = true }
    private var authToken: String? = null
    private var authRetryHandler: AuthRetryHandler? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun setAuthRetryHandler(handler: AuthRetryHandler?) {
        authRetryHandler = handler
    }

    suspend fun getBanners(): Result<List<DesktopBannerDto>> = try {
        val response = executeAuthorized { token ->
            client.get(adminUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }

        Result.success(response.body<BannerEnvelope<List<DesktopBannerDto>>>().data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Không thể tải banner"))
    }

    suspend fun createBanner(request: DesktopBannerUpsertRequest): Result<DesktopBannerDto> = try {
        val response = executeAuthorized { token ->
            client.post(adminUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }

        Result.success(response.body<BannerEnvelope<DesktopBannerDto>>().data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Không thể tạo banner"))
    }

    suspend fun updateBanner(id: String, request: DesktopBannerUpsertRequest): Result<DesktopBannerDto> = try {
        val response = executeAuthorized { token ->
            client.put("$adminUrl/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }

        Result.success(response.body<BannerEnvelope<DesktopBannerDto>>().data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Không thể cập nhật banner"))
    }

    suspend fun deleteBanner(id: String): Result<Unit> = try {
        val response = executeAuthorized { token ->
            client.delete("$adminUrl/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Không thể xóa banner"))
    }

    suspend fun uploadBannerImage(file: File): Result<String> = try {
        val response = executeAuthorized { token ->
            client.submitFormWithBinaryData(
                url = "$baseUrl/upload?type=BANNER",
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
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }

        Result.success(response.body<BannerUploadResponse>().url)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Không thể upload ảnh banner"))
    }

    private fun getContentType(extension: String): String {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "heic" -> "image/heic"
            else -> "application/octet-stream"
        }
    }

    private fun extractErrorMessage(raw: String): String {
        return runCatching {
            val element = json.parseToJsonElement(raw).jsonObject
            element["error"]?.jsonPrimitive?.content
                ?: element["message"]?.jsonPrimitive?.content
                ?: raw
        }.getOrElse { raw.ifBlank { "Không thể xử lý banner" } }
    }

    private suspend fun executeAuthorized(
        request: suspend (String) -> HttpResponse
    ): HttpResponse {
        val firstToken = authToken?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Chưa đăng nhập để thực hiện thao tác")

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
}
