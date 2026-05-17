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
private data class FinanceEnvelope<T>(
    val data: T,
    val message: String? = null
)

@Serializable
data class TopProductDto(
    val productId: String,
    val productName: String,
    val quantitySold: Int,
    val revenue: Double
)

@Serializable
data class FinanceSummaryDto(
    val grossRevenue: Double,
    val onlineRevenue: Double,
    val posRevenue: Double,
    val totalDiscount: Double,
    val totalExpenses: Double,
    val netProfit: Double,
    val successfulOrderCount: Int,
    val expenseCount: Int,
    val averageOrderValue: Double = 0.0,
    val cancelledOrderCount: Int = 0,
    val totalOrderCount: Int = 0,
    val topSellingProducts: List<TopProductDto> = emptyList()
)

class FinanceRepository(private val client: HttpClient) {
    private val baseUrl = "http://localhost:8080/api/v1/admin/finance"
    private val json = Json { ignoreUnknownKeys = true }
    private var authToken: String? = null
    private var authRetryHandler: AuthRetryHandler? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun setAuthRetryHandler(handler: AuthRetryHandler?) {
        authRetryHandler = handler
    }

    suspend fun getFinanceSummary(period: String = "ALL"): Result<FinanceSummaryDto> = try {
        val response = executeAuthorized { token ->
            client.get("$baseUrl?period=$period") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }

        val payload = response.body<FinanceEnvelope<FinanceSummaryDto>>()
        Result.success(payload.data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tai bao cao tai chinh"))
    }

    private suspend fun executeAuthorized(request: suspend (String) -> HttpResponse): HttpResponse {
        val token = authToken?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Chua dang nhap de xem tai chinh")

        val first = request(token)
        if (first.status != HttpStatusCode.Unauthorized) return first

        val refreshed = authRetryHandler?.refreshAccessToken()
        if (refreshed.isNullOrBlank()) {
            authRetryHandler?.onAuthFailed()
            return first
        }

        authToken = refreshed
        val retry = request(refreshed)
        if (retry.status == HttpStatusCode.Unauthorized) {
            authRetryHandler?.onAuthFailed()
        }
        return retry
    }

    private fun extractErrorMessage(raw: String): String {
        return runCatching {
            val element = json.parseToJsonElement(raw).jsonObject
            element["error"]?.jsonPrimitive?.content
                ?: element["message"]?.jsonPrimitive?.content
                ?: raw
        }.getOrElse { raw.ifBlank { "Khong the xem tai chinh" } }
    }
}

