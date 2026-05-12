@file:Suppress("DEPRECATION")
package com.example.nhathuoc.ui.screen.miniscreen

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.example.nhathuoc.data.model.ProductReviewSummaryDto
import com.example.nhathuoc.data.model.ReviewDto
import com.example.nhathuoc.data.remote.BackendUrlResolver
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
    val fileType: String? = null,
    val resourceType: String? = null,
    val issuedBy: String? = null,          // Co quan c?p
    val issuedAt: String? = null,          // Ng�y c?p
    val expiresAt: String? = null          // Ng�y h?t h?n
)

private data class PickedAttachment(
    val uri: Uri,
    val name: String,
    val fileType: String
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
    val sku: String? = null,
    val stockQuantity: Int = 0,
    val productType: String = "MEDICINE",
    val registrationNumber: String? = null,
    val riskClassification: String = "A",
    val requiresCertification: Boolean = false,
    val requiresConsultation: Boolean = false,
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
    onAddToCart: (Int) -> Unit = {},
    onBuyNow: (Int) -> Unit = onAddToCart,
    reviewSummary: ProductReviewSummaryDto? = null,
    reviews: List<ReviewDto> = emptyList(),
    reviewSubmitting: Boolean = false,
    reviewSubmitMessage: String? = null,
    openReviewOnStart: Boolean = false,
    reviewSubmitted: Boolean = false,
    onSubmitReview: ((Int, String, String, List<Uri>) -> Unit)? = null,
    onReportReview: ((String) -> Unit)? = null
) {
    val remoteImageUrls = remember(product.imageUrls, product.imageUrl) {
        product.imageUrls.ifEmpty {
            listOfNotNull(product.imageUrl?.takeIf { it.isNotBlank() })
        }.map { BackendUrlResolver.resolveFileUrl(it) }
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
    val normalizedRisk = product.riskClassification.uppercase()
    val inStock = product.stockQuantity > 0
    val canOrderOnline = normalizedRisk != "C" && normalizedRisk != "D" && inStock
    var selectedQuantity by remember(product.id) { mutableStateOf(1) }
    LaunchedEffect(product.stockQuantity) {
        selectedQuantity = selectedQuantity.coerceIn(1, product.stockQuantity.coerceAtLeast(1))
    }
    val unavailableMessage = when {
        !inStock -> "Sản phẩm đang hết hàng. Bạn có thể nhắn tư vấn để được báo khi có hàng."
        normalizedRisk == "C" || normalizedRisk == "D" ->
            "Sản phẩm loại $normalizedRisk cần tư vấn/ký kết tại nhà thuốc, chưa hỗ trợ đặt online."
        else -> ""
    }
    val productCode = remember(product.sku, product.registrationNumber, product.id) {
        product.sku?.takeIf { it.isNotBlank() }
            ?: product.registrationNumber?.takeIf { it.isNotBlank() }
            ?: product.id.takeLast(8)
    }
    val effectiveReviewCount = listOf(reviewSummary?.totalReviews ?: 0, reviews.size, product.reviewCount).maxOrNull() ?: 0
    val effectiveRating = when {
        reviewSummary != null && reviewSummary.totalReviews > 0 -> reviewSummary.averageRating.toFloat()
        reviews.isNotEmpty() -> reviews.map { it.rating }.average().toFloat()
        else -> product.rating
    }
    val effectiveCommentCount = if (reviews.isNotEmpty()) {
        reviews.count { !it.comment.isNullOrBlank() }
    } else {
        product.commentCount
    }

    // Certificate bottom sheet
    if (showCertSheet) {
        CertificateBottomSheet(
            registrationNumber = product.registrationNumber,
            riskClassification = normalizedRisk,
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
                onBuyNow       = onBuyNow,
                canOrderOnline = canOrderOnline,
                restrictedMessage = unavailableMessage,
                stockQuantity = product.stockQuantity,
                unit = product.unit,
                selectedQuantity = selectedQuantity,
                onQuantityChange = { quantity ->
                    selectedQuantity = quantity.coerceIn(1, product.stockQuantity.coerceAtLeast(1))
                }
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
                            .background(product.iconBg.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
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
                            else      -> "Thông tin sản phẩm được cập nhật theo tồn kho thực tế"
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
                        ProductRiskChip(product.riskClassification)
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

                    // Product code • Rating • Reviews • Comments
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Mã: $productCode",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 120.dp)
                        )
                        DotHorizontalDivider()
                        RatingStars(effectiveRating)
                        DotHorizontalDivider()
                        Text("${effectiveReviewCount} đánh giá", fontSize = 12.sp, color = Color.Gray)
                        DotHorizontalDivider()
                        Text("${effectiveCommentCount} bình luận", fontSize = 12.sp, color = Color.Gray)
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

                    Spacer(Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (inStock) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ) {
                        Text(
                            text = if (inStock) "Còn ${product.stockQuantity} ${product.unit}" else "Hết hàng",
                            color = if (inStock) GreenTop else RedColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
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
                    InfoRow(Icons.Outlined.LocalShipping,  GreenTop,           "Giao nhanh")
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

            Spacer(Modifier.height(8.dp))

            ReviewsSection(
                summary = reviewSummary,
                reviews = reviews,
                isSubmitting = reviewSubmitting,
                submitMessage = reviewSubmitMessage,
                openReviewOnStart = openReviewOnStart,
                reviewSubmitted = reviewSubmitted,
                onSubmitReview = onSubmitReview,
                onReportReview = onReportReview
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// -- Sub-composables ---------------------------------------------------

@Composable
private fun ReviewsSection(
    summary: ProductReviewSummaryDto?,
    reviews: List<ReviewDto>,
    isSubmitting: Boolean,
    submitMessage: String?,
    openReviewOnStart: Boolean,
    reviewSubmitted: Boolean,
    onSubmitReview: ((Int, String, String, List<Uri>) -> Unit)?,
    onReportReview: ((String) -> Unit)?
) {
    var showDialog by remember { mutableStateOf(false) }
    val averageRating = summary?.averageRating ?: 0.0
    val totalReviews = summary?.totalReviews ?: reviews.size

    LaunchedEffect(openReviewOnStart, onSubmitReview) {
        if (openReviewOnStart && onSubmitReview != null) {
            showDialog = true
        }
    }

    LaunchedEffect(reviewSubmitted) {
        if (reviewSubmitted) {
            showDialog = false
        }
    }

    if (showDialog && onSubmitReview != null) {
        ReviewInputDialog(
            isSubmitting = isSubmitting,
            errorMessage = submitMessage,
            onDismiss = { showDialog = false },
            onSubmit = { rating, title, comment, attachments ->
                onSubmitReview(rating, title, comment, attachments)
            }
        )
    }

    Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Đánh giá sản phẩm", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RatingStars(averageRating.toFloat())
                        Text(
                            if (totalReviews > 0) String.format("%.1f/5 • %d đánh giá", averageRating, totalReviews)
                            else "Chưa có đánh giá",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                if (openReviewOnStart && onSubmitReview != null) {
                    OutlinedButton(
                        onClick = { showDialog = true },
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text("Viết đánh giá", color = GreenTop, fontSize = 12.sp)
                    }
                }
            }

            if (!submitMessage.isNullOrBlank() && !showDialog) {
                Text(submitMessage, color = GreenTop, fontSize = 12.sp)
            }

            if (reviews.isEmpty()) {
                Text("Khách đã mua có thể đánh giá sản phẩm tại đây.", fontSize = 12.sp, color = Color.Gray)
            } else {
                reviews.take(5).forEach { review ->
                    ReviewRow(review, onReportReview)
                }
            }
        }
    }
}

@Composable
private fun ReviewInputDialog(
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (Int, String, String, List<Uri>) -> Unit
) {
    val context = LocalContext.current
    var rating by remember { mutableStateOf(5) }
    var title by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var attachments by remember { mutableStateOf<List<PickedAttachment>>(emptyList()) }
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val picked = uris.map { uri ->
            val name = context.displayName(uri).ifBlank { "file_dinh_kem" }
            PickedAttachment(
                uri = uri,
                name = name,
                fileType = detectPickedFileType(context.contentResolver.getType(uri), name)
            )
        }
        attachments = (attachments + picked).distinctBy { it.uri }.take(5)
    }

    // Dùng Dialog custom thay AlertDialog để tránh bị cắt nội dung + nút
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // ── Header ─────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 0.dp)
                ) {
                    Text(
                        "Viết đánh giá",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = GreenTop
                    )
                    Text(
                        "Chia sẻ trải nghiệm của bạn về sản phẩm",
                        fontSize = 13.sp,
                        color = Color(0xFF5A7A62),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // ── Scrollable body ────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Star picker — tap trực tiếp lên ngôi sao
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Đánh giá của bạn",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF374151)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 5 ngôi sao tap được, to rõ
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                (1..5).forEach { value ->
                                    Icon(
                                        imageVector = if (value <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                        contentDescription = "$value sao",
                                        tint = if (value <= rating) GoldColor else Color(0xFFD1D5DB),
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                            ) { rating = value }
                                    )
                                }
                            }
                            // Label mô tả mức sao
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = starLabelBg(rating)
                            ) {
                                Text(
                                    starLabel(rating),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = starLabelFg(rating),
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Bonus điểm hint khi chọn 5 sao
                    if (rating == 5) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFBEB)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("⭐", fontSize = 14.sp)
                                Text(
                                    "Đánh giá 5 sao sẽ được cộng +200 điểm thưởng!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }

                    // Tiêu đề
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Tiêu đề (tuỳ chọn)", fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenTop,
                            focusedLabelColor = GreenTop,
                            unfocusedBorderColor = Color(0xFFE0EDE3),
                            cursorColor = GreenTop
                        )
                    )

                    // Nội dung đánh giá
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Nội dung đánh giá *", fontSize = 13.sp) },
                        placeholder = { Text("Bạn cảm thấy thế nào về sản phẩm?", fontSize = 13.sp, color = Color(0xFFB0BEC5)) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenTop,
                            focusedLabelColor = GreenTop,
                            unfocusedBorderColor = Color(0xFFE0EDE3),
                            cursorColor = GreenTop
                        )
                    )

                    // Đính kèm file
                    OutlinedButton(
                        onClick = { filePicker.launch(arrayOf("image/*", "application/pdf")) },
                        enabled = !isSubmitting && attachments.size < 5,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0EDE3)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenTop)
                    ) {
                        Icon(Icons.Outlined.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Thêm ảnh / PDF  (${attachments.size}/5)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Danh sách file đính kèm
                    if (attachments.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            attachments.forEach { att ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF0F4F1)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                if (att.fileType == "IMAGE") Icons.Outlined.Image else Icons.Outlined.PictureAsPdf,
                                                contentDescription = null,
                                                tint = GreenTop,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                att.name,
                                                fontSize = 12.sp,
                                                color = Color(0xFF374151),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        TextButton(
                                            onClick = { attachments = attachments.filterNot { it.uri == att.uri } },
                                            enabled = !isSubmitting,
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("Xóa", color = RedColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Error banner
                    if (!errorMessage.isNullOrBlank()) {
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Outlined.ErrorOutline, null, tint = RedColor, modifier = Modifier.size(16.dp))
                                Text(errorMessage, color = Color(0xFFC62828), fontSize = 12.sp)
                            }
                        }
                    }
                }

                // ── Action buttons — LUÔN hiển thị, không bị cắt ────────
                HorizontalDivider(color = Color(0xFFE0EDE3))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0EDE3)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF374151))
                    ) {
                        Text("Đóng", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { onSubmit(rating, title, comment, attachments.map { it.uri }) },
                        enabled = !isSubmitting && comment.isNotBlank(),
                        modifier = Modifier.weight(2f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenTop,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFB0C4B1)
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Star, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Gửi đánh giá", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Helpers cho star label
private fun starLabel(rating: Int) = when (rating) {
    1 -> "Rất tệ"
    2 -> "Không tốt"
    3 -> "Bình thường"
    4 -> "Hài lòng"
    5 -> "Tuyệt vời ★"
    else -> ""
}

private fun starLabelBg(rating: Int) = when (rating) {
    1, 2 -> Color(0xFFFFEBEE)
    3    -> Color(0xFFFFF3E0)
    4    -> Color(0xFFE8F5E9)
    5    -> Color(0xFFE8F5E9)
    else -> Color(0xFFF3F4F6)
}

private fun starLabelFg(rating: Int) = when (rating) {
    1, 2 -> Color(0xFFE53935)
    3    -> Color(0xFFD97706)
    4    -> GreenTop
    5    -> GreenTop
    else -> Color(0xFF6B7280)
}

@Composable
private fun ReviewRow(
    review: ReviewDto,
    onReportReview: ((String) -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8FAF8))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                review.userName ?: "Khách hàng",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            RatingStars(review.rating.toFloat())
        }
        if (!review.title.isNullOrBlank()) {
            Text(review.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GreenTop)
        }
        if (!review.comment.isNullOrBlank()) {
            Text(review.comment, fontSize = 13.sp, color = Color(0xFF444444), lineHeight = 18.sp)
        }
        if (review.attachments.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(review.attachments) { _, attachment ->
                    if (attachment.fileType.equals("IMAGE", ignoreCase = true)) {
                        val attachmentUrl = BackendUrlResolver.resolveFileUrl(attachment.fileUrl)
                        AsyncImage(
                            model = attachmentUrl,
                            contentDescription = "Review attachment",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.PictureAsPdf,
                                    contentDescription = null,
                                    tint = GreenTop,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("PDF", color = GreenTop, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (review.isVerifiedPurchase) {
                Text("Đã mua hàng", fontSize = 11.sp, color = GreenTop, fontWeight = FontWeight.SemiBold)
            }
            Text(review.createdAt.take(10), fontSize = 11.sp, color = Color.Gray)
            if (onReportReview != null) {
                Text(
                    "Báo cáo",
                    fontSize = 11.sp,
                    color = Color(0xFFB45309),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onReportReview(review.id) }
                )
            }
        }
    }
}

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
private fun ProductRiskChip(riskClassification: String) {
    val normalizedRisk = riskClassification.trim().uppercase()
    val (label, chipColor) = when (normalizedRisk) {
        "A" -> "Loại A" to Color(0xFF2E7D32)
        "B" -> "Loại B" to Color(0xFF1565C0)
        "C" -> "Loại C" to Color(0xFFE65100)
        "D" -> "Loại D" to Color(0xFFC62828)
        else -> "Chưa phân loại" to Color(0xFF757575)
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = chipColor.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, chipColor.copy(alpha = 0.45f))
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

@Composable
private fun CertificatePreviewRow(cert: ProductCertificate) {
    val context = LocalContext.current
    val resolvedFileUrl = remember(cert.fileUrl) {
        BackendUrlResolver.resolveFileUrl(cert.fileUrl)
    }
    val hasFile = resolvedFileUrl.isNotBlank()
    val isPdf = cert.fileType?.equals("PDF", ignoreCase = true) == true ||
            cert.resourceType?.equals("raw", ignoreCase = true) == true ||
            resolvedFileUrl.isPdfUrl()
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
                context.openCertificateDocument(resolvedFileUrl, cert.name, isPdf)
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                cert.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!cert.issuedBy.isNullOrBlank()) {
                Text(cert.issuedBy, fontSize = 11.sp, color = Color.Gray)
            }
        }
        if (hasFile) {
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = GreenTop.copy(alpha = 0.10f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GreenTop.copy(alpha = 0.30f))
            ) {
                Text(
                    if (isPdf) "Tải" else "Mở",
                    fontSize = 11.sp,
                    color = GreenTop,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        } else if (!cert.expiresAt.isNullOrBlank()) {
            Text("Hết hạn: ${cert.expiresAt}", fontSize = 10.sp, color = Color(0xFFE65100))
        }
    }
}

private fun Context.displayName(uri: Uri): String {
    return contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index).orEmpty() else ""
            } else {
                ""
            }
        }
        .orEmpty()
        .ifBlank { uri.lastPathSegment.orEmpty().substringAfterLast('/') }
}

