
@file:OptIn(ExperimentalMaterial3Api::class)

package com.nutrislice.tracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SetMeal
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nutrislice.tracker.R
import com.nutrislice.tracker.NutritionViewModel
import com.nutrislice.tracker.model.DietaryInfo
import com.nutrislice.tracker.model.DietaryRestriction
import com.nutrislice.tracker.model.MealEntry
import com.nutrislice.tracker.model.MealInput
import com.nutrislice.tracker.model.Location
import com.nutrislice.tracker.model.MacroTotals
import com.nutrislice.tracker.model.NutritionFacts
import com.nutrislice.tracker.model.NutritionGoals
import com.nutrislice.tracker.model.UserProfile
import com.nutrislice.tracker.model.totals
import com.nutrislice.tracker.ui.theme.CarbsOrange
import com.nutrislice.tracker.ui.theme.FatYellow
import com.nutrislice.tracker.ui.theme.FiberBlue
import com.nutrislice.tracker.ui.theme.PrimaryBlue
import com.nutrislice.tracker.ui.theme.PrimaryGreen
import com.nutrislice.tracker.ui.theme.ProteinPurple
import com.nutrislice.tracker.ui.theme.WarningRed
import kotlinx.coroutines.launch

data class Category(val name: String, val items: List<MealEntry>)

@Composable
fun NutritionRoute(viewModel: NutritionViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        val message = uiState.userMessage
        if (!message.isNullOrEmpty()) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    NutritionScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onSaveGoals = viewModel::saveGoals,
        onAddMeals = viewModel::addMeals,
        onDeleteMeal = viewModel::deleteMeal,
        onSaveProfile = viewModel::saveUserProfile,
        onFetchMenu = viewModel::fetchMenuFromWeb,
        viewModel = viewModel
    )
}

