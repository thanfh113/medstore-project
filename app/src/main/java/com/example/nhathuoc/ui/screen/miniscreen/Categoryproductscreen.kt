package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.navigation.NavController
import com.example.nhathuoc.ui.theme.GreenLight
private val GreenTop = Color(0xFF2E7D32)

private val RedColor  = Color(0xFFE53935)

// ── Data ──────────────────────────────────────────────────────────────
data class SubCategory(val name: String, val icon: ImageVector)

data class CategoryProduct(
    val id: Int,
    val name: String,
    val price: String,
    val originalPrice: String,
    val discountPercent: Int?,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val unit: String = "Hộp"
)

data class CategoryScreenData(
    val categoryName: String,
    val parentTabs: List<String>,         // top horizontal tabs
    val subCategories: List<SubCategory>, // sub-category chips below back arrow
    val products: List<CategoryProduct>
)

// ── Sample data per category ──────────────────────────────────────────
private val parentTabs = listOf(
    "Thực phẩm chức năng", "Dược mỹ phẩm", "Thuốc", "Chăm sóc cá nhân", "Thiết bị y tế"
)

val categoryScreenDataMap: Map<String, CategoryScreenData> = mapOf(
    "Thần kinh não" to CategoryScreenData(
        categoryName = "Thần kinh não",
        parentTabs   = parentTabs,
        subCategories = listOf(
            SubCategory("Bổ não - cải thiện trí nhớ", Icons.Outlined.Psychology),
            SubCategory("Hỗ trợ giấc ngủ",            Icons.Outlined.NightlightRound),
            SubCategory("Hoạt huyết",                 Icons.Outlined.Favorite),
        ),
        products = listOf(
            CategoryProduct(301, "Viên uống hỗ trợ giảm nguy cơ tắc mạch, tăng tuần hoàn não Hoạt Huyết Thông Mạch TW3 Gold (Hộp 3 vỉ x 15 viên)", "96.000đ", "120.000đ", 20, Icons.Outlined.Psychology, Color(0xFF1565C0), Color(0xFFE3F2FD)),
            CategoryProduct(302, "Viên uống hỗ trợ tốt cho não và tim mạch Omexxel Ginkgo 120 Premium (60 viên)", "788.000đ", "900.000đ", 12, Icons.Outlined.Psychology, Color(0xFF6A1B9A), Color(0xFFF3E5F5)),
            CategoryProduct(303, "Viên uống bổ não, tốt cho mắt Ultra Brain Lab Well (60 viên)", "191.200đ", "239.000đ", 20, Icons.Outlined.Science, Color(0xFF00838F), Color(0xFFE0F7FA)),
            CategoryProduct(304, "Siro bổ não DHA + Omega 3 cho trẻ em Brauer Kids (200ml)", "245.000đ", "295.000đ", 17, Icons.Outlined.ChildCare, Color(0xFF2E7D32), Color(0xFFE8F5E9)),
            CategoryProduct(305, "Viên nang bổ não Ginkgo Biloba 120mg Traphaco (60 viên)", "135.000đ", "160.000đ", 16, Icons.Outlined.MedicalServices, Color(0xFFC2185B), Color(0xFFFCE4EC)),
            CategoryProduct(306, "Dầu cá Omega-3 hỗ trợ não bộ và tim mạch Nature Made (100 viên)", "320.000đ", "380.000đ", 16, Icons.Outlined.LocalFlorist, Color(0xFF0277BD), Color(0xFFE1F5FE)),
        )
    ),
    "Vitamin & Khoáng chất" to CategoryScreenData(
        categoryName  = "Vitamin & Khoáng chất",
        parentTabs    = parentTabs,
        subCategories = listOf(
            SubCategory("Vitamin tổng hợp", Icons.Outlined.MedicalServices),
            SubCategory("Vitamin C",        Icons.Outlined.Spa),
            SubCategory("Canxi & D3",       Icons.Outlined.FitnessCenter),
            SubCategory("Kẽm & Sắt",       Icons.Outlined.Science),
        ),
        products = listOf(
            CategoryProduct(311, "Vitamin C 1000mg tăng đề kháng DHC (60 viên)", "89.000đ", "120.000đ", 26, Icons.Outlined.MedicalServices, Color(0xFF1565C0), Color(0xFFE3F2FD)),
            CategoryProduct(312, "Canxi hữu cơ D3 K2 Ostelin Calcium & Vitamin D3 (130 viên)", "450.000đ", "520.000đ", 13, Icons.Outlined.FitnessCenter, Color(0xFF2E7D32), Color(0xFFE8F5E9)),
            CategoryProduct(313, "Kẽm hữu cơ tăng miễn dịch Blackmores Bio Zinc (90 viên)", "145.000đ", "180.000đ", 19, Icons.Outlined.Science, Color(0xFF6A1B9A), Color(0xFFF3E5F5)),
            CategoryProduct(314, "Sắt hữu cơ Ferrovit bổ sung sắt và acid folic (30 viên)", "75.000đ", "95.000đ", 21, Icons.Outlined.Spa, Color(0xFFC2185B), Color(0xFFFCE4EC)),
        )
    ),
    "Tim mạch - Huyết áp" to CategoryScreenData(
        categoryName  = "Tim mạch - Huyết áp",
        parentTabs    = parentTabs,
        subCategories = listOf(
            SubCategory("Hạ huyết áp", Icons.Outlined.Favorite),
            SubCategory("Mỡ máu",      Icons.Outlined.Opacity),
        ),
        products = listOf(
            CategoryProduct(321, "Viên uống hỗ trợ ổn định huyết áp Cardiphar (30 viên)", "180.000đ", "220.000đ", 18, Icons.Outlined.Favorite, Color(0xFFE53935), Color(0xFFFFEBEE)),
            CategoryProduct(322, "Dầu cá Omega-3 hỗ trợ mỡ máu Nature Made (200 viên)", "520.000đ", "620.000đ", 16, Icons.Outlined.LocalFlorist, Color(0xFF0277BD), Color(0xFFE1F5FE)),
        )
    ),
    "Miễn dịch - Đề kháng" to CategoryScreenData(
        categoryName  = "Miễn dịch - Đề kháng",
        parentTabs    = parentTabs,
        subCategories = listOf(
            SubCategory("Tăng đề kháng",  Icons.Outlined.Shield),
            SubCategory("Chống oxy hóa",  Icons.Outlined.VerifiedUser),
        ),
        products = listOf(
            CategoryProduct(331, "Viên uống tăng đề kháng Blackmores Immune Defence (60 viên)", "320.000đ", "390.000đ", 18, Icons.Outlined.Shield, Color(0xFF6A1B9A), Color(0xFFF3E5F5)),
            CategoryProduct(332, "Beta Glucan tăng miễn dịch Immuno Glucan (30 viên)", "245.000đ", "290.000đ", 16, Icons.Outlined.VerifiedUser, Color(0xFF2E7D32), Color(0xFFE8F5E9)),
        )
    ),
    "Tiêu hóa" to CategoryScreenData(
        categoryName  = "Tiêu hóa",
        parentTabs    = parentTabs,
        subCategories = listOf(
            SubCategory("Men vi sinh",   Icons.Outlined.Science),
            SubCategory("Hỗ trợ gan",   Icons.Outlined.LocalFlorist),
            SubCategory("Táo bón",      Icons.Outlined.Restaurant),
        ),
        products = listOf(
            CategoryProduct(341, "Men vi sinh Imiale A+ hỗ trợ tiêu hóa (30 ống)", "165.000đ", "200.000đ", 18, Icons.Outlined.Science, Color(0xFF00838F), Color(0xFFE0F7FA)),
            CategoryProduct(342, "Thảo dược bổ gan Boganic (60 viên)", "145.000đ", "200.000đ", 28, Icons.Outlined.LocalFlorist, Color(0xFF2E7D32), Color(0xFFE8F5E9)),
            CategoryProduct(343, "Cốm hỗ trợ tiêu hóa Enterogermina (20 gói)", "185.000đ", "220.000đ", 16, Icons.Outlined.Restaurant, Color(0xFFE65100), Color(0xFFFFF3E0)),
        )
    ),
    "Sinh lý - Nội tiết tố" to CategoryScreenData(
        categoryName  = "Sinh lý - Nội tiết tố",
        parentTabs    = parentTabs,
        subCategories = listOf(
            SubCategory("Nam giới",  Icons.Outlined.Man),
            SubCategory("Nữ giới",  Icons.Outlined.Woman),
        ),
        products = listOf(
            CategoryProduct(351, "Viên uống hỗ trợ sức khỏe nam giới Saw Palmetto (60 viên)", "280.000đ", "340.000đ", 18, Icons.Outlined.Man, Color(0xFF0277BD), Color(0xFFE1F5FE)),
            CategoryProduct(352, "Mầm đậu nành hỗ trợ nội tiết nữ Blackmores (60 viên)", "310.000đ", "380.000đ", 18, Icons.Outlined.Woman, Color(0xFFC2185B), Color(0xFFFCE4EC)),
        )
    ),
    "Chăm sóc mắt" to CategoryScreenData(
        categoryName  = "Chăm sóc mắt",
        parentTabs    = parentTabs,
        subCategories = listOf(
            SubCategory("Nhỏ mắt",     Icons.Outlined.RemoveRedEye),
            SubCategory("Bổ sung mắt", Icons.Outlined.Visibility),
        ),
        products = listOf(
            CategoryProduct(371, "Nhỏ mắt Santen FX Neo (15ml)", "75.000đ", "95.000đ", 21, Icons.Outlined.RemoveRedEye, Color(0xFF0277BD), Color(0xFFE1F5FE)),
            CategoryProduct(372, "Viên uống bổ mắt Lutein & Zeaxanthin Blackmores (60 viên)", "290.000đ", "350.000đ", 17, Icons.Outlined.Visibility, Color(0xFF2E7D32), Color(0xFFE8F5E9)),
        )
    ),
)

