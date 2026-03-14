package com.example.nhathuoc.data.remote

import com.example.nhathuoc.data.local.SessionManager
import com.example.nhathuoc.data.model.RefreshTokenRequest
import com.example.nhathuoc.data.model.RefreshTokenResponse
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

/**
 * OkHttp Interceptor for automatic token management
 * - Adds Authorization header to requests
 * - Automatically refreshes expired tokens
 * - Handles 401 responses
 */
class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    private val json = Json { ignoreUnknownKeys = true }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth for public endpoints
        if (isPublicEndpoint(originalRequest.url.encodedPath)) {
            return chain.proceed(originalRequest)
        }

        // Add access token to request
        val accessToken = runBlocking { sessionManager.getAccessToken() }
        val request = if (accessToken != null) {
            originalRequest.newBuilder()
                .addHeader(ApiConstants.HEADER_AUTHORIZATION, "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        // Execute request
        val response = chain.proceed(request)

        // Handle 401 Unauthorized - try to refresh token
        if (response.code == 401 && accessToken != null) {
            response.close()

            val refreshToken = runBlocking { sessionManager.getRefreshToken() }
            if (refreshToken != null) {
                val refreshResponse = attemptTokenRefresh(chain, refreshToken)

                if (refreshResponse != null) {
                    // Save new tokens
                    runBlocking {
                        sessionManager.updateTokens(
                            refreshResponse.accessToken,
                            refreshResponse.refreshToken
                        )
                    }

                    // Retry original request with new token
                    val newRequest = originalRequest.newBuilder()
                        .addHeader(ApiConstants.HEADER_AUTHORIZATION, "Bearer ${refreshResponse.accessToken}")
                        .build()

                    return chain.proceed(newRequest)
                } else {
                    // Refresh failed - clear session
                    runBlocking { sessionManager.clearSession() }
                }
            }
        }

        return response
    }

    private fun isPublicEndpoint(path: String): Boolean {
        val publicPaths = listOf(
            ApiConstants.AUTH_LOGIN,
            ApiConstants.AUTH_REGISTER,
            ApiConstants.PRODUCTS,
            ApiConstants.PRODUCTS_FLASH_SALE,
            ApiConstants.PRODUCTS_BEST_SELLERS,
            ApiConstants.BANNERS,
            ApiConstants.PHARMACIES
        )
        return publicPaths.any { publicPath ->
            path.startsWith(publicPath) || path == publicPath
        }
    }

    private fun attemptTokenRefresh(
        chain: Interceptor.Chain,
        refreshToken: String
    ): RefreshTokenResponse? {
        return try {
            val refreshRequest = RefreshTokenRequest(refreshToken)
            val requestBody = json.encodeToString(
                RefreshTokenRequest.serializer(),
                refreshRequest
            ).toRequestBody(ApiConstants.CONTENT_TYPE_JSON.toMediaType())

            val request = Request.Builder()
                .url("${ApiConstants.BASE_URL}${ApiConstants.AUTH_REFRESH}")
                .post(requestBody)
                .addHeader(ApiConstants.HEADER_CONTENT_TYPE, ApiConstants.CONTENT_TYPE_JSON)
                .build()

            val response = chain.proceed(request)

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    json.decodeFromString(
                        RefreshTokenResponse.serializer(),
                        responseBody
                    )
                } else null
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}