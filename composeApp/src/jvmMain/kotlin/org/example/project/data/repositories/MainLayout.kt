package org.example.project.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class MenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun MainLayout(
    userRole: String,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    if (userRole != "ADMIN" && userRole != "EMPLOYEE") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.width(80.dp).height(80.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tài khoản này không được phép truy cập desktop",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onLogout) {
                    Text("Đăng xuất")
                }
            }
        }
        return
    }

    val menus = mutableListOf<MenuItem>()

    if (userRole == "ADMIN") {
        menus.add(MenuItem("Tổng quan", Icons.Default.Dashboard, "dashboard"))
    }
    menus.add(MenuItem("POS", Icons.Default.PointOfSale, "pos"))
    menus.add(MenuItem("Đơn hàng", Icons.Default.ShoppingCart, "orders"))
    menus.add(MenuItem("Sản phẩm", Icons.Default.Inventory, "products"))
    if (userRole == "ADMIN") {
        menus.add(MenuItem("Banner", Icons.Default.Campaign, "banners"))
    }
    menus.add(MenuItem("Chat", Icons.Default.Chat, "chat"))
    menus.add(MenuItem("CSKH", Icons.Default.Warning, "ops"))

    if (userRole == "ADMIN") {
        menus.add(MenuItem("Mã giảm giá", Icons.Default.LocalOffer, "coupons"))
        menus.add(MenuItem("Tài chính", Icons.Default.MonetizationOn, "finance"))
        menus.add(MenuItem("Nhân sự", Icons.Default.People, "personnel"))
    }

    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        NavigationRail(
            modifier = Modifier.fillMaxHeight().width(100.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            menus.forEach { menu ->
                NavigationRailItem(
                    selected = currentRoute == menu.route,
                    onClick = { onNavigate(menu.route) },
                    icon = { Icon(menu.icon, contentDescription = menu.title) },
                    label = { Text(menu.title) }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            NavigationRailItem(
                selected = false,
                onClick = onLogout,
                icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Đăng xuất") },
                label = { Text("Đăng xuất", color = MaterialTheme.colorScheme.error) }
            )
        }
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) { content() }
    }
}
