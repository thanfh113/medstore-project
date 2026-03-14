package com.example.nhathuoc.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nhathuoc.ui.theme.GreenTop
import com.example.nhathuoc.ui.theme.GoldColor

@Composable
fun GreetingCard(
    userName: String,
    rewardPoints: Int,
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row {
                    Text("Xin chào, ", fontSize = 14.sp, color = Color.Gray)
                    Text(userName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.EmojiEvents, null, tint = GoldColor, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("$rewardPoints ", color = GoldColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("điểm thưởng", fontSize = 14.sp)
                }
            }
            Button(
                onClick = { navController?.navigate("MyOrdersScreen") },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Outlined.Receipt, null, tint = GreenTop, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Đơn của tôi", color = GreenTop, fontSize = 13.sp)
            }
        }
    }
}