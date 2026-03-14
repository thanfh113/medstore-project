package com.example.nhathuoc.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.nhathuoc.ui.theme.GreenTop

data class HealthArticle(
    val category: String,
    val title: String
)

val defaultHealthArticles = listOf(
    HealthArticle("Truyền thông", "Hà Tiến Thành hợp tác hãng dược Nhật Bản Santen ra mắt bộ câu hỏi tầm soát khô mắt"),
    HealthArticle("Truyền thông", "Hà Tiến Thành phối hợp STADA Pymepharco lan toả kiến thức y tế cộng đồng"),
    HealthArticle("Truyền thông", "Hà Tiến Thành đóng góp sáng kiến về y tế số tại Diễn đàn Kinh tế Thuỵ Sĩ – Việt Nam"),
)

@Composable
fun HealthNewsSection(
    articles: List<HealthArticle> = defaultHealthArticles,
    onSeeAll: () -> Unit = {},
    onArticleClick: (HealthArticle) -> Unit = {},
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
                    imageVector = Icons.Outlined.Article,
                    contentDescription = null,
                    tint = GreenTop,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Góc sức khỏe",
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

        // Article list
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                articles.forEachIndexed { index, article ->
                    ArticleRow(article = article, onClick = { onArticleClick(article) })
                    if (index < articles.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color(0xFFEEEEEE),
                            thickness = 0.8.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleRow(
    article: HealthArticle,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail placeholder
            Box(
                modifier = Modifier
                    .size(width = 90.dp, height = 65.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = Color(0xFFBBCCEE),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.category,
                    fontSize = 11.sp,
                    color = GreenTop,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = article.title,
                    fontSize = 13.sp,
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp,
                    maxLines = 3
                )
            }
        }
    }
}