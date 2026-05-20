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
data class PosOrderItemRequest(
    val productId: String,
    val quantity: Int,
    val unit: String? = null
)

@Serializable
data class CreatePosOrderRequest(
    val items: List<PosOrderItemRequest>,
    val customerId: String? = null,
    val note: String? = null,
    val paymentMethod: String = "CASH",
    val cashReceived: Double? = null,
    val couponCode: String? = null
)

@Serializable
data class ConfirmCashRequest(
    val cashReceived: Double? = null
)

@Serializable
data class InitPosPaymentRequest(
    val paymentMethod: String? = null,
    val returnUrl: String? = null
)

@Serializable
private data class PosEnvelope<T>(
    val data: T,
    val message: String? = null
)

@Serializable
data class PosOrderResult(
    val id: String,
    val orderCode: String,
    val status: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val subtotal: Double,
    val discount: Double,
    val total: Double
)

@Serializable
data class PosConfirmResult(
    val id: String,
    val status: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val cashReceived: Double,
    val cashChange: Double
)

@Serializable
data class PosPaymentInitResult(
    val id: String,
    val orderCode: String,
    val status: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val total: Double,
    val paymentUrl: String,
    val qrContent: String,
    val paymentReference: String,
    val amount: Long
)

@Serializable
data class PosOrderStatusResult(
    val id: String,
    val orderCode: String,
    val status: String,
    val paymentMethod: String? = null,
    val paymentStatus: String,
    val total: Double,
    val cashReceived: Double? = null,
    val cashChange: Double? = null,
    val paymentReference: String? = null,
    val paidAt: String? = null
)

class PosRepository(private val client: HttpClient) {
    private val baseUrl = "http://localhost:8080/api/v1/internal/pos/orders"
    private val json = Json { ignoreUnknownKeys = true }
    private var authToken: String? = null
    private var authRetryHandler: AuthRetryHandler? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun setAuthRetryHandler(handler: AuthRetryHandler?) {
        authRetryHandler = handler
    }

    suspend fun createOrder(request: CreatePosOrderRequest): Result<PosOrderResult> = try {
        val response = executeAuthorized { token ->
            client.post(baseUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }

        val payload = response.body<PosEnvelope<PosOrderResult>>()
        Result.success(payload.data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tao don POS"))
    }

    suspend fun confirmCash(orderId: String, cashReceived: Double? = null): Result<PosConfirmResult> = try {
        val response = executeAuthorized { token ->
            client.post("$baseUrl/$orderId/confirm-cash") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(ConfirmCashRequest(cashReceived))
            }
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }

        val payload = response.body<PosEnvelope<PosConfirmResult>>()
        Result.success(payload.data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the xac nhan thanh toan tien mat"))
    }

    suspend fun initPayment(
        orderId: String,
        paymentMethod: String,
        returnUrl: String? = null
    ): Result<PosPaymentInitResult> = try {
        val response = executeAuthorized { token ->
            client.post("$baseUrl/$orderId/init-payment") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(InitPosPaymentRequest(paymentMethod = paymentMethod, returnUrl = returnUrl))
            }
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }

        val payload = response.body<PosEnvelope<PosPaymentInitResult>>()
        Result.success(payload.data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tao QR thanh toan POS"))
    }

    suspend fun getOrderStatus(orderId: String): Result<PosOrderStatusResult> = try {
        val response = executeAuthorized { token ->
            client.get("$baseUrl/$orderId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }

        val payload = response.body<PosEnvelope<PosOrderStatusResult>>()
        Result.success(payload.data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tai trang thai don POS"))
    }

    suspend fun getPendingOrders(): Result<List<PosOrderStatusResult>> = try {
        val response = executeAuthorized { token ->
            client.get("$baseUrl/pending") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }
        val payload = response.body<PosEnvelope<List<PosOrderStatusResult>>>()
        Result.success(payload.data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tai danh sach don cho"))
    }

    suspend fun cancelOrder(orderId: String): Result<Unit> = try {
        val response = executeAuthorized { token ->
            client.post("$baseUrl/$orderId/cancel") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
            }
        }
        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the huy don POS"))
    }

    suspend fun switchToCash(orderId: String): Result<PosOrderStatusResult> = try {
        val response = executeAuthorized { token ->
            client.post("$baseUrl/$orderId/switch-to-cash") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
            }
        }
        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }
        val payload = response.body<PosEnvelope<PosOrderStatusResult>>()
        Result.success(payload.data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the chuyen sang tien mat"))
    }

    private suspend fun executeAuthorized(request: suspend (String) -> HttpResponse): HttpResponse {
        val token = authToken?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Chua dang nhap de thao tac POS")

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
        }.getOrElse { raw.ifBlank { "Khong the xu ly don POS" } }
    }
}

