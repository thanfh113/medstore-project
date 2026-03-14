package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable

/**
 * Reward system related data transfer objects
 */

@Serializable
data class RewardAccountDto(
    val id: Int,
    val userId: Int,
    val totalPoints: Int,
    val availablePoints: Int,
    val usedPoints: Int,
    val tier: String, // "bronze", "silver", "gold", "platinum"
    val tierBenefits: List<String>,
    val nextTierPoints: Int?,
    val pointsHistory: List<PointTransactionDto>,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class PointTransactionDto(
    val id: Int,
    val userId: Int,
    val type: String, // "earned", "used", "expired"
    val points: Int,
    val description: String,
    val orderId: Int?,
    val redeemId: Int?,
    val expiryDate: String?,
    val createdAt: String
)

@Serializable
data class RewardProductDto(
    val id: Int,
    val name: String,
    val description: String,
    val pointsRequired: Int,
    val category: String, // "discount", "product", "service"
    val value: String, // "10.000đ", "5%", etc.
    val imageUrl: String?,
    val termsConditions: String?,
    val validFrom: String?,
    val validTo: String?,
    val stock: Int,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class RedeemRequest(
    val rewardProductId: Int,
    val quantity: Int = 1
)

@Serializable
data class RedeemResponse(
    val message: String,
    val redemption: RedemptionDto,
    val remainingPoints: Int
)

@Serializable
data class RedemptionDto(
    val id: Int,
    val userId: Int,
    val rewardProductId: Int,
    val rewardProduct: RewardProductDto,
    val pointsUsed: Int,
    val quantity: Int,
    val status: String, // "pending", "approved", "redeemed", "cancelled"
    val redemptionCode: String,
    val redeemedAt: String?,
    val expiryDate: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class RewardProductListResponse(
    val products: List<RewardProductDto>,
    val pagination: PaginationInfo,
    val categories: List<String>
)

// Reward tiers
enum class RewardTier(val value: String, val displayName: String, val minPoints: Int) {
    BRONZE("bronze", "Đồng", 0),
    SILVER("silver", "Bạc", 1000),
    GOLD("gold", "Vàng", 5000),
    PLATINUM("platinum", "Bạch Kim", 10000);

    companion object {
        fun fromValue(value: String) = values().find { it.value == value } ?: BRONZE
        fun fromPoints(points: Int) = values().findLast { it.minPoints <= points } ?: BRONZE
    }
}