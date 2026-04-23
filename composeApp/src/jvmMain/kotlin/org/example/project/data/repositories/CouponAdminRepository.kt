package org.example.project.data.repositories

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
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
private data class CouponEnvelope<T>(
    val data: T,
    val message: String? = null
)

@Serializable
data class CouponDto(
    val id: String,
    val code: String,
    val name: String,
    val discountType: String,
    val discountValue: Double,
    val minOrderTotal: Double? = null,
    val maxDiscountAmount: Double? = null,
    val usageLimit: Int? = null,
    val usagePerUserLimit: Int? = null,
    val usedCount: Int,
    val isActive: Boolean
)

@Serializable
data class CouponValidateData(
    val couponId: String,
    val code: String,
    val discountAmount: Double,
    val payableAmount: Double
)

@Serializable
data class CouponCreateRequest(
    val code: String,
    val name: String,
    val description: String? = null,
    val discountType: String,
    val discountValue: Double,
    val minOrderTotal: Double? = null,
    val maxDiscountAmount: Double? = null,
    val usageLimit: Int? = null,
    val usagePerUserLimit: Int? = null,
    val isActive: Boolean = true
)

@Serializable
data class CouponValidateRequest(
    val code: String,
    val orderTotal: Double,
    val userId: String? = null
)

@Serializable
private data class IdPayload(
    val id: String
)

class CouponAdminRepository(private val client: HttpClient) {
    private val baseUrl = "http://localhost:8080/api/v1/internal/coupons"
    private val json = Json { ignoreUnknownKeys = true }
    private var authToken: String? = null
    private var authRetryHandler: AuthRetryHandler? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun setAuthRetryHandler(handler: AuthRetryHandler?) {
        authRetryHandler = handler
    }

    suspend fun getCoupons(): Result<List<CouponDto>> = try {
        val response = executeAuthorized { token ->
            client.get(baseUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }

        val envelope = response.body<CouponEnvelope<List<CouponDto>>>()
        Result.success(envelope.data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tai danh sach coupon"))
    }

    suspend fun createCoupon(request: CouponCreateRequest): Result<String> = try {
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

        val payload = response.body<CouponEnvelope<IdPayload>>()
        Result.success(payload.data.id)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tao coupon"))
    }

    suspend fun validateCoupon(code: String, orderTotal: Double, userId: String? = null): Result<CouponValidateData> = try {
        val response = executeAuthorized { token ->
            client.post("$baseUrl/validate") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(CouponValidateRequest(code = code, orderTotal = orderTotal, userId = userId))
            }
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalStateException(extractErrorMessage(response.bodyAsText())))
        }

        val payload = response.body<CouponEnvelope<CouponValidateData>>()
        Result.success(payload.data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the kiem tra coupon"))
    }

    private suspend fun executeAuthorized(request: suspend (String) -> HttpResponse): HttpResponse {
        val token = authToken?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Chua dang nhap de thao tac coupon")

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
        }.getOrElse { raw.ifBlank { "Khong the xu ly coupon" } }
    }
}

