package com.example.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "dermavision_settings")

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class ProcessingMode(val label: String, val description: String) {
    HYBRID_AUTO("Hybrid Auto", "Cloud AI when online with automatic 100% offline fallback"),
    OFFLINE_ONLY("Offline Only", "Pure on-device neural inference without internet access"),
    CLOUD_PREFERRED("Cloud Preferred", "High-accuracy cloud multimodal dermatology vision AI"),
    CLOUD_ENHANCED("Cloud Preferred", "High-accuracy cloud multimodal dermatology vision AI")
}

class ThemePreferences(private val context: Context) {

    private val themeModeKey = stringPreferencesKey("app_theme_mode")
    private val disclaimerAcknowledgedKey = booleanPreferencesKey("disclaimer_acknowledged")
    private val languageKey = stringPreferencesKey("app_language")
    private val processingModeKey = stringPreferencesKey("app_processing_mode")

    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[themeModeKey]) {
            AppThemeMode.LIGHT.name -> AppThemeMode.LIGHT
            AppThemeMode.DARK.name -> AppThemeMode.DARK
            else -> AppThemeMode.SYSTEM
        }
    }

    val processingMode: Flow<ProcessingMode> = context.dataStore.data.map { prefs ->
        when (prefs[processingModeKey]) {
            ProcessingMode.OFFLINE_ONLY.name -> ProcessingMode.OFFLINE_ONLY
            ProcessingMode.CLOUD_ENHANCED.name -> ProcessingMode.CLOUD_ENHANCED
            else -> ProcessingMode.HYBRID_AUTO
        }
    }

    val selectedLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        val lang = prefs[languageKey] ?: "English"
        if (lang == "Hindi" || lang == "हिन्दी") "Hindi" else "English"
    }

    val isDisclaimerAcknowledged: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[disclaimerAcknowledgedKey] ?: false
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }

    suspend fun setProcessingMode(mode: ProcessingMode) {
        context.dataStore.edit { prefs ->
            prefs[processingModeKey] = mode.name
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[languageKey] = if (language.contains("Hindi", ignoreCase = true) || language.contains("हिन्दी")) "Hindi" else "English"
        }
    }

    suspend fun setDisclaimerAcknowledged(acknowledged: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[disclaimerAcknowledgedKey] = acknowledged
        }
    }
}
