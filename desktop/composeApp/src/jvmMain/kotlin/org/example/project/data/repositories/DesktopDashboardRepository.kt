package org.example.project.data.repositories

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
private data class DashboardEnvelope<T>(
    val data: T,
    val message: String? = null
)

@Serializable
data class DashboardRecentOrderDto(
    val orderId: String,
    val orderCode: String,
    val customerName: String,
    val total: Double,
    val status: String,
    val paymentStatus: String,
    val orderChannel: String,
    val createdAt: String
)

@Serializable
data class DashboardPeriodDto(
    val revenue: Double,
    val orderCount: Int,
    val completedOrderCount: Int,
    val pendingOrderCount: Int,
    val posRevenue: Double,
    val posOrderCount: Int,
    val posCompletedOrderCount: Int,
    val onlineRevenue: Double,
    val onlineOrderCount: Int,
    val onlineCompletedOrderCount: Int
)

@Serializable
data class InternalDashboardDto(
    val totalRevenue: Double,
    val totalOrders: Int,
    val totalProducts: Int,
    val totalCustomers: Int,
    val pendingOrders: Int,
    val today: DashboardPeriodDto,
    val month: DashboardPeriodDto,
    val recentOrders: List<DashboardRecentOrderDto>
)

class DesktopDashboardRepository(private val client: HttpClient) {
    private val baseUrl = "http://localhost:8080/api/v1/internal/dashboard"
    private val json = Json { ignoreUnknownKeys = true }
    private var authToken: String? = null
    private var authRetryHandler: AuthRetryHandler? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun setAuthRetryHandler(handler: AuthRetryHandler?) {
        authRetryHandler = handler
    }

    suspend fun getDashboard(): Result<InternalDashboardDto> = try {
        val httpResponse = executeAuthorizedGet(baseUrl)

        if (!httpResponse.status.isSuccess()) {
            val raw = httpResponse.bodyAsText()
            return Result.failure(IllegalStateException(extractErrorMessage(raw)))
        }

        val response = httpResponse.body<DashboardEnvelope<InternalDashboardDto>>()
        Result.success(response.data)
    } catch (e: Exception) {
            Result.failure(IllegalStateException(e.message ?: "Không thể tải dashboard"))
    }

    private fun extractErrorMessage(raw: String): String {
        return runCatching {
            val element = json.parseToJsonElement(raw).jsonObject
            element["error"]?.jsonPrimitive?.content
                ?: element["message"]?.jsonPrimitive?.content
                ?: raw
        }.getOrElse { raw.ifBlank { "Không thể tải dashboard" } }
    }

    private suspend fun executeAuthorizedGet(url: String): HttpResponse {
        val firstToken = authToken?.takeIf { it.isNotBlank() }
        val firstResponse = client.get(url) {
            firstToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }
        if (firstResponse.status != HttpStatusCode.Unauthorized) return firstResponse

        val refreshedToken = authRetryHandler?.refreshAccessToken()
        if (refreshedToken.isNullOrBlank()) {
            authRetryHandler?.onAuthFailed()
            return firstResponse
        }

        authToken = refreshedToken
        val retryResponse = client.get(url) {
            header(HttpHeaders.Authorization, "Bearer $refreshedToken")
        }
        if (retryResponse.status == HttpStatusCode.Unauthorized) {
            authRetryHandler?.onAuthFailed()
        }
        return retryResponse
    }
}
