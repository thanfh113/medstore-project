package com.example.nhathuoc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.nhathuoc.data.local.SessionManager
import com.example.nhathuoc.data.model.PushTokenRequest
import com.example.nhathuoc.data.remote.ApiService
import com.example.nhathuoc.navigation.AppnavHost
import com.example.nhathuoc.ui.theme.NhathuocTheme
import com.example.nhathuoc.util.DismissKeyboard
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

object PaymentReturnBus {
    private val _events = MutableSharedFlow<Uri>(replay = 1, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun notify(uri: Uri) {
        _events.tryEmit(uri)
    }
}

// Carries notification tap data to the nav host
object NotificationNavBus {
    private val _events = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun navigate(type: String, refId: String) {
        _events.tryEmit(type to refId)
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var apiService: ApiService
    @Inject lateinit var sessionManager: SessionManager

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handlePaymentReturnIntent(intent)
        handleNotificationIntent(intent)
        requestNotificationPermissionIfNeeded()
        registerFcmToken()
        enableEdgeToEdge()

        setContent {
            NhathuocTheme {
                DismissKeyboard {
                    val navController = rememberNavController()
                    AppnavHost(navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePaymentReturnIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handlePaymentReturnIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "nhathuoc" && uri.host == "payment-return") {
            PaymentReturnBus.notify(uri)
        }
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val type   = intent?.getStringExtra(NhathuocFirebaseMessagingService.EXTRA_NOTIFICATION_TYPE)  ?: return
        val refId  = intent.getStringExtra(NhathuocFirebaseMessagingService.EXTRA_NOTIFICATION_REF_ID) ?: ""
        val notifId = intent.getStringExtra(NhathuocFirebaseMessagingService.EXTRA_NOTIFICATION_ID) ?: ""
        if (type.isNotBlank()) {
            NotificationNavBus.navigate(type, refId)
            if (notifId.isNotBlank()) {
                lifecycleScope.launch {
                    try { apiService.markNotificationAsRead(notifId) } catch (_: Exception) {}
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun registerFcmToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val prefs = getSharedPreferences(
                NhathuocFirebaseMessagingService.FCM_PREFS, MODE_PRIVATE
            )
            val stored = prefs.getString(NhathuocFirebaseMessagingService.KEY_FCM_TOKEN, null)
            if (stored != token) {
                prefs.edit()
                    .putString(NhathuocFirebaseMessagingService.KEY_FCM_TOKEN, token)
                    .apply()
            }

            // Re-register token on every login (handles account switching without app restart)
            lifecycleScope.launch {
                var wasLoggedIn = false
                sessionManager.isLoggedIn.collect { isLoggedIn ->
                    if (isLoggedIn && !wasLoggedIn) {
                        try {
                            val response = apiService.registerPushToken(
                                PushTokenRequest(
                                    fcmToken = token,
                                    platform = "ANDROID",
                                    deviceId = android.provider.Settings.Secure.getString(
                                        contentResolver,
                                        android.provider.Settings.Secure.ANDROID_ID
                                    )
                                )
                            )
                            if (response.isSuccessful) {
                                Log.d("FCM", "Token registered for current account")
                            }
                        } catch (e: Exception) {
                            Log.w("FCM", "Token registration failed: ${e.message}")
                        }
                    }
                    wasLoggedIn = isLoggedIn
                }
            }
        }
    }
}
