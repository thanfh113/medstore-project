package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable

/**
 * Product related data transfer objects
 * Synchronized with Backend Routes.kt product endpoints
 */

@Serializable
data class ProductDto(
    val id: Int,
    val name: String,
    val brand: String,
    val origin: String,
    val price: String, // "123.000đ" format in BE
    val originalPrice: String?,
    val discountPercent: Int,
    val unit: String,
    val description: String?,
    val ingredients: String?,
    val indication: String?,
    val contraindication: String?,
    val sideEffects: String?,
    val dosage: String?,
    val storage: String?,
    val rewardPoints: Int,
    val icon: String?,
    val iconTint: String?,
    val iconBg: String?,
    val imageUrl: String?,
    val category: CategoryDto,
    val isFlashSale: Boolean = false,
    val isBestSeller: Boolean = false,
    val stock: Int,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CategoryDto(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String?,
    val icon: String?,
    val iconTint: String?,
    val iconBg: String?,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ProductCertificateDto(
    val id: Int,
    val productId: Int,
    val name: String,
    val issuer: String,
    val certificateNumber: String,
    val issueDate: String,
    val expiryDate: String?,
    val documentUrl: String?,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ProductListResponse(
    val products: List<ProductDto>,
    val pagination: PaginationInfo,
    val filters: ProductFilters?
)

@Serializable
data class ProductFilters(
    val category: String?,
    val brand: String?,
    val minPrice: Double?,
    val maxPrice: Double?,
    val sortBy: String?, // "price_asc", "price_desc", "name", "created_at"
    val page: Int = 1,
    val limit: Int = 20
)

@Serializable
data class FlashSaleResponse(
    val products: List<ProductDto>,
    val startTime: String,
    val endTime: String,
    val timeRemaining: Long? // seconds
)

@Serializable
data class BestSellersResponse(
    val products: List<ProductDto>,
    val period: String // "today", "week", "month"
)

@Serializable
data class ProductDetailResponse(
    val product: ProductDto,
    val certificates: List<ProductCertificateDto>,
    val relatedProducts: List<ProductDto>
)