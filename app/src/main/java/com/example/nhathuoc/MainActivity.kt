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
        val type  = intent?.getStringExtra(NhathuocFirebaseMessagingService.EXTRA_NOTIFICATION_TYPE)  ?: return
        val refId = intent.getStringExtra(NhathuocFirebaseMessagingService.EXTRA_NOTIFICATION_REF_ID) ?: ""
        if (type.isNotBlank()) {
            NotificationNavBus.navigate(type, refId)
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
            val stored   = prefs.getString(NhathuocFirebaseMessagingService.KEY_FCM_TOKEN, null)
            val isSynced = prefs.getBoolean(NhathuocFirebaseMessagingService.KEY_TOKEN_SYNCED, false)

            // Save locally if new
            if (stored != token) {
                prefs.edit()
                    .putString(NhathuocFirebaseMessagingService.KEY_FCM_TOKEN, token)
                    .putBoolean(NhathuocFirebaseMessagingService.KEY_TOKEN_SYNCED, false)
                    .apply()
            }

            // Register with backend only when user is logged in
            if (!isSynced || stored != token) {
                lifecycleScope.launch {
                    val loggedIn = sessionManager.isLoggedIn.first()
                    if (!loggedIn) return@launch
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
                            prefs.edit()
                                .putBoolean(NhathuocFirebaseMessagingService.KEY_TOKEN_SYNCED, true)
                                .apply()
                            Log.d("FCM", "Token registered with backend")
                        }
                    } catch (e: Exception) {
                        Log.w("FCM", "Token registration failed, will retry next launch", e)
                    }
                }
            }
        }
    }
}
