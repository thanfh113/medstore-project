package org.example.project.data.repositories

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.example.project.data.models.Product
import org.example.project.data.models.ProductCategory
import org.example.project.data.models.ProductCertificate
import org.example.project.data.models.ProductImage
import org.example.project.data.models.RiskClassification
import org.example.project.data.network.ProductCertificatePayload
import org.example.project.data.network.ProductImagePayload
import org.example.project.data.network.UpdateProductRequest
import java.io.File
import kotlin.math.roundToInt

@Serializable
private data class ProductListEnvelope(
    val data: List<BackendProductDto>,
    val pagination: BackendPagination? = null,
    val message: String? = null
)

@Serializable
private data class DataEnvelope<T>(
    val data: T,
    val message: String? = null
)

@Serializable
private data class BackendPagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)

@Serializable
private data class BackendProductImageDto(
    val id: String? = null,
    val url: String,
    val mediaType: String = "IMAGE",
    val publicId: String? = null,
    val sortOrder: Int = 0
)

@Serializable
private data class BackendProductCertificateDto(
    val id: String? = null,
    val type: String = "MOH_LICENSE",
    val name: String,
    val fileUrl: String,
    val fileType: String = "IMAGE",
    val publicId: String? = null,
    val resourceType: String = "image",
    val thumbnailUrl: String? = null,
    val issuer: String? = null,
    val isActive: Boolean = true
)

@Serializable
private data class BackendProductDto(
    val id: String,
    val categoryId: String? = null,
    val name: String,
    val slug: String? = null,
    val shortDescription: String? = null,
    val description: String? = null,
    val brand: String? = null,
    val manufacturer: String? = null,
    val origin: String? = null,
    val sku: String? = null,
    val unit: String = "Cai",
    val price: Double,
    val originalPrice: Double? = null,
    val importPrice: Double? = null,
    val discountPct: Int = 0,
    val rewardPoints: Int = 0,
    val stock: Int = 0,
    val mfgDate: String? = null,
    val expDate: String? = null,
    val inventoryNote: String? = null,
    val registrationNumber: String? = null,
    val riskClassification: String = "A",
    val contactForPrice: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String,
    val images: List<BackendProductImageDto> = emptyList(),
    val certificates: List<BackendProductCertificateDto> = emptyList()
)

@Serializable
private data class BackendCategoryDto(
    val id: String,
    val parentId: String? = null,
    val name: String,
    val slug: String? = null,
    val description: String? = null,
    val iconUrl: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val productTypeDefault: String? = null
)

@Serializable
private data class ProductImageRequestPayload(
    val url: String,
    val mediaType: String = "IMAGE",
    val publicId: String? = null,
    val sortOrder: Int = 0
)

