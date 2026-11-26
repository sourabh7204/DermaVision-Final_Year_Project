package com.example.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.local.ScanHistoryEntity
import com.example.data.model.ClassificationResponse
import com.example.ui.navigation.Screen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ImagePreviewScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DermaVisionViewModel
import com.example.ui.viewmodel.UiEvent
import com.example.util.AppThemeMode
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DermaVisionApp(
    viewModel: DermaVisionViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val totalScans by viewModel.scanCount.collectAsState()
    val allScans by viewModel.allScans.collectAsState()
    val selectedBitmap by viewModel.selectedBitmap.collectAsState()
    val qualityReport by viewModel.qualityReport.collectAsState()
    val activeResult by viewModel.activePredictionResult.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val isSavedToHistory by viewModel.isSavedToHistory.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val connectionType by viewModel.connectionType.collectAsState()
    val processingMode by viewModel.processingMode.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsState()

    // Determine Theme mode
    val isDark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // Gallery Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setImageFromUri(context, uri)
            navController.navigate(Screen.ImagePreview.route)
        }
    }

    var modelErrorDialogMessage by remember { mutableStateOf<String?>(null) }

    // Handle ViewModel Toast UI Events
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.NavigateTo -> {
                    navController.navigate(event.route)
                }
            }
        }
    }

    MyApplicationTheme(darkTheme = isDark) {
        if (modelErrorDialogMessage != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { modelErrorDialogMessage = null },
                title = {
                    Text(
                        text = "TFLite Model Notice",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = modelErrorDialogMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { modelErrorDialogMessage = null }
                    ) {
                        Text("Understood")
                    }
                }
            )
        }

        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.fillMaxSize()
        ) {
            // Splash Destination
            composable(Screen.Splash.route) {
                SplashScreen(
                    isLoggedIn = currentUser != null,
                    selectedLanguage = selectedLanguage,
                    onNavigateNext = { loggedIn ->
                        if (loggedIn) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            // Auth Destination
            composable(Screen.Auth.route) {
                AuthScreen(
                    selectedLanguage = selectedLanguage,
                    onGoogleSignIn = { callback ->
                        viewModel.signInWithGoogle(context, callback)
                    },
                    onSignIn = { email, pass, callback ->
                        viewModel.signIn(email, pass, callback)
                    },
                    onRegister = { name, email, pass, callback ->
                        viewModel.register(name, email, pass, callback)
                    },
                    onGuestLogin = { callback ->
                        viewModel.continueAsGuest(callback)
                    },
                    onResetPassword = { email, callback ->
                        viewModel.resetPassword(email, callback)
                    },
                    onAuthSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }

            // Dashboard Destination
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    user = currentUser,
                    allScans = allScans,
                    isModelAvailable = viewModel.isModelAvailable,
                    selectedLanguage = selectedLanguage,
                    isOnline = isOnline,
                    connectionType = connectionType,
                    processingMode = processingMode,
                    onSetProcessingMode = { mode -> viewModel.setProcessingMode(mode) },
                    notifications = notifications,
                    unreadNotificationCount = unreadNotificationCount,
                    onMarkNotificationRead = { notifId -> viewModel.markNotificationRead(notifId) },
                    onMarkAllNotificationsRead = { viewModel.markAllNotificationsRead() },
                    onClearNotifications = { viewModel.clearNotifications() },
                    onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                    onNavigateToGallery = { galleryLauncher.launch("image/*") },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToAnalytics = {
                        navController.navigate(Screen.Analytics.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSelectScan = { scanEntity ->
                        val result = scanEntity.toPredictionResult()
                        viewModel.setActivePredictionResult(result)
                        navController.navigate(Screen.Result.route)
                    }
                )
            }

            // Camera Destination
            composable(Screen.Camera.route) {
                CameraScreen(
                    onImageCaptured = { bitmap ->
                        viewModel.setImageBitmap(bitmap)
                        navController.navigate(Screen.ImagePreview.route)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Image Preview Destination
            composable(Screen.ImagePreview.route) {
                ImagePreviewScreen(
                    bitmap = selectedBitmap,
                    qualityReport = qualityReport,
                    isAnalyzing = isAnalyzing,
                    onStartAnalysis = {
                        viewModel.runSkinAnalysis { response ->
                            when (response) {
                                is ClassificationResponse.Success -> {
                                    navController.navigate(Screen.Result.route)
                                }
                                is ClassificationResponse.InvalidSkinImage -> {
                                    navController.navigate(Screen.Result.route)
                                }
                                is ClassificationResponse.ModelNotAvailable -> {
                                    modelErrorDialogMessage = "TFLite Model Missing:\n\n${response.message}\n\nPlease drag and drop 'DermaVision_V2_Final.tflite' into the 'app/src/main/assets/' folder in the AI Studio editor file tree."
                                }
                                is ClassificationResponse.Error -> {
                                    modelErrorDialogMessage = "Inference Notice:\n\n${response.errorMessage}"
                                }
                            }
                        }
                    },
                    onChangeImage = {
                        galleryLauncher.launch("image/*")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Result Destination
            composable(Screen.Result.route) {
                ResultScreen(
                    result = activeResult,
                    bitmap = selectedBitmap,
                    isSaved = isSavedToHistory,
                    selectedLanguage = selectedLanguage,
                    onSaveToHistory = { viewModel.saveActiveResultManually() },
                    onGeneratePdf = { viewModel.generateAndSharePdfReport(context) },
                    onShareText = { viewModel.shareAnalysisSummary(context) },
                    onScanAnother = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // History Destination
            composable(Screen.History.route) {
                HistoryScreen(
                    scans = allScans,
                    selectedLanguage = selectedLanguage,
                    onSelectScan = { scanEntity ->
                        val result = scanEntity.toPredictionResult()
                        viewModel.setActivePredictionResult(result)
                        navController.navigate(Screen.Result.route)
                    },
                    onDeleteScan = { scanEntity ->
                        viewModel.deleteScan(scanEntity)
                    },
                    onClearAll = {
                        viewModel.clearAllHistory()
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // Analytics Destination
            composable(Screen.Analytics.route) {
                AnalyticsScreen(
                    scans = allScans,
                    selectedLanguage = selectedLanguage,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // Profile Destination
            composable(Screen.Profile.route) {
                ProfileScreen(
                    user = currentUser,
                    totalScans = totalScans,
                    currentThemeMode = themeMode,
                    selectedLanguage = selectedLanguage,
                    processingMode = processingMode,
                    isOnline = isOnline,
                    connectionType = connectionType,
                    onSetProcessingMode = { mode -> viewModel.setProcessingMode(mode) },
                    onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                    onSetLanguage = { lang -> viewModel.setLanguage(lang) },
                    onUpdateProfilePicture = { uriString ->
                        viewModel.updateProfilePicture(uriString)
                    },
                    onUpdateProfile = { name, email ->
                        viewModel.updateProfile(name, email) {}
                    },
                    onDeleteAccount = {
                        viewModel.deleteAccount {
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    onClearAllHistory = {
                        viewModel.clearAllHistory()
                    },
                    onLogout = {
                        viewModel.logout()
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // Settings Destination
            composable(Screen.Settings.route) {
                SettingsScreen(
                    currentUser = currentUser,
                    currentThemeMode = themeMode,
                    selectedLanguage = selectedLanguage,
                    processingMode = processingMode,
                    isOnline = isOnline,
                    connectionType = connectionType,
                    onSetProcessingMode = { mode -> viewModel.setProcessingMode(mode) },
                    onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                    onSetLanguage = { lang -> viewModel.setLanguage(lang) },
                    onUpdateProfile = { name, email ->
                        viewModel.updateProfile(name, email) {}
                    },
                    onDeleteAccount = {
                        viewModel.deleteAccount {
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    onClearAllHistory = { viewModel.clearAllHistory() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
