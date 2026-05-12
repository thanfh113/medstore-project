package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.nhathuoc.data.model.AiMessageDto
import com.example.nhathuoc.data.model.ChatProductRecommendation
import com.example.nhathuoc.data.remote.BackendUrlResolver
import com.example.nhathuoc.viewmodel.AiChatViewModel

private val AiGreenTop = Color(0xFF1B5E20)
private val AiGreenLight = Color(0xFF43A047)
private val AiBubbleBg = Color(0xFFE8F5E9)
private val UserBubbleBg = Color(0xFF2E7D32)

private val quickSuggestions = listOf(
    "Băng gạc loại nào phù hợp cho vết thương hở?",
    "Máy đo huyết áp cơ học hay điện tử tốt hơn?",
    "Cách sử dụng máy đo đường huyết tại nhà",
    "Khẩu trang N95 và KN95 khác nhau như thế nào?",
    "Ống nghe loại nào phù hợp cho gia đình?",
    "Nhiệt kế hồng ngoại có chính xác không?",
    "Cần trang bị vật tư y tế gì cho tủ thuốc gia đình?"
)

@Composable
fun AiChatScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenHumanChat: (String) -> Unit = {},
    onProductClick: (String) -> Unit = {},
    productId: String? = null,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    var suggestionsExpanded by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(productId) {
        viewModel.startConversation(productId)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    LaunchedEffect(uiState.error) {
        val err = uiState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(err)
        viewModel.clearError()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF5F7FA),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AiChatTopBar(onBack = onBack, isEscalated = uiState.isEscalated)
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (uiState.isEscalated && uiState.humanSessionId != null) {
                        Button(
                            onClick = { onOpenHumanChat(uiState.humanSessionId!!) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(Icons.Filled.HeadsetMic, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Mở cuộc trò chuyện với chuyên viên", fontSize = 14.sp)
                        }
                    } else if (!uiState.isEscalated) {
                        if (uiState.messages.none { it.role == "user" } && !uiState.isLoading) {
                            QuickSuggestionPanel(
                                expanded = suggestionsExpanded,
                                onToggle = { suggestionsExpanded = !suggestionsExpanded },
                                onSelect = { suggestion -> viewModel.sendMessage(suggestion) }
                            )
                            Spacer(Modifier.height(6.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            OutlinedTextField(
                                value = uiState.inputText,
                                onValueChange = viewModel::setInput,
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Hỏi về vật tư y tế...", fontSize = 14.sp) },
                                enabled = !uiState.isLoading && !uiState.isSending,
                                shape = RoundedCornerShape(20.dp),
                                maxLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AiGreenLight,
                                    unfocusedBorderColor = Color(0xFFDDDDDD)
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.sendMessage() },
                                enabled = uiState.inputText.isNotBlank() && !uiState.isSending && !uiState.isLoading,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (uiState.inputText.isNotBlank() && !uiState.isSending) AiGreenTop
                                        else Color(0xFFBDBDBD),
                                        CircleShape
                                    )
                            ) {
                                if (uiState.isSending) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Gửi",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        if (uiState.messages.isNotEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Button(
                                onClick = { viewModel.escalateToHuman() },
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF1F8E9),
                                    contentColor = Color(0xFF388E3C)
                                )
                            ) {
                                Icon(Icons.Filled.HeadsetMic, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Kết nối chuyên viên", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (uiState.isLoading && uiState.messages.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AiGreenTop)
                            Spacer(Modifier.height(8.dp))
                            Text("Đang kết nối AI...", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            } else if (uiState.messages.isEmpty()) {
                item { WelcomeCard() }
            }

            items(uiState.messages) { msg ->
                    MessageBubble(msg, onProductClick = onProductClick)
            }

            if (uiState.isSending) {
                item { TypingIndicator() }
            }
        }
    }
}

@Composable
private fun AiChatTopBar(onBack: () -> Unit, isEscalated: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(AiGreenTop, AiGreenLight)))
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
                    Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Quay lại",
                    tint = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.SmartToy, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "AI Medstore",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
                Text(
                    if (isEscalated) "Đã kết nối chuyên viên" else "Dược sĩ AI · Vật tư y tế",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun WelcomeCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(AiBubbleBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.SmartToy, null, tint = AiGreenTop, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Xin chào! Tôi là AI Medstore",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF1A1A1A),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Tôi có thể tư vấn về vật tư y tế, cách sử dụng thiết bị\nvà gợi ý sản phẩm phù hợp cho bạn.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Chọn câu hỏi gợi ý bên dưới hoặc tự nhập:",
            fontSize = 12.sp,
            color = Color(0xFF888888),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QuickSuggestionPanel(
    expanded: Boolean,
    onToggle: () -> Unit,
    onSelect: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF7FBF7)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gợi ý câu hỏi",
                    modifier = Modifier.weight(1f),
                    color = AiGreenTop,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandMore else Icons.Filled.ExpandLess,
                        contentDescription = if (expanded) "Ẩn gợi ý" else "Hiện gợi ý",
                        tint = AiGreenTop
                    )
                }
            }
            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    quickSuggestions.forEach { suggestion ->
                        AssistChip(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onSelect(suggestion) },
                            label = {
                                Text(
                                    suggestion,
                                    maxLines = 2,
                                    fontSize = 12.sp,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = AiBubbleBg,
                                labelColor = AiGreenTop
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: AiMessageDto, onProductClick: (String) -> Unit) {
    val isUser = msg.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(AiBubbleBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.SmartToy, null, tint = AiGreenTop, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(6.dp))
        }
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) UserBubbleBg else AiBubbleBg,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = msg.text,
                    color = if (isUser) Color.White else Color(0xFF1A1A1A),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                if (!isUser && msg.recommendations.isNotEmpty()) {
                    msg.recommendations.forEach { recommendation ->
                        AiProductRecommendationCard(
                            recommendation = recommendation,
                            onClick = { onProductClick(recommendation.productId) }
                        )
                    }
                }
            }
        }
        if (isUser) {
            Spacer(Modifier.width(6.dp))
        }
    }
}

@Composable
private fun AiProductRecommendationCard(
    recommendation: ChatProductRecommendation,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiProductThumb(
                imageUrl = recommendation.productImage,
                name = recommendation.productName,
                modifier = Modifier.size(64.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = recommendation.productName,
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatAiMoney(recommendation.price)} đ / ${recommendation.productUnit ?: "sản phẩm"}",
                    color = AiGreenTop,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                recommendation.reason.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        color = Color(0xFF5F6368),
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "Bấm để xem chi tiết",
                    color = AiGreenTop,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AiProductThumb(
    imageUrl: String?,
    name: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = AiBubbleBg,
        shape = RoundedCornerShape(12.dp)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = BackendUrlResolver.resolveFileUrl(imageUrl),
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.HealthAndSafety,
                    contentDescription = null,
                    tint = AiGreenTop,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

private fun formatAiMoney(value: Double): String {
    return "%,.0f".format(value).replace(',', '.')
}

@Composable
private fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "alpha"
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(AiBubbleBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.SmartToy, null, tint = AiGreenTop, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(6.dp))
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp),
            color = AiBubbleBg
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                AiGreenTop.copy(alpha = if (it == 1) alpha else 1f - alpha * 0.5f),
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}
