package com.example.nhathuoc.ui.screen

import android.net.Uri
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nhathuoc.ui.component.*
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.NhathuocTheme
import com.example.nhathuoc.viewmodel.CartViewModel
import com.example.nhathuoc.viewmodel.HomeViewModel
import com.example.nhathuoc.viewmodel.NotificationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val HEADER_FULL = 160.dp
private val HEADER_MID = 110.dp
private val HEADER_COLLAPSED = 72.dp

@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavController? = null) {
    val drawerState = rememberDrawerState()
    val notificationViewModel: NotificationViewModel = hiltViewModel()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val unreadNotificationCount by notificationViewModel.unreadCount.collectAsState()
    val rewardPoints by homeViewModel.rewardPoints.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            homeViewModel.refreshRewardAccount()
            notificationViewModel.loadNotifications()
            delay(5_000)
        }
    }

    AppDrawer(
        drawerState = drawerState,
        userName = "Khách hàng",
        rewardPoints = rewardPoints,
        notificationCount = unreadNotificationCount,
        onMenuItemClick = { item ->
            if (item.label != "Thông báo") {
                drawerState.close()
                when (item.label) {
                    else -> {
                        val categoryId = item.categoryId ?: item.label
                        navController?.navigate("CategoryProductScreen/${Uri.encode(categoryId)}/${Uri.encode(item.label)}")
                    }
                }
            }
            if (item.label == "Thông báo") {
                drawerState.close()
                navController?.navigate("NotificationScreen")
            }
        }
    ) {
        HomeScreenContent(
            modifier = modifier,
            onMenuClick = { drawerState.toggle() },
            onChatClick = { navController?.navigate("ChatScreen") },
            onNotificationClick = { navController?.navigate("NotificationScreen") },
            notificationCount = unreadNotificationCount,
            rewardPoints = rewardPoints,
            navController = navController
        )
    }
}

