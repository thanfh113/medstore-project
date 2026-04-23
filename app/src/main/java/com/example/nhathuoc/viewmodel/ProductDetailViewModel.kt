package com.example.nhathuoc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.model.ProductCertificateDto
import com.example.nhathuoc.data.model.ProductDto
import com.example.nhathuoc.data.model.ProductImageDto
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository
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
}
