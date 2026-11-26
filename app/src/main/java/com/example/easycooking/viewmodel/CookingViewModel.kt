package com.example.easycooking.viewmodel

import android.app.Application
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easycooking.data.repository.AppRepository
import com.example.easycooking.model.AlarmSoundPreset
import com.example.easycooking.model.DraftIngredientInput
import com.example.easycooking.model.ExportGroceryResult
import com.example.easycooking.model.FilterState
import com.example.easycooking.model.GroceryCategories
import com.example.easycooking.model.GroceryList
import com.example.easycooking.model.Ingredient
import com.example.easycooking.model.Recipe
import com.example.easycooking.model.RecipeStep
import com.example.easycooking.model.ReviewItem
import com.example.easycooking.model.ScalingMode
import com.example.easycooking.navigation.BottomTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

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

    private val _selectedSortOption = MutableStateFlow("Popularity")
    val selectedSortOption: StateFlow<String> = _selectedSortOption.asStateFlow()

    val selectedAllergens: StateFlow<Set<String>> = repository.preferencesManager.selectedAllergensFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val selectedDietaryLifestyles: StateFlow<Set<String>> = repository.preferencesManager.selectedDietaryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val strictAllergenFilter: StateFlow<Boolean> = repository.preferencesManager.strictAllergenFilterFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isMetricUnit: StateFlow<Boolean> = repository.preferencesManager.isMetricUnitFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val alarmSoundId: StateFlow<String> = repository.preferencesManager.alarmSoundIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlarmSoundPreset.DOUBLE_BEEP.id)

    val customAlarmPath: StateFlow<String> = repository.preferencesManager.customAlarmPathFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val customAlarmName: StateFlow<String> = repository.preferencesManager.customAlarmNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _isPreviewingAlarm = MutableStateFlow(false)
    val isPreviewingAlarm: StateFlow<Boolean> = _isPreviewingAlarm.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = repository.preferencesManager.isLoggedInFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val rememberMe: StateFlow<Boolean> = repository.preferencesManager.rememberMeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val rememberedEmail: StateFlow<String> = repository.preferencesManager.rememberedEmailFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _startupAuthChecked = MutableStateFlow(false)
    val startupAuthChecked: StateFlow<Boolean> = _startupAuthChecked.asStateFlow()

    val loggedInEmail: StateFlow<String> = repository.preferencesManager.loggedInEmailFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val loggedInName: StateFlow<String> = repository.preferencesManager.loggedInNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private var activeDetailRecipeId: String? = null
    private val _servingsCount = MutableStateFlow(2)
    val servingsCount: StateFlow<Int> = _servingsCount.asStateFlow()

    private val _detailCheckedIngredients = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val detailCheckedIngredients: StateFlow<Map<String, Set<String>>> = _detailCheckedIngredients.asStateFlow()

    private var activeCookingRecipeId: String? = null
    private val _currentCookingStep = MutableStateFlow(1)
    val currentCookingStep: StateFlow<Int> = _currentCookingStep.asStateFlow()

    private val _timerSecondsLeft = MutableStateFlow(0)
    val timerSecondsLeft: StateFlow<Int> = _timerSecondsLeft.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _isTimerDone = MutableStateFlow(false)
    val isTimerDone: StateFlow<Boolean> = _isTimerDone.asStateFlow()

    private val _autoAdvanceSecondsLeft = MutableStateFlow<Int?>(null)
    val autoAdvanceSecondsLeft: StateFlow<Int?> = _autoAdvanceSecondsLeft.asStateFlow()

    private val _autoAdvanceEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val autoAdvanceEvent: SharedFlow<Unit> = _autoAdvanceEvent.asSharedFlow()

    private var timerJob: Job? = null
    private var autoAdvanceJob: Job? = null
    private var alarmJob: Job? = null

    private val _newRecipeTitle = MutableStateFlow("")
    val newRecipeTitle: StateFlow<String> = _newRecipeTitle.asStateFlow()

    private val _newRecipeServings = MutableStateFlow(2)
    val newRecipeServings: StateFlow<Int> = _newRecipeServings.asStateFlow()

    private val _newRecipeDuration = MutableStateFlow("30 min")
    val newRecipeDuration: StateFlow<String> = _newRecipeDuration.asStateFlow()

    private val _newRecipeCategory = MutableStateFlow("Dinner")
    val newRecipeCategory: StateFlow<String> = _newRecipeCategory.asStateFlow()

    private val _newRecipeDietaryBadges = MutableStateFlow(setOf("Gluten-Free"))
    val newRecipeDietaryBadges: StateFlow<Set<String>> = _newRecipeDietaryBadges.asStateFlow()

    private val _newRecipeImageUri = MutableStateFlow("")
    val newRecipeImageUri: StateFlow<String> = _newRecipeImageUri.asStateFlow()

    private val _newRecipeIngredients = MutableStateFlow<List<DraftIngredientInput>>(emptyList())
    val newRecipeIngredients: StateFlow<List<DraftIngredientInput>> = _newRecipeIngredients.asStateFlow()

    private val _newRecipeStep1Title = MutableStateFlow("Prep & Sear")
    val newRecipeStep1Title: StateFlow<String> = _newRecipeStep1Title.asStateFlow()

    private val _newRecipeStep1Desc = MutableStateFlow("Prepare fresh ingredients and sear in a warm skillet until golden brown.")
    val newRecipeStep1Desc: StateFlow<String> = _newRecipeStep1Desc.asStateFlow()

    private val _newRecipeStep2Title = MutableStateFlow("Simmer & Serve")
    val newRecipeStep2Title: StateFlow<String> = _newRecipeStep2Title.asStateFlow()

    private val _newRecipeStep2Desc = MutableStateFlow("Combine seasoning, simmer gently until cooked through, and serve warm.")
    val newRecipeStep2Desc: StateFlow<String> = _newRecipeStep2Desc.asStateFlow()

    val curatedRecipes: StateFlow<List<Recipe>> = combine(
        recipes,
        isLoggedIn,
        combine(selectedAllergens, strictAllergenFilter, selectedDietaryLifestyles) { a, b, c -> Triple(a, b, c) }
    ) { rList, loggedIn, pref ->
        val userAllergens = pref.first
        val strictFilter = pref.second
        val userDietary = pref.third
        rList.filter { recipe ->
            if (recipe.isDraft) return@filter false
            if (!loggedIn) return@filter true

            val isAllergenSafe = if (strictFilter && userAllergens.isNotEmpty()) {
                recipe.allergens.none { recipeAllergen ->
                    userAllergens.any { it.equals(recipeAllergen, ignoreCase = true) }
                }
            } else true

            val matchesUserDietary = if (userDietary.isNotEmpty()) {
                userDietary.all { badge ->
                    recipe.dietaryBadges.any { it.equals(badge, ignoreCase = true) }
                }
            } else true

            isAllergenSafe && matchesUserDietary
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredRecipes: StateFlow<List<Recipe>> = combine(
        curatedRecipes,
        filterState,
        searchQuery,
        selectedSortOption
    ) { rList, filter, query, sortOption ->
        val filtered = rList.filter { recipe ->
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
                filter.selectedDietary.all { badge ->
                    val freeAllergen = when (badge.lowercase()) {
                        "shellfish-free" -> "Shellfish"
                        "dairy-free" -> "Dairy"
                        "gluten-free" -> "Gluten"
                        "nut-free" -> "Tree Nuts"
                        else -> null
                    }
                    recipe.dietaryBadges.any { it.equals(badge, ignoreCase = true) } ||
                        (freeAllergen != null && recipe.allergens.none { it.equals(freeAllergen, ignoreCase = true) })
                }
            } else true

            val matchesQuery = if (query.isNotBlank()) {
                recipe.title.contains(query, ignoreCase = true) ||
                    recipe.chefName.contains(query, ignoreCase = true) ||
                    recipe.ingredients.any { it.name.contains(query, ignoreCase = true) }
            } else true

            matchesDuration && matchesMealType && matchesDietary && matchesQuery
        }

        when (sortOption) {
            "Quickest" -> filtered.sortedBy { it.durationMinutes }
            "Highest Rated" -> filtered.sortedByDescending { it.rating }
            else -> filtered.sortedByDescending { it.reviewCount }
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val isRemembered = repository.preferencesManager.isRememberMeEnabled()
            val currentlyLogged = repository.preferencesManager.isLoggedIn()
            if (currentlyLogged && !isRemembered) {
                repository.logoutUser()
            }
            repository.seedDatabaseIfEmpty()
            _startupAuthChecked.value = true
        }
    }

    fun setBottomTab(tab: BottomTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun cycleSortOption() {
        _selectedSortOption.update { current ->
            when (current) {
                "Popularity" -> "Quickest"
                "Quickest" -> "Highest Rated"
                else -> "Popularity"
            }
        }
    }

    fun toggleBookmark(recipeId: String) {
        viewModelScope.launch {
            val currentRecipe = recipes.value.find { it.id == recipeId } ?: return@launch
            repository.toggleBookmark(recipeId, currentRecipe.isBookmarked)
        }
    }

    fun initRecipeDetail(recipeId: String, defaultServings: Int) {
        if (activeDetailRecipeId != recipeId) {
            activeDetailRecipeId = recipeId
            _servingsCount.value = defaultServings.coerceAtLeast(1)
        }
    }

    fun toggleDetailIngredientChecked(recipeId: String, ingredientId: String) {
        _detailCheckedIngredients.update { map ->
            val currentSet = map[recipeId] ?: emptySet()
            val updatedSet = if (currentSet.contains(ingredientId)) {
                currentSet - ingredientId
            } else {
                currentSet + ingredientId
            }
            map + (recipeId to updatedSet)
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

    fun initCookingMode(recipeId: String, firstStepMinutes: Int, forceReset: Boolean = false) {
        if (forceReset || activeCookingRecipeId != recipeId) {
            activeCookingRecipeId = recipeId
            _currentCookingStep.value = 1
            resetTimer(firstStepMinutes)
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

        cancelAutoAdvanceGrace()

        if (_timerSecondsLeft.value <= 0) {
            _timerSecondsLeft.value = minutes * 60
        }

        _isTimerRunning.value = true
        _isTimerDone.value = false

        launchCountdownLoop()
    }

    private fun launchCountdownLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch(Dispatchers.Default) {
            while (_timerSecondsLeft.value > 0 && _isTimerRunning.value) {
                delay(1000)
                _timerSecondsLeft.update { (it - 1).coerceAtLeast(0) }
            }

            if (_timerSecondsLeft.value <= 0 && _isTimerRunning.value) {
                _isTimerRunning.value = false
                _isTimerDone.value = true
                playAlertSound()
                startAutoAdvanceGracePeriod()
            }
        }
    }

    private fun startAutoAdvanceGracePeriod() {
        autoAdvanceJob?.cancel()
        autoAdvanceJob = viewModelScope.launch(Dispatchers.Default) {
            for (sec in 10 downTo 1) {
                _autoAdvanceSecondsLeft.value = sec
                delay(1000)
            }
            _autoAdvanceSecondsLeft.value = null
            stopAlarm()
            _autoAdvanceEvent.tryEmit(Unit)
        }
    }

    fun cancelAutoAdvanceGrace() {
        autoAdvanceJob?.cancel()
        autoAdvanceJob = null
        _autoAdvanceSecondsLeft.value = null
        stopAlarm()
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        cancelAutoAdvanceGrace()
        stopAlarm()
    }

    fun addExtraMinute() {
        val wasDoneOrInGrace = _isTimerDone.value || _autoAdvanceSecondsLeft.value != null
        cancelAutoAdvanceGrace()
        stopAlarm()
        _timerSecondsLeft.update { it + 60 }
        _isTimerDone.value = false

        if (wasDoneOrInGrace && !_isTimerRunning.value) {
            _isTimerRunning.value = true
            launchCountdownLoop()
        }
    }

    fun resetTimer(minutes: Int) {
        timerJob?.cancel()
        cancelAutoAdvanceGrace()
        stopAlarm()
        _isTimerRunning.value = false
        _isTimerDone.value = false
        _timerSecondsLeft.value = minutes * 60
    }

    fun selectAlarmPreset(presetId: String) {
        viewModelScope.launch {
            repository.preferencesManager.setAlarmSoundPreset(presetId)
        }
    }

    fun saveCustomAlarmFromUri(uri: Uri, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val app = getApplication<Application>()
                    val dir = File(app.filesDir, "alarm_sounds").apply { mkdirs() }
                    dir.listFiles()?.forEach { old -> runCatching { old.delete() } }

                    val displayName = queryDisplayName(app, uri) ?: "Custom Alarm Sound"
                    val targetFile = File(dir, "custom_alarm_${System.currentTimeMillis()}")
                    app.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    } ?: error("Cannot read selected audio file.")

                    targetFile.absolutePath to displayName
                }
            }
            result.onSuccess { (path, displayName) ->
                repository.preferencesManager.setCustomAlarmAudio(path, displayName)
                onResult(true, displayName)
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "Failed to load selected audio file.")
            }
        }
    }

    fun previewAlarmSound() {
        if (_isPreviewingAlarm.value) {
            stopAlarm()
        } else {
            playAlertSound(isPreview = true)
        }
    }

    private fun queryDisplayName(app: Application, uri: Uri): String? {
        return runCatching {
            app.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) cursor.getString(idx) else null
                } else {
                    null
                }
            }
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    private fun playAlertSound(isPreview: Boolean = false) {
        alarmJob?.cancel()
        _isPreviewingAlarm.value = isPreview
        val preset = AlarmSoundPreset.fromId(alarmSoundId.value)
        val audioPath = customAlarmPath.value

        alarmJob = viewModelScope.launch(Dispatchers.IO) {
            var mediaPlayer: MediaPlayer? = null
            try {
                if (preset == AlarmSoundPreset.CUSTOM && audioPath.isNotBlank() && File(audioPath).exists()) {
                    val playedCustom = runCatching {
                        mediaPlayer = MediaPlayer().apply {
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_ALARM)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .build()
                            )
                            setDataSource(audioPath)
                            isLooping = true
                            prepare()
                            start()
                        }
                        val totalMs = if (isPreview) 3200L else 9600L
                        val stepMs = 200L
                        var elapsed = 0L
                        while (elapsed < totalMs && currentCoroutineContext()[Job]?.isActive == true) {
                            delay(stepMs)
                            elapsed += stepMs
                        }
                    }.isSuccess
                    if (playedCustom) return@launch
                }

                val (toneType, toneDurationMs, intervalMs, repeats) = when (preset) {
                    AlarmSoundPreset.URGENT_BELL -> listOf(ToneGenerator.TONE_SUP_RINGTONE, 900, 1200, if (isPreview) 2 else 8)
                    AlarmSoundPreset.SOFT_CHIME -> listOf(ToneGenerator.TONE_PROP_ACK, 550, 1250, if (isPreview) 3 else 7)
                    AlarmSoundPreset.HIGH_PULSE -> listOf(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 450, 750, if (isPreview) 4 else 12)
                    else -> listOf(ToneGenerator.TONE_PROP_BEEP2, 700, 1200, if (isPreview) 3 else 8)
                }

                var toneGen: ToneGenerator? = null
                try {
                    toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                    repeat(repeats) {
                        if (currentCoroutineContext()[Job]?.isActive != true) return@repeat
                        toneGen.startTone(toneType, toneDurationMs)
                        delay(intervalMs.toLong())
                    }
                } finally {
                    runCatching {
                        toneGen?.stopTone()
                        toneGen?.release()
                    }
                }
            } catch (_: Exception) {
            } finally {
                _isPreviewingAlarm.value = false
                runCatching {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                }
            }
        }
    }

    fun stopAlarm() {
        alarmJob?.cancel()
        alarmJob = null
        _isPreviewingAlarm.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopAlarm()
    }

    fun calculateProgress(currentStep: Int, totalSteps: Int): Float {
        return if (totalSteps > 0) currentStep.toFloat() / totalSteps.toFloat() else 0f
    }

    fun exportRecipeToGrocery(
        recipe: Recipe,
        targetServings: Int = _servingsCount.value,
        onResult: (ExportGroceryResult) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.exportRecipeToGrocery(recipe, targetServings)
            onResult(result)
        }
    }

    fun createNewGroceryList(title: String, onCreated: (String) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.createNewGroceryList(title)
            onCreated(id)
        }
    }

    fun toggleGroceryItemCheck(listId: String, itemId: String) {
        viewModelScope.launch {
            val list = groceryLists.value.find { it.id == listId } ?: return@launch
            val item = list.items.find { it.id == itemId } ?: return@launch
            repository.toggleGroceryItem(itemId, item.isChecked, listId)
        }
    }

    fun addCustomGroceryItem(listId: String, itemName: String, category: String = GroceryCategories.DEFAULT_SECTION) {
        if (itemName.isBlank()) return
        viewModelScope.launch {
            repository.addCustomGroceryItem(listId, itemName, category)
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

    fun clearDietaryAndAllergenPreferences() {
        viewModelScope.launch {
            repository.preferencesManager.setSelectedDietary(emptySet())
            repository.preferencesManager.setSelectedAllergens(emptySet())
            repository.preferencesManager.setStrictAllergenFilter(false)
        }
    }

    fun updateFilterDuration(duration: String) {
        _filterState.update { current ->
            val newDuration = if (current.selectedDuration == duration) "" else duration
            current.copy(selectedDuration = newDuration)
        }
    }

    fun updateFilterMealType(mealType: String) {
        _filterState.update { current ->
            val newMealType = if (current.selectedMealType == mealType) "" else mealType
            current.copy(selectedMealType = newMealType)
        }
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

    fun updateNewRecipeCategory(category: String) {
        _newRecipeCategory.value = category
    }

    fun toggleNewRecipeDietaryBadge(badge: String) {
        _newRecipeDietaryBadges.update { current ->
            if (current.contains(badge)) current - badge else current + badge
        }
    }

    fun updateNewRecipeStep1Title(title: String) {
        _newRecipeStep1Title.value = title
    }

    fun updateNewRecipeStep1Desc(desc: String) {
        _newRecipeStep1Desc.value = desc
    }

    fun updateNewRecipeStep2Title(title: String) {
        _newRecipeStep2Title.value = title
    }

    fun updateNewRecipeStep2Desc(desc: String) {
        _newRecipeStep2Desc.value = desc
    }

    fun savePickedRecipeImageFromUri(uri: Uri, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val savedPath = withContext(Dispatchers.IO) {
                runCatching {
                    val app = getApplication<Application>()
                    val dir = File(app.filesDir, "recipe_images").apply { mkdirs() }
                    val targetFile = File(dir, "recipe_${java.util.UUID.randomUUID()}.jpg")
                    app.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    } ?: error("Cannot read selected image.")
                    targetFile.absolutePath
                }.getOrNull()
            }
            if (savedPath != null) {
                _newRecipeImageUri.value = savedPath
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun clearNewRecipeImage() {
        _newRecipeImageUri.value = ""
    }

    fun addNewRecipeIngredientRow(
        name: String,
        amountText: String,
        unit: String,
        category: String = GroceryCategories.DEFAULT_SECTION,
        scalingMode: ScalingMode = ScalingMode.PROPORTIONAL
    ): Boolean {
        val cleanName = name.trim()
        val parsedAmount = amountText.trim().replace(",", ".").toDoubleOrNull()
        if (cleanName.isBlank() || parsedAmount == null || parsedAmount <= 0.0) {
            return false
        }
        val cleanUnit = unit.trim().ifBlank { "pcs" }
        _newRecipeIngredients.update {
            it + DraftIngredientInput(
                name = cleanName,
                amount = parsedAmount,
                unit = cleanUnit,
                category = category,
                scalingMode = scalingMode
            )
        }
        return true
    }

    fun removeNewRecipeIngredientRow(index: Int) {
        _newRecipeIngredients.update { list ->
            list.filterIndexed { i, _ -> i != index }
        }
    }

    fun addReview(recipeId: String, rating: Int, comment: String, highlights: Set<String> = emptySet()) {
        val cleanComment = comment.trim()
        if (cleanComment.isBlank()) return
        val formattedComment = if (highlights.isNotEmpty()) {
            "[${highlights.joinToString(", ")}] $cleanComment"
        } else {
            cleanComment
        }
        val authorName = loggedInName.value.ifBlank { "Member" }
        val newReview = ReviewItem(
            id = "rev_${java.util.UUID.randomUUID()}",
            userName = authorName,
            rating = rating,
            timeAgo = "Just now",
            comment = formattedComment
        )
        viewModelScope.launch {
            repository.addReview(recipeId, newReview)
        }
    }

    suspend fun register(fullName: String, email: String, password: String, rememberMe: Boolean = false): Result<Unit> {
        return repository.registerUser(fullName, email, password, rememberMe)
    }

    suspend fun login(email: String, password: String, rememberMe: Boolean = false): Result<Unit> {
        return repository.loginUser(email, password, rememberMe)
    }

    suspend fun continueAsGuest() {
        repository.logoutUser()
        _filterState.value = FilterState()
        _searchQuery.value = ""
        _currentTab.value = BottomTab.DISCOVER
    }

    suspend fun checkEmailExists(email: String): Result<Unit> {
        return repository.checkEmailExists(email)
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Unit> {
        return repository.resetPassword(email, newPassword)
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutUser()
            _filterState.value = FilterState()
            _searchQuery.value = ""
        }
    }

    fun deleteAccount(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteAccount()
            onSuccess()
        }
    }

    fun publishNewRecipe(asDraft: Boolean = false, onSuccess: () -> Unit) {
        if (!asDraft && _newRecipeIngredients.value.isEmpty()) {
            return
        }
        val title = _newRecipeTitle.value.trim().ifBlank { "Homemade Recipe" }
        val author = loggedInName.value.ifBlank { "My Cooking" }
        val servings = _newRecipeServings.value
        val durationText = _newRecipeDuration.value
        val durationMinutes = when {
            durationText.contains("< 15", ignoreCase = true) -> 15
            durationText.contains("45", ignoreCase = true) -> 45
            else -> 30
        }
        val rawIngredients = _newRecipeIngredients.value
        val recipeId = "user_rec_${java.util.UUID.randomUUID()}"

        val ingredientsList = rawIngredients.mapIndexed { idx, item ->
            Ingredient(
                id = "${recipeId}_$idx",
                name = item.name,
                subtitle = "Fresh ingredient",
                baseAmount = item.amount,
                unit = item.unit,
                category = item.category,
                isChecked = false,
                allergenTags = emptyList(),
                scalingMode = item.scalingMode
            )
        }

        val step1Duration = (durationMinutes / 3).coerceAtLeast(3)
        val step2Duration = (durationMinutes - step1Duration).coerceAtLeast(5)
        val stepsList = listOf(
            RecipeStep(
                stepNumber = 1,
                title = _newRecipeStep1Title.value.trim().ifBlank { "Prep & Sear" },
                description = _newRecipeStep1Desc.value.trim().ifBlank { "Prepare ingredients and begin cooking." },
                durationMinutes = step1Duration,
                isFinalStep = false,
                tip = "Keep heat balanced for even cooking."
            ),
            RecipeStep(
                stepNumber = 2,
                title = _newRecipeStep2Title.value.trim().ifBlank { "Simmer & Serve" },
                description = _newRecipeStep2Desc.value.trim().ifBlank { "Finish cooking and season to taste." },
                durationMinutes = step2Duration,
                isFinalStep = true,
                tip = "Taste before serving and adjust seasoning."
            )
        )

        val newRecipe = Recipe(
            id = recipeId,
            title = title,
            chefName = author,
            difficulty = "Medium",
            durationMinutes = durationMinutes,
            calories = 380,
            rating = 0.0,
            reviewCount = 0,
            isBookmarked = !asDraft,
            category = _newRecipeCategory.value,
            dietaryBadges = _newRecipeDietaryBadges.value.toList(),
            allergens = emptyList(),
            defaultServings = servings,
            ingredients = ingredientsList,
            steps = stepsList,
            reviews = emptyList(),
            isDraft = asDraft,
            imageUri = _newRecipeImageUri.value
        )

        viewModelScope.launch {
            repository.publishUserRecipe(newRecipe)
            _newRecipeTitle.value = ""
            _newRecipeImageUri.value = ""
            _newRecipeIngredients.value = emptyList()
            _newRecipeCategory.value = "Dinner"
            _newRecipeDietaryBadges.value = setOf("Gluten-Free")
            _newRecipeStep1Title.value = "Prep & Sear"
            _newRecipeStep1Desc.value = "Prepare fresh ingredients and sear in a warm skillet until golden brown."
            _newRecipeStep2Title.value = "Simmer & Serve"
            _newRecipeStep2Desc.value = "Combine seasoning, simmer gently until cooked through, and serve warm."
            onSuccess()
        }
    }

    fun publishExistingDraft(recipe: Recipe) {
        if (recipe.ingredients.isEmpty()) return
        viewModelScope.launch {
            repository.publishUserRecipe(recipe.copy(isDraft = false, isBookmarked = true))
        }
    }

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            repository.deleteUserRecipe(recipeId)
        }
    }
}
