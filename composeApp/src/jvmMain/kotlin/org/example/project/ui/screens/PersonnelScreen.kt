package org.example.project.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.data.repositories.PersonnelUserDto
import org.example.project.presentation.viewmodels.PersonnelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonnelScreen(viewModel: PersonnelViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var resetTarget by remember { mutableStateOf<PersonnelUserDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    if (showCreateDialog) {
        CreatePersonnelDialog(
            creating = uiState.creating,
            onDismiss = { showCreateDialog = false },
            onCreate = { fullName, phone, email, password, role ->
                viewModel.createUser(fullName, phone, email, password, role)
                showCreateDialog = false
            }
        )
    }

    resetTarget?.let { user ->
        ResetPasswordDialog(
            user = user,
            processing = uiState.processingUserId == user.id,
            onDismiss = { resetTarget = null },
            onConfirm = { newPassword ->
                viewModel.resetPassword(user.id, newPassword)
                resetTarget = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý nhân sự", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = { showCreateDialog = true }) {
                        Text("Thêm tài khoản")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }
            uiState.successMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.users, key = { it.id }) { user ->
                        PersonnelRow(
                            user = user,
                            processing = uiState.processingUserId == user.id,
                            onToggleLock = { viewModel.toggleLock(user.id) },
                            onResetPassword = { resetTarget = user }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonnelRow(
    user: PersonnelUserDto,
    processing: Boolean,
    onToggleLock: () -> Unit,
    onResetPassword: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(user.fullName ?: user.phone, fontWeight = FontWeight.SemiBold)
            Text("Role: ${user.role} | ${if (user.isActive) "Active" else "Locked"}")
            Text("Phone: ${user.phone}")
            user.email?.let { Text("Email: $it") }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onToggleLock, enabled = !processing) {
                    Text(if (user.isActive) "Khóa" else "Mở khóa")
                }
                Button(onClick = onResetPassword, enabled = !processing) {
                    Text("Đặt lại MK")
                }
            }
        }
    }
}

@Composable
private fun CreatePersonnelDialog(
    creating: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("EMPLOYEE") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tạo tài khoản nhân sự") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Họ tên") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Điện thoại") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Mật khẩu") })
                OutlinedTextField(value = role, onValueChange = { role = it.uppercase() }, label = { Text("Vai trò (ADMIN/EMPLOYEE)") })
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(fullName, phone, email, password, role) },
                enabled = !creating && phone.isNotBlank() && password.length >= 6
            ) {
                Text("Tạo")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !creating) { Text("Hủy") } }
    )
}

@Composable
private fun ResetPasswordDialog(
    user: PersonnelUserDto,
    processing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đặt lại mật khẩu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tài khoản: ${user.fullName ?: user.phone}")
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Mật khẩu mới") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(newPassword) }, enabled = !processing && newPassword.length >= 6) {
                Text("Xác nhận")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !processing) { Text("Hủy") } }
    )
}

