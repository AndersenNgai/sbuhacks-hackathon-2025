package com.nutrislice.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nutrislice.tracker.data.NutritionRepository
import com.nutrislice.tracker.model.FoodCategory
import com.nutrislice.tracker.model.MealEntry
import com.nutrislice.tracker.model.MealInput
import com.nutrislice.tracker.model.Location
import com.nutrislice.tracker.model.NutritionGoals
import com.nutrislice.tracker.model.UserProfile
import com.nutrislice.tracker.model.totals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class NutritionViewModel(private val repository: NutritionRepository) : ViewModel() {

    data class NutritionUiState(
        val goals: NutritionGoals = NutritionGoals(),
        val meals: List<MealEntry> = emptyList(),
        val streak: Int = 0,
        val menu: List<MealEntry> = emptyList(),
        val categories: List<FoodCategory> = emptyList(),
        val locations: List<Location> = emptyList(),
        val userProfile: UserProfile = UserProfile(),
        val isLoading: Boolean = true,
        val isFetchingMenu: Boolean = false,
        val userMessage: String? = null,
        val mealPlanSuggestion: String? = null,
        val isGeneratingMealPlan: Boolean = false,
        val mealTimeSuggestion: String? = null,
        val isGeneratingMealTimeSuggestion: Boolean = false
    )

    private val userMessage = MutableStateFlow<String?>(null)
    private val isFetchingMenu = MutableStateFlow(false)
    private val categories = MutableStateFlow<List<FoodCategory>>(emptyList())
    private val mealPlanSuggestion = MutableStateFlow<String?>(null)
    private val isGeneratingMealPlan = MutableStateFlow(false)
    private val mealTimeSuggestion = MutableStateFlow<String?>(null)
    private val isGeneratingMealTimeSuggestion = MutableStateFlow(false)

    val uiState: StateFlow<NutritionUiState> = combine(
        repository.goals,
        repository.meals,
        repository.streak,
        repository.getMenu(),
        repository.getLocations(),
        repository.userProfile,
        userMessage,
        isFetchingMenu,
        categories,
        mealPlanSuggestion,
        isGeneratingMealPlan,
        mealTimeSuggestion,
        isGeneratingMealTimeSuggestion
    ) { flows ->
        val goals = flows[0] as NutritionGoals
        val allMeals = flows[1] as List<MealEntry>
        val streak = flows[2] as Int
        val menu = flows[3] as List<MealEntry>
        val locations = flows[4] as List<Location>
        val userProfile = flows[5] as UserProfile
        val message = flows[6] as String?
        val fetching = flows[7] as Boolean
        val cats = flows[8] as List<FoodCategory>
        val mealPlan = flows[9] as String?
        val generatingPlan = flows[10] as Boolean
        val mealTime = flows[11] as String?
        val generatingMealTime = flows[12] as Boolean

        val todaysMeals = allMeals.filter { isToday(it.timestamp) }
        NutritionUiState(
            goals = goals,
            meals = todaysMeals,
            streak = streak,
            menu = menu,
            categories = cats,
            locations = locations,
            userProfile = userProfile,
            isLoading = false,
            isFetchingMenu = fetching,
            userMessage = message,
            mealPlanSuggestion = mealPlan,
            isGeneratingMealPlan = generatingPlan,
            mealTimeSuggestion = mealTime,
            isGeneratingMealTimeSuggestion = generatingMealTime
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NutritionUiState()
    )

    init {
        viewModelScope.launch {
            checkStreakOnAppStart()
            // Optionally auto-fetch menu on app start (uncomment if desired)
            // fetchMenuFromWeb()
        }
    }

    private suspend fun checkStreakOnAppStart() {
        val lastDay = repository.lastDay.first()
        if (!isToday(lastDay) && !isYesterday(lastDay)) {
            repository.updateStreak(0, 0L)
        }
    }

    fun saveGoals(goals: NutritionGoals) {
        viewModelScope.launch {
            repository.saveGoals(goals)
            userMessage.value = "Goals updated"
            checkAndUpdateStreak()
        }
    }

    fun saveUserProfile(userProfile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(userProfile)
            userMessage.value = "Profile updated"
        }
    }

    fun addMeal(input: MealInput) {
        val meal = input.asMeal()
        if (meal == null) {
            userMessage.value = "Please add a name and calories"
            return
        }
        viewModelScope.launch {
            repository.addMeal(meal)
            userMessage.value = "Meal logged"
            checkAndUpdateStreak()
        }
    }

    fun addMeals(meals: List<MealEntry>) {
        viewModelScope.launch {
            val newMeals = meals.map { it.copy(id = System.nanoTime()) } // Use nanoTime for uniqueness
            repository.addMeals(newMeals)
            userMessage.value = "Meals logged"
            checkAndUpdateStreak()
        }
    }

    fun deleteMeal(mealId: Long) {
        viewModelScope.launch {
            repository.deleteMeal(mealId)
            userMessage.value = "Meal removed"
            checkAndUpdateStreak()
        }
    }

    private suspend fun checkAndUpdateStreak() {
        val goals = repository.goals.first()
        val allMeals = repository.meals.first()
        val todaysMeals = allMeals.filter { isToday(it.timestamp) }
        val totals = todaysMeals.totals()
        val lastDay = repository.lastDay.first()
        val currentStreak = repository.streak.first()

        val goalsMet = totals.calories >= goals.calories &&
            totals.protein >= goals.protein &&
            totals.carbs >= goals.carbs &&
            totals.fat >= goals.fat &&
            totals.fiber >= goals.fiber

        if (goalsMet) {
            if (!isToday(lastDay)) {
                val newStreak = if (isYesterday(lastDay)) currentStreak + 1 else 1
                repository.updateStreak(newStreak, System.currentTimeMillis())
            }
        }
    }

    fun consumeMessage() {
        userMessage.value = null
    }

    /**
     * Fetches menu items from the web using the scraper
     */
    fun fetchMenuFromWeb(url: String = "https://stonybrook.nutrislice.com/menu/east-side-dining") {
        viewModelScope.launch {
            isFetchingMenu.value = true
            userMessage.value = null

            repository.fetchMenuFromWeb(url).onSuccess { menuItems ->
                if (menuItems.isEmpty()) {
                    userMessage.value = "No menu items found. Trying alternative methods..."
                    // Try fetching categories as a fallback
                    repository.fetchCategories(url).onSuccess { categories ->
                        if (categories.isNotEmpty()) {
                            userMessage.value = "Found ${categories.size} categories but no items. The API may require authentication or use a different format."
                        } else {
                            userMessage.value = "Unable to fetch menu data. The website structure may have changed or requires authentication."
                        }
                    }
                } else {
                    userMessage.value = "Successfully fetched ${menuItems.size} menu items"
                }
            }.onFailure { error ->
                val errorMsg = error.message ?: "Unknown error"
                userMessage.value = "Error: $errorMsg. Please check your internet connection and try again."
            }

            isFetchingMenu.value = false
        }
    }

    /**
     * Fetches food categories from the web using the scraper
     */
    fun fetchCategories(url: String = "https://stonybrook.nutrislice.com/menu/east-side-dining") {
        viewModelScope.launch {
            isFetchingMenu.value = true
            userMessage.value = null

            repository.fetchCategories(url).onSuccess { cats ->
                categories.value = cats
                userMessage.value = "Fetched ${cats.size} categories"
            }.onFailure { error ->
                userMessage.value = "Error fetching categories: ${error.message}"
            }

            isFetchingMenu.value = false
        }
    }

    /**
     * Generates a meal plan suggestion using NeuralSeek
     */
    fun generateMealPlan(days: Int = 7, preferences: String = "") {
        viewModelScope.launch {
            isGeneratingMealPlan.value = true
            mealPlanSuggestion.value = null
            userMessage.value = null

            repository.getMealPlanSuggestions(days, preferences).onSuccess { suggestion ->
                mealPlanSuggestion.value = suggestion
                userMessage.value = "Meal plan generated successfully!"
            }.onFailure { error ->
                val errorMsg = error.message ?: "Unknown error"
                userMessage.value = "Error generating meal plan: $errorMsg"
            }

            isGeneratingMealPlan.value = false
        }
    }

    /**
     * Generates meal suggestions for a specific meal time using NeuralSeek
     */
    fun generateMealTimeSuggestion(mealTime: String, preferences: String = "") {
        viewModelScope.launch {
            isGeneratingMealTimeSuggestion.value = true
            mealTimeSuggestion.value = null
            userMessage.value = null

            repository.getMealTimeSuggestions(mealTime, preferences).onSuccess { suggestion ->
                mealTimeSuggestion.value = suggestion
                userMessage.value = "$mealTime suggestions generated!"
            }.onFailure { error ->
                val errorMsg = error.message ?: "Unknown error"
                userMessage.value = "Error generating suggestions: $errorMsg"
            }

            isGeneratingMealTimeSuggestion.value = false
        }
    }

    /**
     * Clears the current meal plan suggestion
     */
    fun clearMealPlanSuggestion() {
        mealPlanSuggestion.value = null
    }

    /**
     * Clears the current meal time suggestion
     */
    fun clearMealTimeSuggestion() {
        mealTimeSuggestion.value = null
    }

    /**
     * Loads menu items from screenshot data
     */
    fun loadScreenshotData() {
        viewModelScope.launch {
            userMessage.value = "Loading screenshot data..."
            repository.loadScreenshotMenuItems().onSuccess { items ->
                userMessage.value = "Loaded ${items.size} items from screenshots"
            }.onFailure { error ->
                userMessage.value = "Error loading screenshot data: ${error.message}"
            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isToday(timestamp: Long): Boolean {
        if (timestamp == 0L) return false
        val today = Calendar.getInstance()
        val comparison = Calendar.getInstance().apply { timeInMillis = timestamp }
        return isSameDay(today, comparison)
    }

    private fun isYesterday(timestamp: Long): Boolean {
        if (timestamp == 0L) return false
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val comparison = Calendar.getInstance().apply { timeInMillis = timestamp }
        return isSameDay(yesterday, comparison)
    }
}

class NutritionViewModelFactory(
    private val repository: NutritionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NutritionViewModel::class.java)) {
            return NutritionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}
