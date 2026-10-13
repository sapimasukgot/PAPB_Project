package com.example.easycooking.viewmodel

import androidx.lifecycle.ViewModel
import com.example.easycooking.model.FilterState
import com.example.easycooking.model.GroceryItem
import com.example.easycooking.model.GroceryList
import com.example.easycooking.model.Ingredient
import com.example.easycooking.model.Recipe
import com.example.easycooking.model.RecipeStep
import com.example.easycooking.model.ReviewItem
import com.example.easycooking.navigation.BottomTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CookingViewModel : ViewModel() {

    private val _currentTab = MutableStateFlow(BottomTab.DISCOVER)
    val currentTab: StateFlow<BottomTab> = _currentTab.asStateFlow()

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _groceryLists = MutableStateFlow<List<GroceryList>>(emptyList())
    val groceryLists: StateFlow<List<GroceryList>> = _groceryLists.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _servingsCount = MutableStateFlow(2)
    val servingsCount: StateFlow<Int> = _servingsCount.asStateFlow()

    private val _currentCookingStep = MutableStateFlow(1)
    val currentCookingStep: StateFlow<Int> = _currentCookingStep.asStateFlow()

    private val _selectedDietaryLifestyles = MutableStateFlow(setOf("Vegetarian", "Gluten-Free"))
    val selectedDietaryLifestyles: StateFlow<Set<String>> = _selectedDietaryLifestyles.asStateFlow()

    private val _selectedAllergens = MutableStateFlow(setOf("Peanuts", "Tree Nuts"))
    val selectedAllergens: StateFlow<Set<String>> = _selectedAllergens.asStateFlow()

    private val _strictAllergenFilter = MutableStateFlow(true)
    val strictAllergenFilter: StateFlow<Boolean> = _strictAllergenFilter.asStateFlow()

    private val _isMetricUnit = MutableStateFlow(true)
    val isMetricUnit: StateFlow<Boolean> = _isMetricUnit.asStateFlow()

    private val _newRecipeTitle = MutableStateFlow("Creamy Tuscan Garlic Chicken")
    val newRecipeTitle: StateFlow<String> = _newRecipeTitle.asStateFlow()

    private val _newRecipeServings = MutableStateFlow(2)
    val newRecipeServings: StateFlow<Int> = _newRecipeServings.asStateFlow()

    private val _newRecipeDuration = MutableStateFlow("30 min")
    val newRecipeDuration: StateFlow<String> = _newRecipeDuration.asStateFlow()

    private val _newRecipeIngredients = MutableStateFlow(
        listOf(
            "2 tbsp Extra Virgin Olive Oil",
            "500 g Chicken Breast Fillets",
            "3 cloves Garlic, finely minced"
        )
    )
    val newRecipeIngredients: StateFlow<List<String>> = _newRecipeIngredients.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val defaultIngredients = listOf(
            Ingredient("1", "Chicken breast", "tender diced fillets", 400.0, "g", "Produce & Meat"),
            Ingredient("2", "Garlic", "finely minced", 3.0, "cloves", "Produce"),
            Ingredient("3", "Heavy cream", "rich cooking cream", 100.0, "ml", "Dairy & Eggs"),
            Ingredient("4", "Olive oil", "extra virgin", 1.0, "tbsp", "Pantry Essentials"),
            Ingredient("5", "Baby spinach & sun-dried tomatoes", "fresh & chopped", 50.0, "g", "Produce & Specialty")
        )

        val defaultSteps = listOf(
            RecipeStep(
                stepNumber = 1,
                title = "Sear Chicken",
                description = "Heat olive oil in a deep skillet over medium-high heat. Brown seasoned chicken breasts 4-5 minutes per side until deeply golden brown.",
                durationMinutes = 6,
                tip = "Make sure the pan is hot before adding chicken for optimal sear."
            ),
            RecipeStep(
                stepNumber = 2,
                title = "Simmer Garlic Cream",
                description = "Saute minced garlic for 1 minute, then pour rich heavy cream & sun-dried tomatoes. Simmer gently on low heat until velvety smooth.",
                durationMinutes = 5,
                tip = "Keep heat at medium-low to prevent cream from curdling."
            ),
            RecipeStep(
                stepNumber = 3,
                title = "Combine & Garnish",
                description = "Return chicken breasts into the skillet with warm garlic cream sauce. Spoon sauce generously over chicken and garnish with freshly chopped basil and shaved parmesan.",
                durationMinutes = 4,
                isFinalStep = true,
                tip = "Let rest for 2 minutes before serving so the chicken retains its juiciness."
            )
        )

        val defaultReviews = listOf(
            ReviewItem("r1", "Sarah K.", 5, "2d ago", "Super easy for weeknights, the garlic aroma is incredible! Paired it with freshly toasted sourdough."),
            ReviewItem("r2", "Alex M.", 4, "5d ago", "Delicious sauce, perfectly balanced with the chicken. Added extra sun-dried tomatoes for sweetness.")
        )

        val initialRecipes = listOf(
            Recipe(
                id = "1",
                title = "Tuscan Garlic Chicken",
                chefName = "Chef Maria",
                difficulty = "Easy",
                durationMinutes = 25,
                calories = 420,
                rating = 4.8,
                reviewCount = 420,
                isBookmarked = true,
                category = "Dinner",
                dietaryBadges = listOf("Gluten-Free", "Nut-Free"),
                ingredients = defaultIngredients,
                steps = defaultSteps,
                reviews = defaultReviews
            ),
            Recipe(
                id = "2",
                title = "Lemon Butter Salmon",
                chefName = "Chef Maria",
                difficulty = "Easy",
                durationMinutes = 20,
                calories = 420,
                rating = 4.9,
                reviewCount = 310,
                isBookmarked = true,
                category = "Dinner",
                dietaryBadges = listOf("Gluten-Free", "Low Carb"),
                ingredients = defaultIngredients,
                steps = defaultSteps,
                reviews = defaultReviews
            ),
            Recipe(
                id = "3",
                title = "Creamy Truffle Pasta",
                chefName = "Chef Elena Vance",
                difficulty = "Medium",
                durationMinutes = 25,
                calories = 380,
                rating = 4.8,
                reviewCount = 180,
                isBookmarked = false,
                category = "Dinner",
                dietaryBadges = listOf("Vegetarian"),
                ingredients = defaultIngredients,
                steps = defaultSteps,
                reviews = defaultReviews
            ),
            Recipe(
                id = "4",
                title = "Garlic Herb Chicken",
                chefName = "Chef Maria",
                difficulty = "Easy",
                durationMinutes = 15,
                calories = 350,
                rating = 4.8,
                reviewCount = 95,
                isBookmarked = false,
                category = "Dinner",
                dietaryBadges = listOf("Gluten-Free"),
                ingredients = defaultIngredients,
                steps = defaultSteps,
                reviews = defaultReviews
            ),
            Recipe(
                id = "5",
                title = "Avocado Pesto Pasta",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 12,
                calories = 320,
                rating = 4.7,
                reviewCount = 140,
                isBookmarked = true,
                category = "Lunch",
                dietaryBadges = listOf("Vegan", "Dairy-Free"),
                ingredients = defaultIngredients,
                steps = defaultSteps,
                reviews = defaultReviews
            ),
            Recipe(
                id = "6",
                title = "Berry Acai Bowl",
                chefName = "Chef Alex",
                difficulty = "Easy",
                durationMinutes = 10,
                calories = 260,
                rating = 5.0,
                reviewCount = 88,
                isBookmarked = true,
                category = "Breakfast",
                dietaryBadges = listOf("Vegan", "Gluten-Free"),
                ingredients = defaultIngredients,
                steps = defaultSteps,
                reviews = defaultReviews
            )
        )

        _recipes.value = initialRecipes

        val initialGrocery = listOf(
            GroceryList(
                id = "g1",
                title = "Tuscan Garlic Chicken",
                updatedAt = "Updated 2h ago",
                items = listOf(
                    GroceryItem("gi1", "400g Chicken breast", "Produce & Meat", isChecked = true),
                    GroceryItem("gi2", "1 tbsp Olive oil", "Pantry Essentials", isChecked = true),
                    GroceryItem("gi3", "3 cloves Fresh Garlic", "Produce", isChecked = false),
                    GroceryItem("gi4", "100ml Heavy whipping cream", "Dairy & Eggs", isChecked = false),
                    GroceryItem("gi5", "50g Baby spinach & sun-dried tomatoes", "Produce & Specialty", isChecked = false)
                )
            ),
            GroceryList(
                id = "g2",
                title = "Berry Blast Smoothie",
                updatedAt = "Updated 5h ago",
                items = listOf(
                    GroceryItem("gi6", "Fresh Strawberries", "Produce", isChecked = true),
                    GroceryItem("gi7", "Almond Milk", "Dairy Alternatives", isChecked = true),
                    GroceryItem("gi8", "Chia Seeds", "Pantry", isChecked = false),
                    GroceryItem("gi9", "Organic Honey", "Pantry", isChecked = false)
                )
            ),
            GroceryList(
                id = "g3",
                title = "Avocado Pasta",
                updatedAt = "Yesterday",
                isCompleted = true,
                items = listOf(
                    GroceryItem("gi10", "Ripe Avocados", "Produce", isChecked = true),
                    GroceryItem("gi11", "Spaghetti Pasta", "Pantry", isChecked = true),
                    GroceryItem("gi12", "Fresh Basil", "Produce", isChecked = true),
                    GroceryItem("gi13", "Lemon", "Produce", isChecked = true),
                    GroceryItem("gi14", "Garlic cloves", "Produce", isChecked = true)
                )
            )
        )

        _groceryLists.value = initialGrocery
    }

    fun setBottomTab(tab: BottomTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleBookmark(recipeId: String) {
        _recipes.update { list ->
            list.map {
                if (it.id == recipeId) it.copy(isBookmarked = !it.isBookmarked) else it
            }
        }
    }

    fun incrementServings() {
        _servingsCount.update { it + 1 }
    }

    fun decrementServings() {
        _servingsCount.update { if (it > 1) it - 1 else 1 }
    }

    fun setCookingStep(step: Int) {
        _currentCookingStep.value = step
    }

    fun toggleGroceryItemCheck(listId: String, itemId: String) {
        _groceryLists.update { lists ->
            lists.map { list ->
                if (list.id == listId) {
                    val updatedItems = list.items.map { item ->
                        if (item.id == itemId) item.copy(isChecked = !item.isChecked) else item
                    }
                    val isAllChecked = updatedItems.isNotEmpty() && updatedItems.all { it.isChecked }
                    list.copy(items = updatedItems, isCompleted = isAllChecked)
                } else {
                    list
                }
            }
        }
    }

    fun addCustomGroceryItem(listId: String, itemName: String) {
        if (itemName.isBlank()) return
        _groceryLists.update { lists ->
            lists.map { list ->
                if (list.id == listId) {
                    val newItem = GroceryItem(
                        id = "custom_${System.currentTimeMillis()}",
                        name = itemName.trim(),
                        category = "Custom Item",
                        isChecked = false
                    )
                    list.copy(items = list.items + newItem, isCompleted = false)
                } else {
                    list
                }
            }
        }
    }

    fun deleteGroceryList(listId: String) {
        _groceryLists.update { lists ->
            lists.filterNot { it.id == listId }
        }
    }

    fun toggleDietaryLifestyle(lifestyle: String) {
        _selectedDietaryLifestyles.update { current ->
            if (current.contains(lifestyle)) current - lifestyle else current + lifestyle
        }
    }

    fun toggleAllergen(allergen: String) {
        _selectedAllergens.update { current ->
            if (current.contains(allergen)) current - allergen else current + allergen
        }
    }

    fun setStrictAllergenFilter(enabled: Boolean) {
        _strictAllergenFilter.value = enabled
    }

    fun updateFilterDuration(duration: String) {
        _filterState.update { it.copy(selectedDuration = duration) }
    }

    fun updateFilterMealType(mealType: String) {
        _filterState.update { it.copy(selectedMealType = mealType) }
    }

    fun toggleFilterDietary(dietary: String) {
        _filterState.update { current ->
            val set = current.selectedDietary
            val newSet = if (set.contains(dietary)) set - dietary else set + dietary
            current.copy(selectedDietary = newSet)
        }
    }

    fun resetFilters() {
        _filterState.value = FilterState()
    }

    fun updateNewRecipeTitle(title: String) {
        _newRecipeTitle.value = title
    }

    fun incrementNewRecipeServings() {
        _newRecipeServings.update { it + 1 }
    }

    fun decrementNewRecipeServings() {
        _newRecipeServings.update { if (it > 1) it - 1 else 1 }
    }

    fun updateNewRecipeDuration(duration: String) {
        _newRecipeDuration.value = duration
    }

    fun addNewRecipeIngredientRow(ingredient: String) {
        if (ingredient.isNotBlank()) {
            _newRecipeIngredients.update { it + ingredient.trim() }
        }
    }

    fun removeNewRecipeIngredientRow(index: Int) {
        _newRecipeIngredients.update { list ->
            list.filterIndexed { i, _ -> i != index }
        }
    }

    fun toggleMeasurementUnit() {
        _isMetricUnit.update { !it }
    }

    fun exportRecipeToGrocery(recipe: Recipe) {
        val isMetric = _isMetricUnit.value
        val newItems = recipe.ingredients.map {
            GroceryItem(
                id = "exp_${recipe.id}_${it.id}",
                name = "${it.formattedAmount(_servingsCount.value / 2.0, isMetric = isMetric)} ${it.name}",
                category = it.category,
                isChecked = false
            )
        }
        val newList = GroceryList(
            id = "list_${System.currentTimeMillis()}",
            title = recipe.title,
            updatedAt = "Just now",
            items = newItems
        )
        _groceryLists.update { listOf(newList) + it }
    }

    fun addReview(recipeId: String, rating: Int, comment: String) {
        val newReview = ReviewItem(
            id = "rev_${System.currentTimeMillis()}",
            userName = "Alex Morgan",
            rating = rating,
            timeAgo = "Just now",
            comment = comment
        )
        _recipes.update { list ->
            list.map {
                if (it.id == recipeId) {
                    it.copy(
                        reviews = listOf(newReview) + it.reviews,
                        reviewCount = it.reviewCount + 1
                    )
                } else it
            }
        }
    }
}
