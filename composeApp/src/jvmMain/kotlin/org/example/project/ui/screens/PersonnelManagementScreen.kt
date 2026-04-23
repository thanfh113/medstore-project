package org.example.project.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.data.repositories.PersonnelEmployeeProfileDto
import org.example.project.data.repositories.PersonnelEmployeeProfileRequest
import org.example.project.data.repositories.PersonnelUserDto
import org.example.project.presentation.viewmodels.PersonnelViewModel
import org.example.project.utils.openFileChooser
import java.awt.Desktop
import java.io.File
import java.net.URI

private enum class PersonnelGroup(val displayName: String) {
    INTERNAL("Nhân viên & admin"),
    USER("Tài khoản khách hàng")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonnelManagementScreen(viewModel: PersonnelViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedGroup by remember { mutableStateOf(PersonnelGroup.INTERNAL) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var resetTarget by remember { mutableStateOf<PersonnelUserDto?>(null) }
    var profileTarget by remember { mutableStateOf<PersonnelUserDto?>(null) }
    var deleteTarget by remember { mutableStateOf<PersonnelUserDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    if (showCreateDialog) {
        CreatePersonnelDialog(
            creating = uiState.creating,
            initialRole = if (selectedGroup == PersonnelGroup.USER) "USER" else "EMPLOYEE",
            onDismiss = { showCreateDialog = false },
            onCreate = { fullName, phone, email, password, role, profile, qualificationDocumentFile ->
                viewModel.createUser(fullName, phone, email, password, role, profile, qualificationDocumentFile)
                showCreateDialog = false
            }
        )
    }

    profileTarget?.let { user ->
        EmployeeProfileDialog(
            user = user,
            processing = uiState.processingUserId == user.id,
            onDismiss = { profileTarget = null },
            onSave = { profile, qualificationDocumentFile ->
                viewModel.updateEmployeeProfile(user, profile, qualificationDocumentFile)
                profileTarget = null
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

    deleteTarget?.let { user ->
        DeletePersonnelDialog(
            user = user,
            processing = uiState.processingUserId == user.id,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.deleteUser(user.id)
                deleteTarget = null
            }
        )
    }

    val filteredUsers = uiState.users.filter { user ->
        val matchGroup = when (selectedGroup) {
            PersonnelGroup.INTERNAL -> user.role.uppercase() != "USER"
            PersonnelGroup.USER -> user.role.uppercase() == "USER"
        }
        val matchQuery = searchQuery.isBlank() ||
            (user.fullName?.contains(searchQuery, ignoreCase = true) == true) ||
            user.phone.contains(searchQuery, ignoreCase = true) ||
            (user.email?.contains(searchQuery, ignoreCase = true) == true) ||
            (user.employeeProfile?.qualificationTitle?.contains(searchQuery, ignoreCase = true) == true)
        matchGroup && matchQuery
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý tài khoản", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = { showCreateDialog = true }) {
                        Text(if (selectedGroup == PersonnelGroup.USER) "Thêm tài khoản user" else "Thêm tài khoản nhân viên")
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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PersonnelGroup.entries.forEach { group ->
                    FilterChip(
                        selected = selectedGroup == group,
                        onClick = { selectedGroup = group },
                        label = { Text(group.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = {
                    Text(
                        if (selectedGroup == PersonnelGroup.USER) {
                            "Tìm khách hàng theo tên, SĐT, email"
                        } else {
                            "Tìm nhân viên theo tên, SĐT, email, bằng cấp"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (selectedGroup) {
                    PersonnelGroup.INTERNAL -> "Nhân viên dùng desktop và được coi là người có chuyên môn. Admin cần bổ sung, xác minh ảnh bằng cấp/chứng chỉ cho EMPLOYEE."
                    PersonnelGroup.USER -> "Tài khoản khách hàng dùng Android: hỗ trợ khóa/mở khóa và đặt lại mật khẩu khi cần."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredUsers, key = { it.id }) { user ->
                        PersonnelUserCard(
                            user = user,
                            processing = uiState.processingUserId == user.id,
                            onEditProfile = { profileTarget = user },
                            onToggleLock = { viewModel.toggleLock(user.id) },
                            onResetPassword = { resetTarget = user },
                            onDelete = { deleteTarget = user }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonnelUserCard(
    user: PersonnelUserDto,
    processing: Boolean,
    onEditProfile: () -> Unit,
    onToggleLock: () -> Unit,
    onResetPassword: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                RoleChip(user.role)
                StatusChip(user.isActive)
                if (user.role.uppercase() == "EMPLOYEE") {
                    ProfileStatusChip(user.employeeProfile?.qualificationVerified == true)
                }
            }

            Text(user.fullName ?: user.phone, fontWeight = FontWeight.SemiBold)
            Text("SĐT: ${user.phone}")
            user.email?.let { Text("Email: $it") }
            Text("Tạo lúc: ${user.createdAt.replace('T', ' ').take(19)}", color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (user.role.uppercase() == "EMPLOYEE") {
                EmployeeProfileSummary(user.employeeProfile)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (user.role.uppercase() == "EMPLOYEE") {
                    Button(onClick = onEditProfile, enabled = !processing) {
                        Text("Sửa hồ sơ chuyên môn")
                    }
                }
                Button(onClick = onToggleLock, enabled = !processing) {
                    Text(if (user.isActive) "Khóa tài khoản" else "Mở khóa")
                }
                Button(
                    onClick = onDelete,
                    enabled = !processing,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Xóa tài khoản")
                }
                Button(onClick = onResetPassword, enabled = !processing) {
                    Text("Đặt lại mật khẩu")
                }
            }
        }
    }
}

@Composable
private fun EmployeeProfileSummary(profile: PersonnelEmployeeProfileDto?) {
    Surface(
        color = Color(0xFFF1F8E9),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Hồ sơ chuyên môn", fontWeight = FontWeight.SemiBold)
            Text(profile?.qualificationTitle ?: "Chưa cập nhật bằng cấp/chứng chỉ")
            profile?.qualificationSpecialty?.let { Text("Chuyên môn: $it") }
            profile?.qualificationInstitution?.let { Text("Đơn vị cấp: $it") }
            profile?.qualificationDocumentUrl?.let { url ->
                Text("Minh chứng: Đã có file", color = MaterialTheme.colorScheme.primary)
                TextButton(onClick = { openExternalUrl(url) }) {
                    Text("Mở minh chứng")
                }
            }
            profile?.qualificationNote?.let { Text("Ghi chú: $it") }
        }
    }
}

@Composable
private fun RoleChip(role: String) {
    val color = when (role.uppercase()) {
        "ADMIN" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        "EMPLOYEE" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        else -> Color(0xFFFFF8E1) to Color(0xFFEF6C00)
    }
    Surface(color = color.first, shape = MaterialTheme.shapes.small) {
        Text(
            text = role.uppercase(),
            color = color.second,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StatusChip(isActive: Boolean) {
    val color = if (isActive) Color(0xFFE8F5E9) to Color(0xFF2E7D32) else Color(0xFFFFEBEE) to Color(0xFFD32F2F)
    Surface(color = color.first, shape = MaterialTheme.shapes.small) {
        Text(
            text = if (isActive) "Đang hoạt động" else "Đã khóa",
            color = color.second,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ProfileStatusChip(isVerified: Boolean) {
    val color = if (isVerified) Color(0xFFE0F2F1) to Color(0xFF00695C) else Color(0xFFFFF3E0) to Color(0xFFE65100)
    Surface(color = color.first, shape = MaterialTheme.shapes.small) {
        Text(
            text = if (isVerified) "Đã xác minh chuyên môn" else "Chờ xác minh chuyên môn",
            color = color.second,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun CreatePersonnelDialog(
    creating: Boolean,
    initialRole: String,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String, PersonnelEmployeeProfileRequest?, File?) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember(initialRole) { mutableStateOf(initialRole) }
    var qualificationTitle by remember { mutableStateOf("") }
    var qualificationSpecialty by remember { mutableStateOf("") }
    var qualificationInstitution by remember { mutableStateOf("") }
    var qualificationDocumentFile by remember { mutableStateOf<File?>(null) }
    var qualificationVerified by remember { mutableStateOf(false) }
    var qualificationNote by remember { mutableStateOf("") }

    val normalizedRole = role.trim().uppercase()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tạo tài khoản") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Họ tên") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Điện thoại") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Mật khẩu") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = role, onValueChange = { role = it.uppercase() }, label = { Text("Vai trò (ADMIN/EMPLOYEE/USER)") }, modifier = Modifier.fillMaxWidth())
                }
                if (normalizedRole == "EMPLOYEE") {
                    item {
                        EmployeeProfileFields(
                            qualificationTitle = qualificationTitle,
                            onQualificationTitleChange = { qualificationTitle = it },
                            qualificationSpecialty = qualificationSpecialty,
                            onQualificationSpecialtyChange = { qualificationSpecialty = it },
                            qualificationInstitution = qualificationInstitution,
                            onQualificationInstitutionChange = { qualificationInstitution = it },
                            currentQualificationDocumentUrl = null,
                            qualificationDocumentFile = qualificationDocumentFile,
                            onChooseQualificationDocument = {
                                pickQualificationDocumentFile()?.let { qualificationDocumentFile = it }
                            },
                            onClearQualificationDocument = {
                                qualificationDocumentFile = null
                            },
                            qualificationVerified = qualificationVerified,
                            onQualificationVerifiedChange = { qualificationVerified = it },
                            qualificationNote = qualificationNote,
                            onQualificationNoteChange = { qualificationNote = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val profile = if (normalizedRole == "EMPLOYEE") {
                        PersonnelEmployeeProfileRequest(
                            qualificationTitle = qualificationTitle.ifBlank { null },
                            qualificationSpecialty = qualificationSpecialty.ifBlank { null },
                            qualificationInstitution = qualificationInstitution.ifBlank { null },
                            qualificationDocumentUrl = null,
                            qualificationDocumentPublicId = null,
                            qualificationVerified = qualificationVerified,
                            qualificationNote = qualificationNote.ifBlank { null }
                        )
                    } else {
                        null
                    }
                    onCreate(
                        fullName,
                        phone,
                        email,
                        password,
                        normalizedRole,
                        profile,
                        if (normalizedRole == "EMPLOYEE") qualificationDocumentFile else null
                    )
                },
                enabled = !creating && phone.isNotBlank() && password.length >= 6
            ) {
                Text("Tạo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !creating) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun EmployeeProfileDialog(
    user: PersonnelUserDto,
    processing: Boolean,
    onDismiss: () -> Unit,
    onSave: (PersonnelEmployeeProfileRequest, File?) -> Unit
) {
    val profile = user.employeeProfile
    var qualificationTitle by remember(profile) { mutableStateOf(profile?.qualificationTitle.orEmpty()) }
    var qualificationSpecialty by remember(profile) { mutableStateOf(profile?.qualificationSpecialty.orEmpty()) }
    var qualificationInstitution by remember(profile) { mutableStateOf(profile?.qualificationInstitution.orEmpty()) }
    var qualificationDocumentUrl by remember(profile) { mutableStateOf(profile?.qualificationDocumentUrl.orEmpty()) }
    var qualificationDocumentPublicId by remember(profile) { mutableStateOf(profile?.qualificationDocumentPublicId.orEmpty()) }
    var qualificationDocumentFile by remember(profile) { mutableStateOf<File?>(null) }
    var qualificationVerified by remember(profile) { mutableStateOf(profile?.qualificationVerified ?: false) }
    var qualificationNote by remember(profile) { mutableStateOf(profile?.qualificationNote.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hồ sơ chuyên môn") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("Nhân viên: ${user.fullName ?: user.phone}", fontWeight = FontWeight.SemiBold)
                }
                item {
                    EmployeeProfileFields(
                        qualificationTitle = qualificationTitle,
                        onQualificationTitleChange = { qualificationTitle = it },
                        qualificationSpecialty = qualificationSpecialty,
                        onQualificationSpecialtyChange = { qualificationSpecialty = it },
                        qualificationInstitution = qualificationInstitution,
                        onQualificationInstitutionChange = { qualificationInstitution = it },
                        currentQualificationDocumentUrl = qualificationDocumentUrl.ifBlank { null },
                        qualificationDocumentFile = qualificationDocumentFile,
                        onChooseQualificationDocument = {
                            pickQualificationDocumentFile()?.let { qualificationDocumentFile = it }
                        },
                        onClearQualificationDocument = {
                            qualificationDocumentFile = null
                        },
                        qualificationVerified = qualificationVerified,
                        onQualificationVerifiedChange = { qualificationVerified = it },
                        qualificationNote = qualificationNote,
                        onQualificationNoteChange = { qualificationNote = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        PersonnelEmployeeProfileRequest(
                            qualificationTitle = qualificationTitle.ifBlank { null },
                            qualificationSpecialty = qualificationSpecialty.ifBlank { null },
                            qualificationInstitution = qualificationInstitution.ifBlank { null },
                            qualificationDocumentUrl = qualificationDocumentUrl.ifBlank { null },
                            qualificationDocumentPublicId = qualificationDocumentPublicId.ifBlank { null },
                            qualificationVerified = qualificationVerified,
                            qualificationNote = qualificationNote.ifBlank { null }
                        ),
                        qualificationDocumentFile
                    )
                },
                enabled = !processing
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !processing) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun EmployeeProfileFields(
    qualificationTitle: String,
    onQualificationTitleChange: (String) -> Unit,
    qualificationSpecialty: String,
    onQualificationSpecialtyChange: (String) -> Unit,
    qualificationInstitution: String,
    onQualificationInstitutionChange: (String) -> Unit,
    currentQualificationDocumentUrl: String?,
    qualificationDocumentFile: File?,
    onChooseQualificationDocument: () -> Unit,
    onClearQualificationDocument: () -> Unit,
    qualificationVerified: Boolean,
    onQualificationVerifiedChange: (Boolean) -> Unit,
    qualificationNote: String,
    onQualificationNoteChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Thông tin chuyên môn", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = qualificationTitle,
            onValueChange = onQualificationTitleChange,
            label = { Text("Bằng cấp / chứng chỉ") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = qualificationSpecialty,
            onValueChange = onQualificationSpecialtyChange,
            label = { Text("Chuyên môn") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = qualificationInstitution,
            onValueChange = onQualificationInstitutionChange,
            label = { Text("Đơn vị cấp") },
            modifier = Modifier.fillMaxWidth()
        )
        Surface(
            color = Color(0xFFF7FBF4),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("File minh chứng chuyên môn", fontWeight = FontWeight.SemiBold)
                Text(
                    text = qualificationDocumentFile?.let {
                        "Đã chọn: ${it.name}. Khi bấm Lưu/Tạo, file này sẽ upload và tự cập nhật URL/public id."
                    } ?: currentQualificationDocumentUrl?.let {
                        "Đang dùng file minh chứng hiện tại. Chọn ảnh/PDF mới nếu cần thay thế."
                    } ?: "Chưa chọn file minh chứng. Chọn ảnh/PDF để upload khi bấm Lưu/Tạo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    currentQualificationDocumentUrl?.let { url ->
                        TextButton(onClick = { openExternalUrl(url) }) {
                            Text("Mở file hiện tại")
                        }
                    }
                    Button(onClick = onChooseQualificationDocument) {
                        Text(if (qualificationDocumentFile == null) "Chọn file" else "Đổi file")
                    }
                    if (qualificationDocumentFile != null) {
                        TextButton(onClick = onClearQualificationDocument) {
                            Text("Bỏ chọn")
                        }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = qualificationVerified, onCheckedChange = onQualificationVerifiedChange)
            Text("Đã xác minh hồ sơ chuyên môn")
        }
        OutlinedTextField(
            value = qualificationNote,
            onValueChange = onQualificationNoteChange,
            label = { Text("Ghi chú") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun pickQualificationDocumentFile(): File? {
    return openFileChooser(
        title = "Chọn file minh chứng chuyên môn",
        allowedExtensions = listOf(".jpg", ".jpeg", ".png", ".pdf", ".heic"),
        allowMultiple = false
    ).firstOrNull()
}

private fun openExternalUrl(url: String) {
    runCatching {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI(url))
        }
    }
}

@Composable
private fun DeletePersonnelDialog(
    user: PersonnelUserDto,
    processing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xóa tài khoản") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tài khoản: ${user.fullName ?: user.phone}", fontWeight = FontWeight.SemiBold)
                Text(
                    "Thao tác này sẽ xóa mềm tài khoản khỏi danh sách quản trị, khóa đăng nhập và giữ lịch sử đơn hàng/chat để không mất dữ liệu đối soát."
                )
                if (user.role.uppercase() == "ADMIN") {
                    Text("Backend sẽ chặn nếu đây là admin cuối cùng hoặc là tài khoản đang đăng nhập.")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !processing,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Xóa")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !processing) {
                Text("Hủy")
            }
        }
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
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !processing) {
                Text("Hủy")
            }
        }
    )
}
