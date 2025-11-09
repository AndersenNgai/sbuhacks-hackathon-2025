package com.nutrislice.tracker.data

import com.nutrislice.tracker.model.FoodCategory
import com.nutrislice.tracker.model.MealEntry

/**
 * Example usage of the MenuScraper and NutritionRepository
 * 
 * This file demonstrates how to use the web scraping functionality
 * to fetch food categories and menu items from the Nutrislice website.
 */

// Example: Fetching categories
/*
suspend fun fetchCategoriesExample(repository: NutritionRepository) {
    val result = repository.fetchCategories("https://stonybrook.nutrislice.com/menu/east-side-dining")
    
    result.onSuccess { categories: List<FoodCategory> ->
        // Handle successful fetch
        categories.forEach { category ->
            println("Category: ${category.name}")
            println("Station: ${category.station}")
        }
    }.onFailure { error ->
        // Handle error
        println("Error fetching categories: ${error.message}")
    }
}
*/

// Example: Fetching menu items
/*
suspend fun fetchMenuItemsExample(repository: NutritionRepository) {
    val result = repository.fetchMenuFromWeb("https://stonybrook.nutrislice.com/menu/east-side-dining")
    
    result.onSuccess { items: List<MealEntry> ->
        // Handle successful fetch
        items.forEach { item ->
            println("Item: ${item.name}")
            println("Category: ${item.category}")
            println("Station: ${item.station}")
            println("Calories: ${item.calories}")
            println("---")
        }
    }.onFailure { error ->
        // Handle error
        println("Error fetching menu: ${error.message}")
    }
}
*/

// Example: Using MenuScraper directly
/*
suspend fun directScraperExample() {
    val scraper = MenuScraper()
    val result = scraper.scrapeMenu("https://stonybrook.nutrislice.com/menu/east-side-dining")
    
    result.onSuccess { menuData ->
        println("Found ${menuData.categories.size} categories")
        println("Found ${menuData.items.size} menu items")
        
        menuData.categories.forEach { category ->
            println("Category: ${category.name}")
        }
        
        menuData.items.forEach { item ->
            println("Item: ${item.name} (${item.category})")
        }
    }.onFailure { error ->
        println("Scraping failed: ${error.message}")
    }
}
*/

