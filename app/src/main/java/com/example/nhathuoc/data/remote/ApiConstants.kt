package com.example.nhathuoc.data.remote

/**
 * API constants for NhaThuoc app backend
 * Base URL for Android emulator pointing to localhost BE
 */
object ApiConstants {
    // Base URLs
    const val BASE_URL = "http://10.0.2.2:8080"

    // Auth endpoints
    const val AUTH_REGISTER = "/api/auth/register"
    const val AUTH_LOGIN = "/api/auth/login"
    const val AUTH_REFRESH = "/api/auth/refresh"
    const val AUTH_LOGOUT = "/api/auth/logout"

    // User endpoints
    const val USER_ME = "/api/user/me"
    const val USER_UPDATE = "/api/user/update"
    const val USER_ADDRESSES = "/api/user/addresses"
    const val USER_ADDRESS_ADD = "/api/user/addresses"

    // Product endpoints
    const val PRODUCTS = "/api/products"
    const val PRODUCTS_FLASH_SALE = "/api/products/flash-sale"
    const val PRODUCTS_BEST_SELLERS = "/api/products/best-sellers"
    const val PRODUCT_BY_ID = "/api/products/{id}"
    const val PRODUCT_CERTIFICATES = "/api/products/{id}/certificates"

    // Cart endpoints
    const val CART = "/api/cart"
    const val CART_ITEMS = "/api/cart/items"
    const val CART_ITEM = "/api/cart/items/{itemId}"

    // Order endpoints
    const val ORDERS = "/api/orders"
    const val ORDER_BY_ID = "/api/orders/{orderId}"
    const val ORDER_CANCEL = "/api/orders/{orderId}/cancel"

    // Reward endpoints
    const val REWARD_ACCOUNT = "/api/rewards/account"
    const val REWARD_PRODUCTS = "/api/rewards/products"
    const val REWARD_REDEEM = "/api/rewards/redeem"

    // Vaccine endpoints
    const val VACCINES = "/api/vaccines"
    const val VACCINE_BOOK = "/api/vaccines/book"
    const val VACCINE_BOOKINGS = "/api/vaccines/my-bookings"

    // Chat endpoints
    const val CHAT_SESSIONS = "/api/chat/sessions"
    const val CHAT_SESSION = "/api/chat/sessions/{sessionId}"
    const val CHAT_MESSAGES = "/api/chat/sessions/{sessionId}/messages"
    const val CHAT_SEND = "/api/chat/sessions/{sessionId}/send"

    // Pharmacy endpoints
    const val PHARMACIES = "/api/pharmacies"
    const val PHARMACY_BY_ID = "/api/pharmacies/{id}"

    // Notification endpoints
    const val NOTIFICATIONS = "/api/notifications"
    const val NOTIFICATION_READ = "/api/notifications/{id}/read"
    const val NOTIFICATIONS_READ_ALL = "/api/notifications/read-all"

    // Banner endpoints
    const val BANNERS = "/api/banners"

    // Upload endpoints
    const val UPLOAD_FILE = "/api/upload"

    // HTTP Headers
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val HEADER_ACCEPT = "Accept"

    // Content Types
    const val CONTENT_TYPE_JSON = "application/json"
    const val CONTENT_TYPE_MULTIPART = "multipart/form-data"

    // Request timeout (seconds)
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}