package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getOrders(status: String? = null, page: Int = 1, limit: Int = 10): NetworkResult<OrderListResponse> {
        return try {
            val response = apiService.getOrders(status, page, limit)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getOrderById(orderId: String): NetworkResult<OrderDto> {
        return try {
            val response = apiService.getOrderById(orderId)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun cancelOrder(orderId: String, reason: String): NetworkResult<MessageResponse> {
        return try {
            val response = apiService.cancelOrder(orderId, CancelOrderRequest(reason))
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
