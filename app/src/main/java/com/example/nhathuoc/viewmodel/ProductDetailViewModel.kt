package com.example.nhathuoc.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.model.ProductCertificateDto
import com.example.nhathuoc.data.model.ProductDto
import com.example.nhathuoc.data.model.ProductImageDto
import com.example.nhathuoc.data.model.ProductReviewSummaryDto
import com.example.nhathuoc.data.model.CreateReviewRequest
import com.example.nhathuoc.data.model.ReportReviewRequest
import com.example.nhathuoc.data.model.ReviewAttachmentInput
import com.example.nhathuoc.data.model.ReviewDto
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.data.repository.FileUploadRepository
import com.example.nhathuoc.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val fileUploadRepository: FileUploadRepository
) : ViewModel() {

    private val _productState = MutableStateFlow<UiState<ProductDto>>(UiState.Idle)
    val productState: StateFlow<UiState<ProductDto>> = _productState.asStateFlow()

    private val _product = MutableStateFlow<ProductDto?>(null)
    val product: StateFlow<ProductDto?> = _product.asStateFlow()

    private val _certificates = MutableStateFlow<List<ProductCertificateDto>>(emptyList())
    val certificates: StateFlow<List<ProductCertificateDto>> = _certificates.asStateFlow()

    private val _images = MutableStateFlow<List<ProductImageDto>>(emptyList())
    val images: StateFlow<List<ProductImageDto>> = _images.asStateFlow()

    private val _relatedProducts = MutableStateFlow<List<ProductDto>>(emptyList())
    val relatedProducts: StateFlow<List<ProductDto>> = _relatedProducts.asStateFlow()

    private val _reviewSummary = MutableStateFlow<ProductReviewSummaryDto?>(null)
    val reviewSummary: StateFlow<ProductReviewSummaryDto?> = _reviewSummary.asStateFlow()

    private val _reviews = MutableStateFlow<List<ReviewDto>>(emptyList())
    val reviews: StateFlow<List<ReviewDto>> = _reviews.asStateFlow()

    private val _reviewSubmitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val reviewSubmitState: StateFlow<UiState<Unit>> = _reviewSubmitState.asStateFlow()

    private val _reviewFeedbackMessage = MutableStateFlow<String?>(null)
    val reviewFeedbackMessage: StateFlow<String?> = _reviewFeedbackMessage.asStateFlow()

    fun loadProduct(productId: String) {
        if (productId.startsWith("mock-")) return  // Mock products don't use API
        viewModelScope.launch {
            _productState.value = UiState.Loading
            when (val result = productRepository.getProductById(productId)) {
                is NetworkResult.Success -> {
                    _product.value = result.data.product
                    _images.value = result.data.images
                    _certificates.value = result.data.certificates
                    _relatedProducts.value = result.data.relatedProducts
                    _productState.value = UiState.Success(result.data.product)
                    loadReviews(productId)
                }
                is NetworkResult.Error -> {
                    _images.value = emptyList()
                    _productState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _images.value = emptyList()
                    _productState.value = UiState.Error(
                        result.e.message ?: "Không thể tải thông tin sản phẩm"
                    )
                }
            }
        }
    }

    fun retry(productId: String) = loadProduct(productId)

    fun loadReviews(productId: String) {
        if (productId.startsWith("mock-")) return
        viewModelScope.launch {
            when (val result = productRepository.getProductReviews(productId)) {
                is NetworkResult.Success -> {
                    _reviewSummary.value = result.data.summary
                    _reviews.value = result.data.reviews
                }
                is NetworkResult.Error -> Unit
                is NetworkResult.Exception -> Unit
            }
        }
    }

    fun submitReview(
        productId: String,
        rating: Int,
        title: String?,
        comment: String?,
        attachmentUris: List<Uri> = emptyList()
    ) {
        if (productId.startsWith("mock-")) return
        viewModelScope.launch {
            _reviewSubmitState.value = UiState.Loading
            _reviewFeedbackMessage.value = null
            val attachments = if (attachmentUris.isNotEmpty()) {
                when (val uploadResult = fileUploadRepository.uploadEvidenceFiles(attachmentUris)) {
                    is NetworkResult.Success -> uploadResult.data.mapIndexed { index, file ->
                        ReviewAttachmentInput(
                            fileUrl = file.fileUrl,
                            fileType = file.fileType,
                            publicId = file.publicId,
                            sortOrder = index
                        )
                    }
                    is NetworkResult.Error -> {
                        _reviewSubmitState.value = UiState.Error(uploadResult.message)
                        return@launch
                    }
                    is NetworkResult.Exception -> {
                        _reviewSubmitState.value = UiState.Error(uploadResult.e.message ?: "Khong the upload file")
                        return@launch
                    }
                }
            } else {
                emptyList()
            }
            val request = CreateReviewRequest(
                rating = rating.coerceIn(1, 5),
                title = title?.trim()?.ifBlank { null },
                comment = comment?.trim()?.ifBlank { null },
                attachments = attachments
            )
            when (val result = productRepository.createProductReview(productId, request)) {
                is NetworkResult.Success -> {
                    _reviewSubmitState.value = UiState.Success(Unit)
                    _reviewFeedbackMessage.value = "Đã gửi đánh giá"
                    loadReviews(productId)
                }
                is NetworkResult.Error -> {
                    _reviewSubmitState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _reviewSubmitState.value = UiState.Error(
                        result.e.message ?: "Không thể gửi đánh giá"
                    )
                }
            }
        }
    }

    fun reportReview(reviewId: String) {
        viewModelScope.launch {
            _reviewSubmitState.value = UiState.Loading
            _reviewFeedbackMessage.value = null
            val request = ReportReviewRequest(
                reason = "OTHER",
                note = "Người dùng báo cáo đánh giá từ ứng dụng Android"
            )
            when (val result = productRepository.reportReview(reviewId, request)) {
                is NetworkResult.Success -> {
                    _reviewSubmitState.value = UiState.Success(Unit)
                    _reviewFeedbackMessage.value = "Đã gửi báo cáo đánh giá"
                }
                is NetworkResult.Error -> {
                    _reviewSubmitState.value = UiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _reviewSubmitState.value = UiState.Error(
                        result.e.message ?: "Không thể báo cáo đánh giá"
                    )
                }
            }
        }
    }

    fun clearReviewSubmitState() {
        _reviewSubmitState.value = UiState.Idle
        _reviewFeedbackMessage.value = null
    }
}
