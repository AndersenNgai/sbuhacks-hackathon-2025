# NeuralSeek Meal Planning Setup Guide

This guide explains how to set up NeuralSeek API integration for AI-powered meal planning in the app.

## What is NeuralSeek?

NeuralSeek is an AI-powered platform that provides contextually relevant responses by integrating with knowledge bases. In this app, it's used to generate personalized meal plan suggestions based on your nutrition goals and dietary restrictions.

## Setup Instructions

### 1. Sign Up for NeuralSeek

1. Visit [https://neuralseek.com](https://neuralseek.com)
2. Sign up for an account
3. Complete the registration process

### 2. Check API Access and Generate an API Key

1. Log in to your NeuralSeek dashboard at [neuralseek.com](https://neuralseek.com)

2. **Check if API Access is Enabled:**
   - Navigate to the **"Integrate"** section in the top navigation bar
   - Look for API-related settings or options
   - If you see an option to "Enable API Access" or similar, make sure it's turned on
   - Some accounts may need to contact NeuralSeek support to enable API access
   - Check your account settings or subscription plan - API access might be a premium feature

3. **Generate an API Key:**
   - In the **"Integrate"** section, look for API key management options
   - Click the button to **Create** or **Generate** a new API key
     - The button might be labeled "Create API Key", "Generate API Key", "New API Key", or similar
   - If you don't see this option, your account might not have API access enabled
   
4. **Important:** Copy the API key immediately after generation
   - Some systems only show the key once for security reasons
   - Store it securely (you'll need it in the next step)

5. **Troubleshooting:**
   - If you can't find the API key section, your account might not have API access
   - Contact NeuralSeek support to enable API access for your account
   - Check if your subscription plan includes API access

### 3. Configure the API Key in the App

You have two options to set the API key:

#### Option A: Direct Configuration (Quick Setup)

1. Open `app/src/main/java/com/nutrislice/tracker/data/NeuralSeekService.kt`
2. Find the `NeuralSeekService` class constructor
3. Replace the empty string with your API key:

```kotlin
class NeuralSeekService(
    private val apiKey: String = "YOUR_API_KEY_HERE", // Replace with your actual API key
    private val baseUrl: String = "https://api.neuralseek.com/v1/test"
)
```

#### Option B: Environment Variable (Recommended for Production)

For a more secure approach, you can use environment variables or a configuration file:

1. Create a `local.properties` file in the project root (if it doesn't exist)
2. Add your API key:

```properties
NEURALSEEK_API_KEY=your_api_key_here
```

3. Update `NeuralSeekService.kt` to read from the property:

```kotlin
class NeuralSeekService(
    private val apiKey: String = System.getProperty("NEURALSEEK_API_KEY", ""),
    private val baseUrl: String = "https://api.neuralseek.com/v1/test"
)
```

**Note:** For production apps, consider using Android's BuildConfig or a secure storage solution.

### 4. Configure Knowledge Base (Optional but Recommended)

To get better meal planning suggestions, you can configure a knowledge base in NeuralSeek:

1. In the NeuralSeek dashboard, go to the **Configure** tab
2. Connect a knowledge base containing:
   - Nutritional information
   - Recipe databases
   - Dietary guidelines
   - Meal prep tips

NeuralSeek supports various knowledge bases:
- Watson Discovery
- Elastic AppSearch
- Amazon Kendra
- OpenSearch

## Using the Meal Planning Feature

### Full Meal Plan

1. Open the app and navigate to **Meal Planning** from the drawer menu
2. Enter the number of days for your meal plan (default: 7 days)
3. Optionally add preferences (e.g., "prefer Mediterranean cuisine, quick prep time")
4. Click **Generate Meal Plan**
5. The AI will create a personalized meal plan based on:
   - Your nutrition goals (calories, protein, carbs, fat, fiber)
   - Your dietary restrictions (from your profile)
   - Your additional preferences

### Meal Time Suggestions

1. Select a meal time (Breakfast, Lunch, or Dinner)
2. Optionally add preferences (e.g., "high protein, low carb")
3. Click **Get [Meal Time] Suggestions**
4. The AI will provide 3-5 meal suggestions for that specific meal time

## Features

- **Personalized Suggestions**: Uses your nutrition goals and dietary restrictions
- **Flexible Planning**: Generate plans for any number of days
- **Meal-Specific Suggestions**: Get suggestions for breakfast, lunch, or dinner
- **Custom Preferences**: Add your own preferences to customize suggestions

## Troubleshooting

### "NeuralSeek API key is not configured"

- Make sure you've set the API key in `NeuralSeekService.kt`
- Verify the API key is correct (no extra spaces, correct format)

### "NeuralSeek API error: 401"

- Your API key is invalid or expired
- Generate a new API key from the NeuralSeek dashboard

### "NeuralSeek API error: 403"

- Your API key doesn't have the necessary permissions
- Check your NeuralSeek account settings

### No suggestions generated

- Check your internet connection
- Verify the API key is correct
- Check the Logcat for detailed error messages (filter by "NeuralSeek")

## API Documentation

For more details about the NeuralSeek API, visit:
- [NeuralSeek API Documentation](https://api.neuralseek.com/)
- [NeuralSeek Help Center](https://help.neuralseek.com/)

## Security Notes

- **Never commit your API key to version control**
- Add `local.properties` to `.gitignore` if storing the key there
- For production apps, use secure storage solutions
- Rotate your API keys regularly

## Support

If you encounter issues:
1. Check the Logcat for error messages
2. Verify your API key is correct
3. Ensure you have an active NeuralSeek account
4. Check the NeuralSeek status page for service outages

