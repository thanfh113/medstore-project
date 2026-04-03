package com.example.nhathuoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.nhathuoc.data.local.SessionManager
import com.example.nhathuoc.data.remote.RetrofitClient
import com.example.nhathuoc.navigation.AppnavHost
import com.example.nhathuoc.ui.screen.MainScreen
import com.example.nhathuoc.ui.theme.NhathuocTheme
import com.example.nhathuoc.util.DismissKeyboard

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize RetrofitClient with SessionManager
        val sessionManager = SessionManager(this)
        RetrofitClient.initialize(sessionManager)

        setContent {
            NhathuocTheme {
                DismissKeyboard {
                    val navController = rememberNavController()
                    AppnavHost(navController)
                }
            }
        }
    }
}
