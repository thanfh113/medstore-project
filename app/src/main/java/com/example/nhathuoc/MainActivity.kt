package com.example.nhathuoc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.nhathuoc.navigation.AppnavHost
import com.example.nhathuoc.ui.theme.NhathuocTheme
import com.example.nhathuoc.util.DismissKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object PaymentReturnBus {
    private val _events = MutableSharedFlow<Uri>(replay = 1, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun notify(uri: Uri) {
        _events.tryEmit(uri)
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handlePaymentReturnIntent(intent)
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
    }

    private fun handlePaymentReturnIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "nhathuoc" && uri.host == "payment-return") {
            PaymentReturnBus.notify(uri)
        }
    }
}
