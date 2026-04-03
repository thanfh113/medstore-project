package com.example.nhathuoc.ui.screen.miniscreen

import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.data.repository.AuthRepository
import com.example.nhathuoc.data.local.SessionManager
import com.example.nhathuoc.ui.theme.NhathuocTheme
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.BgColor
import com.example.nhathuoc.util.ValidationUtils
import com.example.nhathuoc.viewmodel.AuthViewModel
import com.example.nhathuoc.viewmodel.AuthViewModelFactory

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null
) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            AuthRepository(SessionManager(context))
        )
    )

    var credential by remember { mutableStateOf("") }  // Email or Phone
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var credentialError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var hasNavigatedBack by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val loginState by viewModel.loginState.collectAsState()

    Log.d("LoginScreen", "🔐 LoginScreen rendered, loginState=$loginState, hasNavigatedBack=$hasNavigatedBack")

    // Clear previous state when screen enters
    LaunchedEffect(Unit) {
        Log.d("LoginScreen", "🔄 Clearing login state on entry")
        viewModel.clearLoginState()
        hasNavigatedBack = false
    }

    // Handle successful login - navigate to MainScreen
    LaunchedEffect(loginState) {
        Log.d("LoginScreen", "⚡ LaunchedEffect triggered! loginState=$loginState, hasNavigatedBack=$hasNavigatedBack, navController=$navController")

        // Fix: Check class name instead (avoids generic type erasure)
        val isSuccess = loginState.javaClass.simpleName == "Success"
        val notNavigated = !hasNavigatedBack
        Log.d("LoginScreen", "📊 Condition check: isSuccess=$isSuccess, notNavigated=$notNavigated")

        if (isSuccess && notNavigated) {
            Log.d("LoginScreen", "✅ Login Success detected!")
            hasNavigatedBack = true
            Log.d("LoginScreen", "🏠 About to navigate to MainScreen")
            Log.d("LoginScreen", "🔗 navController is null? ${navController == null}")

            if (navController != null) {
                Log.d("LoginScreen", "🔀 Calling navigate(MainScreen)...")
                navController.navigate("MainScreen") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
                Log.d("LoginScreen", "✅ Navigate call completed")
            } else {
                Log.e("LoginScreen", "❌ navController is NULL!")
            }
        }
    }

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
                    .height(200.dp)
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
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(
                            Icons.Filled.LocalPharmacy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "NHÀ THUỐC HELLO",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Đăng nhập tài khoản",
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
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        "Chào mừng quay trở lại!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "Vui lòng đăng nhập để tiếp tục sử dụng ứng dụng",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Error message display
                    if (loginState is UiState.Error) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            color = Color(0xFFffebee)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFc62828),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    (loginState as UiState.Error).message,
                                    color = Color(0xFFc62828),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Credential field (Email or Phone)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Email hoặc Số điện thoại",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        OutlinedTextField(
                            value = credential,
                            onValueChange = {
                                credential = it
                                credentialError = null // Clear error on edit
                            },
                            placeholder = { Text("user@gmail.com hoặc 0xxxxxxxxx") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = GreenTop
                                )
                            },
                            isError = credentialError != null,
                            supportingText = if (credentialError != null) {
                                { Text(credentialError!!, fontSize = 12.sp) }
                            } else null,
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
                            onValueChange = {
                                password = it
                                passwordError = null // Clear error on edit
                            },
                            placeholder = { Text("Nhập mật khẩu") },
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
                            isError = passwordError != null,
                            supportingText = if (passwordError != null) {
                                { Text(passwordError!!, fontSize = 12.sp) }
                            } else null,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Remember me & Forgot password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberMe = !rememberMe }
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = GreenTop)
                            )
                            Text(
                                "Ghi nhớ đăng nhập",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }

                        Text(
                            "Quên mật khẩu?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = GreenTop,
                            modifier = Modifier.clickable {
                                // TODO: Navigate to forgot password
                            }
                        )
                    }

                    // Login button
                    val isLoading = loginState is UiState.Loading
                    val isFormValid = credential.isNotEmpty() && password.isNotEmpty()

                    Button(
                        onClick = {
                            Log.d("LoginScreen", "🔐 Login button clicked")
                            Log.d("LoginScreen", "📝 Input: credential=$credential, password length=${password.length}")

                            credentialError = null
                            passwordError = null

                            // Auto-detect credential type (email or phone)
                            val isEmail = credential.contains("@")
                            Log.d("LoginScreen", "🔍 Credential type: ${if(isEmail) "Email" else "Phone"}")

                            // Validate credential
                            val credentialValid = if (isEmail) {
                                val (emailValid, emailErr) = ValidationUtils.isValidEmail(credential)
                                credentialError = emailErr
                                Log.d("LoginScreen", "📧 Email validation: valid=$emailValid, error=$emailErr")
                                emailValid
                            } else {
                                val (phoneValid, phoneErr) = ValidationUtils.isValidVietnamesePhone(credential)
                                credentialError = phoneErr
                                Log.d("LoginScreen", "📱 Phone validation: valid=$phoneValid, error=$phoneErr")
                                phoneValid
                            }

                            // Validate password
                            val (pwdValid, pwdErr) = ValidationUtils.isValidPassword(password)
                            passwordError = pwdErr
                            Log.d("LoginScreen", "🔑 Password validation: valid=$pwdValid, error=$pwdErr")

                            if (credentialValid && pwdValid) {
                                Log.d("LoginScreen", "✅ Validation passed! Calling viewModel.login()")
                                // Send email or phone based on credential type
                                if (isEmail) {
                                    viewModel.loginWithEmail(credential, password)
                                } else {
                                    viewModel.login(credential, password)
                                }
                            } else {
                                Log.e("LoginScreen", "❌ Validation failed!")
                            }
                        },
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = isFormValid && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenTop,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Đăng nhập",
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

                    // Quick login buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Google login
                        OutlinedButton(
                            onClick = { /* TODO: Google login */ },
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

                        // Facebook login
                        OutlinedButton(
                            onClick = { /* TODO: Facebook login */ },
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

            // Register link
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Text(
                    "Chưa có tài khoản? ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    "Đăng ký ngay",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenTop,
                    modifier = Modifier.clickable {
                        navController?.navigate("RegisterScreen")
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    NhathuocTheme {
        LoginScreen()
    }
}