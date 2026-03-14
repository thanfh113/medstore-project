package com.example.nhathuoc.data.remote

import com.example.nhathuoc.data.local.SessionManager
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client with OkHttp configuration
 * Includes AuthInterceptor for automatic token management
 */
object RetrofitClient {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private var sessionManager: SessionManager? = null
    private var retrofit: Retrofit? = null

    fun initialize(sessionManager: SessionManager) {
        this.sessionManager = sessionManager
        retrofit = createRetrofit()
    }

    private fun createRetrofit(): Retrofit {
        val okHttpClient = createOkHttpClient()

        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory(ApiConstants.CONTENT_TYPE_JSON.toMediaType())
            )
            .build()
    }

    private fun createOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(ApiConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)

        // Add logging interceptor in debug mode
        if (com.example.nhathuoc.BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        // Add authentication interceptor
        sessionManager?.let { manager ->
            builder.addInterceptor(AuthInterceptor(manager))
        }

        return builder.build()
    }

    fun getRetrofit(): Retrofit {
        return retrofit ?: throw IllegalStateException(
            "RetrofitClient must be initialized with SessionManager before use"
        )
    }

    inline fun <reified T> createService(): T {
        return getRetrofit().create(T::class.java)
    }

    // Recreate Retrofit instance (useful for testing or session changes)
    fun recreate(sessionManager: SessionManager) {
        this.sessionManager = sessionManager
        retrofit = createRetrofit()
    }
}