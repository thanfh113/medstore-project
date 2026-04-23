package org.example.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.models.ProductCategory
import org.example.project.data.models.DiseaseCategory
import org.example.project.data.repositories.UploadedProductAsset
import org.example.project.utils.openFileChooser
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState

/**
 * Data class for collecting product form data across all 5 groups
 * Mapped to 5 main database tables:
 * Group 1 -> products table
 * Group 2 -> products + product_diseases tables
 * Group 3 -> products + product_images tables
 * Group 4 -> product_batches table
 * Group 5 -> product_certificates table
 */
data class CompleteProductFormData(
    // Group 1: Basic Information (Thông tin cơ bản)
    val productName: String = "",
    val sku: String = "",
    val categoryId: String = "",
    val unit: String = "Hộp",
    val brand: String = "",
    val manufacturer: String = "",
    val origin: String = "",
    val shortDescription: String = "",
    val fullDescription: String = "",
    val targetAudience: String = "ALL",
    
    // Group 2: Healthcare Specific (Đặc thù Y tế)
    val registrationNumber: String = "",
    val riskClassification: String = "A",
    val requiresConsultation: Boolean = false,
    val requiresCertification: Boolean = false,
    val diseaseIds: List<String> = emptyList(),
    
    // Group 3: Pricing & Images (Giá bán & Hình ảnh)
    val price: Double = 0.0,
    val originalPrice: Double? = null,
    val discountPct: Int = 0,
    val rewardPoints: Int = 0,
    val productImages: List<ProductImageData> = emptyList(),
    
    // Group 4: Batch & Inventory (Quản lý Lô hàng & Tồn kho)
    val firstBatchData: BatchData? = null,
    
    // Group 5: Certificates (Chứng từ kèm theo)
    val certificates: List<CertificateData> = emptyList()
)

data class ProductImageData(
    val url: String = "",
    val mediaType: String = "IMAGE",
    val publicId: String? = null,
    val sortOrder: Int = 0
)

data class BatchData(
    val lotNumber: String = "",
    val mfgDate: String = "",
    val expDate: String = "",
    val quantityOnHand: Int = 0,
    val importPrice: Double = 0.0
)

data class CertificateData(
    val type: String = "",
    val name: String = "",
    val fileUrl: String = "",
    val publicId: String? = null,
    val issuer: String = "",
    val issueDate: String = "",
    val expireDate: String = ""
)

