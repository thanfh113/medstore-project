package com.example.nhathuoc.data.remote

/**
 * API constants for NhaThuoc app backend
 * Base URL for Android emulator pointing to localhost BE
 */
object ApiConstants {
    const val BASE_URL = "http://172.16.76.227:8080"

    const val AUTH_REGISTER = "/api/v1/auth/register"
    const val AUTH_LOGIN = "/api/v1/auth/login"
    const val AUTH_REFRESH = "/api/v1/auth/refresh"
    const val AUTH_LOGOUT = "/api/v1/auth/logout"
    const val AUTH_CHANGE_PASSWORD = "/api/v1/auth/change-password"
    const val AUTH_FORGOT_PASSWORD = "/api/v1/auth/forgot-password"
    const val AUTH_RESET_PASSWORD = "/api/v1/auth/reset-password"

    const val USER_ME = "/api/v1/users/me"
    const val USER_UPDATE = "/api/v1/user/update"
    const val USER_ADDRESSES = "/api/v1/users/me/addresses"
    const val USER_ADDRESS_ADD = "/api/v1/users/me/addresses"
    const val USER_ADDRESS_BY_ID = "/api/v1/users/me/addresses/{id}"

    const val PRODUCTS = "/api/v1/products"
    const val PRODUCTS_FLASH_SALE = "/api/v1/products/flash-sale"
    const val PRODUCTS_BEST_SELLERS = "/api/v1/products/best-sellers"
    const val PRODUCT_BY_ID = "/api/v1/products/{id}"
    const val PRODUCT_CERTIFICATES = "/api/v1/products/{id}/certificates"
    const val PRODUCT_REVIEWS = "/api/v1/products/{id}/reviews"
    const val REVIEW_REPORT = "/api/v1/reviews/{id}/report"

    const val CATEGORIES = "/api/v1/categories"
    const val CATEGORY_BY_ID = "/api/v1/categories/{id}"
    const val CATEGORY_ATTRIBUTES = "/api/v1/categories/{id}/attributes"

    const val CART = "/api/v1/cart"
    const val CART_ITEMS = "/api/v1/cart/items"
    const val CART_ITEM = "/api/v1/cart/items/{itemId}"

    const val ORDERS = "/api/v1/orders"
    const val ORDERS_POS = "/api/v1/orders/pos"
    const val ORDER_BY_ID = "/api/v1/orders/{orderId}"
    const val ORDER_CANCEL = "/api/v1/orders/{orderId}/cancel"
    const val ORDER_CONFIRM_RECEIVED = "/api/v1/orders/{orderId}/confirm-received"
    const val CHECKOUT = "/api/v1/checkout"

    const val COMPLAINTS = "/api/v1/complaints"
    const val COMPLAINT_BY_ID = "/api/v1/complaints/{id}"
    const val COMPLAINT_MESSAGES = "/api/v1/complaints/{id}/messages"
    const val COMPLAINT_ATTACHMENTS = "/api/v1/complaints/{id}/attachments"
    const val COMPLAINT_REQUEST_REFUND = "/api/v1/complaints/{id}/request-refund"

    const val PAYMENTS_MOMO_INIT = "/api/v1/payments/momo/init"
    const val PAYMENTS_VNPAY_INIT = "/api/v1/payments/vnpay/init"
    const val PAYMENTS_ZALOPAY_INIT = "/api/v1/payments/zalopay/init"
    const val PAYMENTS_COD_CREATE = "/api/v1/payments/cod/create"
    const val PAYMENT_STATUS = "/api/v1/payments/{orderId}"

    const val REWARD_ACCOUNT = "/api/v1/rewards/account"
    const val REWARD_PRODUCTS = "/api/v1/rewards/products"
    const val REWARD_PRODUCTS_ME = "/api/v1/rewards/products/me"
    const val REWARD_TRANSACTIONS = "/api/v1/rewards/transactions"
    const val REWARD_REDEMPTIONS = "/api/v1/rewards/redemptions"
    const val REWARD_VOUCHERS = "/api/v1/rewards/vouchers"
    const val REWARD_REDEEM = "/api/v1/rewards/redeem"

    const val CHAT_SESSIONS = "/api/v1/chat/sessions"
    const val CHAT_SESSION = "/api/v1/chat/sessions/{sessionId}"
    const val CHAT_MESSAGES = "/api/v1/chat/sessions/{sessionId}/messages"
    const val CHAT_SESSION_STATUS = "/api/v1/chat/sessions/{sessionId}/status"

    const val AI_CHAT_SESSIONS = "/api/v1/ai-chat/sessions"
    const val AI_CHAT_SESSION = "/api/v1/ai-chat/sessions/{conversationId}"
    const val AI_CHAT_MESSAGE = "/api/v1/ai-chat/sessions/{conversationId}/message"
    const val AI_CHAT_ESCALATE = "/api/v1/ai-chat/sessions/{conversationId}/escalate"
    const val AI_CHAT_CLOSE = "/api/v1/ai-chat/sessions/{conversationId}/close"
    const val AI_CHAT_DELETE = "/api/v1/ai-chat/sessions/{conversationId}"

    const val PHARMACIES = "/api/v1/pharmacies"
    const val PHARMACY_BY_ID = "/api/v1/pharmacies/{id}"

    const val NOTIFICATIONS = "/api/v1/notifications"
    const val NOTIFICATION_READ = "/api/v1/notifications/{id}/read"
    const val NOTIFICATIONS_READ_ALL = "/api/v1/notifications/read-all"
    const val NOTIFICATION_PUSH_TOKEN = "/api/v1/notifications/push-token"

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
