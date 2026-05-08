package org.example.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import org.example.project.presentation.viewmodels.NetworkModule
import org.example.project.ui.layout.MainLayout
import org.example.project.ui.screens.BannerManagementScreen
import org.example.project.ui.screens.ChatScreen
import org.example.project.ui.screens.CouponManagementScreen
import org.example.project.ui.screens.FinanceAdminScreen
import org.example.project.ui.screens.LoginScreen
import org.example.project.ui.screens.OrdersScreen
import org.example.project.ui.screens.OperationsModerationScreen
import org.example.project.ui.screens.PersonnelManagementScreen
import org.example.project.ui.screens.PosWorkspaceScreen
import org.example.project.ui.screens.ProductsScreen
import org.example.project.ui.screens.StoreOverviewScreen

private val adminRoutes = setOf("dashboard", "orders", "products", "pos", "banners", "coupons", "finance", "chat", "ops", "personnel")
private val employeeRoutes = setOf("orders", "products", "pos", "chat", "ops")

private fun allowedRoutesForRole(role: String): Set<String> {
    return if (role == "ADMIN") adminRoutes else employeeRoutes
}

private fun defaultRouteForRole(role: String): String {
    return if (role == "ADMIN") "dashboard" else "orders"
}

val AppLightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF66BB6A),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF4CAF50),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFA5D6A7),
    onSecondaryContainer = Color(0xFF2E7D32),
    tertiary = Color(0xFFFFAB00),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFFFFF8E1),
    onTertiaryContainer = Color(0xFFEF6C00),
    error = Color(0xFFF44336),
    errorContainer = Color(0xFFFFEBEE),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFFD32F2F),
    background = Color(0xFFF2F4F7),
    onBackground = Color(0xFF1B5E20),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B5E20),
    surfaceVariant = Color(0xFFF1F8E9),
    onSurfaceVariant = Color(0xFF2E7D32),
    outline = Color(0xFF81C784),
    inverseOnSurface = Color(0xFFE8F5E8),
    inverseSurface = Color(0xFF2E7D32),
    inversePrimary = Color(0xFF66BB6A),
)

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = AppLightColorScheme) {
        var currentRoute by remember { mutableStateOf("dashboard") }

        val loginViewModel = remember { NetworkModule.loginViewModel() }
        val dashboardViewModel = remember { NetworkModule.dashboardViewModel() }
        val ordersViewModel = remember { NetworkModule.ordersViewModel() }
        val productsViewModel = remember { NetworkModule.productsViewModel() }
        val posViewModel = remember { NetworkModule.posViewModel() }
        val couponAdminViewModel = remember { NetworkModule.couponAdminViewModel() }
        val financeViewModel = remember { NetworkModule.financeDashboardViewModel() }
        val personnelViewModel = remember { NetworkModule.personnelViewModel() }
        val chatViewModel = remember { NetworkModule.chatViewModel() }
        val operationsViewModel = remember { NetworkModule.operationsViewModel() }
        val bannerViewModel = remember { NetworkModule.bannerViewModel() }
        val session by NetworkModule.sessionManager.session.collectAsState()

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (session == null) {
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = { role ->
                        currentRoute = if (role == "ADMIN") "dashboard" else "orders"
                    }
                )
            } else {
                val role = session!!.role.uppercase()
                val allowedRoutes = allowedRoutesForRole(role)

                LaunchedEffect(currentRoute, role) {
                    if (currentRoute !in allowedRoutes) {
                        currentRoute = defaultRouteForRole(role)
                    }
                }

                LaunchedEffect(session!!.accessToken, role) {
                    dashboardViewModel.fetchDashboardData()
                    ordersViewModel.loadOrders()
                    productsViewModel.setUserRole(role)
                    productsViewModel.refreshData()
                    posViewModel.loadProducts()
                    couponAdminViewModel.loadData()
                    financeViewModel.loadSummary()
                    personnelViewModel.loadUsers()
                    operationsViewModel.loadAll()
                    bannerViewModel.loadBanners()
                    chatViewModel.loadConversations()
                }

                MainLayout(
                    userRole = role,
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        currentRoute = if (route in allowedRoutes) route else defaultRouteForRole(role)
                    },
                    onLogout = {
                        loginViewModel.logout()
                        currentRoute = "dashboard"
                    }
                ) {
                    when (currentRoute) {
                        "dashboard" -> StoreOverviewScreen(viewModel = dashboardViewModel)
                        "orders" -> OrdersScreen(viewModel = ordersViewModel)
                        "products" -> ProductsScreen(viewModel = productsViewModel)
                        "pos" -> PosWorkspaceScreen(viewModel = posViewModel)
                        "banners" -> BannerManagementScreen(viewModel = bannerViewModel)
                        "coupons" -> CouponManagementScreen(viewModel = couponAdminViewModel)
                        "finance" -> FinanceAdminScreen(viewModel = financeViewModel)
                        "chat" -> ChatScreen(viewModel = chatViewModel)
                        "ops" -> OperationsModerationScreen(viewModel = operationsViewModel)
                        "personnel" -> PersonnelManagementScreen(viewModel = personnelViewModel)
                        else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Tính năng đang phát triển") }
                    }
                }
            }
        }
    }
}
