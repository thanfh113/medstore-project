package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhathuoc.ui.theme.NhathuocTheme
import kotlinx.coroutines.launch


import com.example.nhathuoc.ui.theme.GreenLight
private val GreenTop = Color(0xFF2E7D32)
private val GoldColor = Color(0xFFFFAB00)
private val RedColor  = Color(0xFFE53935)

// ── Data ──────────────────────────────────────────────────────────────
data class ChatMessage(
    val id: Int,
    val isFromBot: Boolean,
    val content: MessageContent
)

sealed class MessageContent {
    data class Text(val text: String) : MessageContent()
    data class Article(
        val bannerBgColor: Color,
        val title: String,
        val body: String,
        val primaryAction: String,
        val secondaryAction: String
    ) : MessageContent()
    data class ProductCard(
        val productName: String,
        val brand: String,
        val origin: String,
        val price: String,
        val originalPrice: String,
        val discountPercent: Int,
        val icon: ImageVector,
        val iconTint: Color,
        val iconBg: Color
    ) : MessageContent()
}

// ── Optional product info passed from ProductDetailScreen ─────────────
data class ChatProductContext(
    val productName: String,
    val brand: String,
    val origin: String,
    val price: String,
    val originalPrice: String,
    val discountPercent: Int,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color
)

private val defaultInitialMessages = listOf(
    ChatMessage(
        id = 1,
        isFromBot = true,
        content = MessageContent.Article(
            bannerBgColor = Color(0xFF1AB394),
            title = "CÚM DỄ BỊ NHẦM LẪN VỚI \"CẢM NHẸ\" – NHƯNG LẠI CÓ THỂ GÂY BIẾN CHỨNG CỰC KÌ NGUY HIỂM!",
            body = """Quý khách thân mến,

Các triệu chứng mà nhiều người đang lầm tưởng là 'cảm nhẹ' đôi khi lại là dấu hiệu khởi đầu của "cơn bão cúm" nguy hiểm!

Khác với cảm lạnh thông thường, vi rút cúm khởi phát đột ngột, có thể tấn công phổi, tim mạch chỉ trong thời gian ngắn. Theo CDC Hoa Kỳ, Cúm có thể gây các biến chứng nguy hiểm như viêm phổi, viêm cơ tim hoặc thậm chí đột quỵ – đặc biệt ở người lớn tuổi và người có bệnh nền.

Chỉ với bộ thiết bị y tế chuyên dụng, Quý khách có thể theo dõi và chăm sóc sức khỏe hiệu quả ngay tại nhà - tiết kiệm thời gian và chi phí khám bệnh!

Khám phá ngay bộ sưu tập vật tư y tế chất lượng cao – mua sắm tiện lợi tại MedStore ngay hôm nay!""",
            primaryAction = "Gọi tổng đài miễn phí",
            secondaryAction = "Tìm trung tâm gần nhất"
        )
    )
)

