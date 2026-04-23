# 🎨 JetpackCompose Desktop Shop Interface - UI Design Prompt

## 🎯 Tổng quan
Tạo giao diện Desktop Shop cho hệ thống nhà thuốc bằng **JetpackCompose Desktop**, đồng bộ hoàn toàn về tông màu và design language với mobile app hiện có.

---

## 🏪 Chức năng chính của Shop

### 📊 Dashboard & Analytics
- **Thống kê tổng quan:** Doanh thu hôm nay/tuần/tháng, số đơn hàng, sản phẩm bán chạy
- **Biểu đồ:** Line chart doanh thu theo thời gian, bar chart sản phẩm theo danh mục
- **Alerts:** Sản phẩm sắp hết hàng, đơn hàng cần xử lý, tin nhắn chưa đọc

### 🛍️ Quản lý sản phẩm
- **CRUD sản phẩm:** Thêm/sửa/xóa sản phẩm với đầy đủ thông tin
- **Upload ảnh:** Multi-image upload với preview và reorder
- **Quản lý kho:** Cập nhật tồn kho, thiết lập mức cảnh báo
- **Danh mục:** Phân loại sản phẩm theo categories và product types
- **Giấy tờ:** Upload chứng nhận, giấy phép (số đăng ký lưu hành, COA, GMP)

### 📦 Xử lý đơn hàng
- **Danh sách đơn:** Filter theo trạng thái, thời gian, khách hàng
- **Chi tiết đơn:** Xem đầy đủ thông tin khách hàng, sản phẩm, thanh toán
- **Cập nhật trạng thái:** PENDING → CONFIRMED → PREPARING → SHIPPING → DELIVERED
- **In hóa đơn:** Export PDF hóa đơn, phiếu giao hàng
- **Xử lý đổi trả:** Approve/reject return requests

### 💬 Chat tư vấn
- **Inbox:** Danh sách conversation với khách hàng
- **Real-time chat:** Nhận và gửi tin nhắn real-time
- **Product suggestions:** Gợi ý sản phẩm trong chat
- **File sharing:** Gửi hình ảnh, tài liệu tư vấn

### 🏢 Quản lý chi nhánh
- **Thông tin cửa hàng:** Địa chỉ, số điện thoại, giờ mở cửa
- **Nhân viên:** Phân quyền, quản lý tài khoản nhân viên
- **Lịch tiêm vắc-xin:** Schedule và manage vaccine appointments

### 📈 Báo cáo & Reports
- **Doanh thu:** Theo ngày/tuần/tháng/năm
- **Sản phẩm:** Top selling, slow moving, out of stock
- **Khách hàng:** Customer analytics, loyalty reports
- **Export:** PDF, Excel export cho các báo cáo

---

## 🎨 Color Scheme (Đồng bộ với Mobile App)

```kotlin
// Primary Colors - Green Theme (từ mobile app)
val GreenTop = Color(0xFF2E7D32)        // Dark green - Header, primary buttons
val GreenLight = Color(0xFF66BB6A)      // Light green - Gradients, secondary elements
val GreenMedium = Color(0xFF4CAF50)     // Medium green - Accents, active states
val GoldColor = Color(0xFFFFAB00)       // Gold - Rewards, achievements, highlights
val BgColor = Color(0xFFF2F4F7)         // Light gray - Main background

// Status Colors
val StatusSuccess = Color(0xFF4CAF50)   // Delivered, Confirmed orders
val StatusWarning = Color(0xFFFFAB00)   // Pending, Processing orders
val StatusError = Color(0xFFF44336)     // Cancelled, Failed orders
val StatusInfo = Color(0xFF2196F3)      // Info messages, notifications
```

---

## 🖼️ Layout Structure

