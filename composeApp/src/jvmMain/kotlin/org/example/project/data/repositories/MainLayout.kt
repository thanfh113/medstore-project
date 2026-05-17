package org.example.project.ui.layout

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SidebarBg = Color(0xFF132B16)
private val SidebarHeaderBg = Color(0xFF0D1F10)
private val SidebarItemSelectedBg = Color(0x26FFFFFF)
private val SidebarAccentBar = Color(0xFF81C784)
private val SidebarText = Color(0xFFE8F5E9)
private val SidebarTextMuted = Color(0xFF6D8F72)
private val SidebarDivider = Color(0xFF1F3D22)

private val CollapsedWidth = 64.dp
private val ExpandedWidth = 220.dp

data class MenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

private data class MenuSection(
    val label: String?,
    val items: List<MenuItem>
)

private fun buildSections(userRole: String): List<MenuSection> {
    val sections = mutableListOf<MenuSection>()
    if (userRole == "ADMIN") {
        sections.add(MenuSection(null, listOf(
            MenuItem("Tổng quan", Icons.Default.Dashboard, "dashboard")
        )))
    }
    sections.add(MenuSection("BÁN HÀNG", buildList {
        add(MenuItem("POS Bán hàng", Icons.Default.PointOfSale, "pos"))
        add(MenuItem("Đơn hàng", Icons.Default.ShoppingCart, "orders"))
        add(MenuItem("Sản phẩm", Icons.Default.Inventory, "products"))
    }))
    if (userRole == "ADMIN") {
        sections.add(MenuSection("NỘI DUNG", listOf(
            MenuItem("Banner", Icons.Default.Campaign, "banners"),
            MenuItem("Mã giảm giá", Icons.Default.LocalOffer, "coupons")
        )))
        sections.add(MenuSection("QUẢN LÝ", listOf(
            MenuItem("Tài chính", Icons.Default.MonetizationOn, "finance"),
            MenuItem("Nhân sự", Icons.Default.People, "personnel")
        )))
    }
    sections.add(MenuSection("HỖ TRỢ", buildList {
        add(MenuItem("Chat KH", Icons.Default.Chat, "chat"))
        add(MenuItem("CSKH", Icons.Default.Warning, "ops"))
    }))
    return sections
}

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
                    modifier = Modifier.size(80.dp),
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
                Button(onClick = onLogout) { Text("Đăng xuất") }
            }
        }
        return
    }

    var isExpanded by remember { mutableStateOf(true) }
    val sidebarWidth by animateDpAsState(
        targetValue = if (isExpanded) ExpandedWidth else CollapsedWidth,
        label = "sidebar_width"
    )
    val sections = buildSections(userRole)

    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(sidebarWidth)
                .background(SidebarBg)
        ) {
            // App branding header
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SidebarHeaderBg)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text(
                        "MedStore",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Quản lý Vật tư Y tế",
                        color = SidebarTextMuted,
                        fontSize = 11.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SidebarHeaderBg)
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "M",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                }
            }

            HorizontalDivider(color = SidebarDivider, thickness = 1.dp)

            // Navigation sections
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 4.dp)
            ) {
                sections.forEach { section ->
                    if (isExpanded) {
                        if (section.label != null) {
                            Text(
                                section.label.uppercase(),
                                modifier = Modifier.padding(start = 20.dp, top = 18.dp, bottom = 4.dp, end = 16.dp),
                                color = SidebarTextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        } else {
                            Spacer(Modifier.height(8.dp))
                        }
                    } else {
                        Spacer(Modifier.height(6.dp))
                    }
                    section.items.forEach { item ->
                        if (isExpanded) {
                            SidebarNavItem(
                                item = item,
                                isSelected = currentRoute == item.route,
                                onClick = { onNavigate(item.route) }
                            )
                        } else {
                            SidebarNavItemCollapsed(
                                item = item,
                                isSelected = currentRoute == item.route,
                                onClick = { onNavigate(item.route) }
                            )
                        }
                    }
                }
            }

            // Bottom: role info + toggle + logout
            HorizontalDivider(color = SidebarDivider, thickness = 1.dp)

            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text("Vai trò", color = SidebarTextMuted, fontSize = 10.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            if (userRole == "ADMIN") "Quản trị viên" else "Nhân viên",
                            color = SidebarText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = if (userRole == "ADMIN") Color(0xFF2E7D32) else Color(0xFF1565C0),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                if (userRole == "ADMIN") "ADMIN" else "STAFF",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                HorizontalDivider(color = SidebarDivider, thickness = 1.dp)
            }

            // Toggle collapse (above logout)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { isExpanded = !isExpanded }
                    )
                    .background(SidebarHeaderBg.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowLeft else Icons.Default.KeyboardArrowRight,
                        contentDescription = if (isExpanded) "Thu gọn" else "Mở rộng",
                        tint = SidebarText.copy(alpha = 0.55f),
                        modifier = Modifier.size(18.dp)
                    )
                    if (isExpanded) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Thu gọn",
                            color = SidebarText.copy(alpha = 0.55f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = SidebarDivider, thickness = 1.dp)

            if (isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onLogout
                        )
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Đăng xuất",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Đăng xuất",
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                        fontSize = 14.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onLogout
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Đăng xuất",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Main content
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) { content() }
    }
}

@Composable
private fun SidebarNavItem(
    item: MenuItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    if (isSelected) SidebarAccentBar else Color.Transparent,
                    RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp)
                )
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(if (isSelected) SidebarItemSelectedBg else Color.Transparent)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = if (isSelected) SidebarAccentBar else SidebarText.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                item.title,
                color = if (isSelected) Color.White else SidebarText.copy(alpha = 0.7f),
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SidebarNavItemCollapsed(
    item: MenuItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(if (isSelected) SidebarItemSelectedBg else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(28.dp)
                    .align(Alignment.CenterStart)
                    .background(SidebarAccentBar, RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
            )
        }
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = if (isSelected) SidebarAccentBar else SidebarText.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}
