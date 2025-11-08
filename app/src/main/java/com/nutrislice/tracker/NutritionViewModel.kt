package com.nutrislice.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nutrislice.tracker.data.NutritionRepository
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
        val locations: List<Location> = emptyList(),
        val userProfile: UserProfile = UserProfile(),
        val isLoading: Boolean = true,
        val userMessage: String? = null
    )

    private val userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<NutritionUiState> = combine(
        repository.goals,
        repository.meals,
        repository.streak,
        repository.getMenu(),
        repository.getLocations(),
        repository.userProfile,
        userMessage
    ) { flows ->
        val goals = flows[0] as NutritionGoals
        val allMeals = flows[1] as List<MealEntry>
        val streak = flows[2] as Int
        val menu = flows[3] as List<MealEntry>
        val locations = flows[4] as List<Location>
        val userProfile = flows[5] as UserProfile
        val message = flows[6] as String?

        val todaysMeals = allMeals.filter { isToday(it.timestamp) }
        NutritionUiState(
            goals = goals,
            meals = todaysMeals,
            streak = streak,
            menu = menu,
            locations = locations,
            userProfile = userProfile,
            isLoading = false,
            userMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NutritionUiState()
    )

    init {
        viewModelScope.launch {
            checkStreakOnAppStart()
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
