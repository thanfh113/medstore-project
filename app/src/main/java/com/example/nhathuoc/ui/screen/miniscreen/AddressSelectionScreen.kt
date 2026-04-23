package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.LocationOn
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
            CenterAlignedTopAppBar(
                title = { Text("Chọn địa chỉ giao hàng", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Quay lại",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onAddNewAddress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(1.dp, GreenTop),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenTop)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Thêm", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thêm địa chỉ mới", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        if (addresses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Bạn chưa có địa chỉ nào lưu sẵn.", color = Color.Gray, fontSize = 15.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(addresses, key = { it.id }) { address ->
                    val isSelected = selectedAddressId == address.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onAddressSelected(address.id) },
                        color = if (isSelected) Color(0xFFE8F5E9) else Color.White,
                        border = BorderStroke(1.5.dp, if (isSelected) GreenTop else Color.Transparent),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.LocationOn, "Location", tint = GreenTop)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(address.recipientName ?: "Chưa có tên", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    if (address.isDefault) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = GreenTop.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                "Mặc định",
                                                fontSize = 10.sp,
                                                color = GreenTop,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(address.phone ?: "Chưa có SĐT", fontSize = 13.sp, color = Color.Gray)
                                val fullAddr = "${address.address}, ${address.ward ?: ""}, ${address.district ?: ""}, ${address.province ?: ""}".replace(Regex(", ,|, $"), "")
                                Text(fullAddr, fontSize = 13.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
                            }
                            RadioButton(selected = isSelected, onClick = { onAddressSelected(address.id) })
                        }
                    }
                }
            }
        }
    }
}
