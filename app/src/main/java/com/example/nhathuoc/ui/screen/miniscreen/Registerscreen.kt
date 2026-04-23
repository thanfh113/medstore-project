package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.ui.theme.BgColor
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.NhathuocTheme
import com.example.nhathuoc.util.ValidationUtils
import com.example.nhathuoc.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null
) {
    val viewModel: AuthViewModel = hiltViewModel()
    val registerState by viewModel.registerState.collectAsState()
    val focusManager = LocalFocusManager.current

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var termsError by remember { mutableStateOf<String?>(null) }
    var hasNavigated by remember { mutableStateOf(false) }

    val passwordStrength = remember(password) {
        ValidationUtils.assessPasswordStrength(password)
    }

    LaunchedEffect(Unit) {
        viewModel.clearRegisterState()
        hasNavigated = false
    }

    LaunchedEffect(registerState) {
        if (registerState is UiState.Success && !hasNavigated) {
            hasNavigated = true
            navController?.navigate("MainScreen") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            RegisterHeader(
                onBack = { navController?.popBackStack() }
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 32.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Text(
                        text = "Tạo tài khoản",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = GreenTop
                    )
                    Text(
                        text = "Đăng ký để đặt mua vật tư y tế và nhận tư vấn từ nhân viên chuyên môn.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF5E6B5F),
                        lineHeight = 20.sp
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            fullNameError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Họ và tên") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Person, contentDescription = null, tint = GreenTop)
                        },
                        isError = fullNameError != null,
                        supportingText = fullNameError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            phoneError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Số điện thoại") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Phone, contentDescription = null, tint = GreenTop)
                        },
                        isError = phoneError != null,
                        supportingText = phoneError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, contentDescription = null, tint = GreenTop)
                        },
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Mật khẩu") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = GreenTop)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (isPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    if (password.isNotBlank()) {
                        PasswordStrengthView(passwordStrength)
                    }

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            confirmPasswordError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nhập lại mật khẩu") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = GreenTop)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (isConfirmPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                                )
                            }
                        },
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = confirmPasswordError != null,
                        supportingText = confirmPasswordError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                submitRegister(
                                    fullName = fullName,
                                    phone = phone,
                                    email = email,
                                    password = password,
                                    confirmPassword = confirmPassword,
                                    agreeToTerms = agreeToTerms,
                                    onFullNameError = { fullNameError = it },
                                    onPhoneError = { phoneError = it },
                                    onEmailError = { emailError = it },
                                    onPasswordError = { passwordError = it },
                                    onConfirmPasswordError = { confirmPasswordError = it },
                                    onTermsError = { termsError = it },
                                    onSubmit = viewModel::register
                                )
                            }
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = agreeToTerms,
                            onCheckedChange = {
                                agreeToTerms = it
                                termsError = null
                            }
                        )
                        Text(
                            text = buildAnnotatedString {
                                append("Tôi đồng ý với ")
                                withStyle(SpanStyle(color = GreenTop, fontWeight = FontWeight.Bold)) {
                                    append("điều khoản sử dụng")
                                }
                                append(" và ")
                                withStyle(SpanStyle(color = GreenTop, fontWeight = FontWeight.Bold)) {
                                    append("chính sách bảo mật")
                                }
                            },
                            color = Color(0xFF5E6B5F),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                    if (termsError != null) {
                        Text(
                            text = termsError.orEmpty(),
                            color = Color(0xFFC62828),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (registerState is UiState.Error) {
                        RegisterErrorCard((registerState as UiState.Error).message)
                    }

                    Button(
                        onClick = {
                            submitRegister(
                                fullName = fullName,
                                phone = phone,
                                email = email,
                                password = password,
                                confirmPassword = confirmPassword,
                                agreeToTerms = agreeToTerms,
                                onFullNameError = { fullNameError = it },
                                onPhoneError = { phoneError = it },
                                onEmailError = { emailError = it },
                                onPasswordError = { passwordError = it },
                                onConfirmPasswordError = { confirmPasswordError = it },
                                onTermsError = { termsError = it },
                                onSubmit = viewModel::register
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        enabled = registerState !is UiState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
                    ) {
                        if (registerState is UiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Tạo tài khoản",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Đã có tài khoản? ", color = Color(0xFF5E6B5F))
                        Text(
                            text = "Đăng nhập",
                            color = GreenTop,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                navController?.navigate("LoginScreen")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(GreenTop, GreenLight)
                )
            )
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(start = 12.dp, top = 16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Quay lại",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MedicalServices,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Tạo tài khoản",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Mua vật tư y tế nhanh và an toàn",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PasswordStrengthView(result: ValidationUtils.PasswordStrengthResult) {
    val color = when (result.strength) {
        ValidationUtils.PasswordStrength.WEAK -> Color(0xFFD32F2F)
        ValidationUtils.PasswordStrength.MEDIUM -> Color(0xFFF57C00)
        ValidationUtils.PasswordStrength.STRONG -> GreenTop
    }
    val label = when (result.strength) {
        ValidationUtils.PasswordStrength.WEAK -> "Yếu"
        ValidationUtils.PasswordStrength.MEDIUM -> "Trung bình"
        ValidationUtils.PasswordStrength.STRONG -> "Mạnh"
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Độ mạnh mật khẩu",
                color = Color(0xFF5E6B5F),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = label,
                color = color,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
        }
        LinearProgressIndicator(
            progress = { result.score / 3f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(99.dp)),
            color = color,
            trackColor = Color(0xFFE5EDE5)
        )
        Text(
            text = result.feedback,
            color = Color(0xFF6C786D),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun RegisterErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = message,
            color = Color(0xFFC62828),
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun submitRegister(
    fullName: String,
    phone: String,
    email: String,
    password: String,
    confirmPassword: String,
    agreeToTerms: Boolean,
    onFullNameError: (String?) -> Unit,
    onPhoneError: (String?) -> Unit,
    onEmailError: (String?) -> Unit,
    onPasswordError: (String?) -> Unit,
    onConfirmPasswordError: (String?) -> Unit,
    onTermsError: (String?) -> Unit,
    onSubmit: (String, String, String, String) -> Unit
) {
    val (nameValid, nameError) = ValidationUtils.isValidFullName(fullName)
    val (phoneValid, phoneError) = ValidationUtils.isValidVietnamesePhone(phone)
    val (emailValid, emailError) = ValidationUtils.isValidEmail(email)
    val (passwordValid, passwordError) = ValidationUtils.isValidPassword(password)
    val (confirmValid, confirmError) = ValidationUtils.passwordsMatch(password, confirmPassword)
    val termsValid = agreeToTerms

    onFullNameError(nameError)
    onPhoneError(phoneError)
    onEmailError(emailError)
    onPasswordError(passwordError)
    onConfirmPasswordError(confirmError)
    onTermsError(if (termsValid) null else "Vui lòng đồng ý với điều khoản sử dụng.")

    if (nameValid && phoneValid && emailValid && passwordValid && confirmValid && termsValid) {
        onSubmit(fullName.trim(), phone.trim(), email.trim(), password)
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    NhathuocTheme {
        RegisterScreen()
    }
}
