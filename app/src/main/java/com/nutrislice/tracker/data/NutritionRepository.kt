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
import com.nutrislice.tracker.model.MealEntry
import com.nutrislice.tracker.model.Location
import com.nutrislice.tracker.model.NutritionFacts
import com.nutrislice.tracker.model.NutritionGoals
import com.nutrislice.tracker.model.UserProfile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
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
    fun getLocations(): Flow<List<Location>>
}

class DataStoreNutritionRepository(
    private val dataStore: DataStore<Preferences>,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : NutritionRepository {

    private object Keys {
        val Goals = stringPreferencesKey("nutrition_goals")
        val Meals = stringPreferencesKey("nutrition_meals")
        val Streak = intPreferencesKey("streak")
        val LastDay = longPreferencesKey("last_day")
        val UserProfile = stringPreferencesKey("user_profile")
    }

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
        return flowOf(
            listOf(
                MealEntry(
                    id = 1, 
                    name = "Pancakes", 
                    calories = 550.0, 
                    protein = 30.0, 
                    carbs = 45.0, 
                    fat = 25.0, 
                    fiber = 3.0, 
                    category = "East Side Dining", 
                    mealTime = "Breakfast", 
                    station = "Dine-In - Self Serve Pancakes and Waffles",
                    imageUrl = "https://www.inspiredtaste.net/wp-content/uploads/2016/07/Pancake-Recipe-1-1200.jpg",
                    nutritionFacts = NutritionFacts(
                        servingSize = "1 pancake",
                        calories = 140,
                        totalFat = "1g",
                        saturatedFat = "0g",
                        transFat = "0g",
                        cholesterol = "0mg",
                        sodium = "480mg",
                        totalCarbohydrate = "30g",
                        dietaryFiber = "1g",
                        totalSugars = "2g"
                    ),
                    dietaryInfo = listOf(DietaryInfo.CONTAINS_DAIRY, DietaryInfo.CONTAINS_EGG, DietaryInfo.VEGETARIAN),
                    ingredients = listOf("Enriched Bleached Flour", "Sugar", "Leavening", "Salt", "Soybean Oil", "Buttermilk", "Eggs", "Water")
                ),
                MealEntry(
                    id = 2, 
                    name = "Waffles", 
                    calories = 450.0, 
                    protein = 35.0, 
                    carbs = 40.0, 
                    fat = 20.0, 
                    fiber = 2.0, 
                    category = "East Side Dining", 
                    mealTime = "Breakfast", 
                    station = "Dine-In - Self Serve Pancakes and Waffles",
                    imageUrl = "https://www.allrecipes.com/thmb/mvO20-S-u8dC-5-i7h-4-5Q8Yk=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/20513-classic-waffles-mfs-036-4x3-8103a3068f934b49b9968a1854a8a071.jpg",
                    nutritionFacts = NutritionFacts(
                        servingSize = "1 waffle",
                        calories = 140,
                        totalFat = "1g",
                        saturatedFat = "0g",
                        transFat = "0g",
                        cholesterol = "0mg",
                        sodium = "480mg",
                        totalCarbohydrate = "30g",
                        dietaryFiber = "1g",
                        totalSugars = "2g"
                    ),
                    dietaryInfo = listOf(DietaryInfo.CONTAINS_DAIRY, DietaryInfo.CONTAINS_EGG, DietaryInfo.VEGETARIAN),
                    ingredients = listOf("Enriched Bleached Flour", "Sugar", "Leavening", "Salt", "Soybean Oil", "Buttermilk", "Eggs", "Water")
                ),
                MealEntry(
                    id = 3, 
                    name = "Yogurt", 
                    calories = 350.0, 
                    protein = 5.0, 
                    carbs = 50.0, 
                    fat = 15.0, 
                    fiber = 4.0, 
                    category = "East Side Dining", 
                    mealTime = "Breakfast", 
                    station = "Yogurt & Fruit Bar",
                    imageUrl = "https://www.alphafoodie.com/wp-content/uploads/2021/04/Yogurt-fruit-parfait-1-of-1-500x500.jpeg",
                    nutritionFacts = NutritionFacts(
                        servingSize = "1 cup",
                        calories = 350,
                        totalFat = "15g",
                        saturatedFat = "8g",
                        transFat = "0g",
                        cholesterol = "50mg",
                        sodium = "100mg",
                        totalCarbohydrate = "50g",
                        dietaryFiber = "4g",
                        totalSugars = "45g"
                    ),
                    dietaryInfo = listOf(DietaryInfo.CONTAINS_DAIRY, DietaryInfo.VEGETARIAN),
                    ingredients = listOf("Yogurt (Milk, Cream, Pectin, Carrageenan)", "Granola (Oats, Brown Sugar, Coconut Oil, Salt)", "Strawberries", "Blueberries")
                ),
                MealEntry(
                    id = 4, 
                    name = "Pizza", 
                    calories = 600.0, 
                    protein = 25.0, 
                    carbs = 60.0, 
                    fat = 30.0, 
                    fiber = 5.0, 
                    category = "East Side Dining", 
                    mealTime = "Lunch", 
                    station = "Pizza Station",
                    imageUrl = "https://www.stonybrook.edu/commcms/fsa/images/pizza-hero.jpg",
                    nutritionFacts = NutritionFacts(
                        servingSize = "1 slice",
                        calories = 600,
                        totalFat = "30g",
                        saturatedFat = "15g",
                        transFat = "0g",
                        cholesterol = "70mg",
                        sodium = "1200mg",
                        totalCarbohydrate = "60g",
                        dietaryFiber = "5g",
                        totalSugars = "5g"
                    ),
                    dietaryInfo = listOf(DietaryInfo.CONTAINS_DAIRY, DietaryInfo.CONTAINS_EGG, DietaryInfo.VEGETARIAN),
                    ingredients = listOf("Dough (Enriched Flour, Water, Yeast, Sugar, Salt, Olive Oil)", "Tomato Sauce (Tomatoes, Salt, Spices)", "Cheese (Milk, Salt, Enzymes)", "Pepperoni (Pork, Salt, Spices)")
                ),
                 MealEntry(
                    id = 5, 
                    name = "Omelet", 
                    calories = 300.0, 
                    protein = 20.0, 
                    carbs = 2.0, 
                    fat = 22.0, 
                    fiber = 1.0, 
                    category = "East Side Dining", 
                    mealTime = "Breakfast", 
                    station = "Omelet Bar",
                    imageUrl = "https://www.incredibleegg.org/wp-content/uploads/2019/02/basic-french-omelet-930x550.jpg",
                    nutritionFacts = NutritionFacts(
                        servingSize = "1 omelet",
                        calories = 300,
                        totalFat = "22g",
                        saturatedFat = "8g",
                        transFat = "0g",
                        cholesterol = "400mg",
                        sodium = "300mg",
                        totalCarbohydrate = "2g",
                        dietaryFiber = "1g",
                        totalSugars = "1g"
                    ),
                    dietaryInfo = listOf(DietaryInfo.CONTAINS_EGG, DietaryInfo.VEGETARIAN),
                    ingredients = listOf("Eggs", "Cheese (Milk, Salt, Enzymes)", "Spinach", "Mushrooms", "Onions")
                ),
                 MealEntry(
                    id = 6, 
                    name = "Deli Sandwich", 
                    calories = 500.0, 
                    protein = 25.0, 
                    carbs = 50.0, 
                    fat = 20.0, 
                    fiber = 5.0, 
                    category = "East Side Dining", 
                    mealTime = "Lunch", 
                    station = "Deli",
                    imageUrl = "https://www.stonybrook.edu/commcms/fsa/images/deli-hero.jpg",
                    nutritionFacts = NutritionFacts(
                        servingSize = "1 sandwich",
                        calories = 500,
                        totalFat = "20g",
                        saturatedFat = "8g",
                        transFat = "0g",
                        cholesterol = "60mg",
                        sodium = "1000mg",
                        totalCarbohydrate = "50g",
                        dietaryFiber = "5g",
                        totalSugars = "5g"
                    ),
                    dietaryInfo = listOf(DietaryInfo.CONTAINS_DAIRY, DietaryInfo.CONTAINS_EGG, DietaryInfo.VEGETARIAN),
                    ingredients = listOf("Bread (Enriched Flour, Water, Yeast, Sugar, Salt, Olive Oil)", "Turkey", "Cheese (Milk, Salt, Enzymes)", "Lettuce", "Tomato", "Mayonnaise")
                ),
                 MealEntry(
                    id = 7, 
                    name = "Cheesecake", 
                    calories = 400.0, 
                    protein = 5.0, 
                    carbs = 35.0, 
                    fat = 25.0, 
                    fiber = 1.0, 
                    category = "East Side Dining", 
                    mealTime = "Dinner", 
                    station = "Dessert",
                    imageUrl = "https://www.stonybrook.edu/commcms/fsa/images/dessert-hero.jpg",
                    nutritionFacts = NutritionFacts(
                        servingSize = "1 slice",
                        calories = 400,
                        totalFat = "25g",
                        saturatedFat = "15g",
                        transFat = "0g",
                        cholesterol = "80mg",
                        sodium = "300mg",
                        totalCarbohydrate = "35g",
                        dietaryFiber = "1g",
                        totalSugars = "25g"
                    ),
                    dietaryInfo = listOf(DietaryInfo.CONTAINS_DAIRY, DietaryInfo.CONTAINS_EGG, DietaryInfo.VEGETARIAN),
                    ingredients = listOf("Cream Cheese", "Sugar", "Eggs", "Graham Cracker Crust")
                )
            )
        )
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

    private fun mealsFromPrefs(prefs: Preferences): List<MealEntry> {
        val stored = prefs[Keys.Meals] ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(MealEntry.serializer()), stored)
        }.getOrDefault(emptyList())
    }
}
