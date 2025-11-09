# NeuralSeek 401 Error Troubleshooting Guide

## Error: "NeuralSeek API error: 401"

A 401 error means **authentication failed**. Here's how to fix it:

### Step 1: Verify Your API Key

1. **Check if the API key is set correctly:**
   - Open `app/src/main/java/com/nutrislice/tracker/data/NeuralSeekService.kt`
   - Look at line 26 - make sure there's a valid API key (not empty string)

2. **Get a new API key from NeuralSeek:**
   - Go to [neuralseek.com](https://neuralseek.com)
   - Log in to your account
   - Navigate to **"Integrate"** in the top navigation
   - Find the API key section
   - Generate a new API key
   - Copy it immediately

3. **Update the API key in the code:**
   ```kotlin
   class NeuralSeekService(
       private val apiKey: String = "YOUR_NEW_API_KEY_HERE", // Replace this
       private val baseUrl: String = "https://api.neuralseek.com/v1/test"
   )
   ```

### Step 2: Check API Key Format

The API key should look like one of these formats:
- `d854492c-1b3306ac-ad0f1ac6-3363b14d` (with hyphens)
- A long string without spaces

**Common issues:**
- ❌ Extra spaces before/after the key
- ❌ Missing characters (key was cut off)
- ❌ Wrong key copied (copied from wrong field)

### Step 3: Verify API Key is Active

1. Check your NeuralSeek dashboard
2. Make sure the API key is **active** (not revoked or expired)
3. Some API keys have expiration dates - generate a new one if expired

### Step 4: Check API Endpoint

The current endpoint is: `https://api.neuralseek.com/v1/test/seek`

If this doesn't work, try:
- `https://api.neuralseek.com/v1/seek` (without `/test`)
- Check NeuralSeek documentation for the correct endpoint

### Step 5: Test the API Key

You can test your API key using curl:

```bash
curl -X POST "https://api.neuralseek.com/v1/test/seek" \
  -H "accept: application/json" \
  -H "apikey: YOUR_API_KEY_HERE" \
  -H "Content-Type: application/json" \
  -d '{"question": "Test question"}'
```

If this returns 401, the API key is invalid.

### Step 6: Check Logs

Check the Android Logcat for detailed error messages:
1. Filter by "NeuralSeek"
2. Look for:
   - "API Key (first 10 chars): ..." - confirms the key is being used
   - "API error: 401" - authentication failed
   - The full error response from the API

### Common Solutions

1. **Generate a new API key** - Old keys might be expired
2. **Check account status** - Make sure your NeuralSeek account is active
3. **Verify API access** - Some accounts might need API access enabled
4. **Contact NeuralSeek support** - If nothing works, they can help verify your account

### Alternative: Use Manual Entry

If NeuralSeek API continues to have issues, you can:
- Use the "Add from Screenshot" feature to manually enter menu items
- The meal planning feature will work once the API key is fixed

### Current Status

The code now tries both authentication methods:
- `Authorization: Bearer YOUR_API_KEY`
- `apikey: YOUR_API_KEY`

This should work with most NeuralSeek API configurations.

