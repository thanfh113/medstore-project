package com.example.nhathuoc.ui.component

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nhathuoc.data.model.BannerDto
import com.example.nhathuoc.data.remote.BackendUrlResolver
import kotlinx.coroutines.delay

data class PromoBannerItem(
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val title: String,
    val subtitle: String,
    val bgColor: Color,
    val imageUrl: String? = null,
    val linkUrl: String? = null
)

val defaultPromoItems = listOf(
    PromoBannerItem(
        icon = Icons.Outlined.ShoppingCart,
        iconTint = Color(0xFF2E7D32),
        iconBg = Color(0xFFDCEEFB),
        title = "Vật tư y tế chính hãng",
        subtitle = "Đặt hàng nhanh, tư vấn chuyên môn khi cần",
        bgColor = Color(0xFFBBDEFB)
    ),
    PromoBannerItem(
        icon = Icons.Outlined.RemoveRedEye,
        iconTint = Color(0xFF2E7D32),
        iconBg = Color(0xFFB3E5FC),
        title = "Theo dõi sức khỏe tại nhà",
        subtitle = "Máy đo huyết áp, SpO2, nhiệt kế y tế",
        bgColor = Color(0xFFE1F5FE)
    ),
    PromoBannerItem(
        icon = Icons.Outlined.Face,
        iconTint = Color(0xFFC2185B),
        iconBg = Color(0xFFFCE4EC),
        title = "Đổi điểm lấy voucher",
        subtitle = "Tích điểm sau mua hàng, đổi ưu đãi khi thanh toán",
        bgColor = Color(0xFFFCE4EC)
    ),
    PromoBannerItem(
        icon = Icons.Outlined.MedicalServices,
        iconTint = Color(0xFF2E7D32),
        iconBg = Color(0xFFC8E6C9),
        title = "Tư vấn vật tư chuyên môn",
        subtitle = "Nhân viên hỗ trợ chọn đúng sản phẩm theo nhu cầu",
        bgColor = Color(0xFFE8F5E9)
    )
)

fun BannerDto.toPromoBannerItem(): PromoBannerItem {
    return PromoBannerItem(
        icon = Icons.Outlined.Campaign,
        iconTint = Color(0xFF2E7D32),
        iconBg = Color(0xFFE8F5E9),
        title = title?.takeIf { it.isNotBlank() } ?: "Ưu đãi vật tư y tế",
        subtitle = description?.takeIf { it.isNotBlank() } ?: "Xem chương trình đang áp dụng",
        bgColor = Color(0xFFE8F5E9),
        imageUrl = BackendUrlResolver.resolveFileUrl(imageUrl),
        linkUrl = linkUrl
    )
}

@Composable
fun PromoBannerPager(
    items: List<PromoBannerItem> = defaultPromoItems,
    autoScrollDelay: Long = 3000L,
    modifier: Modifier = Modifier,
    onBannerClick: (PromoBannerItem) -> Unit = {}
) {
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })

    LaunchedEffect(items.size, autoScrollDelay) {
        if (items.size > 1) {
            while (true) {
                delay(autoScrollDelay)
                val next = (pagerState.currentPage + 1) % items.size
                pagerState.animateScrollToPage(next, animationSpec = tween(600))
            }
        }
    }

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            PromoBannerCard(
                item = items[page],
                onClick = { onBannerClick(items[page]) }
            )
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
private fun PromoBannerCard(
    item: PromoBannerItem,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = item.bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            item.imageUrl?.takeIf { it.isNotBlank() }?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.28f))
                )
            }

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
                        color = if (item.imageUrl.isNullOrBlank()) Color(0xFF2E7D32) else Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = item.subtitle,
                        fontSize = 13.sp,
                        color = if (item.imageUrl.isNullOrBlank()) Color(0xFF444444) else Color.White.copy(alpha = 0.92f)
                    )
                }
                if (item.imageUrl.isNullOrBlank()) {
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
    }
}
