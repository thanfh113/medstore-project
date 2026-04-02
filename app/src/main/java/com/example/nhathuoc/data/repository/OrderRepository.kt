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

    // Place new order using request object
    suspend fun placeOrder(request: PlaceOrderRequest): NetworkResult<PlaceOrderResponse> {
        return try {
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

    // Place new order with individual parameters
    suspend fun placeOrder(
        items: List<PlaceOrderItem>,
        paymentMethod: String,
        pickupType: String = "DELIVERY", // Added pickup type
        shippingAddressId: String? = null, // Made optional for pickup orders
        branchId: String? = null, // Added for pickup orders
        note: String? = null, // Changed from notes to note to match schema
        pointsToUse: Int = 0 // Renamed from rewardPointsToUse
    ): NetworkResult<PlaceOrderResponse> {
        val request = PlaceOrderRequest(
            items = items,
            paymentMethod = paymentMethod,
            pickupType = pickupType,
            shippingAddressId = shippingAddressId,
            branchId = branchId,
            note = note,
            pointsToUse = pointsToUse
        )
        return placeOrder(request)
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
    suspend fun getOrderById(orderId: String): NetworkResult<OrderDto> { // Changed to String for UUID
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
        orderId: String, // Changed to String for UUID
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

    // Helper: Get pending orders
    suspend fun getPendingOrders(): NetworkResult<OrderListResponse> {
        return getOrders(status = "PENDING")
    }

    // Helper: Get delivered orders
    suspend fun getDeliveredOrders(): NetworkResult<OrderListResponse> {
        return getOrders(status = "DELIVERED")
    }

    // Helper: Get cancelled orders
    suspend fun getCancelledOrders(): NetworkResult<OrderListResponse> {
        return getOrders(status = "CANCELLED")
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