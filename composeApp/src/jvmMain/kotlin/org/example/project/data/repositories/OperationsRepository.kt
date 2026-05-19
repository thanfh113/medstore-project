package org.example.project.data.repositories

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
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
data class OperationsEnvelope<T>(
    val data: T,
    val message: String? = null
)

@Serializable
data class OperationsReviewDto(
    val id: String,
    val productId: String,
    val userId: String,
    val userName: String? = null,
    val orderId: String? = null,
    val rating: Int,
    val title: String? = null,
    val comment: String? = null,
    val status: String,
    val isVerifiedPurchase: Boolean = false,
    val reportCount: Int = 0,
    val hiddenReason: String? = null,
    val moderatedBy: String? = null,
    val moderatedAt: String? = null,
    val createdAt: String,
    val updatedAt: String? = null,
    val attachments: List<OperationsReviewAttachmentDto> = emptyList()
)

@Serializable
data class OperationsReviewAttachmentDto(
    val id: String,
    val fileUrl: String,
    val fileType: String,
    val publicId: String? = null,
    val sortOrder: Int = 0,
    val createdAt: String
)

@Serializable
data class OperationsReviewReportDto(
    val id: String,
    val reviewId: String,
    val reporterUserId: String,
    val reporterName: String? = null,
    val reason: String,
    val note: String? = null,
    val status: String,
    val handledBy: String? = null,
    val handledAt: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val review: OperationsReviewDto? = null
)

@Serializable
data class ModerateReviewRequest(
    val status: String,
    val hiddenReason: String? = null
)

@Serializable
data class UpdateReviewReportRequest(
    val status: String,
    val reviewStatus: String? = null,
    val hiddenReason: String? = null
)

@Serializable
data class OperationsComplaintDto(
    val id: String,
    val complaintCode: String,
    val userId: String,
    val userName: String? = null,
    val orderId: String,
    val productName: String? = null,
    val type: String,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    val resolution: String? = null,
    val refundAmount: Double? = null,
    val refundStatus: String = "NONE",
    val refundMethod: String? = null,
    val refundTransactionId: String? = null,
    val refundedBy: String? = null,
    val refundedAt: String? = null,
    val handledBy: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val resolvedAt: String? = null,
    val closedAt: String? = null,
    val orderTotal: Double? = null,
    val attachments: List<OperationsComplaintAttachmentDto> = emptyList(),
    val messages: List<OperationsComplaintMessageDto> = emptyList(),
    val events: List<OperationsComplaintEventDto> = emptyList()
)

@Serializable
data class OperationsComplaintAttachmentDto(
    val id: String,
    val fileUrl: String,
    val fileType: String,
    val publicId: String? = null,
    val createdAt: String
)

@Serializable
data class OperationsComplaintEventDto(
    val id: String,
    val actorUserId: String? = null,
    val actorName: String? = null,
    val actorRole: String? = null,
    val eventType: String,
    val title: String,
    val description: String? = null,
    val fromStatus: String? = null,
    val toStatus: String? = null,
    val fromPriority: String? = null,
    val toPriority: String? = null,
    val dueAt: String? = null,
    val createdAt: String
)

@Serializable
data class OperationsComplaintMessageDto(
    val id: String,
    val senderUserId: String,
    val senderName: String? = null,
    val senderRole: String,
    val message: String,
    val isInternal: Boolean,
    val createdAt: String
)

@Serializable
data class UpdateComplaintRequest(
    val status: String? = null,
    val priority: String? = null,
    val resolution: String? = null,
    val refundAmount: Double? = null,
    val refundStatus: String? = null,
    val refundMethod: String? = null,
    val refundTransactionId: String? = null,
    val handledBy: String? = null,
    val restoreStock: Boolean = false
)

@Serializable
data class ComplaintMessageRequest(
    val message: String,
    val isInternal: Boolean = false
)

@Serializable
data class OperationsRewardRedemptionDto(
    val id: String,
    val userId: String,
    val userName: String? = null,
    val rewardProductId: String,
    val productName: String,
    val productImageUrl: String? = null,
    val rewardType: String = "ITEM",
    val quantity: Int,
    val pointsUsed: Int,
    val status: String,
    val issuedVoucherCode: String? = null,
    val voucherIssuedAt: String? = null,
    val voucherUsedAt: String? = null,
    val redeemedOrderId: String? = null,
    val assignedTo: String? = null,
    val handledBy: String? = null,
    val handledAt: String? = null,
    val note: String? = null,
    val createdAt: String,
    val updatedAt: String? = null
)

@Serializable
data class UpdateRewardRedemptionRequest(
    val status: String,
    val assignedTo: String? = null,
    val note: String? = null
)

@Serializable
data class AdjustRewardPointsRequest(
    val userId: String,
    val points: Int,
    val description: String,
    val refType: String? = "ADMIN_ADJUSTMENT",
    val refId: String? = null,
    val metadata: String? = null
)

class OperationsRepository(private val client: HttpClient) {
    private val baseUrl = "http://localhost:8080/api/v1/internal"
    private val json = Json { ignoreUnknownKeys = true }
    private var authToken: String? = null
    private var authRetryHandler: AuthRetryHandler? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun setAuthRetryHandler(handler: AuthRetryHandler?) {
        authRetryHandler = handler
    }

    suspend fun getReviews(status: String? = null): Result<List<OperationsReviewDto>> = try {
        val response = executeAuthorized { token ->
            client.get("$baseUrl/reviews") {
                header(HttpHeaders.Authorization, "Bearer $token")
                status?.takeIf { it.isNotBlank() }?.let { url.parameters.append("status", it) }
            }
        }
        if (!response.status.isSuccess()) return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        Result.success(response.body<List<OperationsReviewDto>>())
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tai danh gia"))
    }