private fun detectPickedFileType(mimeType: String?, name: String): String {
    val lowerName = name.lowercase()
    return when {
        mimeType == "application/pdf" || lowerName.endsWith(".pdf") -> "PDF"
        mimeType?.startsWith("image/") == true -> "IMAGE"
        else -> "FILE"
    }
}

private fun Context.openCertificateDocument(url: String, title: String, isPdf: Boolean = url.isPdfUrl()) {
    val targetUrl = BackendUrlResolver.resolveFileUrl(url)
    if (targetUrl.isBlank()) return

    if (isPdf || targetUrl.isPdfUrl()) {
        downloadCertificateDocument(targetUrl, title, isPdf = true)
        return
    }

    runCatching {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }.onFailure {
        Toast.makeText(this, "Không mở được tài liệu, đang tải xuống", Toast.LENGTH_SHORT).show()
        downloadCertificateDocument(targetUrl, title, isPdf = false)
    }
}

private fun Context.downloadCertificateDocument(url: String, title: String, isPdf: Boolean = url.isPdfUrl()) {
    val targetUrl = BackendUrlResolver.resolveFileUrl(url)
    val fileName = buildCertificateFileName(targetUrl, title, isPdf)
    runCatching {
        val request = DownloadManager.Request(Uri.parse(targetUrl))
            .setTitle(fileName)
            .setDescription("Đang tải giấy tờ chứng nhận")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        if (isPdf) {
            request.setMimeType("application/pdf")
        }

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

private fun buildCertificateFileName(url: String, title: String, isPdf: Boolean = url.isPdfUrl()): String {
    val extension = if (isPdf) ".pdf" else ""
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
    riskClassification: String,
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
                    ProductRiskChip(riskClassification)
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
    onAddToCart: (Int) -> Unit,
    onBuyNow: (Int) -> Unit,
    canOrderOnline: Boolean,
    restrictedMessage: String,
    stockQuantity: Int,
    unit: String,
    selectedQuantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    var quantityText by remember { mutableStateOf(selectedQuantity.toString()) }

    LaunchedEffect(selectedQuantity) {
        if (quantityText.toIntOrNull() != selectedQuantity) {
            quantityText = selectedQuantity.toString()
        }
    }

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
            if (!canOrderOnline) {
                Text(
                    text = restrictedMessage,
                    color = RedColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Button(
                    onClick = onChat,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
                ) {
                    Icon(Icons.Outlined.SupportAgent, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tư vấn với nhân viên", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Còn $stockQuantity $unit",
                            color = GreenTop,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Chọn số lượng mua ngay",
                            color = Color(0xFF6B7280),
                            fontSize = 11.sp
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3F7F4))
                    ) {
                        IconButton(
                            onClick = { onQuantityChange(selectedQuantity - 1) },
                            enabled = selectedQuantity > 1,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Giảm",
                                tint = if (selectedQuantity > 1) GreenTop else Color(0xFFBDBDBD),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .defaultMinSize(minWidth = 42.dp)
                                .padding(vertical = 4.dp)
                        ) {
                            BasicTextField(
                                value = quantityText,
                                onValueChange = { raw ->
                                    val digits = raw.filter(Char::isDigit).take(4)
                                    quantityText = digits
                                    digits.toIntOrNull()?.let { value ->
                                        onQuantityChange(value.coerceIn(1, stockQuantity))
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF111827)
                                ),
                                modifier = Modifier
                                    .width(42.dp)
                                    .padding(vertical = 6.dp),
                                decorationBox = { inner ->
                                    Box(contentAlignment = Alignment.Center) { inner() }
                                }
                            )
                        }
                        IconButton(
                            onClick = { onQuantityChange(selectedQuantity + 1) },
                            enabled = selectedQuantity < stockQuantity,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Tăng",
                                tint = if (selectedQuantity < stockQuantity) GreenTop else Color(0xFFBDBDBD),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
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
                        onClick = { onAddToCart(selectedQuantity) },
                        enabled = canOrderOnline,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenTop),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = canOrderOnline).copy(width = 1.5.dp)
                    ) {
                        Icon(Icons.Outlined.ShoppingCart, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Giỏ hàng", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }

                    // Mua ngay -> cart/checkout
                    Button(
                        onClick = { onBuyNow(selectedQuantity) },
                        enabled = canOrderOnline,
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
}