@Composable
fun NutritionScreen(
    uiState: NutritionViewModel.NutritionUiState,
    snackbarHostState: SnackbarHostState,
    onSaveGoals: (NutritionGoals) -> Unit,
    onAddMeals: (List<MealEntry>) -> Unit,
    onDeleteMeal: (Long) -> Unit,
    onSaveProfile: (UserProfile) -> Unit,
    onFetchMenu: () -> Unit,
    viewModel: NutritionViewModel
) {
    var showGoalDialog by rememberSaveable { mutableStateOf(false) }
    var goalsDraft by rememberSaveable(uiState.goals, stateSaver = NutritionGoalsSaver) { mutableStateOf(uiState.goals) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedScreen by rememberSaveable { mutableStateOf("All Locations") }
    var selectedLocationName by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedMealTime by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedStationName by rememberSaveable { mutableStateOf<String?>(null) }
    var showNutritionFacts by remember { mutableStateOf<MealEntry?>(null) }

    val selectedLocation = uiState.locations.find { it.name == selectedLocationName }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.SetMeal, contentDescription = null) },
                    label = { Text("All Locations") },
                    selected = selectedScreen == "All Locations",
                    onClick = { 
                        scope.launch { drawerState.close() }
                        selectedScreen = "All Locations"
                        selectedLocationName = null
                        selectedMealTime = null
                        selectedStationName = null
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Fastfood, contentDescription = null) },
                    label = { Text("Tracker") },
                    selected = selectedScreen == "Tracker",
                    onClick = { 
                        scope.launch { drawerState.close() }
                        selectedScreen = "Tracker"
                        selectedLocationName = null
                        selectedMealTime = null
                        selectedStationName = null
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.SetMeal, contentDescription = null) },
                    label = { Text("Meal History") },
                    selected = selectedScreen == "Meal History",
                    onClick = { 
                        scope.launch { drawerState.close() }
                        selectedScreen = "Meal History"
                        selectedLocationName = null
                        selectedMealTime = null
                        selectedStationName = null
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Fastfood, contentDescription = null) },
                    label = { Text("Menu Items") },
                    selected = selectedScreen == "Menu Items",
                    onClick = { 
                        scope.launch { drawerState.close() }
                        selectedScreen = "Menu Items"
                        selectedLocationName = null
                        selectedMealTime = null
                        selectedStationName = null
                    }
                )
                 NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    label = { Text("Profile") },
                    selected = selectedScreen == "Profile",
                    onClick = { 
                        scope.launch { drawerState.close() }
                        selectedScreen = "Profile"
                        selectedLocationName = null
                        selectedMealTime = null
                        selectedStationName = null
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.SetMeal, contentDescription = null) },
                    label = { Text("Meal Planning") },
                    selected = selectedScreen == "Meal Planning",
                    onClick = { 
                        scope.launch { drawerState.close() }
                        selectedScreen = "Meal Planning"
                        selectedLocationName = null
                        selectedMealTime = null
                        selectedStationName = null
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    label = { Text("Add from Screenshot") },
                    selected = selectedScreen == "Manual Entry",
                    onClick = { 
                        scope.launch { drawerState.close() }
                        selectedScreen = "Manual Entry"
                        selectedLocationName = null
                        selectedMealTime = null
                        selectedStationName = null
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.ThumbUp, contentDescription = null) },
                    label = { Text("Meal Recommendation") },
                    selected = selectedScreen == "Meal Recommendation",
                    onClick = { 
                        scope.launch { drawerState.close() }
                        selectedScreen = "Meal Recommendation"
                        selectedLocationName = null
                        selectedMealTime = null
                        selectedStationName = null
                    }
                )
            }
        }
    ) { 
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                if (selectedLocation != null) selectedLocation.name else "SBU Nutrition", 
                                style = MaterialTheme.typography.titleLarge
                            )
                            if (selectedStationName != null) {
                                Text(
                                    text = selectedStationName!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            } else if (selectedMealTime != null) {
                                Text(
                                    text = selectedMealTime!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        if (selectedStationName != null) {
                            IconButton(onClick = { selectedStationName = null }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                        } else if (selectedMealTime != null) {
                            IconButton(onClick = { selectedMealTime = null }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                        } else if (selectedLocation != null) {
                            IconButton(onClick = { selectedLocationName = null }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Outlined.Menu, contentDescription = "Menu")
                            }
                        }
                    },
                    actions = {
                        StreakCounter(streak = uiState.streak)
                        if (selectedScreen == "All Locations" && selectedLocation == null) {
                            IconButton(
                                onClick = onFetchMenu,
                                enabled = !uiState.isFetchingMenu
                            ) {
                                if (uiState.isFetchingMenu) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Outlined.Refresh,
                                        contentDescription = "Refresh menu"
                                    )
                                }
                            }
                        }
                        if (selectedScreen == "Tracker") {
                            Button(onClick = {
                                goalsDraft = uiState.goals
                                showGoalDialog = true
                            }) {
                                Icon(Icons.Outlined.Flag, contentDescription = null)
                                Spacer(Modifier.size(6.dp))
                                Text("Set Goals")
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                when (selectedScreen) {
                    "Tracker" -> TrackerScreen(uiState = uiState, onDeleteMeal = onDeleteMeal)
                    "Meal History" -> MealHistoryScreen(uiState = uiState, onDeleteMeal = onDeleteMeal)
                    "Menu Items" -> MenuItemsScreen(
                        menuItems = uiState.menu,
                        onAddMeals = onAddMeals,
                        onMealClicked = { showNutritionFacts = it },
                        onReloadMenu = viewModel::loadScreenshotData
                    )
                    "All Locations" -> {
                        if (selectedLocation == null) {
                            LocationGrid(
                                uiState = uiState, 
                                onLocationSelected = { selectedLocationName = it.name },
                                onFetchMenu = onFetchMenu
                            )
                        } else if (selectedMealTime == null) {
                            val mealTimes = uiState.menu
                                .filter { it.category == selectedLocation.name || it.station == selectedLocation.name }
                                .map { it.mealTime }
                                .distinct()
                                .sorted()
                            
                            if (mealTimes.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Fastfood,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Spacer(Modifier.height(16.dp))
                                        Text(
                                            "No meal times available",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "Try fetching the menu first",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                MealTimeGrid(mealTimes = mealTimes, onMealTimeSelected = { selectedMealTime = it })
                            }
                        } else if (selectedStationName == null) {
                            val stations = uiState.menu
                                .filter { 
                                    (it.category == selectedLocation.name || it.station == selectedLocation.name) && 
                                    it.mealTime == selectedMealTime 
                                }
                                .groupBy { it.station }
                                .map { Category(it.key, it.value) }
                            
                            if (stations.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Fastfood,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Spacer(Modifier.height(16.dp))
                                        Text(
                                            "No stations available",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                StationGrid(stations = stations, onStationSelected = { selectedStationName = it.name })
                            }
                        } else {
                            val category = Category(
                                selectedStationName!!,
                                uiState.menu.filter { it.station == selectedStationName!! }
                            )
                            CategoryScreen(
                                category = category, 
                                onAddMeals = onAddMeals, 
                                onMealClicked = { showNutritionFacts = it }
                            )
                        }
                    }
                    "Profile" -> {
                        ProfileScreen(userProfile = uiState.userProfile, onSaveProfile = onSaveProfile)
                    }
                    "Meal Planning" -> {
                        MealPlanningScreen(
                            uiState = uiState,
                            onGenerateMealPlan = viewModel::generateMealPlan,
                            onGenerateMealTimeSuggestion = viewModel::generateMealTimeSuggestion,
                            onClearMealPlan = viewModel::clearMealPlanSuggestion,
                            onClearMealTimeSuggestion = viewModel::clearMealTimeSuggestion
                        )
                    }
                    "Meal Recommendation" -> {
                        MealRecommendationScreen(uiState = uiState, onAddMeals = onAddMeals, onMealClicked = { showNutritionFacts = it })
                    }
                    "Manual Entry" -> {
                        ManualDataEntryScreen(
                            onSaveMeal = { meal -> viewModel.addToMenu(meal) }, // Save to menu instead of meals
                            onDismiss = { selectedScreen = "All Locations" }
                        )
                    }
                }
            }
        }

        if (showGoalDialog) {
            GoalsDialog(
                goals = goalsDraft,
                onGoalsChange = { goalsDraft = it },
                onDismiss = { showGoalDialog = false },
                onSave = {
                    onSaveGoals(goalsDraft)
                    showGoalDialog = false
                }
            )
        }

        if (showNutritionFacts != null) {
            NutritionFactsDialog(
                meal = showNutritionFacts!!,
                onDismiss = { showNutritionFacts = null },
                onAddMeal = { onAddMeals(listOf(it)) }
            )
        }
    }
}

@Composable
fun TrackerScreen(
    uiState: NutritionViewModel.NutritionUiState,
    onDeleteMeal: (Long) -> Unit
) {
    val totals = uiState.meals.totals()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ProgressSection(goals = uiState.goals, totals = totals)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meals Today",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(imageVector = Icons.Outlined.SetMeal, contentDescription = null)
            }
        }

        if (uiState.meals.isEmpty()) {
            item {
                EmptyMealState()
            }
        } else {
            items(uiState.meals) { meal ->
                MealRow(meal = meal, onDeleteMeal = onDeleteMeal)
            }
        }
    }
}

