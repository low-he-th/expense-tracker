package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ExpenseCategory(
    val displayName: String,
    val iconName: String,
    val defaultColor: Long
) {
    FOOD("Food", "Fastfood", 0xFF10B981),
    TRAVEL("Travel", "CardTravel", 0xFF3B82F6),
    SHOPPING("Shopping", "ShoppingBag", 0xFFEC4899),
    ENTERTAINMENT("Entertainment", "Movie", 0xFF8B5CF6),
    HOUSEHOLD("Household", "Home", 0xFFF59E0B),
    HEALTH("Health", "HealthAndSafety", 0xFFEF4444),
    EDUCATION("Education", "School", 0xFF06B6D4),
    BILLS("Bills", "ReceiptLong", 0xFF6366F1),
    OTHERS("Others", "Category", 0xFF64748B);

    companion object {
        fun fromString(name: String?): ExpenseCategory {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) }
                ?: OTHERS
        }

        fun getIcon(category: ExpenseCategory): ImageVector {
            return when (category) {
                FOOD -> Icons.Default.Fastfood
                TRAVEL -> Icons.Default.CardTravel
                SHOPPING -> Icons.Default.ShoppingBag
                ENTERTAINMENT -> Icons.Default.Movie
                HOUSEHOLD -> Icons.Default.Home
                HEALTH -> Icons.Default.HealthAndSafety
                EDUCATION -> Icons.Default.School
                BILLS -> Icons.Default.ReceiptLong
                OTHERS -> Icons.Default.Category
            }
        }
    }
}
