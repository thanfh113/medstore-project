package com.example.nhathuoc.ui.screen.miniscreen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.nhathuoc.data.model.ChatMessageDto
import com.example.nhathuoc.data.model.ChatProductRecommendation
import com.example.nhathuoc.data.model.ChatSessionDto
import com.example.nhathuoc.data.remote.BackendUrlResolver
import com.example.nhathuoc.viewmodel.ChatViewModel
import kotlinx.serialization.json.Json

private val GreenTop = Color(0xFF2E7D32)
private val GreenLight = Color(0xFF66BB6A)
private val ChatJson = Json { ignoreUnknownKeys = true }

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onProductClick: (String) -> Unit = {},
    productId: String? = null,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isResolved = uiState.session?.status.equals("RESOLVED", ignoreCase = true)
    val consultantMessage = uiState.messages.lastOrNull { message ->
        val role = message.senderRole?.uppercase()
        role != null && role !in setOf("USER", "CUSTOMER") && !message.senderName.isNullOrBlank()
    }
    val consultantSubtitle = consultantMessage?.let { message ->
        "${chatRoleLabel(message.senderRole)}: ${message.senderName}"
    }

    LaunchedEffect(productId) {
        viewModel.initSession(productId)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    LaunchedEffect(uiState.error) {
        val error = uiState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(error)
        viewModel.clearError()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF5F7FA),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(GreenTop, GreenLight)))
                    .padding(top = 12.dp, start = 4.dp, end = 12.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tư vấn vật tư y tế",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = when (uiState.session?.status) {
                                "ASSIGNED" -> consultantSubtitle ?: "Nhân viên đang hỗ trợ"
                                "RESOLVED" -> "Phiên tư vấn đã hoàn tất"
                                else -> "Sẵn sàng tư vấn cho bạn"
                            },
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = viewModel::setInput,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nhập câu hỏi tư vấn") },
                        enabled = uiState.session != null && !uiState.isLoading && !isResolved,
                        shape = RoundedCornerShape(18.dp),
                        trailingIcon = {
                            IconButton(
                                onClick = viewModel::sendCurrentMessage,
                                enabled = uiState.inputText.isNotBlank() && !uiState.isSending && !isResolved
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Gửi",
                                    tint = if (uiState.inputText.isNotBlank() && !isResolved) GreenTop else Color.Gray
                                )
                            }
                        },
                        maxLines = 4
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val session = uiState.session
            if (session != null) {
                ChatContextCards(
                    session = session,
                    onProductClick = onProductClick,
                    onOpenConsultantDocument = { url ->
                        val targetUrl = BackendUrlResolver.resolveFileUrl(url)
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)))
                        }.onFailure {
                            Toast.makeText(context, "Không mở được file chuyên môn", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else if (!productId.isNullOrBlank()) {
                ProductPendingChip()
            }

            when {
                uiState.isLoading && uiState.messages.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = GreenTop)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Đang tải cuộc trò chuyện...")
                        }
                    }
                }

                uiState.messages.isEmpty() -> {
                    EmptyChatState()
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.messages, key = { it.id }) { message ->
                            ChatMessageBubble(
                                message = message,
                                isCurrentUser = message.senderId == uiState.currentUserId,
                                onProductClick = onProductClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductPendingChip() {
    AssistChip(
        onClick = {},
        enabled = false,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        label = { Text("Đang tư vấn cho vật tư đang xem") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.HealthAndSafety,
                contentDescription = null
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = Color(0xFFE8F5E9),
            disabledLabelColor = GreenTop,
            disabledLeadingIconContentColor = GreenTop
        )
    )
}

@Composable
private fun ChatContextCards(
    session: ChatSessionDto,
    onProductClick: (String) -> Unit,
    onOpenConsultantDocument: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        session.productName?.takeIf { it.isNotBlank() }?.let { productName ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !session.productId.isNullOrBlank()) {
                        session.productId?.let(onProductClick)
                    },
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProductThumb(
                        imageUrl = session.productImageUrl,
                        name = productName,
                        modifier = Modifier.size(70.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sản phẩm đang tư vấn", fontSize = 12.sp, color = Color(0xFF5F6368))
                        Text(productName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        session.productPrice?.let {
                            Text("${formatMoney(it)} đ / ${session.productUnit ?: "sản phẩm"}", fontSize = 12.sp, color = GreenTop)
                        }
                    }
                }
            }
        }

        session.consultantName?.takeIf { it.isNotBlank() }?.let { consultantName ->
            Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFE8F5E9), tonalElevation = 1.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Nhân viên đang tư vấn", fontSize = 12.sp, color = Color(0xFF5F6368))
                    Text(consultantName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GreenTop)
                    session.consultantQualificationTitle?.takeIf { it.isNotBlank() }?.let {
                        Text(it, fontSize = 13.sp)
                    }
                    session.consultantQualificationInstitution?.takeIf { it.isNotBlank() }?.let {
                        Text("Đơn vị cấp: $it", fontSize = 12.sp, color = Color(0xFF5F6368))
                    }
                    Text(
                        if (session.consultantVerified == true) "Hồ sơ chuyên môn đã xác minh" else "Hồ sơ chuyên môn chưa xác minh",
                        fontSize = 12.sp,
                        color = if (session.consultantVerified == true) GreenTop else Color(0xFFE65100)
                    )
                    session.consultantQualificationDocumentUrl?.takeIf { it.isNotBlank() }?.let { url ->
                        AssistChip(
                            onClick = { onOpenConsultantDocument(url) },
                            label = { Text("Xem chứng chỉ chuyên môn") }
                        )
                    }
                }
            }
        }
    }
}

