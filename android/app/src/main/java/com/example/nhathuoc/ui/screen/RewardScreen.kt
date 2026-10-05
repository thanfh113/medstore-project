package com.example.nhathuoc.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import coil.compose.AsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nhathuoc.data.model.PointTransactionDto
import com.example.nhathuoc.data.model.RewardProductDto
import com.example.nhathuoc.data.model.RewardRedemptionHistoryDto
import com.example.nhathuoc.data.model.RewardVoucherDto
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.util.formatVnDateTime
import com.example.nhathuoc.viewmodel.RewardViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

// ── Colors ────────────────────────────────────────────────────────────────
private val GreenTopRw = Color(0xFF2E7D32)
private val GoldColorRw = Color(0xFFFFAB00)

// ── Point-tier filter labels ──────────────────────────────────────────────
private val pointFilters = listOf("1.500 điểm", "3.000 điểm", "4.500 điểm", "6.000 điểm", "10.000 điểm", "Tất cả")

// ── Fallback UI model (used only when API products list is empty) ─────────
data class RewardProduct(
    val id: Int,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val name: String,
    val priceText: String,
    val pointCost: Int
)

private val fallbackRewardProducts = emptyList<RewardProduct>()

private enum class RewardScreenTab {
    REWARDS,
    HISTORY
}

