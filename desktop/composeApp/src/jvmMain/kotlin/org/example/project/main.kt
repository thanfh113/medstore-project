package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.PrintStream

fun main() {
    // Ensure Vietnamese characters display correctly in Windows console logs
    try {
        System.setOut(PrintStream(System.out, true, "UTF-8"))
        System.setErr(PrintStream(System.err, true, "UTF-8"))
    } catch (_: Exception) {}

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "MedStore — Quản lý Vật tư Y tế",
        ) {
            App()
        }
    }
}