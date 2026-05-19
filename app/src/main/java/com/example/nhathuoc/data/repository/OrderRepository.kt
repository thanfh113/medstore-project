package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getPosOrders(page: Int = 1): NetworkResult<OrderListResponse> {
        return try {
            val response = apiService.getPosOrders(page)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getOrders(status: String? = null, page: Int = 1, limit: Int = 10): NetworkResult<OrderListResponse> {
        return try {
            val response = apiService.getOrders(status, page, limit)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getOrderById(orderId: String): NetworkResult<OrderDto> {
        return try {
            val response = apiService.getOrderById(orderId)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun cancelOrder(orderId: String, reason: String): NetworkResult<MessageResponse> {
        return try {
            val response = apiService.cancelOrder(orderId, CancelOrderRequest(reason))
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun confirmOrderReceived(orderId: String): NetworkResult<MessageResponse> {
        return try {
            val response = apiService.confirmOrderReceived(orderId)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getComplaints(): NetworkResult<List<ComplaintDto>> {
        return try {
            val response = apiService.getComplaints()
            if (response.isSuccessful) NetworkResult.Success(response.body()!!.data) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun createComplaint(request: CreateComplaintRequest): NetworkResult<ComplaintDto> {
        return try {
            val response = apiService.createComplaint(request)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!.data) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun getComplaintById(complaintId: String): NetworkResult<ComplaintDto> {
        return try {
            val response = apiService.getComplaintById(complaintId)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!.data) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun sendComplaintMessage(complaintId: String, message: String): NetworkResult<ComplaintMessageDto> {
        return try {
            val response = apiService.sendComplaintMessage(complaintId, ComplaintMessageRequest(message = message))
            if (response.isSuccessful) NetworkResult.Success(response.body()!!.data) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun addComplaintAttachments(complaintId: String, attachments: List<ComplaintAttachmentInput>): NetworkResult<ComplaintDto> {
        return try {
            val response = apiService.addComplaintAttachments(complaintId, AddComplaintAttachmentsRequest(attachments))
            if (response.isSuccessful) NetworkResult.Success(response.body()!!.data) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun requestRefundForComplaint(complaintId: String): NetworkResult<ComplaintDto> {
        return try {
            val response = apiService.requestRefundForComplaint(complaintId)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!.data) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    private fun parseErrorMessage(errorBody: String?): String = parseErrorBody(errorBody)
}
