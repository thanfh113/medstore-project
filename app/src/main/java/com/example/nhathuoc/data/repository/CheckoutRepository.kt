package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import com.example.nhathuoc.util.ApiErrorHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckoutRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun checkout(request: CheckoutRequest): NetworkResult<ApiResponse<CheckoutOrderSummaryDto>> {
        return ApiErrorHandler.safeApiCall {
            apiService.checkout(request)
        }
    }

    suspend fun initMomoPayment(request: PaymentInitRequest): NetworkResult<DataMessageResponse<PaymentInitData>> {
        return ApiErrorHandler.safeApiCall {
            apiService.initMomoPayment(request)
        }
    }

    suspend fun initZaloPayPayment(request: PaymentInitRequest): NetworkResult<DataMessageResponse<PaymentInitData>> {
        return ApiErrorHandler.safeApiCall {
            apiService.initZaloPayPayment(request)
        }
    }

    suspend fun createCodPayment(request: CodPaymentRequest): NetworkResult<DataMessageResponse<CodPaymentData>> {
        return ApiErrorHandler.safeApiCall {
            apiService.createCodPayment(request)
        }
    }

    suspend fun getPaymentStatus(orderId: String): NetworkResult<DataMessageResponse<PaymentStatusDto>> {
        return ApiErrorHandler.safeApiCall {
            apiService.getPaymentStatus(orderId)
        }
    }
}
