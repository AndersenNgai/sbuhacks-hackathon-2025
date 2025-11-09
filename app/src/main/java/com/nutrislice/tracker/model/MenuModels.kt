package com.nutrislice.tracker.model

import kotlinx.serialization.Serializable

/**
 * Represents a food category from the menu (e.g., "Breakfast", "Lunch", "Desserts")
 */
@Serializable
data class FoodCategory(
    val id: String,
    val name: String,
    val description: String? = null,
    val station: String? = null
)

/**
 * Represents a food item from the menu
 */
@Serializable
data class ScrapedMenuItem(
    val id: String,
    val name: String,
    val category: String,
    val station: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val nutritionFacts: NutritionFacts? = null,
    val dietaryInfo: List<DietaryInfo> = emptyList(),
    val ingredients: List<String> = emptyList(),
    val mealTime: String? = null
)

/**
 * Result of scraping operation
 */
data class ScrapedMenuData(
    val categories: List<FoodCategory>,
    val items: List<ScrapedMenuItem>,
    val location: String = "East Side Dining"
)