@Composable
fun CompleteProductFormStepper(
    categories: List<ProductCategory> = emptyList(),
    diseases: List<DiseaseCategory> = emptyList(),
    onUploadProductMedia: suspend (File, String) -> Result<UploadedProductAsset>,
    onUploadCertificate: suspend (File) -> Result<UploadedProductAsset>,
    onDeleteUploadedAsset: suspend (String, String) -> Result<Unit>,
    onSave: (CompleteProductFormData) -> Unit,
    onCancel: () -> Unit,
    initialData: CompleteProductFormData = CompleteProductFormData()
) {
    var formData by remember { mutableStateOf(initialData) }
    var currentStep by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    var isCleaningUp by remember { mutableStateOf(false) }
    var cleanupError by remember { mutableStateOf<String?>(null) }
    val wizardSteps = listOf(
        "Thông tin chung",
        "Pháp ly y tế",
        "Giá và hình ảnh",
        "Khởi tạo lô"
    )

    val legacySteps = listOf(
        "Thông tin cơ bản",
        "Đặc thù Y tế",
        "Giá & Hình ảnh",
        "Lô hàng & Tồn kho",
        "Chứng chỉ"
    )
    val steps = wizardSteps

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Step indicator
        StepIndicator(currentStep, wizardSteps)
        
        Spacer(modifier = Modifier.height(24.dp))

        cleanupError?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Step content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (currentStep) {
                0 -> Step1BasicInfo(
                    data = formData,
                    categories = categories,
                    onUpdate = { formData = it }
                )
                1 -> Step2HealthcareSpecific(
                    data = formData,
                    diseases = diseases,
                    onUploadCertificate = onUploadCertificate,
                    onDeleteUploadedAsset = onDeleteUploadedAsset,
                    onUpdate = { formData = it }
                )
                2 -> Step3PricingImages(
                    data = formData,
                    onUploadProductMedia = onUploadProductMedia,
                    onDeleteUploadedAsset = onDeleteUploadedAsset,
                    onUpdate = { formData = it }
                )
                3 -> Step4BatchInventory(
                    data = formData,
                    onUpdate = { formData = it }
                )
                4 -> Step5Certificates(
                    data = formData,
                    onUpdate = { formData = it }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { if (currentStep > 0) currentStep-- },
                enabled = currentStep > 0 && !isCleaningUp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quay lại")
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = {
                        scope.launch {
                            isCleaningUp = true
                            cleanupError = null
                            val cleanupResult = cleanupWizardUploads(formData, onDeleteUploadedAsset)
                            isCleaningUp = false
                            cleanupResult.fold(
                                onSuccess = { onCancel() },
                                onFailure = { error ->
                                    cleanupError = error.message ?: "Khong the don dep tep da tai len"
                                }
                            )
                        }
                    },
                    enabled = !isCleaningUp
                ) {
                    Text("Hủy")
                }
                
                Button(
                    onClick = {
                        cleanupError = null
                        if (currentStep < wizardSteps.size - 1) {
                            if (isStepValid(currentStep, formData)) {
                                currentStep++
                            }
                        } else {
                            if (isFormComplete(formData)) {
                                onSave(formData)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !isCleaningUp
                ) {
                    Text(if (currentStep == steps.size - 1) "Lưu sản phẩm" else "Tiếp theo")
                    if (currentStep < wizardSteps.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, steps: List<String>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, step ->
                StepCircle(
                    number = index + 1,
                    isActive = index == currentStep,
                    isCompleted = index < currentStep,
                    modifier = Modifier.weight(1f)
                )
                
                if (index < steps.size - 1) {
                    Divider(
                        modifier = Modifier
                            .weight(0.5f)
                            .height(2.dp),
                        color = if (index < currentStep)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.LightGray
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = steps[currentStep],
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun StepCircle(
    number: Int,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isCompleted -> MaterialTheme.colorScheme.primary
        isActive -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.LightGray
    }
    
    val contentColor = when {
        isCompleted -> Color.White
        isActive -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> Color.Gray
    }
    
    Box(
        modifier = modifier
            .size(40.dp)
            .background(backgroundColor, shape = RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = number.toString(),
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

// ============================================================================
// STEP 1: BASIC INFORMATION (Thông tin cơ bản)
// ============================================================================
@Composable
private fun Step1BasicInfo(
    data: CompleteProductFormData,
    categories: List<ProductCategory>,
    onUpdate: (CompleteProductFormData) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Bước 1: Thông tin cơ bản sản phẩm",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Tên sản phẩm (Required)
        item {
            FormField(
                label = "Tên sản phẩm *",
                value = data.productName,
                onValueChange = { onUpdate(data.copy(productName = it)) },
                placeholder = "VD: Máy đo huyết áp OMRON HEM-7120",
                singleLine = true
            )
        }
        
        // SKU
        item {
            FormField(
                label = "Mã sản phẩm (SKU)",
                value = data.sku,
                onValueChange = { onUpdate(data.copy(sku = it)) },
                placeholder = "VD: OMR-HEM-7120",
                singleLine = true
            )
        }
        
        // Category
        item {
            DropdownField(
                label = "Danh mục *",
                value = categories.find { it.id == data.categoryId }?.displayName ?: "Chọn danh mục",
                options = categories.map { it.displayName to it.id },
                onSelect = { selectedId -> onUpdate(data.copy(categoryId = selectedId)) }
            )
        }
        
        // Unit (Required)
        item {
            DropdownField(
                label = "Đơn vị tính *",
                value = data.unit,
                options = listOf(
                    "Hộp" to "Hộp",
                    "Cái" to "Cái",
                    "Vỉ" to "Vỉ",
                    "Cuộn" to "Cuộn",
                    "Viên" to "Viên",
                    "Chai" to "Chai",
                    "Gói" to "Gói"
                ),
                onSelect = { onUpdate(data.copy(unit = it)) }
            )
        }
        
        // Brand
        item {
            FormField(
                label = "Thương hiệu",
                value = data.brand,
                onValueChange = { onUpdate(data.copy(brand = it)) },
                placeholder = "VD: OMRON, 3M",
                singleLine = true
            )
        }
        
        // Manufacturer
        item {
            FormField(
                label = "Nhà sản xuất",
                value = data.manufacturer,
                onValueChange = { onUpdate(data.copy(manufacturer = it)) },
                placeholder = "VD: OMRON Healthcare Co., Ltd",
                singleLine = true
            )
        }
        
        // Origin
        item {
            FormField(
                label = "Xuất xứ",
                value = data.origin,
                onValueChange = { onUpdate(data.copy(origin = it)) },
                placeholder = "VD: Nhật Bản, Việt Nam, Hàn Quốc",
                singleLine = true
            )
        }
        
        // Short Description
        item {
            FormField(
                label = "Mô tả ngắn",
                value = data.shortDescription,
                onValueChange = { onUpdate(data.copy(shortDescription = it)) },
                placeholder = "Mô tả ngắn gọn về sản phẩm (tối đa 500 ký tự)",
                maxLines = 2
            )
        }
        
        // Full Description
        item {
            FormField(
                label = "Mô tả chi tiết",
                value = data.fullDescription,
                onValueChange = { onUpdate(data.copy(fullDescription = it)) },
                placeholder = "Mô tả chi tiết, đặc điểm, lợi ích của sản phẩm",
                maxLines = 4
            )
        }
        
        // Target Audience
        item {
            DropdownField(
                label = "Đối tượng sử dụng",
                value = when (data.targetAudience) {
                    "ALL" -> "Tất cả (Mặc định)"
                    "CHILDREN" -> "Trẻ em"
                    "ELDERLY" -> "Người già"
                    else -> data.targetAudience
                },
                options = listOf(
                    "Tất cả (Mặc định)" to "ALL",
                    "Trẻ em" to "CHILDREN",
                    "Người lớn" to "ADULT",
                    "Người già" to "ELDERLY"
                ),
                onSelect = { onUpdate(data.copy(targetAudience = it)) }
            )
        }
    }
}

// ============================================================================
// STEP 2: HEALTHCARE SPECIFIC (Đặc thù Y tế)
// ============================================================================
@Composable
private fun LegacyStep2HealthcareSpecific(
    data: CompleteProductFormData,
    diseases: List<DiseaseCategory>,
    onUpdate: (CompleteProductFormData) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Bước 2: Đặc thù Y tế",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Registration Number
        item {
            FormField(
                label = "Số đăng ký lưu hành",
                value = data.registrationNumber,
                onValueChange = { onUpdate(data.copy(registrationNumber = it)) },
                placeholder = "VD: MD-001-2024",
                singleLine = true
            )
        }
        
        // Risk Classification
        item {
            DropdownField(
                label = "Phân loại rủi ro",
                value = data.riskClassification,
                options = listOf(
                    "A - Rủi ro thấp" to "A",
                    "B - Rủi ro trung bình" to "B",
                    "C - Rủi ro cao" to "C",
                    "D - Rủi ro rất cao" to "D"
                ),
                onSelect = { onUpdate(data.copy(riskClassification = it)) }
            )
        }
        
        // Requires Consultation
        item {
            CheckboxField(
                label = "Cần tư vấn trước khi mua",
                checked = data.requiresConsultation,
                onCheckedChange = { onUpdate(data.copy(requiresConsultation = it)) },
                description = "Khách hàng cần tư vấn với Dược sĩ hoặc AI chatbot"
            )
        }
        
        // Requires Certification
        item {
            CheckboxField(
                label = "Yêu cầu chứng chỉ/Giấy phép",
                checked = data.requiresCertification,
                onCheckedChange = { onUpdate(data.copy(requiresCertification = it)) },
                description = "Cần cung cấp chứng chỉ/giấy phép hành nghề khi mua"
            )
        }
        
        // Disease Categories (Multi-select)
        item {
            Text(
                "Nhóm bệnh lý liên quan",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        items(diseases.size) { index ->
            val disease = diseases[index]
            val isSelected = data.diseaseIds.contains(disease.id)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val newList = if (isSelected) {
                            data.diseaseIds.filterNot { it == disease.id }
                        } else {
                            data.diseaseIds + disease.id
                        }
                        onUpdate(data.copy(diseaseIds = newList))
                    }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { checked ->
                        val newList = if (checked) {
                            data.diseaseIds + disease.id
                        } else {
                            data.diseaseIds.filterNot { it == disease.id }
                        }
                        onUpdate(data.copy(diseaseIds = newList))
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(disease.name)
            }
        }
    }
}

// ============================================================================
// STEP 3: PRICING & IMAGES (Giá bán & Hình ảnh)
// ============================================================================
@Composable
private fun Step2HealthcareSpecific(
    data: CompleteProductFormData,
    diseases: List<DiseaseCategory>,
    onUploadCertificate: suspend (File) -> Result<UploadedProductAsset>,
    onDeleteUploadedAsset: suspend (String, String) -> Result<Unit>,
    onUpdate: (CompleteProductFormData) -> Unit
) {
    val scope = rememberCoroutineScope()
    val latestData by rememberUpdatedState(data)
    val license = data.certificates.firstOrNull()
    var isUploading by remember { mutableStateOf(false) }
    var isDeletingFile by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Buoc 2: Phap ly y te va chung tu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            FormField(
                label = "So DKLH / So cong bo",
                value = data.registrationNumber,
                onValueChange = { onUpdate(data.copy(registrationNumber = it)) },
                placeholder = "VD: 220001234/PCBA-HN",
                singleLine = true
            )
        }

        item {
            DropdownField(
                label = "Phan loai rui ro",
                value = data.riskClassification,
                options = listOf(
                    "Loai A" to "A",
                    "Loai B" to "B",
                    "Loai C" to "C",
                    "Loai D" to "D"
                ),
                onSelect = { onUpdate(data.copy(riskClassification = it)) }
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    "Giay phep Bo Y Te se duoc luu voi type co dinh la MOH_LICENSE.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val file = openFileChooser(
                            title = "Chon giay phep Bo Y Te",
                            allowedExtensions = listOf(".jpg", ".jpeg", ".png", ".pdf", ".heic"),
                            allowMultiple = false
                        ).firstOrNull() ?: return@Button

                        scope.launch {
                            isUploading = true
                            uploadError = null
                            val previousLicense = latestData.certificates.firstOrNull()
                            onUploadCertificate(file).fold(
                                onSuccess = { uploaded ->
                                    val previousPublicId = previousLicense?.publicId
                                        ?.takeIf { it.isNotBlank() && it != uploaded.publicId }
                                    previousPublicId?.let { publicId ->
                                        onDeleteUploadedAsset(publicId, "image").onFailure { error ->
                                            uploadError = error.message ?: "Da thay file moi nhung khong the xoa file cu"
                                        }
                                    }

                                    val currentData = latestData
                                    val currentLicense = currentData.certificates.firstOrNull()
                                    onUpdate(
                                        currentData.withPrimaryCertificate(
                                            (currentLicense ?: CertificateData(type = "MOH_LICENSE")).copy(
                                                type = "MOH_LICENSE",
                                                name = (currentLicense?.name?.takeIf { it.isNotBlank() } ?: file.nameWithoutExtension),
                                                fileUrl = uploaded.url,
                                                publicId = uploaded.publicId
                                            )
                                        )
                                    )
                                },
                                onFailure = { error ->
                                    uploadError = error.message ?: "Khong the upload giay phep"
                                }
                            )
                            isUploading = false
                        }
                    },
                    enabled = !isUploading && !isDeletingFile
                ) {
                    Text(if (isUploading) "Dang upload..." else "Chon file giay phep")
                }

                if (!license?.fileUrl.isNullOrBlank()) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val currentData = latestData
                                val currentLicense = currentData.certificates.firstOrNull() ?: return@launch
                                isDeletingFile = true
                                uploadError = null

                                val cleanupResult = currentLicense.publicId
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { onDeleteUploadedAsset(it, "image") }
                                    ?: Result.success(Unit)

                                isDeletingFile = false
                                cleanupResult.fold(
                                    onSuccess = {
                                        onUpdate(
                                            currentData.withPrimaryCertificate(
                                                currentLicense.copy(
                                                    fileUrl = "",
                                                    publicId = null
                                                )
                                            )
                                        )
                                    },
                                    onFailure = { error ->
                                        uploadError = error.message ?: "Khong the xoa file giay phep da tai len"
                                    }
                                )
                            }
                        },
                        enabled = !isUploading && !isDeletingFile
                    ) {
                        Text(if (isDeletingFile) "Dang xoa..." else "Xoa file")
                    }
                }
            }
        }

        item {
            FormField(
                label = "Ten giay phep Bo Y Te",
                value = license?.name.orEmpty(),
                onValueChange = { value ->
                    onUpdate(
                        data.withPrimaryCertificate(
                            (license ?: CertificateData(type = "MOH_LICENSE")).copy(
                                type = "MOH_LICENSE",
                                name = value
                            )
                        )
                    )
                },
                placeholder = "VD: Ban cong bo tieu chuan loai A",
                singleLine = true
            )
        }

        item {
            FormField(
                label = "File dinh kem giay phep (URL)",
                value = license?.fileUrl.orEmpty(),
                onValueChange = { value ->
                    onUpdate(
                        data.withPrimaryCertificate(
                            (license ?: CertificateData(type = "MOH_LICENSE")).copy(
                                type = "MOH_LICENSE",
                                fileUrl = value
                            )
                        )
                    )
                },
                placeholder = "https://.../giay-phep.pdf",
                singleLine = true
            )
        }

        uploadError?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun LegacyStep3PricingImages(
    data: CompleteProductFormData,
    onUpdate: (CompleteProductFormData) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Bước 3: Giá bán & Hình ảnh",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Price (Required)
        item {
            FormField(
                label = "Giá bán (VND) *",
                value = if (data.price == 0.0) "" else data.price.toString(),
                onValueChange = {
                    onUpdate(data.copy(price = it.toDoubleOrNull() ?: 0.0))
                },
                placeholder = "VD: 850000",
                keyboardType = KeyboardType.Decimal,
                singleLine = true
            )
        }
        
        // Original Price
        item {
            FormField(
                label = "Giá gốc (VND)",
                value = data.originalPrice?.toString() ?: "",
                onValueChange = {
                    onUpdate(data.copy(originalPrice = it.toDoubleOrNull()))
                },
                placeholder = "VD: 950000",
                keyboardType = KeyboardType.Decimal,
                singleLine = true
            )
        }
        
        // Discount Percentage
        item {
            FormField(
                label = "Tỉ lệ giảm giá (%)",
                value = if (data.discountPct == 0) "" else data.discountPct.toString(),
                onValueChange = {
                    onUpdate(data.copy(discountPct = it.toIntOrNull() ?: 0))
                },
                placeholder = "VD: 10",
                keyboardType = KeyboardType.Number,
                singleLine = true
            )
        }
        
        // Reward Points
        item {
            FormField(
                label = "Điểm thưởng (Points)",
                value = if (data.rewardPoints == 0) "" else data.rewardPoints.toString(),
                onValueChange = {
                    onUpdate(data.copy(rewardPoints = it.toIntOrNull() ?: 0))
                },
                placeholder = "VD: 50",
                keyboardType = KeyboardType.Number,
                singleLine = true
            )
        }
        
        item {
            Divider()
        }
        
        // Images section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Hình ảnh/Video",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Button(
                    onClick = { /* TODO: Add image upload */ },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thêm ảnh")
                }
            }
        }
        
        if (data.productImages.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        "Chưa có ảnh. Click nút 'Thêm ảnh' để tải lên",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            items(data.productImages.size) { index ->
                Text("Ảnh ${index + 1}: ${data.productImages[index].mediaType}")
            }
        }
    }
}

