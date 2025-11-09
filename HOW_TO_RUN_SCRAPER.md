# How to Run the Menu Scraper

This guide explains how to test and use the web scraper for fetching menu data from the Nutrislice website.

## Prerequisites

1. **Build the project** to download dependencies:
   ```bash
   ./gradlew build
   ```

2. **Ensure you have internet access** - The scraper needs to connect to `https://stonybrook.nutrislice.com`

## Method 1: Run in Android App (Recommended)

### Step 1: Add a Button to Your UI

Add a button in your UI (e.g., in `NutritionScreen.kt`) to trigger the scraper:

```kotlin
// In your Composable function
Button(
    onClick = { viewModel.fetchMenuFromWeb() },
    enabled = !uiState.isFetchingMenu
) {
    if (uiState.isFetchingMenu) {
        CircularProgressIndicator()
    } else {
        Text("Fetch Menu from Web")
    }
}

// Display categories
if (uiState.categories.isNotEmpty()) {
    Text("Categories: ${uiState.categories.size}")
    uiState.categories.forEach { category ->
        Text("  - ${category.name}")
    }
}

// Display user messages
uiState.userMessage?.let { message ->
    Text(message)
    LaunchedEffect(message) {
        delay(3000)
        viewModel.consumeMessage()
    }
}
```

### Step 2: Run the App

1. Connect an Android device or start an emulator
2. Run the app:
   ```bash
   ./gradlew installDebug
   ```
   Or use Android Studio: Click the "Run" button

3. Click the "Fetch Menu from Web" button in the app
4. Check the UI for:
   - Success message showing number of items fetched
   - Categories list (if you call `fetchCategories()`)
   - Any error messages

## Method 2: Test Directly in Code

### Option A: Add to ViewModel init (for testing)

Temporarily add this to your `NutritionViewModel.kt` in the `init` block:

```kotlin
init {
    viewModelScope.launch {
        checkStreakOnAppStart()
        // Test scraper on app start
        fetchMenuFromWeb()
    }
}
```

### Option B: Create a Test Function

Add this to your `MainActivity.kt` or create a test screen:

```kotlin
// In MainActivity onCreate or a test function
lifecycleScope.launch {
    val app = application as NutritionTrackerApp
    val result = app.nutritionRepository.fetchMenuFromWeb()
    
    result.onSuccess { menuItems ->
        Log.d("Scraper", "Successfully fetched ${menuItems.size} items")
        menuItems.forEach { item ->
            Log.d("Scraper", "Item: ${item.name} - ${item.category}")
        }
    }.onFailure { error ->
        Log.e("Scraper", "Error: ${error.message}", error)
    }
}
```

## Method 3: Unit Test (Advanced)

Create a test file `app/src/test/java/com/nutrislice/tracker/MenuScraperTest.kt`:

```kotlin
package com.nutrislice.tracker

import com.nutrislice.tracker.data.MenuScraper
import kotlinx.coroutines.runBlocking
import org.junit.Test

class MenuScraperTest {
    @Test
    fun testScrapeMenu() = runBlocking {
        val scraper = MenuScraper()
        val result = scraper.scrapeMenu("https://stonybrook.nutrislice.com/menu/east-side-dining")
        
        result.onSuccess { menuData ->
            println("Categories: ${menuData.categories.size}")
            println("Items: ${menuData.items.size}")
            assert(menuData.categories.isNotEmpty() || menuData.items.isNotEmpty())
        }.onFailure { error ->
            println("Error: ${error.message}")
            throw error
        }
    }
}
```

Run the test:
```bash
./gradlew test
```

## Method 4: Using Android Studio's Logcat

1. Run the app in Android Studio
2. Open Logcat (View → Tool Windows → Logcat)
3. Filter by tag "Scraper" or your package name
4. Call `fetchMenuFromWeb()` from your UI
5. Watch the logs for output

## Expected Output

When successful, you should see:
- **Categories**: List of food categories/stations (e.g., "Breakfast", "Lunch", "Pizza Station")
- **Menu Items**: List of food items with:
  - Name
  - Category
  - Station
  - Nutrition facts (if available)
  - Dietary information
  - Ingredients (if available)

## Troubleshooting

### Issue: "Network error" or "Failed to fetch"
- **Solution**: Check internet connection
- Ensure the device/emulator has internet access
- Check if the URL is accessible in a browser

### Issue: "Empty results" or "0 items found"
- **Solution**: The website structure may have changed
- Check the HTML structure of the Nutrislice website
- Update selectors in `MenuScraper.kt` if needed

### Issue: "Timeout"
- **Solution**: The website may be slow
- Increase timeout in `MenuScraper.kt` (currently 30 seconds)

### Issue: Build errors
- **Solution**: Sync Gradle files
- In Android Studio: File → Sync Project with Gradle Files
- Or run: `./gradlew --refresh-dependencies`

## Quick Test Command

To quickly test if the scraper works, add this to your `MainActivity.onCreate()`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Quick test
    lifecycleScope.launch {
        val app = application as NutritionTrackerApp
        val result = app.nutritionRepository.fetchCategories()
        result.onSuccess { 
            Log.d("TEST", "Found ${it.size} categories: ${it.map { it.name }}")
        }
    }
    
    setContent {
        // ... rest of your code
    }
}
```

## Next Steps

After testing:
1. Integrate the fetched menu items into your UI
2. Add caching to avoid repeated network calls
3. Add error handling and retry logic
4. Update the scraper selectors if the website structure changes

