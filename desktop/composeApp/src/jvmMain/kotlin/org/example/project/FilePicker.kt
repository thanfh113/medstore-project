package org.example.project.utils

import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter

fun openFileChooser(
    title: String = "Chọn tập tin",
    allowedExtensions: List<String> = listOf(".jpg", ".png", ".jpeg", ".pdf"),
    allowMultiple: Boolean = true
): List<File> {
    val extensions = allowedExtensions
        .map { it.trim().trimStart('.').lowercase() }
        .filter { it.isNotBlank() }
        .distinct()

    val owner = Frame()
    val dialog = FileDialog(owner, title, FileDialog.LOAD)
    return try {
        dialog.isMultipleMode = allowMultiple
        if (extensions.isNotEmpty()) {
            dialog.filenameFilter = FilenameFilter { _, name ->
                extensions.any { extension -> name.endsWith(".$extension", ignoreCase = true) }
            }
        }

        dialog.isVisible = true

        val files = if (allowMultiple) {
            dialog.files?.toList().orEmpty()
        } else {
            dialog.file
                ?.let { fileName -> File(dialog.directory, fileName) }
                ?.let(::listOf)
                .orEmpty()
        }

        files
            .filter { it.isFile }
            .filter { file ->
                extensions.isEmpty() || extensions.any { extension ->
                    file.name.endsWith(".$extension", ignoreCase = true)
                }
            }
    } finally {
        dialog.dispose()
        owner.dispose()
    }
}
