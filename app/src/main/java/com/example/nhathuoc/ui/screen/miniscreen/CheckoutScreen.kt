package com.example.nhathuoc.ui.screen.miniscreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.nhathuoc.viewmodel.CheckoutViewModel

@Composable
fun CheckoutScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: CheckoutViewModel = hiltViewModel(),
    useMockData: Boolean = false
) {
    CheckoutFlowScreen(
        modifier = modifier,
        navController = navController,
        viewModel = viewModel
    )
}
