package com.example.nhathuoc.ui.screen.miniscreen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassFull
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.nhathuoc.data.model.PaymentStatusDto
import com.example.nhathuoc.ui.theme.GreenTop

// ── Deep-link schemes that should be forwarded to native apps ───────────────
private val NATIVE_APP_SCHEMES = listOf(
    "momo",
    "zalopay",
    "intent"
)

private fun isPaymentReturnUrl(url: String): Boolean {
    return url.startsWith("nhathuoc://payment-return") ||
        url.startsWith("app://payment/callback")
}

private fun openNativePaymentApp(context: Context, targetUrl: String): Boolean {
    val scheme = Uri.parse(targetUrl).scheme?.lowercase() ?: return false
    if (NATIVE_APP_SCHEMES.none { scheme.startsWith(it) }) {
        return false
    }

    return try {
        val intent = if (scheme == "intent") {
            Intent.parseUri(targetUrl, Intent.URI_INTENT_SCHEME)
        } else {
            Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentWebViewScreen(
    title: String,
    url: String,
    orderId: String,
    paymentStatus: PaymentStatusDto?,
    onClose: () -> Unit,
    onCheckStatus: () -> Unit,
    onReturnUrlDetected: () -> Unit
) {
    BackHandler(onBack = onClose)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = GreenTop
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Đóng",
                            tint = GreenTop
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onCheckStatus) {
                        Text("Kiểm tra", color = GreenTop, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Status banner ───────────────────────────────────────────
            PaymentStatusBanner(orderId = orderId, paymentStatus = paymentStatus)

            // ── WebView ─────────────────────────────────────────────────
            GatewayWebView(
                modifier = Modifier.fillMaxSize(),
                url = url,
                onReturnUrlDetected = onReturnUrlDetected
            )
        }
    }
}

// ── Status banner ──────────────────────────────────────────────────────────
@Composable
private fun PaymentStatusBanner(orderId: String, paymentStatus: PaymentStatusDto?) {
    val (bgColor, icon, label) = when (paymentStatus?.status) {
        "COMPLETED" -> Triple(
            Color(0xFFE8F5E9),
            Icons.Filled.CheckCircle,
            "Đã thanh toán. Đơn hàng đang được xử lý."
        )
        "PENDING" -> Triple(
            Color(0xFFFFF8E1),
            Icons.Filled.HourglassFull,
            "Đang chờ xác nhận thanh toán từ cổng thanh toán."
        )
        else -> Triple(
            Color(0xFFF5F7FA),
            Icons.Filled.Info,
            "Hoàn tất thanh toán rồi quay lại ứng dụng."
        )
    }

    val iconTint = when (paymentStatus?.status) {
        "COMPLETED" -> Color(0xFF2E7D32)
        "PENDING" -> Color(0xFFF57F17)
        else -> Color(0xFF78909C)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Đơn hàng: $orderId",
                color = GreenTop,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1
            )
            Text(label, color = Color(0xFF555555), fontSize = 12.sp)
        }
    }
}

// WebView with MoMo/ZaloPay native app support.
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun GatewayWebView(
    modifier: Modifier = Modifier,
    url: String,
    onReturnUrlDetected: () -> Unit
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.setSupportMultipleWindows(false)
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                webChromeClient = WebChromeClient()

                webViewClient = object : WebViewClient() {

                    private fun handleUrl(targetUrl: String, view: WebView?): Boolean {
                        // ── Callback deep link → payment completed ──────
                        if (isPaymentReturnUrl(targetUrl)) {
                            onReturnUrlDetected()
                            return true
                        }

                        // ── Native app scheme (momo://, zalopay://, etc.) ─
                        val scheme = Uri.parse(targetUrl).scheme?.lowercase() ?: ""
                        if (NATIVE_APP_SCHEMES.any { scheme.startsWith(it) }) {
                            if (openNativePaymentApp(context, targetUrl)) {
                                return true
                            }
                            try {
                                val intent = if (scheme == "intent") {
                                    Intent.parseUri(targetUrl, Intent.URI_INTENT_SCHEME)
                                } else {
                                    Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                }
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                                true
                            } catch (e: Exception) {
                                // App not installed — let WebView handle it
                                false
                            }
                        }

                        return false
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val target = request?.url?.toString().orEmpty()
                        return handleUrl(target, view)
                    }

                    @Deprecated("Needed for Android < 21")
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        return handleUrl(url.orEmpty(), view)
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        if (url?.let(::isPaymentReturnUrl) == true) {
                            onReturnUrlDetected()
                        }
                    }
                }
                if (!openNativePaymentApp(context, url)) {
                    loadUrl(url)
                }
            }
        },
        update = { webView ->
            if (webView.url != url && !openNativePaymentApp(context, url)) {
                webView.loadUrl(url)
            }
        }
    )
}