```
┌───────────────────────────────────────────────────────────────┐
│ TOP APP BAR (Height: 72.dp, Color: GreenTop)                 │
│ [🏥 Logo] Nhà Thuốc ABC        [🔍 Search] [🔔] [👤 User]     │
├─────────────┬─────────────────────────────────────────────────┤
│ SIDEBAR     │               MAIN CONTENT AREA                 │
│ (240.dp)    │                                                 │
│ GreenTop    │  ┌─────────────────────────────────────────┐    │
│ ─────────── │  │        Content Cards                    │    │
│ 📊 Dashboard│  │      (White background)                 │    │
│ 🛍️ Sản phẩm │  │   RoundedCornerShape(16.dp)            │    │
│ 📦 Đơn hàng  │  │   shadowElevation = 4.dp               │    │
│ 💬 Chat     │  │                                         │    │
│ 🏢 Chi nhánh │  │  [Action buttons với GreenMedium]      │    │
│ 📈 Báo cáo   │  │  [Gold accents cho highlights]         │    │
│ ⚙️ Cài đặt   │  │                                         │    │
│             │  └─────────────────────────────────────────┘    │
└─────────────┴─────────────────────────────────────────────────┘
```

---

## 📂 Danh mục sản phẩm (Product Categories)

### 🏷️ Product Types (product_type)
```kotlin
enum class ProductType(val value: String, val displayName: String, val icon: String) {
    MEDICINE("MEDICINE", "Thuốc", "💊"),
    SUPPLEMENT("SUPPLEMENT", "Thực phẩm chức năng", "🌿"),
    COSMETIC("COSMETIC", "Mỹ phẩm", "💄"),
    DEVICE("DEVICE", "Thiết bị y tế", "🩺"),
    PERSONAL_CARE("PERSONAL_CARE", "Chăm sóc cá nhân", "🧴")
}
```

### 🏥 Categories (Danh mục chi tiết)
```kotlin
val categories = listOf(
    // THUỐC
    Category("Thuốc kê đơn", "prescription-drugs", ProductType.MEDICINE),
    Category("Thuốc không kê đơn", "otc-drugs", ProductType.MEDICINE),
    Category("Thuốc cảm cúm", "cold-flu", ProductType.MEDICINE),
    Category("Thuốc đau bụng", "stomach-pain", ProductType.MEDICINE),
    Category("Thuốc tim mạch", "cardiovascular", ProductType.MEDICINE),
    Category("Thuốc tiểu đường", "diabetes", ProductType.MEDICINE),
    Category("Thuốc cao huyết áp", "hypertension", ProductType.MEDICINE),

    // THỰC PHẨM CHỨC NĂNG
    Category("Vitamin & Khoáng chất", "vitamins", ProductType.SUPPLEMENT),
    Category("Tăng cường miễn dịch", "immunity", ProductType.SUPPLEMENT),
    Category("Bổ não", "brain-health", ProductType.SUPPLEMENT),
    Category("Sức khỏe xương khớp", "bone-joint", ProductType.SUPPLEMENT),
    Category("Hỗ trợ tiêu hóa", "digestive", ProductType.SUPPLEMENT),

    // MỸ PHẨM
    Category("Kem chống nắng", "sunscreen", ProductType.COSMETIC),
    Category("Sữa rửa mặt", "facial-cleanser", ProductType.COSMETIC),
    Category("Kem dưỡng da", "moisturizer", ProductType.COSMETIC),
    Category("Son môi", "lipstick", ProductType.COSMETIC),

    // THIẾT BỊ Y TẾ
    Category("Máy đo huyết áp", "blood-pressure", ProductType.DEVICE),
    Category("Máy đo đường huyết", "glucose-meter", ProductType.DEVICE),
    Category("Nhiệt kế", "thermometer", ProductType.DEVICE),
    Category("Khẩu trang y tế", "medical-masks", ProductType.DEVICE),

    // CHĂM SÓC CÁ NHÂN
    Category("Sữa tắm", "body-wash", ProductType.PERSONAL_CARE),
    Category("Dầu gội", "shampoo", ProductType.PERSONAL_CARE),
    Category("Kem đánh răng", "toothpaste", ProductType.PERSONAL_CARE),
    Category("Băng vệ sinh", "sanitary-pads", ProductType.PERSONAL_CARE)
)
```

