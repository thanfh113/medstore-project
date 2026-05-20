package org.example.project.data.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.example.project.data.models.*
import org.example.project.data.repositories.*

class PharmacyApiService(private val httpClient: HttpClient) {

    companion object {
        private const val BASE_URL = "https://api.nhathuoc.com/v1"
        private const val ORDERS_ENDPOINT = "$BASE_URL/orders"
        private const val CUSTOMERS_ENDPOINT = "$BASE_URL/customers"
        private const val CHAT_ENDPOINT = "$BASE_URL/chat"
        private const val ANALYTICS_ENDPOINT = "$BASE_URL/analytics"
    }
    // Order API methods
    suspend fun getOrders(): List<Order> {
        return httpClient.get(ORDERS_ENDPOINT).body()
    }

    suspend fun getOrder(id: String): Order? {
        return try {
            httpClient.get("$ORDERS_ENDPOINT/$id").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createOrder(request: CreateOrderRequest): Order {
        return httpClient.post(ORDERS_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateOrderStatus(orderId: String, status: String): Order {
        return httpClient.patch("$ORDERS_ENDPOINT/$orderId/status") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("status" to status))
        }.body()
    }

    suspend fun getOrdersByStatus(status: String): List<Order> {
        return httpClient.get("$ORDERS_ENDPOINT/status/$status").body()
    }

    suspend fun getOrdersByCustomer(customerId: String): List<Order> {
        return httpClient.get("$ORDERS_ENDPOINT/customer/$customerId").body()
    }

    suspend fun getOrdersByDateRange(startDate: String, endDate: String): List<Order> {
        return httpClient.get("$ORDERS_ENDPOINT/date-range") {
            parameter("startDate", startDate)
            parameter("endDate", endDate)
        }.body()
    }

    suspend fun getTodaysOrders(): List<Order> {
        return httpClient.get("$ORDERS_ENDPOINT/today").body()
    }

    suspend fun getOrderStatistics(date: String): OrderStatistics {
        return httpClient.get("$ANALYTICS_ENDPOINT/orders") {
            parameter("date", date)
        }.body()
    }

    suspend fun addOrderStatusHistory(history: OrderStatusHistory) {
        httpClient.post("$ORDERS_ENDPOINT/${history.orderId}/history") {
            contentType(ContentType.Application.Json)
            setBody(history)
        }
    }

    suspend fun getOrderStatusHistory(orderId: String): List<OrderStatusHistory> {
        return httpClient.get("$ORDERS_ENDPOINT/$orderId/history").body()
    }

    suspend fun calculateTotalRevenue(date: String): Double {
        return httpClient.get("$ANALYTICS_ENDPOINT/revenue") {
            parameter("date", date)
        }.body<Map<String, Double>>()["total"] ?: 0.0
    }

    suspend fun getTopCustomers(limit: Int): List<Customer> {
        return httpClient.get("$ANALYTICS_ENDPOINT/top-customers") {
            parameter("limit", limit)
        }.body()
    }

    // Customer API methods
    suspend fun getCustomers(): List<Customer> {
        return httpClient.get(CUSTOMERS_ENDPOINT).body()
    }

    suspend fun getCustomer(id: String): Customer? {
        return try {
            httpClient.get("$CUSTOMERS_ENDPOINT/$id").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createCustomer(request: CreateCustomerRequest): Customer {
        return httpClient.post(CUSTOMERS_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateCustomer(id: String, request: UpdateCustomerRequest): Customer {
        return httpClient.put("$CUSTOMERS_ENDPOINT/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteCustomer(id: String) {
        httpClient.delete("$CUSTOMERS_ENDPOINT/$id")
    }

    suspend fun searchCustomers(query: String): List<Customer> {
        return httpClient.get("$CUSTOMERS_ENDPOINT/search") {
            parameter("q", query)
        }.body()
    }

    suspend fun updateRewardPoints(customerId: String, points: Int) {
        httpClient.patch("$CUSTOMERS_ENDPOINT/$customerId/points") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("points" to points))
        }
    }

    suspend fun getCustomerOrderHistory(customerId: String): List<Order> {
        return httpClient.get("$CUSTOMERS_ENDPOINT/$customerId/orders").body()
    }

    // Chat API methods
    suspend fun getChatConversations(): List<ChatConversation> {
        return httpClient.get("$CHAT_ENDPOINT/conversations").body()
    }

    suspend fun getChatConversation(id: String): ChatConversation? {
        return try {
            httpClient.get("$CHAT_ENDPOINT/conversations/$id").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createChatConversation(request: CreateConversationRequest): ChatConversation {
        return httpClient.post("$CHAT_ENDPOINT/conversations") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateChatConversation(id: String, request: UpdateConversationRequest): ChatConversation {
        return httpClient.put("$CHAT_ENDPOINT/conversations/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteChatConversation(id: String) {
        httpClient.delete("$CHAT_ENDPOINT/conversations/$id")
    }

    suspend fun getChatConversationsByStatus(status: String): List<ChatConversation> {
        return httpClient.get("$CHAT_ENDPOINT/conversations/status/$status").body()
    }

    suspend fun assignPharmacist(conversationId: String, pharmacistId: String) {
        httpClient.patch("$CHAT_ENDPOINT/conversations/$conversationId/assign") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("pharmacistId" to pharmacistId))
        }
    }

    suspend fun getChatMessages(conversationId: String, limit: Int = 50, offset: Int = 0): List<ChatMessage> {
        return httpClient.get("$CHAT_ENDPOINT/conversations/$conversationId/messages") {
            parameter("limit", limit)
            parameter("offset", offset)
        }.body()
    }

    suspend fun sendChatMessage(request: SendMessageRequest): ChatMessage {
        return httpClient.post("$CHAT_ENDPOINT/messages") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun markMessageAsRead(messageId: String) {
        httpClient.patch("$CHAT_ENDPOINT/messages/$messageId/read")
    }

    suspend fun markConversationAsRead(conversationId: String) {
        httpClient.patch("$CHAT_ENDPOINT/conversations/$conversationId/read")
    }

    suspend fun editChatMessage(messageId: String, newContent: String): ChatMessage {
        return httpClient.patch("$CHAT_ENDPOINT/messages/$messageId") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("content" to newContent))
        }.body()
    }

    suspend fun deleteChatMessage(messageId: String) {
        httpClient.delete("$CHAT_ENDPOINT/messages/$messageId")
    }

    suspend fun getUnreadCount(conversationId: String): Int {
        return httpClient.get("$CHAT_ENDPOINT/conversations/$conversationId/unread-count").body<Map<String, Int>>()["count"] ?: 0
    }

    suspend fun getTotalUnreadCount(): Int {
        return httpClient.get("$CHAT_ENDPOINT/unread-count").body<Map<String, Int>>()["total"] ?: 0
    }

    suspend fun updateLastSeen(conversationId: String, userId: String) {
        httpClient.patch("$CHAT_ENDPOINT/conversations/$conversationId/last-seen") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("userId" to userId))
        }
    }

    suspend fun getOnlinePharmacists(): List<String> {
        return httpClient.get("$CHAT_ENDPOINT/pharmacists/online").body()
    }

    suspend fun getChatTemplates(): List<ChatTemplate> {
        return httpClient.get("$CHAT_ENDPOINT/templates").body()
    }

    suspend fun createChatTemplate(request: CreateChatTemplateRequest): ChatTemplate {
        return httpClient.post("$CHAT_ENDPOINT/templates") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateChatTemplate(id: String, request: UpdateChatTemplateRequest): ChatTemplate {
        return httpClient.put("$CHAT_ENDPOINT/templates/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteChatTemplate(id: String) {
        httpClient.delete("$CHAT_ENDPOINT/templates/$id")
    }

    suspend fun getChatStatistics(date: String): ChatStatistics {
        return httpClient.get("$ANALYTICS_ENDPOINT/chat") {
            parameter("date", date)
        }.body()
    }

    suspend fun getResponseTimeAnalytics(pharmacistId: String?, startDate: String, endDate: String): ResponseTimeAnalytics {
        return httpClient.get("$ANALYTICS_ENDPOINT/chat/response-time") {
            pharmacistId?.let { parameter("pharmacistId", it) }
            parameter("startDate", startDate)
            parameter("endDate", endDate)
        }.body()
    }

    // Analytics and Dashboard methods
    suspend fun getDashboardStatistics(date: String): DashboardStatistics {
        return httpClient.get("$ANALYTICS_ENDPOINT/dashboard") {
            parameter("date", date)
        }.body()
    }
}

@kotlinx.serialization.Serializable
data class DashboardStatistics(
    val todayRevenue: Double,
    val pendingOrders: Int,
    val activeChats: Int,
    val lowStockProducts: Int,
    val topProducts: List<TopSellingProduct>,
    val recentOrders: List<Order>,
    val salesTrend: List<SalesTrendData>,
    val date: String
)

@kotlinx.serialization.Serializable
data class SalesTrendData(
    val date: String,
    val revenue: Double,
    val orders: Int
)

// HTTP Client Factory
object HttpClientFactory {
    fun create(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                    encodeDefaults = true
                })
            }

            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }
}
