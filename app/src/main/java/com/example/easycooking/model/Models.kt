package com.example.easycooking.model

data class Ingredient(
    val id: String,
    val name: String,
    val subtitle: String = "",
    val baseAmount: Double = 1.0,
    val unit: String = "",
    val category: String = "Pantry",
    val isChecked: Boolean = false
) {
    fun formattedAmount(servingsMultiplier: Double, isMetric: Boolean = true): String {
        val total = baseAmount * servingsMultiplier
        if (isMetric) {
            return if (total % 1.0 == 0.0) {
                "${total.toInt()} $unit".trim()
            } else {
                String.format("%.1f %s", total, unit).trim()
            }
        } else {
            return when (unit.lowercase().trim()) {
                "g", "gram", "grams" -> {
                    val oz = total / 28.35
                    String.format("%.1f oz", oz)
                }
                "ml" -> {
                    val flOz = total / 29.57
                    String.format("%.1f fl oz", flOz)
                }
                "kg" -> {
                    val lbs = total * 2.205
                    String.format("%.1f lbs", lbs)
                }
                else -> {
                    if (total % 1.0 == 0.0) {
                        "${total.toInt()} $unit".trim()
                    } else {
                        String.format("%.1f %s", total, unit).trim()
                    }
                }
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
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<RecipeStep> = emptyList(),
    val reviews: List<ReviewItem> = emptyList()
)

data class GroceryItem(
    val id: String,
    val name: String,
    val category: String,
    val isChecked: Boolean = false
)

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
    val selectedMealType: String = "Dinner"
)
