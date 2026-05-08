package com.example.nhathuoc.ui.screen.miniscreen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nhathuoc.data.model.UserAddress
import com.example.nhathuoc.ui.theme.GreenTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressBookScreen(
    addresses: List<UserAddress>,
    selectedAddressId: String?,
    onAddressSelected: (String) -> Unit,
    onAddNewAddress: () -> Unit,
    onEditAddress: (String) -> Unit = {},
    onDeleteAddress: (String) -> Unit = {},
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var pendingDelete by remember { mutableStateOf<UserAddress?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Địa chỉ giao hàng") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = GreenTop,
                    navigationIconContentColor = GreenTop
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = onAddNewAddress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
                ) {
                    Text("Thêm địa chỉ mới")
                }
            }
        },
        containerColor = Color(0xFFF5F7FA)
    ) { innerPadding ->
        if (addresses.isEmpty()) {
            EmptyAddressState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(addresses, key = { it.id }) { address ->
                    AddressCard(
                        address = address,
                        selected = address.id == selectedAddressId,
                        onClick = { onAddressSelected(address.id) },
                        onEdit = { onEditAddress(address.id) },
                        onDelete = { pendingDelete = address }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(84.dp))
                }
            }
        }
    }

    pendingDelete?.let { address ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Xóa địa chỉ?") },
            text = { Text("Địa chỉ của ${address.recipientName} sẽ bị xóa khỏi sổ địa chỉ.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAddress(address.id)
                        pendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun EmptyAddressState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = GreenTop,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Bạn chưa có địa chỉ nào",
                    style = MaterialTheme.typography.titleMedium,
                    color = GreenTop,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Thêm địa chỉ để tiếp tục đặt hàng.",
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
private fun AddressCard(
    address: UserAddress,
    selected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) GreenTop else Color(0xFFE5E7EB)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFF0FDF4) else Color.White
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = address.recipientName,
                        color = GreenTop,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = address.recipientPhone,
                        color = Color(0xFF4B5563)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (address.isDefault) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text("Mặc định") },
                            colors = AssistChipDefaults.assistChipColors(
                                disabledContainerColor = Color(0xFFE8F5E9),
                                disabledLabelColor = GreenTop
                            )
                        )
                    }
                    if (selected) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = GreenTop
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = buildAddressLine(address),
                color = Color(0xFF374151),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenTop)
                ) {
                    Text(if (selected) "Đang chọn" else "Chọn")
                }
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenTop)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Sửa")
                }
                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935))
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Xóa")
                }
            }
        }
    }
}

private fun buildAddressLine(address: UserAddress): String {
    return listOf(address.fullAddress, address.ward, address.district, address.province)
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(", ")
}
