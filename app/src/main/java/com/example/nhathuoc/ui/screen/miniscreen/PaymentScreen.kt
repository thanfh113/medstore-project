package com.example.nhathuoc.ui.screen.miniscreen

import android.annotation.SuppressLint
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import android.webkit.WebView
import com.example.nhathuoc.ui.theme.GreenTop

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PaymentScreen(
    modifier: Modifier = Modifier,
    orderId: String = "",
    navController: NavController = rememberNavController()
) {
    var paymentStatus by remember { mutableStateOf("PROCESSING") } // PROCESSING, SUCCESS, FAILED
    var paymentUrl by remember { mutableStateOf("https://sandbox.vnpayment.vn/?...")  }
    var showError by remember { mutableStateOf(false) }

    // TODO: Fetch payment URL from backend using orderId

    Box(modifier = modifier.fillMaxSize()) {
        when (paymentStatus) {
            "PROCESSING" -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Surface(modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp), color = GreenTop) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Thanh toán", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // WebView for payment gateway
                    AndroidView(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                webViewClient = PaymentWebViewClient(
                                    onSuccess = {
                                        paymentStatus = "SUCCESS"
                                    },
                                    onError = {
                                        paymentStatus = "FAILED"
                                    }
                                )
                                //loadUrl(paymentUrl)
                                // For demo: load about:blank
                                loadUrl("about:blank")
                            }
                        }
                    )
                }
            }

            "SUCCESS" -> {
                PaymentSuccessScreen(orderId = orderId, navController = navController)
            }

            "FAILED" -> {
                PaymentFailedScreen(
                    onRetry = { paymentStatus = "PROCESSING" },
                    navController = navController
                )
            }
        }
    }
}

private class PaymentWebViewClient(
    val onSuccess: () -> Unit,
    val onError: () -> Unit
) : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
        // Check for success callback URL pattern
        if (url?.contains("app://payment/callback") == true) {
            if (url.contains("status=success") || url.contains("resultCode=0")) {
                onSuccess()
            } else {
                onError()
            }
        }
        super.onPageStarted(view, url, favicon)
    }
}

@Composable
private fun PaymentSuccessScreen(
    orderId: String,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success animation (animated checkmark would be better)
        Surface(
            modifier = Modifier
                .size(80.dp),
            color = Color(0xFFE8F5E9),
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                tint = GreenTop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Thanh toán thành công!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Mã đơn hàng: $orderId", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                navController.navigate("OrderConfirmationScreen/$orderId") {
                    popUpTo("PaymentScreen") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
        ) {
            Text("Xem chi tiết đơn hàng", color = Color.White)
        }
    }
}

@Composable
private fun PaymentFailedScreen(
    onRetry: () -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier
                .size(80.dp),
            color = Color(0xFFFFEBEE),
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            Icon(
                Icons.Filled.Error,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                tint = Color.Red
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Thanh toán thất bại", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Vui lòng thử lại hoặc chọn phương thức thanh toán khác", fontSize = 14.sp, color = Color.Gray, maxLines = 2)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
        ) {
            Text("Thử lại", color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Quay lại", color = GreenTop)
        }
    }
}
