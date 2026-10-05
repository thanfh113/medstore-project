package com.example.nhathuoc.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Error message card component
 */
@Composable
fun ErrorMessageCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = Color(0xFFffebee)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Error,
                contentDescription = null,
                tint = Color(0xFFc62828),
                modifier = Modifier.size(20.dp)
            )
            Text(
                message,
                color = Color(0xFFc62828),
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Warning message card component
 */
@Composable
fun WarningMessageCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = Color(0xFFfff3e0)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Warning,
                contentDescription = null,
                tint = Color(0xFFe65100),
                modifier = Modifier.size(20.dp)
            )
            Text(
                message,
                color = Color(0xFFe65100),
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Success message card component
 */
@Composable
fun SuccessMessageCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = Color(0xFFe8f5e9)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF2e7d32),
                modifier = Modifier.size(20.dp)
            )
            Text(
                message,
                color = Color(0xFF2e7d32),
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Password strength indicator bar
 */
@Composable
fun PasswordStrengthIndicator(
    strength: Int, // 0-3
    strengthLevel: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Độ mạnh mật khẩu",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                strengthLevel,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    strength == 0 || strength == 1 -> Color.Red
                    strength == 2 -> Color(0xFFFFA500)
                    else -> Color.Green
                }
            )
        }

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.LightGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (strength + 1) / 4f)
                    .background(
                        when {
                            strength == 0 || strength == 1 -> Color.Red
                            strength == 2 -> Color(0xFFFFA500)
                            else -> Color.Green
                        }
                    )
            )
        }
    }
}

/**
 * Loading progress indicator for forms
 */
@Composable
fun FormLoadingOverlay(
    isVisible: Boolean,
    message: String = "Vui lòng chờ...",
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                androidx.compose.material3.CircularProgressIndicator()
                Text(
                    message,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Empty state display
 */
@Composable
fun EmptyStateDisplay(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Outlined.Warning,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(48.dp)
        )
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Text(
            message,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}