### 🏥 Disease Categories (Bệnh lý)
```kotlin
val diseaseCategories = listOf(
    "Tim mạch" to "💗",
    "Hô hấp" to "🫁",
    "Tiêu hóa" to "🍽️",
    "Thần kinh" to "🧠",
    "Nội tiết" to "⚡",
    "Cơ xương khớp" to "🦴",
    "Da liễu" to "🧴",
    "Tai mũi họng" to "👂",
    "Mắt" to "👁️",
    "Phụ khoa" to "👩‍⚕️",
    "Nam khoa" to "👨‍⚕️",
    "Nhi khoa" to "🍼"
)
```

---

## 🎯 Key UI Components

### 1. 📊 Dashboard Stats Cards
```kotlin
@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    trend: String = "",
    trendPositive: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GreenMedium,
                    modifier = Modifier.size(32.dp)
                )
                if (trend.isNotEmpty()) {
                    Text(
                        text = trend,
                        color = if (trendPositive) StatusSuccess else StatusError,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = GreenTop
            )
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
```

### 2. 🛍️ Product Management Table
```kotlin
@Composable
fun ProductTable(
    products: List<ProductDto>,
    onEditProduct: (String) -> Unit,
    onDeleteProduct: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(GreenTop, GreenMedium)
                        )
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Hình ảnh", color = Color.White, fontWeight = FontWeight.Medium)
                Text("Tên sản phẩm", color = Color.White, fontWeight = FontWeight.Medium)
                Text("Danh mục", color = Color.White, fontWeight = FontWeight.Medium)
                Text("Giá", color = Color.White, fontWeight = FontWeight.Medium)
                Text("Tồn kho", color = Color.White, fontWeight = FontWeight.Medium)
                Text("Trạng thái", color = Color.White, fontWeight = FontWeight.Medium)
                Text("Hành động", color = Color.White, fontWeight = FontWeight.Medium)
            }

            // Product rows
            products.forEachIndexed { index, product ->
                ProductRow(
                    product = product,
                    isEven = index % 2 == 0,
                    onEdit = { onEditProduct(product.id) },
                    onDelete = { onDeleteProduct(product.id) }
                )
            }
        }
    }
}
```

### 3. 📦 Order Status Chip
```kotlin
@Composable
fun OrderStatusChip(status: OrderStatus) {
    val (backgroundColor, textColor) = when (status) {
        OrderStatus.PENDING -> BgColor to Color.Gray
        OrderStatus.CONFIRMED -> GreenLight to Color.White
        OrderStatus.PREPARING -> GoldColor to Color.White
        OrderStatus.SHIPPING -> StatusInfo to Color.White
        OrderStatus.DELIVERED -> StatusSuccess to Color.White
        OrderStatus.CANCELLED -> StatusError to Color.White
        OrderStatus.RETURNED -> Color.Gray to Color.White
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
```

### 4. 💬 Chat Interface
```kotlin
@Composable
fun ChatInterface(
    conversations: List<ChatSession>,
    selectedConversation: ChatSession?,
    onConversationSelect: (ChatSession) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Conversation List
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(conversations) { conversation ->
                ConversationItem(
                    conversation = conversation,
                    isSelected = conversation.id == selectedConversation?.id,
                    onClick = { onConversationSelect(conversation) }
                )
            }
        }

        VerticalDivider()

        // Chat Messages
        selectedConversation?.let { conversation ->
            ChatMessageArea(
                modifier = Modifier.weight(2f),
                conversation = conversation
            )
        }
    }
}
```

---

## 🎨 Design Specifications

### 📐 Spacing & Dimensions
```kotlin
object DesktopSpacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 32.dp

    // Component specific
    val cardPadding = 20.dp
    val sidebarWidth = 240.dp
    val topBarHeight = 72.dp
    val buttonHeight = 48.dp
}
```

### 🎭 Component Styling
```kotlin
object DesktopShapes {
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(16.dp)
    val large = RoundedCornerShape(24.dp)
    val button = RoundedCornerShape(12.dp)
}

object DesktopElevation {
    val card = 4.dp
    val dialog = 8.dp
    val menu = 6.dp
}
```