private fun formatMoney(value: Double): String {
    return "%,.0f".format(value).replace(",", ".")
}

@Composable
private fun EmptyChatState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(GreenTop, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalPharmacy,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                    Text(
                    text = "Bắt đầu tư vấn",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hãy nhập câu hỏi để được tư vấn về vật tư y tế, cách sử dụng hoặc lựa chọn sản phẩm phù hợp.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5F6368),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessageDto,
    isCurrentUser: Boolean,
    onProductClick: (String) -> Unit
) {
    val productRecommendation = remember(message.type, message.metadata) {
        decodeChatProductRecommendation(message.metadata)
    }
    val isProductRecommendation = remember(message.type, productRecommendation) {
        productRecommendation != null &&
            (message.type.equals("PRODUCT_CARD", ignoreCase = true) ||
                message.type.equals("PRODUCT_RECOMMENDATION", ignoreCase = true))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isCurrentUser) {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(34.dp)
                    .background(GreenTop, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalPharmacy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = if (isCurrentUser) 18.dp else 6.dp,
                topEnd = if (isCurrentUser) 6.dp else 18.dp,
                bottomStart = 18.dp,
                bottomEnd = 18.dp
            ),
            color = if (isCurrentUser) GreenTop else Color.White,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!isCurrentUser && !message.senderName.isNullOrBlank()) {
                    Text(
                        text = "${chatRoleLabel(message.senderRole)} • ${message.senderName}",
                        color = GreenTop,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }

                if (isProductRecommendation && productRecommendation != null) {
                    ChatProductRecommendationCard(
                        recommendation = productRecommendation,
                        onClick = { onProductClick(productRecommendation.productId) }
                    )
                } else {

                Text(
                    text = message.content?.takeIf { it.isNotBlank() }
                        ?: "Tin nhắn ${message.type.lowercase()}",
                    color = if (isCurrentUser) Color.White else Color(0xFF1A1A1A),
                    fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }

                Text(
                    text = message.createdAt.replace('T', ' ').take(16),
                    color = if (isCurrentUser) Color.White.copy(alpha = 0.72f) else Color(0xFF8C9196),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ChatProductRecommendationCard(
    recommendation: ChatProductRecommendation,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProductThumb(
                imageUrl = recommendation.productImage,
                name = recommendation.productName,
                modifier = Modifier.size(76.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "Sản phẩm được gợi ý",
                    color = GreenTop,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                Text(
                    text = recommendation.productName,
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatMoney(recommendation.price)} đ / ${recommendation.productUnit ?: "sản phẩm"}",
                    color = GreenTop,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                recommendation.reason.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        color = Color(0xFF5F6368),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "Bấm để xem chi tiết và đặt hàng",
                    color = GreenTop,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ProductThumb(
    imageUrl: String?,
    name: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFE8F5E9),
        shape = RoundedCornerShape(14.dp)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            val resolvedImageUrl = BackendUrlResolver.resolveFileUrl(imageUrl)
            AsyncImage(
                model = resolvedImageUrl,
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.HealthAndSafety,
                    contentDescription = null,
                    tint = GreenTop,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

private fun decodeChatProductRecommendation(metadata: String?): ChatProductRecommendation? {
    if (metadata.isNullOrBlank()) return null
    return runCatching {
        ChatJson.decodeFromString(ChatProductRecommendation.serializer(), metadata)
    }.getOrNull()
}

private fun chatRoleLabel(role: String?): String = when (role?.uppercase()) {
    "ADMIN" -> "Quản trị viên"
    "EMPLOYEE", "STAFF", "PHARMACIST", "CONSULTANT" -> "Nhân viên chuyên môn"
    "AI", "SYSTEM" -> "Hệ thống"
    else -> "Nhân viên tư vấn"
}
