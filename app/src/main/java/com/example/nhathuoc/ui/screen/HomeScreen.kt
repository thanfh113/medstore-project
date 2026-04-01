package com.example.nhathuoc.ui.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.navigation.NavController
import com.example.nhathuoc.ui.component.*
import com.example.nhathuoc.ui.theme.NhathuocTheme
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.GreenLight

private val HEADER_FULL      = 160.dp
private val HEADER_MID       = 110.dp
private val HEADER_COLLAPSED = 72.dp

@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavController? = null) {
    val drawerState = rememberDrawerState()

    AppDrawer(
        drawerState = drawerState,
        userName = "Uesr",
        rewardPoints = 246,
        notificationCount = 3,
        onMenuItemClick = { item ->
            if (item.label == "Thông báo") {
                drawerState.close()
                navController?.navigate("NotificationScreen")
            }
        }
    ) {
        HomeScreenContent(
            modifier            = modifier,
            onMenuClick         = { drawerState.toggle() },
            onChatClick         = { navController?.navigate("ChatScreen") },
            onNotificationClick = { navController?.navigate("NotificationScreen") },
            navController       = navController
        )
    }
}

@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
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
                scrollOffset < 100  -> 0f
                scrollOffset < 400  -> (scrollOffset - 100f) / 300f
                scrollOffset < 800  -> 1f + (scrollOffset - 400f) / 400f
                else                -> 2f
            }.coerceIn(0f, 2f)
        }
    }

    val hintAlpha    by animateFloatAsState(if (collapseLevel < 0.5f) 1f - collapseLevel * 2f else 0f, tween(150))
    val logoAlpha    by animateFloatAsState(if (collapseLevel < 1.2f) 1f else 1f - (collapseLevel - 1.2f) / 0.8f, tween(150))
    val compactAlpha by animateFloatAsState(if (collapseLevel > 1.5f) (collapseLevel - 1.5f) / 0.5f else 0f, tween(150))

    val headerHeight by animateDpAsState(
        targetValue = when {
            collapseLevel <= 1f -> HEADER_FULL - (HEADER_FULL - HEADER_MID) * collapseLevel
            else                -> HEADER_MID  - (HEADER_MID  - HEADER_COLLAPSED) * (collapseLevel - 1f)
        },
        animationSpec = tween(100)
    )

    Box(modifier = modifier.fillMaxSize()) {

        // ── Content ─────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA)),
            contentPadding = PaddingValues(top = HEADER_FULL + 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GreetingCard(userName = "Thành", rewardPoints = 246, navController)
                    ChatBanner(hasNewMessage = true, onChatClick = onChatClick)
                }
            }
            item { QuickAccessRow(navController = navController, modifier = Modifier.padding(horizontal = 16.dp)) }
            item { PromoBannerPager(modifier = Modifier.padding(horizontal = 16.dp)) }
            item {
                FlashSaleRow(
                    navController = navController,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item { RecentOrdersRow(navController = navController, modifier = Modifier.padding(horizontal = 16.dp)) }
            item { BestSellerList(navController = navController, modifier = Modifier.padding(horizontal = 16.dp)) }
            item { FeaturedCategoriesGrid(navController = navController, modifier = Modifier.padding(horizontal = 16.dp)) }
            item { HealthCheckRow(modifier = Modifier.padding(horizontal = 16.dp)) }
            item { SeasonalDiseaseSection(navController = navController, modifier = Modifier.padding(horizontal = 16.dp)) }
            item { ShortVideoRow(modifier = Modifier.padding(horizontal = 16.dp)) }
            item { HealthNewsSection(modifier = Modifier.padding(horizontal = 16.dp)) }
            item { TrustBadgesGrid(modifier = Modifier.padding(horizontal = 16.dp)) }
            item { HomeFooter(modifier = Modifier.padding(horizontal = 16.dp)) }
        }

        // ── Collapsing TopAppBar ─────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
        ) {
            // Full / mid
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
                BadgedBox(
                    badge = { Badge(containerColor = Color(0xFFFF6D00)) { Text("3", color = Color.White, fontSize = 9.sp) } },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    IconButton(onClick = onNotificationClick) {
                        Icon(Icons.Filled.Notifications, "Thông báo", tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .alpha(logoAlpha)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = if (collapseLevel < 1f) (14 + 24 * (1f - collapseLevel)).dp else 14.dp)
            ) {
                HomeSearchBar()
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .alpha(hintAlpha)
                    .padding(bottom = 8.dp, start = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Thanh toán điện tử - Giao hàng toàn quốc ", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                Text("Đặt ngay", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline)
            }

            // Collapsed
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
                Box(modifier = Modifier.weight(1f)) { HomeSearchBar() }
                BadgedBox(badge = {
                    Badge(containerColor = Color(0xFFFF6D00)) { Text("3", color = Color.White, fontSize = 9.sp) }
                }) {
                    IconButton(onClick = onNotificationClick) {
                        Icon(Icons.Filled.Notifications, null, tint = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
private fun HomeSearchBar() {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().height(44.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Search, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Tìm thiết bị, vật tư, dụng cụ y tế...", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.Mic, "Voice", tint = GreenTop, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Icon(Icons.Outlined.CameraAlt, "Camera", tint = GreenTop, modifier = Modifier.size(20.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    NhathuocTheme { HomeScreen() }
}