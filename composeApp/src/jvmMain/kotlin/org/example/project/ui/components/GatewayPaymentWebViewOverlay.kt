package org.example.project.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import java.util.concurrent.atomic.AtomicReference

@Composable
fun GatewayPaymentWebViewOverlay(
    visible: Boolean,
    paymentUrl: String?,
    paymentMethod: String?,
    orderCode: String?,
    onClose: () -> Unit,
    onRefreshPaymentStatus: () -> Unit,
    onLocationChanged: (String) -> Unit
) {
    if (!visible || paymentUrl.isNullOrBlank()) return

    val latestLocationHandler by rememberUpdatedState(onLocationChanged)
    val webViewHost = remember { GatewayWebViewHost() }

    DisposableEffect(webViewHost) {
        webViewHost.setOnLocationChanged { latestLocationHandler(it) }
        onDispose {
            webViewHost.load("about:blank", force = true)
        }
    }

    LaunchedEffect(paymentUrl) {
        webViewHost.load(paymentUrl, force = true)
    }

    DialogWindow(
        onCloseRequest = onClose,
        title = "Thanh toán ${paymentMethod ?: "gateway"}",
        state = rememberDialogState(size = DpSize(1280.dp, 860.dp))
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Trang thanh toán ${paymentMethod ?: "gateway"}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Đơn ${orderCode ?: "-"} sẽ tự động đóng khi thanh toán hoàn tất.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = {
                            webViewHost.reload()
                        }) {
                            Text("Tải lại")
                        }
                        OutlinedButton(onClick = onRefreshPaymentStatus) {
                            Text("Kiểm tra")
                        }
                        Button(onClick = onClose) {
                            Text("Đóng")
                        }
                    }
                }

                HorizontalDivider()

                SwingPanel(
                    factory = { webViewHost.panel },
                    update = {
                        webViewHost.setOnLocationChanged { latestLocationHandler(it) }
                        webViewHost.load(paymentUrl)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nếu trang quay về pos-return, ứng dụng sẽ đối soát giao dịch ngay.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onRefreshPaymentStatus) {
                            Text("Cập nhật")
                        }
                        Button(onClick = onClose) {
                            Text("Ẩn trang")
                        }
                    }
                }
            }
        }
    }
}

private class GatewayWebViewHost {
    val panel = JFXPanel()

    private val locationHandler = AtomicReference<(String) -> Unit>({})

    @Volatile
    private var webEngine: WebEngine? = null

    @Volatile
    private var lastLoadedUrl: String? = null

    @Volatile
    private var pendingUrl: String? = null

    init {
        Platform.setImplicitExit(false)
        Platform.runLater {
            val webView = WebView()
            webView.engine.isJavaScriptEnabled = true
            webView.engine.loadWorker.stateProperty().addListener { _, _, newValue ->
                if (newValue == Worker.State.SUCCEEDED) {
                    injectAppFont(webView.engine)
                }
            }
            webView.engine.locationProperty().addListener { _, _, newValue ->
                if (!newValue.isNullOrBlank()) {
                    locationHandler.get().invoke(newValue)
                }
            }
            webEngine = webView.engine
            panel.scene = Scene(StackPane(webView))
            pendingUrl?.let { queuedUrl ->
                webView.engine.load(queuedUrl)
                lastLoadedUrl = queuedUrl
                pendingUrl = null
            }
        }
    }

    fun setOnLocationChanged(listener: (String) -> Unit) {
        locationHandler.set(listener)
    }

    fun load(url: String, force: Boolean = false) {
        if (!force && url == lastLoadedUrl) return

        val engine = webEngine
        if (engine == null) {
            pendingUrl = url
            lastLoadedUrl = url
            return
        }

        lastLoadedUrl = url
        Platform.runLater {
            engine.load(url)
        }
    }

    fun reload() {
        val engine = webEngine ?: return
        Platform.runLater {
            engine.reload()
        }
    }

    private fun injectAppFont(engine: WebEngine) {
        runCatching {
            engine.executeScript(
                """
                (function() {
                  if (document.getElementById('medstore-font-style')) return;
                  var style = document.createElement('style');
                  style.id = 'medstore-font-style';
                  style.innerHTML = 'html, body, button, input, select, textarea, div, span, p, a, td, th { font-family: "Segoe UI", "Arial", "Tahoma", sans-serif !important; }';
                  document.head.appendChild(style);
                })();
                """.trimIndent()
            )
        }
    }
}
