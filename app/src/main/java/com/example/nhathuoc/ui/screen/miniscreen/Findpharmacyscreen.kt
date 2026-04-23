@file:Suppress("DEPRECATION")
package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhathuoc.ui.theme.NhathuocTheme
import com.example.nhathuoc.ui.theme.GreenLight
private val GreenTop = Color(0xFF2E7D32)


data class PharmacyBranch(
    val id: Int,
    val name: String,
    val address: String,
    val distance: String,
    val status: String,
    val openTime: String,
    val isOpen: Boolean
)

private val samplePharmacies = listOf(
    PharmacyBranch(1, "Nhà Thuốc FPT Long Châu 1-2-3-4 Chợ Yên Xá",
        "Thôn Yên Xá (Cạnh Cổng Chợ Yên Xá), P. Thanh Liệt, TP. Hà Nội",
        "0.21 km", "Đang dùng", "Mở cửa lúc 07:00", false),
    PharmacyBranch(2, "Nhà Thuốc FPT Long Châu Triều Khúc",
        "Số 15 Triều Khúc, P. Thanh Xuân Nam, Q. Thanh Xuân, Hà Nội",
        "0.85 km", "Đang mở", "Đóng cửa lúc 22:00", true),
    PharmacyBranch(3, "Nhà Thuốc FPT Long Châu Văn Quán",
        "Khu đô thị Văn Quán, P. Văn Quán, Q. Hà Đông, Hà Nội",
        "1.20 km", "Đang mở", "Đóng cửa lúc 22:00", true),
    PharmacyBranch(4, "Nhà Thuốc FPT Long Châu Hà Trì",
        "Số 8 Hà Trì, P. Hà Cầu, Q. Hà Đông, Hà Nội",
        "1.80 km", "Đang mở", "Đóng cửa lúc 21:30", true),
    PharmacyBranch(5, "Nhà Thuốc FPT Long Châu Kiến Hưng",
        "Lô 12 Khu dân cư Kiến Hưng, P. Kiến Hưng, Q. Hà Đông, Hà Nội",
        "2.40 km", "Đang dùng", "Mở cửa lúc 07:00", false),
)

@Composable
fun FindPharmacyScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(searchQuery) {
        if (searchQuery.isEmpty()) samplePharmacies
        else samplePharmacies.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.address.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // TopAppBar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBackIos, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                // Search bar inline
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Search, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = Color(0xFF1A1A1A)),
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) Text("Tìm nhà thuốc theo địa chỉ", color = Color.Gray, fontSize = 13.sp)
                                inner()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.2f)) {
                    Icon(Icons.Outlined.Info, null, tint = Color.White,
                        modifier = Modifier.size(36.dp).padding(8.dp))
                }
            }
            // Sub-label
            TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("Hoặc chọn theo Tỉnh/Thành phố, Phường/Xã", color = Color.White, fontSize = 13.sp)
            }
        }

        // Map placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color(0xFFDCEEFA)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Map, null, tint = Color(0xFF90A4AE), modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(8.dp))
                Text("Bản đồ (Google Maps)", color = Color(0xFF90A4AE), fontSize = 13.sp)
                Text("Tích hợp MapView tại đây", color = Color(0xFFB0BEC5), fontSize = 11.sp)
            }
            // Simulated pins
            Icon(Icons.Filled.LocationOn, null, tint = GreenTop,
                modifier = Modifier.align(Alignment.Center).size(36.dp))
            Icon(Icons.Filled.LocationOn, null, tint = Color(0xFFE53935),
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 60.dp, top = 40.dp).size(30.dp))
            Icon(Icons.Filled.LocationOn, null, tint = Color(0xFFE53935),
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 50.dp, bottom = 30.dp).size(30.dp))
        }

        // Result count
        Surface(color = Color.White, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocalPharmacy, null, tint = GreenTop, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Tìm kiếm ", fontSize = 13.sp, color = Color.Gray)
                    Text("${filtered.size} nhà thuốc", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                }
                TextButton(onClick = {}) {
                    Text("Xem danh sách", color = GreenTop, fontSize = 12.sp)
                }
            }
        }

        // List
        LazyColumn(
            modifier = Modifier.weight(1f).background(Color(0xFFF5F7FA)),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { pharmacy ->
                PharmacyCard(pharmacy = pharmacy)
            }
        }
    }
}

@Composable
private fun PharmacyCard(pharmacy: PharmacyBranch) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(pharmacy.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A), lineHeight = 19.sp)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusColor = if (pharmacy.isOpen) Color(0xFF2E7D32) else Color(0xFFE53935)
                Text(pharmacy.status, fontSize = 12.sp, color = statusColor, fontWeight = FontWeight.SemiBold)
                Text(" • ${pharmacy.openTime}", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                Spacer(Modifier.width(2.dp))
                Text("${pharmacy.distance} • ${pharmacy.address}",
                    fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Directions, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Xem chỉ đường", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Phone, null, tint = GreenTop, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Gọi tư vấn", fontSize = 12.sp, color = GreenTop)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FindPharmacyScreenPreview() { NhathuocTheme { FindPharmacyScreen() } }