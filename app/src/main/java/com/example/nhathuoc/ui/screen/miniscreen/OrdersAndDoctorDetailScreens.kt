package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.OrderRepository
import com.example.nhathuoc.ui.component.EmptyStateDisplay
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.BgColor
import com.example.nhathuoc.util.PriceUtils
import com.example.nhathuoc.viewmodel.OrderViewModel
import com.example.nhathuoc.viewmodel.OrderViewModelFactory

@Composable
fun MyOrdersScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: OrderViewModel = viewModel(
        factory = OrderViewModelFactory(OrderRepository(null))
    )
) {
    val ordersState by viewModel.ordersState.collectAsState()
    var selectedStatus by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        HeaderMyOrders(onBackClick = { navController?.popBackStack() })

        // Status filter tabs
        StatusFilterTabs(
            selectedStatus = selectedStatus,
            onStatusChange = {
                selectedStatus = it
                viewModel.filterByStatus(it)
            }
        )

        // Orders list
        when {
            ordersState is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenTop)
                }
            }
            ordersState is UiState.Success -> {
                val orders = (ordersState as UiState.Success).data
                if (orders.isEmpty()) {
                    EmptyStateDisplay("Chưa có đơn hàng", "")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BgColor),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(orders) { order ->
                            OrderItemCard(
                                order = order,
                                onClick = { navController?.navigate("OrderDetailScreen/${order.id}") }
                            )
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun HeaderMyOrders(onBackClick: () -> Unit) {
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
                "Đơn hàng của tôi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.width(48.dp))
        }
    }
}

@Composable
private fun StatusFilterTabs(
    selectedStatus: String?,
    onStatusChange: (String?) -> Unit
) {
    val statuses = listOf(
        null to "Tất cả",
        "PENDING" to "Chờ xác nhận",
        "CONFIRMED" to "Đã xác nhận",
        "SHIPPING" to "Đang giao",
        "DELIVERED" to "Đã giao"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(statuses.size) { index ->
            val (status, label) = statuses[index]
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusChange(status) },
                label = { Text(label, fontSize = 12.sp) }
            )
        }
    }
}

