package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable

/**
 * Reward system related data transfer objects
 */

@Serializable
data class RewardAccountDto(
    val id: String, // UUID from backend
    val userId: String, // UUID from backend
    val totalPoints: Int,
    val availablePoints: Int,
    val usedPoints: Int,
    val tier: String = "BRONZE", // "BRONZE", "SILVER", "GOLD", "PLATINUM"
    val tierBenefits: List<String> = emptyList(), // Keep for client logic
    val nextTierPoints: Int? = null, // Keep for client logic
    val pointsHistory: List<PointTransactionDto> = emptyList(), // Keep for extended response
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class PointTransactionDto(
    val id: String, // UUID from backend
    val accountId: String? = null, // UUID from backend - Match schema field name
    val userId: String? = null, // UUID from backend - Keep for backward compatibility
    val type: String, // "EARNED", "USED", "EXPIRED"
    val points: Int,
    val description: String? = null,
    val orderId: String? = null, // UUID from backend
    val redemptionId: String? = null, // UUID from backend - Match schema field name
    val redeemId: String? = null, // Keep for backward compatibility
    val expiryDate: String? = null,
    val createdAt: String
)

@Serializable
data class RewardProductDto(
    val id: String, // UUID from backend
    val name: String,
    val description: String? = null, // Keep for extended info
    val imageUrl: String? = null, // Match schema field name
    val pointCost: Int, // Match schema field name
    val pointsRequired: Int? = null, // Keep for backward compatibility
    val priceText: String? = null, // Match schema field name
    val category: String? = null, // Keep for client categorization
    val value: String? = null, // Keep for display
    val termsConditions: String? = null, // Keep for detailed info
    val validFrom: String? = null, // Keep for validity period
    val validTo: String? = null, // Keep for validity period
    val stock: Int = 0,
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class RedeemRequest(
    val rewardProductId: String, // UUID from backend
    val quantity: Int = 1
)

@Serializable
data class RedeemResponse(
    val message: String,
    val redemption: RedemptionDto,
    val remainingPoints: Int
)

@Serializable
data class RedeemRewardResultDto(
    val redemptionId: String
)

@Serializable
data class RewardRedemptionHistoryDto(
    val id: String,
    val userId: String,
    val rewardProductId: String,
    val productName: String,
    val quantity: Int,
    val pointsUsed: Int,
    val status: String,
    val createdAt: String
)

@Serializable
data class RedemptionDto(
    val id: String, // UUID from backend
    val accountId: String? = null, // UUID from backend - Match schema
    val userId: String? = null, // UUID from backend - Keep for backward compatibility
    val productId: String, // UUID from backend - Match schema field name
    val rewardProductId: String? = null, // Keep for backward compatibility
    val rewardProduct: RewardProductDto? = null, // May be populated in response
    val pointsUsed: Int,
    val quantity: Int,
    val status: String, // "PENDING", "APPROVED", "REDEEMED", "CANCELLED"
    val redemptionCode: String,
    val redeemedAt: String? = null,
    val expiryDate: String? = null,
    val createdAt: String,
    val updatedAt: String? = null // Keep for compatibility
)

@Serializable
data class RewardProductListResponse(
    val products: List<RewardProductDto>,
    val pagination: PaginationInfo,
    val categories: List<String>
)

// Points-based reward tiers (different from spending-based RewardTier in Enums.kt)
enum class RewardPointsTier(val value: String, val displayName: String, val minPoints: Int) {
    BRONZE("BRONZE", "Đồng", 0),
    SILVER("SILVER", "Bạc", 1000),
    GOLD("GOLD", "Vàng", 5000),
    PLATINUM("PLATINUM", "Bạch Kim", 10000)
}

// Utility functions for RewardPointsTier
fun findRewardTierByValue(value: String): RewardPointsTier {
    return RewardPointsTier.entries.find { it.value == value } ?: RewardPointsTier.BRONZE
}

fun findRewardTierByPoints(points: Int): RewardPointsTier {
    return RewardPointsTier.entries.findLast { it.minPoints <= points } ?: RewardPointsTier.BRONZE
}

