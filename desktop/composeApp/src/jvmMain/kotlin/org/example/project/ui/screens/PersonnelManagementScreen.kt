package org.example.project.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.project.data.repositories.PersonnelEmployeeProfileDto
import org.example.project.data.repositories.PersonnelEmployeeProfileRequest
import org.example.project.data.repositories.PersonnelUserDto
import org.example.project.presentation.viewmodels.PersonnelViewModel
import org.example.project.util.formatVnDateTime
import org.example.project.utils.openFileChooser
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    var selectedUser by remember { mutableStateOf<PersonnelUserDto?>(null) }
    var resetTarget by remember { mutableStateOf<PersonnelUserDto?>(null) }
    var profileTarget by remember { mutableStateOf<PersonnelUserDto?>(null) }
    var deleteTarget by remember { mutableStateOf<PersonnelUserDto?>(null) }

    LaunchedEffect(Unit) { viewModel.loadUsers() }

    // Keep selectedUser in sync after list refresh
    LaunchedEffect(uiState.users) {
        selectedUser = selectedUser?.let { cur -> uiState.users.find { it.id == cur.id } }
    }

    if (showCreateDialog) {
        CreatePersonnelDialog(
            creating = uiState.creating,
            initialRole = if (selectedGroup == PersonnelGroup.USER) "USER" else "EMPLOYEE",
            onDismiss = { showCreateDialog = false },
            onCreate = { fullName, phone, email, password, role, profile, qualDocFile ->
                viewModel.createUser(fullName, phone, email, password, role, profile, qualDocFile)
                showCreateDialog = false
            }
        )
    }

    profileTarget?.let { user ->
        LaunchedEffect(uiState.lastProfileUpdatedUserId) {
            if (uiState.lastProfileUpdatedUserId == user.id) {
                profileTarget = null
                viewModel.clearMessages()
            }
        }
        EmployeeProfileDialog(
            user = user,
            processing = uiState.processingUserId == user.id,
            onDismiss = { profileTarget = null },
            onSave = { profile, qualDocFile -> viewModel.updateEmployeeProfile(user, profile, qualDocFile) }
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
                selectedUser = null
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
                    uiState.successMessage?.let {
                        Text(it, color = Color(0xFF2E7D32), fontSize = 13.sp, modifier = Modifier.padding(end = 12.dp))
                    }
                    uiState.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, modifier = Modifier.padding(end = 12.dp))
                    }
                    Button(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (selectedGroup == PersonnelGroup.USER) "Thêm user" else "Thêm nhân viên")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = PersonnelGroup.entries.indexOf(selectedGroup),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                PersonnelGroup.entries.forEach { group ->
                    Tab(
                        selected = selectedGroup == group,
                        onClick = {
                            if (selectedGroup != group) {
                                selectedGroup = group
                                selectedUser = null
                                searchQuery = ""
                            }
                        },
                        text = { Text(group.displayName) }
                    )
                }
            }

            Row(modifier = Modifier.fillMaxSize()) {
                // ─── LEFT PANEL: compact user list ───────────────────────────
                Column(
                    modifier = Modifier
                        .width(310.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text("Tìm kiếm...", fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null,
                                modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Text(
                        text = "${filteredUsers.size} " +
                            if (selectedGroup == PersonnelGroup.USER) "khách hàng" else "nhân viên/admin",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    when {
                        uiState.isLoading -> Box(
                            Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                        filteredUsers.isEmpty() -> Box(
                            Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Không có kết quả", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp)
                        }
                        else -> LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(filteredUsers, key = { it.id }) { user ->
                                UserListRow(
                                    user = user,
                                    isSelected = selectedUser?.id == user.id,
                                    onClick = { selectedUser = user }
                                )
                            }
                        }
                    }
                }

                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // ─── RIGHT PANEL: detail ─────────────────────────────────────
                val current = selectedUser
                if (current == null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.ManageAccounts, contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Chọn tài khoản để xem chi tiết",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        )
                    }
                } else {
                    UserDetailPanel(
                        user = current,
                        processing = uiState.processingUserId == current.id,
                        onEditProfile = { profileTarget = current },
                        onToggleLock = { viewModel.toggleLock(current.id) },
                        onResetPassword = { resetTarget = current },
                        onDelete = { deleteTarget = current }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Compact list row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UserListRow(
    user: PersonnelUserDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarCircle(name = user.fullName ?: user.phone, role = user.role, size = 36.dp)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.fullName ?: user.phone,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = user.email ?: user.phone,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (user.isActive) Color(0xFF4CAF50) else Color(0xFFEF5350))
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Detail panel (right side)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UserDetailPanel(
    user: PersonnelUserDto,
    processing: Boolean,
    onEditProfile: () -> Unit,
    onToggleLock: () -> Unit,
    onResetPassword: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header: avatar + name + chips
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AvatarCircleWithImage(
                name = user.fullName ?: user.phone,
                role = user.role,
                size = 72.dp,
                avatarUrl = user.avatarUrl
            )
            Text(user.fullName ?: user.phone, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                RoleChip(user.role)
                StatusChip(user.isActive)
                if (!user.isActive && user.failedLoginAttempts >= 5) FailedLoginChip()
                else if (user.isActive && user.failedLoginAttempts in 1..4) FailedAttemptsWarningChip(user.failedLoginAttempts)
                if (user.role.uppercase() == "EMPLOYEE") {
                    ProfileStatusChip(user.employeeProfile?.qualificationVerified == true)
                }
            }
        }

        // Info card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow(Icons.Outlined.Phone, "Số điện thoại", user.phone)
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                InfoRow(Icons.Outlined.Email, "Email", user.email ?: "—")
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                InfoRow(Icons.Outlined.Person, "Giới tính", user.gender ?: "—")
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                InfoRow(Icons.Outlined.Cake, "Ngày sinh", formatDobForDisplay(user.dateOfBirth))
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                InfoRow(Icons.Outlined.CalendarToday, "Ngày tạo", formatVnDateTime(user.createdAt))
                if (user.failedLoginAttempts > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    InfoRow(Icons.Outlined.FilterList, "Sai mật khẩu", "${user.failedLoginAttempts} lần")
                }
            }
        }

        // Employee qualification section
        if (user.role.uppercase() == "EMPLOYEE") {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F8E9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hồ sơ chuyên môn", fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                        TextButton(onClick = onEditProfile, enabled = !processing) {
                            Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Chỉnh sửa", fontSize = 13.sp)
                        }
                    }
                    val profile = user.employeeProfile
                    if (profile != null) {
                        Text(profile.qualificationTitle, fontWeight = FontWeight.Medium, color = Color(0xFF1B5E20))
                        profile.qualificationInstitution?.let {
                            Text("Đơn vị cấp: $it", fontSize = 13.sp, color = Color(0xFF388E3C))
                        }
                        if (profile.qualificationDocumentUrl != null) {
                            TextButton(onClick = { openExternalUrl(profile.qualificationDocumentUrl) }) {
                                Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Xem minh chứng", fontSize = 13.sp)
                            }
                        }
                        profile.qualificationNote?.let {
                            Text("Ghi chú: $it", fontSize = 12.sp, color = Color(0xFF558B2F))
                        }
                    } else {
                        Text("Chưa cập nhật hồ sơ chuyên môn", color = Color(0xFFE65100), fontSize = 13.sp)
                    }
                }
            }
        }

        // Actions card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Tác vụ quản trị",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (processing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Đang xử lý...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val lockLabel = when {
                            !user.isActive && user.failedLoginAttempts >= 5 -> "Mở khóa & xóa cảnh báo"
                            !user.isActive -> "Mở khóa"
                            else -> "Khóa tài khoản"
                        }
                        FilledTonalButton(
                            onClick = onToggleLock,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (user.isActive) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = if (user.isActive) MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                if (user.isActive) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(lockLabel, fontSize = 13.sp)
                        }

                        FilledTonalButton(onClick = onResetPassword) {
                            Icon(Icons.Outlined.VpnKey, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Đặt lại mật khẩu", fontSize = 13.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Xóa tài khoản", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared small components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(110.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun AvatarCircle(name: String, role: String, size: Dp) {
    val initials = name.trim().split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "?" }
    val bgColor = when (role.uppercase()) {
        "ADMIN" -> Color(0xFF1565C0)
        "EMPLOYEE" -> Color(0xFF2E7D32)
        else -> Color(0xFFE65100)
    }
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = (size.value * 0.32f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AvatarCircleWithImage(name: String, role: String, size: Dp, avatarUrl: String?) {
    if (avatarUrl.isNullOrBlank()) {
        AvatarCircle(name, role, size)
        return
    }
    var loadFailed by remember(avatarUrl) { mutableStateOf(false) }
    if (loadFailed) {
        AvatarCircle(name, role, size)
    } else {
        AsyncImage(
            model = avatarUrl,
            contentDescription = "Avatar",
            modifier = Modifier.size(size).clip(CircleShape),
            contentScale = ContentScale.Crop,
            onError = { loadFailed = true }
        )
    }
}

@Composable
private fun RoleChip(role: String) {
    val (bg, fg) = when (role.uppercase()) {
        "ADMIN" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        "EMPLOYEE" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        else -> Color(0xFFFFF8E1) to Color(0xFFEF6C00)
    }
    Surface(color = bg, shape = MaterialTheme.shapes.small) {
        Text(
            text = role.uppercase(),
            color = fg,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun StatusChip(isActive: Boolean) {
    val (bg, fg) = if (isActive) Color(0xFFE8F5E9) to Color(0xFF2E7D32) else Color(0xFFFFEBEE) to Color(0xFFD32F2F)
    Surface(color = bg, shape = MaterialTheme.shapes.small) {
        Text(
            text = if (isActive) "Đang hoạt động" else "Đã khóa",
            color = fg,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun ProfileStatusChip(isVerified: Boolean) {
    val (bg, fg) = if (isVerified) Color(0xFFE0F2F1) to Color(0xFF00695C) else Color(0xFFFFF3E0) to Color(0xFFE65100)
    Surface(color = bg, shape = MaterialTheme.shapes.small) {
        Text(
            text = if (isVerified) "Đã xác minh" else "Chờ xác minh",
            color = fg,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun FailedLoginChip() {
    Surface(color = Color(0xFFFFEBEE), shape = MaterialTheme.shapes.small) {
        Text(
            text = "Khóa do sai MK 5 lần",
            color = Color(0xFFB71C1C),
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun FailedAttemptsWarningChip(count: Int) {
    Surface(color = Color(0xFFFFF3E0), shape = MaterialTheme.shapes.small) {
        Text(
            text = "Sai MK: $count/5",
            color = Color(0xFFE65100),
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

private fun formatDobForDisplay(dob: String?): String {
    if (dob.isNullOrBlank()) return "—"
    return try {
        val parsed = LocalDate.parse(dob, DateTimeFormatter.ISO_LOCAL_DATE)
        parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (_: Exception) { dob }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dialogs (unchanged logic)
// ─────────────────────────────────────────────────────────────────────────────

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
    var qualificationInstitution by remember { mutableStateOf("") }
    var qualificationDocumentFile by remember { mutableStateOf<File?>(null) }
    var qualificationNote by remember { mutableStateOf("") }
    var isPickingQualificationFile by remember { mutableStateOf(false) }
    val pickerScope = rememberCoroutineScope()

    val normalizedRole = role.trim().uppercase()
    val roleOptions = if (initialRole.uppercase() == "USER") listOf("USER") else listOf("EMPLOYEE", "ADMIN")

    AlertDialog(
        onDismissRequest = { if (!isPickingQualificationFile && !creating) onDismiss() },
        title = { Text("Tạo tài khoản") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it },
                        label = { Text("Họ tên *") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = phone, onValueChange = { phone = it },
                        label = { Text("Điện thoại *") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = email, onValueChange = { email = it },
                        label = { Text("Email (tùy chọn)") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = password, onValueChange = { password = it },
                        label = { Text("Mật khẩu *") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Vai trò", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            roleOptions.forEach { option ->
                                FilterChip(
                                    selected = normalizedRole == option,
                                    onClick = { role = option },
                                    label = {
                                        Text(when (option) {
                                            "ADMIN" -> "Admin"
                                            "EMPLOYEE" -> "Nhân viên"
                                            else -> "Khách hàng"
                                        })
                                    }
                                )
                            }
                        }
                    }
                }
                if (normalizedRole == "EMPLOYEE") {
                    item {
                        EmployeeProfileFields(
                            qualificationTitle = qualificationTitle,
                            onQualificationTitleChange = { qualificationTitle = it },
                            qualificationInstitution = qualificationInstitution,
                            onQualificationInstitutionChange = { qualificationInstitution = it },
                            currentQualificationDocumentUrl = null,
                            qualificationDocumentFile = qualificationDocumentFile,
                            onChooseQualificationDocument = {
                                isPickingQualificationFile = true
                                pickerScope.launch {
                                    try {
                                        delay(120)
                                        pickQualificationDocumentFile()?.let { qualificationDocumentFile = it }
                                    } catch (_: Throwable) {
                                    } finally {
                                        delay(300)
                                        isPickingQualificationFile = false
                                    }
                                }
                            },
                            onClearQualificationDocument = { qualificationDocumentFile = null },
                            qualificationVerified = false,
                            onQualificationVerifiedChange = {},
                            qualificationNote = qualificationNote,
                            onQualificationNoteChange = { qualificationNote = it },
                            showVerificationControls = false
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
                            qualificationInstitution = qualificationInstitution.ifBlank { null },
                            qualificationDocumentUrl = null,
                            qualificationDocumentPublicId = null,
                            qualificationVerified = false,
                            qualificationNote = null
                        )
                    } else null
                    onCreate(
                        fullName, phone, email, password, normalizedRole, profile,
                        if (normalizedRole == "EMPLOYEE") qualificationDocumentFile else null
                    )
                },
                enabled = !creating && fullName.isNotBlank() && phone.isNotBlank() && password.length >= 6
            ) { Text("Tạo") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !creating) { Text("Hủy") }
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
    var qualificationInstitution by remember(profile) { mutableStateOf(profile?.qualificationInstitution.orEmpty()) }
    var qualificationDocumentUrl by remember(profile) { mutableStateOf(profile?.qualificationDocumentUrl.orEmpty()) }
    var qualificationDocumentPublicId by remember(profile) { mutableStateOf(profile?.qualificationDocumentPublicId.orEmpty()) }
    var qualificationDocumentFile by remember(profile) { mutableStateOf<File?>(null) }
    var qualificationVerified by remember(profile) { mutableStateOf(profile?.qualificationVerified ?: false) }
    var qualificationNote by remember(profile) { mutableStateOf(profile?.qualificationNote.orEmpty()) }
    var isPickingQualificationFile by remember(profile) { mutableStateOf(false) }
    val pickerScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!isPickingQualificationFile && !processing) onDismiss() },
        title = { Text("Hồ sơ chuyên môn") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("Nhân viên: ${user.fullName ?: user.phone}", fontWeight = FontWeight.SemiBold)
                }
                if (processing) {
                    item {
                        Text("Đang upload/cập nhật hồ sơ, vui lòng đợi...",
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
                item {
                    EmployeeProfileFields(
                        qualificationTitle = qualificationTitle,
                        onQualificationTitleChange = { qualificationTitle = it },
                        qualificationInstitution = qualificationInstitution,
                        onQualificationInstitutionChange = { qualificationInstitution = it },
                        currentQualificationDocumentUrl = qualificationDocumentUrl.ifBlank { null },
                        qualificationDocumentFile = qualificationDocumentFile,
                        onChooseQualificationDocument = {
                            isPickingQualificationFile = true
                            pickerScope.launch {
                                try {
                                    delay(120)
                                    pickQualificationDocumentFile()?.let { qualificationDocumentFile = it }
                                } catch (_: Throwable) {
                                } finally {
                                    delay(300)
                                    isPickingQualificationFile = false
                                }
                            }
                        },
                        onClearQualificationDocument = { qualificationDocumentFile = null },
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
            ) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !processing) { Text("Hủy") }
        }
    )
}

@Composable
private fun EmployeeProfileFields(
    qualificationTitle: String,
    onQualificationTitleChange: (String) -> Unit,
    qualificationInstitution: String,
    onQualificationInstitutionChange: (String) -> Unit,
    currentQualificationDocumentUrl: String?,
    qualificationDocumentFile: File?,
    onChooseQualificationDocument: () -> Unit,
    onClearQualificationDocument: () -> Unit,
    qualificationVerified: Boolean,
    onQualificationVerifiedChange: (Boolean) -> Unit,
    qualificationNote: String,
    onQualificationNoteChange: (String) -> Unit,
    showVerificationControls: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Thông tin chuyên môn", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = qualificationTitle,
            onValueChange = onQualificationTitleChange,
            label = { Text("Chức danh / chứng chỉ chuyên môn") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = qualificationInstitution,
            onValueChange = onQualificationInstitutionChange,
            label = { Text("Đơn vị cấp") },
            modifier = Modifier.fillMaxWidth()
        )
        Surface(color = Color(0xFFF7FBF4), shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        TextButton(onClick = { openExternalUrl(url) }) { Text("Mở file hiện tại") }
                    }
                    Button(onClick = onChooseQualificationDocument) {
                        Text(if (qualificationDocumentFile == null) "Chọn file" else "Đổi file")
                    }
                    if (qualificationDocumentFile != null) {
                        TextButton(onClick = onClearQualificationDocument) { Text("Bỏ chọn") }
                    }
                }
            }
        }
        if (showVerificationControls) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = qualificationVerified, onCheckedChange = onQualificationVerifiedChange)
                Text("Đã xác minh hồ sơ chuyên môn")
            }
            OutlinedTextField(
                value = qualificationNote,
                onValueChange = onQualificationNoteChange,
                label = { Text("Ghi chú nội bộ") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
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
            TextButton(onClick = onDismiss, enabled = !processing) { Text("Hủy") }
        }
    )
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
                Text("Thao tác này sẽ xóa mềm tài khoản khỏi danh sách quản trị, khóa đăng nhập và giữ lịch sử đơn hàng/chat để không mất dữ liệu đối soát.")
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
            ) { Text("Xóa") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !processing) { Text("Hủy") }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Utilities
// ─────────────────────────────────────────────────────────────────────────────

private fun pickQualificationDocumentFile(): File? {
    return openFileChooser(
        title = "Chọn file minh chứng chuyên môn",
        allowedExtensions = listOf(".jpg", ".jpeg", ".png", ".pdf", ".heic"),
        allowMultiple = false
    ).firstOrNull()
}

private fun openExternalUrl(url: String) {
    runCatching {
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI(url))
    }
}
