package com.example.nhathuoc.ui.screen.miniscreen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.nhathuoc.ui.theme.GreenLight
private val GreenTop = Color(0xFF2E7D32)

private val GoldColor = Color(0xFFFFAB00)
private val RedColor  = Color(0xFFE53935)

// ── Certificate model ───────────────────────────────────────────────────────────────
data class ProductCertificate(
    val id: String = "",
    val type: String = "REGISTRATION",     // REGISTRATION | IMPORT_LICENSE | COA | GMP | OTHER
    val name: String,                       // Tên giấy tờ / số hiệu
    val fileUrl: String = "",              // URL ảnh hoặc PDF
    val issuedBy: String? = null,          // Cơ quan cấp
    val issuedAt: String? = null,          // Ngày cấp
    val expiresAt: String? = null          // Ngày hết hạn
)

// ── Product model passed via nav (simplified) ─────────────────────────────────
data class ProductDetail(
    val id: String = "00046589",
    val name: String,
    val brand: String,
    val origin: String,
    val price: String,
    val originalPrice: String,
    val discountPercent: Int,
    val unit: String = "Hộp",
    val rating: Float = 5f,
    val reviewCount: Int = 12,
    val commentCount: Int = 130,
    val rewardPoints: Int = 962,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val isAuthentic: Boolean = true,
    val thumbnailCount: Int = 6,
    // Loại sản phẩm: Thuốc/TPCN/Mỹ phẩm...
    val productType: String = "MEDICINE",
    // Số đăng ký lưu hành
    val registrationNumber: String? = null,
    // Danh sách giấy tờ chứng nhận
    val certificates: List<ProductCertificate> = emptyList()
)

// ── Screen ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: ProductDetail,
    onBack: () -> Unit = {},
    onChat: (ChatProductContext) -> Unit = {},
    onFindPharmacy: () -> Unit = {},
    onAddToCart: () -> Unit = {}
) {
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { product.thumbnailCount })
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
                onChat = {
                    onChat(
                        ChatProductContext(
                            productName     = product.name,
                            brand           = product.brand,
                            origin          = product.origin,
                            price           = product.price,
                            originalPrice   = product.originalPrice,
                            discountPercent = product.discountPercent,
                            icon            = product.icon,
                            iconTint        = product.iconTint,
                            iconBg          = product.iconBg
                        )
                    )
                },
                onFindPharmacy = onFindPharmacy,
                onAddToCart    = onAddToCart
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Image pager + nav overlay ───────────────────────────────────
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
                            Icon(
                                imageVector = product.icon,
                                contentDescription = product.name,
                                tint = product.iconTint,
                                modifier = Modifier.size(80.dp)
                            )
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
                    repeat(product.thumbnailCount) { i ->
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

            // ── Thumbnail strip (click → jump pager) ───────────────────────────
            Surface(color = Color.White, shadowElevation = 1.dp) {
                LazyRow(
                    state = thumbListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed((0 until product.thumbnailCount).toList()) { idx, _ ->
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

            Spacer(Modifier.height(4.dp))

            // ── Product info card ──────────────────────────────────────────────
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

                    // Hint text
                    Text(
                        "Mẫu mã sản phẩm có thể thay đổi theo lô hàng",
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

                    // ID • Rating • Reviews • Comments
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(product.id, fontSize = 12.sp, color = Color.Gray)
                        DotDivider()
                        RatingStars(product.rating)
                        DotDivider()
                        Text("${product.reviewCount} đánh giá", fontSize = 12.sp, color = Color.Gray)
                        DotDivider()
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

            // ── Promotion / quick info strip ──────────────────────────────
            Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text("Thông tin nổi bật", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Spacer(Modifier.height(10.dp))
                    InfoRow(Icons.Outlined.LocalShipping,  GreenTop, "Giao nhanh 2 giờ toàn quốc")
                    InfoRow(Icons.Outlined.VerifiedUser,   Color(0xFF2E7D32), "Sản phẩm chính hãng 100%")
                    InfoRow(Icons.Outlined.SwapHoriz,      Color(0xFFE65100), "Đổi trả trong 30 ngày")
                    InfoRow(Icons.Outlined.SupportAgent,   Color(0xFF6A1B9A), "Dược sĩ tư vấn 24/7")
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Giấy tờ & Chứng nhận ─────────────────────────────────────────
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
                            "Xem tất cả ›",
                            fontSize = 13.sp,
                            color = GreenTop,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { showCertSheet = true }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    // Hiển thị số đăng ký lưu hành nếu có
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
                    // Danh sách rút gọn (tối đa 2 giấy tờ preview)
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

            // ── Product description stub ───────────────────────────────────────
            Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text("Mô tả sản phẩm", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${product.name} là sản phẩm nhập khẩu chính hãng từ ${product.origin}, " +
                                "thương hiệu ${product.brand}. Sản phẩm được kiểm định chất lượng và phân phối " +
                                "độc quyền tại hệ thống Nhà Thuốc Long Châu trên toàn quốc.",
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

// ── Sub-composables ───────────────────────────────────────────────────

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
                "Tra cứu  ›",
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
        border = ButtonDefaults.outlinedButtonBorder
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

// Chip hiển thị loại sản phẩm
@Composable
private fun ProductTypeChip(productType: String) {
    val (label, chipColor) = when (productType) {
        "MEDICINE"   -> Pair("💊 Thuốc",          Color(0xFF1565C0))
        "SUPPLEMENT" -> Pair("✨ TPCN",            Color(0xFF2E7D32))
        "COSMETIC"   -> Pair("🌸 Mỹ phẩm",       Color(0xFF6A1B9A))
        "DEVICE"     -> Pair("⚕️ Thiết bị y tế",  Color(0xFF00838F))
        "FOOD"       -> Pair("🌿 Thực phẩm",      Color(0xFFE65100))
        else         -> Pair("📎 Khác",             Color(0xFF757575))
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

// Hiển thị 1 dòng giấy tờ rút gọn
@Composable
private fun CertificatePreviewRow(cert: ProductCertificate) {
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
        if (!cert.expiresAt.isNullOrBlank()) {
            Text("HH: ${cert.expiresAt}", fontSize = 10.sp, color = Color(0xFFE65100))
        }
    }
}

// Bottom sheet hiển thị toàn bộ giấy tờ
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

            // Loại sản phẩm
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Category, null, tint = GreenTop, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Loại sản phẩm", fontSize = 11.sp, color = Color.Gray)
                    ProductTypeChip(productType)
                }
            }

            // Số đăng ký lưu hành
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

            // Danh sách giấy tờ
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
                    "ⓘ Chưa có giấy tờ nào được cập nhật cho sản phẩm này.",
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
private fun DotDivider() {
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
    onAddToCart: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chat bubble button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(GreenTop, GreenLight)))
                    .clickable { onChat() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.ChatBubble,
                    contentDescription = "Tư vấn",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Find pharmacy
            OutlinedButton(
                onClick = onFindPharmacy,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenTop),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp)
            ) {
                Text(
                    "Tìm nhà thuốc",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Add to cart / buy
            Button(
                onClick = onAddToCart,
                modifier = Modifier
                    .weight(1.4f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
            ) {
                Text(
                    "Chọn mua",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}