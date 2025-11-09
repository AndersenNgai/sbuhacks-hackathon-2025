package com.nutrislice.tracker.data

import android.util.Log
import com.nutrislice.tracker.model.MealEntry
import com.nutrislice.tracker.model.NutritionGoals
import com.nutrislice.tracker.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Service to interact with NeuralSeek API for meal planning suggestions
 * 
 * To use this service:
 * 1. Sign up at https://neuralseek.com
 * 2. Generate an API key from the Integrate section
 * 3. Set the API key in your app (e.g., via BuildConfig or environment variable)
 */
class NeuralSeekService(
    private val apiKey: String = "d854492c-1b3306ac-ad0f1ac6-3363b14d", // Set your API key here or via dependency injection
    private val baseUrl: String = "https://api.neuralseek.com/v1/test"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Serializable
    data class NeuralSeekRequest(
        val question: String
    )

    @Serializable
    data class NeuralSeekResponse(
        val answer: String? = null,
        val answerText: String? = null,
        val response: String? = null
    )

    /**
     * Generates meal suggestions based on user preferences and goals
     */
    suspend fun getMealSuggestions(
        goals: NutritionGoals,
        userProfile: UserProfile,
        days: Int = 7,
        preferences: String = ""
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (apiKey.isBlank()) {
                    return@withContext Result.failure(
                        Exception("NeuralSeek API key is not configured. Please set your API key.")
                    )
                }

                val question = buildMealPlanningQuestion(goals, userProfile, days, preferences)
                val requestBody = json.encodeToString(
                    NeuralSeekRequest.serializer(),
                    NeuralSeekRequest(question = question)
                )

                val request = Request.Builder()
                    .url("$baseUrl/seek")
                    .header("accept", "application/json")
                    .header("APIkey", apiKey) // NeuralSeek uses "APIkey" as the header name
                    .header("Content-Type", "application/json")
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()

                Log.d("NeuralSeek", "Request URL: $baseUrl/seek")
                Log.d("NeuralSeek", "API Key (first 10 chars): ${apiKey.take(10)}...")
                Log.d("NeuralSeek", "Sending request: $question")
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("NeuralSeek", "Response received: ${responseBody?.take(200)}")
                    
                    if (responseBody != null) {
                        try {
                            val parsedResponse = json.decodeFromString<NeuralSeekResponse>(responseBody)
                            val answer = parsedResponse.answer 
                                ?: parsedResponse.answerText 
                                ?: parsedResponse.response
                                ?: responseBody
                            
                            Log.d("NeuralSeek", "Parsed answer: ${answer.take(200)}")
                            return@withContext Result.success(answer)
                        } catch (e: Exception) {
                            // If JSON parsing fails, return raw response
                            Log.d("NeuralSeek", "JSON parsing failed, using raw response")
                            return@withContext Result.success(responseBody)
                        }
                    } else {
                        return@withContext Result.failure(Exception("Empty response from NeuralSeek"))
                    }
                } else {
                    val errorBody = response.body?.string()
                    Log.e("NeuralSeek", "API error: ${response.code} - $errorBody")
                    
                    // Provide helpful error messages
                    val errorMessage = when (response.code) {
                        401 -> {
                            "Authentication failed. Please check your NeuralSeek API key. " +
                            "Make sure the API key is correct and hasn't expired. " +
                            "You can get a new API key from the NeuralSeek dashboard under 'Integrate' section."
                        }
                        403 -> "API key doesn't have permission to access this endpoint."
                        404 -> "API endpoint not found. Please check the API URL."
                        429 -> "Too many requests. Please try again later."
                        500 -> "NeuralSeek server error. Please try again later."
                        else -> "NeuralSeek API error: ${response.code} - $errorBody"
                    }
                    
                    return@withContext Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e("NeuralSeek", "Error calling NeuralSeek API", e)
                return@withContext Result.failure(
                    Exception("Failed to get meal suggestions: ${e.message}", e)
                )
            }
        }
    }

    /**
     * Gets meal suggestions for a specific meal time (breakfast, lunch, dinner)
     */
    suspend fun getMealTimeSuggestions(
        mealTime: String,
        goals: NutritionGoals,
        userProfile: UserProfile,
        preferences: String = ""
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (apiKey.isBlank()) {
                    return@withContext Result.failure(
                        Exception("NeuralSeek API key is not configured")
                    )
                }

                val question = buildMealTimeQuestion(mealTime, goals, userProfile, preferences)
                val requestBody = json.encodeToString(
                    NeuralSeekRequest.serializer(),
                    NeuralSeekRequest(question = question)
                )

                val request = Request.Builder()
                    .url("$baseUrl/seek")
                    .header("accept", "application/json")
                    .header("APIkey", apiKey) // NeuralSeek uses "APIkey" as the header name
                    .header("Content-Type", "application/json")
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()

                Log.d("NeuralSeek", "Request URL: $baseUrl/seek")
                Log.d("NeuralSeek", "API Key (first 10 chars): ${apiKey.take(10)}...")
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        try {
                            val parsedResponse = json.decodeFromString<NeuralSeekResponse>(responseBody)
                            val answer = parsedResponse.answer 
                                ?: parsedResponse.answerText 
                                ?: parsedResponse.response
                                ?: responseBody
                            return@withContext Result.success(answer)
                        } catch (e: Exception) {
                            return@withContext Result.success(responseBody)
                        }
                    } else {
                        return@withContext Result.failure(Exception("Empty response from NeuralSeek"))
                    }
                } else {
                    val errorBody = response.body?.string()
                    Log.e("NeuralSeek", "API error: ${response.code} - $errorBody")
                    
                    // Provide helpful error messages
                    val errorMessage = when (response.code) {
                        401 -> {
                            "Authentication failed. Please check your NeuralSeek API key. " +
                            "Make sure the API key is correct and hasn't expired. " +
                            "You can get a new API key from the NeuralSeek dashboard under 'Integrate' section."
                        }
                        403 -> "API key doesn't have permission to access this endpoint."
                        404 -> "API endpoint not found. Please check the API URL."
                        429 -> "Too many requests. Please try again later."
                        500 -> "NeuralSeek server error. Please try again later."
                        else -> "NeuralSeek API error: ${response.code} - $errorBody"
                    }
                    
                    return@withContext Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                return@withContext Result.failure(
                    Exception("Failed to get meal suggestions: ${e.message}", e)
                )
            }
        }
    }

    /**
     * Builds a comprehensive meal planning question for NeuralSeek
     */
    private fun buildMealPlanningQuestion(
        goals: NutritionGoals,
        userProfile: UserProfile,
        days: Int,
        preferences: String
    ): String {
        val dietaryRestrictions = if (userProfile.restrictions.isNotEmpty()) {
            userProfile.restrictions.joinToString(", ") { it.name.lowercase().replace("_", "-") }
        } else {
            "none"
        }

        val restrictionsText = if (dietaryRestrictions != "none") {
            "Dietary restrictions: $dietaryRestrictions. "
        } else {
            ""
        }

        val preferencesText = if (preferences.isNotBlank()) {
            "Additional preferences: $preferences. "
        } else {
            ""
        }

        return """
            Create a $days-day meal prep plan with the following requirements:
            
            Daily nutrition goals:
            - Calories: ${goals.calories} kcal
            - Protein: ${goals.protein}g
            - Carbohydrates: ${goals.carbs}g
            - Fat: ${goals.fat}g
            - Fiber: ${goals.fiber}g
            
            $restrictionsText$preferencesText
            
            Please provide:
            1. A detailed meal plan for each day with breakfast, lunch, and dinner
            2. Specific meal suggestions with approximate nutritional values
            3. Meal prep tips and preparation instructions
            4. Shopping list organized by food categories
            
            Format the response in a clear, organized way that's easy to follow for meal prep.
        """.trimIndent()
    }

    /**
     * Builds a question for a specific meal time
     */
    private fun buildMealTimeQuestion(
        mealTime: String,
        goals: NutritionGoals,
        userProfile: UserProfile,
        preferences: String
    ): String {
        val dietaryRestrictions = if (userProfile.restrictions.isNotEmpty()) {
            userProfile.restrictions.joinToString(", ") { it.name.lowercase().replace("_", "-") }
        } else {
            "none"
        }

        val calorieTarget = when (mealTime.lowercase()) {
            "breakfast" -> (goals.calories * 0.25).toInt() // ~25% of daily calories
            "lunch" -> (goals.calories * 0.35).toInt() // ~35% of daily calories
            "dinner" -> (goals.calories * 0.30).toInt() // ~30% of daily calories
            else -> (goals.calories * 0.33).toInt()
        }

        val proteinTarget = when (mealTime.lowercase()) {
            "breakfast" -> (goals.protein * 0.25).toInt()
            "lunch" -> (goals.protein * 0.35).toInt()
            "dinner" -> (goals.protein * 0.30).toInt()
            else -> (goals.protein * 0.33).toInt()
        }

        return """
            Suggest 3-5 healthy $mealTime meal options that meet these nutritional targets:
            - Calories: approximately $calorieTarget kcal
            - Protein: approximately ${proteinTarget}g
            
            Dietary restrictions: $dietaryRestrictions
            ${if (preferences.isNotBlank()) "Additional preferences: $preferences" else ""}
            
            For each meal suggestion, provide:
            1. Meal name
            2. Brief description
            3. Approximate nutritional breakdown (calories, protein, carbs, fat)
            4. Key ingredients
            5. Quick preparation tips
            
            Format the response in a clear list format.
        """.trimIndent()
    }
}

