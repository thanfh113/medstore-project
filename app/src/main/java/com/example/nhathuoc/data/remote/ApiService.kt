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

    @POST(ApiConstants.AUTH_CHANGE_PASSWORD)
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

    @GET(ApiConstants.USER_ME)
    suspend fun getMe(): Response<UserResponse>

    @PUT(ApiConstants.USER_UPDATE)
    suspend fun updateUser(@Body request: UpdateUserRequest): Response<UpdateUserResponse>

    @GET(ApiConstants.USER_ADDRESSES)
    suspend fun getUserAddresses(): Response<AddressListResponse>

    @POST(ApiConstants.USER_ADDRESS_ADD)
    suspend fun addAddress(@Body request: AddAddressRequest): Response<AddAddressResponse>

    @PUT(ApiConstants.USER_ADDRESS_BY_ID)
    suspend fun updateAddress(
        @Path("id") id: String,
        @Body request: AddAddressRequest
    ): Response<AddAddressResponse>

    @DELETE(ApiConstants.USER_ADDRESS_BY_ID)
    suspend fun deleteAddress(@Path("id") id: String): Response<Unit>

    @GET(ApiConstants.PRODUCTS)
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("brand") brand: String? = null,
        @Query("search") search: String? = null,
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

    @GET(ApiConstants.PRODUCT_REVIEWS)
    suspend fun getProductReviews(@Path("id") productId: String): Response<ProductReviewsResponse>

    @POST(ApiConstants.PRODUCT_REVIEWS)
    suspend fun createProductReview(
        @Path("id") productId: String,
        @Body request: CreateReviewRequest
    ): Response<ReviewDto>

    @POST(ApiConstants.REVIEW_REPORT)
    suspend fun reportReview(
        @Path("id") reviewId: String,
        @Body request: ReportReviewRequest
    ): Response<ReviewIdResponse>

    @GET(ApiConstants.CATEGORIES)
    suspend fun getCategories(
        @Query("parentId") parentId: String? = null,
        @Query("isActive") isActive: Boolean = true
    ): Response<DataMessageResponse<List<CategoryDto>>>

    @GET(ApiConstants.CATEGORY_BY_ID)
    suspend fun getCategoryById(@Path("id") categoryId: String): Response<DataMessageResponse<CategoryDto>>

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

    @GET(ApiConstants.ORDERS_POS)
    suspend fun getPosOrders(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<OrderListResponse>

    @GET(ApiConstants.ORDER_BY_ID)
    suspend fun getOrderById(@Path("orderId") orderId: String): Response<OrderDto>

    @POST(ApiConstants.ORDER_CANCEL)
    suspend fun cancelOrder(
        @Path("orderId") orderId: String,
        @Body request: CancelOrderRequest
    ): Response<MessageResponse>

    @POST(ApiConstants.ORDER_CONFIRM_RECEIVED)
    suspend fun confirmOrderReceived(@Path("orderId") orderId: String): Response<MessageResponse>

    @GET(ApiConstants.COMPLAINTS)
    suspend fun getComplaints(): Response<DataMessageResponse<List<ComplaintDto>>>

    @POST(ApiConstants.COMPLAINTS)
    suspend fun createComplaint(@Body request: CreateComplaintRequest): Response<DataMessageResponse<ComplaintDto>>

    @GET(ApiConstants.COMPLAINT_BY_ID)
    suspend fun getComplaintById(@Path("id") complaintId: String): Response<DataMessageResponse<ComplaintDto>>

    @POST(ApiConstants.COMPLAINT_MESSAGES)
    suspend fun sendComplaintMessage(
        @Path("id") complaintId: String,
        @Body request: ComplaintMessageRequest
    ): Response<DataMessageResponse<ComplaintMessageDto>>

    @POST(ApiConstants.COMPLAINT_ATTACHMENTS)
    suspend fun addComplaintAttachments(
        @Path("id") complaintId: String,
        @Body request: AddComplaintAttachmentsRequest
    ): Response<DataMessageResponse<ComplaintDto>>

    @POST(ApiConstants.COMPLAINT_REQUEST_REFUND)
    suspend fun requestRefundForComplaint(
        @Path("id") complaintId: String
    ): Response<DataMessageResponse<ComplaintDto>>

    @GET(ApiConstants.REWARD_ACCOUNT)
    suspend fun getRewardAccount(): Response<DataMessageResponse<RewardAccountDto>>

    @GET(ApiConstants.REWARD_PRODUCTS)
    suspend fun getRewardProducts(
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<DataMessageResponse<List<RewardProductDto>>>

    @GET(ApiConstants.REWARD_PRODUCTS_ME)
    suspend fun getMyRewardProducts(): Response<DataMessageResponse<List<RewardProductDto>>>

    @GET(ApiConstants.REWARD_TRANSACTIONS)
    suspend fun getRewardTransactions(
        @Query("limit") limit: Int = 50
    ): Response<DataMessageResponse<List<PointTransactionDto>>>

    @GET(ApiConstants.REWARD_REDEMPTIONS)
    suspend fun getRewardRedemptions(): Response<DataMessageResponse<List<RewardRedemptionHistoryDto>>>

    @GET(ApiConstants.REWARD_VOUCHERS)
    suspend fun getRewardVouchers(): Response<DataMessageResponse<List<RewardVoucherDto>>>

    @POST(ApiConstants.REWARD_REDEEM)
    suspend fun redeem(@Body request: RedeemRequest): Response<DataMessageResponse<RedeemRewardResultDto>>

    @GET(ApiConstants.NOTIFICATIONS)
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<DataMessageResponse<List<NotificationDto>>>

    @POST(ApiConstants.NOTIFICATION_READ)
    suspend fun markNotificationAsRead(@Path("id") notificationId: String): Response<MessageResponse>

    @POST(ApiConstants.NOTIFICATIONS_READ_ALL)
    suspend fun markAllNotificationsAsRead(): Response<MessageResponse>

    @POST(ApiConstants.NOTIFICATION_PUSH_TOKEN)
    suspend fun registerPushToken(@Body request: PushTokenRequest): Response<DataMessageResponse<PushTokenResponse>>

    @DELETE(ApiConstants.NOTIFICATION_PUSH_TOKEN)
    suspend fun deactivatePushToken(
        @Query("deviceId") deviceId: String? = null,
        @Query("fcmToken") fcmToken: String? = null
    ): Response<MessageResponse>

    @GET(ApiConstants.BANNERS)
    suspend fun getBanners(
        @Query("position") position: String? = null,
        @Query("isActive") isActive: Boolean = true
    ): Response<DataMessageResponse<List<BannerDto>>>

    @GET(ApiConstants.BANNER_BY_ID)
    suspend fun getBannerById(@Path("id") bannerId: String): Response<DataMessageResponse<BannerDto>>

    @Multipart
    @POST(ApiConstants.UPLOAD_FILE)
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Query("type") type: String
    ): Response<UploadResponse>

    @GET(ApiConstants.SHOPS)
    suspend fun getShops(
        @Query("isApproved") isApproved: Boolean = true,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedResponse<ShopDto>>

    @GET(ApiConstants.SHOP_BY_ID)
    suspend fun getShopById(@Path("id") shopId: String): Response<ShopDto>

    @POST(ApiConstants.AI_CHAT_SESSIONS)
    suspend fun createAiConversation(@Body request: CreateAiConversationRequest): Response<DataMessageResponse<AiConversationDto>>

    @GET(ApiConstants.AI_CHAT_SESSIONS)
    suspend fun getAiConversations(): Response<DataMessageResponse<List<AiConversationDto>>>

    @GET(ApiConstants.AI_CHAT_SESSION)
    suspend fun getAiConversation(@Path("conversationId") conversationId: String): Response<DataMessageResponse<AiConversationDto>>

    @POST(ApiConstants.AI_CHAT_MESSAGE)
    suspend fun sendAiMessage(
        @Path("conversationId") conversationId: String,
        @Body request: AiSendMessageRequest
    ): Response<DataMessageResponse<AiSendMessageResponse>>

    @POST(ApiConstants.AI_CHAT_ESCALATE)
    suspend fun escalateAiToHuman(@Path("conversationId") conversationId: String): Response<DataMessageResponse<AiSendMessageResponse>>

    @POST(ApiConstants.AI_CHAT_CLOSE)
    suspend fun closeAiConversation(@Path("conversationId") conversationId: String): Response<DataMessageResponse<AiConversationDto>>

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
