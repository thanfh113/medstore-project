package com.example.nhathuoc.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.nhathuoc.ui.theme.GreenTop

data class ShortVideo(
    val title: String,
    val category: String
)

val defaultShortVideos = listOf(
    ShortVideo("Một khoảnh khắc sơ ý khi ăn trái cây khiến bé trai rơi vào nguy kịch", "Radar sức khỏe"),
    ShortVideo("Nghệ An: Uống nhầm bật thang cổng, bé trai suýt thủng thực quản", "Radar sức khỏe"),
    ShortVideo("Một dấu hiệu khiến người ta truyền 6 lít máu trong 1 đêm", "Radar sức khỏe"),
    ShortVideo("Cách nhận biết đột quỵ sớm và xử trí đúng cách", "Sức khỏe"),
)

@Composable
fun ShortVideoRow(
    videos: List<ShortVideo> = defaultShortVideos,
    onSeeAll: () -> Unit = {},
    onVideoClick: (ShortVideo) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Videocam,
                    contentDescription = null,
                    tint = GreenTop,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Video ngắn nổi bật",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
            TextButton(onClick = onSeeAll) {
                Text("Xem tất cả", color = GreenTop, fontSize = 13.sp)
                Icon(Icons.Outlined.ChevronRight, null, tint = GreenTop, modifier = Modifier.size(16.dp))
            }
        }

        // Video cards
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            videos.forEach { video ->
                VideoCard(video = video, onClick = { onVideoClick(video) })
            }
        }
    }
}

@Composable
private fun VideoCard(
    video: ShortVideo,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.width(180.dp)
    ) {
        Column {
            // Thumbnail placeholder with play button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF1565C0)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Category label
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = video.category.uppercase(),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                // Play button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Phát video",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Title overlay at bottom
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = video.title,
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 13.sp,
                        maxLines = 3
                    )
                }
            }

            // Title below
            Text(
                text = video.title,
                fontSize = 11.sp,
                color = Color(0xFF1A1A1A),
                lineHeight = 15.sp,
                maxLines = 2,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
    }
}