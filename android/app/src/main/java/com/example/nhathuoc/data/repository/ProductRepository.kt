package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val apiService: ApiService
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getProducts(
        category: String? = null,
        brand: String? = null,
        search: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sortBy: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): NetworkResult<ProductListResponse> {
        return try {
            val response = apiService.getProducts(category, brand, search, minPrice, maxPrice, sortBy, page, limit)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getFlashSaleProducts(): NetworkResult<FlashSaleResponse> {
        return try {
            val response = apiService.getFlashSaleProducts()
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getBestSellers(period: String = "week"): NetworkResult<BestSellersResponse> {
        return try {
            val response = apiService.getBestSellers(period)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getProductById(productId: String): NetworkResult<ProductDetailResponse> {
        return try {
            val response = apiService.getProductById(productId)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getProductCertificates(productId: String): NetworkResult<List<ProductCertificateDto>> {
        return try {
            val response = apiService.getProductCertificates(productId)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getProductReviews(productId: String): NetworkResult<ProductReviewsResponse> {
        return try {
            val response = apiService.getProductReviews(productId)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun createProductReview(productId: String, request: CreateReviewRequest): NetworkResult<ReviewDto> {
        return try {
            val response = apiService.createProductReview(productId, request)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun reportReview(reviewId: String, request: ReportReviewRequest): NetworkResult<ReviewIdResponse> {
        return try {
            val response = apiService.reportReview(reviewId, request)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        val fallback = "Có lỗi xảy ra, vui lòng thử lại"
        val body = errorBody?.trim().orEmpty()
        if (body.isBlank()) return fallback

        return try {
            val obj = json.parseToJsonElement(body).jsonObject
            obj["message"]?.jsonPrimitive?.contentOrNull
                ?: obj["error"]?.jsonPrimitive?.contentOrNull
                ?: body
        } catch (e: Exception) {
            body
        }
    }
}
