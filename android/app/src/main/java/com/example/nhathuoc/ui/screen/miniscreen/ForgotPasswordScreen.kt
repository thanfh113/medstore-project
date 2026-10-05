package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.ui.component.GreenAppTopBar
import com.example.nhathuoc.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    lockedEmail: String? = null   // nếu có → pre-fill và khóa trường email
) {
    val viewModel: AuthViewModel = hiltViewModel()
    val forgotState by viewModel.forgotPasswordState.collectAsState()
    val resetState  by viewModel.resetPasswordState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var step by remember { mutableStateOf(1) }  // 1 = nhập email, 2 = nhập OTP + mk mới
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var otpError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.clearForgotPasswordState()
        viewModel.clearResetPasswordState()
    }

    // Sau khi gửi OTP thành công → chuyển sang bước 2
    LaunchedEffect(forgotState) {
        if (forgotState is UiState.Success) step = 2
    }

    // Sau khi reset thành công → hiện thông báo rồi điều hướng
    LaunchedEffect(resetState) {
        if (resetState is UiState.Success) {
            snackbarHostState.showSnackbar(
                message = "Đặt lại mật khẩu thành công!",
                duration = SnackbarDuration.Short
            )
            onSuccess()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearForgotPasswordState()
            viewModel.clearResetPasswordState()
        }
    }

    Scaffold(
        topBar = {
            GreenAppTopBar(
                title = if (step == 1) "Quên mật khẩu" else "Đặt lại mật khẩu",
                onBack = {
                    if (step == 2) {
                        step = 1
                        viewModel.clearForgotPasswordState()
                        viewModel.clearResetPasswordState()
                    } else {
                        onBack()
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                (slideInHorizontally { it } + fadeIn()) togetherWith
                (slideOutHorizontally { -it } + fadeOut())
            },
            label = "forgot_step"
        ) { currentStep ->
            if (currentStep == 1) {
                StepOneContent(
                    modifier = Modifier.padding(padding),
                    email = email,
                    onEmailChange = { email = it; emailError = null },
                    emailError = emailError,
                    isEmailLocked = lockedEmail != null,
                    lockedEmailHint = lockedEmail?.let { maskEmail(it) },
                    isLoading = forgotState is UiState.Loading,
                    errorMessage = (forgotState as? UiState.Error)?.message,
                    onSubmit = {
                        val trimmed = email.trim()
                        if (trimmed.isBlank()) {
                            emailError = "Vui lòng nhập email"
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
                            emailError = "Email không hợp lệ"
                        } else if (lockedEmail != null && trimmed != lockedEmail) {
                            emailError = "Email không khớp với tài khoản của bạn"
                        } else {
                            viewModel.forgotPassword(trimmed)
                        }
                    }
                )
            } else {
                StepTwoContent(
                    modifier = Modifier.padding(padding),
                    email = email,
                    otp = otp,
                    onOtpChange = { otp = it; otpError = null },
                    otpError = otpError,
                    newPassword = newPassword,
                    onPasswordChange = { newPassword = it; passwordError = null },
                    confirmPassword = confirmPassword,
                    onConfirmChange = { confirmPassword = it; passwordError = null },
                    showPassword = showPassword,
                    onToggleShow = { showPassword = !showPassword },
                    passwordError = passwordError,
                    isLoading = resetState is UiState.Loading,
                    errorMessage = (resetState as? UiState.Error)?.message,
                    onResend = {
                        viewModel.clearForgotPasswordState()
                        viewModel.forgotPassword(email.trim())
                    },
                    onSubmit = {
                        var valid = true
                        if (otp.trim().length != 6) { otpError = "Mã OTP gồm 6 chữ số"; valid = false }
                        if (newPassword.length < 6) { passwordError = "Mật khẩu phải có ít nhất 6 ký tự"; valid = false }
                        if (newPassword != confirmPassword) { passwordError = "Mật khẩu xác nhận không khớp"; valid = false }
                        if (valid) viewModel.resetPassword(email.trim(), otp.trim(), newPassword)
                    }
                )
            }
        }
    }
}

@Composable
private fun StepOneContent(
    modifier: Modifier = Modifier,
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    isEmailLocked: Boolean = false,
    lockedEmailHint: String? = null,
    isLoading: Boolean,
    errorMessage: String?,
    onSubmit: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        Icon(
            imageVector = Icons.Outlined.Email,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp).align(Alignment.CenterHorizontally)
        )

        Text(
            "Nhập email đăng ký",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            if (isEmailLocked && lockedEmailHint != null)
                "Nhập email đã đăng ký của bạn để xác nhận.\nGợi ý: $lockedEmailHint"
            else
                "Chúng tôi sẽ gửi mã OTP 6 chữ số đến email của bạn để xác nhận.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            leadingIcon = {
                Icon(Icons.Outlined.Email, null, tint = MaterialTheme.colorScheme.primary)
            },
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor  = MaterialTheme.colorScheme.primary,
                cursorColor        = MaterialTheme.colorScheme.primary
            )
        )

        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text("Gửi mã OTP", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun StepTwoContent(
    modifier: Modifier = Modifier,
    email: String,
    otp: String,
    onOtpChange: (String) -> Unit,
    otpError: String?,
    newPassword: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmChange: (String) -> Unit,
    showPassword: Boolean,
    onToggleShow: () -> Unit,
    passwordError: String?,
    isLoading: Boolean,
    errorMessage: String?,
    onResend: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp).align(Alignment.CenterHorizontally)
        )

        Text(
            "Nhập mã OTP",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Mã OTP đã được gửi đến $email. Mã có hiệu lực trong 10 phút.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 6) onOtpChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Mã OTP") },
            isError = otpError != null,
            supportingText = otpError?.let { { Text(it) } },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor  = MaterialTheme.colorScheme.primary,
                cursorColor        = MaterialTheme.colorScheme.primary
            )
        )

        OutlinedTextField(
            value = newPassword,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Mật khẩu mới") },
            leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                IconButton(onClick = onToggleShow) {
                    Icon(
                        if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor  = MaterialTheme.colorScheme.primary,
                cursorColor        = MaterialTheme.colorScheme.primary
            )
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Xác nhận mật khẩu") },
            leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = MaterialTheme.colorScheme.primary) },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor  = MaterialTheme.colorScheme.primary,
                cursorColor        = MaterialTheme.colorScheme.primary
            )
        )

        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text("Đặt lại mật khẩu", style = MaterialTheme.typography.titleMedium)
            }
        }

        TextButton(
            onClick = onResend,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = !isLoading
        ) {
            Text("Gửi lại mã OTP", color = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun maskEmail(email: String): String {
    val atIndex = email.indexOf('@')
    if (atIndex <= 1) return email
    val local = email.substring(0, atIndex)
    val domain = email.substring(atIndex)
    return "${local.first()}${"*".repeat(local.length - 1)}$domain"
}
