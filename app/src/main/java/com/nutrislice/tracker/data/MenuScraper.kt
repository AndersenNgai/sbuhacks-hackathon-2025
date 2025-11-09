package com.nutrislice.tracker.data

import com.nutrislice.tracker.model.DietaryInfo
import com.nutrislice.tracker.model.FoodCategory
import com.nutrislice.tracker.model.NutritionFacts
import com.nutrislice.tracker.model.ScrapedMenuItem
import com.nutrislice.tracker.model.ScrapedMenuData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.concurrent.TimeUnit

/**
 * Service to scrape menu data from Nutrislice website
 */
class MenuScraper {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Scrapes food categories and items from the Nutrislice menu page
     */
    suspend fun scrapeMenu(url: String = "https://stonybrook.nutrislice.com/menu/east-side-dining"): Result<ScrapedMenuData> {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch the HTML content
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Failed to fetch menu: ${response.code}")
                    )
                }

                val html = response.body?.string() ?: return@withContext Result.failure(
                    Exception("Empty response body")
                )

                // Parse the HTML
                val doc = Jsoup.parse(html)

                // Extract categories and items
                val categories = extractCategories(doc)
                val items = extractMenuItems(doc, categories)

                Result.success(
                    ScrapedMenuData(
                        categories = categories,
                        items = items
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Extracts food categories from the HTML document
     */
    private fun extractCategories(doc: Document): List<FoodCategory> {
        val categories = mutableListOf<FoodCategory>()

        // Try multiple selectors that Nutrislice might use
        val categorySelectors = listOf(
            "h2.menu-category-name",
            ".menu-category-name",
            ".menu-section-title",
            ".station-name",
            "h2[class*='category']",
            "h2[class*='station']",
            ".menu-station",
            ".menu-category"
        )

        var categoryIndex = 0
        for (selector in categorySelectors) {
            val elements = doc.select(selector)
            if (elements.isNotEmpty()) {
                elements.forEach { element ->
                    val name = element.text().trim()
                    if (name.isNotBlank()) {
                        categories.add(
                            FoodCategory(
                                id = "category_${categoryIndex++}",
                                name = name,
                                station = name
                            )
                        )
                    }
                }
                break // Use the first selector that finds elements
            }
        }

        // If no categories found with standard selectors, try to find by data attributes
        if (categories.isEmpty()) {
            doc.select("[data-category-name], [data-station-name]").forEach { element ->
                val name = element.attr("data-category-name").ifBlank {
                    element.attr("data-station-name")
                }
                if (name.isNotBlank()) {
                    categories.add(
                        FoodCategory(
                            id = "category_${categoryIndex++}",
                            name = name,
                            station = name
                        )
                    )
                }
            }
        }

        return categories.distinctBy { it.name }
    }

    /**
     * Extracts menu items from the HTML document
     */
    private fun extractMenuItems(doc: Document, categories: List<FoodCategory>): List<ScrapedMenuItem> {
        val items = mutableListOf<ScrapedMenuItem>()
        var itemIndex = 0

        // Try multiple selectors for menu items
        val itemSelectors = listOf(
            ".menu-item",
            ".menu-item-name",
            "[data-menu-item]",
            ".food-item",
            ".item-name"
        )

        for (selector in itemSelectors) {
            val elements = doc.select(selector)
            if (elements.isNotEmpty()) {
                elements.forEach { element ->
                    val item = extractMenuItemFromElement(element, itemIndex++, categories)
                    if (item != null) {
                        items.add(item)
                    }
                }
                break // Use the first selector that finds elements
            }
        }

        // If no items found, try to find items near category headers
        if (items.isEmpty() && categories.isNotEmpty()) {
            categories.forEach { category ->
                // Find elements that might be items under this category
                doc.select("h2, h3, .menu-category-name").forEach { header ->
                    if (header.text().contains(category.name, ignoreCase = true)) {
                        // Look for items in the same section
                        var current = header.nextElementSibling()
                        var itemCount = 0
                        while (current != null && itemCount < 20) {
                            val itemName = current.select("h3, h4, .item-name, .menu-item-name").firstOrNull()?.text()
                            if (itemName != null && itemName.isNotBlank()) {
                                items.add(
                                    ScrapedMenuItem(
                                        id = "item_${itemIndex++}",
                                        name = itemName.trim(),
                                        category = category.name,
                                        station = category.station
                                    )
                                )
                                itemCount++
                            }
                            current = current.nextElementSibling()
                        }
                    }
                }
            }
        }

        return items.distinctBy { "${it.name}_${it.category}" }
    }

    /**
     * Extracts a menu item from a DOM element
     */
    private fun extractMenuItemFromElement(
        element: Element,
        index: Int,
        categories: List<FoodCategory>
    ): ScrapedMenuItem? {
        val name = element.select("h3, h4, .item-name, .menu-item-name, [data-item-name]")
            .firstOrNull()?.text()?.trim()
            ?: element.text().trim()

        if (name.isBlank()) return null

        // Try to find the category this item belongs to
        var category = categories.firstOrNull()?.name ?: "Uncategorized"
        var parent = element.parent()
        while (parent != null && category == (categories.firstOrNull()?.name ?: "Uncategorized")) {
            val categoryHeader = parent.select("h2.menu-category-name, .menu-category-name, .station-name").firstOrNull()
            if (categoryHeader != null) {
                category = categoryHeader.text().trim()
                break
            }
            parent = parent.parent()
        }

        // Extract image URL
        val imageUrl = element.select("img").firstOrNull()?.attr("src")?.let { src ->
            if (src.startsWith("http")) src else "https://stonybrook.nutrislice.com$src"
        }

        // Extract description
        val description = element.select(".item-description, .description, [data-description]")
            .firstOrNull()?.text()?.trim()

        // Extract nutrition facts (if available)
        val nutritionFacts = extractNutritionFacts(element)

        // Extract dietary info
        val dietaryInfo = extractDietaryInfo(element)

        // Extract ingredients
        val ingredients = extractIngredients(element)

        return ScrapedMenuItem(
            id = "item_$index",
            name = name,
            category = category,
            station = categories.find { it.name == category }?.station,
            description = description,
            imageUrl = imageUrl,
            nutritionFacts = nutritionFacts,
            dietaryInfo = dietaryInfo,
            ingredients = ingredients
        )
    }

    /**
     * Extracts nutrition facts from an element
     */
    private fun extractNutritionFacts(element: Element): NutritionFacts? {
        val nutritionSection = element.select(".nutrition-facts, .nutrition-info, [data-nutrition]").firstOrNull()
            ?: return null

        // Try to extract nutrition values
        val calories = nutritionSection.select("[data-calories], .calories").firstOrNull()?.text()
            ?.replace(Regex("[^0-9]"), "")?.toIntOrNull() ?: 0

        val totalFat = nutritionSection.select("[data-fat], .fat, .total-fat").firstOrNull()?.text()?.trim() ?: "0g"
        val saturatedFat = nutritionSection.select("[data-saturated-fat], .saturated-fat").firstOrNull()?.text()?.trim() ?: "0g"
        val transFat = nutritionSection.select("[data-trans-fat], .trans-fat").firstOrNull()?.text()?.trim() ?: "0g"
        val cholesterol = nutritionSection.select("[data-cholesterol], .cholesterol").firstOrNull()?.text()?.trim() ?: "0mg"
        val sodium = nutritionSection.select("[data-sodium], .sodium").firstOrNull()?.text()?.trim() ?: "0mg"
        val totalCarbohydrate = nutritionSection.select("[data-carbs], .carbs, .total-carbohydrate").firstOrNull()?.text()?.trim() ?: "0g"
        val dietaryFiber = nutritionSection.select("[data-fiber], .fiber, .dietary-fiber").firstOrNull()?.text()?.trim() ?: "0g"
        val totalSugars = nutritionSection.select("[data-sugars], .sugars, .total-sugars").firstOrNull()?.text()?.trim() ?: "0g"

        return NutritionFacts(
            servingSize = "1 serving",
            calories = calories,
            totalFat = totalFat,
            saturatedFat = saturatedFat,
            transFat = transFat,
            cholesterol = cholesterol,
            sodium = sodium,
            totalCarbohydrate = totalCarbohydrate,
            dietaryFiber = dietaryFiber,
            totalSugars = totalSugars
        )
    }

    /**
     * Extracts dietary information from an element
     */
    private fun extractDietaryInfo(element: Element): List<DietaryInfo> {
        val dietaryInfo = mutableListOf<DietaryInfo>()
        val text = element.text().lowercase()

        // Check for dietary indicators in text or data attributes
        if (text.contains("vegan") || element.hasAttr("data-vegan")) {
            dietaryInfo.add(DietaryInfo.VEGAN)
        }
        if (text.contains("vegetarian") || element.hasAttr("data-vegetarian")) {
            dietaryInfo.add(DietaryInfo.VEGETARIAN)
        }
        if (text.contains("gluten-free") || element.hasAttr("data-gluten-free")) {
            dietaryInfo.add(DietaryInfo.GLUTEN_FREE)
        }
        if (text.contains("contains dairy") || text.contains("dairy") || element.hasAttr("data-dairy")) {
            dietaryInfo.add(DietaryInfo.CONTAINS_DAIRY)
        }
        if (text.contains("contains egg") || text.contains("egg") || element.hasAttr("data-egg")) {
            dietaryInfo.add(DietaryInfo.CONTAINS_EGG)
        }
        if (text.contains("fish") || element.hasAttr("data-fish")) {
            dietaryInfo.add(DietaryInfo.CONTAINS_FISH)
        }

        // Also check for icons or badges
        element.select(".dietary-icon, .dietary-badge, [class*='vegan'], [class*='vegetarian']").forEach { icon ->
            val classes = icon.className().lowercase()
            when {
                classes.contains("vegan") -> dietaryInfo.add(DietaryInfo.VEGAN)
                classes.contains("vegetarian") -> dietaryInfo.add(DietaryInfo.VEGETARIAN)
                classes.contains("gluten-free") -> dietaryInfo.add(DietaryInfo.GLUTEN_FREE)
                classes.contains("dairy") -> dietaryInfo.add(DietaryInfo.CONTAINS_DAIRY)
                classes.contains("egg") -> dietaryInfo.add(DietaryInfo.CONTAINS_EGG)
                classes.contains("fish") -> dietaryInfo.add(DietaryInfo.CONTAINS_FISH)
            }
        }

        return dietaryInfo.distinct()
    }

    /**
     * Extracts ingredients from an element
     */
    private fun extractIngredients(element: Element): List<String> {
        val ingredientsSection = element.select(".ingredients, .ingredient-list, [data-ingredients]").firstOrNull()
            ?: return emptyList()

        val ingredientsText = ingredientsSection.text()
        // Split by common delimiters
        return ingredientsText.split(Regex("[,;]"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}

