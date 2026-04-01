package com.example.nhathuoc.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import com.example.nhathuoc.ui.theme.GreenTop
private val IconBg  = Color(0xFFF0F4FF)

data class QuickAccessItem(
    val icon: ImageVector,
    val label: String,
    val iconTint: Color = GreenTop,
    val route: String = ""
)

val defaultQuickAccessItems = listOf(
    QuickAccessItem(Icons.Outlined.MedicalServices,  "Tìm\nthiết bị",       route = "MedicalSuppliesQuoteScreen"),
    QuickAccessItem(Icons.Outlined.RequestQuote,     "Yêu cầu\nbáo giá",    route = "MedicalSuppliesQuoteScreen"),
    QuickAccessItem(Icons.Outlined.Receipt,          "Đơn của\ntôi",        route = "MyOrdersScreen"),
    QuickAccessItem(Icons.Outlined.LocalShipping,    "Theo dõi\ngiao hàng", route = ""),
    QuickAccessItem(Icons.Outlined.Store,            "Cửa hàng\ngần nhất",  route = "FindPharmacyScreen"),
    QuickAccessItem(Icons.Outlined.SupportAgent,     "Hỗ trợ\nkỹ thuật",   route = ""),
    QuickAccessItem(Icons.Outlined.Assignment,       "Hướng dẫn\nsử dụng", route = ""),
)

@Composable
fun QuickAccessRow(
    navController: NavController? = null,
    items: List<QuickAccessItem> = defaultQuickAccessItems,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEach { item ->
                QuickAccessCell(item = item) {
                    if (item.route.isNotEmpty()) navController?.navigate(item.route)
                }
            }
        }
    }
}

@Composable
private fun QuickAccessCell(item: QuickAccessItem, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(4.dp),
        modifier = Modifier.width(72.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = RoundedCornerShape(14.dp), color = IconBg, modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(item.icon, item.label, tint = item.iconTint, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                item.label, fontSize = 11.sp, color = Color(0xFF333333),
                lineHeight = 14.sp, textAlign = TextAlign.Center, minLines = 2
            )
        }
    }
}