@Serializable
private data class ProductCertificateRequestPayload(
    val type: String = "MOH_LICENSE",
    val name: String,
    val fileUrl: String,
    val fileType: String = "IMAGE",
    val publicId: String? = null,
    val resourceType: String = "image",
    val thumbnailUrl: String? = null,
    val issuer: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class CreateStockRequest(
    val mfgDate: String? = null,  // Format: yyyy-MM-dd
    val expDate: String? = null,  // Format: yyyy-MM-dd
    val quantity: Int,
    val importPrice: Double? = null
)

data class CompleteProductImageDraft(
    val url: String,
    val mediaType: String = "IMAGE",
    val publicId: String? = null,
    val sortOrder: Int = 0
)

data class CompleteProductCertificateDraft(
    val type: String = "MOH_LICENSE",
    val name: String,
    val fileUrl: String,
    val fileType: String = "IMAGE",
    val publicId: String? = null,
    val resourceType: String = "image",
    val thumbnailUrl: String? = null,
    val issuer: String? = null
)

data class UploadedProductAsset(
    val url: String,
    val publicId: String,
    val mediaType: String,
    val resourceType: String
)

data class CompleteProductDraft(
    val categoryId: String,
    val name: String,
    val shortDescription: String? = null,
    val description: String? = null,
    val brand: String? = null,
    val manufacturer: String? = null,
    val origin: String? = null,
    val sku: String? = null,
    val unit: String = "Cái",
    val price: Double,
    val originalPrice: Double? = null,
    val importPrice: Double? = null,
    val stock: Int = 0,
    val mfgDate: String? = null,
    val expDate: String? = null,
    val inventoryNote: String? = null,
    val discountPct: Int = 0,
    val rewardPoints: Int = 0,
    val registrationNumber: String? = null,
    val riskClassification: String = "A",
    val contactForPrice: Boolean = false,
    val images: List<CompleteProductImageDraft> = emptyList(),
    val certificates: List<CompleteProductCertificateDraft> = emptyList()
)

@Serializable
private data class CreateDesktopProductRequest(
    val categoryId: String,
    val name: String,
    val shortDescription: String? = null,
    val description: String? = null,
    val brand: String? = null,
    val manufacturer: String? = null,
    val origin: String? = null,
    val sku: String? = null,
    val unit: String = "Cái",
    val price: Double,
    val originalPrice: Double? = null,
    val importPrice: Double? = null,
    val stock: Int = 0,
    val mfgDate: String? = null,
    val expDate: String? = null,
    val inventoryNote: String? = null,
    val discountPct: Int = 0,
    val rewardPoints: Int = 0,
    val registrationNumber: String? = null,
    val riskClassification: String = "A",
    val contactForPrice: Boolean = false,
    val isActive: Boolean = true,
    val attributes: Map<String, String> = emptyMap(),
    val images: List<ProductImageRequestPayload> = emptyList(),
    val certificates: List<ProductCertificateRequestPayload> = emptyList()
)

@Serializable
private data class BackendUploadResponse(
    val url: String,
    val publicId: String,
    val format: String? = null,
    val resourceType: String = "image",
    val bytes: Int? = null
)

@Serializable
private data class ProductMutationData(
    val id: String
)

@Serializable
private data class ProductMutationEnvelope(
    val data: ProductMutationData? = null,
    val message: String? = null
)

@Serializable
data class ProductDeleteRequestDto(
    val id: String,
    val productId: String,
    val productName: String,
    val status: String,
    val reason: String? = null,
    val requestedByUserId: String,
    val reviewedByUserId: String? = null,
    val reviewedAt: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
private data class ProductDeleteRequestPayload(
    val reason: String? = null
)

@Serializable
private data class AdminReviewDeleteRequestPayload(
    val approve: Boolean
)

@Serializable
private data class AdminEnvelope<T>(
    val data: T,
    val message: String
)

class ProductRepository(
    private val client: HttpClient
) {
    private val baseUrl = "http://localhost:8080/api/v1"
    private val json = Json { ignoreUnknownKeys = true }
    private var authToken: String? = null
    private var authRetryHandler: AuthRetryHandler? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun setAuthRetryHandler(handler: AuthRetryHandler?) {
        authRetryHandler = handler
    }

    suspend fun getAllProducts(): Result<List<Product>> = try {
        val httpResponse = executeAuthorized { token ->
            client.get("$baseUrl/internal/products?limit=200&sortBy=created_at") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (!httpResponse.status.isSuccess()) {
            val raw = httpResponse.bodyAsText()
            return Result.failure(IllegalStateException(extractErrorMessage(raw)))
        }

        val response = httpResponse.body<ProductListEnvelope>()
        val products = response.data.map { it.toDesktopProduct() }
        Result.success(products)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tai danh sach san pham"))
    }

    suspend fun getCategories(): Result<List<ProductCategory>> = try {
        val httpResponse = client.get("$baseUrl/categories")
        if (!httpResponse.status.isSuccess()) {
            val raw = httpResponse.bodyAsText()
            return Result.failure(IllegalStateException(extractErrorMessage(raw)))
        }

        val response = httpResponse.body<DataEnvelope<List<BackendCategoryDto>>>()
        Result.success(response.data.map { it.toDesktopCategory() })
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tai danh muc san pham"))
    }

    suspend fun createProduct(product: Product, newImageFiles: List<File> = emptyList()): Result<Unit> {
        var uploadedImages: List<ProductImage> = emptyList()
        return try {
            val prepared = prepareProductForMutation(product, newImageFiles)
            prepared.fold(
                onSuccess = { preparedMutation ->
                    val finalProduct = preparedMutation.product
                    val tokenUsed = preparedMutation.token
                    val uploaded = preparedMutation.uploadedImages
                    uploadedImages = uploaded

                    val httpResponse = client.post("$baseUrl/products") {
                        header(HttpHeaders.Authorization, "Bearer $tokenUsed")
                        contentType(ContentType.Application.Json)
                        setBody(finalProduct.toCreateRequest())
                    }

                    if (!httpResponse.status.isSuccess()) {
                        cleanupUploadedImages(uploadedImages, tokenUsed)
                        val raw = httpResponse.bodyAsText()
                        Result.failure(IllegalStateException(extractErrorMessage(raw)))
                    } else {
                        Result.success(Unit)
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            authToken?.takeIf { it.isNotBlank() }?.let { cleanupUploadedImages(uploadedImages, it) }
            Result.failure(IllegalStateException(e.message ?: "Khong the tao san pham"))
        }
    }

    suspend fun createCompleteProduct(draft: CompleteProductDraft): Result<String> = try {
        val httpResponse = executeAuthorized { token ->
            client.post("$baseUrl/products") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(draft.toCreateRequest())
            }
        }

        if (!httpResponse.status.isSuccess()) {
            val raw = httpResponse.bodyAsText()
            return Result.failure(IllegalStateException(extractErrorMessage(raw)))
        }

        val response = httpResponse.body<ProductMutationEnvelope>()
        val productId = response.data?.id
            ?: return Result.failure(IllegalStateException("Backend khong tra ve productId moi"))
        Result.success(productId)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tao san pham theo form nhap hang"))
    }

    suspend fun uploadCompleteProductMedia(file: File, mediaType: String): Result<UploadedProductAsset> {
        val uploadType = if (mediaType.uppercase() == "VIDEO") "PRODUCT_VIDEO" else "PRODUCT_IMAGE"
        return uploadAsset(file, uploadType, mediaType.uppercase())
    }

    suspend fun uploadCertificate(file: File): Result<UploadedProductAsset> {
        val mediaType = if (file.extension.equals("pdf", ignoreCase = true)) "PDF" else "IMAGE"
        return uploadAsset(file, "CERTIFICATE", mediaType)
    }

    suspend fun deleteUploadedAsset(publicId: String, resourceType: String): Result<Unit> {
        if (publicId.isBlank()) return Result.success(Unit)

        return try {
            val normalizedResourceType = normalizeCloudinaryResourceType(resourceType)
            val httpResponse = executeAuthorized { token ->
                deleteUploadedAssetWithToken(publicId, normalizedResourceType, token)
            }

            if (!httpResponse.status.isSuccess()) {
                val raw = httpResponse.bodyAsText()
                return Result.failure(IllegalStateException(extractErrorMessage(raw)))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException(e.message ?: "Khong the xoa tep da tai len"))
        }
    }

    suspend fun updateProduct(product: Product, newImageFiles: List<File> = emptyList()): Result<Unit> {
        var uploadedImages: List<ProductImage> = emptyList()
        return try {
            val prepared = prepareProductForMutation(product, newImageFiles)
            prepared.fold(
                onSuccess = { preparedMutation ->
                    val finalProduct = preparedMutation.product
                    val tokenUsed = preparedMutation.token
                    val uploaded = preparedMutation.uploadedImages
                    uploadedImages = uploaded

                    val httpResponse = client.put("$baseUrl/products/${product.id}") {
                        header(HttpHeaders.Authorization, "Bearer $tokenUsed")
                        contentType(ContentType.Application.Json)
                        setBody(finalProduct.toUpdateRequest())
                    }

                    if (!httpResponse.status.isSuccess()) {
                        cleanupUploadedImages(uploadedImages, tokenUsed)
                        val raw = httpResponse.bodyAsText()
                        Result.failure(IllegalStateException(extractErrorMessage(raw)))
                    } else {
                        Result.success(Unit)
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            authToken?.takeIf { it.isNotBlank() }?.let { cleanupUploadedImages(uploadedImages, it) }
            Result.failure(IllegalStateException(e.message ?: "Khong the cap nhat san pham"))
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> = try {
        val httpResponse = executeAuthorized { token ->
            client.delete("$baseUrl/products/$productId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (!httpResponse.status.isSuccess()) {
            val raw = httpResponse.bodyAsText()
            return Result.failure(IllegalStateException(extractErrorMessage(raw)))
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the xoa san pham"))
    }

    suspend fun submitDeleteRequest(productId: String, reason: String?): Result<Unit> = try {
        val httpResponse = executeAuthorized { token ->
            client.post("$baseUrl/products/$productId/delete-request") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(ProductDeleteRequestPayload(reason = reason?.trim()?.ifBlank { null }))
            }
        }

        if (!httpResponse.status.isSuccess()) {
            val raw = httpResponse.bodyAsText()
            return Result.failure(IllegalStateException(extractErrorMessage(raw)))
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the gui yeu cau xoa san pham"))
    }

    suspend fun updateStock(productId: String, stock: CreateStockRequest): Result<String> = try {
        val httpResponse = executeAuthorized { token ->
            client.post("$baseUrl/products/$productId/stock") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(stock)
            }
        }

        if (!httpResponse.status.isSuccess()) {
            val raw = httpResponse.bodyAsText()
            return Result.failure(IllegalStateException(extractErrorMessage(raw)))
        }

        val response = httpResponse.body<DataEnvelope<String>>()
        Result.success(response.data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the cap nhat ton kho"))
    }

    suspend fun getDeleteRequests(status: String? = "PENDING"): Result<List<ProductDeleteRequestDto>> = try {
        val httpResponse = executeAuthorized { token ->
            client.get("$baseUrl/admin/product-delete-requests") {
                header(HttpHeaders.Authorization, "Bearer $token")
                status?.takeIf { it.isNotBlank() }?.let { url.parameters.append("status", it) }
            }
        }

        if (!httpResponse.status.isSuccess()) {
            val raw = httpResponse.bodyAsText()
            return Result.failure(IllegalStateException(extractErrorMessage(raw)))
        }

        val response = httpResponse.body<AdminEnvelope<List<ProductDeleteRequestDto>>>()
        Result.success(response.data)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the tai danh sach yeu cau xoa"))
    }

    suspend fun reviewDeleteRequest(requestId: String, approve: Boolean): Result<Unit> = try {
        val httpResponse = executeAuthorized { token ->
            client.post("$baseUrl/admin/product-delete-requests/$requestId/review") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(AdminReviewDeleteRequestPayload(approve = approve))
            }
        }

        if (!httpResponse.status.isSuccess()) {
            val raw = httpResponse.bodyAsText()
            return Result.failure(IllegalStateException(extractErrorMessage(raw)))
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the duyet yeu cau xoa"))
    }

    suspend fun updateStock(productId: String, newQuantity: Int): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Ton kho dang duoc quan ly o nghiep vu nhap kho rieng"))
    }

    private suspend fun prepareProductForMutation(
        product: Product,
        newImageFiles: List<File>
    ): Result<PreparedProductMutation> {
        val token = authToken?.takeIf { it.isNotBlank() }
            ?: return Result.failure(IllegalStateException("Chua dang nhap de cap nhat san pham"))

        return uploadProductImages(newImageFiles, token).map { uploadedImages ->
            val mergedImages = (product.images + uploadedImages)
                .sortedBy { it.sortOrder }
                .mapIndexed { index, image ->
                    image.copy(sortOrder = index)
                }
            PreparedProductMutation(
                product = product.copy(images = mergedImages),
                uploadedImages = uploadedImages,
                token = token
            )
        }
    }

    private suspend fun uploadAsset(
        file: File,
        uploadType: String,
        mediaType: String
    ): Result<UploadedProductAsset> = try {
        val httpResponse = executeAuthorized { token ->
            client.submitFormWithBinaryData(
                url = "$baseUrl/upload?type=$uploadType",
                formData = formData {
                    append("file", file.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, getContentType(file.extension))
                        append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                    })
                }
            ) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        if (!httpResponse.status.isSuccess()) {
            val raw = httpResponse.bodyAsText()
            return Result.failure(IllegalStateException(extractErrorMessage(raw)))
        }

        val response = httpResponse.body<BackendUploadResponse>()
        Result.success(
                UploadedProductAsset(
                    url = response.url,
                    publicId = response.publicId,
                    mediaType = mediaType,
                    resourceType = response.resourceType
                )
            )
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Khong the upload tep"))
    }

    private suspend fun uploadProductImages(files: List<File>, token: String): Result<List<ProductImage>> {
        if (files.isEmpty()) return Result.success(emptyList())

        val uploadedImages = mutableListOf<ProductImage>()
        return try {
            files.forEach { file ->
                val httpResponse = client.submitFormWithBinaryData(
                    url = "$baseUrl/upload?type=PRODUCT_IMAGE",
                    formData = formData {
                        append("file", file.readBytes(), Headers.build {
                            append(HttpHeaders.ContentType, getContentType(file.extension))
                            append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                        })
                    }
                ) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }

                val finalResponse = if (httpResponse.status == HttpStatusCode.Unauthorized) {
                    val refreshedToken = authRetryHandler?.refreshAccessToken()
                    if (refreshedToken.isNullOrBlank()) {
                        authRetryHandler?.onAuthFailed()
                        httpResponse
                    } else {
                        authToken = refreshedToken
                        client.submitFormWithBinaryData(
                            url = "$baseUrl/upload?type=PRODUCT_IMAGE",
                            formData = formData {
                                append("file", file.readBytes(), Headers.build {
                                    append(HttpHeaders.ContentType, getContentType(file.extension))
                                    append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                                })
                            }
                        ) {
                            header(HttpHeaders.Authorization, "Bearer $refreshedToken")
                        }
                    }
                } else {
                    httpResponse
                }

                if (!finalResponse.status.isSuccess()) {
                    cleanupUploadedImages(uploadedImages, token)
                    val raw = finalResponse.bodyAsText()
                    return Result.failure(IllegalStateException(extractErrorMessage(raw)))
                }

                val response = finalResponse.body<BackendUploadResponse>()
                uploadedImages += ProductImage(
                    url = response.url,
                    publicId = response.publicId,
                    sortOrder = uploadedImages.size
                )
            }

            Result.success(uploadedImages)
        } catch (e: Exception) {
            cleanupUploadedImages(uploadedImages, token)
            Result.failure(IllegalStateException(e.message ?: "Khong the upload anh san pham"))
        }
    }

    private suspend fun cleanupUploadedImages(images: List<ProductImage>, token: String) {
        images.mapNotNull { it.publicId }.forEach { publicId ->
            runCatching {
                deleteUploadedAssetWithToken(publicId, "image", token)
            }
        }
    }

    private suspend fun deleteUploadedAssetWithToken(
        publicId: String,
        resourceType: String,
        token: String
    ): HttpResponse {
        val normalizedResourceType = normalizeCloudinaryResourceType(resourceType)
        return client.delete(
            "$baseUrl/upload?publicId=${publicId.encodeURLParameter()}&resourceType=${normalizedResourceType.encodeURLParameter()}"
        ) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    private fun normalizeCloudinaryResourceType(resourceType: String): String {
        return when {
            resourceType.equals("raw", ignoreCase = true) -> "raw"
            resourceType.equals("video", ignoreCase = true) -> "video"
            else -> "image"
        }
    }

    private fun BackendProductDto.toDesktopProduct(): Product {
        val mappedRiskClassification = RiskClassification.entries.firstOrNull { it.value == riskClassification } ?: RiskClassification.A
        return Product(
            id = id,
            name = name,
            shortDescription = shortDescription,
            description = description.orEmpty(),
            price = price,
            originalPrice = originalPrice,
            importPrice = importPrice,
            rewardPoints = rewardPoints,
            categoryId = categoryId ?: "",
            stockQuantity = stock,
            mfgDate = mfgDate,
            expDate = expDate,
            inventoryNote = inventoryNote,
            manufacturer = manufacturer?.ifBlank { null } ?: brand.orEmpty(),
            brand = brand.orEmpty(),
            origin = origin.orEmpty(),
            sku = sku,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lowStockThreshold = 10,
            unit = unit,
            registrationNumber = registrationNumber,
            riskClassification = mappedRiskClassification,
            contactForPrice = contactForPrice,
            images = images
                .sortedBy { it.sortOrder }
                .map {
                    ProductImage(
                        id = it.id,
                        url = it.url,
                        publicId = it.publicId,
                        sortOrder = it.sortOrder
                    )
                },
            certificates = certificates
                .filter { it.isActive }
                .map {
                    ProductCertificate(
                        id = it.id,
                        type = it.type.ifBlank { "MOH_LICENSE" },
                        name = it.name,
                        fileUrl = it.fileUrl,
                        fileType = it.fileType.ifBlank { "IMAGE" },
                        publicId = it.publicId,
                        resourceType = it.resourceType.ifBlank { "image" },
                        thumbnailUrl = it.thumbnailUrl,
                        issuer = it.issuer,
                        isActive = it.isActive
                    )
                }
        )
    }

    private fun BackendCategoryDto.toDesktopCategory(): ProductCategory {
        return ProductCategory(
            id = id,
            name = slug ?: name.lowercase().replace(' ', '-'),
            displayName = name,
            parentId = parentId,
            icon = iconUrl,
            sortOrder = sortOrder
        )
    }

    private fun Product.toCreateRequest(): CreateDesktopProductRequest {
        return CreateDesktopProductRequest(
            categoryId = categoryId,
            name = name,
            shortDescription = shortDescription?.ifBlank { null }
                ?: description.take(160).ifBlank { null },
            description = description.ifBlank { null },
            brand = brand.ifBlank { manufacturer.ifBlank { null } },
            manufacturer = manufacturer.ifBlank { null },
            origin = origin.ifBlank { null },
            sku = sku?.ifBlank { null },
            unit = unit.ifBlank { "Cái" },
            price = price,
            originalPrice = originalPrice,
            importPrice = importPrice,
            stock = stockQuantity,
            mfgDate = mfgDate,
            expDate = expDate,
            inventoryNote = inventoryNote,
            discountPct = calculateDiscountPercent(originalPrice, price),
            rewardPoints = rewardPoints,
            registrationNumber = registrationNumber?.ifBlank { null },
            riskClassification = riskClassification.value,
            contactForPrice = contactForPrice,
            isActive = isActive,
            attributes = emptyMap(),
            images = images.toRequestPayload(),
            certificates = certificates.toCertificateRequestPayload()
        )
    }

    private fun Product.toUpdateRequest(): UpdateProductRequest {
        return UpdateProductRequest(
            categoryId = categoryId,
            name = name,
            shortDescription = shortDescription?.ifBlank { null },
            description = description.ifBlank { null },
            brand = brand.ifBlank { manufacturer.ifBlank { null } },
            manufacturer = manufacturer.ifBlank { null },
            origin = origin.ifBlank { null },
            sku = sku?.ifBlank { null },
            unit = unit.ifBlank { "Cái" },
            price = price,
            originalPrice = originalPrice,
            importPrice = importPrice,
            stock = stockQuantity,
            mfgDate = mfgDate,
            expDate = expDate,
            inventoryNote = inventoryNote,
            discountPct = calculateDiscountPercent(originalPrice, price),
            rewardPoints = rewardPoints,
            registrationNumber = registrationNumber?.ifBlank { null },
            riskClassification = riskClassification.value,
            contactForPrice = contactForPrice,
            isActive = isActive,
            attributes = emptyMap(),
            images = images.toUpdateImagePayload(),
            certificates = certificates.toUpdateCertificatePayload()
        )
    }

    private fun calculateDiscountPercent(originalPrice: Double?, price: Double): Int {
        val original = originalPrice ?: return 0
        if (original <= 0.0 || price <= 0.0 || original <= price) return 0
        return (((original - price) / original) * 100.0)
            .roundToInt()
            .coerceIn(0, 99)
    }

    private fun List<ProductImage>.toRequestPayload(): List<ProductImageRequestPayload> {
        return sortedBy { it.sortOrder }.mapIndexed { index, image ->
            ProductImageRequestPayload(
                url = image.url,
                mediaType = "IMAGE",
                publicId = image.publicId,
                sortOrder = index
            )
        }
    }

    private fun List<ProductCertificate>.toCertificateRequestPayload(): List<ProductCertificateRequestPayload> {
        return filter { it.isActive && it.name.isNotBlank() && it.fileUrl.isNotBlank() }
            .map {
                ProductCertificateRequestPayload(
                    type = it.type.ifBlank { "MOH_LICENSE" },
                    name = it.name,
                    fileUrl = it.fileUrl,
                    fileType = it.fileType.ifBlank { "IMAGE" },
                    publicId = it.publicId,
                    resourceType = it.resourceType.ifBlank { "image" },
                    thumbnailUrl = it.thumbnailUrl?.ifBlank { null },
                    issuer = it.issuer?.ifBlank { null },
                    isActive = it.isActive
                )
            }
    }

    private fun CompleteProductDraft.toCreateRequest(): CreateDesktopProductRequest {
        return CreateDesktopProductRequest(
            categoryId = categoryId,
            name = name,
            shortDescription = shortDescription?.ifBlank { null },
            description = description?.ifBlank { null },
            brand = brand?.ifBlank { null },
            manufacturer = manufacturer?.ifBlank { null },
            origin = origin?.ifBlank { null },
            sku = sku?.ifBlank { null },
            unit = unit.ifBlank { "Cái" },
            price = price,
            originalPrice = originalPrice,
            importPrice = importPrice,
            stock = stock,
            mfgDate = mfgDate,
            expDate = expDate,
            inventoryNote = inventoryNote,
            discountPct = calculateDiscountPercent(originalPrice, price),
            rewardPoints = rewardPoints,
            registrationNumber = registrationNumber?.ifBlank { null },
            riskClassification = riskClassification,
            contactForPrice = contactForPrice,
            isActive = true,
            attributes = emptyMap(),
            images = images
                .filter { it.url.isNotBlank() }
                .sortedBy { it.sortOrder }
                .mapIndexed { index, image ->
                    ProductImageRequestPayload(
                        url = image.url,
                        mediaType = image.mediaType.ifBlank { "IMAGE" },
                        publicId = image.publicId,
                        sortOrder = index
                    )
                },
            certificates = certificates
                .filter { it.name.isNotBlank() && it.fileUrl.isNotBlank() }
                .map {
                    ProductCertificateRequestPayload(
                        type = it.type.ifBlank { "MOH_LICENSE" },
                        name = it.name,
                        fileUrl = it.fileUrl,
                        fileType = it.fileType.ifBlank { "IMAGE" },
                        publicId = it.publicId,
                        resourceType = it.resourceType.ifBlank { "image" },
                        thumbnailUrl = it.thumbnailUrl?.ifBlank { null },
                        issuer = it.issuer?.ifBlank { null }
                    )
                }
        )
    }

    private fun List<ProductImage>.toUpdateImagePayload(): List<ProductImagePayload> {
        return sortedBy { it.sortOrder }.mapIndexed { index, image ->
            ProductImagePayload(
                url = image.url,
                publicId = image.publicId,
                sortOrder = index
            )
        }
    }

    private fun List<ProductCertificate>.toUpdateCertificatePayload(): List<ProductCertificatePayload> {
        return filter { it.isActive && it.name.isNotBlank() && it.fileUrl.isNotBlank() }
            .map {
                ProductCertificatePayload(
                    type = it.type.ifBlank { "MOH_LICENSE" },
                    name = it.name,
                    fileUrl = it.fileUrl,
                    fileType = it.fileType.ifBlank { "IMAGE" },
                    publicId = it.publicId,
                    resourceType = it.resourceType.ifBlank { "image" },
                    thumbnailUrl = it.thumbnailUrl?.ifBlank { null },
                    issuer = it.issuer?.ifBlank { null },
                    isActive = it.isActive
                )
            }
    }

    private fun getContentType(extension: String): String {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "heic" -> "image/heic"
            "pdf" -> "application/pdf"
            "mp4" -> "video/mp4"
            "mov" -> "video/quicktime"
            "avi" -> "video/x-msvideo"
            "webm" -> "video/webm"
            else -> "application/octet-stream"
        }
    }

    private fun extractErrorMessage(raw: String): String {
        return runCatching {
            val element = json.parseToJsonElement(raw).jsonObject
            element["error"]?.jsonPrimitive?.content
                ?: element["message"]?.jsonPrimitive?.content
                ?: raw
        }.getOrElse { raw.ifBlank { "Khong the xu ly yeu cau san pham" } }
    }

    private suspend fun executeAuthorized(
        request: suspend (String) -> HttpResponse
    ): HttpResponse {
        val firstToken = authToken?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Chua dang nhap de thuc hien thao tac")

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

    private data class PreparedProductMutation(
        val product: Product,
        val uploadedImages: List<ProductImage>,
        val token: String
    )

}
