package org.example.project.data.repositories

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class LoginRequest(
    val credential: String,
    val password: String
)

@Serializable
data class AuthUserDto(
    val id: String,
    val phone: String,
    val fullName: String? = null,
    val email: String? = null,
    val role: String,
    val avatarUrl: String? = null
)

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val user: AuthUserDto
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String
)

class AuthRepository(private val client: HttpClient) {
    private val baseUrl = "http://localhost:8080/api/v1/auth"
    private val json = Json { ignoreUnknownKeys = true }

    // In-memory token storage
    private var cachedAccessToken: String? = null
    private var cachedRefreshToken: String? = null

    suspend fun login(credential: String, password: String): Result<AuthResponseDto> = try {
        val httpResponse = client.post("$baseUrl/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(credential = credential.trim(), password = password))
        }

        if (!httpResponse.status.isSuccess()) {
            val raw = httpResponse.bodyAsText()
            return Result.failure(IllegalStateException(extractErrorMessage(raw)))
        }

        val response = httpResponse.body<AuthResponseDto>()
        // Cache tokens after successful login
        cachedAccessToken = response.accessToken
        cachedRefreshToken = response.refreshToken
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Đăng nhập thất bại"))
    }

    suspend fun refreshAccessToken(refreshToken: String): Result<TokenResponseDto> = try {
        val httpResponse = client.post("$baseUrl/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(refreshToken))
        }

        if (!httpResponse.status.isSuccess()) {
            val raw = httpResponse.bodyAsText()
            return Result.failure(IllegalStateException(extractErrorMessage(raw)))
        }

        val response = httpResponse.body<TokenResponseDto>()
        // Update cached tokens
        cachedAccessToken = response.accessToken
        cachedRefreshToken = response.refreshToken
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Làm mới token thất bại"))
    }

    fun getCachedAccessToken(): String? = cachedAccessToken

    fun getCachedRefreshToken(): String? = cachedRefreshToken

    fun setTokens(accessToken: String, refreshToken: String) {
        cachedAccessToken = accessToken
        cachedRefreshToken = refreshToken
    }

    fun clearTokens() {
        cachedAccessToken = null
        cachedRefreshToken = null
    }

    private fun extractErrorMessage(raw: String): String {
        return runCatching {
            val element = json.parseToJsonElement(raw).jsonObject
            element["error"]?.jsonPrimitive?.content
                ?: element["message"]?.jsonPrimitive?.content
                ?: raw
        }.getOrElse { raw.ifBlank { "Đăng nhập thất bại" } }
    }
}
