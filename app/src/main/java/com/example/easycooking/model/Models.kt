package com.example.easycooking.model

import kotlin.math.abs
import kotlin.math.roundToInt

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

private val SPOON_UNITS = setOf("tbsp", "tsp", "tablespoon", "teaspoon")
private val DISCRETE_UNITS = setOf("clove", "cloves", "pcs", "piece", "pieces", "portion", "leaf", "leaves", "slice", "slices")

internal fun normalizeUnitName(rawUnit: String): String {
    return when (rawUnit.trim().lowercase()) {
        "lembar" -> "leaves"
        "buah", "butir" -> "pcs"
        "siung" -> "cloves"
        "sdm" -> "tbsp"
        "sdt" -> "tsp"
        "mililiter" -> "ml"
        else -> rawUnit.trim()
    }
}

internal fun formatQuantityAndUnit(
    total: Double,
    unit: String,
    isMetric: Boolean
): String {
    val cleanUnit = normalizeUnitName(unit)
    val unitLower = cleanUnit.lowercase()
    return when {
        unitLower in SPOON_UNITS -> {
            val formattedFraction = formatToKitchenFraction(total)
            "$formattedFraction $cleanUnit".trim()
        }
        unitLower in DISCRETE_UNITS -> {
            val discreteCount = maxOf(1, total.roundToInt())
            "$discreteCount $cleanUnit".trim()
        }
        isMetric -> formatContinuousAmount(total, cleanUnit)
        else -> when (unitLower) {
            "g", "gram", "grams" -> formatContinuousAmount(total / 28.3495, "oz")
            "ml", "milliliter" -> formatContinuousAmount(total / 29.5735, "fl oz")
            "kg", "kilogram" -> formatContinuousAmount(total * 2.20462, "lb")
            else -> formatContinuousAmount(total, cleanUnit)
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
        else -> "1/4"
    }
}

enum class ScalingMode(
    val id: String,
    val label: String,
    val shortLabel: String,
    val description: String
) {
    PROPORTIONAL(
        id = "proportional",
        label = "Linear Scale (1:1)",
        shortLabel = "1:1 Scale",
        description = "Scales directly with servings (meat, pasta, vegetables)"
    ),
    COOKING_BASE(
        id = "cooking_base",
        label = "Cooking Base (Non-Linear)",
        shortLabel = "Gradual Scale",
        description = "Increases gradually for oils, butter, broth, sauces & spices (not doubled)"
    ),
    FIXED(
        id = "fixed",
        label = "Fixed Amount",
        shortLabel = "Fixed",
        description = "Stays constant regardless of serving count (garnish, pinch, pan spray)"
    );

    fun effectiveMultiplier(servingsMultiplier: Double): Double {
        if (servingsMultiplier <= 0.0) return 1.0
        return when (this) {
            PROPORTIONAL -> servingsMultiplier
            COOKING_BASE -> if (servingsMultiplier >= 1.0) {
                1.0 + (servingsMultiplier - 1.0) * 0.5
            } else {
                maxOf(0.5, 1.0 - (1.0 - servingsMultiplier) * 0.5)
            }
            FIXED -> 1.0
        }
    }

    companion object {
        fun fromId(id: String): ScalingMode = entries.find { it.id == id } ?: PROPORTIONAL
    }
}

data class Ingredient(
    val id: String,
    val name: String,
    val subtitle: String = "",
    val baseAmount: Double = 1.0,
    val unit: String = "",
    val category: String = "Oils, Sauces & Broths",
    val isChecked: Boolean = false,
    val allergenTags: List<String> = emptyList(),
    val scalingMode: ScalingMode = ScalingMode.PROPORTIONAL
) {
    fun scaledAmount(servingsMultiplier: Double): Double {
        return baseAmount * scalingMode.effectiveMultiplier(servingsMultiplier)
    }

    fun formattedAmount(servingsMultiplier: Double, isMetric: Boolean = true): String {
        return formatQuantityAndUnit(scaledAmount(servingsMultiplier), unit, isMetric)
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
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val isBookmarked: Boolean = false,
    val category: String = "Dinner",
    val dietaryBadges: List<String> = emptyList(),
    val allergens: List<String> = emptyList(),
    val defaultServings: Int = 2,
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<RecipeStep> = emptyList(),
    val reviews: List<ReviewItem> = emptyList(),
    val ownerEmail: String = "",
    val isDraft: Boolean = false,
    val imageUri: String = ""
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
        val qtyAndUnit = formatQuantityAndUnit(quantity, unit, isMetric)
        return "$qtyAndUnit $name".trim()
    }
}

data class GroceryList(
    val id: String,
    val title: String,
    val updatedAt: String,
    val items: List<GroceryItem> = emptyList(),
    val isCompleted: Boolean = false,
    val ownerEmail: String = ""
) {
    val totalItems: Int get() = items.size
    val remainingItems: Int get() = items.count { !it.isChecked }
}

data class FilterState(
    val selectedDuration: String = "",
    val selectedDietary: Set<String> = emptySet(),
    val selectedMealType: String = "",
    val selectedAllergens: Set<String> = emptySet(),
    val strictAllergenFilter: Boolean = false
)

enum class ExportGroceryStatus {
    SUCCESS,
    ALREADY_ACTIVE,
    FAILED
}

data class ExportGroceryResult(
    val status: ExportGroceryStatus,
    val listId: String? = null,
    val recipeTitle: String,
    val addedCount: Int = 0,
    val updatedCount: Int = 0,
    val message: String
) {
    val success: Boolean get() = status == ExportGroceryStatus.SUCCESS
}

object GroceryCategories {
    const val FRESH_VEGETABLES = "Fresh Vegetables"
    const val HERBS_AROMATICS = "Herbs & Aromatics"
    const val FRESH_FRUITS = "Fresh Fruits"
    const val FROZEN_FOODS = "Frozen Foods"
    const val POULTRY = "Poultry"
    const val BEEF_PORK = "Beef & Pork"
    const val SEAFOOD_SHELLFISH = "Seafood & Shellfish"
    const val TOFU_LEGUMES = "Tofu & Legumes"
    const val DAIRY_EGGS = "Dairy & Eggs"
    const val CHEESE = "Cheese"
    const val PASTA_GRAINS = "Pasta, Rice & Grains"
    const val BREAD_BAKERY = "Bread & Bakery"
    const val OILS_SAUCES = "Oils, Sauces & Broths"
    const val SPICES_SEASONINGS = "Spices & Seasonings"
    const val NUTS_SEEDS_BAKING = "Nuts, Seeds & Baking"

    const val DEFAULT_SECTION = OILS_SAUCES

    val AISLE_SECTIONS = listOf(
        FRESH_VEGETABLES,
        HERBS_AROMATICS,
        FRESH_FRUITS,
        FROZEN_FOODS,
        POULTRY,
        BEEF_PORK,
        SEAFOOD_SHELLFISH,
        TOFU_LEGUMES,
        DAIRY_EGGS,
        CHEESE,
        PASTA_GRAINS,
        BREAD_BAKERY,
        OILS_SAUCES,
        SPICES_SEASONINGS,
        NUTS_SEEDS_BAKING
    )

    fun aisleSortIndex(category: String): Int {
        val idx = AISLE_SECTIONS.indexOf(category)
        return if (idx >= 0) idx else AISLE_SECTIONS.size
    }

    fun refineCategory(itemName: String, rawCategory: String): String {
        if (rawCategory in AISLE_SECTIONS) return rawCategory
        val n = itemName.lowercase().trim()

        return when {
            containsAny(n, "frozen") -> FROZEN_FOODS
            containsAny(n, "garlic powder") -> SPICES_SEASONINGS
            containsAny(n, "bell pepper") -> FRESH_VEGETABLES
            containsAny(n, "broth", "stock", "sauce", "paste", "crema", "crushed tomatoes", "san marzano", "oil", "vinegar") ->
                OILS_SAUCES
            containsAny(n, "parmesan", "feta", "mozzarella", "mascarpone", "cheddar", "cheese") ->
                CHEESE
            containsAny(n, "milk", "cream", "butter", "egg", "yogurt", "coconut water", "ghee") ->
                DAIRY_EGGS
            containsAny(n, "chicken", "turkey", "duck") ->
                POULTRY
            containsAny(n, "beef", "ribeye", "steak", "sirloin", "bacon", "pork", "lamb") ->
                BEEF_PORK
            containsAny(n, "salmon", "shrimp", "cod", "scallop", "sea bass", "fish", "tuna") ->
                SEAFOOD_SHELLFISH
            containsAny(n, "tofu", "chickpea", "tempeh", "edamame", "lentil", "beans") ->
                TOFU_LEGUMES
            containsAny(n, "lemon", "lime", "berr", "mango", "apple", "kiwi", "dragonfruit", "banana", "orange") ->
                FRESH_FRUITS
            containsAny(n, "bread", "sourdough", "dough", "tortilla", "biscuit", "granola") ->
                BREAD_BAKERY
            containsAny(n, "pasta", "spaghetti", "fettuccine", "rice", "quinoa", "oats") ->
                PASTA_GRAINS
            containsAny(n, "chia", "walnut", "pine nut", "almond", "sesame", "flour", "starch", "sugar", "vanilla", "cocoa", "chocolate", "coconut", "espresso", "matcha", "syrup", "honey") ->
                NUTS_SEEDS_BAKING
            containsAny(n, "salt", "pepper", "paprika", "cumin", "cinnamon", "italian herbs", "flake", "spice", "oregano", "turmeric") ->
                SPICES_SEASONINGS
            containsAny(n, "garlic", "basil", "parsley", "dill", "rosemary", "thyme", "mint", "onion", "ginger", "cilantro", "shallot", "scallion") ->
                HERBS_AROMATICS
            containsAny(n, "spinach", "mushroom", "broccoli", "carrot", "tomato", "cucumber", "asparagus", "potato", "zucchini", "slaw", "cabbage", "kale", "avocado", "bamboo", "olive") ->
                FRESH_VEGETABLES
            rawCategory.equals("Produce & Meat", ignoreCase = true) -> FRESH_VEGETABLES
            else -> DEFAULT_SECTION
        }
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        return keywords.any { it in text }
    }
}

data class DraftIngredientInput(
    val name: String,
    val amount: Double,
    val unit: String,
    val category: String = GroceryCategories.DEFAULT_SECTION,
    val scalingMode: ScalingMode = ScalingMode.PROPORTIONAL
) {
    fun formattedSummary(): String {
        val cleanAmount = if (amount % 1.0 == 0.0) {
            amount.toInt().toString()
        } else {
            String.format(java.util.Locale.US, "%.1f", amount).trimEnd('0').trimEnd('.')
        }
        return listOf(cleanAmount, unit.trim(), name.trim())
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }
}

enum class AlarmSoundPreset(
    val id: String,
    val label: String,
    val description: String
) {
    DOUBLE_BEEP("double_beep", "Classic Double Beep", "Crisp double timer chime"),
    URGENT_BELL("urgent_bell", "Urgent Kitchen Alert", "Loud ring tone for busy kitchens"),
    SOFT_CHIME("soft_chime", "Soft Chime", "Gentle kitchen completion tone"),
    HIGH_PULSE("high_pulse", "High Pulse", "Fast repeated alarm pulse"),
    CUSTOM("custom", "Custom Audio File", "Play an audio file chosen from your device");

    companion object {
        fun fromId(id: String): AlarmSoundPreset {
            return entries.find { it.id == id } ?: DOUBLE_BEEP
        }
    }
}
