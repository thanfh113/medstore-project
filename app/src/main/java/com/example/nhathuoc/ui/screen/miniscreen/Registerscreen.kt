package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nhathuoc.ui.theme.NhathuocTheme
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.BgColor

@Composable
fun RegisterScreen(modifier: Modifier = Modifier, navController: NavController? = null) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var agreeToTerms by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Box(modifier = modifier.fillMaxSize()) {
        // Background
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .verticalScroll(rememberScrollState())
        ) {
            // Header với gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Logo/Icon
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(70.dp)
                    ) {
                        Icon(
                            Icons.Filled.PersonAdd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "NHÀ THUỐC HELLO",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Tạo tài khoản mới",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Back button
                IconButton(
                    onClick = { navController?.popBackStack() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Form card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        "Chào mừng bạn đến!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "Vui lòng tạo tài khoản để sử dụng ứng dụng",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Full name field
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Họ và tên",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholder = { Text("Nhập họ và tên của bạn") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = GreenTop
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Email field
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Email",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("Nhập địa chỉ email") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Email,
                                    contentDescription = null,
                                    tint = GreenTop
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Phone field
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Số điện thoại",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            placeholder = { Text("Nhập số điện thoại") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Phone,
                                    contentDescription = null,
                                    tint = GreenTop
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Password field
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Mật khẩu",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("Nhập mật khẩu (tối thiểu 6 ký tự)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = GreenTop
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                        contentDescription = if (isPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                                        tint = GreenTop
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Confirm password field
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Xác nhận mật khẩu",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = { Text("Nhập lại mật khẩu") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = GreenTop
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                    Icon(
                                        if (isConfirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                        contentDescription = if (isConfirmPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                                        tint = GreenTop
                                    )
                                }
                            },
                            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.fillMaxWidth(),
                            isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                            supportingText = if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                                {
                                    Text(
                                        "Mật khẩu không khớp",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp
                                    )
                                }
                            } else null
                        )
                    }

                    // Terms and conditions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { agreeToTerms = !agreeToTerms }
                    ) {
                        Checkbox(
                            checked = agreeToTerms,
                            onCheckedChange = { agreeToTerms = it },
                            colors = CheckboxDefaults.colors(checkedColor = GreenTop)
                        )
                        Column {
                            Text(
                                "Tôi đồng ý với ",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            Row {
                                Text(
                                    "Điều khoản sử dụng ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GreenTop,
                                    modifier = Modifier.clickable {
                                        // TODO: Open terms of service
                                    }
                                )
                                Text(
                                    "và ",
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Text(
                                    "Chính sách bảo mật",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GreenTop,
                                    modifier = Modifier.clickable {
                                        // TODO: Open privacy policy
                                    }
                                )
                            }
                        }
                    }

                    // Register button
                    Surface(
                        onClick = {
                            if (agreeToTerms && password == confirmPassword &&
                                fullName.isNotBlank() && email.isNotBlank() &&
                                phone.isNotBlank() && password.length >= 6) {
                                // TODO: Handle registration
                                navController?.navigate("MainScreen") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(50.dp),
                        color = if (agreeToTerms && password == confirmPassword &&
                                  fullName.isNotBlank() && email.isNotBlank() &&
                                  phone.isNotBlank() && password.length >= 6) {
                            GreenTop
                        } else {
                            Color.Gray.copy(alpha = 0.3f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                "Tạo tài khoản",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Divider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Divider(modifier = Modifier.weight(1f))
                        Text(
                            "  HOẶC  ",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Divider(modifier = Modifier.weight(1f))
                    }

                    // Quick register buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Google register
                        OutlinedButton(
                            onClick = { /* TODO: Google register */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Icon(
                                Icons.Outlined.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Google", fontSize = 14.sp)
                        }

                        // Facebook register
                        OutlinedButton(
                            onClick = { /* TODO: Facebook register */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Facebook,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Facebook", fontSize = 14.sp)
                        }
                    }
                }
            }

            // Login link
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Text(
                    "Đã có tài khoản? ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    "Đăng nhập ngay",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenTop,
                    modifier = Modifier.clickable {
                        navController?.navigate("LoginScreen")
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    NhathuocTheme {
        RegisterScreen()
    }
}