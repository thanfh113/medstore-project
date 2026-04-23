package org.example.project.data.models

import kotlinx.serialization.Serializable

/**
 * Disease Category Model
 * Represents disease/health condition categories for products
 * Maps to disease_categories table in the database
 *
 * @param id Unique identifier (UUID)
 * @param name Display name (e.g., "Tim mạch", "Hô hấp", "Tiểu đường")
 * @param iconUrl URL to icon image for disease category
 * @param description Description of the disease category
 * @param sortOrder Order for display in UI
 * @param isActive Whether this category is active
 */
@Serializable
data class DiseaseCategory(
    val id: String,
    val name: String,
    val iconUrl: String? = null,
    val description: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true
)

