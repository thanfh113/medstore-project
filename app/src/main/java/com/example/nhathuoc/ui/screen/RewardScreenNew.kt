package com.example.nhathuoc.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nhathuoc.data.model.*
import com.example.nhathuoc.data.repository.RewardRepository
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.GreenLight
import com.example.nhathuoc.ui.theme.BgColor
import com.example.nhathuoc.viewmodel.RewardViewModel
import com.example.nhathuoc.viewmodel.RewardViewModelFactory

@Composable
fun RewardScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: RewardViewModel = viewModel(
        factory = RewardViewModelFactory(RewardRepository())
    )
) {
    val rewardState by viewModel.rewardState.collectAsState()
    val historyState by viewModel.historyState.collectAsState()
    val points by viewModel.points.collectAsState()
    val pointValue by viewModel.pointValue.collectAsState()
    var showRedeemDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        HeaderReward(onBackClick = { navController?.popBackStack() })

        // Content
        when {
            rewardState is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenTop)
                }
            }
            rewardState is UiState.Success -> {
                val reward = (rewardState as UiState.Success).data
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(BgColor),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        RewardsBanner(points = points, pointValue = pointValue, tier = reward.tier)
                    }

                    item {
                        RewardsActionButtons(
                            onRedeemClick = { showRedeemDialog = true },
                            onHistoryClick = { viewModel.loadHistory() }
                        )
                    }

                    item {
                        Text(
                            "Lịch sử điểm",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    when (val history = historyState) {
                        is UiState.Success -> {
                            items(history.data) { item ->
                                RewardHistoryItem(item)
                            }
                        }
                        else -> {}
                    }

                    item {
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
            else -> {}
        }
    }

    if (showRedeemDialog) {
        RedeemPointsDialog(
            availablePoints = points,
            onDismiss = { showRedeemDialog = false },
            onConfirm = { pointsToRedeem ->
                viewModel.redeemPoints(pointsToRedeem)
                showRedeemDialog = false
            }
        )
    }
}

@Composable
private fun HeaderReward(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(GreenTop, GreenLight)))
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(
                "Điểm Thưởng",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.width(48.dp))
        }
    }
}

@Composable
private fun RewardsBanner(points: Int, pointValue: Double, tier: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = Brush.horizontalGradient(
            listOf(GreenTop, GreenLight)
        ).let { Color(0xFF2E7D32) },
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Điểm Thưởng Của Bạn",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    "$points",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                "Giá trị ≈ ${String.format("%,d", pointValue.toLong())}đ",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Hạng: $tier",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun RewardsActionButtons(
    onRedeemClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onRedeemClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenTop),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Filled.CardGiftcard, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Đổi điểm", color = Color.White)
        }

        OutlinedButton(
            onClick = onHistoryClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Lịch sử", color = GreenTop)
        }
    }
}

@Composable
private fun RewardHistoryItem(item: RewardHistoryDto) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(20.dp),
                color = if (item.type == "EARNING") Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFFE53935).copy(alpha = 0.2f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (item.type == "EARNING") Icons.Filled.Add else Icons.Filled.Remove,
                        contentDescription = null,
                        tint = if (item.type == "EARNING") Color(0xFF4CAF50) else Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(item.description, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                Text("Vừa xong", fontSize = 11.sp, color = Color.Gray)
            }

            Text(
                "${if (item.type == "EARNING") "+" else "-"} ${item.points}",
                fontWeight = FontWeight.Bold,
                color = if (item.type == "EARNING") Color(0xFF4CAF50) else Color(0xFFE53935),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun RedeemPointsDialog(
    availablePoints: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var pointsToRedeem by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đổi điểm thưởng") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Điểm có sẵn: $availablePoints điểm", fontSize = 12.sp, color = Color.Gray)
                OutlinedTextField(
                    value = pointsToRedeem,
                    onValueChange = { pointsToRedeem = it },
                    label = { Text("Số điểm muốn đổi") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Giá trị ≈ ${(pointsToRedeem.toIntOrNull() ?: 0) * 1000}đ",
                    fontSize = 12.sp,
                    color = GreenTop,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val points = pointsToRedeem.toIntOrNull() ?: 0
                    if (points > 0 && points <= availablePoints) {
                        onConfirm(points)
                    }
                }
            ) {
                Text("Đổi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Preview
@Composable
fun RewardScreenPreview() {
    RewardScreen()
}
