package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import com.example.nhathuoc.data.remote.RetrofitClient
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository for reward operations
 * Handles reward account, reward products, and redemption
 */
class RewardRepository {

    private val apiService: ApiService by lazy {
        RetrofitClient.createService<ApiService>()
    }

    // Get reward account info
    suspend fun getRewardAccount(): NetworkResult<RewardAccountDto> {
        return try {
            val response = apiService.getRewardAccount()

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

    // Get reward products available for redemption
    suspend fun getRewardProducts(
        category: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): NetworkResult<RewardProductListResponse> {
        return try {
            val response = apiService.getRewardProducts(
                category = category,
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

    // Redeem reward product
    suspend fun redeem(request: RedeemRequest): NetworkResult<RedeemResponse> {
        return try {
            val response = apiService.redeem(request)

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