@Composable
private fun OrderItemCard(
    order: OrderDto,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Order header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Mã đơn: ${order.id}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Ngày đặt: 2026-04-02", fontSize = 11.sp, color = Color.Gray)
                }
                Surface(
                    color = when (order.status) {
                        OrderStatus.PENDING -> Color(0xFFFFB300).copy(alpha = 0.1f)
                        OrderStatus.CONFIRMED -> Color(0xFF2196F3).copy(alpha = 0.1f)
                        OrderStatus.SHIPPING -> Color(0xFF9C27B0).copy(alpha = 0.1f)
                        OrderStatus.DELIVERED -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else -> Color.Gray.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        when (order.status) {
                            OrderStatus.PENDING -> "Chờ xác nhận"
                            OrderStatus.CONFIRMED -> "Đã xác nhận"
                            OrderStatus.SHIPPING -> "Đang giao"
                            OrderStatus.DELIVERED -> "Đã giao"
                            OrderStatus.CANCELLED -> "Đã hủy"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (order.status) {
                            OrderStatus.PENDING -> Color(0xFFFFB300)
                            OrderStatus.CONFIRMED -> Color(0xFF2196F3)
                            OrderStatus.SHIPPING -> Color(0xFF9C27B0)
                            OrderStatus.DELIVERED -> Color(0xFF4CAF50)
                            else -> Color.Gray
                        },
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Divider()

            // Order items preview
            order.items.take(2).forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.name, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("x${item.quantity}", fontSize = 11.sp, color = Color.Gray)
                }
            }

            if (order.items.size > 2) {
                Text("+${order.items.size - 2} sản phẩm khác", fontSize = 11.sp, color = Color.Gray)
            }

            Divider()

            // Total and action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tổng cộng", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        PriceUtils.formatPrice(order.totalAmount),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenTop
                    )
                }
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
                ) {
                    Text("Chi tiết", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

@Preview
@Composable
fun MyOrdersScreenPreview() {
    MyOrdersScreen()
}

// ============= Doctor Detail Screen =============

@Composable
fun DoctorDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    doctorId: String = "doc-1",
    viewModel: ConsultViewModel = viewModel(
        factory = ConsultViewModelFactory(ConsultRepository())
    )
) {
    val doctorDetailState by viewModel.doctorDetailState.collectAsState()
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var showBookDialog by remember { mutableStateOf(false) }

    LaunchedEffect(doctorId) {
        viewModel.getDoctorDetail(doctorId)
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            doctorDetailState is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenTop)
                }
            }
            doctorDetailState is UiState.Success -> {
                val doctor = (doctorDetailState as UiState.Success).data
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(BgColor),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        DoctorHeaderDetail(doctor, onBackClick = { navController?.popBackStack() })
                    }

                    item {
                        DoctorInfoCard(doctor)
                    }

                    item {
                        AboutSection(doctor.description ?: "")
                    }

                    item {
                        QualificationSection(
                            experience = doctor.experience ?: "",
                            qualification = doctor.qualification ?: ""
                        )
                    }

                    item {
                        RatingsSection(rating = doctor.rating, reviewCount = doctor.reviewCount)
                    }

                    item {
                        Spacer(Modifier.height(20.dp))
                    }
                }

                // Book button
                Button(
                    onClick = { showBookDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTop)
                ) {
                    Text("Đặt lịch tư vấn", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            else -> {}
        }
    }

    if (showBookDialog) {
        BookConsultationDialog(
            onDismiss = { showBookDialog = false },
            onConfirm = { date, time, reason ->
                viewModel.bookConsultation(doctorId, "$date $time", "VIDEO", reason)
                showBookDialog = false
            }
        )
    }
}

@Composable
private fun DoctorHeaderDetail(doctor: DoctorDto, onBackClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                }
            }

            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(50.dp),
                color = BgColor
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(20.dp),
                    tint = GreenTop
                )
            }

            Text(doctor.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(doctor.specialization, fontSize = 14.sp, color = Color.Gray)

            Rating(rating = doctor.rating, count = doctor.reviewCount)
        }
    }
}

@Composable
private fun DoctorInfoCard(doctor: DoctorDto) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoItem("500.000đ", "Giá tư vấn")
            Divider(modifier = Modifier.width(1.dp).height(50.dp))
            InfoItem("Sẵn sàng", "Trạng thái")
            Divider(modifier = Modifier.width(1.dp).height(50.dp))
            InfoItem("Video", "Loại tư vấn")
        }
    }
}

@Composable
private fun InfoItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
private fun About Section(description: String) {
    if (description.isNotEmpty()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Giới thiệu", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(description, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun QualificationSection(experience: String, qualification: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (experience.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Kinh nghiệm", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(experience, fontSize = 12.sp, color = Color.Gray)
                }
            }
            if (qualification.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Bằng cấp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(qualification, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun RatingsSection(rating: Double, reviewCount: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Đánh giá", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Rating(rating = rating, count = reviewCount)
        }
    }
}

@Composable
private fun Rating(rating: Double, count: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
        Text("$rating", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text("($count đánh giá)", fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
private fun BookConsultationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var selectedDate by remember { mutableStateOf("2026-04-15") }
    var selectedTime by remember { mutableStateOf("14:00") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đặt lịch tư vấn") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { selectedDate = it },
                    label = { Text("Ngày") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = selectedTime,
                    onValueChange = { selectedTime = it },
                    label = { Text("Giờ") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Lý do tư vấn") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedDate, selectedTime, reason) }) {
                Text("Đặt lịch")
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
fun DoctorDetailScreenPreview() {
    DoctorDetailScreen()
}
