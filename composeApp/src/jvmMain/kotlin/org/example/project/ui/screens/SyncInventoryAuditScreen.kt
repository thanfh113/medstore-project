package org.example.project.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.presentation.viewmodels.SyncInventoryAuditItem
import org.example.project.presentation.viewmodels.SyncInventoryAuditViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncInventoryAuditScreen(viewModel: SyncInventoryAuditViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var productQuery by remember { mutableStateOf("") }
    var reasonQuery by remember { mutableStateOf("") }
    var fromDateText by remember { mutableStateOf("") }
    var toDateText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadAudits()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lich su audit ton kho sync", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = viewModel::loadAudits) {
                        Icon(Icons.Default.Refresh, contentDescription = "Tai lai")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        val filteredAudits = uiState.audits.filter { item ->
            matchesProduct(item, productQuery) &&
                matchesReason(item, reasonQuery) &&
                matchesDateRange(item, fromDateText, toDateText)
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        FilterSection(
                            productQuery = productQuery,
                            reasonQuery = reasonQuery,
                            fromDateText = fromDateText,
                            toDateText = toDateText,
                            onProductQueryChange = { productQuery = it },
                            onReasonQueryChange = { reasonQuery = it },
                            onFromDateChange = { fromDateText = it },
                            onToDateChange = { toDateText = it }
                        )
                    }
                    if (filteredAudits.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Khong co ban ghi phu hop bo loc")
                            }
                        }
                    } else {
                        items(filteredAudits, key = { it.id }) { item ->
                            AuditCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    productQuery: String,
    reasonQuery: String,
    fromDateText: String,
    toDateText: String,
    onProductQueryChange: (String) -> Unit,
    onReasonQueryChange: (String) -> Unit,
    onFromDateChange: (String) -> Unit,
    onToDateChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = productQuery,
            onValueChange = onProductQueryChange,
            label = { Text("Search product (name/id)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = reasonQuery,
            onValueChange = onReasonQueryChange,
            label = { Text("Reason contains") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = fromDateText,
                onValueChange = onFromDateChange,
                label = { Text("From (yyyy-MM-dd)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = toDateText,
                onValueChange = onToDateChange,
                label = { Text("To (yyyy-MM-dd)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun AuditCard(item: SyncInventoryAuditItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Product ID: ${item.productId}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Local: ${item.beforeStock}", color = MaterialTheme.colorScheme.error)
                Text("Server: ${item.serverStock}", color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Policy: ${item.policy}", style = MaterialTheme.typography.bodySmall)
            Text("Reason: ${item.reason}", style = MaterialTheme.typography.bodySmall)
            if (!item.sourceDeviceId.isNullOrBlank()) {
                Text("Source device: ${item.sourceDeviceId}", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "At: ${formatEpoch(item.createdAtEpochMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (item.resolvedBySyncAtEpochMs != null) {
                Text(
                    "Resolved by sync at: ${formatEpoch(item.resolvedBySyncAtEpochMs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun matchesProduct(item: SyncInventoryAuditItem, query: String): Boolean {
    if (query.isBlank()) return true
    return item.productName.contains(query, ignoreCase = true) ||
        item.productId.contains(query, ignoreCase = true)
}

private fun matchesReason(item: SyncInventoryAuditItem, query: String): Boolean {
    if (query.isBlank()) return true
    return item.reason.contains(query, ignoreCase = true)
}

private fun matchesDateRange(item: SyncInventoryAuditItem, fromText: String, toText: String): Boolean {
    val itemDate = Instant.ofEpochMilli(item.createdAtEpochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    val fromDate = parseLocalDate(fromText)
    val toDate = parseLocalDate(toText)

    if (fromDate != null && itemDate.isBefore(fromDate)) return false
    if (toDate != null && itemDate.isAfter(toDate)) return false
    return true
}

private fun parseLocalDate(raw: String): LocalDate? {
    if (raw.isBlank()) return null
    return runCatching { LocalDate.parse(raw.trim()) }.getOrNull()
}

private fun formatEpoch(epochMs: Long): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(formatter)
}

