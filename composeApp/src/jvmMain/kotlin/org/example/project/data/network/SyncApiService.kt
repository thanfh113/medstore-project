package org.example.project.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.example.project.data.repositories.AuthRetryHandler

@Serializable
data class PushSyncChange(
    val entityType: String,
    val entityId: String,
    val operation: String,
    val payload: JsonElement? = null,
    val clientMutationId: String
)

@Serializable
data class PushSyncRequest(
    val deviceId: String,
    val changes: List<PushSyncChange>
)

@Serializable
data class PushSyncAckItem(
    val entityType: String,
    val entityId: String,
    val clientMutationId: String? = null,
    val serverVersion: Long,
    val status: String
)

@Serializable
data class PushSyncResponse(
    val accepted: List<PushSyncAckItem>,
    val latestServerVersion: Long,
    val message: String
)

@Serializable
data class PullSyncChangeItem(
    val id: String,
    val serverVersion: Long,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val payloadJson: String? = null,
    val sourceDeviceId: String? = null,
    val createdAt: String
)

@Serializable
data class PullSyncResponse(
    val data: List<PullSyncChangeItem>,
    val latestServerVersion: Long,
    val message: String
)

class SyncApiService(private val client: HttpClient) {
    private val baseUrl = "http://localhost:8080/api/v1/internal/sync"
    private val json = Json { ignoreUnknownKeys = true }
    private var authToken: String? = null
    private var authRetryHandler: AuthRetryHandler? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun setAuthRetryHandler(handler: AuthRetryHandler?) {
        authRetryHandler = handler
    }

    suspend fun pushChanges(request: PushSyncRequest): Result<PushSyncResponse> = try {
        val response = executeAuthorized { token ->
            client.post("$baseUrl/push") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        }

        Result.success(response.body())
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the push sync"))
    }

    suspend fun pullChanges(deviceId: String, sinceVersion: Long, limit: Int = 500): Result<PullSyncResponse> = try {
        val response = executeAuthorized { token ->
            client.get("$baseUrl/pull") {
                header(HttpHeaders.Authorization, "Bearer $token")
                url.parameters.append("deviceId", deviceId)
                url.parameters.append("sinceVersion", sinceVersion.toString())
                url.parameters.append("limit", limit.toString())
            }
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        }

        Result.success(response.body())
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the pull sync"))
    }

    private suspend fun executeAuthorized(request: suspend (String) -> HttpResponse): HttpResponse {
        val firstToken = authToken?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Chua dang nhap de dong bo")

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
            val element = json.parseToJsonElement(raw).jsonObject
            element["error"]?.jsonPrimitive?.content
                ?: element["message"]?.jsonPrimitive?.content
                ?: raw
        }.getOrElse { raw.ifBlank { "Dong bo that bai" } }
    }
}

