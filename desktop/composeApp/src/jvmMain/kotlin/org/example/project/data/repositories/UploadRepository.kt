package org.example.project.data.repositories

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class UploadResponse(
    val urls: List<String>
)

class UploadRepository(private val client: HttpClient) {

    // Äá»•i URL nĂ y thĂ nh URL Backend thá»±c táº¿ cá»§a báº¡n
    private val baseUrl = "http://localhost:8080/api/v1"

    /**
     * Gá»­i danh sĂ¡ch file (áº¢nh/PDF) lĂªn Backend báº±ng Multipart Form Data.
     * Backend (Ktor) sáº½ xá»­ lĂ½ Ä‘áº©y lĂªn Cloudinary vĂ  tráº£ vá» List URL.
     */
    suspend fun uploadFiles(files: List<File>): Result<List<String>> {
        if (files.isEmpty()) return Result.success(emptyList())
        
        return try {
            val response: UploadResponse = client.submitFormWithBinaryData(
                url = "$baseUrl/upload",
                formData = formData {
                    files.forEach { file ->
                        // Äá»c byte array cá»§a file vĂ  Ä‘Ă­nh kĂ¨m vĂ o formData
                        append("files", file.readBytes(), Headers.build {
                            append(HttpHeaders.ContentType, getContentType(file.extension))
                            append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                        })
                    }
                }
            ).body()

            Result.success(response.urls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getContentType(extension: String): String {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
    }
}
