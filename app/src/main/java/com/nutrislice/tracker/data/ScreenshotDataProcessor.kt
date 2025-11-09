package com.nutrislice.tracker.data

import android.util.Log
import com.nutrislice.tracker.model.DietaryInfo
import com.nutrislice.tracker.model.MealEntry
import com.nutrislice.tracker.model.NutritionFacts
import java.util.regex.Pattern

/**
 * Processes screenshot data and extracts menu item information
 * This can be populated with data extracted from screenshots
 */
object ScreenshotDataProcessor {
    
    /**
     * Pre-populated menu items extracted from screenshots
     * Add new items here as you provide more screenshots
     */
    val extractedMenuItems: List<MealEntry> = listOf(
        // Breakfast item
        MealEntry(
            name = "Scrambled Eggs with Cream and Butter",
            calories = 170.0,
            protein = 10.0,
            carbs = 1.0,
            fat = 14.0,
            fiber = 0.0,
            category = "Breakfast",
            mealTime = "Breakfast",
            station = "Breakfast Specials",
            nutritionFacts = NutritionFacts(
                servingSize = "0.5 cups",
                calories = 170,
                totalFat = "14g",
                saturatedFat = "6g",
                transFat = "0g",
                cholesterol = "317mg",
                sodium = "400mg",
                totalCarbohydrate = "1g",
                dietaryFiber = "0g",
                totalSugars = "0g"
            ),
            dietaryInfo = listOf(
                DietaryInfo.VEGETARIAN,
                DietaryInfo.CONTAINS_DAIRY,
                DietaryInfo.CONTAINS_EGG
            )
        ),
        
        // Lunch/Dinner items
        MealEntry(
            name = "Kidney Bean, Green Chilis & Tomato Curry",
            calories = 150.0,
            protein = 5.0,
            carbs = 18.0,
            fat = 8.0,
            fiber = 5.0,
            category = "Main Course",
            mealTime = "Lunch",
            station = "Vegetarian Station",
            nutritionFacts = NutritionFacts(
                servingSize = "1 cups",
                calories = 150,
                totalFat = "8g",
                saturatedFat = "0.5g",
                transFat = "0g",
                cholesterol = "0mg",
                sodium = "420mg",
                totalCarbohydrate = "18g",
                dietaryFiber = "5g",
                totalSugars = "0g"
            ),
            dietaryInfo = listOf(
                DietaryInfo.VEGETARIAN,
                DietaryInfo.VEGAN
            ),
            ingredients = listOf(
                "Water", "Low Sodium Kidney Beans", "Tomatoes", "White Onions", 
                "Canola Oil", "Curry Leaf", "Coriander", "Kosher Salt", "Jalapeno", 
                "Garlic", "Cilantro", "Chili Powder", "Turmeric", "Cumin", 
                "Ginger Root", "Garam Masala"
            )
        ),
        
        MealEntry(
            name = "Roasted Cherry Tomatoes and Broccoli",
            calories = 70.0,
            protein = 2.0,
            carbs = 6.0,
            fat = 5.0,
            fiber = 2.0,
            category = "Side Dish",
            mealTime = "Lunch",
            station = "Vegetable Station",
            nutritionFacts = NutritionFacts(
                servingSize = "4 oz",
                calories = 70,
                totalFat = "5g",
                saturatedFat = "0g",
                transFat = "0g",
                cholesterol = "0mg",
                sodium = "250mg",
                totalCarbohydrate = "6g",
                dietaryFiber = "2g",
                totalSugars = "2g"
            ),
            dietaryInfo = listOf(
                DietaryInfo.VEGETARIAN,
                DietaryInfo.VEGAN
            )
        ),
        
        MealEntry(
            name = "Roasted Eggplant",
            calories = 40.0,
            protein = 1.0,
            carbs = 7.0,
            fat = 1.5,
            fiber = 4.0,
            category = "Side Dish",
            mealTime = "Lunch",
            station = "Vegetable Station",
            nutritionFacts = NutritionFacts(
                servingSize = "4 oz",
                calories = 40,
                totalFat = "1.5g",
                saturatedFat = "0g",
                transFat = "0g",
                cholesterol = "0mg",
                sodium = "250mg",
                totalCarbohydrate = "7g",
                dietaryFiber = "4g",
                totalSugars = "4g"
            ),
            dietaryInfo = listOf(
                DietaryInfo.VEGETARIAN,
                DietaryInfo.VEGAN
            )
        ),
        
        MealEntry(
            name = "Creamy Rigatoni alla Vodka",
            calories = 310.0,
            protein = 7.0,
            carbs = 33.0,
            fat = 13.0,
            fiber = 4.0,
            category = "Main Course",
            mealTime = "Dinner",
            station = "Pasta Station",
            nutritionFacts = NutritionFacts(
                servingSize = "8 oz",
                calories = 310,
                totalFat = "13g",
                saturatedFat = "7g",
                transFat = "0g",
                cholesterol = "31mg",
                sodium = "420mg",
                totalCarbohydrate = "33g",
                dietaryFiber = "4g",
                totalSugars = "7g"
            ),
            dietaryInfo = listOf(
                DietaryInfo.VEGETARIAN,
                DietaryInfo.CONTAINS_DAIRY
            )
        ),
        
        MealEntry(
            name = "Pepper Jack Chicken Mac & Cheese",
            calories = 360.0,
            protein = 22.0,
            carbs = 28.0,
            fat = 18.0,
            fiber = 2.0,
            category = "Main Course",
            mealTime = "Dinner",
            station = "Comfort Food Station",
            nutritionFacts = NutritionFacts(
                servingSize = "8 oz",
                calories = 360,
                totalFat = "18g",
                saturatedFat = "10g",
                transFat = "0g",
                cholesterol = "69mg",
                sodium = "760mg",
                totalCarbohydrate = "28g",
                dietaryFiber = "2g",
                totalSugars = "5g"
            ),
            dietaryInfo = listOf(
                DietaryInfo.CONTAINS_DAIRY
            )
        ),
        
        MealEntry(
            name = "Cooked Pasta with Oil",
            calories = 340.0,
            protein = 11.0,
            carbs = 65.0,
            fat = 3.0,
            fiber = 3.0,
            category = "Main Course",
            mealTime = "Lunch",
            station = "Pasta Station",
            nutritionFacts = NutritionFacts(
                servingSize = "1 cups",
                calories = 340,
                totalFat = "3g",
                saturatedFat = "0g",
                transFat = "0g",
                cholesterol = "0mg",
                sodium = "10mg",
                totalCarbohydrate = "65g",
                dietaryFiber = "3g",
                totalSugars = "2g"
            ),
            dietaryInfo = listOf(
                DietaryInfo.VEGETARIAN,
                DietaryInfo.VEGAN
            )
        ),
        
        MealEntry(
            name = "Marinara Sauce",
            calories = 430.0,
            protein = 17.0,
            carbs = 76.0,
            fat = 14.0,
            fiber = 19.0,
            category = "Sauce",
            mealTime = "All Day",
            station = "Sauce Station",
            nutritionFacts = NutritionFacts(
                servingSize = "4 oz portion",
                calories = 430,
                totalFat = "14g",
                saturatedFat = "2g",
                transFat = "0g",
                cholesterol = "0mg",
                sodium = "2360mg",
                totalCarbohydrate = "76g",
                dietaryFiber = "19g",
                totalSugars = "0g"
            ),
            dietaryInfo = listOf(
                DietaryInfo.VEGETARIAN,
                DietaryInfo.VEGAN
            )
        ),
        
        MealEntry(
            name = "Alfredo Sauce",
            calories = 150.0,
            protein = 2.0,
            carbs = 3.0,
            fat = 15.0,
            fiber = 0.0,
            category = "Sauce",
            mealTime = "All Day",
            station = "Sauce Station",
            nutritionFacts = NutritionFacts(
                servingSize = "8 fl oz",
                calories = 150,
                totalFat = "15g",
                saturatedFat = "10g",
                transFat = "0g",
                cholesterol = "46mg",
                sodium = "35mg",
                totalCarbohydrate = "3g",
                dietaryFiber = "0g",
                totalSugars = "1g"
            ),
            dietaryInfo = listOf(
                DietaryInfo.VEGETARIAN,
                DietaryInfo.CONTAINS_DAIRY
            ),
            ingredients = listOf(
                "Cream sauce flavored with garlic, onions, and parmesan cheese"
            )
        )
    )