// ============================================================================
// STEP 4: BATCH & INVENTORY (Quản lý Lô hàng & Tồn kho)
// ============================================================================
@Composable
private fun Step3PricingImages(
    data: CompleteProductFormData,
    onUploadProductMedia: suspend (File, String) -> Result<UploadedProductAsset>,
    onDeleteUploadedAsset: suspend (String, String) -> Result<Unit>,
    onUpdate: (CompleteProductFormData) -> Unit
) {
    val scope = rememberCoroutineScope()
    val latestData by rememberUpdatedState(data)
    var isUploading by remember { mutableStateOf(false) }
    var deletingImageIndex by remember { mutableStateOf<Int?>(null) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Buoc 3: Gia ban va hinh anh",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            FormField(
                label = "Gia ban thuc te (VND) *",
                value = if (data.price == 0.0) "" else data.price.toString(),
                onValueChange = { onUpdate(data.copy(price = it.toDoubleOrNull() ?: 0.0)) },
                placeholder = "VD: 850000",
                keyboardType = KeyboardType.Decimal,
                singleLine = true
            )
        }

        item {
            FormField(
                label = "Gia goc (VND)",
                value = data.originalPrice?.toString() ?: "",
                onValueChange = { onUpdate(data.copy(originalPrice = it.toDoubleOrNull())) },
                placeholder = "VD: 950000",
                keyboardType = KeyboardType.Decimal,
                singleLine = true
            )
        }

        item {
            FormField(
                label = "Ti le giam gia (%)",
                value = if (data.discountPct == 0) "" else data.discountPct.toString(),
                onValueChange = { onUpdate(data.copy(discountPct = it.toIntOrNull() ?: 0)) },
                placeholder = "VD: 10",
                keyboardType = KeyboardType.Number,
                singleLine = true
            )
        }

        item {
            FormField(
                label = "Diem thuong tich luy",
                value = if (data.rewardPoints == 0) "" else data.rewardPoints.toString(),
                onValueChange = { onUpdate(data.copy(rewardPoints = it.toIntOrNull() ?: 0)) },
                placeholder = "VD: 50",
                keyboardType = KeyboardType.Number,
                singleLine = true
            )
        }

        item {
            Divider()
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Danh sach hinh anh / video",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            onUpdate(
                                data.copy(
                                    productImages = reindexProductImages(
                                        data.productImages + ProductImageData(
                                            url = "",
                                            mediaType = "IMAGE",
                                            sortOrder = data.productImages.size
                                        )
                                    )
                                )
                            )
                        },
                        enabled = deletingImageIndex == null,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Them URL")
                    }

                    Button(
                        onClick = {
                            val files = openFileChooser(
                                title = "Chon anh / video san pham",
                                allowedExtensions = listOf(".jpg", ".jpeg", ".png", ".webp", ".heic", ".mp4", ".mov", ".avi", ".webm"),
                                allowMultiple = true
                            )
                            if (files.isEmpty()) return@Button

                            scope.launch {
                                isUploading = true
                                uploadError = null
                                val uploaded = mutableListOf<ProductImageData>()

                                files.forEachIndexed { _, file ->
                                    val mediaType = detectMediaType(file)
                                    val result = onUploadProductMedia(file, mediaType)
                                    result.fold(
                                        onSuccess = { asset ->
                                            uploaded += ProductImageData(
                                                url = asset.url,
                                                mediaType = asset.mediaType,
                                                publicId = asset.publicId
                                            )
                                        },
                                        onFailure = { error ->
                                            if (uploadError == null) {
                                                uploadError = error.message ?: "Khong the upload tep ${file.name}"
                                            }
                                        }
                                    )
                                }

                                if (uploaded.isNotEmpty()) {
                                    val currentData = latestData
                                    onUpdate(
                                        currentData.copy(
                                            productImages = reindexProductImages(currentData.productImages + uploaded)
                                        )
                                    )
                                }
                                isUploading = false
                            }
                        },
                        enabled = !isUploading && deletingImageIndex == null,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isUploading) "Dang tai..." else "Tai tep")
                    }
                }
            }
        }

        uploadError?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (data.productImages.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        "Chua co hinh anh nao. Them URL cho anh bia va cac anh phu neu can.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            items(data.productImages.size) { index ->
                val image = data.productImages[index]
                val isDeletingThisImage = deletingImageIndex == index
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (index == 0) "Anh bia" else "Anh phu ${index + 1}",
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        val currentData = latestData
                                        val currentImage = currentData.productImages.getOrNull(index) ?: return@launch
                                        deletingImageIndex = index
                                        uploadError = null

                                        val cleanupResult = currentImage.publicId
                                            ?.takeIf { it.isNotBlank() }
                                            ?.let {
                                                onDeleteUploadedAsset(
                                                    it,
                                                    resourceTypeForMedia(currentImage.mediaType)
                                                )
                                            }
                                            ?: Result.success(Unit)

                                        deletingImageIndex = null
                                        cleanupResult.fold(
                                            onSuccess = {
                                                val refreshedData = latestData
                                                val updated = refreshedData.productImages.toMutableList()
                                                val removalIndex = currentImage.publicId
                                                    ?.let { publicId ->
                                                        updated.indexOfFirst { it.publicId == publicId }
                                                    }
                                                    ?: index.takeIf { it in updated.indices }

                                                if (removalIndex != null && removalIndex >= 0) {
                                                    updated.removeAt(removalIndex)
                                                    onUpdate(
                                                        refreshedData.copy(
                                                            productImages = reindexProductImages(updated)
                                                        )
                                                    )
                                                }
                                            },
                                            onFailure = { error ->
                                                uploadError = error.message ?: "Khong the xoa tep da tai len"
                                            }
                                        )
                                    }
                                },
                                enabled = !isUploading && deletingImageIndex == null
                            ) {
                                Text(if (isDeletingThisImage) "Dang xoa..." else "Xoa")
                            }
                        }

                        FormField(
                            label = "Link tep",
                            value = image.url,
                            onValueChange = { value ->
                                val updated = data.productImages.toMutableList()
                                updated[index] = updated[index].copy(url = value)
                                onUpdate(data.copy(productImages = reindexProductImages(updated)))
                            },
                            placeholder = "https://.../image.jpg",
                            singleLine = true
                        )

                        DropdownField(
                            label = "Loai tep",
                            value = image.mediaType,
                            options = listOf(
                                "IMAGE" to "IMAGE",
                                "VIDEO" to "VIDEO"
                            ),
                            onSelect = { value ->
                                val updated = data.productImages.toMutableList()
                                updated[index] = updated[index].copy(mediaType = value)
                                onUpdate(data.copy(productImages = reindexProductImages(updated)))
                            }
                        )

                        if (index > 0) {
                            TextButton(
                                onClick = {
                                    val updated = data.productImages.toMutableList()
                                    val selected = updated.removeAt(index)
                                    updated.add(0, selected)
                                    onUpdate(data.copy(productImages = reindexProductImages(updated)))
                                },
                                enabled = deletingImageIndex == null
                            ) {
                                Text("Dat lam anh bia")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Step4BatchInventory(
    data: CompleteProductFormData,
    onUpdate: (CompleteProductFormData) -> Unit
) {
    val batchData = data.firstBatchData ?: BatchData()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Bước 4: Nhập lô hàng đầu tiên",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    "💡 Thay vì nhập tồn kho chung, bạn sẽ nhập theo từng Lô (Batch) của lần nhập hàng. Số lượng sẽ được cộng dồn vào tồn kho sản phẩm.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        // Lot Number
        item {
            FormField(
                label = "Số lô",
                value = batchData.lotNumber,
                onValueChange = {
                    onUpdate(data.copy(firstBatchData = batchData.copy(lotNumber = it)))
                },
                placeholder = "VD: LOT202404001",
                singleLine = true
            )
        }
        
        // Manufacturing Date - Using DatePickerField
        item {
            DatePickerField(
                label = "Ngày sản xuất",
                value = batchData.mfgDate,
                onValueChange = {
                    onUpdate(data.copy(firstBatchData = batchData.copy(mfgDate = it)))
                }
            )
        }
        
        // Expiry Date - Using DatePickerField
        item {
            DatePickerField(
                label = "Hạn sử dụng",
                value = batchData.expDate,
                onValueChange = {
                    onUpdate(data.copy(firstBatchData = batchData.copy(expDate = it)))
                }
            )
        }
        
        // Quantity (Required)
        item {
            FormField(
                label = "Số lượng nhập *",
                value = if (batchData.quantityOnHand == 0) "" else batchData.quantityOnHand.toString(),
                onValueChange = {
                    onUpdate(data.copy(
                        firstBatchData = batchData.copy(
                            quantityOnHand = it.toIntOrNull() ?: 0
                        )
                    ))
                },
                placeholder = "VD: 100",
                keyboardType = KeyboardType.Number,
                singleLine = true
            )
        }
        
        // Import Price
        item {
            FormField(
                label = "Giá nhập (VND)",
                value = if (batchData.importPrice == 0.0) "" else batchData.importPrice.toString(),
                onValueChange = {
                    onUpdate(data.copy(
                        firstBatchData = batchData.copy(
                            importPrice = it.toDoubleOrNull() ?: 0.0
                        )
                    ))
                },
                placeholder = "VD: 750000",
                keyboardType = KeyboardType.Decimal,
                singleLine = true
            )
        }
    }
}

// ============================================================================
// STEP 5: CERTIFICATES (Chứng từ kèm theo)
// ============================================================================
@Composable
private fun Step5Certificates(
    data: CompleteProductFormData,
    onUpdate: (CompleteProductFormData) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Bước 5: Chứng từ kèm theo (Tùy chọn)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (!data.requiresCertification) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text(
                        "✓ Sản phẩm này không yêu cầu chứng chỉ. Phần này là tùy chọn.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text(
                        "⚠️ Sản phẩm yêu cầu chứng chỉ. Vui lòng cung cấp giấy tờ liên quan.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Các chứng chỉ đã thêm: ${data.certificates.size}",
                    style = MaterialTheme.typography.labelMedium
                )
                Button(
                    onClick = { /* TODO: Add certificate */ },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thêm chứng chỉ")
                }
            }
        }
        
        if (data.certificates.isEmpty()) {
            item {
                Text(
                    "Chưa có chứng chỉ nào",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        } else {
            items(data.certificates.size) { index ->
                CertificateListItem(data.certificates[index])
            }
        }
    }
}

