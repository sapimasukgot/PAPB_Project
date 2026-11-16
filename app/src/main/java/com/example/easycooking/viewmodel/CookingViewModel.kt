package com.example.easycooking.viewmodel

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easycooking.data.repository.AppRepository
import com.example.easycooking.model.FilterState
import com.example.easycooking.model.GroceryList
import com.example.easycooking.model.Recipe
import com.example.easycooking.model.ReviewItem
import com.example.easycooking.navigation.BottomTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CookingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    private val _currentTab = MutableStateFlow(BottomTab.DISCOVER)
    val currentTab: StateFlow<BottomTab> = _currentTab.asStateFlow()

    val recipes: StateFlow<List<Recipe>> = repository.getAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groceryLists: StateFlow<List<GroceryList>> = repository.getGroceryLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val selectedAllergens: StateFlow<Set<String>> = repository.preferencesManager.selectedAllergensFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), setOf("Peanuts", "Tree Nuts"))

    val selectedDietaryLifestyles: StateFlow<Set<String>> = repository.preferencesManager.selectedDietaryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), setOf("Vegetarian", "Gluten-Free"))

    val strictAllergenFilter: StateFlow<Boolean> = repository.preferencesManager.strictAllergenFilterFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isMetricUnit: StateFlow<Boolean> = repository.preferencesManager.isMetricUnitFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _servingsCount = MutableStateFlow(2)
    val servingsCount: StateFlow<Int> = _servingsCount.asStateFlow()

    private val _currentCookingStep = MutableStateFlow(1)
    val currentCookingStep: StateFlow<Int> = _currentCookingStep.asStateFlow()

    private val _timerSecondsLeft = MutableStateFlow(0)
    val timerSecondsLeft: StateFlow<Int> = _timerSecondsLeft.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _isTimerDone = MutableStateFlow(false)
    val isTimerDone: StateFlow<Boolean> = _isTimerDone.asStateFlow()

    private var timerJob: Job? = null

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

    val filteredRecipes: StateFlow<List<Recipe>> = combine(
        recipes,
        filterState,
        searchQuery,
        selectedAllergens,
        strictAllergenFilter
    ) { rList, filter, query, userAllergens, strictFilter ->
        rList.filter { recipe ->
            val isAllergenSafe = if (strictFilter && userAllergens.isNotEmpty()) {
                recipe.allergens.none { it in userAllergens }
            } else true

            val matchesDuration = when (filter.selectedDuration) {
                "< 15m", "< 15 Mins" -> recipe.durationMinutes < 15
                "15-30m" -> recipe.durationMinutes in 15..30
                "30-60m" -> recipe.durationMinutes in 31..60
                "1h+" -> recipe.durationMinutes > 60
                else -> true
            }

            val matchesMealType = if (filter.selectedMealType.isNotBlank() && filter.selectedMealType != "All") {
                recipe.category.equals(filter.selectedMealType, ignoreCase = true)
            } else true

            val matchesDietary = if (filter.selectedDietary.isNotEmpty()) {
                filter.selectedDietary.any { badge -> recipe.dietaryBadges.contains(badge) }
            } else true

            val matchesQuery = if (query.isNotBlank()) {
                recipe.title.contains(query, ignoreCase = true) ||
                        recipe.chefName.contains(query, ignoreCase = true) ||
                        recipe.ingredients.any { it.name.contains(query, ignoreCase = true) }
            } else true

            isAllergenSafe && matchesDuration && matchesMealType && matchesDietary && matchesQuery
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    fun setBottomTab(tab: BottomTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleBookmark(recipeId: String) {
        viewModelScope.launch {
            val currentRecipe = recipes.value.find { it.id == recipeId } ?: return@launch
            repository.toggleBookmark(recipeId, currentRecipe.isBookmarked)
        }
    }

    fun incrementServings() {
        _servingsCount.update { if (it < 50) it + 1 else 50 }
    }

    fun decrementServings() {
        _servingsCount.update { if (it > 1) it - 1 else 1 }
    }

    fun toggleMeasurementUnit() {
        viewModelScope.launch {
            repository.preferencesManager.setMetricUnit(!isMetricUnit.value)
        }
    }

    fun setCookingStep(step: Int, initialMinutes: Int = 5) {
        _currentCookingStep.value = step
        resetTimer(initialMinutes)
    }

    fun startStepTimer(minutes: Int) {
        if (_isTimerRunning.value) {
            pauseTimer()
            return
        }

        if (_timerSecondsLeft.value <= 0) {
            _timerSecondsLeft.value = minutes * 60
        }

        _isTimerRunning.value = true
        _isTimerDone.value = false

        timerJob?.cancel()
        timerJob = viewModelScope.launch(Dispatchers.Default) {
            while (_timerSecondsLeft.value > 0 && _isTimerRunning.value) {
                delay(1000)
                _timerSecondsLeft.update { it - 1 }
            }

            if (_timerSecondsLeft.value <= 0 && _isTimerRunning.value) {
                _isTimerRunning.value = false
                _isTimerDone.value = true
                playAlertSound()
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun addExtraMinute() {
        _timerSecondsLeft.update { it + 60 }
        _isTimerDone.value = false
    }

    fun resetTimer(minutes: Int) {
        timerJob?.cancel()
        _isTimerRunning.value = false
        _isTimerDone.value = false
        _timerSecondsLeft.value = minutes * 60
    }

    private fun playAlertSound() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 1000)
        } catch (_: Exception) {}
    }

    fun calculateProgress(currentStep: Int, totalSteps: Int): Float {
        return if (totalSteps > 0) currentStep.toFloat() / totalSteps.toFloat() else 0f
    }

    fun exportRecipeToGrocery(recipe: Recipe) {
        viewModelScope.launch {
            repository.exportRecipeToGrocery(recipe, _servingsCount.value)
        }
    }

    fun toggleGroceryItemCheck(listId: String, itemId: String) {
        viewModelScope.launch {
            val list = groceryLists.value.find { it.id == listId } ?: return@launch
            val item = list.items.find { it.id == itemId } ?: return@launch
            repository.toggleGroceryItem(itemId, item.isChecked, listId)
        }
    }

    fun addCustomGroceryItem(listId: String, itemName: String) {
        if (itemName.isBlank()) return
        viewModelScope.launch {
            repository.addCustomGroceryItem(listId, itemName)
        }
    }

    fun deleteGroceryList(listId: String) {
        viewModelScope.launch {
            repository.deleteGroceryList(listId)
        }
    }

    fun toggleDietaryLifestyle(lifestyle: String) {
        viewModelScope.launch {
            val current = selectedDietaryLifestyles.value
            val newSet = if (current.contains(lifestyle)) current - lifestyle else current + lifestyle
            repository.preferencesManager.setSelectedDietary(newSet)
        }
    }

    fun toggleAllergen(allergen: String) {
        viewModelScope.launch {
            val current = selectedAllergens.value
            val newSet = if (current.contains(allergen)) current - allergen else current + allergen
            repository.preferencesManager.setSelectedAllergens(newSet)
        }
    }

    fun setStrictAllergenFilter(enabled: Boolean) {
        viewModelScope.launch {
            repository.preferencesManager.setStrictAllergenFilter(enabled)
        }
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
        _newRecipeServings.update { if (it < 50) it + 1 else 50 }
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

    fun addReview(recipeId: String, rating: Int, comment: String) {
        val newReview = ReviewItem(
            id = "rev_${System.currentTimeMillis()}",
            userName = "Alex Morgan",
            rating = rating,
            timeAgo = "Just now",
            comment = comment
        )
        repository.addReviewInMemory(recipeId, newReview)
    }
}
