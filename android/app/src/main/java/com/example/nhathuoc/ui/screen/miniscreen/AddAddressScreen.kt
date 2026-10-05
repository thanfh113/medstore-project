package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            com.example.nhathuoc.ui.component.GreenAppTopBar(
                title = "Thêm địa chỉ mới",
                subtitle = "Điền thông tin giao hàng",
                onBack = onBack
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Button(
                        onClick = {
                            val (isNameValid, nameErrMsg) = ValidationUtils.isValidFullName(name)
                            nameError = nameErrMsg
                            val (isPhoneValid, phoneErrMsg) = ValidationUtils.isValidVietnamesePhone(phone)
                            phoneError = phoneErrMsg
                            val isAddressValid = ValidationUtils.isRequired(specificAddress) && ValidationUtils.isRequired(province)
                            addressError = if (!isAddressValid) "Vui lòng nhập Tỉnh/Thành phố và Địa chỉ cụ thể" else null
                            if (isNameValid && isPhoneValid && isAddressValid) {
                                val parts = listOf(specificAddress, ward, district, province).filter { it.isNotBlank() }
                                val fullAddress = parts.joinToString(", ")
                                onSave(name, phone, fullAddress, isDefault)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            "Lưu địa chỉ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // === CONTACT SECTION ===
            FormSection(
                title = "Thông tin liên hệ",
                icon = Icons.Outlined.Badge
            ) {
                StyledTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = "Họ và tên người nhận",
                    placeholder = "Nguyễn Văn A",
                    leadingIcon = Icons.Outlined.Badge,
                    error = nameError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(4.dp))
                StyledTextField(
                    value = phone,
                    onValueChange = { phone = it; phoneError = null },
                    label = "Số điện thoại",
                    placeholder = "0912 345 678",
                    leadingIcon = Icons.Outlined.Phone,
                    error = phoneError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )
                )
            }

            // === ADDRESS SECTION ===
            FormSection(
                title = "Địa chỉ giao hàng",
                icon = Icons.Outlined.LocationOn
            ) {
                StyledTextField(
                    value = province,
                    onValueChange = { province = it; addressError = null },
                    label = "Tỉnh / Thành phố",
                    placeholder = "TP. Hồ Chí Minh",
                    leadingIcon = Icons.Outlined.LocationOn,
                    error = null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        StyledTextField(
                            value = district,
                            onValueChange = { district = it },
                            label = "Quận / Huyện",
                            placeholder = "Quận 1",
                            leadingIcon = null,
                            error = null,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StyledTextField(
                            value = ward,
                            onValueChange = { ward = it },
                            label = "Phường / Xã",
                            placeholder = "Phường Bến Nghé",
                            leadingIcon = null,
                            error = null,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                StyledTextField(
                    value = specificAddress,
                    onValueChange = { specificAddress = it; addressError = null },
                    label = "Địa chỉ cụ thể",
                    placeholder = "Số nhà, tên đường...",
                    leadingIcon = Icons.Outlined.Home,
                    error = addressError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    singleLine = false,
                    minLines = 2
                )
            }

            // === DEFAULT TOGGLE ===
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isDefault) MaterialTheme.colorScheme.primaryContainer else Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .clickable { isDefault = !isDefault }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Đặt làm địa chỉ mặc định",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Dùng cho mọi đơn hàng tiếp theo",
                            fontSize = 12.sp,
                            color = if (isDefault) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF9CA3AF)
                        )
                    }
                    Switch(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFCDD5CF)
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 0.2.sp
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp, modifier = Modifier.padding(bottom = 14.dp))
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector?,
    error: String?,
    keyboardOptions: KeyboardOptions,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 13.sp) },
            placeholder = { Text(placeholder, color = Color(0xFFB0BEC5), fontSize = 14.sp) },
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        leadingIcon,
                        contentDescription = null,
                        tint = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            minLines = minLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorLabelColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFFAFCFA)
            )
        )
        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            ) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
        }
    }
}