    suspend fun moderateReview(id: String, status: String, reason: String?): Result<OperationsReviewDto> = try {
        val response = executeAuthorized { token ->
            client.patch("$baseUrl/reviews/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(ModerateReviewRequest(status, reason?.ifBlank { null }))
            }
        }
        if (!response.status.isSuccess()) return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        Result.success(response.body())
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the cap nhat danh gia"))
    }

    suspend fun getReviewReports(status: String? = null): Result<List<OperationsReviewReportDto>> = try {
        val response = executeAuthorized { token ->
            client.get("$baseUrl/review-reports") {
                header(HttpHeaders.Authorization, "Bearer $token")
                status?.takeIf { it.isNotBlank() }?.let { url.parameters.append("status", it) }
            }
        }
        if (!response.status.isSuccess()) return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        Result.success(response.body<List<OperationsReviewReportDto>>())
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tai bao cao danh gia"))
    }

    suspend fun updateReviewReport(
        id: String,
        status: String,
        reviewStatus: String?,
        hiddenReason: String?
    ): Result<OperationsReviewReportDto> = try {
        val response = executeAuthorized { token ->
            client.patch("$baseUrl/review-reports/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(
                    UpdateReviewReportRequest(
                        status = status,
                        reviewStatus = reviewStatus,
                        hiddenReason = hiddenReason?.ifBlank { null }
                    )
                )
            }
        }
        if (!response.status.isSuccess()) return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        Result.success(response.body())
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the xu ly bao cao danh gia"))
    }

    suspend fun getComplaints(
        status: String? = null,
        priority: String? = null,
        type: String? = null
    ): Result<List<OperationsComplaintDto>> = try {
        val response = executeAuthorized { token ->
            client.get("$baseUrl/complaints") {
                header(HttpHeaders.Authorization, "Bearer $token")
                status?.takeIf { it.isNotBlank() }?.let { url.parameters.append("status", it) }
                priority?.takeIf { it.isNotBlank() }?.let { url.parameters.append("priority", it) }
                type?.takeIf { it.isNotBlank() }?.let { url.parameters.append("type", it) }
            }
        }
        if (!response.status.isSuccess()) return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        Result.success(response.body<OperationsEnvelope<List<OperationsComplaintDto>>>().data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tai khieu nai"))
    }

    suspend fun updateComplaint(id: String, request: UpdateComplaintRequest): Result<OperationsComplaintDto> = try {
        val response = executeAuthorized { token ->
            client.patch("$baseUrl/complaints/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
        if (!response.status.isSuccess()) return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        Result.success(response.body<OperationsEnvelope<OperationsComplaintDto>>().data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the cap nhat khieu nai"))
    }

    suspend fun syncComplaintRefund(id: String): Result<Boolean> = try {
        val response = executeAuthorized { token ->
            client.post("$baseUrl/complaints/$id/sync-refund") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        if (!response.status.isSuccess()) return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        val body = response.bodyAsText()
        Result.success(body.contains("\"synced\":true"))
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the dong bo trang thai"))
    }

    suspend fun getComplaint(id: String): Result<OperationsComplaintDto> = try {
        val response = executeAuthorized { token ->
            client.get("$baseUrl/complaints/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        if (!response.status.isSuccess()) return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        Result.success(response.body<OperationsEnvelope<OperationsComplaintDto>>().data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tai chi tiet khieu nai"))
    }

    suspend fun sendComplaintMessage(
        id: String,
        message: String,
        isInternal: Boolean
    ): Result<OperationsComplaintMessageDto> = try {
        val response = executeAuthorized { token ->
            client.post("$baseUrl/complaints/$id/messages") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(ComplaintMessageRequest(message = message, isInternal = isInternal))
            }
        }
        if (!response.status.isSuccess()) return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        Result.success(response.body<OperationsEnvelope<OperationsComplaintMessageDto>>().data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the gui phan hoi khieu nai"))
    }

    suspend fun getRewardRedemptions(status: String? = null): Result<List<OperationsRewardRedemptionDto>> = try {
        val response = executeAuthorized { token ->
            client.get("$baseUrl/rewards/redemptions") {
                header(HttpHeaders.Authorization, "Bearer $token")
                status?.takeIf { it.isNotBlank() }?.let { url.parameters.append("status", it) }
            }
        }
        if (!response.status.isSuccess()) return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        Result.success(response.body<OperationsEnvelope<List<OperationsRewardRedemptionDto>>>().data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tai yeu cau doi diem"))
    }

    suspend fun updateRewardRedemption(id: String, status: String, note: String?): Result<OperationsRewardRedemptionDto> = try {
        val response = executeAuthorized { token ->
            client.patch("$baseUrl/rewards/redemptions/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(UpdateRewardRedemptionRequest(status = status, note = note?.ifBlank { null }))
            }
        }
        if (!response.status.isSuccess()) return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        Result.success(response.body<OperationsEnvelope<OperationsRewardRedemptionDto>>().data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the cap nhat doi diem"))
    }

    suspend fun adjustPoints(userId: String, points: Int, description: String): Result<Unit> = try {
        val response = executeAuthorized { token ->
            client.post("$baseUrl/rewards/adjust-points") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(AdjustRewardPointsRequest(userId = userId, points = points, description = description))
            }
        }
        if (!response.status.isSuccess()) return Result.failure(IllegalStateException(extractError(response.bodyAsText())))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the dieu chinh diem"))
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
}
