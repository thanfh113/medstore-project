package org.example.project.presentation.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.models.Product
import org.example.project.data.models.ProductCategory
import org.example.project.data.models.RiskClassification
import org.example.project.data.models.productCategoryMatches
import org.example.project.data.repositories.CreateStockRequest
import org.example.project.data.repositories.CompleteProductCertificateDraft
import org.example.project.data.repositories.CompleteProductDraft
import org.example.project.data.repositories.CompleteProductImageDraft
import org.example.project.data.repositories.ProductDeleteRequestDto
import org.example.project.data.repositories.ProductRepository
import org.example.project.data.repositories.UploadedProductAsset
import org.example.project.ui.components.CompleteProductFormData
import java.io.File

class ProductsViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    fun refreshData() {
        loadCategories()
        loadProducts()
        if (_uiState.value.userRole == "ADMIN") {
            loadDeleteRequests()
        }
    }

    fun setUserRole(role: String) {
        val normalized = role.uppercase()
        _uiState.update { it.copy(userRole = normalized) }
        if (normalized == "ADMIN") {
            loadDeleteRequests()
        }
    }

    fun loadProducts() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            productRepository.getAllProducts().fold(
                onSuccess = { products ->
                    _uiState.update { state ->
                        state.copy(
                            products = products,
                            filteredProducts = applyFilters(
                                products = products,
                                query = state.searchQuery,
                                categoryId = state.selectedCategory,
                                sortType = state.sortType
                            ),
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            error = error.message ?: "Khong the tai danh sach san pham",
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    fun loadCategories() {
        scope.launch {
            productRepository.getCategories().fold(
                onSuccess = { categories ->
                    _uiState.update { it.copy(categories = categories) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message ?: "Khong the tai danh muc san pham") }
                }
            )
        }
    }

    fun searchProducts(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredProducts = applyFilters(
                    products = state.products,
                    query = query,
                    categoryId = state.selectedCategory,
                    sortType = state.sortType
                )
            )
        }
    }

    fun filterByCategory(categoryId: String?) {
        _uiState.update { state ->
            state.copy(
                selectedCategory = categoryId,
                filteredProducts = applyFilters(
                    products = state.products,
                    query = state.searchQuery,
                    categoryId = categoryId,
                    sortType = state.sortType
                )
            )
        }
    }

    fun sortProducts(sortType: ProductSortType) {
        _uiState.update { state ->
            state.copy(
                sortType = sortType,
                filteredProducts = applyFilters(
                    products = state.products,
                    query = state.searchQuery,
                    categoryId = state.selectedCategory,
                    sortType = sortType
                )
            )
        }
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true, selectedProduct = null, error = null) }
    }

    fun hideCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false, selectedProduct = null) }
    }

    fun selectProduct(product: Product) {
        _uiState.update { it.copy(selectedProduct = product, showCreateDialog = true, error = null) }
    }

    fun createProduct(product: Product, newImageFiles: List<File> = emptyList()) {
        scope.launch {
            _uiState.update { it.copy(isCreating = true, error = null) }

            productRepository.createProduct(product, newImageFiles).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            showCreateDialog = false,
                            selectedProduct = null,
                            isCreating = false,
                            successMessage = "Da tao san pham thanh cong"
                        )
                    }
                    loadProducts()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            error = error.message ?: "Khong the tao san pham",
                            isCreating = false
                        )
                    }
                }
            )
        }
    }

    fun updateProduct(product: Product, newImageFiles: List<File> = emptyList()) {
        scope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }

            productRepository.updateProduct(product, newImageFiles).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            showCreateDialog = false,
                            selectedProduct = null,
                            isUpdating = false,
                            successMessage = "Da cap nhat san pham thanh cong"
                        )
                    }
                    loadProducts()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            error = error.message ?: "Khong the cap nhat san pham",
                            isUpdating = false
                        )
                    }
                }
            )
        }
    }


    fun deleteProduct(productId: String) {
        scope.launch {
            _uiState.update { it.copy(isDeleting = productId, error = null) }

            if (_uiState.value.userRole != "ADMIN") {
                productRepository.submitDeleteRequest(productId, "Employee requested product delete").fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isDeleting = null,
                                successMessage = "Da gui yeu cau xoa. Cho ADMIN phe duyet"
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                error = error.message ?: "Khong the gui yeu cau xoa",
                                isDeleting = null
                            )
                        }
                    }
                )
                return@launch
            }

            productRepository.deleteProduct(productId).fold(
                onSuccess = {
                    _uiState.update { state ->
                        val updatedProducts = state.products.filterNot { it.id == productId }
                        state.copy(
                            products = updatedProducts,
                            filteredProducts = applyFilters(
                                products = updatedProducts,
                                query = state.searchQuery,
                                categoryId = state.selectedCategory,
                                sortType = state.sortType
                            ),
                            isDeleting = null,
                            successMessage = "Da xoa san pham thanh cong"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            error = error.message ?: "Khong the xoa san pham",
                            isDeleting = null
                        )
                    }
                }
            )
        }
    }

    fun loadDeleteRequests() {
        if (_uiState.value.userRole != "ADMIN") return
        scope.launch {
            _uiState.update { it.copy(isLoadingDeleteRequests = true, error = null) }
            productRepository.getDeleteRequests("PENDING").fold(
                onSuccess = { rows ->
                    _uiState.update {
                        it.copy(
                            isLoadingDeleteRequests = false,
                            pendingDeleteRequests = rows
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingDeleteRequests = false,
                            error = error.message ?: "Khong the tai yeu cau xoa"
                        )
                    }
                }
            )
        }
    }

    fun reviewDeleteRequest(requestId: String, approve: Boolean) {
        if (_uiState.value.userRole != "ADMIN") return
        scope.launch {
            _uiState.update { it.copy(reviewingDeleteRequestId = requestId, error = null) }
            productRepository.reviewDeleteRequest(requestId, approve).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            reviewingDeleteRequestId = null,
                            successMessage = if (approve) "Da duyet xoa san pham" else "Da tu choi yeu cau xoa"
                        )
                    }
                    loadDeleteRequests()
                    loadProducts()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            reviewingDeleteRequestId = null,
                            error = error.message ?: "Khong the xu ly yeu cau xoa"
                        )
                    }
                }
            )
        }
    }

    fun updateStock(productId: String, newQuantity: Int) {
        scope.launch {
            productRepository.updateStock(productId, newQuantity).fold(
                onSuccess = {
                    _uiState.update { it.copy(successMessage = "Da cap nhat ton kho") }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message ?: "Khong the cap nhat ton kho") }
                }
            )
        }
    }

    fun addStockReceipt(
        productId: String,
        mfgDate: String?,
        expDate: String?,
        quantity: Int,
        importPrice: Double?
    ) {
        scope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }

            productRepository.updateStock(
                productId,
                CreateStockRequest(
                    mfgDate = mfgDate?.ifBlank { null },
                    expDate = expDate?.ifBlank { null },
                    quantity = quantity,
                    importPrice = importPrice
                )
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            successMessage = "Da cap nhat ton kho thanh cong"
                        )
                    }
                    loadProducts()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            error = error.message ?: "Khong the cap nhat ton kho",
                            isUpdating = false
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun showCompleteProductForm() {
        _uiState.update { it.copy(showCompleteProductForm = true, error = null) }
    }

    fun hideCompleteProductForm() {
        _uiState.update { it.copy(showCompleteProductForm = false) }
    }

    fun createCompleteProduct(formData: CompleteProductFormData) {
        scope.launch {
            _uiState.update { it.copy(isCreatingCompleteProduct = true, error = null) }

            try {
                val primaryCertificate = formData.certificates.firstOrNull()
                val normalizedRisk = try {
                    RiskClassification.entries.first { it.value == formData.riskClassification }.value
                } catch (e: Exception) {
                    RiskClassification.A.value
                }
                val restrictedOnlineRisk = normalizedRisk == RiskClassification.C.value || normalizedRisk == RiskClassification.D.value
                val draft = CompleteProductDraft(
                    categoryId = formData.categoryId,
                    name = formData.productName,
                    shortDescription = formData.shortDescription.ifBlank { null },
                    description = formData.fullDescription.ifBlank { formData.shortDescription.ifBlank { null } },
                    brand = formData.brand.ifBlank { null },
                    manufacturer = formData.manufacturer.ifBlank { null },
                    origin = formData.origin.ifBlank { null },
                    sku = formData.sku.ifBlank { null },
                    unit = formData.unit,
                    price = formData.price,
                    originalPrice = formData.originalPrice,
                    discountPct = formData.discountPct,
                    rewardPoints = formData.rewardPoints,
                    importPrice = formData.firstStockData?.importPrice?.takeIf { it > 0.0 },
                    stock = formData.firstStockData?.quantityOnHand?.coerceAtLeast(0) ?: 0,
                    mfgDate = formData.firstStockData?.mfgDate?.ifBlank { null },
                    expDate = formData.firstStockData?.expDate?.ifBlank { null },
                    inventoryNote = null,
                    registrationNumber = formData.registrationNumber.ifBlank { null },
                    riskClassification = normalizedRisk,
                    requiresCertification = formData.requiresCertification || restrictedOnlineRisk,
                    requiresConsultation = formData.requiresConsultation || restrictedOnlineRisk,
                    images = formData.productImages.mapIndexed { index, image ->
                        CompleteProductImageDraft(
                            url = image.url,
                            mediaType = image.mediaType.ifBlank { "IMAGE" },
                            sortOrder = index,
                            publicId = image.publicId
                        )
                    },
                    certificates = primaryCertificate
                        ?.takeIf { it.name.isNotBlank() && it.fileUrl.isNotBlank() }
                        ?.let {
                            listOf(
                                CompleteProductCertificateDraft(
                                    type = it.type.ifBlank { "MOH_LICENSE" },
                                    name = it.name,
                                    fileUrl = it.fileUrl,
                                    fileType = it.fileType.ifBlank { "IMAGE" },
                                    publicId = it.publicId,
                                    resourceType = it.resourceType.ifBlank { "image" },
                                    thumbnailUrl = it.thumbnailUrl?.ifBlank { null },
                                    issuer = it.issuer.ifBlank { null }
                                )
                            )
                        }
                        ?: emptyList()
                )

                productRepository.createCompleteProduct(draft).fold(
                    onSuccess = { _ ->
                        _uiState.update {
                            it.copy(
                                isCreatingCompleteProduct = false,
                                showCompleteProductForm = false,
                                successMessage = "Da tao san pham thanh cong"
                            )
                        }

                        loadProducts()
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                error = error.message ?: "Khong the tao san pham",
                                isCreatingCompleteProduct = false
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Loi: ${e.message}",
                        isCreatingCompleteProduct = false
                    )
                }
            }
        }
    }

    suspend fun uploadCompleteProductMedia(file: File, mediaType: String): Result<UploadedProductAsset> {
        return productRepository.uploadCompleteProductMedia(file, mediaType)
    }

    suspend fun uploadCertificate(file: File): Result<UploadedProductAsset> {
        return productRepository.uploadCertificate(file)
    }

    suspend fun deleteUploadedAsset(publicId: String, resourceType: String): Result<Unit> {
        return productRepository.deleteUploadedAsset(publicId, resourceType)
    }

    private fun applyFilters(
        products: List<Product>,
        query: String,
        categoryId: String?,
        sortType: ProductSortType
    ): List<Product> {
        var filtered = products

        if (query.isNotBlank()) {
            filtered = filtered.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true) ||
                    product.manufacturer.contains(query, ignoreCase = true) ||
                    product.origin.contains(query, ignoreCase = true) ||
                    product.sku.orEmpty().contains(query, ignoreCase = true) ||
                    product.registrationNumber.orEmpty().contains(query, ignoreCase = true)
            }
        }

        if (categoryId != null) {
            filtered = filtered.filter { product ->
                productCategoryMatches(
                    productCategoryId = product.categoryId,
                    selectedCategoryId = categoryId,
                    categories = _uiState.value.categories
                )
            }
        }


        return when (sortType) {
            ProductSortType.NAME_ASC -> filtered.sortedBy { it.name }
            ProductSortType.NAME_DESC -> filtered.sortedByDescending { it.name }
            ProductSortType.PRICE_ASC -> filtered.sortedBy { it.price }
            ProductSortType.PRICE_DESC -> filtered.sortedByDescending { it.price }
            ProductSortType.STOCK_ASC -> filtered.sortedBy { it.stockQuantity }
            ProductSortType.STOCK_DESC -> filtered.sortedByDescending { it.stockQuantity }
            ProductSortType.DATE_CREATED -> filtered.sortedByDescending { it.createdAt }
            ProductSortType.DEFAULT -> filtered
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}

