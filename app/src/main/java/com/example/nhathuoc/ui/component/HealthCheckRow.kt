package com.example.nhathuoc.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GreenTop   = Color(0xFF2E7D32)
private val GreenLight = Color(0xFF2E7D32)

data class HealthCheckItem(
    val icon: ImageVector,
    val title: String
)

val defaultHealthChecks = listOf(
    HealthCheckItem(Icons.Outlined.MonitorHeart,  "Hướng dẫn dùng\nmáy đo huyết áp"),
    HealthCheckItem(Icons.Outlined.MedicalServices, "Sử dụng dụng cụ\ny tế đúng quy trình"),
    HealthCheckItem(Icons.Outlined.HealthAndSafety, "Cách băng bó\nvết thương cơ bản"),
    HealthCheckItem(Icons.Outlined.Masks,         "Đeo khẩu trang\nN95 đúng chuẩn"),
)

@Composable
fun HealthCheckRow(
    items: List<HealthCheckItem> = defaultHealthChecks,
    onItemClick: (HealthCheckItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(GreenTop, GreenLight)),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hướng dẫn sử dụng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Sử dụng đúng cách để\nđảm bảo hiệu quả và an toàn!",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 17.sp
                )
            }
            Icon(
                imageVector = Icons.Outlined.HealthAndSafety,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(Modifier.height(14.dp))

        // Horizontal scrollable check cards
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items.forEach { item ->
                HealthCheckCard(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
private fun HealthCheckCard(
    item: HealthCheckItem,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = Modifier.width(160.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFEEF2FF),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = GreenTop,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = item.title,
                fontSize = 12.sp,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.SemiBold,
                lineHeight = 16.sp,
                minLines = 2
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Xem ngay",
                color = GreenTop,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}