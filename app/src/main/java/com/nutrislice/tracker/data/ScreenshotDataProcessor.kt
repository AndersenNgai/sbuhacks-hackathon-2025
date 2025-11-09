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
        // Example: Scrambled Eggs from your first screenshot
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
        )
        // Add more items here as you provide screenshots
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