    /**
     * Parses nutrition facts from text extracted from screenshots
     * This can be used if implementing OCR in the future
     */
    fun parseNutritionFactsFromText(text: String): NutritionFacts? {
        return try {
            val servingSize = extractValue(text, "Serving Size[:\\s]+([0-9.]+\\s*(cups?|oz|g|ml))", 1) ?: "1 serving"
            val calories = extractNumericValue(text, "Calories[:\\s]+([0-9]+)") ?: 0
            val totalFat = extractValue(text, "Total Fat[:\\s]+([0-9.]+g?)", 1) ?: "0g"
            val saturatedFat = extractValue(text, "Saturated Fat[:\\s]+([0-9.]+g?)", 1) ?: "0g"
            val transFat = extractValue(text, "Trans Fat[:\\s]+([0-9.]+g?)", 1) ?: "0g"
            val cholesterol = extractValue(text, "Cholesterol[:\\s]+([0-9.]+mg?)", 1) ?: "0mg"
            val sodium = extractValue(text, "Sodium[:\\s]+([0-9.]+mg?)", 1) ?: "0mg"
            val totalCarb = extractValue(text, "Total Carbohydrate[:\\s]+([0-9.]+g?)", 1) ?: "0g"
            val fiber = extractValue(text, "Dietary Fiber[:\\s]+([0-9.]+g?)", 1) ?: "0g"
            val sugars = extractValue(text, "Total Sugars[:\\s]+([0-9.]+g?)", 1) ?: "0g"

            NutritionFacts(
                servingSize = servingSize,
                calories = calories,
                totalFat = totalFat,
                saturatedFat = saturatedFat,
                transFat = transFat,
                cholesterol = cholesterol,
                sodium = sodium,
                totalCarbohydrate = totalCarb,
                dietaryFiber = fiber,
                totalSugars = sugars
            )
        } catch (e: Exception) {
            Log.e("ScreenshotProcessor", "Error parsing nutrition facts", e)
            null
        }
    }

