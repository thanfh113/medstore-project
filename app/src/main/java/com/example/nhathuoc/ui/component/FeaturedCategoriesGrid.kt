package com.example.nhathuoc.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.nhathuoc.ui.theme.GreenTop
private val CatIconBg = Color(0xFFEEF2FF)

data class FeaturedCategory(
    val icon: ImageVector,
    val name: String,
    val productCount: Int
)

val defaultFeaturedCategories = listOf(
    FeaturedCategory(Icons.Outlined.Psychology,       "Thần kinh não",           59),
    FeaturedCategory(Icons.Outlined.MedicalServices,  "Vitamin & Khoáng chất",   80),
    FeaturedCategory(Icons.Outlined.Favorite,         "Tim mạch - Huyết áp",     23),
    FeaturedCategory(Icons.Outlined.Shield,           "Miễn dịch - Đề kháng",   53),
    FeaturedCategory(Icons.Outlined.Restaurant,       "Tiêu hóa",               83),
    FeaturedCategory(Icons.Outlined.Science,          "Sinh lý - Nội tiết tố",  45),
    FeaturedCategory(Icons.Outlined.Vaccines,         "Vắc xin",                 18),
    FeaturedCategory(Icons.Outlined.RemoveRedEye,     "Chăm sóc mắt",           31),
)

@Composable
fun FeaturedCategoriesGrid(
    categories: List<FeaturedCategory> = defaultFeaturedCategories,
    initialShowCount: Int = 6,
    onCategoryClick: (FeaturedCategory) -> Unit = {},
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val displayed = if (expanded) categories else categories.take(initialShowCount)
    val hiddenCount = categories.size - initialShowCount

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Danh mục nổi bật",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 2-column grid
        displayed.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { cat ->
                    CategoryCard(
                        category = cat,
                        onClick = {
                            onCategoryClick(cat)
                            navController?.navigate("CategoryProductScreen/${cat.name.replace(" ", "_")}")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                // fill empty slot if odd number
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        // Expand/collapse button
        if (categories.size > initialShowCount) {
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = if (expanded) "Thu gọn" else "Xem thêm $hiddenCount danh mục",
                    color = GreenTop,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: FeaturedCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CatIconBg,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.name,
                        tint = GreenTop,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = category.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Có ${category.productCount} sản phẩm",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}