data class ProductsUiState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val categories: List<ProductCategory> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: String? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedProduct: Product? = null,
    val showCreateDialog: Boolean = false,
    val showCompleteProductForm: Boolean = false,
    val isCreatingCompleteProduct: Boolean = false,
    val sortType: ProductSortType = ProductSortType.DEFAULT,
    val showOnlyLowStock: Boolean = false,
    val showOnlyActive: Boolean = true,
    val pageSize: Int = 20,
    val currentPage: Int = 0,
    val userRole: String = "EMPLOYEE",
    val pendingDeleteRequests: List<ProductDeleteRequestDto> = emptyList(),
    val isLoadingDeleteRequests: Boolean = false,
    val reviewingDeleteRequestId: String? = null
) {
    val hasProducts get() = products.isNotEmpty()
    val hasFilteredProducts get() = filteredProducts.isNotEmpty()
    val isSearchActive get() = searchQuery.isNotBlank()
    val paginatedProducts get() = filteredProducts.drop(currentPage * pageSize).take(pageSize)
    val totalPages get() = (filteredProducts.size + pageSize - 1) / pageSize
}

enum class ProductSortType(val displayName: String) {
    DEFAULT("Mac dinh"),
    NAME_ASC("Ten A-Z"),
    NAME_DESC("Ten Z-A"),
    PRICE_ASC("Gia thap den cao"),
    PRICE_DESC("Gia cao den thap"),
    STOCK_ASC("Ton kho it nhat"),
    STOCK_DESC("Ton kho nhieu nhat"),
    DATE_CREATED("Moi nhat")
}
