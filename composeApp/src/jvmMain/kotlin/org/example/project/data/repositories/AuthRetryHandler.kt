package org.example.project.data.repositories

interface AuthRetryHandler {
    suspend fun refreshAccessToken(): String?
    fun onAuthFailed()
}

