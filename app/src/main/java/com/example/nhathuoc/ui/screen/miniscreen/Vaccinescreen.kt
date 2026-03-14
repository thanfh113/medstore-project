package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhathuoc.ui.theme.NhathuocTheme

private val Teal      = Color(0xFF26A69A)
private val TealLight = Color(0xFF80CBC4)
private val TealBg    = Color(0xFFE0F2F1)

@Composable
fun VaccineScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("b bào ngọc") }
    var gender   by remember { mutableStateOf("Nam") }
    var dob      by remember { mutableStateOf("") }
    val phone    = "0329645776"

    Column(modifier = modifier.fillMaxSize()) {
        // TopAppBar teal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Teal, TealLight)))
                .statusBarsPadding()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)) {
                Icon(Icons.Filled.ArrowBackIos, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero teal banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Brush.verticalGradient(listOf(Teal, TealLight))),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Logo row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.MedicalServices, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("TIÊM CHỦNG", color = Color.White, fontSize = 10.sp, letterSpacing = 1.sp)
                            Text("HELLO", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    // Illustration icons
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.Bottom) {
                        Icon(Icons.Outlined.LocalHospital, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(40.dp))
                        Icon(Icons.Outlined.PregnantWoman, null, tint = Color.White, modifier = Modifier.size(56.dp))
                        Icon(Icons.Outlined.FamilyRestroom, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(40.dp))
                    }
                }
                // Sparkle decorators
                Text("✦", color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.TopEnd).padding(end = 40.dp, top = 16.dp))
                Text("✦", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.TopStart).padding(start = 60.dp, top = 30.dp))
            }

            // White card
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                    Text("Bổ sung thông tin",
                        fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A1A), textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Vui lòng nhập thêm thông tin để Long Châu gợi ý các vắc xin phù hợp với Quý khách.",
                        fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(20.dp))

                    // Họ và tên
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = {
                            Row {
                                Text("Họ và tên ", fontSize = 12.sp)
                                Text("*", color = Color.Red, fontSize = 12.sp)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal,
                            unfocusedBorderColor = Color(0xFFDDDDDD)
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    // Giới tính
                    Row {
                        Text("Giới tính ", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                        Text("*", color = Color.Red, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(
                            Triple("Nam",  Icons.Outlined.Male,   Color(0xFF26A69A)),
                            Triple("Nữ",   Icons.Outlined.Female, Color(0xFFE91E63)),
                            Triple("Khác", Icons.Outlined.Transgender, Color(0xFFFF9800))
                        ).forEach { (label, icon, tint) ->
                            val isSelected = gender == label
                            Surface(
                                onClick = { gender = label },
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) TealBg else Color.White,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (isSelected) Teal else Color(0xFFDDDDDD)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(label, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Teal else Color(0xFF1A1A1A))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Ngày sinh
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = {
                            Row {
                                Text("Ngày sinh ", fontSize = 12.sp)
                                Text("*", color = Color.Red, fontSize = 12.sp)
                            }
                        },
                        trailingIcon = {
                            Icon(Icons.Outlined.CalendarMonth, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal,
                            unfocusedBorderColor = Color(0xFFDDDDDD)
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    // Số điện thoại (readonly)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Số điện thoại", fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {},
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("Xác nhận", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VaccineScreenPreview() { NhathuocTheme { VaccineScreen() } }