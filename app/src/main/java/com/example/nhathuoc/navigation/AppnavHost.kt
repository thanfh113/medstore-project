package com.example.nhathuoc.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nhathuoc.ui.component.defaultFlashSaleProducts
import com.example.nhathuoc.ui.component.defaultBestSellers
import com.example.nhathuoc.ui.component.defaultDiseases
import com.example.nhathuoc.ui.screen.miniscreen.categoryScreenDataMap
import com.example.nhathuoc.ui.screen.MainScreen
import com.example.nhathuoc.ui.screen.miniscreen.*
import com.example.nhathuoc.util.AuthenticatedAction

@Composable
fun AppnavHost(navController: NavHostController) {
    // Lưu product context tạm khi navigate từ ProductDetail → ChatScreen
    var pendingProductContext by remember { mutableStateOf<ChatProductContext?>(null) }
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = "MainScreen"
    ) {
        // Authentication screens
        composable("LoginScreen") {
            LoginScreen(navController = navController)
        }
        composable("RegisterScreen") {
            RegisterScreen(navController = navController)
        }

        // Main app screen
        composable("MainScreen") {
            MainScreen(navController)
        }
        composable("ChatScreen") {
            // Lấy context nếu có (từ ProductDetail), sau đó clear
            val ctx = pendingProductContext
            ChatScreen(
                onBack          = { navController.popBackStack() },
                productContext  = ctx
            )
        }
        composable("BuyMedicineScreen") {
            BuyMedicineScreen(onBack = { navController.popBackStack() })
        }
        composable("VaccineScreen") {
            VaccineScreen(onBack = { navController.popBackStack() })
        }
        composable("MyOrdersScreen") {
            MyOrdersScreen(onBack = { navController.popBackStack() })
        }
        composable("FindPharmacyScreen") {
            FindPharmacyScreen(onBack = { navController.popBackStack() })
        }
        composable("NotificationScreen") {
            NotificationScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = "ProductDetailScreen/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 1
            // FlashSale ids: 1-6 | BestSeller ids: 101-103 | Disease ids: 201-208
            val detail = if (productId >= 100) {
                val bs = defaultBestSellers.find { it.id == productId } ?: defaultBestSellers.first()
                ProductDetail(
                    id              = "BS${productId}",
                    name            = bs.name,
                    brand           = "Long Châu",
                    origin          = "Việt Nam",
                    price           = bs.price,
                    originalPrice   = bs.originalPrice,
                    discountPercent = bs.discountAmount.replace("-","").replace("%","").toIntOrNull() ?: 0,
                    unit            = "Hộp",
                    rewardPoints    = bs.price.replace(".","").replace("đ","").toIntOrNull()?.div(1000) ?: 0,
                    icon            = bs.icon,
                    iconTint        = bs.iconTint,
                    iconBg          = bs.iconBg
                )
            } else if (productId >= 200) {
                val dp = defaultDiseases.flatMap { it.products }.find { it.id == productId }
                    ?: defaultDiseases.first().products.first()
                ProductDetail(
                    id              = "DS${productId}",
                    name            = dp.name,
                    brand           = "Long Châu",
                    origin          = "Việt Nam",
                    price           = dp.price,
                    originalPrice   = dp.originalPrice ?: dp.price,
                    discountPercent = dp.discountPercent ?: 0,
                    unit            = "Hộp",
                    rewardPoints    = dp.price.replace(".","").replace("đ","").toIntOrNull()?.div(1000) ?: 0,
                    icon            = dp.icon,
                    iconTint        = dp.iconTint,
                    iconBg          = dp.iconBg
                )
            } else {
                val fp = defaultFlashSaleProducts.find { it.id == productId } ?: defaultFlashSaleProducts.first()
                ProductDetail(
                    id              = "0004${productId.toString().padStart(4, '0')}",
                    name            = fp.name,
                    brand           = fp.brand,
                    origin          = fp.origin,
                    price           = fp.price,
                    originalPrice   = fp.originalPrice,
                    discountPercent = fp.discountPercent,
                    unit            = fp.unit,
                    rewardPoints    = fp.rewardPoints,
                    icon            = fp.icon,
                    iconTint        = fp.iconTint,
                    iconBg          = fp.iconBg
                )
            }

            ProductDetailScreen(
                product        = detail,
                onBack         = { navController.popBackStack() },
                onChat         = { ctx ->
                    pendingProductContext = ctx
                    navController.navigate("ChatScreen")
                },
                onFindPharmacy = { navController.navigate("FindPharmacyScreen") },
                onAddToCart    = AuthenticatedAction(
                    context = context,
                    navController = navController
                ) {
                    // Add to cart logic here - authenticated user only
                    // TODO: Implement add to cart functionality
                }
            )
        }
        composable(
            route = "OrderDetailScreen/{orderDate}",
            arguments = listOf(androidx.navigation.navArgument("orderDate") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val orderDate = backStackEntry.arguments?.getString("orderDate") ?: ""
            // Find matching order or fallback to sample
            val order = com.example.nhathuoc.ui.component.defaultRecentOrders
                .find { it.date == orderDate }
                ?.let { ro ->
                    sampleOrderDetail.copy(
                        date       = ro.date,
                        subtotal   = ro.totalPrice,
                        total      = ro.totalPrice,
                        rewardPoints = ro.totalPrice.replace(".000đ","").replace(".","").toIntOrNull()?.div(1000) ?: 0
                    )
                } ?: sampleOrderDetail

            OrderDetailScreen(
                order     = order,
                onBack    = { navController.popBackStack() },
                onSupport = { ctx ->
                    pendingProductContext = ctx
                    navController.navigate("ChatScreen")
                },
                onReorder = { navController.popBackStack() }
            )
        }
        composable(
            route = "CategoryProductScreen/{categoryName}",
            arguments = listOf(androidx.navigation.navArgument("categoryName") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val rawName = backStackEntry.arguments?.getString("categoryName") ?: ""
            val categoryName = rawName.replace("_", " ")
            CategoryProductScreen(
                categoryName  = categoryName,
                navController = navController,
                onBack        = { navController.popBackStack() }
            )
        }
    }
}