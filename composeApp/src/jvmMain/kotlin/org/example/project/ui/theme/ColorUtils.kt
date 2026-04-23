package org.example.project.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Utility functions for color parsing and manipulation
 */
object ColorUtils {

    /**
     * Predefined colors for common status values
     */
    private val colorMap = mapOf(
        "#4CAF50" to Color(0xFF4CAF50), // Green
        "#FFAB00" to Color(0xFFFFAB00), // Orange
        "#2196F3" to Color(0xFF2196F3), // Blue
        "#F44336" to Color(0xFFF44336), // Red
        "#9E9E9E" to Color(0xFF9E9E9E), // Gray

        // Common colors
        "#FF0000" to Color.Red,
        "#00FF00" to Color.Green,
        "#0000FF" to Color.Blue,
        "#FFFF00" to Color.Yellow,
        "#FF00FF" to Color.Magenta,
        "#00FFFF" to Color.Cyan,
        "#000000" to Color.Black,
        "#FFFFFF" to Color.White,
    )

    /**
     * Parse hex color string to Compose Color
     * Uses predefined color map for common colors to ensure safety
     */
    fun parseColor(colorString: String): Color {
        if (colorString.isBlank()) {
            return Color.Black
        }

        // Check predefined colors first (safer)
        colorMap[colorString.uppercase()]?.let { return it }

        return try {
            val cleanColor = colorString.removePrefix("#").uppercase()
            when (cleanColor.length) {
                6 -> {
                    val rgb = cleanColor.toLong(16)
                    Color(
                        red = ((rgb shr 16) and 0xFF) / 255f,
                        green = ((rgb shr 8) and 0xFF) / 255f,
                        blue = (rgb and 0xFF) / 255f,
                        alpha = 1f
                    )
                }
                8 -> {
                    val argb = cleanColor.toLong(16)
                    Color(
                        red = ((argb shr 16) and 0xFF) / 255f,
                        green = ((argb shr 8) and 0xFF) / 255f,
                        blue = (argb and 0xFF) / 255f,
                        alpha = ((argb shr 24) and 0xFF) / 255f
                    )
                }
                else -> Color.Black // Invalid length
            }
        } catch (e: Exception) {
            // Fallback for any parsing error
            Color.Black
        }
    }

    /**
     * Convert hex string to Color with alpha
     */
    fun parseColorWithAlpha(colorString: String, alpha: Float = 1f): Color {
        val baseColor = parseColor(colorString)
        return baseColor.copy(alpha = alpha.coerceIn(0f, 1f))
    }

    /**
     * Get status color safely with fallback
     */
    fun getStatusColor(colorString: String): Color {
        return when (colorString.uppercase()) {
            "#4CAF50" -> Color(0xFF4CAF50) // Green - Success/Active
            "#FFAB00" -> Color(0xFFFFAB00) // Orange - Warning/Waiting
            "#2196F3" -> Color(0xFF2196F3) // Blue - Info/Processing
            "#F44336" -> Color(0xFFF44336) // Red - Error/Urgent
            "#9E9E9E" -> Color(0xFF9E9E9E) // Gray - Inactive/Closed
            else -> parseColor(colorString)
        }
    }
}