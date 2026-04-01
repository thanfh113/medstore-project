package com.example.nhathuoc.data.repository

import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.remote.ApiService
import com.example.nhathuoc.data.remote.RetrofitClient
import com.example.nhathuoc.util.ApiErrorHandler

/**
 * Repository for category operations
 * Handles hierarchical categories and category filtering
 */
class CategoryRepository {

    private val apiService: ApiService by lazy {
        RetrofitClient.createService<ApiService>()
    }

    // Get all categories with optional parent filter
    suspend fun getCategories(
        parentId: String? = null,
        isActive: Boolean = true
    ): NetworkResult<List<CategoryDto>> {
        return ApiErrorHandler.safeApiCall(
            defaultErrorMessage = "Có lỗi xảy ra khi tải danh mục"
        ) {
            apiService.getCategories(parentId = parentId, isActive = isActive)
        }
    }

    // Get category by ID
    suspend fun getCategoryById(categoryId: String): NetworkResult<CategoryDto> {
        return ApiErrorHandler.safeApiCall(
            defaultErrorMessage = "Có lỗi xảy ra khi tải danh mục"
        ) {
            apiService.getCategoryById(categoryId)
        }
    }

    // Get top-level categories (parentId = null)
    suspend fun getTopLevelCategories(): NetworkResult<List<CategoryDto>> {
        return getCategories(parentId = null, isActive = true)
    }

    // Get subcategories of a parent category
    suspend fun getSubcategories(parentId: String): NetworkResult<List<CategoryDto>> {
        return getCategories(parentId = parentId, isActive = true)
    }

    // Get all active categories (flat list)
    suspend fun getAllActiveCategories(): NetworkResult<List<CategoryDto>> {
        return ApiErrorHandler.safeApiCall(
            defaultErrorMessage = "Có lỗi xảy ra khi tải danh mục"
        ) {
            apiService.getCategories(isActive = true)
        }
    }

    // Helper function to build hierarchical structure
    suspend fun getCategoriesHierarchy(): NetworkResult<List<CategoryDto>> {
        return try {
            // Get all categories first
            when (val result = getAllActiveCategories()) {
                is NetworkResult.Success -> {
                    val allCategories = result.data
                    val topLevel = allCategories.filter { it.parentId == null }

                    // For now, return top-level categories
                    // Later can build full hierarchy if needed
                    NetworkResult.Success(topLevel)
                }
                is NetworkResult.Error -> result
                is NetworkResult.Exception -> result
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}