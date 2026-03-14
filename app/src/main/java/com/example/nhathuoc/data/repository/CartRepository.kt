package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import com.example.nhathuoc.data.remote.RetrofitClient
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository for cart operations
 * Handles cart operations: get, add, update, remove items
 */
class CartRepository {

    private val apiService: ApiService by lazy {
        RetrofitClient.createService<ApiService>()
    }

    // Get user cart
    suspend fun getCart(): NetworkResult<CartDto> {
        return try {
            val response = apiService.getCart()

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

    // Add product to cart
    suspend fun addToCart(
        productId: Int,
        quantity: Int = 1
    ): NetworkResult<AddCartResponse> {
        return try {
            val request = AddCartRequest(
                productId = productId,
                quantity = quantity
            )
            val response = apiService.addToCart(request)

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

    // Update cart item quantity
    suspend fun updateCartItem(
        itemId: Int,
        quantity: Int
    ): NetworkResult<CartResponse> {
        return try {
            val request = UpdateCartItemRequest(quantity = quantity)
            val response = apiService.updateCartItem(itemId, request)

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

    // Remove item from cart
    suspend fun removeCartItem(itemId: Int): NetworkResult<CartResponse> {
        return try {
            val response = apiService.removeCartItem(itemId)

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