@Composable
fun LocationGrid(
    uiState: NutritionViewModel.NutritionUiState, 
    onLocationSelected: (Location) -> Unit,
    onFetchMenu: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Fetch menu button with loading state
            Button(
                onClick = onFetchMenu,
                enabled = !uiState.isFetchingMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (uiState.isFetchingMenu) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Fetching Menu...")
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Fetch Menu from Web")
                }
            }
            
            // Show menu status
            when {
                uiState.isFetchingMenu -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(8.dp))
                            Text("Loading menu items...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                uiState.menu.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Fastfood,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No menu items loaded",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Tap 'Fetch Menu from Web' to load today's menu",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    // Show menu count and stats
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${uiState.menu.size} Menu Items",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${uiState.categories.size} Categories",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.SetMeal,
                                contentDescription = null,
                                tint = PrimaryGreen
                            )
                        }
                    }
                }
            }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.locations) { location ->
                    LocationCard(location = location, onLocationSelected = onLocationSelected)
                }
            }
        }
    }
}

@Composable
fun LocationCard(location: Location, onLocationSelected: (Location) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onLocationSelected(location) }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(location.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = location.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(location.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun MealTimeGrid(mealTimes: List<String>, onMealTimeSelected: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(mealTimes) { mealTime ->
            MealTimeCard(mealTime = mealTime, onMealTimeSelected = onMealTimeSelected)
        }
    }
}

@Composable
fun MealTimeCard(mealTime: String, onMealTimeSelected: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onMealTimeSelected(mealTime) }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(mealTime, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StationGrid(stations: List<Category>, onStationSelected: (Category) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(stations) { station ->
            StationCard(station = station, onStationSelected = onStationSelected)
        }
    }
}

@Composable
fun StationCard(station: Category, onStationSelected: (Category) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onStationSelected(station) }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Only show image if items exist and have imageUrl
            val imageUrl = station.items.firstOrNull()?.imageUrl
            if (imageUrl != null && imageUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = station.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )
            } else {
                // Show colored background if no image
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(PrimaryGreen.copy(alpha = 0.2f))
                )
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        station.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (imageUrl != null) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    if (station.items.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${station.items.size} items",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (imageUrl != null) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItemsScreen(
    menuItems: List<MealEntry>,
    onAddMeals: (List<MealEntry>) -> Unit,
    onMealClicked: (MealEntry) -> Unit,
    onReloadMenu: () -> Unit = {}
) {
    // Group menu items by meal time for easier browsing
    val itemsByMealTime = menuItems.groupBy { it.mealTime.ifBlank { "Other" } }
        .toList()
        .sortedBy { it.first }
    
    if (menuItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Fastfood,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "No menu items available",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Add items from screenshots or fetch menu from web",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Menu Items",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${menuItems.size} items",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        IconButton(
                            onClick = { onReloadMenu() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Reload menu items",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            itemsByMealTime.forEach { (mealTime, items) ->
                item {
                    Text(
                        text = mealTime,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(items) { meal ->
                    MenuItem(meal = meal, onMealClicked = onMealClicked)
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                }
            }
        }
    }
}

@Composable
fun CategoryScreen(
    category: Category,
    onAddMeals: (List<MealEntry>) -> Unit,
    onMealClicked: (MealEntry) -> Unit
) {
    if (category.items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Fastfood,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "No items in this category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Try fetching the menu from the main screen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "${category.items.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(category.items) { meal ->
                MenuItem(meal = meal, onMealClicked = onMealClicked)
            }
        }
    }
}

@Composable
fun MenuItem(
    meal: MealEntry, 
    onMealClicked: (MealEntry) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp), 
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMealClicked(meal) }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.name,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (meal.calories > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${meal.calories.toInt()} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                if (meal.station.isNotBlank() && meal.station != meal.category) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = meal.station,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                if (meal.dietaryInfo.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row {
                        meal.dietaryInfo.forEach { 
                            Icon(
                                painter = painterResource(id = it.icon),
                                contentDescription = it.name,
                                modifier = Modifier.size(20.dp).padding(end = 4.dp),
                                tint = MaterialTheme.colorScheme.primary
                            ) 
                        }
                    }
                }
            }
            Icon(
                imageVector = Icons.Outlined.Fastfood,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ProfileScreen(userProfile: UserProfile, onSaveProfile: (UserProfile) -> Unit) {
    var age by rememberSaveable { mutableStateOf(userProfile.age.toString()) }
    var gender by rememberSaveable { mutableStateOf(userProfile.gender) }
    var year by rememberSaveable { mutableStateOf(userProfile.year) }
    var restrictions by rememberSaveable { mutableStateOf(userProfile.restrictions) }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text("Gender") }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = year,
            onValueChange = { year = it },
            label = { Text("Year") }
        )
        Spacer(Modifier.height(16.dp))
        Text("Dietary Restrictions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Column {
            DietaryRestriction.values().forEach { restriction ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = restriction in restrictions,
                        onCheckedChange = { 
                            restrictions = if (restriction in restrictions) {
                                restrictions - restriction
                            } else {
                                restrictions + restriction
                            }
                        }
                    )
                    Text(restriction.name)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { 
            val profile = UserProfile(
                age = age.toIntOrNull() ?: 0,
                gender = gender,
                year = year,
                restrictions = restrictions
            )
            onSaveProfile(profile) 
        }) {
            Text("Save Profile")
        }
    }
}

