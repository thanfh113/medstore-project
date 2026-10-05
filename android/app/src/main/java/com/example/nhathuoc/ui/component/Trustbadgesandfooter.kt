package com.example.nhathuoc.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


data class TrustBadge(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

val defaultTrustBadges = listOf(
    TrustBadge(Icons.Outlined.Shield,        "Vật tư y tế chính hãng", "đa dạng và chuyên sâu"),
    TrustBadge(Icons.Outlined.Replay,        "Đổi trả trong 30 ngày",  "kể từ ngày mua hàng"),
    TrustBadge(Icons.Outlined.ThumbUp,       "Cam kết 100%",           "chất lượng sản phẩm"),
    TrustBadge(Icons.Outlined.LocalShipping, "Miễn phí vận chuyển",    "theo chính sách giao hàng"),
)

@Composable
fun TrustBadgesGrid(
    badges: List<TrustBadge> = defaultTrustBadges,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            badges.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { badge ->
                        TrustBadgeCell(badge = badge, modifier = Modifier.weight(1f))
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TrustBadgeCell(
    badge: TrustBadge,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = badge.icon,
            contentDescription = badge.title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = badge.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = badge.subtitle,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HomeFooter(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(color = Color(0xFFEEEEEE))
        Spacer(Modifier.height(16.dp))
        Text(
            text = "©2026 Công ty Tư Y Tế Hà Tiến Thành",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Số ĐKKD 0315275368 cấp ngày 17/09/2019 tại\nSở Kế hoạch Đầu tư Hà Nội",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
        Spacer(Modifier.height(6.dp))
    }
}