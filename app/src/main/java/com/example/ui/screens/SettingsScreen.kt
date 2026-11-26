package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.auth.UserSession
import com.example.util.AppThemeMode
import com.example.util.LocalizedStrings
import com.example.util.NetworkConnectionType
import com.example.util.ProcessingMode

private val SettingIconBlueBg = Color(0xFFE8F1FF)
private val SettingIconBlueTint = Color(0xFF007BFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: UserSession?,
    currentThemeMode: AppThemeMode,
    selectedLanguage: String,
    processingMode: ProcessingMode = ProcessingMode.HYBRID_AUTO,
    isOnline: Boolean = true,
    connectionType: NetworkConnectionType = NetworkConnectionType.WIFI,
    onSetProcessingMode: (ProcessingMode) -> Unit = {},
    onSetThemeMode: (AppThemeMode) -> Unit,
    onSetLanguage: (String) -> Unit,
    onUpdateProfile: (String, String) -> Unit,
    onDeleteAccount: () -> Unit,
    onClearAllHistory: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val strings = remember(selectedLanguage) { LocalizedStrings.get(selectedLanguage) }

    // Dialog state controllers
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showProcessingModeDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showHelpCenterDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showRateUsDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("button_settings_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {

            // 1. GENERAL SECTION
            SettingsCategoryHeader(title = "General")

            SettingsRowItem(
                icon = Icons.Default.Language,
                title = "Language",
                subtitle = selectedLanguage,
                showChevron = true,
                testTag = "setting_item_language",
                onClick = { showLanguageDialog = true }
            )

            SettingsRowItem(
                icon = Icons.Default.Palette,
                title = "Theme",
                subtitle = when (currentThemeMode) {
                    AppThemeMode.LIGHT -> "Light"
                    AppThemeMode.DARK -> "Dark"
                    AppThemeMode.SYSTEM -> "System Default"
                },
                showChevron = true,
                testTag = "setting_item_theme",
                onClick = { showThemeDialog = true }
            )

            SettingsRowItem(
                icon = Icons.Default.CloudSync,
                title = strings.hybridProcessingSection,
                subtitle = when (processingMode) {
                    ProcessingMode.HYBRID_AUTO -> "${strings.hybridAutoTitle} (${if (isOnline) strings.onlineStatus else strings.offlineStatus})"
                    ProcessingMode.OFFLINE_ONLY -> "${strings.offlineOnlyTitle} (100% On-Device)"
                    ProcessingMode.CLOUD_PREFERRED, ProcessingMode.CLOUD_ENHANCED -> "${strings.cloudPreferredTitle} (${if (isOnline) "Cloud AI" else "Offline Fallback"})"
                },
                showChevron = true,
                testTag = "setting_item_processing_mode",
                onClick = { showProcessingModeDialog = true }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2. ACCOUNT SECTION
            SettingsCategoryHeader(title = "Account")

            SettingsRowItem(
                icon = Icons.Default.Person,
                title = "Edit Profile",
                subtitle = "Update your information",
                showChevron = false,
                testTag = "setting_item_edit_profile",
                onClick = { showEditProfileDialog = true }
            )

            SettingsRowItem(
                icon = Icons.Default.Delete,
                title = "Delete Account",
                subtitle = "Permanently delete your account",
                showChevron = false,
                testTag = "setting_item_delete_account",
                onClick = { showDeleteAccountDialog = true }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3. SUPPORT SECTION
            SettingsCategoryHeader(title = "Support")

            SettingsRowItem(
                icon = Icons.Default.Help,
                title = "Help Center",
                subtitle = "Get help and support",
                showChevron = false,
                testTag = "setting_item_help_center",
                onClick = { showHelpCenterDialog = true }
            )

            SettingsRowItem(
                icon = Icons.Default.Message,
                title = "Send Feedback",
                subtitle = "Help us improve",
                showChevron = false,
                testTag = "setting_item_send_feedback",
                onClick = { showFeedbackDialog = true }
            )

            SettingsRowItem(
                icon = Icons.Default.Star,
                title = "Rate Us",
                subtitle = "Rate us on the app store",
                showChevron = false,
                testTag = "setting_item_rate_us",
                onClick = { showRateUsDialog = true }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 4. LEGAL SECTION
            SettingsCategoryHeader(title = "Legal")

            SettingsRowItem(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy Policy",
                subtitle = "Read our privacy policy",
                showChevron = false,
                testTag = "setting_item_privacy_policy",
                onClick = { showPrivacyPolicyDialog = true }
            )

            SettingsRowItem(
                icon = Icons.Default.Description,
                title = "Terms of Service",
                subtitle = "Read our terms",
                showChevron = false,
                testTag = "setting_item_terms_of_service",
                onClick = { showTermsDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Additional Local History Option
            OutlinedButton(
                onClick = { showClearHistoryDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("button_settings_clear_history"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear On-Device Scan Records", fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // -------------------------------------------------------------
    // DIALOG IMPLEMENTATIONS
    // -------------------------------------------------------------

    // A. Language Dialog
    if (showLanguageDialog) {
        val languages = listOf("English", "हिन्दी (Hindi)")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(strings.selectLanguageTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    languages.forEach { lang ->
                        val isSelected = (lang == "English" && selectedLanguage == "English") ||
                                ((lang.contains("Hindi") || lang.contains("हिन्दी")) && (selectedLanguage == "Hindi" || selectedLanguage == "हिन्दी"))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onSetLanguage(if (lang.contains("Hindi") || lang.contains("हिन्दी")) "Hindi" else "English")
                                    showLanguageDialog = false
                                    Toast.makeText(context, "Language set to $lang", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = lang,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(strings.closeButton)
                }
            }
        )
    }

    // B. Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ThemeOptionItem(
                        title = "System Default",
                        description = "Sync with Android device setting",
                        icon = Icons.Default.BrightnessAuto,
                        isSelected = currentThemeMode == AppThemeMode.SYSTEM,
                        onClick = {
                            onSetThemeMode(AppThemeMode.SYSTEM)
                            showThemeDialog = false
                        }
                    )
                    ThemeOptionItem(
                        title = "Light Mode",
                        description = "High-contrast clean light theme",
                        icon = Icons.Default.LightMode,
                        isSelected = currentThemeMode == AppThemeMode.LIGHT,
                        onClick = {
                            onSetThemeMode(AppThemeMode.LIGHT)
                            showThemeDialog = false
                        }
                    )
                    ThemeOptionItem(
                        title = "Dark Mode",
                        description = "OLED dark theme for low light",
                        icon = Icons.Default.DarkMode,
                        isSelected = currentThemeMode == AppThemeMode.DARK,
                        onClick = {
                            onSetThemeMode(AppThemeMode.DARK)
                            showThemeDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // AI & Hybrid Processing Mode Dialog
    if (showProcessingModeDialog) {
        AlertDialog(
            onDismissRequest = { showProcessingModeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(strings.hybridProcessingSection, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOnline) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                        border = BorderStroke(1.dp, if (isOnline) Color(0xFF81C784) else Color(0xFFFFB74D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isOnline) {
                                    if (connectionType == NetworkConnectionType.WIFI) Icons.Default.Wifi else Icons.Default.Sensors
                                } else {
                                    Icons.Default.WifiOff
                                },
                                contentDescription = null,
                                tint = if (isOnline) Color(0xFF2E7D32) else Color(0xFFEF6C00),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isOnline) "${strings.onlineStatus} • High-speed cloud analysis available" else "${strings.offlineStatus} • 100% on-device neural inference enabled",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
                                color = if (isOnline) Color(0xFF1B5E20) else Color(0xFFE65100)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    ThemeOptionItem(
                        title = strings.hybridAutoTitle,
                        description = strings.hybridAutoDesc,
                        icon = Icons.Default.CloudSync,
                        isSelected = processingMode == ProcessingMode.HYBRID_AUTO,
                        onClick = {
                            onSetProcessingMode(ProcessingMode.HYBRID_AUTO)
                            showProcessingModeDialog = false
                            Toast.makeText(context, "Set to Hybrid Auto Mode", Toast.LENGTH_SHORT).show()
                        }
                    )

                    ThemeOptionItem(
                        title = strings.offlineOnlyTitle,
                        description = strings.offlineOnlyDesc,
                        icon = Icons.Default.OfflineBolt,
                        isSelected = processingMode == ProcessingMode.OFFLINE_ONLY,
                        onClick = {
                            onSetProcessingMode(ProcessingMode.OFFLINE_ONLY)
                            showProcessingModeDialog = false
                            Toast.makeText(context, "Set to Offline Only Mode", Toast.LENGTH_SHORT).show()
                        }
                    )

                    ThemeOptionItem(
                        title = strings.cloudPreferredTitle,
                        description = strings.cloudPreferredDesc,
                        icon = Icons.Default.Dns,
                        isSelected = processingMode == ProcessingMode.CLOUD_PREFERRED || processingMode == ProcessingMode.CLOUD_ENHANCED,
                        onClick = {
                            onSetProcessingMode(ProcessingMode.CLOUD_PREFERRED)
                            showProcessingModeDialog = false
                            Toast.makeText(context, "Set to Cloud Enhanced Mode", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showProcessingModeDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // C. Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(currentUser?.displayName ?: "") }
        var editEmail by remember { mutableStateOf(currentUser?.email ?: "") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank() && editEmail.isNotBlank()) {
                            onUpdateProfile(editName.trim(), editEmail.trim())
                            showEditProfileDialog = false
                            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Fields cannot be blank", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // D. Delete Account Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to permanently delete your account and all associated local scan records? This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccount()
                        Toast.makeText(context, "Account and data deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // E. Help Center Dialog
    if (showHelpCenterDialog) {
        AlertDialog(
            onDismissRequest = { showHelpCenterDialog = false },
            title = { Text("Help Center", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HelpTopicCard(
                        topic = "📸 Best Photo Quality",
                        details = "Ensure high ambient lighting, steady focus from 10-15cm distance, and avoid heavy shadows or glares over the skin lesion."
                    )
                    HelpTopicCard(
                        topic = "🔍 Understanding Assessment",
                        details = "The screening score represents statistical likeness across 10 common dermatology classes. High confidence highlights prominent patterns."
                    )
                    HelpTopicCard(
                        topic = "📄 Exporting PDF Reports",
                        details = "Tap 'Download PDF Report' from any analysis result to save a shareable clinical document with embedded images and timestamps."
                    )
                    HelpTopicCard(
                        topic = "✉️ Contact Support",
                        details = "Need developer assistance or feature questions? Reach out to support@dermavision.ai"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpCenterDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }

    // F. Send Feedback Dialog
    if (showFeedbackDialog) {
        var feedbackRating by remember { mutableIntStateOf(5) }
        var feedbackText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text("Send Feedback", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("How would you rate your experience?", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in 1..5) {
                            IconButton(onClick = { feedbackRating = i }) {
                                Icon(
                                    imageVector = if (i <= feedbackRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$i stars",
                                    tint = if (i <= feedbackRating) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        placeholder = { Text("Share thoughts, bugs, or feature ideas...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFeedbackDialog = false
                        Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // G. Rate Us Dialog
    if (showRateUsDialog) {
        var ratingVal by remember { mutableIntStateOf(5) }
        AlertDialog(
            onDismissRequest = { showRateUsDialog = false },
            title = { Text("Rate DermaVision", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Your review helps us enhance dermatology screening tools!", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.Center) {
                        for (i in 1..5) {
                            IconButton(onClick = { ratingVal = i }) {
                                Icon(
                                    imageVector = if (i <= ratingVal) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$i Stars",
                                    tint = if (i <= ratingVal) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRateUsDialog = false
                        Toast.makeText(context, "Thank you for rating us $ratingVal stars!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Submit Rating")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRateUsDialog = false }) {
                    Text("Maybe Later")
                }
            }
        )
    }

    // H. Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "1. Zero Remote Image Collection",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "DermaVision performs skin lesion classification locally on your device via TensorFlow Lite. Your photos and scans are never shared, sold, or transmitted to third-party ad networks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "2. Local SQLite Persistence",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Historical records, quality parameters, and timestamps are stored exclusively in your local Room Database and can be deleted anytime using the 'Clear On-Device Scan Records' button.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // I. Terms of Service Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms of Service", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "1. Educational & Screening Scope",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "DermaVision provides automated image analysis intended strictly for informational, educational, and pre-clinical triage guidance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "2. Not a Medical Diagnosis",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "The application does not constitute medical advice or a definitive diagnosis. Always seek the advice of a board-certified dermatologist or qualified healthcare provider with any questions regarding a skin condition.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Accept & Close")
                }
            }
        )
    }

    // J. Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear All History") },
            text = { Text("Are you sure you want to delete all saved scan history? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllHistory()
                        showClearHistoryDialog = false
                        Toast.makeText(context, "Scan history cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// REUSABLE COMPONENTS
// -------------------------------------------------------------

@Composable
private fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        ),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    showChevron: Boolean = false,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Light Blue Circular Icon Container (Exact match to uploaded image)
        Surface(
            shape = CircleShape,
            color = SettingIconBlueBg,
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SettingIconBlueTint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title and Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
            )
        }

        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ThemeOptionItem(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun HelpTopicCard(topic: String, details: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = topic,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}
