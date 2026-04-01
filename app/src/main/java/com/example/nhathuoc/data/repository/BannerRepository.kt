package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import com.example.nhathuoc.data.remote.RetrofitClient
import com.example.nhathuoc.util.ApiErrorHandler

/**
 * Repository for banner operations
 * Handles banner loading and filtering by position
 */
class BannerRepository {

    private val apiService: ApiService by lazy {
        RetrofitClient.createService<ApiService>()
    }

    // Get banners with optional position filter
    suspend fun getBanners(position: String? = null): NetworkResult<List<BannerDto>> {
        return ApiErrorHandler.safeApiCall(
            defaultErrorMessage = "Có lỗi xảy ra khi tải banner"
        ) {
            apiService.getBanners(position = position)
        }
    }

    // Get banner by ID
    suspend fun getBannerById(bannerId: String): NetworkResult<BannerDto> {
        return ApiErrorHandler.safeApiCall(
            defaultErrorMessage = "Có lỗi xảy ra khi tải banner"
        ) {
            apiService.getBannerById(bannerId)
        }
    }

    // Get active banners only
    suspend fun getActiveBanners(position: String? = null): NetworkResult<List<BannerDto>> {
        return when (val result = getBanners(position)) {
            is NetworkResult.Success -> {
                val activeBanners = result.data.filter { it.isActive }
                NetworkResult.Success(activeBanners)
            }
            is NetworkResult.Error -> result
            is NetworkResult.Exception -> result
        }
    }
}