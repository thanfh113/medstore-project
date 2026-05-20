package com.example.nhathuoc.data.local

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.nhathuoc.R
import com.example.nhathuoc.data.model.*

// -----------------------------------------------------------------------------
// Mock Product wrapper cho UI (kết hợp ProductDto + drawable image)
// -----------------------------------------------------------------------------

data class MockProduct(
    // Data thật (khớp ProductDto từ backend)
    val dto: ProductDto,
    // UI extras (chỉ dùng cho mock vì backend sẽ trả imageUrl thay thế)
    val imageResIds: List<Int> = emptyList(),  // mảng ảnh cho ProductDetailScreen
    val imageResId: Int? = null,               // drawable chính (thumbnail) - backward compat
    val icon: ImageVector,                     // fallback icon nếu chưa có ảnh
    val iconTint: Color,
    val iconBg: Color,
    // Flash sale / BestSeller flags cho HomeScreen
    val isFlashSale: Boolean = false,
    val flashDiscountPct: Int = 0,
    val isBestSeller: Boolean = false,
    val bestSellerDiscountLabel: String = ""
)

// -----------------------------------------------------------------------------
// 6 sản phẩm vật tư y tế mock
// ID format: "mock-prod-X" để phân biệt với real product khi BE sẵn sàng
// -----------------------------------------------------------------------------

private val mockNow = "2026-04-08T10:00:00"
private val mockCat = CategoryDto(
    id = "cat-mock-01", name = "Thiết bị y tế", slug = "thiet-bi-y-te",
    description = "Thiết bị và vật tư y tế chất lượng cao",
    createdAt = mockNow, updatedAt = mockNow
)

