package com.example.nhathuoc.ui.screen.miniscreen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nhathuoc.data.model.AddAddressRequest
import com.example.nhathuoc.ui.theme.GreenTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAddressScreen(
    onSave: (AddAddressRequest) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var recipientName by remember { mutableStateOf("") }
    var recipientPhone by remember { mutableStateOf("") }
    var fullAddress by remember { mutableStateOf("") }
    var ward by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("home") }
    var isDefault by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thêm địa chỉ mới") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = GreenTop,
                    navigationIconContentColor = GreenTop
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        val phone = recipientPhone.filter(Char::isDigit)
                        when {
                            recipientName.isBlank() -> errorText = "Vui lòng nhập tên người nhận"
                            phone.length < 9 -> errorText = "Số điện thoại không hợp lệ"
                            fullAddress.isBlank() -> errorText = "Vui lòng nhập số nhà, tên đường"
                            province.isBlank() -> errorText = "Vui lòng nhập tỉnh/thành phố"
                            district.isBlank() -> errorText = "Vui lòng nhập quận/huyện"
                            ward.isBlank() -> errorText = "Vui lòng nhập phường/xã"
                            else -> {
                                errorText = null
                                onSave(
                                    AddAddressRequest(
                                        type = type,
                                        recipientName = recipientName.trim(),
                                        recipientPhone = phone,
                                        fullAddress = fullAddress.trim(),
                                        ward = ward.trim(),
                                        district = district.trim(),
                                        province = province.trim(),
                                        isDefault = isDefault
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
                ) {
                    Text("Lưu địa chỉ")
                }
            }
        },
        containerColor = Color(0xFFF5F7FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (errorText != null) {
                Text(
                    text = errorText!!,
                    color = Color(0xFFDC2626),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedTextField(
                value = recipientName,
                onValueChange = { recipientName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Người nhận") },
                singleLine = true
            )

            OutlinedTextField(
                value = recipientPhone,
                onValueChange = { recipientPhone = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số điện thoại") },
                singleLine = true
            )

            OutlinedTextField(
                value = fullAddress,
                onValueChange = { fullAddress = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số nhà, tên đường") },
                singleLine = true
            )

            OutlinedTextField(
                value = ward,
                onValueChange = { ward = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Phường/Xã") },
                singleLine = true
            )

            OutlinedTextField(
                value = district,
                onValueChange = { district = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Quận/Huyện") },
                singleLine = true
            )

            OutlinedTextField(
                value = province,
                onValueChange = { province = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tỉnh/Thành phố") },
                singleLine = true
            )

            Text(
                text = "Loại địa chỉ",
                color = GreenTop,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    "home" to "Nhà riêng",
                    "work" to "Công ty",
                    "other" to "Khác"
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = type == value,
                        onClick = { type = value },
                        label = { Text(label) }
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Đặt làm mặc định",
                            color = GreenTop,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ưu tiên dùng địa chỉ này khi thanh toán",
                            color = Color(0xFF6B7280)
                        )
                    }
                    Switch(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it }
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(84.dp))
        }
    }
}
