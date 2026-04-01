package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import com.example.nhathuoc.data.remote.RetrofitClient
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository for product operations
 * Handles product listing, search, details, flash sales, etc.
 */
class ProductRepository {

    private val apiService: ApiService by lazy {
        RetrofitClient.createService<ApiService>()
    }

    // Get products with filters
    suspend fun getProducts(
        category: String? = null,
        brand: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sortBy: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): NetworkResult<ProductListResponse> {
        return try {
            val response = apiService.getProducts(
                category = category,
                brand = brand,
                minPrice = minPrice,
                maxPrice = maxPrice,
                sortBy = sortBy,
                page = page,
                limit = limit
            )

            if (response.isSuccessful) {
                NetworkResult.Success(response.body()!!)
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                NetworkResult.Error(response.code(), errorMessage)
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    // Get flash sale products
    suspend fun getFlashSaleProducts(): NetworkResult<FlashSaleResponse> {
        return try {
            val response = apiService.getFlashSaleProducts()

            if (response.isSuccessful) {
                NetworkResult.Success(response.body()!!)
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                NetworkResult.Error(response.code(), errorMessage)
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    // Get best seller products
    suspend fun getBestSellers(period: String = "week"): NetworkResult<BestSellersResponse> {
        return try {
            val response = apiService.getBestSellers(period)

            if (response.isSuccessful) {
                NetworkResult.Success(response.body()!!)
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                NetworkResult.Error(response.code(), errorMessage)
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    // Get product details by ID
    suspend fun getProductById(productId: String): NetworkResult<ProductDetailResponse> {
        return try {
            val response = apiService.getProductById(productId)

            if (response.isSuccessful) {
                NetworkResult.Success(response.body()!!)
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                NetworkResult.Error(response.code(), errorMessage)
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    // Get product certificates
    suspend fun getProductCertificates(productId: String): NetworkResult<List<ProductCertificateDto>> {
        return try {
            val response = apiService.getProductCertificates(productId)

            if (response.isSuccessful) {
                NetworkResult.Success(response.body()!!)
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                NetworkResult.Error(response.code(), errorMessage)
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    // Helper function to parse error messages
    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            if (errorBody != null) {
                kotlinx.serialization.json.Json.decodeFromString<ApiError>(errorBody).message
            } else {
                "Có lỗi xảy ra, vui lòng thử lại"
            }
        } catch (e: Exception) {
            errorBody ?: "Có lỗi xảy ra, vui lòng thử lại"
        }
    }
}