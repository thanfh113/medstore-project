package com.example.nhathuoc.ui.screen.miniscreen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.nhathuoc.data.model.UiState
import com.example.nhathuoc.ui.screen.AvatarInitials
import com.example.nhathuoc.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val GreenTopHeader = Color(0xFF2E7D32)
private val GreenLightHeader = Color(0xFF66BB6A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    val storedName = authViewModel.userFullName.collectAsState(initial = "")
    val storedPhone = authViewModel.userPhone.collectAsState(initial = "")
    val storedEmail = authViewModel.userEmail.collectAsState(initial = "")
    val storedAvatarUri = authViewModel.userAvatarUri.collectAsState(initial = null)
    val storedGender = authViewModel.userGender.collectAsState(initial = null)
    val storedDob = authViewModel.userDateOfBirth.collectAsState(initial = null)
    val userState by authViewModel.userState.collectAsState()

    var fullName by remember(storedName.value) { mutableStateOf(storedName.value ?: "") }
    var email by remember(storedEmail.value) { mutableStateOf(storedEmail.value ?: "") }
    var avatarUri by remember(storedAvatarUri.value) { mutableStateOf(storedAvatarUri.value) }
    var avatarChanged by remember { mutableStateOf(false) }
    var gender by remember(storedGender.value) { mutableStateOf(storedGender.value) }
    var dateOfBirth by remember(storedDob.value) { mutableStateOf(storedDob.value ?: "") }
    val phone = storedPhone.value ?: ""

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var hasAttemptedSave by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val isLoading = userState is UiState.Loading
    val saveError = (userState as? UiState.Error)?.message

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val path = withContext(Dispatchers.IO) { copyUriToFile(context, uri) }
                if (path != null) {
                    avatarUri = path
                    avatarChanged = true
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.clearUserState()
        authViewModel.getCurrentUser()
    }

    LaunchedEffect(userState) {
        if (userState is UiState.Success && hasAttemptedSave) {
            authViewModel.clearUserState()
            onBack()
        }
    }

    LaunchedEffect(showDatePicker) {
        if (showDatePicker) {
            datePickerState.selectedDateMillis = parseYMDToMillis(dateOfBirth)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateOfBirth = formatMillisToYMD(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Hủy") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            com.example.nhathuoc.ui.component.GreenAppTopBar(
                title = "Thông tin cá nhân",
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                    .background(Brush.horizontalGradient(listOf(GreenTopHeader, GreenLightHeader)))
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .clickable { imagePicker.launch("image/*") }
                ) {
                    if (!avatarUri.isNullOrBlank()) {
                        val avatarModel = if (avatarUri!!.startsWith("http")) avatarUri else File(avatarUri!!)
                        AsyncImage(
                            model = avatarModel,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AvatarInitials(name = fullName.ifBlank { "U" }, size = 80)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.CameraAlt,
                            contentDescription = "Chọn ảnh",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
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
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileField(
                        label = "Số điện thoại",
                        value = phone,
                        onValueChange = {},
                        placeholder = "",
                        enabled = false,
                        trailingText = "Không thể thay đổi"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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

            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(
                            "Giới tính",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Nam" to 1, "Nữ" to 2, "Khác" to 3).forEach { (label, value) ->
                                val selected = gender == value
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            1.dp,
                                            if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable(enabled = !isLoading) { gender = value }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selected,
                                        onClick = { gender = value },
                                        enabled = !isLoading,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(label, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Column {
                        Text(
                            "Ngày sinh",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
                        Box {
                            OutlinedTextField(
                                value = formatDateForDisplay(dateOfBirth),
                                onValueChange = {},
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                placeholder = {
                                    Text(
                                        "Chọn ngày sinh",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Outlined.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        enabled = !isLoading
                                    ) { showDatePicker = true }
                            )
                        }
                    }
                }
            }

            if (saveError != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    saveError,
                    color = MaterialTheme.colorScheme.error,
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
                    if (valid) {
                        hasAttemptedSave = true
                        authViewModel.updateProfile(
                            fullName = fullName.trim(),
                            email = email.trim(),
                            gender = gender,
                            dateOfBirth = dateOfBirth.ifBlank { null },
                            avatarFile = if (avatarChanged && !avatarUri.isNullOrBlank() && !avatarUri!!.startsWith("http"))
                                File(avatarUri!!) else null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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

private fun copyUriToFile(context: Context, uri: Uri): String? {
    return try {
        val input = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.filesDir, "user_avatar.jpg")
        file.outputStream().use { output -> input.use { it.copyTo(output) } }
        file.absolutePath
    } catch (_: Exception) { null }
}

private fun parseYMDToMillis(date: String?): Long? {
    if (date.isNullOrBlank()) return null
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        sdf.parse(date)?.time
    } catch (_: Exception) { null }
}

private fun formatMillisToYMD(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(millis))
}

private fun formatDateForDisplay(date: String?): String {
    if (date.isNullOrBlank()) return ""
    return try {
        val parse = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val display = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        parse.parse(date)?.let { display.format(it) } ?: date
    } catch (_: Exception) { date }
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
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            isError = error != null,
            trailingIcon = if (trailingText != null) ({
                Text(trailingText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 4.dp))
            }) else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(8.dp)
        )
        if (error != null) {
            Text(error, fontSize = 11.sp, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