// ── Screen ────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardScreen(
    modifier: Modifier = Modifier,
    onShopNow: () -> Unit = {},
    onUseVoucher: (String) -> Unit = {}
) {
    val viewModel: RewardViewModel = hiltViewModel()
    val accountState   by viewModel.accountState.collectAsState()
    val productsState  by viewModel.productsState.collectAsState()
    val transactionsState by viewModel.transactionsState.collectAsState()
    val redemptionsState by viewModel.redemptionsState.collectAsState()
    val vouchersState by viewModel.vouchersState.collectAsState()
    val redeemState    by viewModel.redeemState.collectAsState()

    var selectedTab       by remember { mutableStateOf(RewardScreenTab.REWARDS) }
    var selectedFilter    by remember { mutableStateOf("1.500 điểm") }
    var showBanner        by remember { mutableStateOf(true) }
    var showRedeemSheet   by remember { mutableStateOf(false) }
    var redeemProductDto  by remember { mutableStateOf<RewardProductDto?>(null) }
    var redeemFallback    by remember { mutableStateOf<RewardProduct?>(null) }

    // Derived reward points
    val userPoints = when (val s = accountState) {
        is UiState.Success -> s.data.availablePoints
        else               -> 0
    }

    // API product list
    val apiProducts: List<RewardProductDto> = when (val s = productsState) {
        is UiState.Success -> s.data
        else               -> emptyList()
    }
    val transactions: List<PointTransactionDto> = when (val s = transactionsState) {
        is UiState.Success -> s.data
        else -> emptyList()
    }
    val redemptions: List<RewardRedemptionHistoryDto> = when (val s = redemptionsState) {
        is UiState.Success -> s.data
        else -> emptyList()
    }
    val vouchers: List<RewardVoucherDto> = when (val s = vouchersState) {
        is UiState.Success -> s.data
        else -> emptyList()
    }

    // Tier filter
    val tierPts = selectedFilter.replace(".", "").replace(" điểm", "").trim().toIntOrNull() ?: 0
    val filteredApi      = if (selectedFilter == "Tất cả") apiProducts else apiProducts.filter { it.pointCost <= tierPts + 1500 }
    val filteredFallback = if (selectedFilter == "Tất cả") fallbackRewardProducts else fallbackRewardProducts.filter { it.pointCost <= tierPts + 1500 }
    val totalEarnedPoints = transactions.filter { it.points > 0 }.sumOf { it.points }
    val totalUsedPoints = transactions.filter { it.points < 0 }.sumOf { -it.points }

    // Snackbar for redeem feedback
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadRewardAccount()
        viewModel.loadRewardProducts()
        viewModel.loadRewardTransactions()
        viewModel.loadRewardRedemptions()
        viewModel.loadRewardVouchers()
    }
    LaunchedEffect(accountState) { if (accountState !is UiState.Loading) isRefreshing = false }

    LaunchedEffect(redeemState) {
        when (val s = redeemState) {
            is UiState.Success -> {
                showRedeemSheet = false
                scope.launch {
                    snackbarHostState.showSnackbar(s.data)
                }
                viewModel.clearRedeemState()
            }
            is UiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar("Lỗi: ${s.message}") }
                viewModel.clearRedeemState()
            }
            else -> {}
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.loadRewardAccount()
                viewModel.loadRewardProducts()
                viewModel.loadRewardTransactions()
                viewModel.loadRewardRedemptions()
                viewModel.loadRewardVouchers()
            }
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // ── Header xanh ───────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(GreenTopRw, GreenLight)))
                        .statusBarsPadding()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 32.dp)
                ) {
                        Column {
                            // Header tabs
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                HeaderTab(
                                    icon = Icons.Outlined.CardGiftcard,
                                    label = "Quà của tôi",
                                    selected = selectedTab == RewardScreenTab.REWARDS,
                                    onClick = { selectedTab = RewardScreenTab.REWARDS }
                                )
                                HeaderTab(
                                    icon = Icons.Outlined.History,
                                    label = "Lịch sử",
                                    selected = selectedTab == RewardScreenTab.HISTORY,
                                    onClick = { selectedTab = RewardScreenTab.HISTORY }
                                )
                            }
                            Spacer(Modifier.height(20.dp))

                            // Points display
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Điểm thưởng", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.EmojiEvents, null, tint = GoldColorRw, modifier = Modifier.size(28.dp))
                                        Spacer(Modifier.width(6.dp))
                                        when (accountState) {
                                            is UiState.Loading -> CircularProgressIndicator(
                                                color = GoldColorRw,
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp
                                            )
                                            is UiState.Error   -> Text("--", color = GoldColorRw, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                                            else               -> Text(
                                                userPoints.toString(),
                                                color = GoldColorRw,
                                                fontSize = 36.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Xem thể lệ", color = Color.White, fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textDecoration = TextDecoration.Underline)
                                        Spacer(Modifier.width(2.dp))
                                        Icon(Icons.Outlined.ChevronRight, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }

                                // Coin illustration
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Filled.Savings, null, tint = GoldColorRw, modifier = Modifier.size(52.dp))
                                        Spacer(Modifier.height(2.dp))
                                        Row {
                                            repeat(3) {
                                                Icon(Icons.Filled.MonetizationOn, null, tint = GoldColorRw, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (selectedTab == RewardScreenTab.REWARDS && showBanner) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp).offset(y = (-16).dp)) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 4.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Mua sắm để tích điểm và đổi quà vật tư y tế ngay hôm nay!",
                                            fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp
                                        )
                                        Spacer(Modifier.height(10.dp))
                                        Button(
                                            onClick = onShopNow,
                                            shape = RoundedCornerShape(50),
                                            colors = ButtonDefaults.buttonColors(containerColor = GreenTopRw),
                                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                                        ) {
                                            Text("Mua sắm ngay", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Box(
                                        modifier = Modifier.size(72.dp).clip(CircleShape).background(Color(0xFFE8F5E9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.HealthAndSafety, null, tint = GreenTopRw, modifier = Modifier.size(38.dp))
                                    }
                                }
                            }
                            IconButton(
                                onClick = { showBanner = false },
                                modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
                            ) {
                                Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                if (selectedTab == RewardScreenTab.REWARDS) {
                    // ── Filter chips ──────────────────────────────────────
                    item {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = if (showBanner) 0.dp else 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Quà tặng", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Tích điểm đổi quà với giá 1.000 VNĐ.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            val surfaceColor = MaterialTheme.colorScheme.surface
                            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
                            val outlineColor = MaterialTheme.colorScheme.outline
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pointFilters.forEach { filter ->
                                    val isSelected = selectedFilter == filter
                                    val bgColor by animateColorAsState(if (isSelected) GreenTopRw else surfaceColor, tween(200))
                                    val textColor by animateColorAsState(if (isSelected) Color.White else onSurfaceColor, tween(200))
                                    Surface(
                                        onClick = { selectedFilter = filter },
                                        shape = RoundedCornerShape(50),
                                        color = bgColor,
                                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, outlineColor),
                                        shadowElevation = if (isSelected) 2.dp else 0.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isSelected) {
                                                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                Spacer(Modifier.width(4.dp))
                                            }
                                            Text(filter, color = textColor, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Product loading state ─────────────────────────────
                    when (productsState) {
                        is UiState.Loading -> item {
                            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = GreenTopRw)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Đang tải danh sách quà...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                }
                            }
                        }
                        is UiState.Error -> item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFFF8E1),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Info, null, tint = Color(0xFFFF8F00), modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Đang hiển thị danh sách quà mẫu.\nDữ liệu thật sẽ tải khi có kết nối.",
                                            fontSize = 12.sp, color = Color(0xFF5D4037), lineHeight = 17.sp
                                        )
                                    }
                                }
                            }
                        }
                        else -> {}
                    }

                    // ── Product grid (API or fallback) ────────────────────
                    item {
                        Spacer(Modifier.height(12.dp))
                        val useApi = filteredApi.isNotEmpty()
                        if (useApi) {
                            val rows = filteredApi.chunked(2)
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rows.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowItems.forEach { product ->
                                            ApiRewardProductCard(
                                                product = product,
                                                userPoints = userPoints,
                                                onRedeem = {
                                                    redeemProductDto = product
                                                    redeemFallback = null
                                                    showRedeemSheet = true
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        } else {
                            val rows = filteredFallback.chunked(2)
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rows.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowItems.forEach { product ->
                                            FallbackRewardProductCard(
                                                product = product,
                                                userPoints = userPoints,
                                                onRedeem = {
                                                    redeemFallback = product
                                                    redeemProductDto = null
                                                    showRedeemSheet = true
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item {
                        RewardHistorySummaryCard(
                            currentPoints = userPoints,
                            totalEarnedPoints = totalEarnedPoints,
                            totalUsedPoints = totalUsedPoints,
                            redemptionCount = redemptions.size,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .offset(y = (-16).dp)
                        )
                    }

                    item {
                        MyRewardVouchersSection(
                            vouchersState = vouchersState,
                            vouchers = vouchers,
                            onUseVoucher = onUseVoucher
                        )
                    }

                    item {
                        RewardHistorySection(
                            title = "Lịch sử tích và tiêu điểm",
                            subtitle = "Theo dõi các lần cộng, trừ và điều chỉnh điểm."
                        ) {
                            when (val state = transactionsState) {
                                is UiState.Loading -> HistoryLoadingState("Đang tải giao dịch điểm...")
                                is UiState.Error -> HistoryErrorState(state.message)
                                is UiState.Success -> {
                                    if (state.data.isEmpty()) {
                                        EmptyHistoryState("Chưa có giao dịch điểm nào.")
                                    } else {
                                        state.data.take(8).forEachIndexed { index, transaction ->
                                            PointTransactionCard(transaction)
                                            if (index != state.data.take(8).lastIndex) {
                                                Spacer(Modifier.height(10.dp))
                                            }
                                        }
                                    }
                                }
                                else -> EmptyHistoryState("Chưa có dữ liệu giao dịch điểm.")
                            }
                        }
                    }

                    item {
                        RewardHistorySection(
                            title = "Lịch sử đổi quà",
                            subtitle = "Kiểm tra trạng thái các đơn đổi thưởng gần đây."
                        ) {
                            when (val state = redemptionsState) {
                                is UiState.Loading -> HistoryLoadingState("Đang tải lịch sử đổi quà...")
                                is UiState.Error -> HistoryErrorState(state.message)
                                is UiState.Success -> {
                                    if (state.data.isEmpty()) {
                                        EmptyHistoryState("Bạn chưa đổi quà nào bằng điểm thưởng.")
                                    } else {
                                        state.data.take(8).forEachIndexed { index, redemption ->
                                            RewardRedemptionCard(redemption)
                                            if (index != state.data.take(8).lastIndex) {
                                                Spacer(Modifier.height(10.dp))
                                            }
                                        }
                                    }
                                }
                                else -> EmptyHistoryState("Chưa có dữ liệu đổi quà.")
                            }
                        }
                    }
                }
            } // end LazyColumn
        } // end PullToRefreshBox

            // ── Redeem bottom sheet ───────────────────────────────────
            UnifiedRedeemSheet(
                visible       = showRedeemSheet,
                apiProduct    = redeemProductDto,
                localProduct  = redeemFallback,
                userPoints    = userPoints,
                isLoading     = redeemState is UiState.Loading,
                onDismiss     = { showRedeemSheet = false },
                onConfirm     = { id, qty ->
                    viewModel.redeemProduct(id, qty)
                }
            )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
        )
    }
}

// ── API product card ───────────────────────────────────────────────────────
@Composable
private fun ApiRewardProductCard(
    product: RewardProductDto,
    userPoints: Int,
    onRedeem: () -> Unit,
    modifier: Modifier = Modifier
) {
    val limitReached = product.usagePerUserLimit != null &&
        product.userRedemptionCount >= product.usagePerUserLimit
    val canRedeem = userPoints >= product.pointCost && !limitReached
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().height(110.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Outlined.CardGiftcard, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(product.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium,
                lineHeight = 16.sp, maxLines = 3, modifier = Modifier.heightIn(min = 48.dp))
            Spacer(Modifier.height(6.dp))
            Text("${product.pointCost.toLong().fmtPts()} điểm", fontSize = 12.sp, color = Color.Gray)
            if (product.usagePerUserLimit != null && product.usagePerUserLimit > 0) {
                Spacer(Modifier.height(2.dp))
                Text(
                    "Giới hạn: ${product.usagePerUserLimit} lần/người",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.EmojiEvents, null, tint = GoldColorRw, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    product.pointCost.toLong().fmtPts(),
                    fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = GoldColorRw
                )
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onRedeem,
                enabled = canRedeem,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenTopRw,
                    disabledContainerColor = Color(0xFFCCCCCC)
                ),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        limitReached -> "Đã đạt giới hạn"
                        canRedeem -> "Đổi ngay"
                        else -> "Thiếu điểm"
                    },
                    color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Fallback local card ────────────────────────────────────────────────────
@Composable
private fun FallbackRewardProductCard(
    product: RewardProduct,
    userPoints: Int,
    onRedeem: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().height(110.dp)
                    .clip(RoundedCornerShape(10.dp)).background(product.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(product.icon, null, tint = product.iconTint, modifier = Modifier.size(56.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(product.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium,
                lineHeight = 16.sp, maxLines = 3, modifier = Modifier.heightIn(min = 48.dp))
            Spacer(Modifier.height(6.dp))
            Text(product.priceText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GreenTopRw)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.EmojiEvents, null, tint = GoldColorRw, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    product.pointCost.toLong().fmtPts(),
                    fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = GoldColorRw
                )
            }
            Spacer(Modifier.height(10.dp))
            val canRedeem = userPoints >= product.pointCost
            Button(
                onClick = onRedeem,
                enabled = canRedeem,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenTopRw,
                    disabledContainerColor = Color(0xFFCCCCCC)
                ),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (canRedeem) "Đổi ngay" else "Thiếu điểm",
                    color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun Long.fmtPts() = String.format("%,d", this).replace(',', '.')

// ── Header tab ────────────────────────────────────────────────────────────
@Composable
private fun HeaderTab(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) Color.White else Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = if (selected) GreenTopRw else Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = if (selected) GreenTopRw else Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private val rewardHistoryDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

@Composable
private fun RewardHistorySummaryCard(
    currentPoints: Int,
    totalEarnedPoints: Int,
    totalUsedPoints: Int,
    redemptionCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Tổng quan điểm thưởng",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RewardHistoryMetricCard(
                    title = "Hiện có",
                    value = currentPoints.toLong().fmtPts(),
                    accent = GoldColorRw,
                    modifier = Modifier.weight(1f)
                )
                RewardHistoryMetricCard(
                    title = "Đã tích",
                    value = totalEarnedPoints.toLong().fmtPts(),
                    accent = GreenTopRw,
                    modifier = Modifier.weight(1f)
                )
                RewardHistoryMetricCard(
                    title = "Đã đổi",
                    value = totalUsedPoints.toLong().fmtPts(),
                    accent = Color(0xFFE65100),
                    modifier = Modifier.weight(1f)
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Lượt đổi quà đã tạo",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Text(
                        redemptionCount.toString(),
                        color = GreenTopRw,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardHistoryMetricCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Text(value, color = accent, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun RewardHistorySection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 18.sp)
                content()
            }
        )
    }
}

@Composable
private fun MyRewardVouchersSection(
    vouchersState: UiState<List<RewardVoucherDto>>,
    vouchers: List<RewardVoucherDto>,
    onUseVoucher: (String) -> Unit
) {
    RewardHistorySection(
        title = "Voucher của tôi",
        subtitle = "Mã đã đổi bằng điểm sẽ hiện ở đây và có thể chọn lại trong màn thanh toán."
    ) {
        when (vouchersState) {
            is UiState.Loading -> HistoryLoadingState("Đang tải voucher đã đổi...")
            is UiState.Error -> HistoryErrorState(vouchersState.message)
            is UiState.Success -> {
                if (vouchers.isEmpty()) {
                    EmptyHistoryState("Chưa có voucher nào. Đổi voucher trong catalog điểm thưởng để dùng khi checkout.")
                } else {
                    vouchers.forEachIndexed { index, voucher ->
                        RewardVoucherCard(voucher = voucher, onUseVoucher = onUseVoucher)
                        if (index != vouchers.lastIndex) Spacer(Modifier.height(10.dp))
                    }
                }
            }
            else -> EmptyHistoryState("Chưa có dữ liệu voucher.")
        }
    }
}

@Composable
private fun RewardVoucherCard(voucher: RewardVoucherDto, onUseVoucher: (String) -> Unit) {
    val normalizedStatus = voucher.status.uppercase()
    val used = normalizedStatus == "USED"
    val unusable = normalizedStatus in setOf("USED", "CANCELLED", "EXPIRED")
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (unusable) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(voucher.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(voucher.code, color = GreenTopRw, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text(formatRewardVoucherValue(voucher), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                RedemptionStatusChip(normalizedStatus)
            }
            voucher.terms?.takeIf { it.isNotBlank() }?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 18.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Nhận lúc: ${formatRewardDateTime(voucher.createdAt)}",
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.sp
                )
                Button(
                    onClick = { onUseVoucher(voucher.code) },
                    enabled = !unusable,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTopRw),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        when {
                            used -> "Đã dùng"
                            unusable -> "Không dùng được"
                            else -> "Dùng voucher"
                        },
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private fun formatRewardVoucherValue(voucher: RewardVoucherDto): String {
    val value = if (voucher.discountType.uppercase() == "PERCENT") {
        "Giảm ${voucher.discountValue.toLong()}%"
    } else {
        "Giảm ${voucher.discountValue.toLong().fmtPts()} đ"
    }
    val minOrder = voucher.minOrderTotal?.let { " | Đơn tối thiểu ${it.toLong().fmtPts()} đ" }.orEmpty()
    val maxDiscount = voucher.maxDiscountAmount?.let { " | Giảm tối đa ${it.toLong().fmtPts()} đ" }.orEmpty()
    return value + minOrder + maxDiscount
}

@Composable
private fun HistoryLoadingState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = GreenTopRw, modifier = Modifier.size(26.dp), strokeWidth = 2.dp)
            Spacer(Modifier.height(8.dp))
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
private fun HistoryErrorState(message: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF5F5)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.ErrorOutline, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
            Text(message, color = Color(0xFF8B1E1E), fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun EmptyHistoryState(message: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Outlined.HourglassEmpty, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(8.dp))
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PointTransactionCard(transaction: PointTransactionDto) {
    val transactionType = transaction.type.uppercase()
    val accentColor = when (transactionType) {
        "EARN" -> GreenTopRw
        "REDEEM" -> Color(0xFFE65100)
        "EXPIRE" -> Color(0xFF757575)
        else -> Color(0xFF1565C0)
    }
    val icon = when (transactionType) {
        "EARN" -> Icons.Filled.AddCircle
        "REDEEM" -> Icons.Filled.Redeem
        "EXPIRE" -> Icons.Filled.TimerOff
        else -> Icons.Filled.Tune
    }
    val pointsLabel = buildString {
        if (transaction.points > 0) append("+")
        append(transaction.points.toLong().fmtPts())
    }
    val reference = transaction.orderId?.let { "Đơn hàng ${shortRewardId(it)}" }
        ?: transaction.redemptionId?.let { "Phiếu đổi ${shortRewardId(it)}" }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = accentColor, modifier = Modifier.size(18.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            formatRewardTransactionType(transactionType),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            transaction.description ?: "Không có mô tả giao dịch.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
                Text(
                    pointsLabel,
                    color = accentColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatRewardDateTime(transaction.createdAt),
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.sp
                )
                if (reference != null) {
                    Text(
                        reference,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardRedemptionCard(redemption: RewardRedemptionHistoryDto) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        redemption.productName,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Số lượng: ${redemption.quantity} • -${redemption.pointsUsed.toLong().fmtPts()} điểm",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                RedemptionStatusChip(redemption.status)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Mã đổi: ${shortRewardId(redemption.id)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Text(
                    formatRewardDateTime(redemption.createdAt),
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun RedemptionStatusChip(status: String) {
    val normalized = status.uppercase()
    val background = when (normalized) {
        "DELIVERED" -> Color(0xFFE8F5E9)
        "SHIPPED", "APPROVED" -> Color(0xFFE3F2FD)
        "PROCESSING" -> Color(0xFFFFF3E0)
        "CANCELLED" -> Color(0xFFFFEBEE)
        else -> Color(0xFFF3F4F6)
    }
    val textColor = when (normalized) {
        "DELIVERED" -> GreenTopRw
        "SHIPPED", "APPROVED" -> Color(0xFF1565C0)
        "PROCESSING" -> Color(0xFFE65100)
        "CANCELLED" -> Color(0xFFD32F2F)
        else -> Color(0xFF6B7280)
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = background
    ) {
        Text(
            text = formatRewardRedemptionStatus(normalized),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

private fun formatRewardTransactionType(type: String): String = when (type.uppercase()) {
    "EARN" -> "Tích điểm"
    "REDEEM" -> "Dùng điểm đổi quà"
    "EXPIRE" -> "Điểm hết hạn"
    "ADJUST" -> "Điều chỉnh điểm"
    else -> type
}

private fun formatRewardRedemptionStatus(status: String): String = when (status.uppercase()) {
    "PROCESSING" -> "Đang xử lý"
    "APPROVED" -> "Đã duyệt"
    "SHIPPED" -> "Đang giao"
    "DELIVERED" -> "Hoàn tất"
    "CANCELLED" -> "Đã hủy"
    else -> status
}

private fun shortRewardId(value: String): String {
    return if (value.length <= 8) value else value.takeLast(8)
}

private fun formatRewardDateTime(value: String): String = formatVnDateTime(value)

// ── Redeem bottom sheet (unified for API and fallback) ────────────────────
@Composable
fun UnifiedRedeemSheet(
    visible: Boolean,
    apiProduct: RewardProductDto?,
    localProduct: RewardProduct?,
    userPoints: Int,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (productId: String, quantity: Int) -> Unit
) {
    val sheetHeightDp = 430.dp
    val density       = LocalDensity.current
    val sheetHeightPx = with(density) { sheetHeightDp.toPx() }
    val offsetY       = remember { Animatable(sheetHeightPx) }
    val scrimAlpha    = remember { Animatable(0f) }
    val scope         = rememberCoroutineScope()
    var quantity      by remember { mutableIntStateOf(1) }

    LaunchedEffect(apiProduct?.id, localProduct?.id) { quantity = 1 }

    LaunchedEffect(visible) {
        if (visible) {
            scope.launch {
                launch { offsetY.animateTo(0f, tween(320, easing = FastOutSlowInEasing)) }
                launch { scrimAlpha.animateTo(0.45f, tween(280)) }
            }
        } else {
            scope.launch {
                launch { offsetY.animateTo(sheetHeightPx, tween(260, easing = FastOutLinearInEasing)) }
                launch { scrimAlpha.animateTo(0f, tween(240)) }
            }
        }
    }

    val productName = apiProduct?.name ?: localProduct?.name ?: ""
    val pointCost   = apiProduct?.pointCost ?: localProduct?.pointCost ?: 0

    // Max quantity = min(remaining redemptions allowed, max affordable by points)
    val remainingRedemptions: Int? = apiProduct?.usagePerUserLimit?.let { limit ->
        (limit - (apiProduct.userRedemptionCount)).coerceAtLeast(0)
    }
    val maxByPoints = if (pointCost > 0) (userPoints / pointCost).coerceAtLeast(1) else 1
    val maxQuantity = minOf(remainingRedemptions ?: Int.MAX_VALUE, maxByPoints).coerceAtLeast(1)

    LaunchedEffect(maxQuantity) { if (quantity > maxQuantity) quantity = maxQuantity }

    val totalCost   = pointCost * quantity
    val hasEnough   = userPoints >= totalCost

    if (offsetY.value < sheetHeightPx || visible) {
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha.value))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        scope.launch {
                            launch { offsetY.animateTo(sheetHeightPx, tween(260, easing = FastOutLinearInEasing)) }
                            launch { scrimAlpha.animateTo(0f, tween(240)) }
                        }
                        onDismiss()
                    }
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth().height(sheetHeightDp)
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, offsetY.value.roundToInt()) },
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 16.dp
            ) {
                Column(Modifier.fillMaxSize()) {
                    // Handle bar
                    Box(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                            .padding(top = 10.dp).width(36.dp).height(4.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
                    )
                    // Title
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp)) {
                        Text("Đổi quà", fontSize = 17.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.Center))
                        IconButton(
                            onClick = {
                                scope.launch {
                                    launch { offsetY.animateTo(sheetHeightPx, tween(260, easing = FastOutLinearInEasing)) }
                                    launch { scrimAlpha.animateTo(0f, tween(240)) }
                                }
                                onDismiss()
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                        }
                    }
                    HorizontalDivider()

                    // Product row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(10.dp))
                                .background(localProduct?.iconBg ?: Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                localProduct?.icon ?: Icons.Outlined.CardGiftcard,
                                null,
                                tint = localProduct?.iconTint ?: GreenTopRw,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                        Text(productName, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider()

                    // Quantity
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Số lượng:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            if (remainingRedemptions != null) {
                                Text(
                                    "Còn ${remainingRedemptions} lần đổi",
                                    fontSize = 12.sp,
                                    color = if (remainingRedemptions > 0) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFE53935)
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Surface(onClick = { if (quantity > 1) quantity-- }, shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(34.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Remove, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                                }
                            }
                            Text(quantity.toString(), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Surface(
                                onClick = { if (quantity < maxQuantity) quantity++ },
                                shape = RoundedCornerShape(8.dp),
                                color = if (quantity < maxQuantity) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Add, null,
                                        tint = if (quantity < maxQuantity) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                    HorizontalDivider()

                    // Points used
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Điểm sử dụng:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.EmojiEvents, null, tint = GoldColorRw, modifier = Modifier.size(20.dp))
                            Text("-${totalCost.toLong().fmtPts()}", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = GoldColorRw)
                        }
                    }

                    // Points current
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Điểm hiện có:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(userPoints.toString(), fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (!hasEnough) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Error, null, tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                            Text("Không đủ điểm thưởng.", fontSize = 13.sp, color = Color(0xFFE53935))
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Confirm button
                    Button(
                        onClick = {
                            val id = apiProduct?.id ?: localProduct?.id?.toString() ?: return@Button
                            onConfirm(id, quantity)
                        },
                        enabled = hasEnough && !isLoading,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenTopRw,
                            disabledContainerColor = Color(0xFFDDDDDD)
                        ),
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 20.dp).padding(bottom = 20.dp).height(52.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Xác nhận", fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                                color = if (hasEnough) Color.White else Color(0xFF888888))
                        }
                    }
                }
            }
        }
    }
}
