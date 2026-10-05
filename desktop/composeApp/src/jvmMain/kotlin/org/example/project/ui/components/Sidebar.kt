package org.example.project.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.ui.navigation.Destination

@Composable
fun Sidebar(
    currentDestination: Destination,
    onNavigate: (Destination) -> Unit
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        header = {
            Text(
                text = "Nhà Thuốc",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    ) {
        Destination.all.forEach { destination ->
            NavigationRailItem(
                selected = currentDestination == destination,
                onClick = { onNavigate(destination) },
                icon = { Icon(destination.icon, contentDescription = destination.title) },
                label = { Text(destination.title) },
                alwaysShowLabel = false
            )
        }
    }
}
