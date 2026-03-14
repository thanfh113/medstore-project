package com.example.nhathuoc.data.remote

import com.example.nhathuoc.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service interface
 * Defines all API endpoints synchronized with Backend Routes.kt
 */
interface ApiService {

    // ===== Authentication Endpoints =====

    @POST(ApiConstants.AUTH_REGISTER)
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST(ApiConstants.AUTH_LOGIN)
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST(ApiConstants.AUTH_REFRESH)
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @POST(ApiConstants.AUTH_LOGOUT)
    suspend fun logout(): Response<LogoutResponse>

    // ===== User Endpoints =====

    @GET(ApiConstants.USER_ME)
    suspend fun getMe(): Response<UserResponse>

    @PUT(ApiConstants.USER_UPDATE)
    suspend fun updateUser(@Body request: UpdateUserRequest): Response<UpdateUserResponse>

    @GET(ApiConstants.USER_ADDRESSES)
    suspend fun getUserAddresses(): Response<List<UserAddress>>

    @POST(ApiConstants.USER_ADDRESS_ADD)
    suspend fun addAddress(@Body request: AddAddressRequest): Response<AddAddressResponse>

    // ===== Product Endpoints =====

    @GET(ApiConstants.PRODUCTS)
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("brand") brand: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ProductListResponse>

    @GET(ApiConstants.PRODUCTS_FLASH_SALE)
    suspend fun getFlashSaleProducts(): Response<FlashSaleResponse>

    @GET(ApiConstants.PRODUCTS_BEST_SELLERS)
    suspend fun getBestSellers(
        @Query("period") period: String? = "week"
    ): Response<BestSellersResponse>

    @GET(ApiConstants.PRODUCT_BY_ID)
    suspend fun getProductById(@Path("id") productId: Int): Response<ProductDetailResponse>

    @GET(ApiConstants.PRODUCT_CERTIFICATES)
    suspend fun getProductCertificates(@Path("id") productId: Int): Response<List<ProductCertificateDto>>

    // ===== Cart Endpoints =====

    @GET(ApiConstants.CART)
    suspend fun getCart(): Response<CartDto>

    @POST(ApiConstants.CART_ITEMS)
    suspend fun addToCart(@Body request: AddCartRequest): Response<AddCartResponse>

    @PUT(ApiConstants.CART_ITEM)
    suspend fun updateCartItem(
        @Path("itemId") itemId: Int,
        @Body request: UpdateCartItemRequest
    ): Response<CartResponse>

    @DELETE(ApiConstants.CART_ITEM)
    suspend fun removeCartItem(@Path("itemId") itemId: Int): Response<CartResponse>

    // ===== Order Endpoints =====

    @POST(ApiConstants.ORDERS)
    suspend fun placeOrder(@Body request: PlaceOrderRequest): Response<PlaceOrderResponse>

    @GET(ApiConstants.ORDERS)
    suspend fun getOrders(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<OrderListResponse>

    @GET(ApiConstants.ORDER_BY_ID)
    suspend fun getOrderById(@Path("orderId") orderId: Int): Response<OrderDto>

    @POST(ApiConstants.ORDER_CANCEL)
    suspend fun cancelOrder(
        @Path("orderId") orderId: Int,
        @Body request: CancelOrderRequest
    ): Response<CancelOrderResponse>

    // ===== Reward Endpoints =====

    @GET(ApiConstants.REWARD_ACCOUNT)
    suspend fun getRewardAccount(): Response<RewardAccountDto>

    @GET(ApiConstants.REWARD_PRODUCTS)
    suspend fun getRewardProducts(
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<RewardProductListResponse>

    @POST(ApiConstants.REWARD_REDEEM)
    suspend fun redeem(@Body request: RedeemRequest): Response<RedeemResponse>

    // ===== Pharmacy Endpoints =====

    @GET(ApiConstants.PHARMACIES)
    suspend fun searchPharmacies(
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("radius") radius: Double? = null,
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedResponse<PharmacyDto>>

    @GET(ApiConstants.PHARMACY_BY_ID)
    suspend fun getPharmacyById(@Path("id") pharmacyId: Int): Response<PharmacyDto>

    // ===== Notification Endpoints =====

    @GET(ApiConstants.NOTIFICATIONS)
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedResponse<NotificationDto>>

    @POST(ApiConstants.NOTIFICATION_READ)
    suspend fun markNotificationAsRead(@Path("id") notificationId: Int): Response<MessageResponse>

    @POST(ApiConstants.NOTIFICATIONS_READ_ALL)
    suspend fun markAllNotificationsAsRead(): Response<MessageResponse>

    // ===== Banner Endpoints =====

    @GET(ApiConstants.BANNERS)
    suspend fun getBanners(
        @Query("position") position: String? = null,
        @Query("isActive") isActive: Boolean = true
    ): Response<List<BannerDto>>

    // ===== Upload Endpoints =====

    @Multipart
    @POST(ApiConstants.UPLOAD_FILE)
    suspend fun uploadFile(
        @Part file: okhttp3.MultipartBody.Part,
        @Part("type") type: okhttp3.RequestBody
    ): Response<UploadResponse>
}