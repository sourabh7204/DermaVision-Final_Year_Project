package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.auth.AuthResult
import com.example.data.auth.UserSession
import com.example.data.gemini.GeminiVisionService
import com.example.data.local.AppDatabase
import com.example.data.local.ScanHistoryEntity
import com.example.data.local.ScanHistoryRepository
import com.example.data.model.ClassificationResponse
import com.example.data.model.AppNotification
import com.example.data.model.ImageQualityReport
import com.example.data.model.NotificationType
import com.example.data.model.PredictionResult
import com.example.data.model.RiskLevel
import com.example.data.model.SkinDiseaseClass
import com.example.data.tflite.SkinDiseaseClassifier
import com.example.util.AppThemeMode
import com.example.util.ImageQualityChecker
import com.example.util.NetworkConnectivityMonitor
import com.example.util.NetworkConnectionType
import com.example.util.NotificationHelper
import com.example.util.PdfReportGenerator
import com.example.util.ProcessingMode
import com.example.util.ThemePreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.util.UUID

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class NavigateTo(val route: String) : UiEvent()
}

class DermaVisionViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo = AuthRepository(application)
    private val database = AppDatabase.getInstance(application)
    private val historyRepo = ScanHistoryRepository(database.scanHistoryDao(), application)
    private val classifier = SkinDiseaseClassifier(application)
    private val themePrefs = ThemePreferences(application)
    private val networkMonitor = NetworkConnectivityMonitor(application)

    // Network & Connectivity State for Hybrid Mode
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
    val connectionType: StateFlow<NetworkConnectionType> = networkMonitor.connectionType

    // AI Processing Mode State (Hybrid Auto / Offline Only / Cloud Preferred)
    val processingMode: StateFlow<ProcessingMode> = themePrefs.processingMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProcessingMode.HYBRID_AUTO
    )

    fun setProcessingMode(mode: ProcessingMode) {
        viewModelScope.launch {
            themePrefs.setProcessingMode(mode)
        }
    }

    // Auth State
    val currentUser: StateFlow<UserSession?> = authRepo.currentUser

    // Theme State
    val themeMode: StateFlow<AppThemeMode> = themePrefs.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppThemeMode.SYSTEM
    )

    // Language State
    val selectedLanguage: StateFlow<String> = themePrefs.selectedLanguage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "English"
    )

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            val normalized = if (lang.contains("Hindi", ignoreCase = true) || lang.contains("हिन्दी")) "Hindi" else "English"
            themePrefs.setLanguage(normalized)
        }
    }

    // History State
    val allScans: StateFlow<List<ScanHistoryEntity>> = historyRepo.allScans.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val scanCount: StateFlow<Int> = historyRepo.scanCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val latestScan: StateFlow<ScanHistoryEntity?> = historyRepo.latestScan.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Real-Time Notifications State
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    val unreadNotificationCount: StateFlow<Int> = _notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addNotification(
        title: String,
        message: String,
        type: NotificationType = NotificationType.SCAN_COMPLETED,
        diseaseClass: SkinDiseaseClass? = null,
        confidencePercentage: Int? = null,
        scanId: Long? = null
    ) {
        val notif = AppNotification(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            type = type,
            isRead = false,
            diseaseClass = diseaseClass,
            confidencePercentage = confidencePercentage,
            scanId = scanId
        )
        _notifications.value = listOf(notif) + _notifications.value
    }

    fun markNotificationRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    fun clearNotifications() {
        clearAllNotifications()
    }

    fun clearNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }

    // Active Scan / Analysis State
    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _qualityReport = MutableStateFlow<ImageQualityReport?>(null)
    val qualityReport: StateFlow<ImageQualityReport?> = _qualityReport.asStateFlow()

    private val _classificationResponse = MutableStateFlow<ClassificationResponse?>(null)
    val classificationResponse: StateFlow<ClassificationResponse?> = _classificationResponse.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _activePredictionResult = MutableStateFlow<PredictionResult?>(null)
    val activePredictionResult: StateFlow<PredictionResult?> = _activePredictionResult.asStateFlow()

    private val _isSavedToHistory = MutableStateFlow(false)
    val isSavedToHistory: StateFlow<Boolean> = _isSavedToHistory.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    // Model Availability Status
    val isModelAvailable: Boolean
        get() = classifier.isModelAvailable()

    // Auth Actions
    fun signInWithGoogle(activityContext: Context, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val res = authRepo.signInWithGoogle(activityContext)) {
                is AuthResult.Success -> onComplete(true, null)
                is AuthResult.Error -> onComplete(false, res.message)
                is AuthResult.Cancelled -> onComplete(false, null)
            }
        }
    }

    fun signIn(email: String, pass: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val res = authRepo.signIn(email, pass)) {
                is AuthResult.Success -> onComplete(true, null)
                is AuthResult.Error -> onComplete(false, res.message)
                is AuthResult.Cancelled -> onComplete(false, null)
            }
        }
    }

    fun register(name: String, email: String, pass: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val res = authRepo.register(name, email, pass)) {
                is AuthResult.Success -> onComplete(true, null)
                is AuthResult.Error -> onComplete(false, res.message)
                is AuthResult.Cancelled -> onComplete(false, null)
            }
        }
    }

    fun continueAsGuest(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepo.continueAsGuest()
            onComplete()
        }
    }

    fun resetPassword(email: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val res = authRepo.resetPassword(email)) {
                is AuthResult.Success -> onComplete(true, "Password reset instructions sent to $email")
                is AuthResult.Error -> onComplete(false, res.message)
                is AuthResult.Cancelled -> onComplete(false, "Operation cancelled")
            }
        }
    }

    fun logout() {
        authRepo.logout()
    }

    fun updateProfile(name: String, email: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            authRepo.updateProfile(name, email)
            onComplete(true)
        }
    }

    fun updateProfilePicture(uriString: String?, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            authRepo.updateProfilePicture(uriString)
            onComplete(true)
        }
    }

    fun deleteAccount(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            historyRepo.clearHistory()
            authRepo.deleteAccount()
            onComplete(true)
        }
    }

    // Image & Analysis Actions
    fun setImageBitmap(bitmap: Bitmap, uri: Uri? = null) {
        _selectedBitmap.value = bitmap
        _selectedImageUri.value = uri
        _classificationResponse.value = null
        _activePredictionResult.value = null
        _isSavedToHistory.value = false

        // Run quality check on the chosen image
        val report = ImageQualityChecker.analyze(bitmap)
        _qualityReport.value = report
    }

    fun setImageFromUri(context: Context, uri: Uri) {
        try {
            var rotationDegrees = 0
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = android.media.ExifInterface(stream)
                    val orientation = exif.getAttributeInt(
                        android.media.ExifInterface.TAG_ORIENTATION,
                        android.media.ExifInterface.ORIENTATION_NORMAL
                    )
                    rotationDegrees = when (orientation) {
                        android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                }
            } catch (e: Exception) {
                // Ignore EXIF read failure
            }

            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val decoded = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (decoded != null) {
                val finalBitmap = if (rotationDegrees != 0) {
                    val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                    Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
                } else {
                    decoded
                }
                setImageBitmap(finalBitmap, uri)
            }
        } catch (e: Exception) {
            viewModelScope.launch {
                _uiEvents.emit(UiEvent.ShowToast("Failed to load selected image: ${e.localizedMessage}"))
            }
        }
    }

    fun runSkinAnalysis(onComplete: (ClassificationResponse) -> Unit) {
        val bitmap = _selectedBitmap.value
        if (bitmap == null) {
            val err = ClassificationResponse.Error("No skin lesion image selected for analysis.")
            _classificationResponse.value = err
            onComplete(err)
            return
        }

        viewModelScope.launch {
            _isAnalyzing.value = true

            val currentMode = processingMode.value
            val online = isOnline.value
            var finalResponse: ClassificationResponse? = null

            when (currentMode) {
                ProcessingMode.OFFLINE_ONLY -> {
                    // Force pure 100% on-device local inference
                    finalResponse = classifier.classifyImage(bitmap)
                }
                ProcessingMode.HYBRID_AUTO -> {
                    // 1. If online and API key exists, attempt cloud multimodal vision
                    if (online && GeminiVisionService.isApiKeyAvailable()) {
                        try {
                            val cloudResult = GeminiVisionService.analyzeLesion(bitmap)
                            if (cloudResult != null) {
                                finalResponse = ClassificationResponse.Success(cloudResult)
                            }
                        } catch (e: Exception) {
                            // Silent offline fallback
                        }
                    }

                    // 2. Seamlessly fall back to On-Device TFLite Model with 5-View TTA
                    if (finalResponse == null) {
                        finalResponse = classifier.classifyImage(bitmap)
                    }
                }
                ProcessingMode.CLOUD_ENHANCED, ProcessingMode.CLOUD_PREFERRED -> {
                    // Try cloud AI first; fall back to on-device if network is unavailable
                    if (online && GeminiVisionService.isApiKeyAvailable()) {
                        try {
                            val cloudResult = GeminiVisionService.analyzeLesion(bitmap)
                            if (cloudResult != null) {
                                finalResponse = ClassificationResponse.Success(cloudResult)
                            }
                        } catch (e: Exception) {
                            // Fallback to local on-device
                        }
                    }

                    if (finalResponse == null) {
                        finalResponse = classifier.classifyImage(bitmap)
                        _uiEvents.emit(UiEvent.ShowToast("Cloud unavailable. Completed using On-Device Neural Model."))
                    }
                }
            }

            val resolvedResponse = finalResponse ?: classifier.classifyImage(bitmap)
            _classificationResponse.value = resolvedResponse
            _isAnalyzing.value = false

            if (resolvedResponse is ClassificationResponse.Success) {
                val res = resolvedResponse.result
                _activePredictionResult.value = res
                
                if (res.isInvalidSkinImage) {
                    addNotification(
                        title = "Non-Skin Image Detected",
                        message = res.invalidImageReason ?: "Please capture a clear, close-up photo of the skin lesion.",
                        type = NotificationType.QUALITY_ADVICE
                    )
                } else {
                    // Automatically save to local Room history
                    saveCurrentScanToHistory(res, bitmap)
                    
                    // Add real-time in-app notification
                    addNotification(
                        title = "Screening: ${res.primaryClass.displayName}",
                        message = "${res.confidencePercentage}% match • ${res.primaryClass.category.label}",
                        type = NotificationType.SCAN_COMPLETED,
                        diseaseClass = res.primaryClass,
                        confidencePercentage = res.confidencePercentage
                    )

                    if (res.primaryClass.riskLevel == RiskLevel.HIGH) {
                        addNotification(
                            title = "Clinical Alert: ${res.primaryClass.displayName}",
                            message = "High-risk lesion indicators detected. Dermatology consultation recommended.",
                            type = NotificationType.HIGH_RISK_ALERT,
                            diseaseClass = res.primaryClass,
                            confidencePercentage = res.confidencePercentage
                        )
                    }

                    // Trigger real system notification
                    try {
                        NotificationHelper.showAnalysisCompletedNotification(
                            context = getApplication(),
                            conditionName = res.primaryClass.displayName,
                            confidencePercent = res.confidencePercentage
                        )
                    } catch (e: Exception) {
                        // Ignore notification error
                    }
                }
            } else if (resolvedResponse is ClassificationResponse.InvalidSkinImage) {
                val invalidResult = PredictionResult.createInvalidSkinResult(
                    imagePath = null,
                    reason = resolvedResponse.message
                )
                _activePredictionResult.value = invalidResult
                addNotification(
                    title = "Non-Skin Image Detected",
                    message = resolvedResponse.message,
                    type = NotificationType.QUALITY_ADVICE
                )
            } else if (resolvedResponse is ClassificationResponse.Error) {
                _uiEvents.emit(UiEvent.ShowToast(resolvedResponse.errorMessage))
            } else if (resolvedResponse is ClassificationResponse.ModelNotAvailable) {
                _uiEvents.emit(UiEvent.ShowToast(resolvedResponse.message))
            }
            onComplete(resolvedResponse)
        }
    }

    fun setActivePredictionResult(result: PredictionResult, bitmap: Bitmap? = null) {
        _activePredictionResult.value = result
        _selectedBitmap.value = bitmap
        _isSavedToHistory.value = true
    }

    private fun saveCurrentScanToHistory(result: PredictionResult, bitmap: Bitmap) {
        val uid = currentUser.value?.uid ?: ""
        viewModelScope.launch {
            try {
                historyRepo.saveScan(result, bitmap, currentUid = uid)
                _isSavedToHistory.value = true
            } catch (e: Exception) {
                // Ignore history save error
            }
        }
    }

    fun saveActiveResultManually() {
        val result = _activePredictionResult.value ?: return
        val bitmap = _selectedBitmap.value
        val uid = currentUser.value?.uid ?: ""
        viewModelScope.launch {
            try {
                historyRepo.saveScan(result, bitmap, currentUid = uid)
                _isSavedToHistory.value = true
                _uiEvents.emit(UiEvent.ShowToast("Saved scan result to history!"))
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowToast("Failed to save: ${e.localizedMessage}"))
            }
        }
    }

    fun deleteScan(scan: ScanHistoryEntity) {
        val uid = currentUser.value?.uid ?: ""
        viewModelScope.launch {
            historyRepo.deleteScan(scan, currentUid = uid)
            _uiEvents.emit(UiEvent.ShowToast("Scan deleted from history."))
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepo.clearHistory()
            _uiEvents.emit(UiEvent.ShowToast("All scan history cleared."))
        }
    }

    fun generateAndSharePdfReport(context: Context) {
        val result = _activePredictionResult.value
        if (result == null) {
            viewModelScope.launch {
                _uiEvents.emit(UiEvent.ShowToast("No prediction result to generate PDF."))
            }
            return
        }

        viewModelScope.launch {
            try {
                val userName = currentUser.value?.displayName ?: "Self Examination"
                val pdfFile: File = PdfReportGenerator.generateReport(
                    context = context,
                    result = result,
                    patientName = userName,
                    bitmap = _selectedBitmap.value
                )
                addNotification(
                    title = "PDF Report Generated",
                    message = "Clinical summary for ${result.primaryClass.displayName} ready to view and share.",
                    type = NotificationType.REPORT_GENERATED,
                    diseaseClass = result.primaryClass,
                    confidencePercentage = result.confidencePercentage
                )
                try {
                    NotificationHelper.showReportGeneratedNotification(
                        context = context,
                        conditionName = result.primaryClass.displayName,
                        filePath = pdfFile.absolutePath
                    )
                } catch (e: Exception) {
                    // Ignore notification error
                }
                PdfReportGenerator.sharePdf(context, pdfFile)
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowToast("Failed to generate PDF report: ${e.localizedMessage}"))
            }
        }
    }

    fun shareAnalysisSummary(context: Context) {
        val result = _activePredictionResult.value
        if (result == null) {
            viewModelScope.launch {
                _uiEvents.emit(UiEvent.ShowToast("No prediction result to share."))
            }
            return
        }

        try {
            val userName = currentUser.value?.displayName ?: "Patient / Self Examination"
            val summaryText = buildString {
                appendLine("🩺 DermaVision Skin Analysis Report")
                appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                appendLine("Subject: $userName")
                appendLine("Primary Finding: ${result.primaryClass.displayName}")
                appendLine("Confidence Match: ${result.confidencePercentage}%")
                if (result.isLowConfidence) {
                    appendLine("⚠️ Low Confidence Note: ${result.lowConfidenceMessage}")
                }
                appendLine("Risk Assessment: ${result.primaryClass.riskLevel.label}")
                appendLine("Clinical Severity: ${result.severityLevel.label}")
                appendLine("Category: ${result.primaryClass.category.label}")
                appendLine()
                if (result.topPredictions.isNotEmpty()) {
                    appendLine("📊 Differential Analysis:")
                    result.topPredictions.forEachIndexed { idx, p ->
                        appendLine(" ${idx + 1}. ${p.diseaseClass.displayName} (${p.percentage}%)")
                    }
                    appendLine()
                }
                appendLine("📋 Key Clinical Advice:")
                appendLine("• ${result.primaryClass.educationalAdvice}")
                appendLine("• When to see a doctor: ${result.primaryClass.whenToSeeDoctor}")
                appendLine()
                appendLine("⚠️ Medical Disclaimer: DermaVision is a clinical screening assessment and is not a definitive medical diagnosis. Please consult a qualified dermatologist or physician for proper evaluation.")
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "DermaVision Analysis - ${result.primaryClass.displayName}")
                putExtra(Intent.EXTRA_TEXT, summaryText)
            }
            val chooser = Intent.createChooser(shareIntent, "Share Analysis Report")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            viewModelScope.launch {
                _uiEvents.emit(UiEvent.ShowToast("Failed to share report: ${e.localizedMessage}"))
            }
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            themePrefs.setThemeMode(mode)
        }
    }

    override fun onCleared() {
        super.onCleared()
        classifier.close()
    }
}
