package org.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.repositories.InternalOrderItemDto
import org.example.project.presentation.viewmodels.OrderChannel
import org.example.project.presentation.viewmodels.OrderStatus
import org.example.project.presentation.viewmodels.OrdersViewModel
import org.example.project.ui.components.OrderItemDetailDialog

@Composable
private fun OrderStatusChip(status: OrderStatus) {
    val (backgroundColor, textColor) = when (status) {
        OrderStatus.PENDING -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
        OrderStatus.PROCESSING -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
        OrderStatus.SHIPPING -> Color(0xFF2196F3) to Color.White
        OrderStatus.DELIVERED -> Color(0xFF4CAF50) to Color.White
        OrderStatus.CANCELLED -> Color(0xFFF44336) to Color.White
        OrderStatus.RETURNED -> Color.Gray to Color.White
    }

    Surface(shape = RoundedCornerShape(20.dp), color = backgroundColor) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun OrderChannelChip(channel: OrderChannel) {
    val (backgroundColor, textColor) = when (channel) {
        OrderChannel.POS -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        OrderChannel.ONLINE -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
    }

    Surface(shape = RoundedCornerShape(16.dp), color = backgroundColor) {
        Text(
            text = channel.displayName,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(viewModel: OrdersViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val statusOptions = viewModel.availableStatuses(uiState.selectedChannel)
    var selectedOrderItem by remember(uiState.selectedOrderDetail?.id) {
        mutableStateOf<InternalOrderItemDto?>(null)
    }

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý đơn hàng", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.filterOrders(it, uiState.selectedStatus, uiState.selectedChannel) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Tìm đơn hàng theo mã, khách hàng hoặc số điện thoại") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedChannel == null,
                            onClick = {
                                viewModel.filterOrders(uiState.searchQuery, uiState.selectedStatus, null)
                                viewModel.loadOrders()
                            },
                            label = { Text("Tất cả") }
                        )
                        OrderChannel.entries.forEach { channel ->
                            FilterChip(
                                selected = uiState.selectedChannel == channel,
                                onClick = {
                                    val nextStatus = uiState.selectedStatus?.takeIf { it in viewModel.availableStatuses(channel) }
                                    viewModel.filterOrders(uiState.searchQuery, nextStatus, channel)
                                    viewModel.loadOrders()
                                },
                                label = { Text(channel.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ScrollableTabRow(
                        selectedTabIndex = statusOptions.indexOf(uiState.selectedStatus).let { if (it >= 0) it + 1 else 0 },
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        indicator = {},
                        divider = {}
                    ) {
                        FilterChip(
                            selected = uiState.selectedStatus == null,
                            onClick = {
                                viewModel.filterOrders(uiState.searchQuery, null, uiState.selectedChannel)
                                viewModel.loadOrders()
                            },
                            label = { Text("Tất cả trạng thái") },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        statusOptions.forEach { status ->
                            FilterChip(
                                selected = uiState.selectedStatus == status,
                                onClick = {
                                    viewModel.filterOrders(uiState.searchQuery, status, uiState.selectedChannel)
                                    viewModel.loadOrders()
                                },
                                label = { Text(status.displayName) },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text("Mã đơn", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text("Khách hàng / kênh", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                        Text("Ngày tạo", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text("Trạng thái", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text("Tổng tiền", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    }

                    when {
                        uiState.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        uiState.error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(uiState.error!!, color = MaterialTheme.colorScheme.error) }
                        uiState.filteredOrders.isEmpty() -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Không có đơn hàng phù hợp bộ lọc hiện tại")
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Thử đổi kênh, trạng thái hoặc từ khóa tìm kiếm.",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(uiState.filteredOrders) { order ->
                                val isSelected = order.id == uiState.selectedOrder?.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                                        .clickable { viewModel.selectOrder(order) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(order.orderCode, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Text(order.customerName, fontWeight = FontWeight.Medium)
                                        Text(order.customerPhone, color = Color.Gray, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OrderChannelChip(order.channel)
                                    }
                                    Text(order.createdAt.take(10), modifier = Modifier.weight(1f), color = Color.DarkGray)
                                    Box(modifier = Modifier.weight(1f)) { OrderStatusChip(order.status) }
                                    Text(formatVND(order.total), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            uiState.selectedOrder?.let { order ->
                val detailItems = uiState.selectedOrderDetail?.items ?: emptyList()
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Chi tiết ${order.orderCode}", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    OrderStatusChip(order.status)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OrderChannelChip(order.channel)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Ngày tạo: ${order.createdAt}", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 18.dp, vertical = 14.dp)
                                    .padding(bottom = 156.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    if (order.channel == OrderChannel.POS) "Thông tin bán tại quầy" else "Thông tin giao hàng",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("${order.customerName} - ${order.customerPhone}")
                                }
                                if (order.channel == OrderChannel.ONLINE || order.address.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (order.address.isBlank()) "Chưa có địa chỉ giao hàng" else order.address, color = Color.DarkGray)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                                Text("Sản phẩm (${detailItems.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                detailItems.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedOrderItem = item }
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.name, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("${formatVND(item.price)} x ${item.quantity} ${item.unit}", color = Color.Gray, fontSize = 12.sp)
                                            val subtitle = listOfNotNull(
                                                item.categoryName?.takeIf { it.isNotBlank() },
                                                item.manufacturer?.takeIf { it.isNotBlank() }
                                            ).joinToString(" • ")
                                            if (subtitle.isNotBlank()) {
                                                Text(subtitle, color = Color.Gray, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            Text(
                                                "Xem ảnh và thông số chi tiết",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(formatVND(item.price * item.quantity), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.35f))
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Thanh toán (${order.paymentMethod})", color = Color.Gray, fontSize = 12.sp)
                                    Text(
                                        order.paymentStatus,
                                        color = if (order.paymentStatus == "COMPLETED") Color(0xFF4CAF50) else Color(0xFFFFAB00),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                order.paymentReference?.let {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Mã giao dịch", color = Color.Gray, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            it,
                                            modifier = Modifier.weight(1f),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                order.cashierName?.let {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Thu ngân", color = Color.Gray, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            it,
                                            modifier = Modifier.weight(1f),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                order.cashReceived?.let {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Tiền khách đưa", color = Color.Gray, fontSize = 12.sp)
                                        Text(formatVND(it), fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                    }
                                }
                                order.cashChange?.let {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Tiền thối", color = Color.Gray, fontSize = 12.sp)
                                        Text(formatVND(it), fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                    }
                                }
                                order.paidAt?.let {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Đã thanh toán lúc", color = Color.Gray, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            it.replace('T', ' ').take(19),
                                            modifier = Modifier.weight(1f),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Tổng cộng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(formatVND(order.total), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                uiState.invoiceMessage?.let {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(it, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                }
                                uiState.error?.let {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                }
                            }
                        }

                        Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = viewModel::openInvoiceFolder,
                                        enabled = !uiState.isExportingInvoice,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Mở thư mục", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Button(
                                        onClick = viewModel::exportSelectedInvoicePdf,
                                        enabled = !uiState.isExportingInvoice && uiState.selectedOrderDetail != null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            if (uiState.isExportingInvoice) "Đang xuất PDF" else "Xuất PDF",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                when {
                                    order.channel == OrderChannel.POS &&
                                        order.status == OrderStatus.PENDING -> {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { viewModel.updateOrderStatus(order.id, OrderStatus.CANCELLED) },
                                                enabled = !uiState.isUpdatingStatus,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                            ) {
                                                Text("Hủy đơn", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            Button(
                                                onClick = { viewModel.updateOrderStatus(order.id, OrderStatus.DELIVERED) },
                                                enabled = !uiState.isUpdatingStatus,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                            ) {
                                                Text("Hoàn tất đơn", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                    order.channel == OrderChannel.ONLINE && order.status == OrderStatus.PENDING -> {
                                        val paymentPending = order.paymentStatus.equals("PENDING", ignoreCase = true)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            if (paymentPending) {
                                                OutlinedButton(
                                                    onClick = { viewModel.updateOrderStatus(order.id, OrderStatus.CANCELLED) },
                                                    enabled = !uiState.isUpdatingStatus,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp)
                                                ) {
                                                    Text("Hủy đơn treo", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                }
                                            }
                                            Button(
                                                onClick = { viewModel.updateOrderStatus(order.id, OrderStatus.PROCESSING) },
                                                enabled = !uiState.isUpdatingStatus && !paymentPending,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                            ) {
                                                Text(
                                                    if (paymentPending) "Chờ thanh toán" else "Bắt đầu xử lý",
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                    order.channel == OrderChannel.ONLINE && order.status == OrderStatus.PROCESSING -> {
                                        Button(
                                            onClick = { viewModel.updateOrderStatus(order.id, OrderStatus.SHIPPING) },
                                            enabled = !uiState.isUpdatingStatus,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                        ) {
                                            Text("Chuyển giao vận", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    order.channel == OrderChannel.ONLINE && order.status == OrderStatus.SHIPPING -> {
                                        Button(
                                            onClick = { viewModel.updateOrderStatus(order.id, OrderStatus.DELIVERED) },
                                            enabled = !uiState.isUpdatingStatus,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                        ) {
                                            Text("Đánh dấu đã giao", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedOrderItem?.let { item ->
        OrderItemDetailDialog(
            item = item,
            onDismiss = { selectedOrderItem = null },
            formatVnd = ::formatVND
        )
    }
}
