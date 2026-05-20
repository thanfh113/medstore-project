package org.example.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.example.project.data.models.Product
import org.example.project.data.models.ProductCertificate
import java.awt.Desktop
import java.net.URI

@Composable
fun ProductDetailDialog(
    product: Product,
    categoryName: String? = null,
    onDismiss: () -> Unit,
    onAddToCart: (() -> Unit)? = null,
    formatVnd: (Double) -> String
) {
    var selectedImageUrl by remember(product.id, product.images) {
        mutableStateOf(product.images.firstOrNull()?.url)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text(
                    categoryName?.takeIf { it.isNotBlank() } ?: "Chưa gán danh mục",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedImageUrl != null) {
                    AsyncImage(
                        model = selectedImageUrl,
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    if (product.images.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            product.images.forEach { image ->
                                val isSelected = image.url == selectedImageUrl
                                AsyncImage(
                                    model = image.url,
                                    contentDescription = product.name,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { selectedImageUrl = image.url },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            "Chưa có ảnh sản phẩm",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip("Giá bán", formatVnd(product.price))
                    InfoChip("Tồn kho", product.stockQuantity.toString())
                    InfoChip("Phân loại", product.riskClassification.displayName)
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailRow("SKU", product.sku ?: "Chưa có")
                        DetailRow("Đơn vị", product.unit)
                        DetailRow("Hãng", product.manufacturer.ifBlank { "Chưa có" })
                        DetailRow("Xuất xứ", product.origin.ifBlank { "Chưa có" })
                        DetailRow("Số lưu hành", product.registrationNumber ?: "Chưa có")
                        DetailRow("Giá gốc", product.originalPrice?.let(formatVnd) ?: "Chưa có")
                    }
                }

                if (product.description.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Mô tả", fontWeight = FontWeight.SemiBold)
                        Text(
                            product.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (product.certificates.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Giấy tờ & chứng minh", fontWeight = FontWeight.SemiBold)
                            product.registrationNumber?.takeIf { it.isNotBlank() }?.let {
                                DetailRow("Số lưu hành", it)
                            }
                            product.certificates.forEach { certificate ->
                                CertificatePreviewItem(certificate)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onAddToCart != null) {
                    Button(
                        onClick = {
                            onAddToCart()
                            onDismiss()
                        }
                    ) {
                        Text("Thêm vào hóa đơn")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Đóng")
                }
            }
        }
    )
}

@Composable
private fun CertificatePreviewItem(certificate: ProductCertificate) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(certificate.name.ifBlank { "Giấy tờ sản phẩm" }, fontWeight = FontWeight.Medium)
                Text(
                    "${certificate.fileType.ifBlank { "FILE" }} • ${certificate.issuer ?: "Chưa có đơn vị cấp"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = { openExternalUrl(certificate.fileUrl) }) {
                Text("Mở")
            }
        }
    }
}

private fun openExternalUrl(url: String) {
    runCatching {
        if (url.isNotBlank() && Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI(url))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Surface(
        color = Color(0xFFE8F5E9),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        }
    }
}