@Composable
fun MealRecommendationScreen(
    uiState: NutritionViewModel.NutritionUiState,
    onAddMeals: (List<MealEntry>) -> Unit,
    onMealClicked: (MealEntry) -> Unit
) {
    if (uiState.userProfile.age == 0 || uiState.userProfile.gender.isBlank()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Please complete your profile to get meal recommendations.")
        }
        return
    }

    val recommendations = remember(uiState.menu, uiState.userProfile, uiState.goals) {
        getMealRecommendations(uiState.menu, uiState.userProfile, uiState.goals)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Breakfast (8-9 AM)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        items(recommendations.breakfast) { meal ->
            MenuItem(meal = meal, onMealClicked = onMealClicked)
        }
        item {
            Button(onClick = { onAddMeals(recommendations.breakfast) }) {
                Text("Add Breakfast to Tracker")
            }
        }

        item {
            Text("Lunch (12-1 PM)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        items(recommendations.lunch) { meal ->
            MenuItem(meal = meal, onMealClicked = onMealClicked)
        }
        item {
            Button(onClick = { onAddMeals(recommendations.lunch) }) {
                Text("Add Lunch to Tracker")
            }
        }

        item {
            Text("Dinner (6-7 PM)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        items(recommendations.dinner) { meal ->
            MenuItem(meal = meal, onMealClicked = onMealClicked)
        }
        item {
            Button(onClick = { onAddMeals(recommendations.dinner) }) {
                Text("Add Dinner to Tracker")
            }
        }
    }
}

data class MealRecommendations(
    val breakfast: List<MealEntry>,
    val lunch: List<MealEntry>,
    val dinner: List<MealEntry>
)

fun getMealRecommendations(menu: List<MealEntry>, userProfile: UserProfile, goals: NutritionGoals): MealRecommendations {
    val filteredMenu = menu.filter { meal ->
        userProfile.restrictions.none { restriction ->
            when (restriction) {
                DietaryRestriction.VEGETARIAN -> !meal.dietaryInfo.contains(DietaryInfo.VEGETARIAN)
                DietaryRestriction.VEGAN -> !meal.dietaryInfo.contains(DietaryInfo.VEGAN)
                DietaryRestriction.GLUTEN_FREE -> meal.dietaryInfo.contains(DietaryInfo.GLUTEN_FREE)
            }
        }
    }

    val breakfast = filteredMenu.filter { it.mealTime == "Breakfast" }.shuffled().take(1)
    val lunch = filteredMenu.filter { it.mealTime == "Lunch" }.shuffled().take(1)
    val dinner = filteredMenu.filter { it.mealTime == "Dinner" }.shuffled().take(1)

    return MealRecommendations(breakfast, lunch, dinner)
}


@Composable
private fun StreakCounter(streak: Int) {
    if (streak > 0) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
            Icon(Icons.Outlined.LocalFireDepartment, contentDescription = "Streak", tint = CarbsOrange)
            Spacer(Modifier.size(4.dp))
            Text(streak.toString(), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProgressSection(goals: NutritionGoals, totals: MacroTotals) {
    Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Today's Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            MacroProgressRow(
                label = "Calories",
                current = totals.calories,
                goal = goals.calories,
                unit = "kcal",
                barColor = PrimaryBlue
            )
            MacroProgressRow(
                label = "Protein",
                current = totals.protein,
                goal = goals.protein,
                unit = "g",
                barColor = ProteinPurple
            )
            MacroProgressRow(
                label = "Carbs",
                current = totals.carbs,
                goal = goals.carbs,
                unit = "g",
                barColor = CarbsOrange
            )
            MacroProgressRow(
                label = "Fat",
                current = totals.fat,
                goal = goals.fat,
                unit = "g",
                barColor = FatYellow
            )
            MacroProgressRow(
                label = "Fiber",
                current = totals.fiber,
                goal = goals.fiber,
                unit = "g",
                barColor = FiberBlue
            )
        }
    }
}

