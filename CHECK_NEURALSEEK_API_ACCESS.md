# How to Check if NeuralSeek Account Has API Access Enabled

## Step-by-Step Guide

### Method 1: Check in the Integrate Section

1. **Log in to NeuralSeek:**
   - Go to [neuralseek.com](https://neuralseek.com)
   - Log in with your account credentials

2. **Navigate to Integrate:**
   - Click on **"Integrate"** in the top navigation bar
   - This is where API settings are typically located

3. **Look for API Key Options:**
   - If you see options to create/generate API keys → **API access is enabled** ✅
   - If you see a message like "API access not available" → **API access is disabled** ❌
   - If you don't see any API-related options → **API access might not be enabled** ❌

### Method 2: Check Account Settings

1. **Go to Account Settings:**
   - Look for "Settings", "Account", or "Profile" in the navigation
   - Check for API-related settings or permissions

2. **Check Subscription/Plan:**
   - Some NeuralSeek plans might require a specific tier for API access
   - Look for "API Access" in your plan features
   - Check if you're on a plan that includes API functionality

### Method 3: Try to Generate an API Key

1. **In the Integrate section:**
   - Look for a button like "Create API Key", "Generate API Key", or "New API Key"
   - If you can click it and generate a key → **API access is enabled** ✅
   - If the button is disabled or missing → **API access might not be enabled** ❌
   - If you get an error message → Read the error - it will tell you if API access is the issue

### Method 4: Check Error Messages

If you're getting 401 errors in the app:
- The error message might indicate if API access is the problem
- Check the NeuralSeek dashboard for any notifications about API status
- Look for any warnings or messages about API access

### Method 5: Contact NeuralSeek Support

If you're unsure:
1. **Contact NeuralSeek Support:**
   - Go to their help/support section
   - Ask: "Does my account have API access enabled?"
   - Provide your account email/username

2. **What to ask:**
   - "How do I enable API access for my account?"
   - "Does my subscription plan include API access?"
   - "I'm getting 401 errors - is API access enabled?"

## Common Signs API Access is Enabled

✅ **You have API access if:**
- You can see "Create API Key" or similar buttons
- You can generate API keys successfully
- You see API-related settings in your dashboard
- Your subscription plan lists "API Access" as a feature

❌ **You might NOT have API access if:**
- You can't find any API key options
- API-related buttons are disabled or grayed out
- You see messages about upgrading your plan
- You get errors when trying to generate keys
- Your plan doesn't mention API access

## How to Enable API Access

If API access is not enabled:

1. **Check Your Plan:**
   - Upgrade to a plan that includes API access (if available)
   - Some plans might require a paid subscription

2. **Contact Support:**
   - Reach out to NeuralSeek support
   - Request API access for your account
   - They can enable it or guide you on how to get it

3. **Verify Account Status:**
   - Make sure your account is fully activated
   - Complete any required account setup steps
   - Verify your email if needed

## Alternative: Test API Access

You can test if your API key works using curl:

```bash
curl -X POST "https://api.neuralseek.com/v1/test/seek" \
  -H "accept: application/json" \
  -H "APIkey: YOUR_API_KEY_HERE" \
  -H "Content-Type: application/json" \
  -d '{"question": "Test question"}'
```

- If you get a 200 response → API access is working ✅
- If you get 401 → API key might be invalid or API access not enabled ❌
- If you get 403 → API access might be restricted ❌

## Quick Checklist

- [ ] Logged into NeuralSeek dashboard
- [ ] Checked "Integrate" section for API options
- [ ] Tried to generate/create an API key
- [ ] Checked account settings for API permissions
- [ ] Verified subscription plan includes API access
- [ ] Contacted support if still unsure

## Need Help?

If you're still having issues:
1. Check NeuralSeek's documentation
2. Contact their support team
3. Check your account email for any API-related notifications
4. Verify your API key is correct and active

