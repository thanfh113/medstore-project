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
        tabLabel = "Cúm",
        description = "Cúm khiến cơ thể mệt mỏi, dễ đuối sức, đặc biệt ở người lớn tuổi và trẻ nhỏ. Tiêm vắc xin cúm hàng năm và tăng cường đề kháng có thể giúp giảm nguy cơ mắc bệnh.",
        products = listOf(
            DiseaseProduct(id=201, Icons.Outlined.Vaccines, Color(0xFF00838F), Color(0xFFE0F7FA), "Trà thảo mộc Good Night Datino", "45.000đ", null, null, "Mua 2 tặng 1"),
            DiseaseProduct(id=202, Icons.Outlined.LocalFlorist, Color(0xFF2E7D32), Color(0xFFE8F5E9), "Bột Tía Tô Nguyên Chất Datino (15 gói)", "86.250đ", "115.000đ", 25, null),
            DiseaseProduct(id=203, Icons.Outlined.MedicalServices, Color(0xFF1565C0), Color(0xFFE3F2FD), "Thuốc cúm Tamiflu 75mg", "320.000đ", "380.000đ", 16, null),
        )
    ),
    DiseaseSample(
        tabLabel = "Sốt xuất huyết",
        description = "Sốt xuất huyết lây qua muỗi Aedes, gây sốt cao đột ngột và có thể dẫn đến biến chứng nguy hiểm. Diệt muỗi và bảo vệ cơ thể là cách phòng bệnh hiệu quả nhất.",
        products = listOf(
            DiseaseProduct(id=204, Icons.Outlined.Opacity, Color(0xFFB71C1C), Color(0xFFFFEBEE), "Oresol bù điện giải", "25.000đ", null, null, null),
            DiseaseProduct(id=205, Icons.Outlined.MedicalServices, Color(0xFF1565C0), Color(0xFFE3F2FD), "Paracetamol 500mg", "15.000đ", null, null, null),
        )
    ),
    DiseaseSample(
        tabLabel = "Viêm phổi",
        description = "Viêm phổi do vi khuẩn hoặc virus gây ra, cần điều trị sớm để tránh biến chứng nặng. Tăng cường miễn dịch và tiêm phòng là biện pháp phòng ngừa tốt nhất.",
        products = listOf(
            DiseaseProduct(id=206, Icons.Outlined.Air, Color(0xFF0277BD), Color(0xFFE1F5FE), "Vitamin C 1000mg tăng đề kháng", "89.000đ", "120.000đ", 26, null),
            DiseaseProduct(id=207, Icons.Outlined.Shield, Color(0xFF6A1B9A), Color(0xFFF3E5F5), "Kẽm hữu cơ tăng miễn dịch", "145.000đ", "180.000đ", 19, null),
        )
    ),
    DiseaseSample(
        tabLabel = "Viêm amidan trẻ",
        description = "Viêm amidan thường gặp ở trẻ em, gây đau họng và khó nuốt. Giữ vệ sinh răng miệng và tránh tiếp xúc nguồn lây là cách phòng bệnh hiệu quả.",
        products = listOf(
            DiseaseProduct(id=208, Icons.Outlined.ChildCare, Color(0xFF00838F), Color(0xFFE0F7FA), "Siro ho trẻ em Prospan", "185.000đ", "220.000đ", 16, null),
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
            text = "Bệnh phổ biến mùa này",
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