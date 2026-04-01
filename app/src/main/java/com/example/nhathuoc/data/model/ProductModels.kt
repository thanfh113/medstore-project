package com.example.nhathuoc.data.model

import kotlinx.serialization.Serializable

/**
 * Product related data transfer objects
 * Synchronized with Backend Routes.kt product endpoints
 */

@Serializable
data class ProductDto(
    val id: String, // UUID from backend
    val shopId: String, // UUID from backend - Added for shop relationship
    val name: String,
    val slug: String? = null, // Added for SEO friendly URLs
    val brand: String,
    val origin: String,
    val price: Double, // Changed from String to numeric
    val originalPrice: Double?,
    val discountPct: Int, // Changed from discountPercent to match DB field discount_pct
    val unit: String,
    val sku: String? = null, // Added for inventory management
    val description: String?,
    val ingredients: String?, // TODO: Move to dynamic attributes
    val indication: String?, // TODO: Move to dynamic attributes
    val contraindication: String?, // TODO: Move to dynamic attributes
    val sideEffects: String?, // TODO: Move to dynamic attributes
    val dosage: String?, // TODO: Move to dynamic attributes
    val storage: String?, // TODO: Move to dynamic attributes
    val rewardPoints: Int,
    val icon: String?,
    val iconTint: String?,
    val iconBg: String?,
    val imageUrl: String?,
    val category: CategoryDto,
    val attributes: Map<String, String> = emptyMap(), // Dynamic attributes
    val productType: String = "MEDICINE", // Added: MEDICINE, SUPPLEMENT, DEVICE, etc.
    val registrationNumber: String? = null, // Added for regulatory compliance
    val isPrescription: Boolean = false, // Added for prescription requirement
    val requiresConsultation: Boolean = false, // Added for consultation requirement
    val isFlashSale: Boolean = false,
    val flashSaleEnd: String? = null, // Added for flash sale end time
    val isBestSeller: Boolean = false,
    val stock: Int,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null // Added for soft delete
)

@Serializable
data class CategoryDto(
    val id: String, // UUID from backend
    val parentId: String? = null, // Added for category hierarchy
    val name: String,
    val slug: String,
    val description: String?,
    val productTypeDefault: String? = null, // Added for default product type
    val iconUrl: String? = null, // Keep only iconUrl to match database icon_url field
    val sortOrder: Int = 0, // Added for display order
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null // Added for soft delete
)

@Serializable
data class ProductCertificateDto(
    val id: String, // UUID from backend
    val productId: String, // UUID from backend
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

// Dynamic product attributes support
@Serializable
data class CategoryAttributeDto(
    val id: String, // UUID from backend
    val key: String, // Field key like "ingredients", "dosage"
    val label: String, // Display name like "Thành phần", "Liều dùng"
    val dataType: String, // "text", "textarea", "number", "select", "multiselect", "boolean", "date"
    val required: Boolean,
    val options: List<String>? = null, // For select/multiselect types
    val unit: String? = null, // For number types like "mg", "ml"
    val sortOrder: Int // Display order in form
)

@Serializable
data class CategoryAttributesResponse(
    val attributes: List<CategoryAttributeDto>
)

// Product Images model based on product_images table
@Serializable
data class ProductImageDto(
    val id: String, // UUID from backend
    val productId: String, // UUID from backend
    val imageUrl: String,
    val altText: String? = null,
    val sortOrder: Int = 0,
    val isPrimary: Boolean = false,
    val createdAt: String
)

// Product Batches model based on product_batches table
@Serializable
data class ProductBatchDto(
    val id: String, // UUID from backend
    val productId: String, // UUID from backend
    val lotNumber: String? = null,
    val mfgDate: String? = null, // Manufacturing date
    val expDate: String? = null, // Expiry date
    val quantity: Int,
    val importPrice: Double? = null,
    val supplier: String? = null,
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String
)

// Product with images and batches for detailed view
@Serializable
data class ProductDetailWithExtrasResponse(
    val product: ProductDto,
    val images: List<ProductImageDto>,
    val batches: List<ProductBatchDto>,
    val certificates: List<ProductCertificateDto>,
    val relatedProducts: List<ProductDto>
)