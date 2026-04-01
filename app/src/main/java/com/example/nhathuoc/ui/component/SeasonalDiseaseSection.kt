package com.example.nhathuoc.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.nhathuoc.ui.theme.GreenTop

data class DiseaseSample(
    val tabLabel: String,
    val description: String,
    val products: List<DiseaseProduct>
)

data class DiseaseProduct(
    val id: Int = 0,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val name: String,
    val price: String,
    val originalPrice: String?,
    val discountPercent: Int?,
    val tag: String?
)

val defaultDiseases = listOf(
    DiseaseSample(
        tabLabel = "Phẫu thuật",
        description = "Vật tư phẫu thuật chính hãng, đảm bảo tiêu chuẩn vô trùng tuyệt đối. Từ kim chỉ khâu đến dụng cụ vi phẫu — đáp ứng mọi nhu cầu phòng mổ hiện đại.",
        products = listOf(
            DiseaseProduct(id=201, Icons.Outlined.MedicalServices, Color(0xFF1565C0), Color(0xFFE3F2FD), "Kim chỉ khâu Vicryl 2-0 (hộp 12 sợi)", "320.000đ", "380.000đ", 16, null),
            DiseaseProduct(id=202, Icons.Outlined.CleanHands, Color(0xFF00838F), Color(0xFFE0F7FA), "Găng tay phẫu thuật vô trùng (hộp 50 đôi)", "185.000đ", "220.000đ", 16, null),
            DiseaseProduct(id=203, Icons.Outlined.HealthAndSafety, Color(0xFF2E7D32), Color(0xFFE8F5E9), "Dao mổ dùng một lần No.22 (hộp 100)", "145.000đ", null, null, "Bán chạy"),
        )
    ),
    DiseaseSample(
        tabLabel = "Chẩn đoán",
        description = "Thiết bị chẩn đoán chính xác, chuẩn WHO. Máy đo huyết áp, SpO2, đường huyết — hỗ trợ theo dõi sức khỏe tại bệnh viện và tại nhà.",
        products = listOf(
            DiseaseProduct(id=204, Icons.Outlined.MonitorHeart, Color(0xFFB71C1C), Color(0xFFFFEBEE), "Máy đo huyết áp điện tử Omron HEM-7156T", "1.250.000đ", "1.500.000đ", 17, null),
            DiseaseProduct(id=205, Icons.Outlined.Fingerprint, Color(0xFF00838F), Color(0xFFE0F7FA), "Máy đo SpO2 ngón tay CMS50D", "320.000đ", "400.000đ", 20, null),
        )
    ),
    DiseaseSample(
        tabLabel = "Băng bó",
        description = "Băng gạc y tế vô trùng tiêu chuẩn TCVN, phù hợp cho vết thương hở, phẫu thuật và chăm sóc tại nhà. Đa dạng kích cỡ và chất liệu.",
        products = listOf(
            DiseaseProduct(id=206, Icons.Outlined.HealthAndSafety, Color(0xFF0277BD), Color(0xFFE1F5FE), "Gạc vô trùng 10x10cm (hộp 100 gói)", "89.000đ", "120.000đ", 26, null),
            DiseaseProduct(id=207, Icons.Outlined.Shield, Color(0xFF6A1B9A), Color(0xFFF3E5F5), "Băng dính y tế micropore 2.5cm (cuộn)", "45.000đ", "55.000đ", 18, null),
        )
    ),
    DiseaseSample(
        tabLabel = "Bảo hộ",
        description = "Trang bị bảo hộ cá nhân (PPE) đạt chuẩn CE & ISO: khẩu trang N95, quần áo phòng dịch, kính bảo hộ — bảo vệ nhân viên y tế và bệnh nhân.",
        products = listOf(
            DiseaseProduct(id=208, Icons.Outlined.Masks, Color(0xFF00838F), Color(0xFFE0F7FA), "Khẩu trang N95 3M 1860 (hộp 20 cái)", "450.000đ", "520.000đ", 13, null),
        )
    ),
)


@Composable
fun SeasonalDiseaseSection(
    diseases: List<DiseaseSample> = defaultDiseases,
    onExplore: (DiseaseSample) -> Unit = {},
    onProductClick: (DiseaseProduct) -> Unit = {},
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val selected = diseases[selectedIndex]

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Vật tư theo chuyên khoa",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column {
                // Scrollable tab row
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    diseases.forEachIndexed { i, disease ->
                        val isSelected = selectedIndex == i
                        Surface(
                            onClick = { selectedIndex = i },
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) GreenTop else Color.Transparent
                        ) {
                            Text(
                                text = disease.tabLabel,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color.Gray,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                // Description card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF1565C0), Color(0xFF42A5F5))),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = selected.description,
                            fontSize = 13.sp,
                            color = Color.White,
                            lineHeight = 19.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { onExplore(selected) },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text("Khám phá ngay giải pháp", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                // Product row
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    selected.products.forEach { product ->
                        DiseaseProductCard(
                            product = product,
                            onClick = {
                                onProductClick(product)
                                navController?.navigate("ProductDetailScreen/${product.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiseaseProductCard(
    product: DiseaseProduct,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8F9FF),
        modifier = Modifier.width(150.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(product.iconBg)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(product.icon, null, tint = product.iconTint, modifier = Modifier.size(36.dp))
                }
                // Tag or discount badge
                if (product.tag != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE53935),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.CardGiftcard, null, tint = Color.White, modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(2.dp))
                            Text(product.tag, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (product.discountPercent != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE53935),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text("-${product.discountPercent}%", color = Color.White, fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(product.name, fontSize = 11.sp, color = Color(0xFF1A1A1A), lineHeight = 15.sp, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Text(product.price, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
            if (product.originalPrice != null) {
                Text(product.originalPrice, fontSize = 10.sp, color = Color.Gray,
                    style = androidx.compose.ui.text.TextStyle(textDecoration = TextDecoration.LineThrough))
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chọn mua", color = Color.White, fontSize = 11.sp)
            }
        }
    }
}