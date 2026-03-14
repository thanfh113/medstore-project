package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import com.example.nhathuoc.data.remote.RetrofitClient
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository for order operations
 * Handles order placement, history, tracking, and cancellation
 */
class OrderRepository {

    private val apiService: ApiService by lazy {
        RetrofitClient.createService<ApiService>()
    }

    // Place new order
    suspend fun placeOrder(
        items: List<PlaceOrderItem>,
        paymentMethod: String,
        shippingAddressId: Int,
        notes: String? = null,
        rewardPointsToUse: Int = 0
    ): NetworkResult<PlaceOrderResponse> {
        return try {
            val request = PlaceOrderRequest(
                items = items,
                paymentMethod = paymentMethod,
                shippingAddressId = shippingAddressId,
                notes = notes,
                rewardPointsToUse = rewardPointsToUse
            )
            val response = apiService.placeOrder(request)

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

    // Get user orders with filters
    suspend fun getOrders(
        status: String? = null,
        page: Int = 1,
        limit: Int = 10
    ): NetworkResult<OrderListResponse> {
        return try {
            val response = apiService.getOrders(
                status = status,
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

    // Get order by ID
    suspend fun getOrderById(orderId: Int): NetworkResult<OrderDto> {
        return try {
            val response = apiService.getOrderById(orderId)

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

    // Cancel order
    suspend fun cancelOrder(
        orderId: Int,
        reason: String
    ): NetworkResult<CancelOrderResponse> {
        return try {
            val request = CancelOrderRequest(reason = reason)
            val response = apiService.cancelOrder(orderId, request)

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

    // Helper: Get orders by status
    suspend fun getOrdersByStatus(status: OrderStatus): NetworkResult<OrderListResponse> {
        return getOrders(status = status.value)
    }

    // Helper: Get pending orders
    suspend fun getPendingOrders(): NetworkResult<OrderListResponse> {
        return getOrdersByStatus(OrderStatus.PENDING)
    }

    // Helper: Get delivered orders
    suspend fun getDeliveredOrders(): NetworkResult<OrderListResponse> {
        return getOrdersByStatus(OrderStatus.DELIVERED)
    }

    // Helper: Get cancelled orders
    suspend fun getCancelledOrders(): NetworkResult<OrderListResponse> {
        return getOrdersByStatus(OrderStatus.CANCELLED)
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