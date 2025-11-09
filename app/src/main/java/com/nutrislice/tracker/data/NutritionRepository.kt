package com.nutrislice.tracker.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nutrislice.tracker.model.DefaultGoals
import com.nutrislice.tracker.model.DietaryInfo
import com.nutrislice.tracker.model.FoodCategory
import com.nutrislice.tracker.model.MealEntry
import com.nutrislice.tracker.model.Location
import com.nutrislice.tracker.model.NutritionFacts
import com.nutrislice.tracker.model.NutritionGoals
import com.nutrislice.tracker.model.ScrapedMenuItem
import com.nutrislice.tracker.model.UserProfile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

interface NutritionRepository {
    val goals: Flow<NutritionGoals>
    val meals: Flow<List<MealEntry>>
    val streak: Flow<Int>
    val lastDay: Flow<Long>
    val userProfile: Flow<UserProfile>

    suspend fun saveGoals(goals: NutritionGoals)
    suspend fun addMeal(mealEntry: MealEntry)
    suspend fun addMeals(mealEntries: List<MealEntry>)
    suspend fun deleteMeal(mealId: Long)
    suspend fun updateStreak(streak: Int, lastDay: Long)
    suspend fun saveUserProfile(userProfile: UserProfile)
    fun getMenu(): Flow<List<MealEntry>>
    suspend fun fetchMenuFromWeb(url: String = "https://stonybrook.nutrislice.com/menu/east-side-dining"): Result<List<MealEntry>>
    suspend fun fetchCategories(url: String = "https://stonybrook.nutrislice.com/menu/east-side-dining"): Result<List<FoodCategory>>
    fun getLocations(): Flow<List<Location>>
    
    // NeuralSeek meal planning
    suspend fun getMealPlanSuggestions(days: Int = 7, preferences: String = ""): Result<String>
    suspend fun getMealTimeSuggestions(mealTime: String, preferences: String = ""): Result<String>
    
    // Screenshot data loading
    suspend fun loadScreenshotMenuItems(): Result<List<MealEntry>>
}