    /**
     * Extracts dietary info from text or icons
     */
    fun parseDietaryInfo(text: String, icons: List<String> = emptyList()): List<DietaryInfo> {
        val info = mutableListOf<DietaryInfo>()
        val lowerText = text.lowercase()
        
        if (lowerText.contains("vegetarian") || icons.contains("vegetarian")) {
            info.add(DietaryInfo.VEGETARIAN)
        }
        if (lowerText.contains("vegan") || icons.contains("vegan")) {
            info.add(DietaryInfo.VEGAN)
        }
        if (lowerText.contains("gluten") && lowerText.contains("free") || icons.contains("gluten-free")) {
            info.add(DietaryInfo.GLUTEN_FREE)
        }
        if (lowerText.contains("dairy") || lowerText.contains("milk") || icons.contains("milk")) {
            info.add(DietaryInfo.CONTAINS_DAIRY)
        }
        if (lowerText.contains("egg") || icons.contains("egg")) {
            info.add(DietaryInfo.CONTAINS_EGG)
        }
        if (lowerText.contains("fish") || icons.contains("fish")) {
            info.add(DietaryInfo.CONTAINS_FISH)
        }
        
        return info
    }

    private fun extractValue(text: String, pattern: String, group: Int): String? {
        val regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
        val matcher = regex.matcher(text)
        return if (matcher.find()) matcher.group(group)?.trim() else null
    }

    private fun extractNumericValue(text: String, pattern: String): Int? {
        val value = extractValue(text, pattern, 1)
        return value?.toIntOrNull()
    }
}

