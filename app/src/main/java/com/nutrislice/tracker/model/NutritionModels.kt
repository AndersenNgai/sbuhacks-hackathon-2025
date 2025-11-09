package com.nutrislice.tracker.model

import androidx.annotation.DrawableRes
import com.nutrislice.tracker.R
import kotlinx.serialization.Serializable

@Serializable
data class NutritionGoals(
    val calories: Int = 2000,
    val protein: Int = 150,
    val carbs: Int = 250,
    val fat: Int = 65,
    val fiber: Int = 25
)

val DefaultGoals = NutritionGoals()

@Serializable
data class MealEntry(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val category: String,
    val mealTime: String,
    val station: String,
    val imageUrl: String? = null,
    val nutritionFacts: NutritionFacts? = null,
    val dietaryInfo: List<DietaryInfo> = emptyList(),
    val ingredients: List<String> = emptyList(),
    val servings: Double = 1.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class MealInput(
    val name: String = "",
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val fiber: String = "",
    val category: String = "",
    val mealTime: String = "",
    val station: String = "",
    val imageUrl: String? = null
) {
    fun asMeal(): MealEntry? {
        if (name.isBlank() || calories.isBlank()) return null
        val calorieValue = calories.toDoubleOrNull() ?: return null
        val proteinValue = protein.toDoubleOrNull() ?: 0.0
        val carbsValue = carbs.toDoubleOrNull() ?: 0.0
        val fatValue = fat.toDoubleOrNull() ?: 0.0
        val fiberValue = fiber.toDoubleOrNull() ?: 0.0
        return MealEntry(
            name = name.trim(),
            calories = calorieValue,
            protein = proteinValue,
            carbs = carbsValue,
            fat = fatValue,
            fiber = fiberValue,
            category = category,
            mealTime = mealTime,
            station = station,
            imageUrl = imageUrl
        )
    }
}

data class MacroTotals(
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0
)

fun List<MealEntry>.totals(): MacroTotals = fold(MacroTotals()) { acc, meal ->
    MacroTotals(
        calories = acc.calories + meal.calories,
        protein = acc.protein + meal.protein,
        carbs = acc.carbs + meal.carbs,
        fat = acc.fat + meal.fat,
        fiber = acc.fiber + meal.fiber
    )
}

data class Location(val name: String, val imageUrl: String)

@Serializable
data class NutritionFacts(
    val servingSize: String,
    val calories: Int,
    val totalFat: String,
    val saturatedFat: String,
    val transFat: String,
    val cholesterol: String,
    val sodium: String,
    val totalCarbohydrate: String,
    val dietaryFiber: String,
    val totalSugars: String
)

@Serializable
enum class DietaryInfo(@DrawableRes val icon: Int) {
    VEGAN(R.drawable.ic_vegan),
    VEGETARIAN(R.drawable.ic_vegetarian),
    CONTAINS_FISH(R.drawable.ic_fish),
    CONTAINS_EGG(R.drawable.ic_egg),
    GLUTEN_FREE(R.drawable.ic_gluten_free),
    CONTAINS_DAIRY(R.drawable.ic_dairy)
}

@Serializable
enum class DietaryRestriction {
    VEGETARIAN,
    VEGAN,
    GLUTEN_FREE
}

@Serializable
data class UserProfile(
    val age: Int = 0,
    val gender: String = "",
    val year: String = "",
    val restrictions: Set<DietaryRestriction> = emptySet()
)

/**
 * Represents a meal plan suggestion from NeuralSeek
 */
@Serializable
data class MealPlanSuggestion(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val description: String,
    val days: Int,
    val generatedAt: Long = System.currentTimeMillis(),
    val suggestions: String, // The AI-generated meal plan text
    val mealEntries: List<MealEntry> = emptyList() // Parsed meal entries if available
)

/**
 * Represents a single meal suggestion for a specific meal time
 */
@Serializable
data class MealTimeSuggestion(
    val id: String = System.currentTimeMillis().toString(),
    val mealTime: String,
    val suggestions: String, // The AI-generated suggestions text
    val generatedAt: Long = System.currentTimeMillis()
)
