package com.example.easycooking.model

import kotlin.math.abs
import kotlin.math.roundToInt

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class Ingredient(
    val id: String,
    val name: String,
    val subtitle: String = "",
    val baseAmount: Double = 1.0,
    val unit: String = "",
    val category: String = "Pantry Essentials",
    val isChecked: Boolean = false,
    val allergenTags: List<String> = emptyList()
) {
    fun formattedAmount(servingsMultiplier: Double, isMetric: Boolean = true): String {
        val total = baseAmount * servingsMultiplier
        val unitLower = unit.lowercase().trim()

        return when {
            unitLower in listOf("sdm", "sdt", "tbsp", "tsp", "tablespoon", "teaspoon") -> {
                val formattedFraction = formatToKitchenFraction(total)
                "$formattedFraction $unit".trim()
            }

            unitLower in listOf("butir", "siung", "lembar", "buah", "clove", "cloves", "pcs", "piece", "pieces") -> {
                val discreteCount = maxOf(1, total.roundToInt())
                "$discreteCount $unit".trim()
            }

            else -> {
                if (isMetric) {
                    formatContinuousAmount(total, unit)
                } else {
                    when (unitLower) {
                        "g", "gram", "grams" -> {
                            val oz = total / 28.3495
                            formatContinuousAmount(oz, "oz")
                        }
                        "ml", "mililiter" -> {
                            val flOz = total / 29.5735
                            formatContinuousAmount(flOz, "fl oz")
                        }
                        "kg", "kilogram" -> {
                            val lbs = total * 2.20462
                            formatContinuousAmount(lbs, "lb")
                        }
                        else -> formatContinuousAmount(total, unit)
                    }
                }
            }
        }
    }

    private fun formatContinuousAmount(amount: Double, displayUnit: String): String {
        val roundedInt = amount.roundToInt()
        return if (abs(amount - roundedInt) < 0.05) {
            "$roundedInt $displayUnit".trim()
        } else {
            String.format(java.util.Locale.US, "%.1f %s", amount, displayUnit).trim()
        }
    }

    private fun formatToKitchenFraction(amount: Double): String {
        val whole = amount.toInt()
        val remainder = amount - whole

        val fractionStr = when {
            remainder < 0.125 -> ""
            remainder in 0.125..<0.375 -> "1/4"
            remainder in 0.375..<0.625 -> "1/2"
            remainder in 0.625..<0.875 -> "3/4"
            else -> ""
        }

        return when {
            whole == 0 && fractionStr.isNotEmpty() -> fractionStr
            whole > 0 && fractionStr.isNotEmpty() -> "$whole $fractionStr"
            whole > 0 && fractionStr.isEmpty() -> {
                if (remainder >= 0.875) "${whole + 1}" else "$whole"
            }
            else -> {
                "1/4"
            }
        }
    }
}

data class RecipeStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val isFinalStep: Boolean = false,
    val tip: String = ""
)

data class ReviewItem(
    val id: String,
    val userName: String,
    val rating: Int,
    val timeAgo: String,
    val comment: String
)

data class Recipe(
    val id: String,
    val title: String,
    val chefName: String = "Chef Maria",
    val difficulty: String = "Easy",
    val durationMinutes: Int = 25,
    val calories: Int = 420,
    val rating: Double = 4.8,
    val reviewCount: Int = 420,
    val isBookmarked: Boolean = false,
    val category: String = "Dinner",
    val dietaryBadges: List<String> = emptyList(),
    val allergens: List<String> = emptyList(),
    val defaultServings: Int = 2,
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<RecipeStep> = emptyList(),
    val reviews: List<ReviewItem> = emptyList()
)

data class GroceryItem(
    val id: String,
    val recipeId: String = "",
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "",
    val category: String = "Pantry Essentials",
    val isChecked: Boolean = false
) {
    fun displayLabel(isMetric: Boolean = true): String {
        val convertedQuantity: Double
        val convertedUnit: String

        if (isMetric) {
            convertedQuantity = quantity
            convertedUnit = unit
        } else {
            when (unit.lowercase().trim()) {
                "g", "gram", "grams" -> {
                    convertedQuantity = quantity / 28.3495
                    convertedUnit = "oz"
                }
                "ml", "mililiter" -> {
                    convertedQuantity = quantity / 29.5735
                    convertedUnit = "fl oz"
                }
                "kg", "kilogram" -> {
                    convertedQuantity = quantity * 2.20462
                    convertedUnit = "lb"
                }
                else -> {
                    convertedQuantity = quantity
                    convertedUnit = unit
                }
            }
        }

        val roundedInt = convertedQuantity.roundToInt()
        val qtyStr = if (abs(convertedQuantity - roundedInt) < 0.05) {
            "$roundedInt"
        } else {
            String.format(java.util.Locale.US, "%.1f", convertedQuantity)
        }

        return if (convertedUnit.isNotBlank()) {
            "$qtyStr $convertedUnit $name".trim()
        } else {
            "$qtyStr $name".trim()
        }
    }
}

data class GroceryList(
    val id: String,
    val title: String,
    val updatedAt: String,
    val items: List<GroceryItem> = emptyList(),
    val isCompleted: Boolean = false
) {
    val totalItems: Int get() = items.size
    val remainingItems: Int get() = items.count { !it.isChecked }
}

data class FilterState(
    val selectedDuration: String = "15-30m",
    val selectedDietary: Set<String> = setOf("Gluten-Free", "Nut-Free"),
    val selectedMealType: String = "Dinner",
    val selectedAllergens: Set<String> = setOf("Peanuts", "Tree Nuts"),
    val strictAllergenFilter: Boolean = true
)
