package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val apiService: ApiService
) {
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
            val response = apiService.getProducts(category, brand, minPrice, maxPrice, sortBy, page, limit)
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

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            if (errorBody != null) kotlinx.serialization.json.Json.decodeFromString<ApiError>(errorBody).message else "Co loi xay ra, vui long thu lai"
        } catch (e: Exception) {
            errorBody ?: "Co loi xay ra, vui long thu lai"
        }
    }
}