class DataStoreNutritionRepository(
    private val dataStore: DataStore<Preferences>,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val menuScraper: MenuScraper = MenuScraper(),
    private val neuralSeekService: NeuralSeekService = NeuralSeekService() // TODO: Pass API key from config
) : NutritionRepository {

    private object Keys {
        val Goals = stringPreferencesKey("nutrition_goals")
        val Meals = stringPreferencesKey("nutrition_meals")
        val Streak = intPreferencesKey("streak")
        val LastDay = longPreferencesKey("last_day")
        val UserProfile = stringPreferencesKey("user_profile")
        val Menu = stringPreferencesKey("scraped_menu")
    }

    // Store fetched menu items in memory
    private val _menuItems = MutableStateFlow<List<MealEntry>>(emptyList())
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    override val goals: Flow<NutritionGoals> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            prefs[Keys.UserProfile]?.let { stored ->
                runCatching { json.decodeFromString(NutritionGoals.serializer(), stored) }
                    .getOrNull() ?: DefaultGoals
            } ?: DefaultGoals
        }

    override val meals: Flow<List<MealEntry>> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            prefs[Keys.Meals]?.let { stored ->
                runCatching {
                    json.decodeFromString(ListSerializer(MealEntry.serializer()), stored)
                }.getOrDefault(emptyList())
            } ?: emptyList()
        }

    override val streak: Flow<Int> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            prefs[Keys.Streak] ?: 0
        }

    override val lastDay: Flow<Long> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            prefs[Keys.LastDay] ?: 0
        }

    override val userProfile: Flow<UserProfile> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            prefs[Keys.UserProfile]?.let { stored ->
                runCatching { json.decodeFromString(UserProfile.serializer(), stored) }
                    .getOrNull() ?: UserProfile()
            } ?: UserProfile()
        }

    init {
        // Load saved menu from DataStore on initialization
        repositoryScope.launch {
            dataStore.data.first().let { prefs ->
                prefs[Keys.Menu]?.let { stored ->
                    runCatching {
                        val savedMenu = json.decodeFromString(
                            ListSerializer(MealEntry.serializer()),
                            stored
                        )
                        _menuItems.value = savedMenu
                    }
                }
            }
        }
    }

    override suspend fun saveGoals(goals: NutritionGoals) {
        withContext(ioDispatcher) {
            dataStore.edit { prefs ->
                prefs[Keys.Goals] = json.encodeToString(NutritionGoals.serializer(), goals)
            }
        }
    }

    override suspend fun addMeal(mealEntry: MealEntry) {
        withContext(ioDispatcher) {
            dataStore.edit { prefs ->
                val updated = mealsFromPrefs(prefs) + mealEntry
                prefs[Keys.Meals] = json.encodeToString(
                    ListSerializer(MealEntry.serializer()),
                    updated
                )
            }
        }
    }

    override suspend fun addMeals(mealEntries: List<MealEntry>) {
        withContext(ioDispatcher) {
            dataStore.edit { prefs ->
                val updated = mealsFromPrefs(prefs) + mealEntries
                prefs[Keys.Meals] = json.encodeToString(
                    ListSerializer(MealEntry.serializer()),
                    updated
                )
            }
        }
    }

    override suspend fun deleteMeal(mealId: Long) {
        withContext(ioDispatcher) {
            dataStore.edit { prefs ->
                val updated = mealsFromPrefs(prefs).filterNot { it.id == mealId }
                prefs[Keys.Meals] = json.encodeToString(
                    ListSerializer(MealEntry.serializer()),
                    updated
                )
            }
        }
    }

    override suspend fun updateStreak(streak: Int, lastDay: Long) {
        withContext(ioDispatcher) {
            dataStore.edit { prefs ->
                prefs[Keys.Streak] = streak
                prefs[Keys.LastDay] = lastDay
            }
        }
    }

    override suspend fun saveUserProfile(userProfile: UserProfile) {
        withContext(ioDispatcher) {
            dataStore.edit { prefs ->
                prefs[Keys.UserProfile] = json.encodeToString(UserProfile.serializer(), userProfile)
            }
        }
    }

    override fun getMenu(): Flow<List<MealEntry>> {
        // Return the stored menu items (populated by fetchMenuFromWeb)
        return _menuItems
    }

    override suspend fun fetchMenuFromWeb(url: String): Result<List<MealEntry>> {
        return withContext(ioDispatcher) {
            menuScraper.scrapeMenu(url).map { scrapedData ->
                val menuItems = scrapedData.items.map { scrapedItem ->
                    convertToMealEntry(scrapedItem)
                }
                // Store the fetched menu items
                _menuItems.value = menuItems
                // Also save to DataStore for persistence
                dataStore.edit { prefs ->
                    prefs[Keys.Menu] = json.encodeToString(
                        ListSerializer(MealEntry.serializer()),
                        menuItems
                    )
                }
                menuItems
            }
        }
    }

    override suspend fun fetchCategories(url: String): Result<List<FoodCategory>> {
        return withContext(ioDispatcher) {
            menuScraper.scrapeMenu(url).map { scrapedData ->
                scrapedData.categories
            }
        }
    }

    /**
     * Converts a ScrapedMenuItem to a MealEntry
     */
    private fun convertToMealEntry(scrapedItem: ScrapedMenuItem): MealEntry {
        val nutritionFacts = scrapedItem.nutritionFacts
        val calories = nutritionFacts?.calories?.toDouble() ?: 0.0
        
        // Extract numeric values from nutrition facts strings
        // Note: Protein is not directly available in NutritionFacts, will need to be extracted from other sources
        val protein = 0.0 // Protein info may not be available in scraped data
        val carbs = extractNumericValue(nutritionFacts?.totalCarbohydrate) ?: 0.0
        val fat = extractNumericValue(nutritionFacts?.totalFat) ?: 0.0
        val fiber = extractNumericValue(nutritionFacts?.dietaryFiber) ?: 0.0

        return MealEntry(
            id = scrapedItem.id.hashCode().toLong(),
            name = scrapedItem.name,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            fiber = fiber,
            category = scrapedItem.category,
            mealTime = scrapedItem.mealTime ?: determineMealTime(scrapedItem.category),
            station = scrapedItem.station ?: scrapedItem.category,
            imageUrl = scrapedItem.imageUrl,
            nutritionFacts = nutritionFacts,
            dietaryInfo = scrapedItem.dietaryInfo,
            ingredients = scrapedItem.ingredients
        )
    }

    /**
     * Extracts numeric value from a string like "25g" or "100mg"
     */
    private fun extractNumericValue(value: String?): Double? {
        if (value == null) return null
        val numericPart = value.replace(Regex("[^0-9.]"), "")
        return numericPart.toDoubleOrNull()
    }

    /**
     * Determines meal time based on category name
     */
    private fun determineMealTime(category: String): String {
        val lowerCategory = category.lowercase()
        return when {
            lowerCategory.contains("breakfast") -> "Breakfast"
            lowerCategory.contains("lunch") -> "Lunch"
            lowerCategory.contains("dinner") -> "Dinner"
            lowerCategory.contains("dessert") -> "Dessert"
            lowerCategory.contains("snack") -> "Snack"
            else -> "All Day"
        }
    }

    override fun getLocations(): Flow<List<Location>> {
        return flowOf(
            listOf(
                Location(name = "East Side Dining", imageUrl = "https://www.stonybrook.edu/commcms/fsa/images/East-Side-Dining-4.jpg"),
                Location(name = "East Side Retail", imageUrl = "https://www.stonybrook.edu/commcms/fsa/images/East-Side-Dining-4.jpg"),
                Location(name = "Peet's Coffee", imageUrl = "https://www.stonybrook.edu/commcms/fsa/images/Peets-Hero-Our-Coffee.jpg"),
                Location(name = "Roth Cafe", imageUrl = "https://www.stonybrook.edu/commcms/fsa/images/Roth-Cafe-2.jpg"),
                Location(name = "Jasmine", imageUrl = "https://www.stonybrook.edu/commcms/fsa/images/Jasmine-hero.jpg"),
                Location(name = "Food Trucks", imageUrl = "https://www.stonybrook.edu/commcms/fsa/images/the-met-food-truck.jpg"),
                Location(name = "Dental Cafe", imageUrl = "https://www.stonybrook.edu/commcms/fsa/images/dental-cafe-hero-new.jpg"),
                Location(name = "Craft", imageUrl = "https://www.stonybrook.edu/commcms/fsa/images/WSD-Retail-4.jpg")
            )
        )
    }

    override suspend fun getMealPlanSuggestions(days: Int, preferences: String): Result<String> {
        return withContext(ioDispatcher) {
            val goals = goals.first()
            val profile = userProfile.first()
            neuralSeekService.getMealSuggestions(goals, profile, days, preferences)
        }
    }

    override suspend fun getMealTimeSuggestions(mealTime: String, preferences: String): Result<String> {
        return withContext(ioDispatcher) {
            val goals = goals.first()
            val profile = userProfile.first()
            neuralSeekService.getMealTimeSuggestions(mealTime, goals, profile, preferences)
        }
    }

    /**
     * Loads menu items extracted from screenshots
     * Call this to populate the menu with screenshot data
     */
    suspend fun loadScreenshotMenuItems(): Result<List<MealEntry>> {
        return withContext(ioDispatcher) {
            try {
                val items = ScreenshotDataProcessor.extractedMenuItems
                if (items.isNotEmpty()) {
                    // Add to menu
                    _menuItems.value = items
                    // Save to DataStore
                    dataStore.edit { prefs ->
                        prefs[Keys.Menu] = json.encodeToString(
                            ListSerializer(MealEntry.serializer()),
                            items
                        )
                    }
                    Result.success(items)
                } else {
                    Result.failure(Exception("No screenshot data available"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun mealsFromPrefs(prefs: Preferences): List<MealEntry> {
        val stored = prefs[Keys.Meals] ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(MealEntry.serializer()), stored)
        }.getOrDefault(emptyList())
    }
}
