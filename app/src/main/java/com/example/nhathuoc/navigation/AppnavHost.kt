package com.example.nhathuoc.navigation

import android.net.Uri
import android.widget.Toast
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
import com.example.nhathuoc.ui.screen.miniscreen.ChatScreen
import com.example.nhathuoc.ui.screen.miniscreen.CheckoutFlowScreen
import com.example.nhathuoc.ui.screen.miniscreen.CreateAddressScreen
import com.example.nhathuoc.ui.screen.miniscreen.FindPharmacyScreen
import com.example.nhathuoc.ui.screen.miniscreen.LoginScreen
import com.example.nhathuoc.ui.screen.miniscreen.MedicalSuppliesQuoteScreen
import com.example.nhathuoc.ui.screen.miniscreen.NotificationScreen
import com.example.nhathuoc.ui.screen.miniscreen.OrderConfirmationScreen
import com.example.nhathuoc.ui.screen.miniscreen.OrderDetailScreen
import com.example.nhathuoc.ui.screen.miniscreen.ProductDetail
import com.example.nhathuoc.ui.screen.miniscreen.ProductDetailScreen
import com.example.nhathuoc.ui.screen.miniscreen.ProductListScreen
import com.example.nhathuoc.ui.screen.miniscreen.RegisterScreen
// Note: AddressSelectionScreen not registered in NavHost; AddressBookScreen is used instead
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

    NavHost(
        navController = navController,
        startDestination = "MainScreen"
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
                onUseVoucher = { navController.navigate("CheckoutScreen") }
            )
        }
        composable("HomeScreen") {
            HomeScreen(navController = navController)
        }
        composable("ProductListScreen") {
            ProductListScreen(navController = navController)
        }
        composable(
            route = "ChatScreen?productId={productId}",
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ChatScreen(
                onBack = { navController.popBackStack() },
                onProductClick = { id ->
                    navController.navigate("ProductDetailScreen/${Uri.encode(id)}")
                },
                productId = productId
            )
        }
        composable("MedicalSuppliesQuoteScreen") {
            MedicalSuppliesQuoteScreen(onBack = { navController.popBackStack() })
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
        composable("FindPharmacyScreen") {
            FindPharmacyScreen(onBack = { navController.popBackStack() })
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
                        "CHAT" -> navController.navigate("ChatScreen")
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
            route = "ProductDetailScreen/{productId}?openReview={openReview}",
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType },
                navArgument("openReview") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val openReview = backStackEntry.arguments?.getBoolean("openReview") ?: false
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
                    riskClassification = dto.riskClassification,
                    requiresCertification = dto.requiresCertification || dto.isPrescription,
                    requiresConsultation = dto.requiresConsultation
                )

                ProductDetailScreen(
                    product = detail,
                    onBack = { navController.popBackStack() },
                    onChat = {
                        navController.navigate("ChatScreen")
                    },
                    onFindPharmacy = { navController.navigate("FindPharmacyScreen") },
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
                            requiresCertification = dto.requiresCertification || dto.isPrescription,
                            requiresConsultation = dto.requiresConsultation,
                            certificates = certificates.map { cert ->
                                com.example.nhathuoc.ui.screen.miniscreen.ProductCertificate(
                                    id = cert.id,
                                    type = "REGISTRATION",
                                    name = cert.name,
                                    fileUrl = cert.documentUrl.orEmpty(),
                                    fileType = cert.fileType,
                                    resourceType = cert.resourceType ?: cert.cloudinaryResourceType,
                                    issuedBy = cert.issuer,
                                    issuedAt = cert.issueDate,
                                    expiresAt = cert.expiryDate
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
                                navController.navigate("ChatScreen?productId=${Uri.encode(dto.id)}")
                            },
                            onFindPharmacy = { navController.navigate("FindPharmacyScreen") },
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
                            onSubmitReview = { rating, title, comment, attachments ->
                                detailViewModel.submitReview(dto.id, rating, title, comment, attachments)
                            },
                            onReportReview = { reviewId ->
                                detailViewModel.reportReview(reviewId)
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
                onOpenProduct = { productId, openReview ->
                    navController.navigate("ProductDetailScreen/${Uri.encode(productId)}?openReview=$openReview")
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
    }
}
