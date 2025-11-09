# Data Storage and Viewing Guide

## Where Data is Saved

All data in the app is saved using **Android DataStore (Preferences DataStore)**, which is a persistent storage solution that saves data to your device's internal storage.

### Storage Location
- **Physical Location**: `/data/data/com.nutrislice.tracker/datastore/` (on your device)
- **Access**: Only accessible by your app (private storage)
- **Persistence**: Data persists even after closing the app or restarting your device

### What Data is Saved

1. **Meals** (`nutrition_meals`)
   - All meals you've logged
   - Includes: name, calories, macros, timestamp, category, meal time, station, etc.

2. **Nutrition Goals** (`nutrition_goals`)
   - Your daily calorie and macro goals
   - Calories, protein, carbs, fat, fiber targets

3. **User Profile** (`user_profile`)
   - Age, gender, year, dietary restrictions
   - Personal information for meal planning

4. **Streak Data** (`streak`, `last_day`)
   - Your current streak count
   - Last day you met your goals

5. **Scraped Menu** (`scraped_menu`)
   - Menu items fetched from the web
   - Cached for offline access

## Where to View Saved Data in the App

### 1. **Tracker Screen** (Today's Meals)
- **Location**: Open the navigation drawer → Tap "Tracker"
- **Shows**: Only meals logged **today**
- **Features**:
  - Progress bars showing your daily goals vs. current intake
  - List of today's meals with macros
  - Delete meals
  - Empty state if no meals logged today

### 2. **Meal History Screen** (All Saved Meals) ✨ NEW!
- **Location**: Open the navigation drawer → Tap "Meal History"
- **Shows**: **ALL meals** you've ever logged, grouped by date
- **Features**:
  - Total meal count at the top
  - Meals grouped by date (most recent first)
  - Date headers show "Today", "Yesterday", or full date
  - Each meal shows:
    - Meal name
    - Time logged (e.g., "2:30 PM")
    - Full nutrition info (calories, protein, carbs, fat, fiber)
    - Meal time and station (if available)
  - Delete any meal from history
  - Scrollable list with date separators

### 3. **Profile Screen**
- **Location**: Navigation drawer → "Profile"
- **Shows**: Your user profile information
- **Features**: Edit age, gender, year, dietary restrictions

### 4. **All Locations Screen**
- **Location**: Navigation drawer → "All Locations"
- **Shows**: Menu items from different dining locations
- **Features**: Browse and add menu items to your meals

## How to Access Meal History

1. **Open the app**
2. **Tap the menu icon** (☰) in the top-left corner to open the navigation drawer
3. **Tap "Meal History"** (it's right below "Tracker")
4. **View all your saved meals** grouped by date

## Data Persistence

- ✅ Data is automatically saved when you:
  - Add a meal
  - Update your goals
  - Update your profile
  - Log meals from menu items

- ✅ Data persists across:
  - App restarts
  - Device reboots
  - App updates (usually)

- ⚠️ Data is **NOT** synced to cloud or other devices
- ⚠️ Uninstalling the app will delete all data

## Troubleshooting

### "I can't see my saved meals"
1. Make sure you're looking at the **"Meal History"** screen, not just "Tracker"
2. "Tracker" only shows today's meals
3. "Meal History" shows all meals ever logged

### "My meals disappeared"
- Check if you're filtering by date
- Make sure you're on the "Meal History" screen
- Data should persist unless the app was uninstalled

### "I want to export my data"
- Currently, there's no export feature
- Data is stored in Android DataStore format
- You can access it via Android Debug Bridge (ADB) if needed

## Summary

- **Storage**: Android DataStore (device's private storage)
- **View Today's Meals**: Navigation → "Tracker"
- **View All Meals**: Navigation → "Meal History" ✨
- **Data Persists**: Yes, automatically saved and persists across app restarts