### 🔤 Typography
```kotlin
val DesktopTypography = Typography(
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = GreenTop
    ),
    headlineMedium = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = GreenTop
    ),
    titleLarge = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Black
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        color = Color.Black
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = Color.Gray
    )
)
```

---

## 🎪 Animations & Interactions

### ✨ Hover Effects
```kotlin
@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    text: String,
    backgroundColor: Color = GreenTop
) {
    var isHovered by remember { mutableStateOf(false) }
    val animatedColor by animateColorAsState(
        targetValue = if (isHovered) backgroundColor.copy(alpha = 0.8f) else backgroundColor,
        animationSpec = tween(200)
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = animatedColor),
        modifier = Modifier
            .pointerInput(Unit) {
                detectHoverGestures(
                    onEnter = { isHovered = true },
                    onExit = { isHovered = false }
                )
            }
    ) {
        Text(text, color = Color.White)
    }
}
```

### 🔄 Loading States
```kotlin
@Composable
fun LoadingCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = GreenMedium)
        }
    }
}
```

---

## 📱 Responsive Design

### 🖥️ Window Size Classes
```kotlin
enum class WindowSizeClass {
    COMPACT,    // < 840.dp
    MEDIUM,     // 840.dp - 1200.dp
    EXPANDED    // > 1200.dp
}

@Composable
fun AdaptiveLayout(
    windowSizeClass: WindowSizeClass,
    content: @Composable () -> Unit
) {
    when (windowSizeClass) {
        WindowSizeClass.COMPACT -> {
            // Sidebar collapses to icons only
            // Single column layout
        }
        WindowSizeClass.MEDIUM -> {
            // Normal sidebar
            // Two column layout for some screens
        }
        WindowSizeClass.EXPANDED -> {
            // Full sidebar
            // Three column layout possible
        }
    }
}
```

---

## 🚀 Performance Considerations

### ⚡ Lazy Loading
- Product images load on demand
- Virtual scrolling cho large datasets
- Pagination cho tables

### 💾 State Management
- ViewModel pattern với StateFlow
- Local caching cho frequently accessed data
- Optimistic updates cho better UX

### 🔄 Real-time Updates
- WebSocket connection cho chat
- Push notifications cho new orders
- Auto-refresh cho critical data

---

## 🎯 Accessibility

### ♿ A11y Features
- Keyboard navigation support
- Screen reader compatibility
- High contrast mode support
- Proper focus indicators
- Semantic markup

---

## 🔧 Development Notes

### 📦 Required Dependencies
```gradle
// Compose Desktop
implementation("androidx.compose.desktop:desktop:")

// Navigation
implementation("androidx.navigation:navigation-compose:")

// State management
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:")

// Networking
implementation("io.ktor:ktor-client:")

// Image loading
implementation("io.coil-kt:coil-compose:")
```

### 🏗️ Project Structure
```
desktop/
├── src/main/kotlin/
│   ├── ui/
│   │   ├── screens/        # Main screens
│   │   ├── components/     # Reusable components
│   │   ├── theme/          # Colors, typography, shapes
│   │   └── navigation/     # Navigation setup
│   ├── data/              # Data models, repositories
│   ├── viewmodel/         # ViewModels
│   └── utils/             # Utilities, extensions
└── resources/             # Images, fonts
```

---

## 🎉 Tóm tắt

Giao diện Desktop Shop cần đảm bảo:
- ✅ Đồng bộ hoàn toàn với mobile app về màu sắc và design language
- ✅ Workflow hiệu quả cho nhân viên nhà thuốc
- ✅ Responsive design cho nhiều kích thước màn hình
- ✅ Professional và user-friendly
- ✅ Performance tốt với large datasets
- ✅ Real-time capabilities cho chat và notifications

Kết quả cuối cùng là một ứng dụng desktop hiện đại, mạnh mẽ, giúp nhà thuốc quản lý business một cách hiệu quả nhất.