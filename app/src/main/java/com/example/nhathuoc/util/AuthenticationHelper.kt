package com.example.nhathuoc.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.example.nhathuoc.data.local.SessionManager

/**
 * Authentication helper for checking login status and navigating to login
 */
class AuthenticationHelper(private val sessionManager: SessionManager) {

    /**
     * Check if user is logged in
     */
    suspend fun isLoggedIn(): Boolean {
        return sessionManager.getAccessToken() != null
    }

    /**
     * Execute action if logged in, otherwise navigate to login
     */
    suspend fun requireAuth(
        navController: NavController,
        onAuthenticated: () -> Unit
    ) {
        if (isLoggedIn()) {
            onAuthenticated()
        } else {
            navController.navigate("LoginScreen")
        }
    }
}

/**
 * Composable helper for authentication checks
 */
@Composable
fun rememberAuthenticationHelper(context: Context): AuthenticationHelper {
    val sessionManager = remember { SessionManager(context) }
    return remember { AuthenticationHelper(sessionManager) }
}

/**
 * Composable that provides authentication check with reactive state
 */
@Composable
fun WithAuthentication(
    context: Context,
    navController: NavController,
    onAuthenticated: @Composable () -> Unit,
    onUnauthenticated: @Composable () -> Unit = {
        // Default: navigate to login
        navController.navigate("LoginScreen")
    }
) {
    val sessionManager = remember { SessionManager(context) }
    val isLoggedIn = sessionManager.isLoggedIn.collectAsState(initial = false)

    if (isLoggedIn.value) {
        onAuthenticated()
    } else {
        onUnauthenticated()
    }
}

/**
 * Execute an action that requires authentication
 */
@Composable
fun AuthenticatedAction(
    context: Context,
    navController: NavController,
    onClick: () -> Unit
): () -> Unit {
    val sessionManager = remember { SessionManager(context) }
    val isLoggedIn = sessionManager.isLoggedIn.collectAsState(initial = false)

    return {
        if (isLoggedIn.value) {
            onClick()
        } else {
            navController.navigate("LoginScreen")
        }
    }
}