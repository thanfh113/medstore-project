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
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val originalPrice: Double? = null,
    val categoryId: String,
    val stockQuantity: Int,
    val manufacturer: String,
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
    val images: List<ProductImage> = emptyList()
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
    val icon: String? = null
)

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
        ProductCategory("vat-tu-co-ban", "basic-supplies", "Vật tư y tế cơ bản"),
        ProductCategory("vat-tu-cap-cuu", "emergency-supplies", "Vật tư cấp cứu"),
        ProductCategory("kim-tiem", "syringes", "Kim tiêm và ống tiêm"),
        ProductCategory("ong-nghe", "stethoscopes", "Ống nghe"),
        ProductCategory("gang-tay", "gloves", "Găng tay y tế"),
        ProductCategory("may-do-huyet-ap", "blood-pressure", "Máy đo huyết áp"),
        ProductCategory("may-do-duong-huyet", "glucose-meter", "Máy đo đường huyết"),
        ProductCategory("nhiet-ke", "thermometer", "Nhiệt kế"),
        ProductCategory("may-sieu-am", "ultrasound", "Máy siêu âm"),
        ProductCategory("may-xquang", "xray", "Máy X-quang"),
        ProductCategory("kiem-phau-thuat", "surgical-forceps", "Kìm phẫu thuật"),
        ProductCategory("kim-khau", "surgical-needles", "Kim khâu"),
        ProductCategory("keo-phau-thuat", "surgical-scissors", "Kéo phẫu thuật"),
        ProductCategory("dao-phau-thuat", "surgical-knives", "Dao mổ"),
        ProductCategory("bang-gac", "bandages", "Băng gạc"),
        ProductCategory("mieng-dan", "patches", "Miếng dán vết thương"),
        ProductCategory("sat-trung", "antiseptics", "Dung dịch sát trùng"),
        ProductCategory("bang-ep", "compression-bandages", "Băng ép"),
        ProductCategory("khau-trang-y-te", "medical-masks", "Khẩu trang y tế"),
        ProductCategory("kinh-bao-ho", "protective-goggles", "Kính bảo hộ"),
        ProductCategory("ao-bao-ho", "protective-gowns", "Áo bảo hộ"),
        ProductCategory("mu-bao-ho", "protective-caps", "Mũ bảo hộ y tế")
    )
}

