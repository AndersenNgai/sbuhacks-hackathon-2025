package com.nutrislice.tracker

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.nutrislice.tracker.data.DataStoreNutritionRepository
import com.nutrislice.tracker.data.NutritionRepository

private const val DATASTORE_NAME = "nutrition_tracker"

val Context.nutritionDataStore by preferencesDataStore(name = DATASTORE_NAME)

class NutritionTrackerApp : Application() {
    val nutritionRepository: NutritionRepository by lazy {
        DataStoreNutritionRepository(nutritionDataStore)
    }
}
