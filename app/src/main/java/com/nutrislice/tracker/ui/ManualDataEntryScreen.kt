package com.nutrislice.tracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nutrislice.tracker.model.DietaryInfo
import com.nutrislice.tracker.model.MealEntry
import com.nutrislice.tracker.model.NutritionFacts

@Composable
fun ManualDataEntryScreen(
    onSaveMeal: (MealEntry) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var station by remember { mutableStateOf("") }
    var mealTime by remember { mutableStateOf("Breakfast") }
    
    // Nutrition Facts
    var servingSize by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var totalFat by remember { mutableStateOf("") }
    var saturatedFat by remember { mutableStateOf("") }
    var transFat by remember { mutableStateOf("") }
    var cholesterol by remember { mutableStateOf("") }
    var sodium by remember { mutableStateOf("") }
    var totalCarbohydrate by remember { mutableStateOf("") }
    var dietaryFiber by remember { mutableStateOf("") }
    var totalSugars by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    
    // Dietary Info
    var isVegetarian by remember { mutableStateOf(false) }
    var isVegan by remember { mutableStateOf(false) }
    var isGlutenFree by remember { mutableStateOf(false) }
    var containsDairy by remember { mutableStateOf(false) }
    var containsEgg by remember { mutableStateOf(false) }
    var containsFish by remember { mutableStateOf(false) }
    
    var showSuccessDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add Menu Item from Screenshot",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Outlined.Close, contentDescription = "Close")
            }
        }

        Text(
            text = "Enter the nutrition information from your screenshot",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Basic Information Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Basic Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name *") },
                    leadingIcon = { Icon(Icons.Outlined.Fastfood, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true,
                    placeholder = { Text("e.g., Scrambled Eggs with Cream and Butter") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("e.g., Breakfast") }
                    )
                    OutlinedTextField(
                        value = station,
                        onValueChange = { station = it },
                        label = { Text("Station") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("e.g., Breakfast Specials") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { time ->
                        FilterChip(
                            selected = mealTime == time,
                            onClick = { mealTime = time },
                            label = { Text(time) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Nutrition Facts Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Nutrition Facts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = servingSize,
                    onValueChange = { servingSize = it },
                    label = { Text("Serving Size") },
                    leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    placeholder = { Text("e.g., 0.5 cups") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Calories *") },
                        leadingIcon = { Icon(Icons.Outlined.LocalFireDepartment, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("170") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein (g)") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("10") },
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = totalFat,
                        onValueChange = { totalFat = it },
                        label = { Text("Total Fat") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("14g") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = saturatedFat,
                        onValueChange = { saturatedFat = it },
                        label = { Text("Saturated Fat") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("6g") },
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = transFat,
                    onValueChange = { transFat = it },
                    label = { Text("Trans Fat") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    placeholder = { Text("0g") },
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cholesterol,
                        onValueChange = { cholesterol = it },
                        label = { Text("Cholesterol") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("317mg") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sodium,
                        onValueChange = { sodium = it },
                        label = { Text("Sodium") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("400mg") },
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = totalCarbohydrate,
                        onValueChange = { totalCarbohydrate = it },
                        label = { Text("Total Carbs") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("1g") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dietaryFiber,
                        onValueChange = { dietaryFiber = it },
                        label = { Text("Dietary Fiber") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("0g") },
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = totalSugars,
                    onValueChange = { totalSugars = it },
                    label = { Text("Total Sugars") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0g") },
                    singleLine = true
                )
            }
        }

        // Dietary Information Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Dietary Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isVegetarian,
                                onCheckedChange = { isVegetarian = it }
                            )
                            Text("Vegetarian", modifier = Modifier.padding(start = 8.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isVegan,
                                onCheckedChange = { isVegan = it }
                            )
                            Text("Vegan", modifier = Modifier.padding(start = 8.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isGlutenFree,
                                onCheckedChange = { isGlutenFree = it }
                            )
                            Text("Gluten Free", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = containsDairy,
                                onCheckedChange = { containsDairy = it }
                            )
                            Text("Contains Dairy", modifier = Modifier.padding(start = 8.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = containsEgg,
                                onCheckedChange = { containsEgg = it }
                            )
                            Text("Contains Egg", modifier = Modifier.padding(start = 8.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = containsFish,
                                onCheckedChange = { containsFish = it }
                            )
                            Text("Contains Fish", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }

        // Save Button
        Button(
            onClick = {
                val meal = createMealEntry(
                    name = name,
                    category = category,
                    station = station,
                    mealTime = mealTime,
                    servingSize = servingSize,
                    calories = calories,
                    totalFat = totalFat,
                    saturatedFat = saturatedFat,
                    transFat = transFat,
                    cholesterol = cholesterol,
                    sodium = sodium,
                    totalCarbohydrate = totalCarbohydrate,
                    dietaryFiber = dietaryFiber,
                    totalSugars = totalSugars,
                    protein = protein,
                    isVegetarian = isVegetarian,
                    isVegan = isVegan,
                    isGlutenFree = isGlutenFree,
                    containsDairy = containsDairy,
                    containsEgg = containsEgg,
                    containsFish = containsFish
                )
                if (meal != null) {
                    onSaveMeal(meal)
                    showSuccessDialog = true
                }
            },
            enabled = name.isNotBlank() && calories.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Outlined.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Menu Item")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "How to Use",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter data from your screenshots manually, or share screenshots with the developer to automatically extract and add the data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Fill Button (for testing)
        OutlinedButton(
            onClick = {
                // Pre-fill with example data from screenshot
                name = "Scrambled Eggs with Cream and Butter"
                category = "Breakfast"
                station = "Breakfast Specials"
                mealTime = "Breakfast"
                servingSize = "0.5 cups"
                calories = "170"
                totalFat = "14g"
                saturatedFat = "6g"
                transFat = "0g"
                cholesterol = "317mg"
                sodium = "400mg"
                totalCarbohydrate = "1g"
                dietaryFiber = "0g"
                totalSugars = "0g"
                protein = "10"
                isVegetarian = true
                containsDairy = true
                containsEgg = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Quick Fill: Scrambled Eggs Example")
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                onDismiss()
            },
            title = { Text("Success!") },
            text = { Text("Menu item added to menu! You can now find it in 'All Locations' and add it to your tracker.") },
            confirmButton = {
                TextButton(onClick = { 
                    showSuccessDialog = false
                    onDismiss()
                }) {
                    Text("OK")
                }
            }
        )
    }
}

private fun createMealEntry(
    name: String,
    category: String,
    station: String,
    mealTime: String,
    servingSize: String,
    calories: String,
    totalFat: String,
    saturatedFat: String,
    transFat: String,
    cholesterol: String,
    sodium: String,
    totalCarbohydrate: String,
    dietaryFiber: String,
    totalSugars: String,
    protein: String,
    isVegetarian: Boolean,
    isVegan: Boolean,
    isGlutenFree: Boolean,
    containsDairy: Boolean,
    containsEgg: Boolean,
    containsFish: Boolean
): MealEntry? {
    if (name.isBlank() || calories.isBlank()) return null

    val caloriesValue = calories.toDoubleOrNull() ?: return null
    val proteinValue = protein.toDoubleOrNull() ?: 0.0
    
    // Extract numeric values from strings like "14g" or "317mg"
    val fatValue = extractNumericValue(totalFat) ?: 0.0
    val carbsValue = extractNumericValue(totalCarbohydrate) ?: 0.0
    val fiberValue = extractNumericValue(dietaryFiber) ?: 0.0

    val dietaryInfo = mutableListOf<DietaryInfo>()
    if (isVegetarian) dietaryInfo.add(DietaryInfo.VEGETARIAN)
    if (isVegan) dietaryInfo.add(DietaryInfo.VEGAN)
    if (isGlutenFree) dietaryInfo.add(DietaryInfo.GLUTEN_FREE)
    if (containsDairy) dietaryInfo.add(DietaryInfo.CONTAINS_DAIRY)
    if (containsEgg) dietaryInfo.add(DietaryInfo.CONTAINS_EGG)
    if (containsFish) dietaryInfo.add(DietaryInfo.CONTAINS_FISH)

    val nutritionFacts = NutritionFacts(
        servingSize = servingSize.ifBlank { "1 serving" },
        calories = caloriesValue.toInt(),
        totalFat = totalFat.ifBlank { "0g" },
        saturatedFat = saturatedFat.ifBlank { "0g" },
        transFat = transFat.ifBlank { "0g" },
        cholesterol = cholesterol.ifBlank { "0mg" },
        sodium = sodium.ifBlank { "0mg" },
        totalCarbohydrate = totalCarbohydrate.ifBlank { "0g" },
        dietaryFiber = dietaryFiber.ifBlank { "0g" },
        totalSugars = totalSugars.ifBlank { "0g" }
    )

    return MealEntry(
        name = name.trim(),
        calories = caloriesValue,
        protein = proteinValue,
        carbs = carbsValue,
        fat = fatValue,
        fiber = fiberValue,
        category = category.ifBlank { mealTime },
        mealTime = mealTime,
        station = station.ifBlank { category.ifBlank { mealTime } },
        nutritionFacts = nutritionFacts,
        dietaryInfo = dietaryInfo
    )
}

private fun extractNumericValue(value: String): Double? {
    if (value.isBlank()) return null
    // Extract numbers and decimal points, ignore units like "g", "mg", etc.
    val numericPart = value.replace(Regex("[^0-9.]"), "")
    return numericPart.toDoubleOrNull()
}

