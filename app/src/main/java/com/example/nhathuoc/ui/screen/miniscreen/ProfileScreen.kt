package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.ProfileRepository
import com.example.nhathuoc.ui.component.ErrorMessageCard
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.BgColor
import com.example.nhathuoc.viewmodel.ProfileViewModel
import com.example.nhathuoc.viewmodel.ProfileViewModelFactory

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(ProfileRepository())
    )
) {
    val profileState by viewModel.profileState.collectAsState()
    val addressesState by viewModel.addressesState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddAddressDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        HeaderProfile(onBackClick = { navController?.popBackStack() })

        // Content
        when {
            profileState is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenTop)
                }
            }
            profileState is UiState.Error -> {
                ErrorMessageCard((profileState as UiState.Error).message)
            }
            profileState is UiState.Success -> {
                val profile = (profileState as UiState.Success).data
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(BgColor),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ProfileHeader(profile, onEditClick = { showEditDialog = true })
                    }

                    item {
                        ProfileStats()
                    }

                    item {
                        Text(
                            "Địa chỉ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    when (val addrState = addressesState) {
                        is UiState.Success -> {
                            items(addrState.data) { address ->
                                AddressItem(
                                    address = address,
                                    onDeleteClick = { viewModel.deleteAddress(address.id) },
                                    onSetDefaultClick = { viewModel.setDefaultAddress(address.id) }
                                )
                            }
                        }
                        else -> {}
                    }

                    item {
                        Button(
                            onClick = { showAddAddressDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Thêm địa chỉ mới")
                        }
                    }

                    item {
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            onDismiss = { showEditDialog = false },
            onConfirm = { name, email ->
                viewModel.updateProfile(name, email)
                showEditDialog = false
            }
        )
    }

    if (showAddAddressDialog) {
        AddAddressDialog(
            onDismiss = { showAddAddressDialog = false },
            onConfirm = { name, phone, address ->
                viewModel.addAddress(name, phone, address, "", "", AddressType.HOME)
                showAddAddressDialog = false
            }
        )
    }
}

@Composable
private fun HeaderProfile(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(GreenTop, GreenLight)))
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(
                "Hồ sơ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.width(48.dp))
        }
    }
}

@Composable
private fun ProfileHeader(profile: UserProfileDto, onEditClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(40.dp),
                color = GreenTop.copy(alpha = 0.2f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = GreenTop,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    profile.fullName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    profile.email,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    profile.phone,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
            ) {
                Text("Chỉnh sửa hồ sơ", color = Color.White)
            }
        }
    }
}

@Composable
private fun ProfileStats() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard("Đơn hàng", "12", Modifier.weight(1f))
        StatCard("Reward", "500đ", Modifier.weight(1f))
        StatCard("Đánh giá", "4.5", Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenTop)
            Text(title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun AddressItem(
    address: UserAddressDto,
    onDeleteClick: () -> Unit,
    onSetDefaultClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(address.fullName, fontWeight = FontWeight.Bold)
                        if (address.isDefault) {
                            Surface(
                                color = GreenTop,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text("Mặc định", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                            }
                        }
                    }
                    Text(address.phone, fontSize = 12.sp, color = Color.Gray)
                    Text(address.address, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                }
            }
            if (!address.isDefault) {
                Button(
                    onClick = onSetDefaultClick,
                    modifier = Modifier
                        .align(Alignment.End)
                        .height(32.dp),
                    colors = ButtonDefaults.textButtonColors()
                ) {
                    Text("Đặt mặc định", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun EditProfileDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chỉnh sửa hồ sơ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Họ và tên") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, email) }) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun AddAddressDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm địa chỉ mới") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Họ và tên") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Địa chỉ") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, phone, address) }) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Preview
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
