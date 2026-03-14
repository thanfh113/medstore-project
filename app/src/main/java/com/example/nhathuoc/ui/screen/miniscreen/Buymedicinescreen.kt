package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhathuoc.ui.theme.NhathuocTheme
import com.example.nhathuoc.ui.theme.GreenLight
private val GreenTop = Color(0xFF2E7D32)


@Composable
fun BuyMedicineScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    var fullName   by remember { mutableStateOf("b bào ngọc") }
    var phone      by remember { mutableStateOf("0329 645 776") }
    var note       by remember { mutableStateOf("") }
    var medicines  by remember { mutableStateOf(listOf<String>()) }
    var newMed     by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        // TopAppBar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
                .statusBarsPadding()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)) {
                Icon(Icons.Filled.ArrowBackIos, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Text("Cần mua thuốc", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = {}, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)) {
                Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.2f)) {
                    Icon(Icons.Outlined.HelpOutline, null, tint = Color.White,
                        modifier = Modifier.size(32.dp).padding(4.dp))
                }
            }
        }

        // Hero banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Brush.verticalGradient(listOf(GreenTop, GreenLight))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Outlined.CameraAlt, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(28.dp))
                    Box(
                        modifier = Modifier.size(90.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.MedicalServices, null, tint = Color.White, modifier = Modifier.size(52.dp))
                    }
                    Icon(Icons.Outlined.Add, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                }
            }
            // + decorators
            Text("+", color = Color.White.copy(alpha = 0.5f), fontSize = 22.sp,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 40.dp, top = 20.dp))
            Text("+", color = Color.White.copy(alpha = 0.5f), fontSize = 22.sp,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 30.dp, top = 40.dp))
            Text("+", color = Color.White.copy(alpha = 0.5f), fontSize = 22.sp,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 60.dp, bottom = 20.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F7FA))
        ) {
            // Thêm ảnh đơn thuốc
            Surface(color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(50), color = GreenTop) {
                        Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(28.dp).padding(4.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Thêm ảnh nếu có đơn thuốc (không bắt buộc)",
                            color = GreenTop, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Giúp dược sỹ tư vấn chính xác nhất",
                            color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            // Thêm thuốc
            Surface(color = Color.White) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(50), color = GreenTop) {
                            Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(28.dp).padding(4.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("Thêm thuốc cần tư vấn (không bắt buộc)",
                                color = GreenTop, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Nhập theo tên thuốc hoặc sản phẩm",
                                color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                    medicines.forEach { med ->
                        Spacer(Modifier.height(8.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF0F4FF)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(med, fontSize = 13.sp, color = Color(0xFF1A1A1A))
                                IconButton(onClick = { medicines = medicines - med }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Filled.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Thông tin liên hệ
            Surface(color = Color.White) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
                    Text("Thông tin liên hệ", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A1A))
                    Spacer(Modifier.height(14.dp))

                    LcOutlinedField(label = "Họ và tên", value = fullName, onValueChange = { fullName = it })
                    Spacer(Modifier.height(10.dp))
                    LcOutlinedField(label = "Số điện thoại", value = phone, onValueChange = { phone = it })
                    Spacer(Modifier.height(10.dp))
                    LcOutlinedField(
                        label = "Ghi chú (không bắt buộc)",
                        value = note,
                        onValueChange = { note = it },
                        placeholder = "Ví dụ: Tôi cần tư vấn thuốc về bệnh đau dạ dày",
                        minLines = 3
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Gửi yêu cầu
            Column(modifier = Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Gửi yêu cầu tư vấn", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.clickable { },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Receipt, null, tint = GreenTop, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Xem lại Đơn thuốc của tôi", color = GreenTop, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LcOutlinedField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        placeholder = if (placeholder.isNotEmpty()) ({ Text(placeholder, fontSize = 13.sp, color = Color.Gray) }) else null,
        minLines = minLines,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF1565C0),
            unfocusedBorderColor = Color(0xFFDDDDDD),
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BuyMedicineScreenPreview() { NhathuocTheme { BuyMedicineScreen() } }