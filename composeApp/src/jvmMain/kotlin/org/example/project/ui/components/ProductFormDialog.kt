package org.example.project.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.benasher44.uuid.uuid4
import org.example.project.data.models.Product
import org.example.project.data.models.ProductCategory
import org.example.project.data.models.ProductCertificate
import org.example.project.data.models.ProductImage
import org.example.project.data.models.RiskClassification
import org.example.project.data.models.categoryDisplayPath
import org.example.project.data.models.childProductCategories
import org.example.project.data.models.selectedCategoryGroupId
import org.example.project.data.models.topLevelProductCategories
import org.example.project.data.repositories.UploadedProductAsset
import org.example.project.utils.openFileChooser
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.io.File
import java.net.URI

@Composable
fun ProductFormDialog(
    product: Product? = null,
    categories: List<ProductCategory>,
    onSave: (Product, List<File>) -> Unit,
    onCancel: () -> Unit,
    onUploadCertificate: (suspend (File) -> Result<UploadedProductAsset>)? = null,
    isCreating: Boolean = false,
    isUpdating: Boolean = false
) {
    val isEditing = product != null
    val title = if (isEditing) "Cập nhật sản phẩm" else "Thêm sản phẩm mới"
    val isLoading = isCreating || isUpdating
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf(product?.name ?: "") }
    var shortDescription by remember { mutableStateOf(product?.shortDescription ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var originalPrice by remember { mutableStateOf(product?.originalPrice?.toString() ?: "") }
    var importPrice by remember { mutableStateOf(product?.importPrice?.toString() ?: "") }
    var rewardPoints by remember { mutableStateOf(product?.rewardPoints?.toString() ?: "0") }
    var quantity by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "") }
    var selectedCategoryId by remember { mutableStateOf(product?.categoryId?.takeIf { it.isNotBlank() }) }
    var brand by remember { mutableStateOf(product?.brand ?: "") }
    var manufacturer by remember { mutableStateOf(product?.manufacturer ?: "") }
    var origin by remember { mutableStateOf(product?.origin ?: "") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var unit by remember { mutableStateOf(product?.unit ?: "Cái") }
    var mfgDate by remember { mutableStateOf(product?.mfgDate ?: "") }
    var expDate by remember { mutableStateOf(product?.expDate ?: "") }
    var registrationNumber by remember { mutableStateOf(product?.registrationNumber ?: "") }
    var riskClassification by remember { mutableStateOf(product?.riskClassification ?: RiskClassification.A) }
    var isActive by remember { mutableStateOf(product?.isActive ?: true) }
    val existingImages = remember(product?.id) {
        mutableStateListOf<ProductImage>().apply {
            addAll(product?.images?.sortedBy { it.sortOrder } ?: emptyList())
        }
    }
    val newImageFiles = remember(product?.id) { mutableStateListOf<File>() }
    val certificates = remember(product?.id) {
        mutableStateListOf<ProductCertificate>().apply {
            addAll(product?.certificates ?: emptyList())
        }
    }
    var certificateError by remember(product?.id) { mutableStateOf<String?>(null) }
    var isCertificateUploading by remember(product?.id) { mutableStateOf(false) }

    val parsedPrice = price.toDoubleOrNull()
    val parsedOriginalPrice = originalPrice.toDoubleOrNull()
    val parsedImportPrice = importPrice.toDoubleOrNull()
    val parsedRewardPoints = rewardPoints.toIntOrNull()
    val parsedQuantity = quantity.toIntOrNull()
    val restrictedOnlineRisk = riskClassification == RiskClassification.C || riskClassification == RiskClassification.D
    val isOriginalPriceValid = originalPrice.isBlank() || (parsedOriginalPrice != null && parsedPrice != null && parsedOriginalPrice >= parsedPrice)
    val isImportPriceValid = importPrice.isBlank() || (parsedImportPrice != null && parsedImportPrice >= 0.0)
    val isRewardPointsValid = rewardPoints.isBlank() || (parsedRewardPoints != null && parsedRewardPoints >= 0)
    val isQuantityValid = quantity.isBlank() || (parsedQuantity != null && parsedQuantity >= 0)
    val isFormValid = name.isNotBlank() &&
        selectedCategoryId != null &&
        parsedPrice != null &&
        parsedPrice > 0.0 &&
        isOriginalPriceValid &&
        isImportPriceValid &&
        isRewardPointsValid &&
        isQuantityValid

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
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
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
                    FormSection(title = "Thông tin bán hàng") {
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
                            value = shortDescription,
                            onValueChange = { shortDescription = it },
                            label = { Text("Mô tả ngắn hiển thị ngoài danh sách") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isLoading
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Mô tả chi tiết") },
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
                                label = { Text("Giá bán * (VND)") },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading,
                                isError = parsedPrice == null,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )

                            OutlinedTextField(
                                value = originalPrice,
                                onValueChange = { originalPrice = it },
                                label = { Text("Giá gốc/giá niêm yết") },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading,
                                isError = !isOriginalPriceValid,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }

                        if (!isOriginalPriceValid) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Giá gốc phải lớn hơn hoặc bằng giá bán",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = rewardPoints,
                            onValueChange = { rewardPoints = it.filter { char -> char.isDigit() } },
                            label = { Text("Điểm thưởng cộng sau khi mua") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            isError = !isRewardPointsValid,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            supportingText = { Text("Điểm này cộng khi đơn hoàn tất. Quy đổi dùng điểm: 1 điểm = 1đ.") }
                        )

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
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    FormSection(title = "Kho & giá vốn") {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Số lượng tồn kho") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            isError = !isQuantityValid,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = importPrice,
                                onValueChange = { importPrice = it },
                                label = { Text("Giá nhập") },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading,
                                isError = !isImportPriceValid,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )

                            OutlinedTextField(
                                value = mfgDate,
                                onValueChange = { mfgDate = it },
                                label = { Text("Ngày SX (yyyy-MM-dd)") },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            )

                            OutlinedTextField(
                                value = expDate,
                                onValueChange = { expDate = it },
                                label = { Text("Ngày HSD (yyyy-MM-dd)") },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    FormSection(title = "Hồ sơ y tế") {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Giữ vừa đủ dữ liệu cho bán hàng: phân loại A/B/C/D, hồ sơ lưu hành, thương hiệu, nhà sản xuất, xuất xứ và trạng thái kinh doanh.",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }


                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Thương hiệu") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = manufacturer,
                                onValueChange = { manufacturer = it },
                                label = { Text("Nhà sản xuất") },
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
                            onRiskClassificationSelected = { riskClassification = it },
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
                                    text = "Loại ${riskClassification.value} chỉ hiển thị để tư vấn. User không thể đặt online, cần tư vấn/ký kết tại Medstore.",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
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

                    FormSection(title = "Giấy tờ & chứng minh") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Đang có ${certificates.size} file giấy tờ của sản phẩm",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = {
                                    val selectedFile = openFileChooser(
                                        title = "Chọn giấy tờ chứng minh",
                                        allowedExtensions = listOf(".jpg", ".jpeg", ".png", ".webp", ".pdf"),
                                        allowMultiple = false
                                    ).firstOrNull() ?: return@Button

                                    val upload = onUploadCertificate
                                    if (upload == null) {
                                        certificateError = "Chưa cấu hình upload giấy tờ"
                                        return@Button
                                    }

                                    coroutineScope.launch {
                                        isCertificateUploading = true
                                        certificateError = null
                                        upload(selectedFile)
                                            .onSuccess { asset ->
                                                certificates.add(asset.toProductCertificate(selectedFile, manufacturer))
                                            }
                                            .onFailure {
                                                certificateError = it.message ?: "Không thể upload giấy tờ"
                                            }
                                        isCertificateUploading = false
                                    }
                                },
                                enabled = !isLoading && !isCertificateUploading
                            ) {
                                if (isCertificateUploading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.padding(4.dp))
                                }
                                Text("Chọn ảnh/PDF")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        certificateError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (certificates.isEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Chưa có giấy tờ. Có thể upload ảnh hoặc PDF; file PDF sẽ được backend lưu local để Android/Desktop mở ổn định.",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            certificates.forEachIndexed { index, certificate ->
                                SelectedCertificateItem(
                                    title = certificate.name.ifBlank { "Giấy tờ ${index + 1}" },
                                    subtitle = "${certificate.fileType.ifBlank { "FILE" }} • ${certificate.issuer ?: "Chưa có đơn vị cấp"}",
                                    enabled = !isLoading && !isCertificateUploading,
                                    onOpen = { openExternalUrl(certificate.fileUrl) },
                                    onRemove = { certificates.remove(certificate) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    FormSection(title = "Ảnh sản phẩm") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Đang có ${existingImages.size + newImageFiles.size} ảnh gắn với sản phẩm",
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
                                    text = "Chưa có ảnh nào. File mới sẽ được upload lên backend, backend đẩy lên Cloudinary rồi lưu link vào DB.",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            existingImages.forEachIndexed { index, image ->
                                SelectedImageItem(
                                    title = "Ảnh đã lưu ${index + 1}",
                                    subtitle = image.url,
                                    enabled = !isLoading,
                                    onRemove = { existingImages.remove(image) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            newImageFiles.forEachIndexed { index, file ->
                                SelectedImageItem(
                                    title = "Ảnh mới ${index + 1}",
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
                                    shortDescription = shortDescription.trim().ifBlank { null },
                                    description = description.trim(),
                                    price = parsedPrice!!,
                                    originalPrice = parsedOriginalPrice,
                                    importPrice = parsedImportPrice,
                                    rewardPoints = parsedRewardPoints ?: 0,
                                    categoryId = selectedCategoryId!!,
                                    stockQuantity = parsedQuantity ?: 0,
                                    mfgDate = mfgDate.trim().ifBlank { null },
                                    expDate = expDate.trim().ifBlank { null },
                                    manufacturer = manufacturer.trim(),
                                    brand = brand.trim(),
                                    origin = origin.trim(),
                                    sku = sku.trim().ifBlank { null },
                                    isActive = isActive,
                                    createdAt = product?.createdAt ?: now,
                                    updatedAt = now,
                                    lowStockThreshold = product?.lowStockThreshold ?: 10,
                                    unit = unit.trim().ifBlank { "Cái" },
                                    registrationNumber = registrationNumber.trim().ifBlank { null },
                                    riskClassification = riskClassification,
                                    images = normalizedImages,
                                    certificates = certificates.toList()
                                ),
                                newImageFiles.toList()
                            )
                        },
                        enabled = isFormValid && !isLoading && !isCertificateUploading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.padding(4.dp))
                        }
                        Text(if (isEditing) "Cập nhật" else "Tạo sản phẩm")
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

@Composable
private fun SelectedCertificateItem(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onOpen: () -> Unit,
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
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onOpen, enabled = enabled) {
                Text("Mở")
            }
            TextButton(onClick = onRemove, enabled = enabled) {
                Text("Xóa")
            }
        }
    }
}

private fun UploadedProductAsset.toProductCertificate(file: File, issuer: String): ProductCertificate {
    val normalizedFileType = mediaType.ifBlank {
        if (file.extension.equals("pdf", ignoreCase = true)) "PDF" else "IMAGE"
    }.uppercase()
    return ProductCertificate(
        type = "MOH_LICENSE",
        name = file.nameWithoutExtension.ifBlank { "Giấy tờ sản phẩm" },
        fileUrl = url,
        fileType = normalizedFileType,
        publicId = publicId,
        resourceType = resourceType.ifBlank { if (normalizedFileType == "PDF") "raw" else "image" },
        thumbnailUrl = if (normalizedFileType == "IMAGE") url else null,
        issuer = issuer.ifBlank { null },
        isActive = true
    )
}

private fun openExternalUrl(url: String) {
    runCatching {
        if (url.isNotBlank() && Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI(url))
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
