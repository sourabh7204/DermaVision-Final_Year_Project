package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Auth : Screen("auth")
    data object Dashboard : Screen("dashboard")
    data object Camera : Screen("camera")
    data object ImagePreview : Screen("image_preview")
    data object Result : Screen("result")
    data object History : Screen("history")
    data object Analytics : Screen("analytics")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
}
