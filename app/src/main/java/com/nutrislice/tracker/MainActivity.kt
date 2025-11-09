package com.nutrislice.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.nutrislice.tracker.ui.NutritionRoute
import com.nutrislice.tracker.ui.theme.NutrisliceTheme

class MainActivity : ComponentActivity() {

    private val viewModel: NutritionViewModel by viewModels {
        val app = application as NutritionTrackerApp
        NutritionViewModelFactory(app.nutritionRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NutrisliceTheme {
                NutritionRoute(viewModel = viewModel)
            }
        }
    }
}
