package com.example.nhathuoc.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * SessionManager for handling user authentication state and tokens
 * Uses DataStore for secure local storage
 */

// DataStore extension for Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nhathuoc_session")

class SessionManager(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_FULL_NAME_KEY = stringPreferencesKey("user_full_name")
        private val USER_PHONE_KEY = stringPreferencesKey("user_phone")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
    }

    // Save authentication data
    suspend fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        userId: String, // Changed to String for UUID
        fullName: String,
        phone: String,
        email: String,
        role: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
            preferences[USER_ID_KEY] = userId // Store UUID directly as String
            preferences[USER_FULL_NAME_KEY] = fullName
            preferences[USER_PHONE_KEY] = phone
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_ROLE_KEY] = role
        }
    }

    // Get access token
    suspend fun getAccessToken(): String? {
        return context.dataStore.data.first()[ACCESS_TOKEN_KEY]
    }

    // Get refresh token
    suspend fun getRefreshToken(): String? {
        return context.dataStore.data.first()[REFRESH_TOKEN_KEY]
    }

    // Get user ID
    suspend fun getUserId(): String? {
        return context.dataStore.data.first()[USER_ID_KEY]
    }

    // Get user info flows for reactive UI
    val userFullName: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_FULL_NAME_KEY] }

    val userPhone: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_PHONE_KEY] }

    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_EMAIL_KEY] }

    val userRole: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_ROLE_KEY] }

    // Check if user is logged in
    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ACCESS_TOKEN_KEY] != null &&
            preferences[REFRESH_TOKEN_KEY] != null
        }

    // Update tokens (for refresh token flow)
    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    // Update user info
    suspend fun updateUserInfo(
        fullName: String,
        phone: String,
        email: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[USER_FULL_NAME_KEY] = fullName
            preferences[USER_PHONE_KEY] = phone
            preferences[USER_EMAIL_KEY] = email
        }
    }

    // Clear all session data (logout)
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Get complete user session data
    suspend fun getSessionData(): SessionData? {
        val preferences = context.dataStore.data.first()
        val accessToken = preferences[ACCESS_TOKEN_KEY]
        val refreshToken = preferences[REFRESH_TOKEN_KEY]
        val userId = preferences[USER_ID_KEY]

        return if (accessToken != null && refreshToken != null && userId != null) {
            SessionData(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = userId,
                fullName = preferences[USER_FULL_NAME_KEY] ?: "",
                phone = preferences[USER_PHONE_KEY] ?: "",
                email = preferences[USER_EMAIL_KEY] ?: "",
                role = preferences[USER_ROLE_KEY] ?: "user"
            )
        } else null
    }
}

// Data class for complete session information
data class SessionData(
    val accessToken: String,
    val refreshToken: String,
    val userId: String, // Changed to String for UUID
    val fullName: String,
    val phone: String,
    val email: String,
    val role: String
)