package org.example.project.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.example.project.data.repositories.DesktopBannerDto
import org.example.project.presentation.viewmodels.BannerUiState
import org.example.project.presentation.viewmodels.BannerViewModel
import org.example.project.util.formatVnDateTime
import org.example.project.utils.openFileChooser

private data class BannerLinkOption(val label: String, val prefix: String, val needsId: Boolean = false)

private val bannerLinkOptions = listOf(
    BannerLinkOption("Không điều hướng", ""),
    BannerLinkOption("Danh sách sản phẩm", "/products"),
    BannerLinkOption("Flash Sale", "/flash-sale"),
    BannerLinkOption("Sản phẩm cụ thể (nhập ID)", "/products/", needsId = true),
    BannerLinkOption("Danh mục sản phẩm (nhập ID)", "/categories/", needsId = true),
    BannerLinkOption("Giỏ hàng", "/cart"),
    BannerLinkOption("Chat tư vấn nhân viên", "/chat"),
    BannerLinkOption("Tư vấn AI", "/ai-chat"),
    BannerLinkOption("Điểm thưởng", "/rewards"),
    BannerLinkOption("Đơn hàng của tôi", "/orders"),
    BannerLinkOption("Khiếu nại", "/complaints"),
    BannerLinkOption("Thông báo", "/notifications"),
    BannerLinkOption("Trang cá nhân", "/profile"),
)

