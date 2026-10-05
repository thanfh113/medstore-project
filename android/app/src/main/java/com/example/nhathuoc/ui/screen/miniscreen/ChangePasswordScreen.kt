package com.example.nhathuoc.ui.screen.miniscreen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(onBack: () -> Unit) {
    val viewModel: AuthViewModel = hiltViewModel()
    val state by viewModel.changePasswordState.collectAsState()
    val context = LocalContext.current

    var currentPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var clientError by remember { mutableStateOf<String?>(null) }

    // Navigate back after success
    LaunchedEffect(state) {
        if (state is UiState.Success) {
            Toast.makeText(context, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
            viewModel.clearChangePasswordState()
            onBack()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearChangePasswordState() }
    }

    fun validate(): Boolean {
        clientError = when {
            currentPwd.isBlank()               -> "Vui lòng nhập mật khẩu hiện tại"
            newPwd.length < 6                  -> "Mật khẩu mới phải có ít nhất 6 ký tự"
            newPwd != confirmPwd               -> "Xác nhận mật khẩu không khớp"
            newPwd == currentPwd               -> "Mật khẩu mới không được trùng mật khẩu hiện tại"
            else                               -> null
        }
        return clientError == null
    }

    Scaffold(
        topBar = {
            com.example.nhathuoc.ui.component.GreenAppTopBar(
                title = "Đổi mật khẩu",
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Info card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Sau khi đổi mật khẩu, bạn sẽ cần đăng nhập lại trên tất cả thiết bị.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 18.sp
                    )
                }
            }

            // Current password field
            OutlinedTextField(
                value = currentPwd,
                onValueChange = { currentPwd = it; clientError = null },
                label = { Text("Mật khẩu hiện tại") },
                visualTransformation = if (showCurrent) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showCurrent = !showCurrent }) {
                        Icon(
                            if (showCurrent) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            HorizontalDivider()

            // New password field
            OutlinedTextField(
                value = newPwd,
                onValueChange = { newPwd = it; clientError = null },
                label = { Text("Mật khẩu mới") },
                visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showNew = !showNew }) {
                        Icon(
                            if (showNew) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                supportingText = { Text("Ít nhất 6 ký tự", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            // Confirm password field
            val pwdMatch = confirmPwd.isNotEmpty() && newPwd == confirmPwd
            OutlinedTextField(
                value = confirmPwd,
                onValueChange = { confirmPwd = it; clientError = null },
                label = { Text("Xác nhận mật khẩu mới") },
                visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                isError = confirmPwd.isNotEmpty() && !pwdMatch,
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (confirmPwd.isNotEmpty()) {
                            Icon(
                                if (pwdMatch) Icons.Filled.CheckCircle else Icons.Outlined.Cancel,
                                contentDescription = null,
                                tint = if (pwdMatch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                        }
                        IconButton(onClick = { showConfirm = !showConfirm }) {
                            Icon(
                                if (showConfirm) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                supportingText = {
                    if (confirmPwd.isNotEmpty() && !pwdMatch)
                        Text("Mật khẩu không khớp", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            // Error banner (client or server)
            val errorMsg = clientError
                ?: if (state is UiState.Error) (state as UiState.Error).message else null
            if (!errorMsg.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(errorMsg, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = { if (validate()) viewModel.changePassword(currentPwd, newPwd) },
                enabled = state !is UiState.Loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (state is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Đổi mật khẩu", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
