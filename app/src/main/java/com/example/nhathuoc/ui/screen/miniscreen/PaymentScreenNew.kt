package com.example.nhathuoc.ui.screen.miniscreen

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.PaymentMethod
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.GreenLight
import kotlinx.coroutines.delay

@Composable
fun PaymentScreenNew(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    orderId: String = "ORD-123456",
    amount: Long = 2500000,
    method: String = "VNPAY"
) {
    var paymentStatus by remember { mutableStateOf(PaymentStatus.PROCESSING) }
    var showPaymentURL by remember { mutableStateOf(true) }
    var paymentURL by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // In real app, fetch payment URL from backend
        paymentURL = buildPaymentURL(method, orderId, amount)
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        HeaderPayment(onBackClick = { navController?.popBackStack() })

        // Content based on status
        when (paymentStatus) {
            PaymentStatus.PROCESSING -> {
                if (showPaymentURL && paymentURL.isNotEmpty()) {
                    // WebView for payment gateway
                    PaymentWebView(
                        url = paymentURL,
                        onSuccess = { paymentStatus = PaymentStatus.SUCCESS },
                        onError = { paymentStatus = PaymentStatus.FAILED }
                    )
                } else {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = GreenTop)
                            Text("Đang tải trang thanh toán...", color = Color.Gray)
                        }
                    }
                }
            }
            PaymentStatus.SUCCESS -> {
                PaymentSuccessScreen(
                    orderId = orderId,
                    amount = amount,
                    onContinueClick = {
                        navController?.navigate("OrderConfirmationScreen/$orderId") {
                            popUpTo("PaymentScreenNew") { inclusive = true }
                        }
                    }
                )
            }
            PaymentStatus.FAILED -> {
                PaymentFailedScreen(
                    error = "Thanh toán không thành công. Vui lòng thử lại.",
                    onRetryClick = {
                        paymentStatus = PaymentStatus.PROCESSING
                        showPaymentURL = true
                    },
                    onCancelClick = { navController?.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun HeaderPayment(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(GreenTop, GreenLight)))
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(
                "Thanh toán",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.width(48.dp))
        }
    }
}

@Composable
private fun PaymentWebView(
    url: String,
    onSuccess: () -> Unit,
    onError: () -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)

                        // Check if payment successful by URL (in real app, use callback)
                        if (url?.contains("vnpay") == true && url.contains("vnp_ResponseCode=00")) {
                            onSuccess()
                        } else if (url?.contains("momo") == true) {
                            // MoMo success check
                            onSuccess()
                        }
                    }

                    override fun onReceivedError(view: WebView?, request: android.webkit.WebResourceRequest?, error: android.webkit.WebResourceError?) {
                        super.onReceivedError(view, request, error)
                        onError()
                    }
                }
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                }
                loadUrl(url)
            }
        }
    )
}

@Composable
private fun PaymentSuccessScreen(
    orderId: String,
    amount: Long,
    onContinueClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.weight(0.5f))

        // Success icon
        Surface(
            modifier = Modifier.size(120.dp),
            color = Color(0xFFE3F2FD),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(60.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFF4CAF50)
                )
            }
        }

        // Success message
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Thanh toán thành công",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Text(
                "Đơn hàng của bạn đã được xác nhận",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // Order info
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mã đơn hàng", color = Color.Gray)
                    Text(orderId, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Số tiền", color = Color.Gray)
                    Text(
                        "${String.format("%,d", amount)}đ",
                        fontWeight = FontWeight.Bold,
                        color = GreenTop
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Continue button
        Button(
            onClick = onContinueClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
        ) {
            Text("Xem chi tiết đơn hàng", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PaymentFailedScreen(
    error: String,
    onRetryClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.weight(0.5f))

        // Error icon
        Surface(
            modifier = Modifier.size(120.dp),
            color = Color(0xFFffebee),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(60.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Error,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFFc62828)
                )
            }
        }

        // Error message
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Thanh toán không thành công",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFc62828)
            )
            Text(
                error,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(Modifier.weight(1f))

        // Buttons
        Button(
            onClick = onRetryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
        ) {
            Text("Thử lại", color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
            onClick = onCancelClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Quay lại", color = GreenTop, fontWeight = FontWeight.SemiBold)
        }
    }
}

enum class PaymentStatus {
    PROCESSING,
    SUCCESS,
    FAILED
}

private fun buildPaymentURL(
    method: String,
    orderId: String,
    amount: Long
): String {
    return when (method) {
        "VNPAY" -> {
            // In real app, call backend to get VNPay payment URL
            "https://sandbox.vnpayment.vn/paygate?..." // Placeholder
        }
        "MOMO" -> {
            // In real app, call backend to get MoMo payment URL
            "https://test-payment.momo.vn/..." // Placeholder
        }
        else -> ""
    }
}

@Preview
@Composable
fun PaymentScreenNewPreview() {
    PaymentScreenNew()
}
