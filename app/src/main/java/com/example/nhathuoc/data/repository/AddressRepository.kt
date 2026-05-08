package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getUserAddresses(): NetworkResult<List<UserAddress>> {
        return try {
            val response = apiService.getUserAddresses()
            if (response.isSuccessful) NetworkResult.Success(response.body() ?: emptyList()) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun addAddress(request: AddAddressRequest): NetworkResult<AddAddressResponse> {
        return try {
            val response = apiService.addAddress(request)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun updateAddress(id: String, request: AddAddressRequest): NetworkResult<AddAddressResponse> {
        return try {
            val response = apiService.updateAddress(id, request)
            if (response.isSuccessful) NetworkResult.Success(response.body()!!) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun deleteAddress(id: String): NetworkResult<Unit> {
        return try {
            val response = apiService.deleteAddress(id)
            if (response.isSuccessful) NetworkResult.Success(Unit) else NetworkResult.Error(response.code(), parseErrorMessage(response.errorBody()?.string()))
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            if (errorBody != null) kotlinx.serialization.json.Json.decodeFromString<ApiError>(errorBody).message else "Co loi xay ra, vui long thu lai"
        } catch (e: Exception) {
            errorBody ?: "Co loi xay ra, vui long thu lai"
        }
    }
}
