package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.NotificationRepository
import com.example.nhathuoc.ui.component.EmptyStateDisplay
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.BgColor
import com.example.nhathuoc.viewmodel.NotificationViewModel
import com.example.nhathuoc.viewmodel.NotificationViewModelFactory

@Composable
fun NotificationScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(NotificationRepository())
    )
) {
    val notificationsState by viewModel.notificationsState.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        HeaderNotification(unreadCount = unreadCount, onBackClick = { navController?.popBackStack() })

        // Content
        when {
            notificationsState is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenTop)
                }
            }
            notificationsState is UiState.Success -> {
                val notifications = (notificationsState as UiState.Success).data
                if (notifications.isEmpty()) {
                    EmptyStateDisplay(
                        title = "Không có thông báo",
                        message = "Bạn sẽ nhận được thông báo khi có cập nhật",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BgColor),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = {
                                    viewModel.markAsRead(notification.id)
                                }
                            )
                        }
                    }
                }
            }
            else -> {
                EmptyStateDisplay(
                    title = "Lỗi",
                    message = "Không thể tải thông báo"
                )
            }
        }
    }
}

@Composable
private fun HeaderNotification(unreadCount: Int, onBackClick: () -> Unit) {
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Thông báo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (unreadCount > 0) {
                    Text(
                        "$unreadCount chưa đọc",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            Spacer(Modifier.width(48.dp))
        }
    }
}

@Composable
private fun NotificationItem(
    notification: NotificationDto,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (notification.isRead) Color.White else Color(0xFFF0F7FF),
        shadowElevation = if (notification.isRead) 1.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = when (notification.type) {
                    "ORDER" -> GreenTop.copy(alpha = 0.2f)
                    "PROMOTION" -> Color(0xFFFFB300).copy(alpha = 0.2f)
                    else -> Color.Gray.copy(alpha = 0.2f)
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (notification.type) {
                            "ORDER" -> Icons.Outlined.LocalShipping
                            "PROMOTION" -> Icons.Outlined.LocalOffer
                            else -> Icons.Outlined.Info
                        },
                        contentDescription = null,
                        tint = when (notification.type) {
                            "ORDER" -> GreenTop
                            "PROMOTION" -> Color(0xFFFFB300)
                            else -> Color.Gray
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Top),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        notification.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    if (!notification.isRead) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = RoundedCornerShape(4.dp),
                            color = GreenTop
                        )
                    }
                }
                Text(
                    notification.message,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2
                )
                Text(
                    "Vừa xong",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Preview
@Composable
fun NotificationScreenPreview() {
    NotificationScreen()
}

// ============= Doctor List Screen =============

@Composable
fun DoctorListScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: ConsultViewModel = viewModel(
        factory = ConsultViewModelFactory(ConsultRepository())
    )
) {
    val doctorsState by viewModel.doctorsState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSpecialization by viewModel.selectedSpecialization.collectAsState()
    var showFilterBottomSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        HeaderDoctorList(
            searchQuery = searchQuery,
            onSearchChange = { viewModel.searchQuery.value = it },
            onFilterClick = { showFilterBottomSheet = true },
            onBackClick = { navController?.popBackStack() }
        )

        // Content
        when {
            doctorsState is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenTop)
                }
            }
            doctorsState is UiState.Success -> {
                val doctors = (doctorsState as UiState.Success).data
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgColor),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(doctors) { doctor ->
                        DoctorCard(
                            doctor = doctor,
                            onBookClick = {
                                navController?.navigate("DoctorDetailScreen/${doctor.id}")
                            }
                        )
                    }
                }
            }
            else -> {
                EmptyStateDisplay("Không tìm thấy bác sĩ", "")
            }
        }
    }

    if (showFilterBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterBottomSheet = false }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Chuyên khoa", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                listOf("Tim mạch", "Ngoại da", "Hô hấp", "Tiêu hóa").forEach { spec ->
                    FilterChip(
                        selected = selectedSpecialization == spec,
                        onClick = { viewModel.selectedSpecialization.value = if (selectedSpecialization == spec) null else spec },
                        label = { Text(spec) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun HeaderDoctorList(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
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
                "Tìm Bác Sĩ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onFilterClick) {
                Icon(Icons.Outlined.TuneVariant, contentDescription = null, tint = Color.White)
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Tìm bác sĩ...") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )
    }
}

@Composable
private fun DoctorCard(
    doctor: DoctorDto,
    onBookClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = BgColor
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        tint = GreenTop
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.Top),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(doctor.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(doctor.specialization, fontSize = 12.sp, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${doctor.rating} (${doctor.reviewCount})", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "500.000đ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenTop
                )
                Button(
                    onClick = onBookClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
                ) {
                    Text("Đặt lịch", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Preview
@Composable
fun DoctorListScreenPreview() {
    DoctorListScreen()
}
