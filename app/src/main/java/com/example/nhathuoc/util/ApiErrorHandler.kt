package com.example.nhathuoc.util

import com.example.nhathuoc.data.model.ApiError
import com.example.nhathuoc.data.model.NetworkResult
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Shared utility for handling API errors and network operations
 * Eliminates duplicated error handling across repositories
 */
object ApiErrorHandler {

    /**
     * Parse error message from API response body
     */
    fun parseErrorMessage(errorBody: String?, defaultMessage: String = "Có lỗi xảy ra, vui lòng thử lại"): String {
        return try {
            if (errorBody != null) {
                kotlinx.serialization.json.Json.decodeFromString<ApiError>(errorBody).message
            } else {
                defaultMessage
            }
        } catch (e: Exception) {
            errorBody ?: defaultMessage
        }
    }

    /**
     * Safe API call wrapper that handles all common exceptions
     * Eliminates try-catch duplication in repositories
     */
    suspend fun <T> safeApiCall(
        defaultErrorMessage: String = "Có lỗi xảy ra, vui lòng thử lại",
        apiCall: suspend () -> Response<T>
    ): NetworkResult<T> {
        return try {
            val response = apiCall()

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    NetworkResult.Success(data)
                } ?: NetworkResult.Error(204, "Không có dữ liệu")
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string(), defaultErrorMessage)
                NetworkResult.Error(response.code(), errorMessage)
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    /**
     * Safe API call for nullable responses
     */
    suspend fun <T> safeApiCallNullable(
        defaultErrorMessage: String = "Có lỗi xảy ra, vui lòng thử lại",
        notFoundMessage: String = "Không tìm thấy",
        apiCall: suspend () -> Response<T?>
    ): NetworkResult<T> {
        return try {
            val response = apiCall()

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    NetworkResult.Success(data)
                } ?: NetworkResult.Error(404, notFoundMessage)
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string(), defaultErrorMessage)
                NetworkResult.Error(response.code(), errorMessage)
            }
        } catch (e: HttpException) {
            NetworkResult.Error(e.code(), e.message())
        } catch (e: IOException) {
            NetworkResult.Exception(e)
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}