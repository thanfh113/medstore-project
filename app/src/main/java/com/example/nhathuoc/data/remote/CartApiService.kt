package com.example.nhathuoc.data.remote

import com.example.nhathuoc.data.model.AddCartRequest
import com.example.nhathuoc.data.model.CartDto
import com.example.nhathuoc.data.model.DataMessageResponse
import com.example.nhathuoc.data.model.MessageResponse
import com.example.nhathuoc.data.model.UpdateCartItemRequest
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

@Serializable
data class AddCartItemResult(
    val cartItemId: String
)

interface CartApiService {
    @GET("api/v1/cart")
    suspend fun getCart(): Response<DataMessageResponse<CartDto>>

    @POST("api/v1/cart/items")
    suspend fun addToCart(
        @Body request: AddCartRequest
    ): Response<DataMessageResponse<AddCartItemResult>>

    @PUT("api/v1/cart/items/{id}")
    suspend fun updateCartItem(
        @Path("id") itemId: String,
        @Body request: UpdateCartItemRequest
    ): Response<MessageResponse>

    @DELETE("api/v1/cart/items/{id}")
    suspend fun removeCartItem(@Path("id") itemId: String): Response<MessageResponse>

    @DELETE("api/v1/cart/clear")
    suspend fun clearCart(): Response<MessageResponse>
}
