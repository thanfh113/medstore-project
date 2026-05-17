package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhathuoc.data.model.UserAddressDto
import com.example.nhathuoc.ui.theme.GreenTop

private val GreenLight = Color(0xFFE8F5E9)
private val TextPrimary = Color(0xFF1B2B1F)
private val TextSecondary = Color(0xFF5A7A62)
private val BgGray = Color(0xFFF3F7F4)
private val DividerColor = Color(0xFFE0EDE3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressSelectionScreen(
    addresses: List<UserAddressDto>,
    selectedAddressId: String?,
    onAddressSelected: (String) -> Unit,
    onAddNewAddress: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            com.example.nhathuoc.ui.component.GreenAppTopBar(
                title = "Chọn địa chỉ giao hàng",
                subtitle = "${addresses.size} địa chỉ",
                onBack = onBack
            )
        },
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 12.dp) {
                Button(
                    onClick = onAddNewAddress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Thêm địa chỉ mới", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = BgGray
    ) { padding ->
        if (addresses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(GreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = GreenTop,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                    Text(
                        "Chưa có địa chỉ nào",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        "Thêm địa chỉ để tiếp tục đặt hàng.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(addresses, key = { it.id }) { address ->
                    val isSelected = selectedAddressId == address.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAddressSelected(address.id) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) GreenLight else Color.White,
                        border = BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) GreenTop else DividerColor
                        ),
                        shadowElevation = if (isSelected) 0.dp else 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) GreenTop else Color(0xFFF0F4F1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else GreenTop,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        address.recipientName ?: "Chưa có tên",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isSelected) Color(0xFF2E7D32) else TextPrimary
                                    )
                                    if (address.isDefault) {
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (isSelected) GreenTop else Color(0xFFE8F5E9)
                                        ) {
                                            Text(
                                                "Mặc định",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else GreenTop,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    address.recipientPhone ?: "Chưa có SĐT",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Spacer(Modifier.height(4.dp))
                                val fullAddr = listOf(address.address, address.ward, address.district, address.province)
                                    .filterNotNull()
                                    .filter { it.isNotBlank() }
                                    .joinToString(", ")
                                Text(
                                    fullAddr,
                                    fontSize = 13.sp,
                                    color = Color(0xFF4B5563),
                                    lineHeight = 18.sp
                                )
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = { onAddressSelected(address.id) },
                                colors = RadioButtonDefaults.colors(selectedColor = GreenTop)
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}