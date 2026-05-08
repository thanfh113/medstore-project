package org.example.project.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.benasher44.uuid.uuid4
import org.example.project.data.models.Product
import org.example.project.data.models.ProductCategory
import org.example.project.data.models.ProductImage
import org.example.project.data.models.RiskClassification
import org.example.project.data.models.categoryDisplayPath
import org.example.project.data.models.childProductCategories
import org.example.project.data.models.selectedCategoryGroupId
import org.example.project.data.models.topLevelProductCategories
import org.example.project.utils.openFileChooser
import java.io.File

@Composable
fun ProductFormDialog(
    product: Product? = null,
    categories: List<ProductCategory>,
    onSave: (Product, List<File>) -> Unit,
    onCancel: () -> Unit,
    isCreating: Boolean = false,
    isUpdating: Boolean = false
) {
    val isEditing = product != null
    val title = if (isEditing) "Cập nhật sản phẩm" else "Thêm sản phẩm mới"
    val isLoading = isCreating || isUpdating

    var name by remember { mutableStateOf(product?.name ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var originalPrice by remember { mutableStateOf(product?.originalPrice?.toString() ?: "") }
    var quantity by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "") }
    var selectedCategoryId by remember { mutableStateOf(product?.categoryId?.takeIf { it.isNotBlank() }) }
    var manufacturer by remember { mutableStateOf(product?.manufacturer ?: "") }
    var origin by remember { mutableStateOf(product?.origin ?: "") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var unit by remember { mutableStateOf(product?.unit ?: "Cái") }
    var registrationNumber by remember { mutableStateOf(product?.registrationNumber ?: "") }
    var riskClassification by remember { mutableStateOf(product?.riskClassification ?: RiskClassification.A) }
    var requiresCertification by remember { mutableStateOf(product?.ceIsoRequired ?: false) }
    var requiresConsultation by remember { mutableStateOf(product?.requiresTechnicalConsultation ?: false) }
    var isActive by remember { mutableStateOf(product?.isActive ?: true) }
    val existingImages = remember(product?.id) {
        mutableStateListOf<ProductImage>().apply {
            addAll(product?.images?.sortedBy { it.sortOrder } ?: emptyList())
        }
    }
    val newImageFiles = remember(product?.id) { mutableStateListOf<File>() }

    val parsedPrice = price.toDoubleOrNull()
    val parsedOriginalPrice = originalPrice.toDoubleOrNull()
    val restrictedOnlineRisk = riskClassification == RiskClassification.C || riskClassification == RiskClassification.D
    val isOriginalPriceValid = originalPrice.isBlank() || (parsedOriginalPrice != null && parsedPrice != null && parsedOriginalPrice <= parsedPrice)
    val isFormValid = name.isNotBlank() && selectedCategoryId != null && parsedPrice != null && parsedPrice > 0.0 && isOriginalPriceValid

    Dialog(
        onDismissRequest = { if (!isLoading) onCancel() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.72f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 20.dp, 16.dp, 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = title, style = MaterialTheme.typography.headlineSmall)
                    IconButton(onClick = onCancel, enabled = !isLoading) {
                        Icon(Icons.Default.Close, contentDescription = "Dong")
                    }
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    FormSection(title = "Thong tin co ban") {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Tên sản phẩm *") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            isError = name.isBlank()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CategoryDropdown(
                            categories = categories,
                            selectedCategory = selectedCategoryId,
                            onCategorySelected = { selectedCategoryId = it },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Mô tả") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            enabled = !isLoading
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = price,
                                onValueChange = { price = it },
                                label = { Text("Gia ban * (VND)") },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading,
                                isError = parsedPrice == null
                            )

                            OutlinedTextField(
                                value = originalPrice,
                                onValueChange = { originalPrice = it },
                                label = { Text("Gia goc (VND)") },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading,
                                isError = !isOriginalPriceValid
                            )
                        }

                        if (!isOriginalPriceValid) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Giá gốc phải nhỏ hơn hoặc bằng giá bán",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text("Đơn vị") },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            )

                            OutlinedTextField(
                                value = sku,
                                onValueChange = { sku = it },
                                label = { Text("SKU / mã nội bộ") },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Số lượng tồn kho") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    FormSection(title = "Thong tin nghiep vu") {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Loai du lieu hien tai duoc xac dinh theo danh muc. Ton kho duoc quan ly o nghiep vu nhap kho, khong sua truc tiep tai form nay.",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }


                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = manufacturer,
                                onValueChange = { manufacturer = it },
                                label = { Text("Hãng / nhân hàng") },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            )

                            OutlinedTextField(
                                value = origin,
                                onValueChange = { origin = it },
                                label = { Text("Xuất xứ") },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = registrationNumber,
                            onValueChange = { registrationNumber = it },
                            label = { Text("Số lưu hành / mã đăng ký") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        RiskClassificationDropdown(
                            selectedRiskClassification = riskClassification,
                            onRiskClassificationSelected = {
                                riskClassification = it
                                if (it == RiskClassification.C || it == RiskClassification.D) {
                                    requiresCertification = true
                                    requiresConsultation = true
                                }
                            },
                            enabled = !isLoading
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (restrictedOnlineRisk) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Loại ${riskClassification.value} chỉ hiển thị để tư vấn. User không thể đặt online, cần tư vấn/ký kết tại nhà thuốc.",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = requiresCertification || restrictedOnlineRisk,
                                onCheckedChange = { requiresCertification = it || restrictedOnlineRisk },
                                enabled = !isLoading && !restrictedOnlineRisk
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Yêu cầu chứng nhận hồ sơ thiết bị")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = requiresConsultation || restrictedOnlineRisk,
                                onCheckedChange = { requiresConsultation = it || restrictedOnlineRisk },
                                enabled = !isLoading && !restrictedOnlineRisk
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Cần tư vấn kỹ thuật trước khi bán")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isActive,
                                onCheckedChange = { isActive = it },
                                enabled = !isLoading
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Đang kinh doanh")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    FormSection(title = "Anh san pham") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dang co ${existingImages.size + newImageFiles.size} anh gan voi san pham",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = {
                                    val selectedFiles = openFileChooser(
                                        title = "Chọn ảnh sản phẩm",
                                        allowedExtensions = listOf(".jpg", ".jpeg", ".png", ".webp", ".heic"),
                                        allowMultiple = true
                                    )
                                    selectedFiles.forEach { selectedFile ->
                                        val duplicated = newImageFiles.any { it.absolutePath == selectedFile.absolutePath }
                                        if (!duplicated) {
                                            newImageFiles.add(selectedFile)
                                        }
                                    }
                                },
                                enabled = !isLoading
                            ) {
                                Text("Chọn ảnh")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (existingImages.isEmpty() && newImageFiles.isEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Chua co anh nao. File moi se duoc upload len backend, backend day len Cloudinary roi luu link vao DB.",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            existingImages.forEachIndexed { index, image ->
                                SelectedImageItem(
                                    title = "Anh da luu ${index + 1}",
                                    subtitle = image.url,
                                    enabled = !isLoading,
                                    onRemove = { existingImages.remove(image) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            newImageFiles.forEachIndexed { index, file ->
                                SelectedImageItem(
                                    title = "Anh moi ${index + 1}",
                                    subtitle = "${file.name} (${formatFileSize(file.length())})",
                                    enabled = !isLoading,
                                    onRemove = { newImageFiles.remove(file) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancel, enabled = !isLoading) {
                        Text("Hủy")
                    }

                    Spacer(modifier = Modifier.padding(6.dp))

                    Button(
                        onClick = {
                            val now = java.time.Instant.now().toString()
                            val normalizedImages = existingImages.mapIndexed { index, image ->
                                image.copy(sortOrder = index)
                            }
                            onSave(
                                Product(
                                    id = product?.id ?: uuid4().toString(),
                                    name = name.trim(),
                                    description = description.trim(),
                                    price = parsedPrice!!,
                                    originalPrice = parsedOriginalPrice,
                                    categoryId = selectedCategoryId!!,
                                    stockQuantity = quantity.toIntOrNull() ?: 0,
                                    manufacturer = manufacturer.trim(),
                                    origin = origin.trim(),
                                    sku = sku.trim().ifBlank { null },
                                    ceIsoRequired = requiresCertification || restrictedOnlineRisk,
                                    isActive = isActive,
                                    createdAt = product?.createdAt ?: now,
                                    updatedAt = now,
                                    lowStockThreshold = product?.lowStockThreshold ?: 10,
                                    unit = unit.trim().ifBlank { "Cái" },
                                    registrationNumber = registrationNumber.trim().ifBlank { null },
                                    riskClassification = riskClassification,
                                    requiresTechnicalConsultation = requiresConsultation || restrictedOnlineRisk,
                                    images = normalizedImages
                                ),
                                newImageFiles.toList()
                            )
                        },
                        enabled = isFormValid && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.padding(4.dp))
                        }
                        Text(if (isEditing) "Cap nhat" else "Tao san pham")
                    }
                }
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun SelectedImageItem(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onRemove: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onRemove, enabled = enabled) {
                Text("Bỏ")
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "${bytes} B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.1f KB", kb)
    val mb = kb / 1024.0
    return String.format("%.1f MB", mb)
}

@Composable
private fun RiskClassificationDropdown(
    selectedRiskClassification: RiskClassification,
    onRiskClassificationSelected: (RiskClassification) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedRiskClassification.displayName,
            onValueChange = { },
            label = { Text("Phân loại rủi ro TTBYT") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = enabled
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled) { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            RiskClassification.entries.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.displayName) },
                    onClick = {
                        onRiskClassificationSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryDropdown(
    categories: List<ProductCategory>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var groupExpanded by remember { mutableStateOf(false) }
    var childExpanded by remember { mutableStateOf(false) }
    val groups = remember(categories) { topLevelProductCategories(categories) }
    val selectedGroupId = selectedCategoryGroupId(categories, selectedCategory)
    val selectedGroup = groups.firstOrNull { it.id == selectedGroupId }
    val children = remember(categories, selectedGroupId) {
        childProductCategories(categories, selectedGroupId)
    }
    val selectedChild = children.firstOrNull { it.id == selectedCategory }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedGroup?.displayName.orEmpty(),
                onValueChange = { },
                readOnly = true,
                label = { Text("Nhóm danh mục *") },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                isError = selectedCategory == null,
                trailingIcon = {
                    if (enabled) {
                        Text("▼", modifier = Modifier.clickable { groupExpanded = !groupExpanded })
                    }
                }
            )

            DropdownMenu(
                expanded = groupExpanded,
                onDismissRequest = { groupExpanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = 320.dp)
            ) {
                if (groups.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Không có nhóm danh mục nào") },
                        onClick = { }
                    )
                } else {
                    groups.forEach { category ->
                        DropdownMenuItem(
                text = { Text(category.displayName) },
                onClick = {
                    val firstChild = childProductCategories(categories, category.id).firstOrNull()
                    onCategorySelected(firstChild?.id ?: category.id)
                    groupExpanded = false
                }
            )
                    }
                }
            }
        }

        if (selectedGroup != null && children.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedChild?.displayName ?: if (selectedCategory == selectedGroup.id) "Nhóm chung: ${selectedGroup.displayName}" else "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Danh mục con") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    trailingIcon = {
                        if (enabled) {
                            Text("▼", modifier = Modifier.clickable { childExpanded = !childExpanded })
                        }
                    },
                    supportingText = {
                        val path = categoryDisplayPath(categories, selectedCategory)
                        if (path.isNotBlank()) Text(path)
                    }
                )

                DropdownMenu(
                    expanded = childExpanded,
                    onDismissRequest = { childExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .heightIn(max = 320.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Nhóm chung: ${selectedGroup.displayName}") },
                        onClick = {
                            onCategorySelected(selectedGroup.id)
                            childExpanded = false
                        }
                    )
                    children.forEach { child ->
                        DropdownMenuItem(
                            text = { Text(child.displayName) },
                            onClick = {
                                onCategorySelected(child.id)
                                childExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