val mockProducts = listOf(

    MockProduct(
        dto = ProductDto(
            id = "mock-prod-1",
            name = "Máy đo huyết áp OMRON HEM-7156T",
            brand = "OMRON", origin = "Nhật Bản",
            price = 850_000.0, originalPrice = 990_000.0, discountPct = 14,
            unit = "Cái",
            description = "Máy đo huyết áp bắp tay tự động OMRON HEM-7156T với công nghệ Intelli Wrap Cuff đo chính xác ở mọi vị trí quấn băng. Tích hợp Bluetooth kết nối app OMRON Connect.",
            ingredients = null, indication = "Đo huyết áp và nhịp tim tại nhà",
            contraindication = "Không dùng cho bệnh nhân có rung tâm nhĩ nặng",
            sideEffects = null,
            dosage = null, storage = "Nhiệt độ 10–40°C, độ ẩm 15–85%",
            rewardPoints = 85, icon = null, iconTint = null, iconBg = null,
            imageUrl = null, category = mockCat,
            productType = "DEVICE",
            registrationNumber = "QLQN-0001-18",
            isPrescription = false,
            stock = 15, createdAt = mockNow, updatedAt = mockNow
        ),
        imageResIds = listOf(
            R.drawable.mock_omron_1,  // omron_1: main
            R.drawable.mock_omron_2   // omron_2: side view
        ),
        imageResId = R.drawable.mock_omron_1,
        icon = Icons.Outlined.MonitorHeart,
        iconTint = Color(0xFF0277BD), iconBg = Color(0xFFE1F5FE),
        isFlashSale = true, flashDiscountPct = 14,
        isBestSeller = true, bestSellerDiscountLabel = "-140.000d"
    ),

    MockProduct(
        dto = ProductDto(
            id = "mock-prod-2",
            name = "Khẩu trang y tế 4 lớp KIMBERLY-CLARK (Hộp 50 cái)",
            brand = "KIMBERLY-CLARK", origin = "Việt Nam",
            price = 120_000.0, originalPrice = 150_000.0, discountPct = 20,
            unit = "Hộp",
            description = "Khẩu trang y tế 4 lớp không khuẩn, đạt tiêu chuẩn ASTM Level 2. Lớp lọc meltblown BFE =98%, không nước và dịch. Hộp 50 cái.",
            ingredients = null, indication = "Bảo vệ hô hấp trong môi trường y tế và hằng ngày",
            contraindication = null, sideEffects = null,
            dosage = null, storage = "Nơi khô ráo, tránh ánh nắng trực tiếp",
            rewardPoints = 12, icon = null, iconTint = null, iconBg = null,
            imageUrl = null, category = mockCat,
            productType = "SUPPLY",
            registrationNumber = null,
            isPrescription = false,
            stock = 200, createdAt = mockNow, updatedAt = mockNow
        ),
        imageResIds = listOf(
            R.drawable.mock_kimberly_1,  // kimberly_1: box front
            R.drawable.mock_kimberly_2,  // kimberly_2: box back
            R.drawable.mock_kimberly_3   // kimberly_3: open/inside
        ),
        imageResId = R.drawable.mock_kimberly_1,
        icon = Icons.Outlined.Masks,
        iconTint = Color(0xFF2E7D32), iconBg = Color(0xFFE8F5E9),
        isFlashSale = true, flashDiscountPct = 20
    ),

    MockProduct(
        dto = ProductDto(
            id = "mock-prod-3",
            name = "Nhiệt kế điện tử hồng ngoại BEURER JFT95",
            brand = "BEURER", origin = "Đức",
            price = 450_000.0, originalPrice = 520_000.0, discountPct = 13,
            unit = "Cái",
            description = "Nhiệt kế hồng ngoại đa năng đo trán, tai và nhiệt độ bề mặt. Cho kết quả trong 1 giây, độ chính xác ±0.2°C. Ghi nhớ 30 lần đo, chức năng Fever Alert.",
            ingredients = null, indication = "Đo thân nhiệt cho mọi lứa tuổi",
            contraindication = null, sideEffects = null,
            dosage = null, storage = "Nhiệt độ 10–40°C",
            rewardPoints = 45, icon = null, iconTint = null, iconBg = null,
            imageUrl = null, category = mockCat,
            productType = "DEVICE",
            registrationNumber = "QLQN-0023-20",
            isPrescription = false,
            stock = 30, createdAt = mockNow, updatedAt = mockNow
        ),
        imageResIds = listOf(
            R.drawable.mock_beurer_1,  // beurer_1: main
            R.drawable.mock_beurer_1   // beurer_2: detail display (use same for now)
        ),
        imageResId = R.drawable.mock_beurer_1,
        icon = Icons.Outlined.DeviceThermostat,
        iconTint = Color(0xFFE64A19), iconBg = Color(0xFFFBE9E7),
        isBestSeller = true, bestSellerDiscountLabel = "-70.000d"
    ),

    MockProduct(
        dto = ProductDto(
            id = "mock-prod-4",
            name = "Máy đo nồng độ oxy SpO2 Jumper JPD-500D",
            brand = "Jumper", origin = "Trung Quốc",
            price = 280_000.0, originalPrice = 350_000.0, discountPct = 20,
            unit = "Cái",
            description = "Máy đo SpO2 và nhịp tim kẹp ngón tay Jumper JPD-500D. Màn hình OLED 4 chiều, đo trong 8 giây, kết quả chính xác SpO2 ±2%, nhịp tim ±2 BPM.",
            ingredients = null, indication = "Theo dõi nồng độ oxy máu và nhịp tim",
            contraindication = "Không thay thế thiết bị y tế chuyên dụng",
            sideEffects = null, dosage = null,
            storage = "Nhiệt độ 10–40°C, tránh ẩm",
            rewardPoints = 28, icon = null, iconTint = null, iconBg = null,
            imageUrl = null, category = mockCat,
            productType = "DEVICE",
            registrationNumber = null,
            isPrescription = false,
            stock = 50, createdAt = mockNow, updatedAt = mockNow
        ),
        imageResIds = listOf(
            R.drawable.mock_jumper_1,  // jumper_1: main
            R.drawable.mock_jumper_2   // jumper_2: on hand
        ),
        imageResId = R.drawable.mock_jumper_1,
        icon = Icons.Outlined.Favorite,
        iconTint = Color(0xFFE91E63), iconBg = Color(0xFFFCE4EC),
        isFlashSale = true, flashDiscountPct = 20
    ),

    MockProduct(
        dto = ProductDto(
            id = "mock-prod-5",
            name = "Găng tay y tế Nitrile Ansell TouchNTuff (100 cái)",
            brand = "Ansell", origin = "Malaysia",
            price = 195_000.0, originalPrice = 240_000.0, discountPct = 19,
            unit = "Hộp",
            description = "Găng tay nitrile không bột, không latex Ansell TouchNTuff 92-670. Bề mặt nhám tại ngón tay tăng độ bám, chống hóa chất nhẹ. AQL 1.5, hộp 100 cái.",
            ingredients = null, indication = "Bảo vệ tay trong môi trường y tế, phòng thí nghiệm",
            contraindication = null, sideEffects = null,
            dosage = null, storage = "Dưới 27°C, tránh ánh sáng UV",
            rewardPoints = 19, icon = null, iconTint = null, iconBg = null,
            imageUrl = null, category = mockCat,
            productType = "SUPPLY",
            registrationNumber = null,
            isPrescription = false,
            stock = 100, createdAt = mockNow, updatedAt = mockNow
        ),
        imageResIds = listOf(
            R.drawable.mock_ansell_1,  // ansell_1: box
            R.drawable.mock_ansell_2   // ansell_2: gloves open
        ),
        imageResId = R.drawable.mock_ansell_1,
        icon = Icons.Outlined.BackHand,
        iconTint = Color(0xFF546E7A), iconBg = Color(0xFFECEFF1),
        isBestSeller = true, bestSellerDiscountLabel = "-19%"
    ),

    MockProduct(
        dto = ProductDto(
            id = "mock-prod-6",
            name = "Băng dính y tế vô khuẩn 3M Tegaderm (10×12cm, Hộp 10 miếng)",
            brand = "3M", origin = "Mỹ",
            price = 165_000.0, originalPrice = 200_000.0, discountPct = 18,
            unit = "Hộp",
            description = "Băng keo trong suốt vô khuẩn 3M Tegaderm 1624W (10×12cm). Chất liệu polyurethane thấm hơi, chống nước và vi khuẩn. Dùng bảo vệ vết thương, IV catheter.",
            ingredients = null, indication = "Băng bó và bảo vệ vết thương, cố định catheter",
            contraindication = "Không dùng trên da bị viêm cấp tính",
            sideEffects = null, dosage = null,
            storage = "15–30°C, tránh ẩm và ánh sáng trực tiếp",
            rewardPoints = 17, icon = null, iconTint = null, iconBg = null,
            imageUrl = null, category = mockCat,
            productType = "SUPPLY",
            registrationNumber = null,
            isPrescription = false,
            stock = 80, createdAt = mockNow, updatedAt = mockNow
        ),
        imageResIds = listOf(
            R.drawable.mock_bandage_1,  // bandage_1: packaging
            R.drawable.mock_bandage_2   // bandage_2: on skin
        ),
        imageResId = R.drawable.mock_bandage_1,
        icon = Icons.Outlined.HealthAndSafety,
        iconTint = Color(0xFF00897B), iconBg = Color(0xFFE0F2F1),
        isFlashSale = true, flashDiscountPct = 18
    )
)