@Composable
private fun CertificateListItem(cert: CertificateData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                cert.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                "Loại: ${cert.type} | Cấp bởi: ${cert.issuer}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

// ============================================================================
// REUSABLE FORM COMPONENTS
// ============================================================================

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 12.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            maxLines = maxLines,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(value, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                Icon(Icons.Default.ExpandMore, contentDescription = null)
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                options.forEach { (displayText, value) ->
                    DropdownMenuItem(
                        text = { Text(displayText) },
                        onClick = {
                            onSelect(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckboxField(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String = ""
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (description.isNotEmpty()) {
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

private fun CompleteProductFormData.withPrimaryCertificate(certificate: CertificateData): CompleteProductFormData {
    val updated = certificates.toMutableList()
    if (updated.isEmpty()) {
        updated += certificate
    } else {
        updated[0] = certificate
    }
    return copy(certificates = updated)
}

private data class UploadedAssetCleanupTarget(
    val publicId: String,
    val resourceType: String
)

private suspend fun cleanupWizardUploads(
    formData: CompleteProductFormData,
    onDeleteUploadedAsset: suspend (String, String) -> Result<Unit>
): Result<Unit> {
    val assets = buildList {
        formData.certificates.forEach { certificate ->
            certificate.publicId
                ?.takeIf { it.isNotBlank() }
                ?.let { add(UploadedAssetCleanupTarget(it, "image")) }
        }
        formData.productImages.forEach { image ->
            image.publicId
                ?.takeIf { it.isNotBlank() }
                ?.let { add(UploadedAssetCleanupTarget(it, resourceTypeForMedia(image.mediaType))) }
        }
    }.distinctBy { "${it.resourceType}:${it.publicId}" }

    if (assets.isEmpty()) return Result.success(Unit)

    val failures = mutableListOf<String>()
    assets.forEach { asset ->
        onDeleteUploadedAsset(asset.publicId, asset.resourceType).onFailure { error ->
            failures += error.message ?: "Khong the xoa asset ${asset.publicId}"
        }
    }

    return if (failures.isEmpty()) {
        Result.success(Unit)
    } else {
        Result.failure(IllegalStateException(failures.first()))
    }
}

private fun reindexProductImages(images: List<ProductImageData>): List<ProductImageData> {
    return images.mapIndexed { index, image ->
        image.copy(
            mediaType = image.mediaType.ifBlank { "IMAGE" },
            sortOrder = index
        )
    }
}

private fun detectMediaType(file: File): String {
    return when (file.extension.lowercase()) {
        "mp4", "mov", "avi", "webm" -> "VIDEO"
        else -> "IMAGE"
    }
}

private fun resourceTypeForMedia(mediaType: String): String {
    return if (mediaType.equals("VIDEO", ignoreCase = true)) "video" else "image"
}

private fun isStepValid(step: Int, formData: CompleteProductFormData): Boolean {
    return when (step) {
        0 -> formData.productName.isNotBlank() && formData.categoryId.isNotBlank() && formData.unit.isNotBlank()
        1 -> {
            val certificate = formData.certificates.firstOrNull()
            val hasPartialCertificate = certificate != null &&
                (certificate.name.isNotBlank() xor certificate.fileUrl.isNotBlank())
            !hasPartialCertificate
        }
        2 -> formData.price > 0.0
        3 -> formData.firstBatchData?.quantityOnHand ?: 0 > 0
        else -> true
    }
}

private fun isFormComplete(formData: CompleteProductFormData): Boolean {
    return formData.productName.isNotBlank() &&
            formData.categoryId.isNotBlank() &&
            formData.unit.isNotBlank() &&
            formData.price > 0.0 &&
            (formData.firstBatchData?.quantityOnHand ?: 0) > 0
}

// ============================================================================
// REUSABLE COMPOSABLE: DatePickerField
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Parse current value if valid
    val currentDate = try {
        if (value.isNotBlank()) LocalDate.parse(value) else LocalDate.now()
    } catch (e: Exception) {
        LocalDate.now()
    }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            onValueChange(selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    OutlinedTextField(
        value = value,
        onValueChange = { /* Read-only via date picker */ },
        label = { Text(label) },
        placeholder = { Text("yyyy-MM-dd") },
        modifier = modifier.fillMaxWidth(),
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Chọn ngày")
            }
        },
        singleLine = true
    )
}
