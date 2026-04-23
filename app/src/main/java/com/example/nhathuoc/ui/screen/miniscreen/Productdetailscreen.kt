@file:Suppress("DEPRECATION")
package com.example.nhathuoc.ui.screen.miniscreen

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.example.nhathuoc.ui.theme.GreenLight
private val GreenTop = Color(0xFF2E7D32)

private val GoldColor = Color(0xFFFFAB00)
private val RedColor  = Color(0xFFE53935)

// -- Certificate model ---------------------------------------------------------------
data class ProductCertificate(
    val id: String = "",
    val type: String = "REGISTRATION",     // REGISTRATION | IMPORT_LICENSE | COA | GMP | OTHER
    val name: String,                       // T�n gi?y t? / s? hi?u
    val fileUrl: String = "",              // URL ?nh ho?c PDF
    val issuedBy: String? = null,          // Co quan c?p
    val issuedAt: String? = null,          // Ng�y c?p
    val expiresAt: String? = null          // Ng�y h?t h?n
)

// -- Product model passed via nav (simplified) ---------------------------------
data class ProductDetail(
    val id: String = "",
    val name: String,
    val brand: String,
    val origin: String,
    val price: String,
    val originalPrice: String,
    val discountPercent: Int,
    val unit: String = "Hộp",
    val rating: Float = 5f,
    val reviewCount: Int = 0,
    val commentCount: Int = 0,
    val rewardPoints: Int = 0,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val imageResIds: List<Int> = emptyList(),  // drawable images for pager
    val imageResId: Int? = null,               // single drawable (thumbnail)
    val imageUrl: String? = null,              // remote image URL (from backend)
    val imageUrls: List<String> = emptyList(), // remote gallery URLs (from backend)
    val isAuthentic: Boolean = true,
    val productType: String = "MEDICINE",
    val registrationNumber: String? = null,
    val certificates: List<ProductCertificate> = emptyList()
)