@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    notificationCount: Int = 0,
    rewardPoints: Int = 0,
    navController: NavController? = null
) {
    val listState = rememberLazyListState()

    val scrollOffset by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex * 1000 + listState.firstVisibleItemScrollOffset
        }
    }
    val collapseLevel by remember {
        derivedStateOf {
            when {
                scrollOffset < 100 -> 0f
                scrollOffset < 400 -> (scrollOffset - 100f) / 300f
                scrollOffset < 800 -> 1f + (scrollOffset - 400f) / 400f
                else -> 2f
            }.coerceIn(0f, 2f)
        }
    }

    val hintAlpha = if (collapseLevel < 0.5f) 1f - collapseLevel * 2f else 0f
    val logoAlpha = if (collapseLevel < 1.2f) 1f else 1f - (collapseLevel - 1.2f) / 0.8f
    val compactAlpha = if (collapseLevel > 1.5f) (collapseLevel - 1.5f) / 0.5f else 0f

    val headerHeight = when {
        collapseLevel <= 1f -> HEADER_FULL - (HEADER_FULL - HEADER_MID) * collapseLevel
        else -> HEADER_MID - (HEADER_MID - HEADER_COLLAPSED) * (collapseLevel - 1f)
    }

    // ViewModels
    val cartViewModel: CartViewModel = hiltViewModel()
    val homeViewModel: HomeViewModel = hiltViewModel()

    // Collect real data from HomeViewModel
    val flashSaleProducts by homeViewModel.flashSaleProducts.collectAsState()
    val flashSaleLoading by homeViewModel.flashSaleLoading.collectAsState()
    val bestSellerProducts by homeViewModel.bestSellerProducts.collectAsState()
    val bestSellerLoading by homeViewModel.bestSellerLoading.collectAsState()
    val banners by homeViewModel.banners.collectAsState()
    val recentOrders by homeViewModel.recentOrders.collectAsState()
    val promoItems = remember(banners) {
        banners
            .sortedBy { it.sortOrder }
            .map { it.toPromoBannerItem() }
            .ifEmpty { defaultPromoItems }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(top = HEADER_FULL + 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GreetingCard(userName = "Bạn", rewardPoints = rewardPoints, navController)
                    ChatBanner(hasNewMessage = true, onChatClick = onChatClick)
                }
            }
            item {
                PromoBannerPager(
                    items = promoItems,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onBannerClick = { item ->
                        val link = item.linkUrl?.trim().orEmpty()
                        when {
                            link.equals("/chat", ignoreCase = true) -> navController?.navigate("ChatScreen")
                            link.equals("/ai-chat", ignoreCase = true) -> navController?.navigate("AiChatScreen")
                            link.equals("/cart", ignoreCase = true) -> navController?.navigate("CartScreen")
                            link.equals("/rewards", ignoreCase = true) ||
                                link.equals("/reward", ignoreCase = true) -> navController?.navigate("RewardScreen")
                            link.equals("/orders", ignoreCase = true) -> navController?.navigate("MyOrdersScreen")
                            link.equals("/complaints", ignoreCase = true) -> navController?.navigate("MyComplaintsScreen")
                            link.equals("/notifications", ignoreCase = true) -> navController?.navigate("NotificationScreen")
                            link.equals("/profile", ignoreCase = true) -> navController?.navigate("ProfileScreen")
                            link.equals("/flash-sale", ignoreCase = true) -> navController?.navigate("ProductListScreen")
                            link.startsWith("/products/", ignoreCase = true) -> {
                                navController?.navigate("ProductDetailScreen/${Uri.encode(link.substringAfterLast('/'))}")
                            }
                            link.startsWith("/categories/", ignoreCase = true) -> {
                                val categoryId = link.substringAfterLast('/')
                                navController?.navigate(
                                    "CategoryProductScreen/${Uri.encode(categoryId)}/${Uri.encode(item.title)}"
                                )
                            }
                            link.equals("/products", ignoreCase = true) -> navController?.navigate("ProductListScreen")
                        }
                    }
                )
            }
            item {
                FlashSaleRow(
                    navController = navController,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    products = flashSaleProducts,
                    isLoading = flashSaleLoading,
                    onSeeAll = { navController?.navigate("ProductListScreen") },
                    onAddToCart = { product ->
                        scope.launch {
                            cartViewModel.addToCart(
                                productId = product.id,
                                quantity = 1,
                                unit = product.unit
                            )
                            snackbarHostState.showSnackbar("Đã thêm \"${product.name.take(30)}\" vào giỏ hàng.")
                        }
                    }
                )
            }
            item {
                RecentOrdersRow(
                    orders = recentOrders,
                    navController = navController,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onSeeAll = { navController?.navigate("OrderHistoryScreen") },
                    onReorder = { order ->
                        scope.launch {
                            val unavailable = order.items.filter { item ->
                                val stock = item.product?.stock ?: return@filter false
                                stock < item.quantity
                            }
                            if (unavailable.isNotEmpty()) {
                                val msg = if (unavailable.size == 1) {
                                    val item = unavailable.first()
                                    val stock = item.product?.stock ?: 0
                                    if (stock == 0) "\"${item.name.take(30)}\" hiện hết hàng"
                                    else "\"${item.name.take(30)}\" chỉ còn $stock ${item.unit}"
                                } else {
                                    "${unavailable.size} sản phẩm không đủ hàng để đặt lại"
                                }
                                snackbarHostState.showSnackbar(msg)
                            } else {
                                order.items.forEach { item ->
                                    cartViewModel.addToCart(
                                        productId = item.productId,
                                        quantity = item.quantity,
                                        unit = item.unit
                                    )
                                }
                                navController?.navigate("CartScreen")
                            }
                        }
                    }
                )
            }
            item {
                BestSellerList(
                    navController = navController,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    products = bestSellerProducts,
                    isLoading = bestSellerLoading,
                    onSeeAll = { navController?.navigate("ProductListScreen") },
                    onAddToCart = { product ->
                        scope.launch {
                            cartViewModel.addToCart(
                                productId = product.id,
                                quantity = 1,
                                unit = product.unit
                            )
                            snackbarHostState.showSnackbar("Đã thêm \"${product.name.take(30)}\" vào giỏ hàng.")
                        }
                    }
                )
            }
            item { TrustBadgesGrid(modifier = Modifier.padding(horizontal = 16.dp)) }
            item { HomeFooter(modifier = Modifier.padding(horizontal = 16.dp)) }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(logoAlpha)
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp)
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Filled.Menu, "Menu", tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("VẬT TƯ Y TẾ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                    Text("MedStore", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                }
                HomeNotificationButton(
                    notificationCount = notificationCount,
                    onClick = onNotificationClick,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .alpha(logoAlpha)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = if (collapseLevel < 1f) (14 + 24 * (1f - collapseLevel)).dp else 14.dp)
            ) {
                HomeSearchBar(onClick = { navController?.navigate("ProductListScreen") })
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .alpha(hintAlpha)
                    .padding(bottom = 8.dp, start = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(4.dp))
                Text("Thanh toán điện tử", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .alpha(compactAlpha)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Filled.Menu, null, tint = Color.White)
                }
                Box(modifier = Modifier.weight(1f)) {
                    HomeSearchBar(onClick = { navController?.navigate("ProductListScreen") })
                }
                HomeNotificationButton(
                    notificationCount = notificationCount,
                    onClick = onNotificationClick
                )
            }
        }
    }
}

@Composable
private fun HomeNotificationButton(
    notificationCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayCount = notificationCount.coerceAtMost(99).toString()
    if (notificationCount > 0) {
        BadgedBox(
            badge = {
                Badge(containerColor = Color(0xFFFF6D00)) {
                    Text(displayCount, color = Color.White, fontSize = 9.sp)
                }
            },
            modifier = modifier
        ) {
            IconButton(onClick = onClick) {
                Icon(Icons.Filled.Notifications, "Thông báo", tint = Color.White, modifier = Modifier.size(26.dp))
            }
        }
    } else {
        IconButton(onClick = onClick, modifier = modifier) {
            Icon(Icons.Filled.Notifications, "Thông báo", tint = Color.White, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun HomeSearchBar(onClick: () -> Unit = {}) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().height(44.dp).clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Tìm thiết bị, vật tư y tế...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    NhathuocTheme { HomeScreen() }
}