private fun linkUrlLabel(linkUrl: String?): String {
    if (linkUrl.isNullOrBlank()) return "Không điều hướng"
    val opt = bannerLinkOptions.firstOrNull { opt ->
        if (opt.prefix.isEmpty()) false
        else if (opt.needsId) linkUrl.startsWith(opt.prefix)
        else linkUrl.equals(opt.prefix, ignoreCase = true)
    } ?: return linkUrl
    return if (opt.needsId) {
        val id = linkUrl.removePrefix(opt.prefix)
        "${opt.label.substringBefore(" (")}: $id"
    } else opt.label
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BannerManagementScreen(viewModel: BannerViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý banner", fontWeight = FontWeight.Bold) },
                actions = {
                    state.successMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    state.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = viewModel::loadBanners, enabled = !state.isLoading) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Tải lại")
                        }
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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BannerFormCard(
                state = state,
                modifier = Modifier.weight(0.9f),
                onPickImage = {
                    openFileChooser(
                        title = "Chọn ảnh banner",
                        allowedExtensions = listOf(".jpg", ".jpeg", ".png", ".webp", ".heic"),
                        allowMultiple = false
                    ).firstOrNull()?.let(viewModel::uploadBannerImage)
                },
                onImageUrlChange = viewModel::updateImageUrl,
                onTitleChange = viewModel::updateTitle,
                onDescriptionChange = viewModel::updateDescription,
                onLinkUrlChange = viewModel::updateLinkUrl,
                onSortOrderChange = viewModel::updateSortOrder,
                onStartDtChange = viewModel::updateStartDt,
                onEndDtChange = viewModel::updateEndDt,
                onToggleActive = viewModel::toggleActive,
                onSubmit = viewModel::submitBanner,
                onCancelEdit = viewModel::cancelEdit
            )

            BannerListCard(
                banners = state.banners,
                modifier = Modifier.weight(1.25f),
                onEdit = viewModel::editBanner,
                onToggle = viewModel::toggleBannerFromList,
                onDelete = viewModel::deleteBanner
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BannerFormCard(
    state: BannerUiState,
    modifier: Modifier,
    onPickImage: () -> Unit,
    onImageUrlChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onLinkUrlChange: (String) -> Unit,
    onSortOrderChange: (String) -> Unit,
    onStartDtChange: (String) -> Unit,
    onEndDtChange: (String) -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onCancelEdit: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Thông tin banner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                state.editingId?.let { AssistChip(onClick = {}, label = { Text("Đang sửa") }) }
            }

            BannerPreview(state.imageUrl)

            Button(onClick = onPickImage, enabled = !state.isUploading) {
                Text(if (state.isUploading) "Đang upload..." else "Chọn ảnh")
            }

            OutlinedTextField(
                value = state.imageUrl,
                onValueChange = onImageUrlChange,
                label = { Text("URL ảnh banner") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Tiêu đề") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                label = { Text("Mô tả ngắn") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            // Link dropdown
            val selectedOpt = remember(state.linkUrl) {
                bannerLinkOptions.firstOrNull { opt ->
                    if (opt.prefix.isEmpty()) state.linkUrl.isEmpty()
                    else if (opt.needsId) state.linkUrl.startsWith(opt.prefix)
                    else state.linkUrl.equals(opt.prefix, ignoreCase = true)
                } ?: bannerLinkOptions.first()
            }
            val extractedId = remember(state.linkUrl, selectedOpt) {
                if (selectedOpt.needsId) state.linkUrl.removePrefix(selectedOpt.prefix) else ""
            }
            var dropdownExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedOpt.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Điều hướng tới") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    bannerLinkOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt.label) },
                            onClick = {
                                dropdownExpanded = false
                                onLinkUrlChange(opt.prefix)
                            }
                        )
                    }
                }
            }
            if (selectedOpt.needsId) {
                OutlinedTextField(
                    value = extractedId,
                    onValueChange = { onLinkUrlChange("${selectedOpt.prefix}$it") },
                    label = { Text(if (selectedOpt.prefix.startsWith("/products/")) "Product ID" else "Category ID") },
                    placeholder = { Text("Nhập ID tại đây") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            OutlinedTextField(
                value = state.sortOrder,
                onValueChange = onSortOrderChange,
                label = { Text("Thứ tự hiển thị") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            DateTimePickerField(
                label = "Bắt đầu",
                isoValue = state.startDt,
                emptyLabel = "Hiệu lực ngay",
                onClear = { onStartDtChange("") },
                onConfirm = onStartDtChange
            )
            DateTimePickerField(
                label = "Kết thúc",
                isoValue = state.endDt,
                emptyLabel = "Không giới hạn",
                onClear = { onEndDtChange("") },
                onConfirm = onEndDtChange
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Switch(checked = state.isActive, onCheckedChange = onToggleActive)
                Text(if (state.isActive) "Đang bật" else "Đang tắt")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onSubmit,
                    enabled = !state.isSubmitting && !state.isUploading && state.imageUrl.isNotBlank()
                ) {
                    Text(if (state.editingId == null) "Thêm banner" else "Lưu thay đổi")
                }
                if (state.editingId != null) {
                    OutlinedButton(onClick = onCancelEdit) {
                        Text("Hủy sửa")
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerListCard(
    banners: List<DesktopBannerDto>,
    modifier: Modifier,
    onEdit: (DesktopBannerDto) -> Unit,
    onToggle: (DesktopBannerDto) -> Unit,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Danh sách banner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (banners.isEmpty()) {
                Text("Chưa có banner.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(banners, key = { it.id }) { banner ->
                        BannerRow(
                            banner = banner,
                            onEdit = { onEdit(banner) },
                            onToggle = { onToggle(banner) },
                            onDelete = { onDelete(banner.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerRow(
    banner: DesktopBannerDto,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            BannerThumb(banner.imageUrl)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        banner.title?.takeIf { it.isNotBlank() } ?: "Banner không tiêu đề",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        AssistChip(onClick = {}, label = { Text(if (banner.isActive) "Đang bật" else "Đang tắt") })
                        AssistChip(onClick = {}, label = { Text("#${banner.sortOrder}") })
                    }
                }
                banner.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "→ ${linkUrlLabel(banner.linkUrl)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${banner.startDt?.let { formatVnDateTime(it) } ?: "Hiệu lực ngay"} → ${banner.endDt?.let { formatVnDateTime(it) } ?: "∞"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onToggle) {
                        Text(if (banner.isActive) "Tắt" else "Bật")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Sửa")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerPreview(imageUrl: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFE8F5E9)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isBlank()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(6.dp))
                Text("Chưa chọn ảnh banner")
            }
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Banner preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun BannerThumb(imageUrl: String) {
    Box(
        modifier = Modifier
            .size(width = 128.dp, height = 72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE8F5E9)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Banner",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun DateTimePickerField(
    label: String,
    isoValue: String,
    emptyLabel: String,
    onClear: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val displayText = if (isoValue.isBlank()) emptyLabel else formatVnDateTime(isoValue)

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Surface(
                onClick = { showDialog = true },
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        displayText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isoValue.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface
                    )
                    Icon(Icons.Default.DateRange, contentDescription = null,
                        modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            if (isoValue.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = "Xóa",
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }

    if (showDialog) {
        DateTimePickerDialog(
            initial = isoValue,
            onDismiss = { showDialog = false },
            onConfirm = { onConfirm(it); showDialog = false }
        )
    }
}

@Composable
private fun DateTimePickerDialog(
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val now = java.time.LocalDateTime.now()
    val parsed = remember(initial) {
        runCatching { java.time.LocalDateTime.parse(initial.substringBefore('.')) }.getOrElse { now }
    }

    var year by remember { mutableStateOf(parsed.year) }
    var month by remember { mutableStateOf(parsed.monthValue) }
    var day by remember { mutableStateOf(parsed.dayOfMonth) }
    var hour by remember { mutableStateOf(parsed.hour) }
    var minute by remember { mutableStateOf(parsed.minute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 380.dp),
        title = { Text("Chọn ngày giờ", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Ngày tháng năm", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DtSpinnerField("Ngày", day, 1..31, Modifier.weight(1f)) {
                        day = it.coerceIn(1, bannerMaxDay(year, month))
                    }
                    DtSpinnerField("Tháng", month, 1..12, Modifier.weight(1f)) {
                        month = it.coerceIn(1, 12)
                        day = day.coerceIn(1, bannerMaxDay(year, month))
                    }
                    DtSpinnerField("Năm", year, 2024..2035, Modifier.weight(1.5f)) {
                        year = it.coerceIn(2024, 2035)
                        day = day.coerceIn(1, bannerMaxDay(year, month))
                    }
                }
                HorizontalDivider()
                Text("Giờ phút", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DtSpinnerField("Giờ", hour, 0..23, Modifier.weight(1f)) { hour = it.coerceIn(0, 23) }
                    Text(":", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    DtSpinnerField("Phút", minute, 0..59, Modifier.weight(1f)) { minute = it.coerceIn(0, 59) }
                }
                HorizontalDivider()
                Text(
                    "Kết quả: %02d/%02d/%04d %02d:%02d".format(day, month, year, hour, minute),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val iso = "%04d-%02d-%02dT%02d:%02d:00".format(year, month, day, hour, minute)
                onConfirm(iso)
            }) { Text("Xác nhận") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
private fun DtSpinnerField(
    label: String,
    value: Int,
    range: IntRange,
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        IconButton(
            onClick = { if (value < range.last) onValueChange(value + 1) },
            modifier = Modifier.size(28.dp)
        ) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null) }
        OutlinedTextField(
            value = "%02d".format(value),
            onValueChange = { txt -> txt.toIntOrNull()?.let { onValueChange(it) } },
            modifier = Modifier.width(64.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        IconButton(
            onClick = { if (value > range.first) onValueChange(value - 1) },
            modifier = Modifier.size(28.dp)
        ) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) }
    }
}

private fun bannerMaxDay(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    else -> 30
}
