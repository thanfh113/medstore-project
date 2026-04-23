package org.example.project.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.example.project.presentation.viewmodels.FinanceDashboardViewModel
import org.example.project.presentation.viewmodels.FinanceUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceAdminScreen(viewModel: FinanceDashboardViewModel) {
    val state by viewModel.uiState.collectAsState()
    var retriedAuth by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadSummary()
    }

    LaunchedEffect(state) {
        val current = state
        if (current is FinanceUiState.Error && !retriedAuth) {
            retriedAuth = true
            delay(700)
            viewModel.loadSummary()
        } else if (current !is FinanceUiState.Error) {
            retriedAuth = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tài chính ADMIN", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { viewModel.loadSummary() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tải lại dữ liệu")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            when (val ui = state) {
                is FinanceUiState.Loading -> Text("Đang tải dữ liệu tài chính...")
                is FinanceUiState.Error -> Text(ui.message, color = MaterialTheme.colorScheme.error)
                is FinanceUiState.Success -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FinanceCard("Gross", formatVND(ui.data.grossRevenue), Modifier.weight(1f))
                        FinanceCard("Online", formatVND(ui.data.onlineRevenue), Modifier.weight(1f))
                        FinanceCard("POS", formatVND(ui.data.posRevenue), Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FinanceCard("Discount", formatVND(ui.data.totalDiscount), Modifier.weight(1f))
                        FinanceCard("Expenses", formatVND(ui.data.totalExpenses), Modifier.weight(1f))
                        FinanceCard("Net Profit", formatVND(ui.data.netProfit), Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("So don thanh cong: ${ui.data.successfulOrderCount}")
                    Text("So phieu chi: ${ui.data.expenseCount}")
                }
            }
        }
    }
}

@Composable
private fun FinanceCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

