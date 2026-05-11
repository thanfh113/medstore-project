package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nhathuoc.data.local.SessionManager
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.ui.screen.AvatarInitials
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val authViewModel: AuthViewModel = hiltViewModel()

    val storedName  = sessionManager.userFullName.collectAsState(initial = "")
    val storedPhone = sessionManager.userPhone.collectAsState(initial = "")
    val storedEmail = sessionManager.userEmail.collectAsState(initial = "")
    val userState   by authViewModel.userState.collectAsState()

    var fullName by remember(storedName.value) { mutableStateOf(storedName.value ?: "") }
    var email    by remember(storedEmail.value) { mutableStateOf(storedEmail.value ?: "") }
    val phone = storedPhone.value ?: ""

    var nameError  by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    val isLoading = userState is UiState.Loading
    val saveError = (userState as? UiState.Error)?.message

    LaunchedEffect(Unit) {
        authViewModel.clearUserState()
    }

    LaunchedEffect(userState) {
        if (userState is UiState.Success) {
            authViewModel.clearUserState()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thông tin cá nhân", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBackIosNew, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                AvatarInitials(name = fullName.ifBlank { "U" }, size = 80)
            }

            Spacer(Modifier.height(24.dp))

            Surface(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProfileField(
                        label = "Họ và tên",
                        value = fullName,
                        onValueChange = { fullName = it; nameError = null },
                        placeholder = "Nhập họ và tên",
                        error = nameError,
                        enabled = !isLoading
                    )
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    ProfileField(
                        label = "Số điện thoại",
                        value = phone,
                        onValueChange = {},
                        placeholder = "",
                        enabled = false,
                        trailingText = "Không thể thay đổi"
                    )
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    ProfileField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it; emailError = null },
                        placeholder = "Nhập email",
                        keyboardType = KeyboardType.Email,
                        error = emailError,
                        enabled = !isLoading
                    )
                }
            }

            if (saveError != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    saveError,
                    color = Color(0xFFE53935),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    var valid = true
                    if (!authViewModel.isValidFullName(fullName)) {
                        nameError = "Họ tên phải có ít nhất 2 ký tự"
                        valid = false
                    }
                    if (email.isNotBlank() && !authViewModel.isValidEmail(email)) {
                        emailError = "Email không hợp lệ"
                        valid = false
                    }
                    if (valid) authViewModel.updateProfile(fullName.trim(), email.trim())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Lưu thông tin", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null,
    trailingText: String? = null
) {
    Column {
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            placeholder = { Text(placeholder, color = Color.LightGray) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            isError = error != null,
            trailingIcon = if (trailingText != null) ({
                Text(trailingText, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(end = 4.dp))
            }) else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenTop,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                disabledBorderColor = Color(0xFFF0F0F0),
                disabledTextColor = Color(0xFF888888)
            ),
            shape = RoundedCornerShape(8.dp)
        )
        if (error != null) {
            Text(error, fontSize = 11.sp, color = Color(0xFFE53935), modifier = Modifier.padding(top = 2.dp))
        }
    }
}