@Composable
private fun MacroProgressRow(
    label: String,
    current: Double,
    goal: Int,
    unit: String,
    barColor: Color
) {
    val percentage = if (goal <= 0) 0f else (current / goal).toFloat().coerceIn(0f, 1f)
    val isOverGoal = goal > 0 && current > goal
    val remaining = if (goal <= 0) 0.0 else goal - current
    val goalLabel = if (goal > 0) goal.toString() else "—"

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text("${current.toInt()} / $goalLabel $unit")
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                .padding(0.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            color = if (isOverGoal) WarningRed else barColor
        )
        val feedback = when {
            goal <= 0 -> "No goal set"
            isOverGoal -> "${(current - goal).toInt()} over goal"
            else -> "${remaining.toInt()} remaining"
        }
        Text(
            feedback,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun MealRow(meal: MealEntry, onDeleteMeal: (Long) -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(meal.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "${meal.calories.toInt()} kcal • ${meal.protein.toInt()}g P • ${meal.carbs.toInt()}g C • ${meal.fat.toInt()}g F • ${meal.fiber.toInt()}g D",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = { onDeleteMeal(meal.id) }) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete meal")
            }
        }
    }
}

@Composable
private fun EmptyMealState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(color = PrimaryGreen.copy(alpha = 0.1f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Fastfood,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text("No meals logged yet", fontWeight = FontWeight.Medium)
        Text(
            "Tap the + button to log your first meal",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MealHistoryScreen(
    uiState: NutritionViewModel.NutritionUiState,
    onDeleteMeal: (Long) -> Unit
) {
    // Group meals by date
    val mealsByDate = uiState.allMeals
        .sortedByDescending { it.timestamp }
        .groupBy { 
            val calendar = java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }
            String.format("%04d-%02d-%02d", calendar.get(java.util.Calendar.YEAR), 
                calendar.get(java.util.Calendar.MONTH) + 1, 
                calendar.get(java.util.Calendar.DAY_OF_MONTH))
        }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meal History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${uiState.allMeals.size} total meals",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        if (mealsByDate.isEmpty()) {
            item {
                EmptyMealState()
            }
        } else {
            mealsByDate.forEach { (date, meals) ->
                item {
                    // Date header
                    Text(
                        text = formatDate(date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // Meals for this date
                items(meals) { meal ->
                    MealRowWithDate(meal = meal, onDeleteMeal = onDeleteMeal)
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun MealRowWithDate(meal: MealEntry, onDeleteMeal: (Long) -> Unit) {
    val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
    val mealTime = timeFormat.format(java.util.Date(meal.timestamp))
    
    Card(
        shape = RoundedCornerShape(12.dp), 
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        meal.name, 
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "• $mealTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${meal.calories.toInt()} kcal • ${meal.protein.toInt()}g P • ${meal.carbs.toInt()}g C • ${meal.fat.toInt()}g F • ${meal.fiber.toInt()}g Fiber",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (meal.mealTime.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "${meal.mealTime} • ${meal.station}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
            IconButton(onClick = { onDeleteMeal(meal.id) }) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete meal")
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(dateString)
        val calendar = java.util.Calendar.getInstance()
        val today = java.util.Calendar.getInstance()
        val yesterday = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
        
        if (date != null) {
            calendar.time = date
            val formatter = when {
                calendar.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                calendar.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR) -> {
                    "Today"
                }
                calendar.get(java.util.Calendar.YEAR) == yesterday.get(java.util.Calendar.YEAR) &&
                calendar.get(java.util.Calendar.DAY_OF_YEAR) == yesterday.get(java.util.Calendar.DAY_OF_YEAR) -> {
                    "Yesterday"
                }
                else -> {
                    java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault()).format(date)
                }
            }
            formatter
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

@Composable
private fun GoalsDialog(
    goals: NutritionGoals,
    onGoalsChange: (NutritionGoals) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onSave) {
                Text("Save Goals")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Set Daily Goals", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GoalField(label = "Calories (kcal)", value = goals.calories, onValueChange = {
                    onGoalsChange(goals.copy(calories = it))
                })
                GoalField(label = "Protein (g)", value = goals.protein, onValueChange = {
                    onGoalsChange(goals.copy(protein = it))
                })
                GoalField(label = "Carbs (g)", value = goals.carbs, onValueChange = {
                    onGoalsChange(goals.copy(carbs = it))
                })
                GoalField(label = "Fat (g)", value = goals.fat, onValueChange = {
                    onGoalsChange(goals.copy(fat = it))
                })
                GoalField(label = "Fiber (g)", value = goals.fiber, onValueChange = {
                    onGoalsChange(goals.copy(fiber = it))
                })
            }
        }
    )
}

@Composable
private fun GoalField(label: String, value: Int, onValueChange: (Int) -> Unit) {
    OutlinedTextField(
        value = value.takeIf { it >= 0 }?.toString().orEmpty(),
        onValueChange = { input ->
            val parsed = input.toIntOrNull() ?: 0
            onValueChange(parsed)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
fun NutritionFactsDialog(meal: MealEntry, onDismiss: () -> Unit, onAddMeal: (MealEntry) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(meal.name, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    meal.dietaryInfo.forEach { 
                        Icon(painter = painterResource(id = it.icon), contentDescription = it.name, modifier = Modifier.size(24.dp)) 
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Ingredients", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(meal.ingredients.joinToString(), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                Text("Nutrition Facts", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                NutritionFactItem(label = "Serving Size", value = meal.nutritionFacts?.servingSize)
                NutritionFactItem(label = "Calories", value = meal.nutritionFacts?.calories.toString())
                NutritionFactItem(label = "Total Fat", value = meal.nutritionFacts?.totalFat)
                NutritionFactItem(label = "Saturated Fat", value = meal.nutritionFacts?.saturatedFat)
                NutritionFactItem(label = "Trans Fat", value = meal.nutritionFacts?.transFat)
                NutritionFactItem(label = "Cholesterol", value = meal.nutritionFacts?.cholesterol)
                NutritionFactItem(label = "Sodium", value = meal.nutritionFacts?.sodium)
                NutritionFactItem(label = "Total Carbohydrate", value = meal.nutritionFacts?.totalCarbohydrate)
                NutritionFactItem(label = "Dietary Fiber", value = meal.nutritionFacts?.dietaryFiber)
                NutritionFactItem(label = "Total Sugars", value = meal.nutritionFacts?.totalSugars)
            }
        },
        confirmButton = {
            Button(onClick = { onAddMeal(meal) }) {
                Text("Add to Tracker")
            }
        }
    )
}

@Composable
fun NutritionFactItem(label: String, value: String?) {
    if (value != null) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label)
            Text(value)
        }
        Divider(modifier = Modifier.padding(vertical = 4.dp))
    }
}

private val MealInputSaver: Saver<MealInput, List<String>> = Saver(
    save = { state ->
        listOf(state.name, state.calories, state.protein, state.carbs, state.fat, state.fiber)
    },
    restore = {
        restored ->
            MealInput(
                name = restored.getOrNull(0) ?: "",
                calories = restored.getOrNull(1) ?: "",
                protein = restored.getOrNull(2) ?: "",
                carbs = restored.getOrNull(3) ?: "",
                fat = restored.getOrNull(4) ?: "",
                fiber = restored.getOrNull(5) ?: ""
            )
    }
)

private val NutritionGoalsSaver: Saver<NutritionGoals, List<Int>> = Saver(
    save = {
        listOf(it.calories, it.protein, it.carbs, it.fat, it.fiber)
    },
    restore = {
        NutritionGoals(
            calories = it[0],
            protein = it[1],
            carbs = it[2],
            fat = it[3],
            fiber = it[4]
        )
    }
)
