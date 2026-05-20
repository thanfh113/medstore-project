package com.example.nhathuoc.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nhathuoc.NotificationNavBus
import com.example.nhathuoc.data.local.SessionManager
import com.example.nhathuoc.data.local.findMockProduct
import com.example.nhathuoc.data.local.mockProducts
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.ui.screen.CartScreen
import com.example.nhathuoc.ui.screen.ComplaintDetailScreen
import com.example.nhathuoc.ui.screen.HomeScreen
import com.example.nhathuoc.ui.screen.MainScreen
import com.example.nhathuoc.ui.screen.MyComplaintsScreen
import com.example.nhathuoc.ui.screen.MyOrdersScreen
import com.example.nhathuoc.ui.screen.RewardScreen
import com.example.nhathuoc.ui.screen.miniscreen.AddressBookScreen
import com.example.nhathuoc.ui.screen.miniscreen.CategoryProductScreen
import com.example.nhathuoc.ui.screen.miniscreen.AiChatScreen
import com.example.nhathuoc.ui.screen.miniscreen.ChatHistoryScreen
import com.example.nhathuoc.ui.screen.miniscreen.ChatScreen
import com.example.nhathuoc.ui.screen.miniscreen.CheckoutFlowScreen
import com.example.nhathuoc.ui.screen.miniscreen.CreateAddressScreen
import com.example.nhathuoc.ui.screen.miniscreen.LoginScreen
import com.example.nhathuoc.ui.screen.miniscreen.NotificationScreen
import com.example.nhathuoc.ui.screen.miniscreen.OrderConfirmationScreen
import com.example.nhathuoc.ui.screen.miniscreen.OrderDetailScreen
import com.example.nhathuoc.ui.screen.miniscreen.ProductDetail
import com.example.nhathuoc.ui.screen.miniscreen.ProductDetailScreen
import com.example.nhathuoc.ui.screen.miniscreen.ProductListScreen
import com.example.nhathuoc.ui.screen.miniscreen.ChangePasswordScreen
import com.example.nhathuoc.ui.screen.miniscreen.ProfileScreen
import com.example.nhathuoc.ui.screen.miniscreen.RegisterScreen
// Note: AddressSelectionScreen not registered in NavHost; AddressBookScreen is used instead
import com.example.nhathuoc.viewmodel.AddressViewModel
import com.example.nhathuoc.viewmodel.CartViewModel
import com.example.nhathuoc.viewmodel.CheckoutViewModel
import com.example.nhathuoc.viewmodel.ProductDetailViewModel

