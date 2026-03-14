package com.example.nhathuoc.ui.component

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class PromoBannerItem(
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val title: String,
    val subtitle: String,
    val bgColor: Color
)

val defaultPromoItems = listOf(
    PromoBannerItem(
        icon = Icons.Outlined.ShoppingCart,
        iconTint = Color(0xFF2E7D32),
        iconBg = Color(0xFFDCEEFB),
        title = "Khỏe Đẹp Vẹn Toàn",
        subtitle = "Thực phẩm chức năng giảm đến 830.000đ",
        bgColor = Color(0xFFBBDEFB)
    ),
    PromoBannerItem(
        icon = Icons.Outlined.RemoveRedEye,
        iconTint = Color(0xFF2E7D32),
        iconBg = Color(0xFFB3E5FC),
        title = "Chăm sóc mắt Santen",
        subtitle = "Ưu đãi cộng đồng 20.000đ",
        bgColor = Color(0xFFE1F5FE)
    ),
    PromoBannerItem(
        icon = Icons.Outlined.Face,
        iconTint = Color(0xFFC2185B),
        iconBg = Color(0xFFFCE4EC),
        title = "Mã đáo Đẹp Da",
        subtitle = "Giảm đến 35% - Giao nhanh 1h",
        bgColor = Color(0xFFFCE4EC)
    ),
    PromoBannerItem(
        icon = Icons.Outlined.MedicalServices,
        iconTint = Color(0xFF2E7D32),
        iconBg = Color(0xFFC8E6C9),
        title = "Mua trước trả sau",
        subtitle = "0% lãi suất - Đổi trả 30 ngày",
        bgColor = Color(0xFFE8F5E9)
    ),
)

@Composable
fun PromoBannerPager(
    items: List<PromoBannerItem> = defaultPromoItems,
    autoScrollDelay: Long = 3000L,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { items.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(autoScrollDelay)
            val next = (pagerState.currentPage + 1) % items.size
            pagerState.animateScrollToPage(next, animationSpec = tween(600))
        }
    }

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            PromoBannerCard(item = items[page])
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(items.size) { i ->
                val isSelected = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (isSelected) 20.dp else 6.dp, 6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF2E7D32) else Color(0xFFBBBBBB))
                )
            }
        }
    }
}

@Composable
private fun PromoBannerCard(item: PromoBannerItem) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = item.bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2E7D32)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF444444)
                )
            }
            Spacer(Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(item.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.iconTint,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
    }
}