// ── Screen ────────────────────────────────────────────────────────────
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    productContext: ChatProductContext? = null   // null = vào chat thông thường
) {
    // Nếu có sản phẩm, prepend product card + lời chào của bot
    val startMessages = remember(productContext) {
        if (productContext != null) {
            listOf(
                ChatMessage(
                    id = 1,
                    isFromBot = false,
                    content = MessageContent.ProductCard(
                        productName    = productContext.productName,
                        brand          = productContext.brand,
                        origin         = productContext.origin,
                        price          = productContext.price,
                        originalPrice  = productContext.originalPrice,
                        discountPercent = productContext.discountPercent,
                        icon           = productContext.icon,
                        iconTint       = productContext.iconTint,
                        iconBg         = productContext.iconBg
                    )
                ),
                ChatMessage(
                    id = 2,
                    isFromBot = true,
                    content = MessageContent.Text(
                        "Xin chào! Tôi là Dược sĩ Long Châu 👋\n\nBạn đang quan tâm đến sản phẩm " +
                                "**${productContext.productName}**. Tôi có thể giúp gì cho bạn?\n\n" +
                                "• Hướng dẫn sử dụng\n• Tác dụng phụ\n• Tương tác thuốc\n• Hoặc bất kỳ câu hỏi nào khác"
                    )
                )
            )
        } else {
            defaultInitialMessages
        }
    }

    var messages by remember { mutableStateOf(startMessages) }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {

        // ── TopAppBar ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
                .statusBarsPadding()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)
            ) {
                Icon(Icons.Filled.ArrowBackIos, "Quay lại", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Text(
                "Dược sĩ HELLO",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // ── Message list ──────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFF5F7FA)),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageRow(message = msg)
            }
        }

        // ── Input bar ─────────────────────────────────────────────
        Surface(color = Color.White, shadowElevation = 8.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.CameraAlt, null, tint = Color.Gray, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.SentimentSatisfiedAlt, null, tint = Color.Gray, modifier = Modifier.size(22.dp))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFF0F0F0))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (inputText.isEmpty()) {
                        Text("Gửi yêu cầu", color = Color.Gray, fontSize = 14.sp)
                    }
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF1A1A1A)),
                        cursorBrush = SolidColor(GreenTop),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        val text = inputText.trim()
                        if (text.isNotEmpty()) {
                            val newMsg = ChatMessage(
                                id = messages.size + 1,
                                isFromBot = false,
                                content = MessageContent.Text(text)
                            )
                            messages = messages + newMsg
                            inputText = ""
                            scope.launch {
                                listState.animateScrollToItem(messages.lastIndex)
                            }
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.Send,
                        null,
                        tint = if (inputText.isNotEmpty()) GreenTop else Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

// ── Message row ───────────────────────────────────────────────────────
@Composable
private fun MessageRow(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = if (message.isFromBot) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        if (message.isFromBot) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(GreenTop),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.LocalPharmacy, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
        }

        when (val content = message.content) {

            // ── Plain text ────────────────────────────────────────
            is MessageContent.Text -> {
                Surface(
                    shape = RoundedCornerShape(
                        topStart    = if (message.isFromBot) 4.dp else 16.dp,
                        topEnd      = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd   = if (message.isFromBot) 16.dp else 4.dp
                    ),
                    color = if (message.isFromBot) Color.White else GreenTop,
                    shadowElevation = 1.dp,
                    modifier = Modifier.widthIn(max = 280.dp)
                ) {
                    Text(
                        text = content.text,
                        fontSize = 14.sp,
                        color = if (message.isFromBot) Color(0xFF1A1A1A) else Color.White,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            // ── Article card ──────────────────────────────────────
            is MessageContent.Article -> {
                Surface(
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.widthIn(max = 300.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(content.bannerBgColor, content.bannerBgColor.copy(alpha = 0.7f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFF6D00)) {
                                    Text(
                                        "TIÊM VẮC-XIN CÚM có thể PHÒNG NGỪA 90%",
                                        color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.MedicalServices, null, tint = Color.White, modifier = Modifier.size(32.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text("THIẾT BỊ Y TẾ", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                        Surface(shape = RoundedCornerShape(4.dp), color = Color.White.copy(alpha = 0.2f)) {
                                            Text("Từ 50.000đ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(content.title, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A), lineHeight = 18.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(content.body, fontSize = 13.sp, color = Color(0xFF444444), lineHeight = 19.sp)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {}, shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
                                modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Icon(Icons.Filled.Phone, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(content.primaryAction, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {}, shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF)),
                                modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Icon(Icons.Outlined.LocationOn, null, tint = GreenTop, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(content.secondaryAction, color = GreenTop, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // ── Product card (từ ProductDetailScreen) ─────────────
            is MessageContent.ProductCard -> {
                Surface(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.widthIn(max = 300.dp)
                ) {
                    Column {
                        // Banner màu theo iconBg
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(GreenTop, GreenLight))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(content.iconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = content.icon,
                                        contentDescription = null,
                                        tint = content.iconTint,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Column {
                                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE53935)) {
                                        Text(
                                            "-${content.discountPercent}%",
                                            color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        content.productName,
                                        color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                        lineHeight = 18.sp, maxLines = 2
                                    )
                                }
                            }
                        }

                        // Info
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE53935))
                                )
                                Text(content.origin, fontSize = 12.sp, color = Color(0xFF555555))
                                Text("•", color = Color.LightGray, fontSize = 12.sp)
                                Text(
                                    content.brand,
                                    fontSize = 12.sp, color = GreenTop, fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            Spacer(Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    content.price,
                                    fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = GreenTop
                                )
                                Text(
                                    content.originalPrice,
                                    fontSize = 12.sp, color = Color.Gray,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }

                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Tôi muốn tư vấn về sản phẩm này",
                                fontSize = 11.sp, color = Color.Gray,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatScreenPreview() {
    NhathuocTheme { ChatScreen() }
}