@Composable
fun AppnavHost(navController: NavHostController) {
    val context = LocalContext.current

    // Global authentication state monitoring
    val sessionManager = remember { SessionManager(context) }
    val isLoggedIn = sessionManager.isLoggedIn.collectAsState(initial = false)

    LaunchedEffect(isLoggedIn.value) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (!isLoggedIn.value && currentRoute != null &&
            currentRoute != "LoginScreen" && currentRoute != "RegisterScreen") {
            navController.navigate("LoginScreen") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Handle notification taps that launched / resumed the app
    LaunchedEffect(Unit) {
        NotificationNavBus.events.collect { (type, refId) ->
            when (type.uppercase()) {
                "ORDER", "ORDER_STATUS" -> {
                    if (refId.isNotBlank()) navController.navigate("OrderDetailScreen/$refId")
                }
                "CHAT" -> {
                    if (refId.isNotBlank()) navController.navigate("ChatScreen?sessionId=${Uri.encode(refId)}")
                    else navController.navigate("ChatHistoryScreen")
                }
                "REWARD" -> navController.navigate("RewardScreen")
                "COMPLAINT", "REFUND" -> {
                    if (refId.isNotBlank()) navController.navigate("ComplaintDetailScreen/$refId")
                }
                else -> navController.navigate("NotificationScreen")
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "MainScreen",
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(250))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 4 },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(200))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 4 },
                animationSpec = tween(350, easing = LinearOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(250))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(200))
        }
    ) {
        composable("LoginScreen") {
            LoginScreen(navController = navController)
        }
        composable("RegisterScreen") {
            RegisterScreen(navController = navController)
        }
        composable("MainScreen") {
            MainScreen(navController)
        }
        composable("RewardScreen") {
            RewardScreen(
                onShopNow = { navController.navigate("HomeScreen") },
                onUseVoucher = { code ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("applyVoucherCode", code)
                    navController.navigate("CheckoutScreen")
                }
            )
        }
        composable("HomeScreen") {
            HomeScreen(navController = navController)
        }
        composable("ProductListScreen") {
            ProductListScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "AiChatScreen?productId={productId}&conversationId={conversationId}",
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("conversationId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            val conversationId = backStackEntry.arguments?.getString("conversationId")
            AiChatScreen(
                onBack = { navController.popBackStack() },
                onOpenHumanChat = { sessionId ->
                    navController.navigate("ChatScreen?sessionId=${Uri.encode(sessionId)}")
                },
                onProductClick = { id ->
                    navController.navigate("ProductDetailScreen/${Uri.encode(id)}")
                },
                productId = productId,
                conversationId = conversationId
            )
        }

        composable("ChatHistoryScreen") {
            ChatHistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenSession = { sessionId ->
                    navController.navigate("ChatScreen?sessionId=${Uri.encode(sessionId)}")
                },
                onOpenAiConversation = { conversationId ->
                    navController.navigate("AiChatScreen?conversationId=${Uri.encode(conversationId)}")
                },
                onNewChat = { navController.navigate("AiChatScreen") }
            )
        }
        composable(
            route = "ChatScreen?productId={productId}&sessionId={sessionId}",
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("sessionId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            val sessionId = backStackEntry.arguments?.getString("sessionId")
            ChatScreen(
                onBack = { navController.popBackStack() },
                onProductClick = { id ->
                    navController.navigate("ProductDetailScreen/${Uri.encode(id)}")
                },
                productId = productId,
                sessionId = sessionId
            )
        }
        composable("MyOrdersScreen") {
            MyOrdersScreen(
                onBack = { navController.popBackStack() },
                navController = navController
            )
        }
        composable("MyComplaintsScreen") {
            MyComplaintsScreen(
                onBack = { navController.popBackStack() },
                navController = navController
            )
        }
        composable(
            route = "ComplaintDetailScreen/{complaintId}",
            arguments = listOf(navArgument("complaintId") { type = NavType.StringType })
        ) { backStackEntry ->
            val complaintId = backStackEntry.arguments?.getString("complaintId") ?: ""
            ComplaintDetailScreen(
                complaintId = complaintId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("NotificationScreen") {
            NotificationScreen(
                onBack = { navController.popBackStack() },
                onNotificationClick = { notification ->
                    val refId = notification.refId
                    when (notification.type.uppercase()) {
                        "ORDER", "ORDER_STATUS" -> {
                            if (!refId.isNullOrBlank()) navController.navigate("OrderDetailScreen/$refId")
                        }
                        "COMPLAINT", "REFUND" -> {
                            if (!refId.isNullOrBlank()) navController.navigate("ComplaintDetailScreen/$refId")
                        }
                        "CHAT" -> {
                            if (!refId.isNullOrBlank()) navController.navigate("ChatScreen?sessionId=${Uri.encode(refId)}")
                            else navController.navigate("ChatHistoryScreen")
                        }
                        "REWARD" -> navController.navigate("RewardScreen")
                        "REVIEW" -> {
                            if (!refId.isNullOrBlank()) {
                                navController.navigate("ProductDetailScreen/${Uri.encode(refId)}?openReview=true")
                            }
                        }
                        else -> Unit
                    }
                }
            )
        }

        // â”€â”€ Product Detail â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        composable(
            route = "ProductDetailScreen/{productId}?openReview={openReview}&reviewOrderId={reviewOrderId}&reviewOrderItemId={reviewOrderItemId}",
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType },
                navArgument("openReview") { type = NavType.BoolType; defaultValue = false },
                navArgument("reviewOrderId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("reviewOrderItemId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val openReview = backStackEntry.arguments?.getBoolean("openReview") ?: false
            val reviewOrderId = backStackEntry.arguments?.getString("reviewOrderId")
            val reviewOrderItemId = backStackEntry.arguments?.getString("reviewOrderItemId")
            val cartViewModel: CartViewModel = hiltViewModel()

            if (productId.startsWith("mock-")) {
                // â”€â”€ Mock product: build detail from local data â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                val mockProduct = findMockProduct(productId) ?: mockProducts.first()
                val dto = mockProduct.dto

                fun Long.fmt() = String.format("%,d", this).replace(',', '.')

                val detail = ProductDetail(
                    id = dto.id,
                    name = dto.name,
                    brand = dto.brand,
                    origin = dto.origin,
                    price = "${dto.price.toLong().fmt()}đ",
                    originalPrice = "${(dto.originalPrice ?: dto.price).toLong().fmt()}đ",
                    discountPercent = dto.discountPct,
                    unit = dto.unit,
                    rewardPoints = dto.rewardPoints,
                    icon = mockProduct.icon,
                    iconTint = mockProduct.iconTint,
                    iconBg = mockProduct.iconBg,
                    imageResIds = mockProduct.imageResIds,
                    imageResId = mockProduct.imageResId,
                    sku = dto.sku,
                    stockQuantity = dto.stock,
                    productType = dto.productType,
                    registrationNumber = dto.registrationNumber,
                    riskClassification = dto.riskClassification
                )

                ProductDetailScreen(
                    product = detail,
                    onBack = { navController.popBackStack() },
                    onChat = {
                        val risk = detail.riskClassification.trim().uppercase()
                        if (risk == "C" || risk == "D") {
                            navController.navigate("ChatScreen?productId=${Uri.encode(dto.id)}")
                        } else {
                            navController.navigate("AiChatScreen?productId=${Uri.encode(dto.id)}")
                        }
                    },
                    onAddToCart = { _ ->
                        Toast.makeText(
                            context,
                            "Sản phẩm demo - vui lòng chọn sản phẩm thật.",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onBuyNow = { _ ->
                        Toast.makeText(
                            context,
                            "Sản phẩm demo - vui lòng chọn sản phẩm thật.",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    openReviewOnStart = openReview
                )
            } else {
                // â”€â”€ Real product: load from API via ProductDetailViewModel â”€â”€
                val detailViewModel: ProductDetailViewModel = hiltViewModel()
                val productState by detailViewModel.productState.collectAsState()
                val product by detailViewModel.product.collectAsState()
                val images by detailViewModel.images.collectAsState()
                val certificates by detailViewModel.certificates.collectAsState()
                val reviewSummary by detailViewModel.reviewSummary.collectAsState()
                val reviews by detailViewModel.reviews.collectAsState()
                val reviewSubmitState by detailViewModel.reviewSubmitState.collectAsState()
                val reviewFeedbackMessage by detailViewModel.reviewFeedbackMessage.collectAsState()
                val currentUserId by detailViewModel.currentUserId.collectAsState()

                LaunchedEffect(productId) {
                    detailViewModel.loadProduct(productId)
                }

                when (val s = productState) {
                    is UiState.Loading, is UiState.Idle -> {
                        // Loading skeleton
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF5F7FA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFF2E7D32))
                                Spacer(Modifier.height(16.dp))
                                Text("Đang tải sản phẩm...", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }
                    is UiState.Error -> {
                        // Error state with retry
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF5F7FA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.MedicalServices,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Không thể tải sản phẩm",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    s.message,
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                                Spacer(Modifier.height(20.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedButton(onClick = { navController.popBackStack() }) {
                                        Icon(Icons.Outlined.ArrowBackIosNew, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Quay lại")
                                    }
                                    Button(
                                        onClick = { detailViewModel.retry(productId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                    ) {
                                        Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Thử lại")
                                    }
                                }
                            }
                        }
                    }
                    is UiState.Success -> {
                        val dto = product ?: return@composable
                        fun Long.fmt() = String.format("%,d", this).replace(',', '.')
                        val remoteImageUrls = images
                            .sortedBy { it.sortOrder }
                            .mapNotNull { it.imageUrl.takeIf(String::isNotBlank) }
                            .ifEmpty { listOfNotNull(dto.imageUrl?.takeIf { it.isNotBlank() }) }

                        val detail = ProductDetail(
                            id = dto.id,
                            name = dto.name,
                            brand = dto.brand,
                            origin = dto.origin,
                            price = "${dto.price.toLong().fmt()}đ",
                            originalPrice = "${(dto.originalPrice ?: dto.price).toLong().fmt()}đ",
                            discountPercent = dto.discountPct,
                            unit = dto.unit,
                            rewardPoints = dto.rewardPoints,
                            icon = Icons.Outlined.MedicalServices,
                            iconTint = Color(0xFF2E7D32),
                            iconBg = Color(0xFFE8F5E9),
                            imageResIds = emptyList(),
                            imageResId = null,
                            imageUrl = remoteImageUrls.firstOrNull(),
                            imageUrls = remoteImageUrls,
                            sku = dto.sku,
                            stockQuantity = dto.stock,
                            productType = dto.productType,
                            registrationNumber = dto.registrationNumber,
                            riskClassification = dto.riskClassification,
                            certificates = certificates.map { cert ->
                                com.example.nhathuoc.ui.screen.miniscreen.ProductCertificate(
                                    id = cert.id,
                                    type = "REGISTRATION",
                                    name = cert.name,
                                    fileUrl = cert.documentUrl.orEmpty(),
                                    fileType = cert.fileType,
                                    resourceType = cert.resourceType ?: cert.cloudinaryResourceType,
                                    issuedBy = cert.issuer
                                )
                            }
                        )

                        val addToCartAction: (Int) -> Unit = { quantity ->
                            if (isLoggedIn.value) {
                                cartViewModel.addToCart(
                                    productId = dto.id,
                                    quantity = quantity,
                                    unit = dto.unit
                                )
                            } else {
                                navController.navigate("LoginScreen")
                            }
                        }

                        ProductDetailScreen(
                            product = detail,
                            onBack = { navController.popBackStack() },
                            onChat = {
                                val risk = dto.riskClassification.trim().uppercase()
                                if (risk == "C" || risk == "D") {
                                    navController.navigate("ChatScreen?productId=${Uri.encode(dto.id)}")
                                } else {
                                    navController.navigate("AiChatScreen?productId=${Uri.encode(dto.id)}")
                                }
                            },
                            onAddToCart = { quantity ->
                                addToCartAction(quantity)
                                Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                            },
                            onBuyNow = { quantity ->
                                if (isLoggedIn.value) {
                                    navController.currentBackStackEntry?.savedStateHandle?.set("directProductId", dto.id)
                                    navController.currentBackStackEntry?.savedStateHandle?.set("directQuantity", quantity)
                                    navController.currentBackStackEntry?.savedStateHandle?.set("directUnit", dto.unit)
                                    navController.navigate("CheckoutScreen")
                                } else {
                                    navController.navigate("LoginScreen")
                                }
                            },
                            reviewSummary = reviewSummary,
                            reviews = reviews,
                            reviewSubmitting = reviewSubmitState is UiState.Loading,
                            reviewSubmitMessage = reviewFeedbackMessage ?: when (val submitState = reviewSubmitState) {
                                is UiState.Error -> submitState.message
                                else -> null
                            },
                            openReviewOnStart = openReview,
                            reviewSubmitted = reviewSubmitState is UiState.Success,
                            currentUserId = currentUserId,
                            onSubmitReview = { rating, title, comment, attachments ->
                                detailViewModel.submitReview(dto.id, rating, title, comment, attachments, reviewOrderId, reviewOrderItemId)
                            },
                            onReportReview = { reviewId, reason, note ->
                                detailViewModel.reportReview(reviewId, reason, note)
                            }
                        )
                    }
                }
            }
        }

        composable(
            route = "OrderDetailScreen/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailScreen(
                orderId = orderId,
                onBack = { navController.popBackStack() },
                onOpenProduct = { productId, openReview, orderId, orderItemId ->
                    val base = "ProductDetailScreen/${Uri.encode(productId)}?openReview=$openReview"
                    val withOrder = if (!orderId.isNullOrBlank()) "$base&reviewOrderId=${Uri.encode(orderId)}" else base
                    val full = if (!orderItemId.isNullOrBlank()) "$withOrder&reviewOrderItemId=${Uri.encode(orderItemId)}" else withOrder
                    navController.navigate(full)
                },
                onResumePayment = { pendingOrderId, paymentMethod ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("resumeOrderId", pendingOrderId)
                    navController.currentBackStackEntry?.savedStateHandle?.set("resumePaymentMethod", paymentMethod)
                    navController.navigate("CheckoutScreen")
                }
            )
        }
        composable(
            route = "CategoryProductScreen/{categoryId}/{categoryTitle}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("categoryTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = Uri.decode(backStackEntry.arguments?.getString("categoryId") ?: "")
            val categoryTitle = Uri.decode(backStackEntry.arguments?.getString("categoryTitle") ?: categoryId)
            CategoryProductScreen(
                categoryId = categoryId,
                categoryTitle = categoryTitle,
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "CategoryProductScreen/{categoryName}",
            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
        ) { backStackEntry ->
            val rawName = backStackEntry.arguments?.getString("categoryName") ?: ""
            val categoryName = Uri.decode(rawName).replace("_", " ")
            CategoryProductScreen(
                categoryId = categoryName,
                categoryTitle = categoryName,
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }
        composable("CartScreen") {
            CartScreen(navController = navController)
        }
        composable("CheckoutScreen") {
            CheckoutFlowScreen(navController = navController)
        }
        composable("AddressSelectionScreen") { backStackEntry ->
            val checkoutEntry = remember(backStackEntry) {
                navController.getBackStackEntry("CheckoutScreen")
            }
            val checkoutViewModel: CheckoutViewModel = hiltViewModel(checkoutEntry)
            val state by checkoutViewModel.checkoutState.collectAsState()

            AddressBookScreen(
                addresses = state.addresses,
                selectedAddressId = state.selectedAddressId,
                onAddressSelected = { id ->
                    checkoutViewModel.selectAddress(id)
                    navController.popBackStack()
                },
                onAddNewAddress = { navController.navigate("AddAddressScreen") },
                onEditAddress = { id -> navController.navigate("EditAddressScreen/${Uri.encode(id)}") },
                onDeleteAddress = { id -> checkoutViewModel.deleteAddress(id) },
                onBack = { navController.popBackStack() }
            )
        }
        composable("AddAddressScreen") { backStackEntry ->
            val checkoutEntry = remember(backStackEntry) {
                navController.getBackStackEntry("CheckoutScreen")
            }
            val checkoutViewModel: CheckoutViewModel = hiltViewModel(checkoutEntry)

            CreateAddressScreen(
                onSave = { request ->
                    checkoutViewModel.addAddress(request)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "EditAddressScreen/{addressId}",
            arguments = listOf(navArgument("addressId") { type = NavType.StringType })
        ) { backStackEntry ->
            val checkoutEntry = remember(backStackEntry) {
                navController.getBackStackEntry("CheckoutScreen")
            }
            val checkoutViewModel: CheckoutViewModel = hiltViewModel(checkoutEntry)
            val state by checkoutViewModel.checkoutState.collectAsState()
            val addressId = backStackEntry.arguments?.getString("addressId").orEmpty()
            val editingAddress = state.addresses.firstOrNull { it.id == addressId }

            if (editingAddress == null) {
                LaunchedEffect(addressId) {
                    Toast.makeText(context, "Không tìm thấy địa chỉ cần sửa", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            } else {
                CreateAddressScreen(
                    initialAddress = editingAddress,
                    onSave = { request ->
                        checkoutViewModel.updateAddress(addressId, request)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(
            route = "OrderConfirmationScreen/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderConfirmationScreen(orderId = orderId, navController = navController)
        }
        composable("ProfileScreen") {
            ProfileScreen(onBack = { navController.popBackStack() })
        }
        composable("ChangePasswordScreen") {
            ChangePasswordScreen(onBack = { navController.popBackStack() })
        }

        // ── Profile Address Management ──────────────────────────────────
        composable("ProfileAddressBookScreen") {
            val addressViewModel: AddressViewModel = hiltViewModel()
            val addresses by addressViewModel.addresses.collectAsState()
            AddressBookScreen(
                addresses = addresses,
                selectedAddressId = null,
                onAddressSelected = {},
                onAddNewAddress = { navController.navigate("ProfileAddAddressScreen") },
                onEditAddress = { id -> navController.navigate("ProfileEditAddressScreen/${Uri.encode(id)}") },
                onDeleteAddress = { id -> addressViewModel.deleteAddress(id) },
                onBack = { navController.popBackStack() }
            )
        }
        composable("ProfileAddAddressScreen") { backStackEntry ->
            val profileEntry = remember(backStackEntry) {
                navController.getBackStackEntry("ProfileAddressBookScreen")
            }
            val addressViewModel: AddressViewModel = hiltViewModel(profileEntry)
            CreateAddressScreen(
                onSave = { request ->
                    addressViewModel.addAddress(request)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "ProfileEditAddressScreen/{addressId}",
            arguments = listOf(navArgument("addressId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileEntry = remember(backStackEntry) {
                navController.getBackStackEntry("ProfileAddressBookScreen")
            }
            val addressViewModel: AddressViewModel = hiltViewModel(profileEntry)
            val addresses by addressViewModel.addresses.collectAsState()
            val addressId = backStackEntry.arguments?.getString("addressId").orEmpty()
            val editingAddress = addresses.firstOrNull { it.id == addressId }

            if (editingAddress == null) {
                LaunchedEffect(addressId) {
                    Toast.makeText(context, "Không tìm thấy địa chỉ", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            } else {
                CreateAddressScreen(
                    initialAddress = editingAddress,
                    onSave = { request ->
                        addressViewModel.updateAddress(addressId, request)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
