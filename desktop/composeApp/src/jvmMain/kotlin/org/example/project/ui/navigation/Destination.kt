package org.example.project.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Destination("dashboard", "Tổng quan", Icons.Filled.Home)
    object Products : Destination("products", "Sản phẩm Vật tư", Icons.Filled.Build)
    object Orders : Destination("orders", "Yêu cầu Báo giá", Icons.Filled.List)
    object Chat : Destination("chat", "Hỗ trợ Kỹ thuật", Icons.Filled.MailOutline)
    object Settings : Destination("settings", "Cài đặt", Icons.Filled.Settings)

    companion object {
        val all = listOf(Dashboard, Products, Orders, Chat, Settings)
    }
}
