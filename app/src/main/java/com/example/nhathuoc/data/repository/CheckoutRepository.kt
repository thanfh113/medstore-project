package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import com.example.nhathuoc.data.remote.RetrofitClient
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository for checkout operations
 */
class CheckoutRepository {
    private val apiService: ApiService by lazy {
        RetrofitClient.createService<ApiService>()
    }

    // Preview checkout (calculate totals)
    suspend fun previewCheckout(
        addressId: String,
        pickupType: String,
        paymentMethod: String,
        rewardPointsToUse: Int = 0,
        promoCode: String? = null
    ): NetworkResult<CheckoutPreviewDto> {
        return try {
            // This would be called as an API endpoint
            // For now, return mock data
            val mockResponse = CheckoutPreviewDto(
                orderId = "ord-temp",
                items = emptyList(),
                pricing = PricingBreakdown(
                    subtotal = 0.0,
                    discount5percent = 0.0,
                    shipping = 0.0,
                    tax = 0.0,
                    rewardPointsDiscount = 0.0,
                    finalTotal = 0.0
                )
            )
            NetworkResult.Success(mockResponse)
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message ?: "HTTP Error")
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    // Create order
    suspend fun createOrder(
        addressId: String,
        pickupType: String,
        paymentMethod: String,
        rewardPointsToUse: Int = 0,
        promoCode: String? = null,
        notes: String? = null
    ): NetworkResult<OrderDto> {
        return try {
            // This would call the actual API
            val mockResponse = OrderDto(
                id = "ord-123456",
                userId = "",
                items = emptyList(),
                totalAmount = 0.0,
                status = OrderStatus.PENDING,
                paymentStatus = PaymentStatus.UNPAID,
                paymentMethod = PaymentMethod.COD,
                shippingAddress = null,
                shippingType = PickupType.DELIVERY,
                notes = notes
            )
            NetworkResult.Success(mockResponse)
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message ?: "HTTP Error")
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    // Get order by ID
    suspend fun getOrderById(orderId: String): NetworkResult<OrderDto> {
        return try {
            val mockResponse = OrderDto(
                id = orderId,
                userId = "",
                items = listOf(
                    OrderItemDto("1", "Vitamin D3", 2, 150000.0),
                    OrderItemDto("2", "Muối rửa mũi", 1, 170000.0)
                ),
                totalAmount = 470000.0,
                status = OrderStatus.CONFIRMED,
                paymentStatus = PaymentStatus.COMPLETED,
                paymentMethod = PaymentMethod.VNPAY,
                shippingAddress = UserAddressDto("addr-1", "Nguyễn Văn A", "0987654321", "123 Street", "", ""),
                shippingType = PickupType.DELIVERY
            )
            NetworkResult.Success(mockResponse)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    // Get user addresses
    suspend fun getUserAddresses(): NetworkResult<List<UserAddressDto>> {
        return try {
            val mockAddresses = listOf(
                UserAddressDto(
                    id = "addr-1",
                    fullName = "Nguyễn Văn A",
                    phone = "0987654321",
                    address = "123 Đường Lê Lợi, Quận 1",
                    district = "Quận 1",
                    city = "TP.HCM",
                    type = AddressType.HOME,
                    isDefault = true
                )
            )
            NetworkResult.Success(mockAddresses)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    // Add user address
    suspend fun addUserAddress(
        fullName: String,
        phone: String,
        address: String,
        district: String,
        city: String,
        type: AddressType = AddressType.HOME
    ): NetworkResult<UserAddressDto> {
        return try {
            val newAddress = UserAddressDto(
                id = "addr-${System.currentTimeMillis()}",
                fullName = fullName,
                phone = phone,
                address = address,
                district = district,
                city = city,
                type = type,
                isDefault = false
            )
            NetworkResult.Success(newAddress)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    // Delete user address
    suspend fun deleteUserAddress(addressId: String): NetworkResult<String> {
        return try {
            NetworkResult.Success("Address deleted successfully")
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    // Set default address
    suspend fun setDefaultAddress(addressId: String): NetworkResult<UserAddressDto> {
        return try {
            val mockAddress = UserAddressDto(
                id = addressId,
                fullName = "User",
                phone = "123456",
                address = "Address",
                district = "",
                city = "",
                type = AddressType.HOME,
                isDefault = true
            )
            NetworkResult.Success(mockAddress)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}
