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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null
) {
    val viewModel: AuthViewModel = hiltViewModel()
    val loginState by viewModel.loginState.collectAsState()
    val focusManager = LocalFocusManager.current

    var credential by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var credentialError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var hasNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.clearLoginState()
        hasNavigated = false
    }

    LaunchedEffect(loginState) {
        if (loginState is UiState.Success && !hasNavigated) {
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
            LoginHeader(
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Chào mừng trở lại",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = GreenTop
                    )
                    Text(
                        text = "Đăng nhập để mua sắm, theo dõi đơn hàng và nhận tư vấn vật tư y tế.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF5E6B5F),
                        lineHeight = 20.sp
                    )

                    OutlinedTextField(
                        value = credential,
                        onValueChange = {
                            credential = it
                            credentialError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email hoặc số điện thoại") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (credential.contains("@")) Icons.Outlined.Email else Icons.Outlined.Phone,
                                contentDescription = null,
                                tint = GreenTop
                            )
                        },
                        isError = credentialError != null,
                        supportingText = credentialError?.let { { Text(it) } },
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
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = GreenTop
                            )
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
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                validateAndLogin(
                                    credential = credential,
                                    password = password,
                                    onCredentialError = { credentialError = it },
                                    onPasswordError = { passwordError = it },
                                    onSubmit = { loginCredential, loginPassword ->
                                        viewModel.loginWithEmail(loginCredential, loginPassword)
                                    }
                                )
                            }
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it }
                        )
                        Text(
                            text = "Ghi nhớ đăng nhập",
                            color = Color(0xFF315F35),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Quên mật khẩu?",
                            color = GreenTop,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (loginState is UiState.Error) {
                        LoginErrorCard((loginState as UiState.Error).message)
                    }

                    Button(
                        onClick = {
                            validateAndLogin(
                                credential = credential,
                                password = password,
                                onCredentialError = { credentialError = it },
                                onPasswordError = { passwordError = it },
                                onSubmit = { loginCredential, loginPassword ->
                                    viewModel.loginWithEmail(loginCredential, loginPassword)
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        enabled = loginState !is UiState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
                    ) {
                        if (loginState is UiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Đăng nhập",
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
                        Text("Chưa có tài khoản? ", color = Color(0xFF5E6B5F))
                        Text(
                            text = "Đăng ký ngay",
                            color = GreenTop,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                navController?.navigate("RegisterScreen")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
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
                text = "Vật tư y tế",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Đăng nhập để tiếp tục",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoginErrorCard(message: String) {
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

private fun validateAndLogin(
    credential: String,
    password: String,
    onCredentialError: (String?) -> Unit,
    onPasswordError: (String?) -> Unit,
    onSubmit: (String, String) -> Unit
) {
    val trimmedCredential = credential.trim()
    val (credentialValid, credentialError) = if (trimmedCredential.contains("@")) {
        ValidationUtils.isValidEmail(trimmedCredential)
    } else {
        ValidationUtils.isValidVietnamesePhone(trimmedCredential)
    }
    val (passwordValid, passwordError) = ValidationUtils.isValidPassword(password)

    onCredentialError(credentialError)
    onPasswordError(passwordError)

    if (credentialValid && passwordValid) {
        onSubmit(trimmedCredential, password)
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    NhathuocTheme {
        LoginScreen()
    }
}