// Fallback for unmapped categories
private fun fallbackData(name: String) = CategoryScreenData(
    categoryName  = name,
    parentTabs    = parentTabs,
    subCategories = emptyList(),
    products      = emptyList()
)

// ── Screen ────────────────────────────────────────────────────────────
@Composable
fun CategoryProductScreen(
    categoryName: String,
    navController: NavController? = null,
    onBack: () -> Unit = {}
) {
    val data = categoryScreenDataMap[categoryName] ?: fallbackData(categoryName)

    var selectedParentTab by remember { mutableIntStateOf(
        data.parentTabs.indexOfFirst { it == "Thực phẩm chức năng" }.coerceAtLeast(0)
    ) }
    var selectedSubCat by remember { mutableStateOf<String?>(null) }
    var sortMode by remember { mutableStateOf("Bán chạy") }
    val sortOptions = listOf("Bán chạy", "Giá thấp", "Giá cao")

    val filteredProducts = remember(selectedSubCat, sortMode, data.products) {
        var list = data.products
        when (sortMode) {
            "Giá thấp" -> list = list.sortedBy { it.price.replace(".", "").replace("đ", "").toIntOrNull() ?: 0 }
            "Giá cao"  -> list = list.sortedByDescending { it.price.replace(".", "").replace("đ", "").toIntOrNull() ?: 0 }
        }
        list
    }

    Scaffold(
        containerColor = Color(0xFFF0F2F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── TopBar ─────────────────────────────────────────────
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
                            .statusBarsPadding()
                            .height(56.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(Icons.Filled.ArrowBackIosNew, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Text(
                            data.categoryName,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        Row(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {}) {
                                Icon(Icons.Outlined.Search, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            BadgedBox(badge = {
                                Badge(containerColor = Color(0xFFFF6D00)) {
                                    Text("1", color = Color.White, fontSize = 9.sp)
                                }
                            }) {
                                IconButton(onClick = {}) {
                                    Icon(Icons.Outlined.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                            }
                        }
                    }

                    // Parent tab row (scrollable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 4.dp),
                    ) {
                        data.parentTabs.forEachIndexed { idx, tab ->
                            val isSelected = idx == selectedParentTab
                            Column(
                                modifier = Modifier
                                    .clickable { selectedParentTab = idx }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    tab,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GreenTop else Color(0xFF777777),
                                    maxLines = 2,
                                    modifier = Modifier.widthIn(max = 80.dp)
                                )
                                if (isSelected) {
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(32.dp)
                                            .height(2.dp)
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(GreenTop)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                // ── Sub-category chips ──────────────────────────────
                if (data.subCategories.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.ArrowBackIosNew,
                                    null,
                                    tint = Color(0xFF333333),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    data.categoryName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A1A)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            // Sub-cat cards in scrollable row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                data.subCategories.forEach { sub ->
                                    val isSelected = selectedSubCat == sub.name
                                    Surface(
                                        onClick = {
                                            selectedSubCat = if (isSelected) null else sub.name
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) Color(0xFFE3F2FD) else Color.White,
                                        shadowElevation = 2.dp,
                                        modifier = Modifier.width(130.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFEEF2FF)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    sub.icon,
                                                    null,
                                                    tint = GreenTop,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Text(
                                                sub.name,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) GreenTop else Color(0xFF333333),
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Filter + Sort bar ───────────────────────────────
                item(span = { GridItemSpan(2) }) {
                    Column {
                        // Filter chips
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterButton(Icons.Outlined.FilterList, "Lọc") {}
                            FilterButton(null, "Đối tượng") {}
                            FilterButton(null, "Giá bán") {}
                        }
                        Spacer(Modifier.height(8.dp))
                        // Sort row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Sắp xếp theo:", fontSize = 13.sp, color = Color(0xFF555555))
                            sortOptions.forEach { opt ->
                                val isSel = sortMode == opt
                                Text(
                                    opt,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) GreenTop else Color(0xFF777777),
                                    modifier = Modifier.clickable { sortMode = opt }
                                )
                            }
                        }
                    }
                }

                // ── Notice ──────────────────────────────────────────
                item(span = { GridItemSpan(2) }) {
                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Outlined.Info, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text(
                                "Lưu ý: Thuốc kê đơn và một số sản phẩm sẽ cần được tư vấn từ dược sĩ",
                                fontSize = 12.sp,
                                color = Color(0xFF555555),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                // ── Section header ──────────────────────────────────
                item(span = { GridItemSpan(2) }) {
                    Text(
                        "Danh sách sản phẩm",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }

                // ── Product grid ────────────────────────────────────
                items(filteredProducts) { product ->
                    ProductGridCard(
                        product = product,
                        onClick = {
                            navController?.navigate("ProductDetailScreen/${product.id}")
                        }
                    )
                }

                item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

// ── Filter button ─────────────────────────────────────────────────────
@Composable
private fun FilterButton(
    icon: ImageVector?,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = Color.White,
        border = ButtonDefaults.outlinedButtonBorder,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(icon, null, tint = Color(0xFF333333), modifier = Modifier.size(16.dp))
            }
            Text(label, fontSize = 13.sp, color = Color(0xFF333333))
        }
    }
}

// ── Product card (2-col grid) ─────────────────────────────────────────
@Composable
private fun ProductGridCard(
    product: CategoryProduct,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Image area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        product.iconBg,
                        RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    product.icon,
                    null,
                    tint = product.iconTint,
                    modifier = Modifier.size(72.dp)
                )
                // Discount badge
                if (product.discountPercent != null && product.discountPercent > 0) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 14.dp, bottomEnd = 8.dp),
                        color = RedColor,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            "-${product.discountPercent}%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Info
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    product.name,
                    fontSize = 12.sp,
                    color = Color(0xFF1A1A1A),
                    lineHeight = 17.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.heightIn(min = 51.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${product.price} / ${product.unit}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenTop
                )
                Text(
                    product.originalPrice,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textDecoration = TextDecoration.LineThrough
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Chọn mua", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}