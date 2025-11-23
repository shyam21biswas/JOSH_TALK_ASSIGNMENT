package com.example.josh


import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext



//help to route ..................
sealed class Screen(val route: String) {
    object Start : Screen("start")
    object NoiseTest : Screen("noise_test")
    object TaskSelection : Screen("task_selection")
    object TextReading : Screen("text_reading")
    object ImageDescription : Screen("image_description")

    object PhotoCapture : Screen("photo_capture")
    object TaskHistory : Screen("task_history")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Initialize dependencies manually
    val database = AppDatabase.getDatabase(context)
    val taskDao = database.taskDao()
    val apiService = RetrofitInstance.apiService
    val repository = TaskRepository(taskDao, apiService)
    val viewModelFactory = TaskViewModelFactory(repository)
    val viewModel: TaskViewModel = viewModel(factory = viewModelFactory)

    NavHost(
        navController = navController,
        startDestination = Screen.Start.route
    ) {
        composable(Screen.Start.route) {
            StartScreen(
                onStartTask = { navController.navigate(Screen.NoiseTest.route) }
            )
        }

        composable(Screen.NoiseTest.route) {
            NoiseTestScreen(
                onTestPassed = { navController.navigate(Screen.TaskSelection.route) }
            )
        }

        composable(Screen.TaskSelection.route) {
            TaskSelectionScreen(
                onTextReadingClick = { navController.navigate(Screen.TextReading.route) },
                onImageDescriptionClick = { navController.navigate(Screen.ImageDescription.route) },
                onPhotoCaptureClick = { navController.navigate(Screen.PhotoCapture.route) },

                onViewHistory = { navController.navigate(Screen.TaskHistory.route) }
            )
        }

        composable(Screen.TextReading.route) {
            TextReadingScreen(
                onTaskComplete = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(Screen.ImageDescription.route) {
            ImageDescriptionScreen(
                onTaskComplete = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(Screen.PhotoCapture.route) {
            PhotoCaptureScreen(
                onTaskComplete = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(Screen.TaskHistory.route) {

            TaskHistoryScreen(
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )

        }
    }
}
