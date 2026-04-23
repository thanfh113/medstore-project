package org.example.project.data.repositories

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class ShopSettingsRequest(
    val name: String? = null,
    val description: String? = null,
    val logoUrl: String? = null,
    val licenseNumber: String? = null,
    val expiryAlertDays: Int? = null
)

@Serializable
data class ShopSettingsResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val logoUrl: String? = null,
    val licenseNumber: String? = null,
    val isApproved: Boolean,
    val expiryAlertDays: Int
)

class SettingsRepository(
    private val client: HttpClient,
    private val baseUrl: String = "http://localhost:8080/api/v1",
    private val tokenProvider: () -> String? = { null }
) {
    suspend fun getSettings(): Result<ShopSettingsResponse> = runCatching {
        val response: ShopSettingsResponse = client.get("$baseUrl/shop/settings") {
            tokenProvider()?.let { 
                headers {
                    append("Authorization", "Bearer $it")
                }
            }
        }.body()
        response
    }

    suspend fun updateSettings(request: ShopSettingsRequest): Result<ShopSettingsResponse> = runCatching {
        val response: ShopSettingsResponse = client.put("$baseUrl/shop/settings") {
            contentType(ContentType.Application.Json)
            tokenProvider()?.let { 
                headers {
                    append("Authorization", "Bearer $it")
                }
            }
            setBody(request)
        }.body()
        response
    }
}

