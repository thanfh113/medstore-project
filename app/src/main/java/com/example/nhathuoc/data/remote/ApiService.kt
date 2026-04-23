package com.example.nhathuoc.data.remote

import com.example.nhathuoc.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST(ApiConstants.AUTH_REGISTER)
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST(ApiConstants.AUTH_LOGIN)
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST(ApiConstants.AUTH_REFRESH)
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @POST(ApiConstants.AUTH_LOGOUT)
    suspend fun logout(): Response<LogoutResponse>

    @GET(ApiConstants.USER_ME)
    suspend fun getMe(): Response<UserResponse>

    @PUT(ApiConstants.USER_UPDATE)
    suspend fun updateUser(@Body request: UpdateUserRequest): Response<UpdateUserResponse>

    @GET(ApiConstants.USER_ADDRESSES)
    suspend fun getUserAddresses(): Response<List<UserAddress>>

    @POST(ApiConstants.USER_ADDRESS_ADD)
    suspend fun addAddress(@Body request: AddAddressRequest): Response<AddAddressResponse>

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
    suspend fun getBestSellers(@Query("period") period: String? = "week"): Response<BestSellersResponse>

    @GET(ApiConstants.PRODUCT_BY_ID)
    suspend fun getProductById(@Path("id") productId: String): Response<ProductDetailResponse>

    @GET(ApiConstants.PRODUCT_CERTIFICATES)
    suspend fun getProductCertificates(@Path("id") productId: String): Response<List<ProductCertificateDto>>

    @GET(ApiConstants.CATEGORIES)
    suspend fun getCategories(
        @Query("parentId") parentId: String? = null,
        @Query("isActive") isActive: Boolean = true
    ): Response<DataMessageResponse<List<CategoryDto>>>

    @GET(ApiConstants.CATEGORY_BY_ID)
    suspend fun getCategoryById(@Path("id") categoryId: String): Response<DataMessageResponse<CategoryDto>>

    @GET(ApiConstants.CATEGORY_ATTRIBUTES)
    suspend fun getCategoryAttributes(@Path("id") categoryId: String): Response<DataMessageResponse<List<CategoryAttributeDto>>>

    @GET(ApiConstants.CART)
    suspend fun getCart(): Response<CartDto>

    @POST(ApiConstants.CART_ITEMS)
    suspend fun addToCart(@Body request: AddCartRequest): Response<AddCartResponse>

    @PUT(ApiConstants.CART_ITEM)
    suspend fun updateCartItem(
        @Path("itemId") itemId: String,
        @Body request: UpdateCartItemRequest
    ): Response<CartResponse>

    @DELETE(ApiConstants.CART_ITEM)
    suspend fun removeCartItem(@Path("itemId") itemId: String): Response<CartResponse>

    @POST(ApiConstants.CHECKOUT)
    suspend fun checkout(@Body request: CheckoutRequest): Response<ApiResponse<CheckoutOrderSummaryDto>>

    @POST(ApiConstants.PAYMENTS_MOMO_INIT)
    suspend fun initMomoPayment(@Body request: PaymentInitRequest): Response<DataMessageResponse<PaymentInitData>>

    @POST(ApiConstants.PAYMENTS_VNPAY_INIT)
    suspend fun initVnPayPayment(@Body request: PaymentInitRequest): Response<DataMessageResponse<PaymentInitData>>

    @POST(ApiConstants.PAYMENTS_ZALOPAY_INIT)
    suspend fun initZaloPayPayment(@Body request: PaymentInitRequest): Response<DataMessageResponse<PaymentInitData>>

    @POST(ApiConstants.PAYMENTS_COD_CREATE)
    suspend fun createCodPayment(@Body request: CodPaymentRequest): Response<DataMessageResponse<CodPaymentData>>

    @GET(ApiConstants.PAYMENT_STATUS)
    suspend fun getPaymentStatus(@Path("orderId") orderId: String): Response<DataMessageResponse<PaymentStatusDto>>

    @GET(ApiConstants.ORDERS)
    suspend fun getOrders(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<OrderListResponse>

    @GET(ApiConstants.ORDER_BY_ID)
    suspend fun getOrderById(@Path("orderId") orderId: String): Response<OrderDto>

    @POST(ApiConstants.ORDER_CANCEL)
    suspend fun cancelOrder(
        @Path("orderId") orderId: String,
        @Body request: CancelOrderRequest
    ): Response<MessageResponse>

    @GET(ApiConstants.REWARD_ACCOUNT)
    suspend fun getRewardAccount(): Response<DataMessageResponse<RewardAccountDto>>

    @GET(ApiConstants.REWARD_PRODUCTS)
    suspend fun getRewardProducts(
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<DataMessageResponse<List<RewardProductDto>>>

    @GET(ApiConstants.REWARD_TRANSACTIONS)
    suspend fun getRewardTransactions(
        @Query("limit") limit: Int = 50
    ): Response<DataMessageResponse<List<PointTransactionDto>>>

    @GET(ApiConstants.REWARD_REDEMPTIONS)
    suspend fun getRewardRedemptions(): Response<DataMessageResponse<List<RewardRedemptionHistoryDto>>>

    @POST(ApiConstants.REWARD_REDEEM)
    suspend fun redeem(@Body request: RedeemRequest): Response<DataMessageResponse<RedeemRewardResultDto>>

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
    suspend fun getPharmacyById(@Path("id") pharmacyId: String): Response<PharmacyDto>

    @GET(ApiConstants.NOTIFICATIONS)
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<DataMessageResponse<List<NotificationDto>>>

    @POST(ApiConstants.NOTIFICATION_READ)
    suspend fun markNotificationAsRead(@Path("id") notificationId: String): Response<MessageResponse>

    @POST(ApiConstants.NOTIFICATIONS_READ_ALL)
    suspend fun markAllNotificationsAsRead(): Response<MessageResponse>

    @GET(ApiConstants.BANNERS)
    suspend fun getBanners(
        @Query("position") position: String? = null,
        @Query("isActive") isActive: Boolean = true
    ): Response<DataMessageResponse<List<BannerDto>>>

    @GET(ApiConstants.BANNER_BY_ID)
    suspend fun getBannerById(@Path("id") bannerId: String): Response<DataMessageResponse<BannerDto>>

    @POST(ApiConstants.INVENTORY_BATCHES)
    suspend fun createBatch(@Body request: CreateBatchRequest): Response<BatchDto>

    @GET(ApiConstants.INVENTORY_ALERTS_EXPIRING)
    suspend fun getExpiringAlerts(@Query("days") days: Int = 30): Response<ExpiringAlertsResponse>

    @Multipart
    @POST(ApiConstants.UPLOAD_FILE)
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody
    ): Response<UploadResponse>

    @GET(ApiConstants.SHOPS)
    suspend fun getShops(
        @Query("isApproved") isApproved: Boolean = true,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedResponse<ShopDto>>

    @GET(ApiConstants.SHOP_BY_ID)
    suspend fun getShopById(@Path("id") shopId: String): Response<ShopDto>

    @GET(ApiConstants.PRESCRIPTIONS)
    suspend fun getPrescriptions(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<PaginatedResponse<PrescriptionDto>>

    @POST(ApiConstants.PRESCRIPTIONS)
    suspend fun uploadPrescription(@Body request: PrescriptionDto): Response<PrescriptionDto>

    @GET(ApiConstants.PRESCRIPTION_BY_ID)
    suspend fun getPrescriptionById(@Path("id") prescriptionId: String): Response<PrescriptionDto>

    @GET(ApiConstants.HEALTH_ARTICLES)
    suspend fun getHealthArticles(
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedResponse<HealthArticleDto>>

    @GET(ApiConstants.HEALTH_ARTICLE_BY_SLUG)
    suspend fun getHealthArticleBySlug(@Path("slug") slug: String): Response<HealthArticleDto>

    @GET(ApiConstants.DISEASE_CATEGORIES)
    suspend fun getDiseaseCategories(): Response<List<DiseaseCategoryDto>>

    @GET(ApiConstants.PAYMENT_METHODS)
    suspend fun getPaymentMethods(@Query("isActive") isActive: Boolean = true): Response<List<String>>

    @GET(ApiConstants.CHAT_SESSIONS)
    suspend fun getChatSessions(): Response<DataMessageResponse<List<ChatSessionDto>>>

    @POST(ApiConstants.CHAT_SESSIONS)
    suspend fun createChatSession(@Body request: CreateChatSessionRequest): Response<DataMessageResponse<ChatSessionDto>>

    @GET(ApiConstants.CHAT_MESSAGES)
    suspend fun getChatMessages(
        @Path("sessionId") sessionId: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Long = 0
    ): Response<DataMessageResponse<List<ChatMessageDto>>>

    @POST(ApiConstants.CHAT_MESSAGES)
    suspend fun sendChatMessage(
        @Path("sessionId") sessionId: String,
        @Body request: SendChatMessageRequest
    ): Response<DataMessageResponse<ChatMessageDto>>
}
