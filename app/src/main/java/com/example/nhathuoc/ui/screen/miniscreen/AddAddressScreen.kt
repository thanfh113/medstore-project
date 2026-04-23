package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.util.ValidationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressScreen(
    onSave: (name: String, phone: String, fullAddress: String, isDefault: Boolean) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var ward by remember { mutableStateOf("") }
    var specificAddress by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thêm địa chỉ mới", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                    Button(
                        onClick = {
                            // Validate
                            val (isNameValid, nameErrMsg) = ValidationUtils.isValidFullName(name)
                            nameError = nameErrMsg

                            val (isPhoneValid, phoneErrMsg) = ValidationUtils.isValidVietnamesePhone(phone)
                            phoneError = phoneErrMsg

                            val isAddressValid = ValidationUtils.isRequired(specificAddress) && ValidationUtils.isRequired(province)
                            addressError = if (!isAddressValid) "Vui lòng nhập đầy đủ Tỉnh/Thành phố và Địa chỉ cụ thể" else null

                            if (isNameValid && isPhoneValid && isAddressValid) {
                                // Tạo chuỗi địa chỉ đầy đủ
                                val parts = listOf(specificAddress, ward, district, province).filter { it.isNotBlank() }
                                val fullAddress = parts.joinToString(", ")
                                onSave(name, phone, fullAddress, isDefault)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
                    ) {
                        Text("Lưu địa chỉ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Thông tin liên hệ
            Text("Thông tin liên hệ", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = { Text("Họ và tên") },
                isError = nameError != null,
                supportingText = if (nameError != null) { { Text(nameError!!) } } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenTop)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; phoneError = null },
                label = { Text("Số điện thoại") },
                isError = phoneError != null,
                supportingText = if (phoneError != null) { { Text(phoneError!!) } } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenTop)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Địa chỉ
            Text("Địa chỉ giao hàng", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)

            OutlinedTextField(
                value = province,
                onValueChange = { province = it; addressError = null },
                label = { Text("Tỉnh / Thành phố") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenTop)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text("Quận / Huyện") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenTop)
                )
                OutlinedTextField(
                    value = ward,
                    onValueChange = { ward = it },
                    label = { Text("Phường / Xã") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenTop)
                )
            }

            OutlinedTextField(
                value = specificAddress,
                onValueChange = { specificAddress = it; addressError = null },
                label = { Text("Địa chỉ cụ thể (Số nhà, Tên đường...)") },
                isError = addressError != null,
                supportingText = if (addressError != null) { { Text(addressError!!) } } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenTop)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tùy chọn đặt làm mặc định
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { isDefault = !isDefault },
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Đặt làm địa chỉ mặc định", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GreenTop)
                    )
                }
            }
        }
    }
}