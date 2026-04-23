package com.example.nhathuoc.data.remote

/**
 * API constants for NhaThuoc app backend
 * Base URL for Android emulator pointing to localhost BE
 */
object ApiConstants {
    const val BASE_URL = "http://10.196.200.29:8080"

    const val AUTH_REGISTER = "/api/v1/auth/register"
    const val AUTH_LOGIN = "/api/v1/auth/login"
    const val AUTH_REFRESH = "/api/v1/auth/refresh"
    const val AUTH_LOGOUT = "/api/v1/auth/logout"

    const val USER_ME = "/api/v1/user/me"
    const val USER_UPDATE = "/api/v1/user/update"
    const val USER_ADDRESSES = "/api/v1/user/addresses"
    const val USER_ADDRESS_ADD = "/api/v1/user/addresses"

    const val PRODUCTS = "/api/v1/products"
    const val PRODUCTS_FLASH_SALE = "/api/v1/products/flash-sale"
    const val PRODUCTS_BEST_SELLERS = "/api/v1/products/best-sellers"
    const val PRODUCT_BY_ID = "/api/v1/products/{id}"
    const val PRODUCT_CERTIFICATES = "/api/v1/products/{id}/certificates"

    const val CATEGORIES = "/api/v1/categories"
    const val CATEGORY_BY_ID = "/api/v1/categories/{id}"
    const val CATEGORY_ATTRIBUTES = "/api/v1/categories/{id}/attributes"

    const val CART = "/api/v1/cart"
    const val CART_ITEMS = "/api/v1/cart/items"
    const val CART_ITEM = "/api/v1/cart/items/{itemId}"

    const val ORDERS = "/api/v1/orders"
    const val ORDER_BY_ID = "/api/v1/orders/{orderId}"
    const val ORDER_CANCEL = "/api/v1/orders/{orderId}/cancel"
    const val CHECKOUT = "/api/v1/checkout"

    const val PAYMENTS_MOMO_INIT = "/api/v1/payments/momo/init"
    const val PAYMENTS_VNPAY_INIT = "/api/v1/payments/vnpay/init"
    const val PAYMENTS_ZALOPAY_INIT = "/api/v1/payments/zalopay/init"
    const val PAYMENTS_COD_CREATE = "/api/v1/payments/cod/create"
    const val PAYMENT_STATUS = "/api/v1/payments/{orderId}"

    const val REWARD_ACCOUNT = "/api/v1/rewards/account"
    const val REWARD_PRODUCTS = "/api/v1/rewards/products"
    const val REWARD_TRANSACTIONS = "/api/v1/rewards/transactions"
    const val REWARD_REDEMPTIONS = "/api/v1/rewards/redemptions"
    const val REWARD_REDEEM = "/api/v1/rewards/redeem"

    const val CHAT_SESSIONS = "/api/v1/chat/sessions"
    const val CHAT_SESSION = "/api/v1/chat/sessions/{sessionId}"
    const val CHAT_MESSAGES = "/api/v1/chat/sessions/{sessionId}/messages"
    const val CHAT_SESSION_STATUS = "/api/v1/chat/sessions/{sessionId}/status"

    const val PHARMACIES = "/api/v1/pharmacies"
    const val PHARMACY_BY_ID = "/api/v1/pharmacies/{id}"

    const val NOTIFICATIONS = "/api/v1/notifications"
    const val NOTIFICATION_READ = "/api/v1/notifications/{id}/read"
    const val NOTIFICATIONS_READ_ALL = "/api/v1/notifications/read-all"

    const val BANNERS = "/api/v1/banners"
    const val BANNER_BY_ID = "/api/v1/banners/{id}"

    const val SHOPS = "/api/v1/shops"
    const val SHOP_BY_ID = "/api/v1/shops/{id}"

    const val PRESCRIPTIONS = "/api/v1/prescriptions"
    const val PRESCRIPTION_BY_ID = "/api/v1/prescriptions/{id}"

    const val HEALTH_ARTICLES = "/api/v1/health-articles"
    const val HEALTH_ARTICLE_BY_SLUG = "/api/v1/health-articles/{slug}"

    const val DISEASE_CATEGORIES = "/api/v1/disease-categories"
    const val PAYMENT_METHODS = "/api/v1/payment-methods"

    const val UPLOAD_FILE = "/api/v1/upload"

    const val INVENTORY_BATCHES = "/api/v1/inventory/batches"
    const val INVENTORY_ALERTS_EXPIRING = "/api/v1/inventory/alerts/expiring"

    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val HEADER_ACCEPT = "Accept"

    const val CONTENT_TYPE_JSON = "application/json"
    const val CONTENT_TYPE_MULTIPART = "multipart/form-data"

    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}