// -----------------------------------------------------------------------------
// Helpers
// -----------------------------------------------------------------------------

fun findMockProduct(id: String): MockProduct? = mockProducts.find { it.dto.id == id }

val flashSaleMockProducts  get() = mockProducts.filter { it.isFlashSale }
val bestSellerMockProducts get() = mockProducts.filter { it.isBestSeller }

// -----------------------------------------------------------------------------
// In-memory Mock Cart (thay thế API cart khi BE chưa sẵn sàng)
// -----------------------------------------------------------------------------

object MockCart {
    private val items = mutableMapOf<String, Int>()   // productId -> qty

    fun add(productId: String, qty: Int = 1) {
        items[productId] = (items[productId] ?: 0) + qty
    }

    fun remove(productId: String) { items.remove(productId) }

    fun updateQty(productId: String, qty: Int) {
        if (qty <= 0) items.remove(productId) else items[productId] = qty
    }

    fun clear() { items.clear() }

    fun getItems(): Map<String, Int> = items.toMap()

    fun isEmpty(): Boolean = items.isEmpty()

    /** Chuyển sang CartDto (khớp với CartViewModel) */
    fun toCartDto(): CartDto {
        val cartItems = items.mapNotNull { (id, qty) ->
            val mp = findMockProduct(id) ?: return@mapNotNull null
            CartItemDto(
                id        = "mock-ci-$id",
                userId    = "user-mock",
                productId = id,
                productName = mp.dto.name,
                productPrice = mp.dto.price,
                productImageUrl = mp.dto.imageUrl,
                product   = mp.dto,
                quantity  = qty,
                unit      = mp.dto.unit,
                subtotal = mp.dto.price * qty,
                stock = mp.dto.stock,
                createdAt = mockNow
            )
        }
        val subtotal = cartItems.sumOf { it.totalPrice }
        val discount = subtotal * 0.05
        return CartDto(
            id          = "cart-mock",
            userId      = "user-mock",
            items       = cartItems,
            totalItems  = cartItems.sumOf { it.quantity },
            subtotal    = subtotal,
            discount    = discount,
            total = subtotal - discount,
            createdAt   = mockNow,
            updatedAt   = mockNow
        )
    }
}
