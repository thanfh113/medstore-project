package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getRewardAccount(): NetworkResult<RewardAccountDto> {
        return try {
            val response = apiService.getRewardAccount()
            if (response.isSuccessful) {
                NetworkResult.Success(response.body()!!.data)
            } else {
                NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getRewardProducts(category: String? = null, page: Int = 1, limit: Int = 20): NetworkResult<RewardProductListResponse> {
        return try {
            val response = apiService.getRewardProducts(category, page, limit)
            if (response.isSuccessful) {
                val products = response.body()!!.data
                    .let { items ->
                        category?.takeIf { it.isNotBlank() }?.let { selected ->
                            items.filter { it.category.equals(selected, ignoreCase = true) }
                        } ?: items
                    }
                val totalPages = if (products.isEmpty()) 0 else ((products.size + limit - 1) / limit).coerceAtLeast(1)
                NetworkResult.Success(
                    RewardProductListResponse(
                        products = products,
                        pagination = PaginationInfo(
                            page = page,
                            limit = limit,
                            total = products.size,
                            totalPages = totalPages,
                            hasNext = page < totalPages,
                            hasPrev = page > 1
                        ),
                        categories = products.mapNotNull { it.category }.distinct()
                    )
                )
            } else {
                NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getRewardTransactions(limit: Int = 50): NetworkResult<List<PointTransactionDto>> {
        return try {
            val response = apiService.getRewardTransactions(limit)
            if (response.isSuccessful) {
                NetworkResult.Success(response.body()!!.data)
            } else {
                NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getRewardRedemptions(): NetworkResult<List<RewardRedemptionHistoryDto>> {
        return try {
            val response = apiService.getRewardRedemptions()
            if (response.isSuccessful) {
                NetworkResult.Success(response.body()!!.data)
            } else {
                NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getRewardVouchers(): NetworkResult<List<RewardVoucherDto>> {
        return try {
            val response = apiService.getRewardVouchers()
            if (response.isSuccessful) {
                NetworkResult.Success(response.body()!!.data)
            } else {
                NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun redeem(request: RedeemRequest): NetworkResult<DataMessageResponse<RedeemRewardResultDto>> {
        return try {
            val response = apiService.redeem(request)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    private fun parseErrorMessage(errorBody: String?): String = parseErrorBody(errorBody)
}
