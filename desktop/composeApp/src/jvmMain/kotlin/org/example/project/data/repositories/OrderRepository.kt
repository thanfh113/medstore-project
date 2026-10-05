package org.example.project.data.repositories

import org.example.project.data.models.*

interface OrderRepository {
    suspend fun getAllOrders(): Result<List<Order>>
    suspend fun getOrderById(id: String): Result<Order?>
    suspend fun createOrder(order: Order): Result<Order>
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Order>
    suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>>
    suspend fun getOrdersByCustomer(customerId: String): Result<List<Order>>
    suspend fun getOrdersByDateRange(startDate: String, endDate: String): Result<List<Order>>
    suspend fun getTodaysOrders(): Result<List<Order>>
    suspend fun getPendingOrders(): Result<List<Order>>
    suspend fun getOrderStatistics(date: String): Result<OrderStatistics>
    suspend fun addOrderStatusHistory(history: OrderStatusHistory): Result<Unit>
    suspend fun getOrderStatusHistory(orderId: String): Result<List<OrderStatusHistory>>
    suspend fun calculateTotalRevenue(date: String): Result<Double>
    suspend fun getTopCustomers(limit: Int = 10): Result<List<Customer>>
}

class OrderRepositoryImpl(
    private val apiService: org.example.project.data.network.PharmacyApiService
) : OrderRepository {

    override suspend fun getAllOrders(): Result<List<Order>> {
        return try {
            val orders = apiService.getOrders()
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrderById(id: String): Result<Order?> {
        return try {
            val order = apiService.getOrder(id)
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createOrder(order: Order): Result<Order> {
        return try {
            val request = CreateOrderRequest(
                customerId = order.customerId,
                customerName = order.customerName,
                customerPhone = order.customerPhone,
                customerEmail = order.customerEmail,
                items = order.items.map { item ->
                    CreateOrderItemRequest(
                        productId = item.productId,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice,
                        dosageInstructions = item.dosageInstructions
                    )
                },
                paymentMethod = order.paymentMethod,
                deliveryAddress = order.deliveryAddress,
                notes = order.notes,
                prescriptionImageUrl = order.quotationImageUrl,
                deliveryFee = order.deliveryFee,
                discount = order.discount
            )
            val createdOrder = apiService.createOrder(request)
            Result.success(createdOrder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Order> {
        return try {
            val updatedOrder = apiService.updateOrderStatus(orderId, status.value)
            Result.success(updatedOrder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>> {
        return try {
            val orders = apiService.getOrdersByStatus(status.value)
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrdersByCustomer(customerId: String): Result<List<Order>> {
        return try {
            val orders = apiService.getOrdersByCustomer(customerId)
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrdersByDateRange(startDate: String, endDate: String): Result<List<Order>> {
        return try {
            val orders = apiService.getOrdersByDateRange(startDate, endDate)
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTodaysOrders(): Result<List<Order>> {
        return try {
            val orders = apiService.getTodaysOrders()
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPendingOrders(): Result<List<Order>> {
        return getOrdersByStatus(OrderStatus.PENDING)
    }

    override suspend fun getOrderStatistics(date: String): Result<OrderStatistics> {
        return try {
            val stats = apiService.getOrderStatistics(date)
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addOrderStatusHistory(history: OrderStatusHistory): Result<Unit> {
        return try {
            apiService.addOrderStatusHistory(history)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrderStatusHistory(orderId: String): Result<List<OrderStatusHistory>> {
        return try {
            val history = apiService.getOrderStatusHistory(orderId)
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun calculateTotalRevenue(date: String): Result<Double> {
        return try {
            val revenue = apiService.calculateTotalRevenue(date)
            Result.success(revenue)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTopCustomers(limit: Int): Result<List<Customer>> {
        return try {
            val customers = apiService.getTopCustomers(limit)
            Result.success(customers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Request DTOs
@kotlinx.serialization.Serializable
data class CreateOrderRequest(
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String? = null,
    val items: List<CreateOrderItemRequest>,
    val paymentMethod: PaymentMethod,
    val deliveryAddress: Address,
    val notes: String? = null,
    val prescriptionImageUrl: String? = null,
    val deliveryFee: Double = 0.0,
    val discount: Double = 0.0
)

@kotlinx.serialization.Serializable
data class CreateOrderItemRequest(
    val productId: String,
    val quantity: Int,
    val unitPrice: Double,
    val dosageInstructions: String? = null
)

@kotlinx.serialization.Serializable
data class OrderStatistics(
    val totalOrders: Int,
    val totalRevenue: Double,
    val averageOrderValue: Double,
    val pendingOrders: Int,
    val completedOrders: Int,
    val cancelledOrders: Int,
    val topSellingProducts: List<TopSellingProduct>,
    val revenueByPaymentMethod: Map<String, Double>,
    val date: String
)

@kotlinx.serialization.Serializable
data class TopSellingProduct(
    val productId: String,
    val productName: String,
    val quantitySold: Int,
    val revenue: Double
)

// Customer repository interface
interface CustomerRepository {
    suspend fun getAllCustomers(): Result<List<Customer>>
    suspend fun getCustomerById(id: String): Result<Customer?>
    suspend fun createCustomer(customer: Customer): Result<Customer>
    suspend fun updateCustomer(customer: Customer): Result<Customer>
    suspend fun deleteCustomer(id: String): Result<Unit>
    suspend fun searchCustomers(query: String): Result<List<Customer>>
    suspend fun updateRewardPoints(customerId: String, points: Int): Result<Unit>
    suspend fun getCustomerOrderHistory(customerId: String): Result<List<Order>>
}

class CustomerRepositoryImpl(
    private val apiService: org.example.project.data.network.PharmacyApiService
) : CustomerRepository {

    override suspend fun getAllCustomers(): Result<List<Customer>> {
        return try {
            val customers = apiService.getCustomers()
            Result.success(customers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCustomerById(id: String): Result<Customer?> {
        return try {
            val customer = apiService.getCustomer(id)
            Result.success(customer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createCustomer(customer: Customer): Result<Customer> {
        return try {
            val request = CreateCustomerRequest(
                fullName = customer.fullName,
                phone = customer.phone,
                email = customer.email,
                dateOfBirth = customer.dateOfBirth,
                gender = customer.gender,
                addresses = customer.addresses,
                notes = customer.notes
            )
            val createdCustomer = apiService.createCustomer(request)
            Result.success(createdCustomer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCustomer(customer: Customer): Result<Customer> {
        return try {
            val request = UpdateCustomerRequest(
                fullName = customer.fullName,
                phone = customer.phone,
                email = customer.email,
                dateOfBirth = customer.dateOfBirth,
                gender = customer.gender,
                addresses = customer.addresses,
                notes = customer.notes,
                isActive = customer.isActive
            )
            val updatedCustomer = apiService.updateCustomer(customer.id, request)
            Result.success(updatedCustomer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCustomer(id: String): Result<Unit> {
        return try {
            apiService.deleteCustomer(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchCustomers(query: String): Result<List<Customer>> {
        return try {
            val customers = apiService.searchCustomers(query)
            Result.success(customers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRewardPoints(customerId: String, points: Int): Result<Unit> {
        return try {
            apiService.updateRewardPoints(customerId, points)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCustomerOrderHistory(customerId: String): Result<List<Order>> {
        return try {
            val orders = apiService.getCustomerOrderHistory(customerId)
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@kotlinx.serialization.Serializable
data class CreateCustomerRequest(
    val fullName: String,
    val phone: String,
    val email: String? = null,
    val dateOfBirth: String? = null,
    val gender: Gender? = null,
    val addresses: List<Address> = emptyList(),
    val notes: String? = null
)

@kotlinx.serialization.Serializable
data class UpdateCustomerRequest(
    val fullName: String,
    val phone: String,
    val email: String? = null,
    val dateOfBirth: String? = null,
    val gender: Gender? = null,
    val addresses: List<Address> = emptyList(),
    val notes: String? = null,
    val isActive: Boolean = true
)