package org.example.project.utils

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/**
 * Mở hộp thoại chọn file trên Desktop (Windows, macOS, Linux).
 * Sử dụng AWT FileDialog vì nó gọi Native OS Dialog, mang lại UX tốt nhất trên Desktop.
 */
fun openFileChooser(
    title: String = "Chọn tập tin",
    allowedExtensions: List<String> = listOf(".jpg", ".png", ".jpeg", ".pdf"),
    allowMultiple: Boolean = true
): List<File> {
    val dialog = FileDialog(null as Frame?, title, FileDialog.LOAD)
    dialog.isMultipleMode = allowMultiple
    
    // Lọc đuôi file trên Windows/Linux
    dialog.file = allowedExtensions.joinToString(";") { "*$it" }
    
    dialog.isVisible = true
    return dialog.files.toList()
}