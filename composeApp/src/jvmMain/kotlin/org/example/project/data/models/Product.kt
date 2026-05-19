package org.example.project.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ProductImage(
    val id: String? = null,
    val url: String,
    val publicId: String? = null,
    val sortOrder: Int = 0
)

@Serializable
data class ProductCertificate(
    val id: String? = null,
    val type: String = "MOH_LICENSE",
    val name: String,
    val fileUrl: String,
    val fileType: String = "IMAGE",
    val publicId: String? = null,
    val resourceType: String = "image",
    val thumbnailUrl: String? = null,
    val issuer: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class Product(
    val id: String,
    val name: String,
    val shortDescription: String? = null,
    val description: String,
    val price: Double,
    val originalPrice: Double? = null,
    val importPrice: Double? = null,
    val rewardPoints: Int = 0,
    val categoryId: String,
    val stockQuantity: Int,
    val mfgDate: String? = null,
    val expDate: String? = null,
    val inventoryNote: String? = null,
    val manufacturer: String,
    val brand: String = "",
    val origin: String = "",
    val sku: String? = null,
    val ceIsoRequired: Boolean,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String,
    val lowStockThreshold: Int = 10,
    val unit: String = "Cái",
    val registrationNumber: String? = null,
    val riskClassification: RiskClassification = RiskClassification.A,
    val requiresTechnicalConsultation: Boolean = false,
    val isFlashSale: Boolean = false,
    val flashSaleEnd: String? = null,
    val images: List<ProductImage> = emptyList(),
    val certificates: List<ProductCertificate> = emptyList()
)

@Serializable
enum class RiskClassification(val value: String, val displayName: String) {
    A("A", "Loại A"),
    B("B", "Loại B"),
    C("C", "Loại C"),
    D("D", "Loại D")
}

@Serializable
data class ProductCategory(
    val id: String,
    val name: String,
    val displayName: String,
    val parentId: String? = null,
    val icon: String? = null,
    val sortOrder: Int = 0
)

private val coreProductCategoryIds = listOf(
    "cat-supplies",
    "cat-bandage",
    "cat-device",
    "cat-protect",
    "cat-instrument",
    "cat-infection-control",
    "cat-therapy",
    "cat-lab"
)

fun topLevelProductCategories(categories: List<ProductCategory>): List<ProductCategory> {
    val coreGroups = categories.filter { it.id in coreProductCategoryIds }
    val decadeGroups = categories.filter { it.sortOrder > 0 && it.sortOrder % 10 == 0 }
    val groups = coreGroups.ifEmpty {
        decadeGroups.ifEmpty {
            categories.filter { it.parentId.isNullOrBlank() }
        }
    }
    return groups.sortedWith(compareBy<ProductCategory> {
        coreProductCategoryIds.indexOf(it.id).takeIf { index -> index >= 0 } ?: Int.MAX_VALUE
    }.thenBy { it.sortOrder }.thenBy { it.displayName })
}

fun childProductCategories(
    categories: List<ProductCategory>,
    parentId: String?
): List<ProductCategory> {
    if (parentId.isNullOrBlank()) return emptyList()
    return categories
        .filter { it.parentId == parentId }
        .sortedBy { it.displayName }
}

fun selectedCategoryGroupId(
    categories: List<ProductCategory>,
    selectedCategoryId: String?
): String? {
    val selected = categories.firstOrNull { it.id == selectedCategoryId } ?: return null
    return selected.parentId ?: selected.id
}

fun categoryDisplayPath(
    categories: List<ProductCategory>,
    categoryId: String?
): String {
    val selected = categories.firstOrNull { it.id == categoryId } ?: return ""
    val parent = selected.parentId?.let { parentId -> categories.firstOrNull { it.id == parentId } }
    return if (parent != null) {
        "${parent.displayName} / ${selected.displayName}"
    } else {
        selected.displayName
    }
}

fun productCategoryMatches(
    productCategoryId: String,
    selectedCategoryId: String?,
    categories: List<ProductCategory>
): Boolean {
    if (selectedCategoryId.isNullOrBlank()) return true
    if (productCategoryId == selectedCategoryId) return true

    val selected = categories.firstOrNull { it.id == selectedCategoryId } ?: return false
    if (!selected.parentId.isNullOrBlank()) return false

    return categories.any { it.parentId == selectedCategoryId && it.id == productCategoryId }
}

@Serializable
data class MedicalSpecialty(
    val id: String,
    val name: String,
    val displayName: String,
    val description: String,
    val icon: String
)

object MedicalSpecialties {
    val specialties = listOf(
        MedicalSpecialty("tim-mach", "cardiovascular", "Tim mạch", "Chuyên khoa tim mạch", "TIM"),
        MedicalSpecialty("ho-hap", "respiratory", "Hô hấp", "Chuyên khoa hô hấp", "HO"),
        MedicalSpecialty("tieu-hoa", "digestive", "Tiêu hóa", "Chuyên khoa tiêu hóa", "TH"),
        MedicalSpecialty("than-kinh", "neurological", "Thần kinh", "Chuyên khoa thần kinh", "TK"),
        MedicalSpecialty("noi-tiet", "endocrine", "Nội tiết", "Chuyên khoa nội tiết", "NT"),
        MedicalSpecialty("co-xuong-khop", "orthopedic", "Cơ xương khớp", "Chuyên khoa cơ xương khớp", "CXK"),
        MedicalSpecialty("da-lieu", "dermatology", "Da liễu", "Chuyên khoa da liễu", "DL"),
        MedicalSpecialty("tai-mui-hong", "ent", "Tai mũi họng", "Chuyên khoa tai mũi họng", "TMH"),
        MedicalSpecialty("mat", "ophthalmology", "Nhãn khoa", "Chuyên khoa mắt", "NK"),
        MedicalSpecialty("phu-khoa", "gynecology", "Phụ khoa", "Chuyên khoa phụ khoa", "PK"),
        MedicalSpecialty("nam-khoa", "urology", "Nam khoa", "Chuyên khoa nam khoa", "NAM"),
        MedicalSpecialty("nhi-khoa", "pediatrics", "Nhi khoa", "Chuyên khoa nhi", "NHI"),
        MedicalSpecialty("phau-thuat", "surgery", "Phẫu thuật", "Chuyên khoa phẫu thuật", "PT"),
        MedicalSpecialty("chan-doan-hinh-anh", "radiology", "Chẩn đoán hình ảnh", "Chuyên khoa chẩn đoán hình ảnh", "CĐHA")
    )
}

object ProductCategories {
    val categories = listOf(
        ProductCategory("cat-supplies", "dung-cu-tiem-truyen", "Dụng cụ tiêm truyền"),
        ProductCategory("cat-syringe", "bom-tiem-ong-xi-lanh", "Bơm tiêm - Ống xi lanh", "cat-supplies"),
        ProductCategory("cat-needle", "kim-tiem", "Kim tiêm", "cat-supplies"),
        ProductCategory("cat-infusion-set", "day-truyen-dich", "Dây truyền dịch", "cat-supplies"),
        ProductCategory("cat-bandage", "bang-gac-cam-mau", "Băng gạc - Cầm máu"),
        ProductCategory("cat-sterile-gauze", "gac-vo-trung", "Gạc vô trùng", "cat-bandage"),
        ProductCategory("cat-device", "thiet-bi-chan-doan", "Thiết bị chẩn đoán"),
        ProductCategory("cat-blood-pressure", "may-do-huyet-ap", "Máy đo huyết áp", "cat-device"),
        ProductCategory("cat-thermometer", "nhiet-ke-y-te", "Nhiệt kế y tế", "cat-device"),
        ProductCategory("cat-protect", "khau-trang-ppe", "Khẩu trang - PPE"),
        ProductCategory("cat-mask", "khau-trang-y-te", "Khẩu trang y tế", "cat-protect"),
        ProductCategory("cat-gloves", "gang-tay-y-te", "Găng tay y tế", "cat-protect"),
        ProductCategory("cat-instrument", "thiet-bi-phau-thuat", "Thiết bị phẫu thuật"),
        ProductCategory("cat-infection-control", "chong-nhiem-khuan", "Chống nhiễm khuẩn"),
        ProductCategory("cat-therapy", "phuc-hoi-chuc-nang", "Phục hồi chức năng"),
        ProductCategory("cat-lab", "vat-tu-xet-nghiem", "Vật tư xét nghiệm")
    )
}
