package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable

typealias UserAddressDto = UserAddress

@Serializable
data class CheckoutRequest(
    val addressId: String,
    val pickupType: String,
    val paymentMethod: String,
    val rewardPointsToUse: Int = 0,
    val promoCode: String? = null,
    val notes: String? = null
)

@Serializable
data class CheckoutOrderSummaryDto(
    val id: String,
    val orderCode: String,
    val total: Double? = null,
    val paymentMethod: String,
    val paymentStatus: String,
    val paymentUrl: String? = null
)

@Serializable
data class PaymentInitRequest(
    val orderId: String,
    val returnUrl: String
)

@Serializable
data class PaymentInitData(
    val paymentUrl: String,
    val transactionRef: String? = null,
    val requestId: String? = null,
    val amount: Long? = null,
    val status: String? = null,
    val qrContent: String? = null,
    val deeplink: String? = null
)

@Serializable
data class CodPaymentRequest(
    val orderId: String
)

@Serializable
data class CodPaymentData(
    val paymentId: String
)

@Serializable
data class PaymentStatusDto(
    val orderId: String,
    val paymentId: String,
    val method: String,
    val amount: Double,
    val status: String,
    val transactionRef: String? = null,
    val paidAt: String? = null
)

@Serializable
data class SendMessageRequest(
    val content: String,
    val type: String = "TEXT"
)
