package org.example.project.data.repositories

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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
private data class InternalDataEnvelope<T>(
    val data: T,
    val message: String? = null
)

@Serializable
data class InternalOrderSummaryDto(
    val id: String,
    val orderCode: String,
    val customerId: String,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val orderChannel: String,
    val status: String,
    val paymentMethod: String? = null,
    val paymentStatus: String,
    val total: Double? = null,
    val note: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class InternalOrderItemDto(
    val id: String,
    val productId: String? = null,
    val name: String,
    val price: Double,
    val quantity: Int,
    val unit: String,
    val categoryName: String? = null,
    val description: String? = null,
    val manufacturer: String? = null,
    val origin: String? = null,
    val sku: String? = null,
    val registrationNumber: String? = null,
    val riskClassification: String? = null,
    val imageUrls: List<String> = emptyList(),
    val attributes: Map<String, String> = emptyMap()
)

@Serializable
data class InternalOrderDetailDto(
    val id: String,
    val orderCode: String,
    val customerId: String,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val orderChannel: String,
    val cashierUserId: String? = null,
    val cashierName: String? = null,
    val address: String? = null,
    val ward: String? = null,
    val district: String? = null,
    val province: String? = null,
    val status: String,
    val paymentMethod: String? = null,
    val paymentStatus: String,
    val subtotal: Double? = null,
    val shippingFee: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double? = null,
    val cashReceived: Double? = null,
    val cashChange: Double? = null,
    val paymentReference: String? = null,
    val paidAt: String? = null,
    val note: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val items: List<InternalOrderItemDto> = emptyList()
)

@Serializable
private data class InternalOrderStatusUpdateDataDto(
    val id: String,
    val status: String
)

@Serializable
private data class OrderStatusUpdateRequest(
    val status: String
)

class DesktopOrderRepository(
    private val client: HttpClient
) {
    private val baseUrl = "http://localhost:8080/api/v1/internal/orders"
    private val json = Json { ignoreUnknownKeys = true }
    private var authToken: String? = null
    private var authRetryHandler: AuthRetryHandler? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun setAuthRetryHandler(handler: AuthRetryHandler?) {
        authRetryHandler = handler
    }

    suspend fun getOrders(status: String? = null, channel: String? = null): Result<List<InternalOrderSummaryDto>> = try {
        val httpResponse = executeAuthorized(
            request = { token ->
                client.get(baseUrl) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    status?.takeIf { it.isNotBlank() }?.let { url.parameters.append("status", it) }
                    channel?.takeIf { it.isNotBlank() }?.let { url.parameters.append("channel", it) }
                }
            }
        )

        if (!httpResponse.status.isSuccess()) {
            val raw = httpResponse.bodyAsText()
            return Result.failure(IllegalStateException(extractErrorMessage(raw)))
        }

        val response = httpResponse.body<InternalDataEnvelope<List<InternalOrderSummaryDto>>>()
        Result.success(response.data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tai don hang"))
    }

    suspend fun getOrderDetail(orderId: String): Result<InternalOrderDetailDto> = try {
        val httpResponse = executeAuthorized(
            request = { token ->
                client.get("$baseUrl/$orderId") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        )

        if (!httpResponse.status.isSuccess()) {
            val raw = httpResponse.bodyAsText()
            return Result.failure(IllegalStateException(extractErrorMessage(raw)))
        }

        val response = httpResponse.body<InternalDataEnvelope<InternalOrderDetailDto>>()
        Result.success(response.data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tai chi tiet don hang"))
    }

    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> = try {
        val httpResponse = executeAuthorized(
            request = { token ->
                client.post("$baseUrl/$orderId/status") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(OrderStatusUpdateRequest(status))
                }
            }
        )

        if (!httpResponse.status.isSuccess()) {
            val raw = httpResponse.bodyAsText()
            return Result.failure(IllegalStateException(extractErrorMessage(raw)))
        }

        httpResponse.body<InternalDataEnvelope<InternalOrderStatusUpdateDataDto>>()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the cap nhat trang thai don hang"))
    }

    private fun extractErrorMessage(raw: String): String {
        return runCatching {
            val element = json.parseToJsonElement(raw).jsonObject
            element["error"]?.jsonPrimitive?.content
                ?: element["message"]?.jsonPrimitive?.content
                ?: raw
        }.getOrElse { raw.ifBlank { "Không thể xử lý đơn hàng" } }
    }

    private suspend fun executeAuthorized(
        request: suspend (String) -> HttpResponse
    ): HttpResponse {
        val firstToken = authToken?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Chua dang nhap de tai du lieu don hang")

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