// -- Screen ------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: ProductDetail,
    onBack: () -> Unit = {},
    onChat: () -> Unit = {},
    onFindPharmacy: () -> Unit = {},
    onAddToCart: () -> Unit = {},
    onBuyNow: () -> Unit = onAddToCart
) {
    val remoteImageUrls = remember(product.imageUrls, product.imageUrl) {
        product.imageUrls.ifEmpty {
            listOfNotNull(product.imageUrl?.takeIf { it.isNotBlank() })
        }
    }
    val imageCount = when {
        product.imageResIds.isNotEmpty() -> product.imageResIds.size
        remoteImageUrls.isNotEmpty() -> remoteImageUrls.size
        else -> 1
    }
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        pageCount = { imageCount }
    )
    val thumbListState = androidx.compose.foundation.lazy.rememberLazyListState()
    var showCertSheet by remember { mutableStateOf(false) }

    // Certificate bottom sheet
    if (showCertSheet) {
        CertificateBottomSheet(
            registrationNumber = product.registrationNumber,
            productType        = product.productType,
            certificates       = product.certificates,
            onDismiss          = { showCertSheet = false }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        bottomBar = {
            ProductBottomBar(
                onChat = onChat,
                onFindPharmacy = onFindPharmacy,
                onAddToCart    = onAddToCart,
                onBuyNow       = onBuyNow
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // -- Image pager + nav overlay -----------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color(0xFFF8F9FF))
            ) {
                // Swipeable image pager
                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(product.iconBg.copy(alpha = 0.15f + page * 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape)
                                .background(product.iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            val currentImageResId = if (product.imageResIds.isNotEmpty() && page < product.imageResIds.size) {
                                product.imageResIds[page]
                            } else {
                                product.imageResId
                            }
                            val currentImageUrl = remoteImageUrls.getOrNull(page)

                            when {
                                currentImageResId != null -> {
                                    Image(
                                        painter = painterResource(currentImageResId),
                                        contentDescription = product.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                !currentImageUrl.isNullOrBlank() -> {
                                    AsyncImage(
                                        model = currentImageUrl,
                                        contentDescription = product.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = product.icon,
                                        contentDescription = product.name,
                                        tint = product.iconTint,
                                        modifier = Modifier.size(80.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Dot indicators bottom-center
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(imageCount) { i ->
                        val selected = pagerState.currentPage == i
                        Box(
                            modifier = Modifier
                                .size(if (selected) 8.dp else 5.dp)
                                .clip(CircleShape)
                                .background(if (selected) GreenTop else Color(0xFFBBBBBB))
                        )
                    }
                }

                // Authentic badge top-right
                if (product.isAuthentic) {
                    AuthenticBadge(
                        onTraceClick = { showCertSheet = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 56.dp, end = 12.dp)
                    )
                }

                // Top navigation bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Quay lại",
                            tint = Color(0xFF333333),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Row {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Outlined.Share,
                                contentDescription = "Chia sẻ",
                                tint = Color(0xFF333333),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        BadgedBox(badge = {
                            Badge(containerColor = Color(0xFFFF6D00)) {
                                Text("1", color = Color.White, fontSize = 9.sp)
                            }
                        }) {
                            IconButton(onClick = {}) {
                                Icon(
                                    Icons.Outlined.ShoppingCart,
                                    contentDescription = "Giỏ hàng",
                                    tint = GreenTop,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // -- Thumbnail strip (click ? jump pager) ---------------------------
            Surface(color = Color.White, shadowElevation = 1.dp) {
                LazyRow(
                    state = thumbListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed((0 until imageCount).toList()) { idx, _ ->
                        val isSelected = pagerState.currentPage == idx
                        val scope = androidx.compose.runtime.rememberCoroutineScope()
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(product.iconBg.copy(alpha = if (isSelected) 1f else 0.35f))
                                .then(
                                    if (isSelected) Modifier.border(2.dp, GreenTop, RoundedCornerShape(8.dp))
                                    else Modifier.border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                )
                                .clickable {
                                    scope.launch { pagerState.animateScrollToPage(idx) }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Show image from array if available
                            val currentImageResId = if (product.imageResIds.isNotEmpty() && idx < product.imageResIds.size) {
                                product.imageResIds[idx]
                            } else {
                                product.imageResId
                            }
                            val currentImageUrl = remoteImageUrls.getOrNull(idx)

                            if (currentImageResId != null) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(currentImageResId),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().padding(10.dp),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                            } else if (!currentImageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = currentImageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().padding(6.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Icon(
                                    imageVector = product.icon,
                                    contentDescription = null,
                                    tint = product.iconTint.copy(alpha = if (isSelected) 1f else 0.45f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // -- Product info card ----------------------------------------------
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

                    // Hint text
                    Text(
                        when (product.productType) {
                            "DEVICE"  -> "Thiết bị y tế - kiểm định chất lượng trước khi xuất kho"
                            "SUPPLY"  -> "Vật tư tiêu hao - đảm bảo vô khuẩn theo tiêu chuẩn"
                            "MEDICINE"-> "Sản phẩm cần tư vấn - vui lòng hỏi nhân viên trước khi mua"
                            else      -> "Mẫu mã sản phẩm có thể thay đổi theo lô hàng"
                        },
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Origin + Brand + Product Type chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OriginChip(product.origin)
                        ProductTypeChip(product.productType)
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Thương hiệu: ",
                            fontSize = 13.sp,
                            color = Color(0xFF555555)
                        )
                        Text(
                            product.brand,
                            fontSize = 13.sp,
                            color = GreenTop,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Product name
                    Text(
                        text = product.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        lineHeight = 22.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    // ID � Rating � Reviews � Comments
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(product.id, fontSize = 12.sp, color = Color.Gray)
                        DotHorizontalDivider()
                        RatingStars(product.rating)
                        DotHorizontalDivider()
                        Text("${product.reviewCount} đánh giá", fontSize = 12.sp, color = Color.Gray)
                        DotHorizontalDivider()
                        Text("${product.commentCount} bình luận", fontSize = 12.sp, color = Color.Gray)
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Spacer(Modifier.height(14.dp))

                    // Price row
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "${product.price} / ${product.unit}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GreenTop
                        )
                        Column(horizontalAlignment = Alignment.Start) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = RedColor
                            ) {
                                Text(
                                    "-${product.discountPercent}%",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                product.originalPrice,
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Reward points
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(GoldColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("P", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "+${product.rewardPoints} điểm thưởng",
                            fontSize = 13.sp,
                            color = GoldColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // -- Promotion / quick info strip ------------------------------
            Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text("Thông tin nổi bật", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Spacer(Modifier.height(10.dp))
                    InfoRow(Icons.Outlined.LocalShipping,  GreenTop,           "Giao nhanh 2 giờ - hỏa tốc toàn quốc")
                    InfoRow(Icons.Outlined.VerifiedUser,   Color(0xFF2E7D32),  "Vật tư chính hãng, có giấy phép lưu hành")
                    InfoRow(Icons.Outlined.SwapHoriz,      Color(0xFFE65100),  "Đổi trả trong 30 ngày nếu lỗi nhà sản xuất")
                    InfoRow(Icons.Outlined.SupportAgent,   Color(0xFF6A1B9A),  "Kỹ thuật viên hỗ trợ kỹ thuật 24/7")
                    InfoRow(Icons.Outlined.MedicalServices,Color(0xFF0277BD),  "Đạt tiêu chuẩn ISO 13485 / CE Mark")
                }
            }

            Spacer(Modifier.height(8.dp))

            // -- Gi?y t? & Ch?ng nh?n -----------------------------------------
            Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Giấy tờ & Chứng nhận",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            "Xem tất cả",
                            fontSize = 13.sp,
                            color = GreenTop,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { showCertSheet = true }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    // Hi?n th? s? dang k� luu h�nh n?u c�
                    if (!product.registrationNumber.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.AssignmentTurnedIn,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Số đăng ký lưu hành", fontSize = 11.sp, color = Color.Gray)
                                Text(product.registrationNumber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    // Danh s�ch r�t g?n (t?i da 2 gi?y t? preview)
                    product.certificates.take(2).forEach { cert ->
                        CertificatePreviewRow(cert)
                        Spacer(Modifier.height(6.dp))
                    }
                    if (product.certificates.isEmpty() && product.registrationNumber.isNullOrBlank()) {
                        Text(
                            "Chưa có giấy tờ được cập nhật",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // -- Product description stub ---------------------------------------
            Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text("Mô tả sản phẩm", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${product.name} là vật tư y tế chính hãng, nguồn gốc ${product.origin}, " +
                        "thương hiệu ${product.brand}. " +
                        "Sản phẩm đạt tiêu chuẩn kiểm định chất lượng, được cấp phép lưu hành " +
                        "và phân phối bởi hệ thống MedStore.",
                        fontSize = 13.sp,
                        color = Color(0xFF555555),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// -- Sub-composables ---------------------------------------------------

@Composable
private fun AuthenticBadge(modifier: Modifier = Modifier, onTraceClick: () -> Unit) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            shadowElevation = 3.dp,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Circular stamp border
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, RedColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row {
                            repeat(3) {
                                Icon(Icons.Filled.Star, null, tint = RedColor, modifier = Modifier.size(10.dp))
                            }
                        }
                        Text("CHÍNH", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = RedColor)
                        Text("HÃNG", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = RedColor)
                        Row {
                            repeat(3) {
                                Icon(Icons.Filled.Star, null, tint = RedColor, modifier = Modifier.size(10.dp))
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE3F2FD),
            modifier = Modifier.clickable { onTraceClick() }
        ) {
            Text(
                "Tra cứu",
                fontSize = 11.sp,
                color = GreenTop,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun OriginChip(origin: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFF5F5F5),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Flag dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
            )
            Text(origin, fontSize = 12.sp, color = Color(0xFF444444))
        }
    }
}

// Chip hi?n th? lo?i s?n ph?m
@Composable
private fun ProductTypeChip(productType: String) {
    val (label, chipColor) = when (productType) {
        "MEDICINE"   -> Pair("Thuốc",             Color(0xFF1565C0))
        "SUPPLEMENT" -> Pair("TPCN",              Color(0xFF2E7D32))
        "COSMETIC"   -> Pair("Mỹ phẩm",           Color(0xFF6A1B9A))
        "DEVICE"     -> Pair("Thiết bị y tế",     Color(0xFF00838F))
        "FOOD"       -> Pair("Thực phẩm",         Color(0xFFE65100))
        else         -> Pair("Khác",              Color(0xFF757575))
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = chipColor.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, chipColor.copy(alpha = 0.4f))
    ) {
        Text(
            label,
            fontSize = 11.sp,
            color = chipColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// Hi?n th? 1 d�ng gi?y t? r�t g?n
@Composable
private fun CertificatePreviewRow(cert: ProductCertificate) {
    val context = LocalContext.current
    val hasFile = cert.fileUrl.isNotBlank()
    val (icon, iconColor) = when (cert.type) {
        "REGISTRATION"   -> Pair(Icons.Outlined.AssignmentTurnedIn, Color(0xFF2E7D32))
        "IMPORT_LICENSE" -> Pair(Icons.Outlined.LocalShipping,       Color(0xFF1565C0))
        "COA"            -> Pair(Icons.Outlined.Science,             Color(0xFF6A1B9A))
        "GMP"            -> Pair(Icons.Outlined.VerifiedUser,        Color(0xFF00838F))
        else             -> Pair(Icons.Outlined.Description,          Color(0xFF757575))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F7FA))
            .clickable(enabled = hasFile) {
                context.openCertificateDocument(cert.fileUrl, cert.name)
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(cert.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
            if (!cert.issuedBy.isNullOrBlank()) {
                Text(cert.issuedBy, fontSize = 11.sp, color = Color.Gray)
            }
        }
        if (hasFile) {
            Text(
                if (cert.fileUrl.isPdfUrl()) "Xem/Tải" else "Mở",
                fontSize = 11.sp,
                color = GreenTop,
                fontWeight = FontWeight.SemiBold
            )
        } else if (!cert.expiresAt.isNullOrBlank()) {
            Text("Hết hạn: ${cert.expiresAt}", fontSize = 10.sp, color = Color(0xFFE65100))
        }
    }
}

private fun Context.openCertificateDocument(url: String, title: String) {
    if (url.isBlank()) return

    if (url.isPdfUrl()) {
        val pdfIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(url), "application/pdf")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            startActivity(pdfIntent)
        }.onFailure { throwable ->
            if (throwable is ActivityNotFoundException) {
                downloadCertificateDocument(url, title)
            } else {
                Toast.makeText(this, "Không mở được PDF, đang tải xuống", Toast.LENGTH_SHORT).show()
                downloadCertificateDocument(url, title)
            }
        }
        return
    }

    runCatching {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }.onFailure {
        Toast.makeText(this, "Không mở được tài liệu", Toast.LENGTH_SHORT).show()
    }
}

private fun Context.downloadCertificateDocument(url: String, title: String) {
    val fileName = buildCertificateFileName(url, title)
    runCatching {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Đang tải giấy tờ chứng nhận")
            .setMimeType(if (url.isPdfUrl()) "application/pdf" else null)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        getSystemService(DownloadManager::class.java).enqueue(request)
        Toast.makeText(this, "Đang tải xuống: $fileName", Toast.LENGTH_SHORT).show()
    }.onFailure {
        Toast.makeText(this, "Không tải được tài liệu", Toast.LENGTH_SHORT).show()
    }
}

private fun String.isPdfUrl(): Boolean {
    val path = runCatching { Uri.parse(this).path.orEmpty() }
        .getOrElse { substringBefore('?') }
    return path.lowercase().endsWith(".pdf")
}

private fun buildCertificateFileName(url: String, title: String): String {
    val extension = if (url.isPdfUrl()) ".pdf" else ""
    val rawName = title.ifBlank {
        runCatching { Uri.parse(url).lastPathSegment.orEmpty().substringBefore('?') }
            .getOrDefault("certificate")
    }
    val safeName = rawName
        .replace(Regex("[\\\\/:*?\"<>|]"), "_")
        .trim()
        .ifBlank { "certificate" }
    return if (extension.isNotBlank() && !safeName.lowercase().endsWith(extension)) {
        safeName + extension
    } else {
        safeName
    }
}

// Bottom sheet hi?n th? to�n b? gi?y t?
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CertificateBottomSheet(
    registrationNumber: String?,
    productType: String,
    certificates: List<ProductCertificate>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Giấy tờ & Chứng nhận",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Thông tin kiểm tra nguồn gốc & chất lượng sản phẩm",
                fontSize = 12.sp, color = Color.Gray
            )
            Spacer(Modifier.height(16.dp))

            // Lo?i s?n ph?m
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Category, null, tint = GreenTop, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Loại sản phẩm", fontSize = 11.sp, color = Color.Gray)
                    ProductTypeChip(productType)
                }
            }

            // S? dang k� luu h�nh
            if (!registrationNumber.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AssignmentTurnedIn, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Số đăng ký lưu hành", fontSize = 11.sp, color = Color.Gray)
                        Text(registrationNumber, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    }
                }
            }

            // Danh s�ch gi?y t?
            if (certificates.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(Modifier.height(12.dp))
                Text("Tài liệu đính kèm (${certificates.size})",
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                Spacer(Modifier.height(8.dp))
                certificates.forEach { cert ->
                    CertificatePreviewRow(cert)
                    Spacer(Modifier.height(6.dp))
                }
            } else if (registrationNumber.isNullOrBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Chưa có giấy tờ nào được cập nhật cho sản phẩm này.",
                    fontSize = 13.sp, color = Color.Gray
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RatingStars(rating: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { i ->
            Icon(
                imageVector = if (i < rating.toInt()) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = GoldColor,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun DotHorizontalDivider() {
    Box(
        modifier = Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(Color.LightGray)
    )
}

@Composable
private fun InfoRow(icon: ImageVector, iconTint: Color, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 13.sp, color = Color(0xFF444444))
    }
}


@Composable
private fun ProductBottomBar(
    onChat: () -> Unit,
    onFindPharmacy: () -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Thin green divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(listOf(GreenTop, GreenLight))
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chat / tư vấn kỹ thuật
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(GreenTop, GreenLight)))
                        .clickable { onChat() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.SupportAgent,
                        contentDescription = "Tư vấn kỹ thuật",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Thêm vào giỏ hàng
                OutlinedButton(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenTop),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.5.dp)
                ) {
                    Icon(Icons.Outlined.ShoppingCart, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Vào giỏ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }

                // Mua ngay -> cart/checkout
                Button(
                    onClick = onBuyNow,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
                ) {
                    Text(
                        "Mua ngay",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
