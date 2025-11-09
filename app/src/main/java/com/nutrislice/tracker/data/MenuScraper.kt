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
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Requests", "1")
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

                // Try to extract JSON data from script tags (Nutrislice often uses JSON)
                val jsonData = extractJsonData(html)
                if (jsonData != null) {
                    return@withContext jsonData
                }

                // Try to find Nutrislice API endpoints or embedded data
                val apiData = extractNutrisliceApiData(html)
                if (apiData != null) {
                    return@withContext apiData
                }

                // Parse the HTML
                val doc = Jsoup.parse(html)

                // Extract categories and items
                val categories = extractCategories(doc)
                val items = extractMenuItems(doc, categories)

                // If still no items, try more aggressive extraction
                val finalItems = if (items.isEmpty()) {
                    extractMenuItemsAggressive(doc)
                } else {
                    items
                }

                Result.success(
                    ScrapedMenuData(
                        categories = categories.ifEmpty { extractCategoriesAggressive(doc) },
                        items = finalItems
                    )
                )
            } catch (e: Exception) {
                Result.failure(Exception("Scraping error: ${e.message}", e))
            }
        }
    }

    /**
     * Tries to extract JSON data from script tags
     */
    private fun extractJsonData(html: String): Result<ScrapedMenuData>? {
        try {
            // Look for JSON data in script tags
            val scriptPattern = Regex("""<script[^>]*>(.*?)</script>""", RegexOption.DOT_MATCHES_ALL)
            val matches = scriptPattern.findAll(html)
            
            for (match in matches) {
                val scriptContent = match.groupValues[1]
                // Look for menu data patterns
                if (scriptContent.contains("menu") || scriptContent.contains("items") || scriptContent.contains("stations")) {
                    // Try to find JSON objects
                    val jsonPattern = Regex("""\{[^{}]*"name"[^{}]*\}""")
                    val jsonMatches = jsonPattern.findAll(scriptContent)
                    if (jsonMatches.any()) {
                        // Found potential JSON data
                        // For now, return null to continue with HTML parsing
                        // This can be enhanced to parse actual JSON
                    }
                }
            }
        } catch (e: Exception) {
            // Continue with HTML parsing
        }
        return null
    }

    /**
     * Tries to extract data from Nutrislice API endpoints or embedded data
     */
    private fun extractNutrisliceApiData(html: String): Result<ScrapedMenuData>? {
        try {
            // Look for API endpoints in the HTML
            val apiPattern = Regex("""https?://[^"'\s]+nutrislice[^"'\s]+api[^"'\s]+""")
            val apiMatches = apiPattern.findAll(html)
            
            // Look for embedded JSON data
            val jsonDataPattern = Regex("""window\.__INITIAL_STATE__\s*=\s*(\{.*?\});""", RegexOption.DOT_MATCHES_ALL)
            val jsonMatch = jsonDataPattern.find(html)
            
            // Look for data attributes with menu info
            val dataPattern = Regex("""data-menu-items\s*=\s*["']([^"']+)["']""")
            val dataMatch = dataPattern.find(html)
            
            // For now, return null - we'll rely on HTML parsing
            // In the future, we could make API calls to the endpoints found
        } catch (e: Exception) {
            // Continue with HTML parsing
        }
        return null
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

        // Try multiple selectors for menu items - expanded list
        val itemSelectors = listOf(
            ".menu-item",
            ".menu-item-name",
            "[data-menu-item]",
            ".food-item",
            ".item-name",
            "[data-item-name]",
            ".dish-name",
            ".food-name",
            "h3.menu-item-title",
            "h4.menu-item-title",
            ".ns-menu-item",
            ".nutrislice-menu-item",
            "[class*='menu-item']",
            "[class*='food-item']"
        )

        for (selector in itemSelectors) {
            val elements = doc.select(selector)
            if (elements.isNotEmpty()) {
                elements.forEach { element ->
                    val item = extractMenuItemFromElement(element, itemIndex++, categories)
                    if (item != null && item.name.length > 2) { // Filter out very short names
                        items.add(item)
                    }
                }
                if (items.isNotEmpty()) {
                    break // Use the first selector that finds elements
                }
            }
        }

        // If no items found, try to find items near category headers
        if (items.isEmpty() && categories.isNotEmpty()) {
            categories.forEach { category ->
                // Find elements that might be items under this category
                doc.select("h2, h3, h4, .menu-category-name, [class*='station'], [class*='category']").forEach { header ->
                    val headerText = header.text().trim()
                    if (headerText.isNotBlank() && (headerText.contains(category.name, ignoreCase = true) || category.name.contains(headerText, ignoreCase = true))) {
                        // Look for items in the same section
                        var current = header.nextElementSibling()
                        var itemCount = 0
                        var depth = 0
                        while (current != null && itemCount < 50 && depth < 10) {
                            // Try to find item name in various ways
                            val itemName = current.select("h3, h4, h5, .item-name, .menu-item-name, [class*='item'], [class*='dish']")
                                .firstOrNull()?.text()?.trim()
                                ?: current.text().trim().takeIf { it.length in 3..100 && !it.contains("\n") && it.split(" ").size <= 5 }
                            
                            if (itemName != null && itemName.isNotBlank() && itemName.length > 2) {
                                items.add(
                                    ScrapedMenuItem(
                                        id = "item_${itemIndex++}",
                                        name = itemName,
                                        category = category.name,
                                        station = category.station
                                    )
                                )
                                itemCount++
                            }
                            current = current.nextElementSibling()
                            depth++
                        }
                    }
                }
            }
        }

        return items.distinctBy { "${it.name}_${it.category}" }
    }

    /**
     * More aggressive extraction when standard methods fail
     */
    private fun extractMenuItemsAggressive(doc: Document): List<ScrapedMenuItem> {
        val items = mutableListOf<ScrapedMenuItem>()
        var itemIndex = 0

        // First, try to find elements with data attributes (Nutrislice might use these)
        doc.select("[data-name], [data-item-name], [data-dish-name]").forEach { element ->
            val name = element.attr("data-name").ifBlank {
                element.attr("data-item-name").ifBlank {
                    element.attr("data-dish-name")
                }
            }
            if (name.isNotBlank() && name.length > 2) {
                items.add(
                    ScrapedMenuItem(
                        id = "item_${itemIndex++}",
                        name = name.trim(),
                        category = "Uncategorized",
                        station = "Unknown"
                    )
                )
            }
        }

        // If still no items, look for text in cards/containers
        if (items.isEmpty()) {
            // Look for common container patterns
            doc.select(".card, .item-card, .menu-card, [class*='card'], [class*='item']").forEach { card ->
                val text = card.text().trim()
                // Extract first line or first meaningful text
                val firstLine = text.split("\n", ".", ",").firstOrNull()?.trim()
                if (firstLine != null && firstLine.length in 3..50 && firstLine.split(" ").size <= 5) {
                    if (items.none { it.name.equals(firstLine, ignoreCase = true) }) {
                        items.add(
                            ScrapedMenuItem(
                                id = "item_${itemIndex++}",
                                name = firstLine,
                                category = "Uncategorized",
                                station = "Unknown"
                            )
                        )
                    }
                }
            }
        }

        // Last resort: look for any meaningful text
        if (items.isEmpty()) {
            doc.select("div, li, span").forEach { element ->
                val text = element.text().trim()
                if (text.length in 3..50 && 
                    text.split(" ").size <= 5 &&
                    !text.contains(":") &&
                    !text.contains("@") &&
                    !text.matches(Regex(".*\\d{4}.*")) &&
                    element.select("a, button").isEmpty() &&
                    !text.lowercase().contains("click") &&
                    !text.lowercase().contains("menu") &&
                    !text.lowercase().contains("view")
                ) {
                    val looksLikeFood = text.split(" ").any { word ->
                        word.length > 2 && word[0].isUpperCase()
                    }
                    
                    if (looksLikeFood && items.none { it.name.equals(text, ignoreCase = true) }) {
                        items.add(
                            ScrapedMenuItem(
                                id = "item_${itemIndex++}",
                                name = text,
                                category = "Uncategorized",
                                station = "Unknown"
                            )
                        )
                    }
                }
            }
        }

        return items.distinctBy { it.name.lowercase() }.take(100) // Limit to 100 items
    }

    /**
     * More aggressive category extraction
     */
    private fun extractCategoriesAggressive(doc: Document): List<FoodCategory> {
        val categories = mutableListOf<FoodCategory>()
        var categoryIndex = 0

        // Look for headers that might be categories
        doc.select("h1, h2, h3, h4").forEach { header ->
            val text = header.text().trim()
            if (text.length in 3..50 && 
                text.split(" ").size <= 5 &&
                !text.contains(":") &&
                !text.matches(Regex(".*\\d{4}.*"))
            ) {
                categories.add(
                    FoodCategory(
                        id = "category_${categoryIndex++}",
                        name = text,
                        station = text
                    )
                )
            }
        }

        return categories.distinctBy { it.name.lowercase() }
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

