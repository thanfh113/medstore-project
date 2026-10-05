package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.AddCartRequest
import com.example.nhathuoc.data.model.CartDto
import com.example.nhathuoc.data.model.DataMessageResponse
import com.example.nhathuoc.data.model.MessageResponse
import com.example.nhathuoc.data.model.NetworkResult
import com.example.nhathuoc.data.model.UpdateCartItemRequest
import com.example.nhathuoc.data.remote.AddCartItemResult
import com.example.nhathuoc.data.remote.CartApiService
import com.example.nhathuoc.util.ApiErrorHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val apiService: CartApiService
) {
    suspend fun getCart(): NetworkResult<DataMessageResponse<CartDto>> =
        ApiErrorHandler.safeApiCall { apiService.getCart() }

    suspend fun addToCart(request: AddCartRequest): NetworkResult<DataMessageResponse<AddCartItemResult>> =
        ApiErrorHandler.safeApiCall { apiService.addToCart(request) }

    suspend fun updateCartItem(itemId: String, quantity: Int): NetworkResult<MessageResponse> =
        ApiErrorHandler.safeApiCall { apiService.updateCartItem(itemId, UpdateCartItemRequest(quantity)) }

    suspend fun removeCartItem(itemId: String): NetworkResult<MessageResponse> =
        ApiErrorHandler.safeApiCall { apiService.removeCartItem(itemId) }

    suspend fun clearCart(): NetworkResult<MessageResponse> =
        ApiErrorHandler.safeApiCall { apiService.clearCart() }
}
