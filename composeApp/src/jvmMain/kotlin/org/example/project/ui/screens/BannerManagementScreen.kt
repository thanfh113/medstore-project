package org.example.project.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.example.project.data.repositories.DesktopBannerDto
import org.example.project.presentation.viewmodels.BannerUiState
import org.example.project.presentation.viewmodels.BannerViewModel
import org.example.project.utils.openFileChooser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BannerManagementScreen(viewModel: BannerViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý banner", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = viewModel::loadBanners, enabled = !state.isLoading) {
                    Text(if (state.isLoading) "Đang tải..." else "Tải lại")
                }
                state.successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            Row(
                modifier = Modifier.fillMaxSize(),
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
}

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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onPickImage, enabled = !state.isUploading) {
                    Text(if (state.isUploading) "Đang upload..." else "Chọn ảnh")
                }
                Text("Ảnh banner lưu local qua BE, Android tự resolve URL theo máy đang test.")
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
            OutlinedTextField(
                value = state.linkUrl,
                onValueChange = onLinkUrlChange,
                label = { Text("Link điều hướng, ví dụ: /products, /chat, /products/{id}") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.sortOrder,
                onValueChange = onSortOrderChange,
                label = { Text("Thứ tự hiển thị") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.startDt,
                onValueChange = onStartDtChange,
                label = { Text("Bắt đầu, dạng 2026-05-06T08:00:00") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.endDt,
                onValueChange = onEndDtChange,
                label = { Text("Kết thúc, dạng 2026-05-31T23:59:59") },
                modifier = Modifier.fillMaxWidth()
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
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Danh sách banner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (banners.isEmpty()) {
                Text("Chưa có banner.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            BannerThumb(banner.imageUrl)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        banner.title?.takeIf { it.isNotBlank() } ?: "Banner không tiêu đề",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    AssistChip(onClick = {}, label = { Text(if (banner.isActive) "Đang bật" else "Đang tắt") })
                    AssistChip(onClick = {}, label = { Text("Thứ tự ${banner.sortOrder}") })
                }
                banner.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text("Link: ${banner.linkUrl ?: "Không điều hướng"}")
                Text("Thời gian: ${banner.startDt ?: "ngay"} -> ${banner.endDt ?: "không giới hạn"}")
            }
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
