package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.auth.UserSession
import com.example.data.local.ScanHistoryEntity
import com.example.data.model.AppNotification
import com.example.data.model.NotificationType
import com.example.data.model.RiskLevel
import com.example.data.model.SkinDiseaseClass
import com.example.ui.components.DermaVisionBottomBar
import com.example.ui.components.SkinConditionThumbnail
import com.example.ui.navigation.Screen
import com.example.util.LocalizedStrings
import com.example.util.NetworkConnectionType
import com.example.util.ProcessingMode
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class SampleScreeningItem(
    val id: String,
    val name: String,
    val matchPercentage: Int,
    val timestamp: String,
    val color1: Color,
    val color2: Color,
    val description: String,
    val diseaseClass: SkinDiseaseClass = SkinDiseaseClass.PSORIASIS_LICHEN_PLANUS
)

data class SkinHealthTipItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color,
    val iconBgColor: Color,
    val detailedAdvice: String
)

data class SkinConditionChip(
    val name: String,
    val color: Color,
    val diseaseClass: SkinDiseaseClass?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: UserSession?,
    allScans: List<ScanHistoryEntity>,
    isModelAvailable: Boolean,
    isOnline: Boolean = true,
    connectionType: NetworkConnectionType = NetworkConnectionType.WIFI,
    processingMode: ProcessingMode = ProcessingMode.HYBRID_AUTO,
    onSetProcessingMode: (ProcessingMode) -> Unit = {},
    notifications: List<AppNotification> = emptyList(),
    unreadNotificationCount: Int = 0,
    onMarkNotificationRead: (String) -> Unit = {},
    onMarkAllNotificationsRead: () -> Unit = {},
    onClearNotifications: () -> Unit = {},
    onNavigateToCamera: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSelectScan: (ScanHistoryEntity) -> Unit,
    selectedLanguage: String = "English"
) {
    val scrollState = rememberScrollState()
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var selectedConditionForModal by remember { mutableStateOf<SkinDiseaseClass?>(null) }
    var selectedTipForModal by remember { mutableStateOf<SkinHealthTipItem?>(null) }
    var showSkinGuidesDialog by remember { mutableStateOf(false) }
    val strings = remember(selectedLanguage) { LocalizedStrings.get(selectedLanguage) }

    // Dynamic greeting based on time of day
    val greetingText = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    // User first name
    val userName = remember(user?.displayName) {
        val name = user?.displayName?.trim()
        if (!name.isNullOrBlank()) {
            name.split(" ").firstOrNull() ?: "Sourabh"
        } else {
            "Sourabh"
        }
    }

    // Initials for avatar fallback
    val initials = remember(user?.displayName) {
        val displayName = user?.displayName?.ifBlank { "Sourabh" } ?: "Sourabh"
        val parts = displayName.trim().split(" ")
        if (parts.size >= 2) {
            "${parts[0].firstOrNull()?.uppercase() ?: ""}${parts[1].firstOrNull()?.uppercase() ?: ""}"
        } else {
            displayName.take(2).uppercase()
        }
    }

    // Sample screenings to display if scan history is empty, matching the reference image exactly
    val defaultSampleScreenings = remember {
        listOf(
            SampleScreeningItem(
                id = "sample_1",
                name = "Psoriasis",
                matchPercentage = 92,
                timestamp = "25 Aug 2025 • 10:30 AM",
                color1 = Color(0xFFEF9A9A),
                color2 = Color(0xFFC62828),
                description = "Thick, erythematous scaly plaques typical of plaque psoriasis.",
                diseaseClass = SkinDiseaseClass.PSORIASIS_LICHEN_PLANUS
            ),
            SampleScreeningItem(
                id = "sample_2",
                name = "Lichen Planus",
                matchPercentage = 78,
                timestamp = "24 Aug 2025 • 09:15 AM",
                color1 = Color(0xFFCE93D8),
                color2 = Color(0xFFAD1457),
                description = "Violaceous polygonal flat-topped papules with fine scale.",
                diseaseClass = SkinDiseaseClass.PSORIASIS_LICHEN_PLANUS
            ),
            SampleScreeningItem(
                id = "sample_3",
                name = "Atopic Dermatitis",
                matchPercentage = 35,
                timestamp = "23 Aug 2025 • 08:20 AM",
                color1 = Color(0xFFFFCC80),
                color2 = Color(0xFFD84315),
                description = "Pruritic erythematous patches with slight excoriation.",
                diseaseClass = SkinDiseaseClass.ATOPIC_DERMATITIS
            )
        )
    }

    // Health Tips List
    val healthTips = remember {
        listOf(
            SkinHealthTipItem(
                title = "Sun Protection",
                subtitle = "Use sunscreen daily to protect your skin.",
                icon = Icons.Default.WbSunny,
                iconColor = Color(0xFFFFA000),
                iconBgColor = Color(0xFFFFF8E1),
                detailedAdvice = "Apply broad-spectrum SPF 30+ sunscreen 15 minutes before going outdoors. Reapply every 2 hours and after swimming or sweating to protect against UVA and UVB damage."
            ),
            SkinHealthTipItem(
                title = "Stay Hydrated",
                subtitle = "Drink plenty of water to keep your skin healthy.",
                icon = Icons.Default.WaterDrop,
                iconColor = Color(0xFF0288D1),
                iconBgColor = Color(0xFFE1F5FE),
                detailedAdvice = "Drinking 2 to 3 liters of water daily supports skin elasticity, flushes toxins, maintains the skin's lipid moisture barrier, and prevents dry flakiness."
            ),
            SkinHealthTipItem(
                title = "Gentle Care",
                subtitle = "Use mild cleansers and avoid harsh chemicals.",
                icon = Icons.Default.Spa,
                iconColor = Color(0xFF388E3C),
                iconBgColor = Color(0xFFE8F5E9),
                detailedAdvice = "Wash with lukewarm water and soap-free syndet cleansers. Avoid scrubs with abrasive micro-beads and products containing synthetic fragrance or denatured alcohols."
            ),
            SkinHealthTipItem(
                title = "Know Your Skin",
                subtitle = "Learn about conditions and spot early signs.",
                icon = Icons.Default.Search,
                iconColor = Color(0xFF7B1FA2),
                iconBgColor = Color(0xFFF3E5F5),
                detailedAdvice = "Perform monthly skin self-exams using the ABCDE method (Asymmetry, Border, Color, Diameter, Evolving) to detect unusual spots or changing lesions early."
            )
        )
    }

    // Conditions List for "Learn About Skin Conditions"
    val conditionChips = remember {
        listOf(
            SkinConditionChip("Psoriasis", Color(0xFFE57373), SkinDiseaseClass.PSORIASIS_LICHEN_PLANUS),
            SkinConditionChip("Eczema", Color(0xFFFF8A65), SkinDiseaseClass.ECZEMA),
            SkinConditionChip("Acne", Color(0xFFFFB74D), SkinDiseaseClass.BENIGN_KERATOSIS),
            SkinConditionChip("Melanoma", Color(0xFF5D4037), SkinDiseaseClass.MELANOMA),
            SkinConditionChip("Lichen Planus", Color(0xFFBA68C8), SkinDiseaseClass.PSORIASIS_LICHEN_PLANUS),
            SkinConditionChip("Atopic Dermatitis", Color(0xFF4DB6AC), SkinDiseaseClass.ATOPIC_DERMATITIS),
            SkinConditionChip("Basal Cell", Color(0xFF7986CB), SkinDiseaseClass.BASAL_CELL_CARCINOMA),
            SkinConditionChip("Viral Warts", Color(0xFF81C784), SkinDiseaseClass.WARTS_VIRAL)
        )
    }

    Scaffold(
        bottomBar = {
            DermaVisionBottomBar(
                currentRoute = Screen.Dashboard.route,
                onNavigateToRoute = { route ->
                    when (route) {
                        Screen.Dashboard.route -> { /* Already on dashboard */ }
                        Screen.History.route -> onNavigateToHistory()
                        Screen.Camera.route -> onNavigateToCamera()
                        Screen.Analytics.route -> onNavigateToAnalytics()
                        Screen.Profile.route -> onNavigateToProfile()
                    }
                },
                selectedLanguage = selectedLanguage
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // ==========================================
            // 1. TOP APP BAR (App Name, Logo, Bell & Profile)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Logo and Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // DermaVision Logo Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.2.dp, Color(0xFF009688).copy(alpha = 0.5f)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF008F8F),
                                            Color(0xFF004D4D)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = "DermaVision Logo",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Derma",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Vision",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = Color(0xFF00897B)
                            )
                        }
                        Text(
                            text = "Advanced Skin Analysis",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        )
                    }
                }

                // Right Actions: Notification Bell + Profile Avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Notification Bell with Real-time Unread Badge
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable {
                                showNotificationsDialog = true
                                onMarkAllNotificationsRead()
                            }
                            .testTag("button_notifications"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (unreadNotificationCount > 0) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = if (unreadNotificationCount > 0) Color(0xFF00897B) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        // Small Red Dot indicator for unviewed notifications
                        if (unreadNotificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-2).dp, y = 2.dp)
                                    .size(10.dp)
                                    .background(Color(0xFFE53935), CircleShape)
                                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            )
                        }
                    }

                    // User Profile DP Avatar
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2F1))
                            .border(1.5.dp, Color(0xFF00897B).copy(alpha = 0.5f), CircleShape)
                            .clickable { onNavigateToProfile() }
                            .testTag("nav_top_profile"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!user?.profilePictureUri.isNullOrBlank()) {
                            AsyncImage(
                                model = user?.profilePictureUri,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = Color(0xFF00695C)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 2. GREETING HEADER
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$greetingText, $userName 👋",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Let's take care of your skin health today.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // 3. "ANALYZE YOUR SKIN" HERO CARD
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_hero_analyze"),
                    shape = RoundedCornerShape(26.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A3C3E))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF0C4648),
                                        Color(0xFF083335),
                                        Color(0xFF042426)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            // Top Row: Left Content + Right Stylized Scanner Illustration
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1.3f)) {
                                    // Start New Analysis Badge Pill
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF145356),
                                        border = BorderStroke(1.dp, Color(0xFF1E6B6F))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color(0xFF64FFDA),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "START NEW ANALYSIS",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    letterSpacing = 0.4.sp
                                                ),
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Analyze Your Skin",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        ),
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "Capture or upload a clear image to get dermatological insights.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.5.sp,
                                            lineHeight = 17.sp
                                        ),
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Right Side Decorative Visual: Scanner Phone & Lens Graphic
                                Box(
                                    modifier = Modifier
                                        .size(width = 110.dp, height = 115.dp)
                                        .align(Alignment.CenterVertically),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Phone Frame
                                    Surface(
                                        modifier = Modifier
                                            .size(width = 72.dp, height = 100.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF102A2B),
                                        border = BorderStroke(1.5.dp, Color(0xFF26A69A).copy(alpha = 0.6f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color(0xFF0F3B3D), Color(0xFF071F20))
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Skin Lesion preview circle inside scanner phone
                                            Surface(
                                                modifier = Modifier.size(48.dp),
                                                shape = CircleShape,
                                                color = Color(0xFF145356),
                                                border = BorderStroke(2.dp, Color(0xFF26A69A))
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    SkinConditionThumbnail(
                                                        diseaseClass = SkinDiseaseClass.MELANOMA,
                                                        modifier = Modifier.fillMaxSize(),
                                                        shape = CircleShape
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.CenterFocusStrong,
                                                        contentDescription = null,
                                                        tint = Color.White.copy(alpha = 0.85f),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Floating Metric Tag 1 (Shield)
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .size(24.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF145356),
                                        border = BorderStroke(0.8.dp, Color(0xFF26A69A))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = null,
                                                tint = Color(0xFF64FFDA),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }

                                    // Floating Metric Tag 2 (Bar Chart)
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(24.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF145356),
                                        border = BorderStroke(0.8.dp, Color(0xFF26A69A))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color(0xFF64FFDA),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Dual Action Buttons: [Camera Scan] & [Upload Image]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Camera Scan Button (White pill)
                                Surface(
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onNavigateToCamera() }
                                        .testTag("button_camera_scan"),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoCamera,
                                            contentDescription = null,
                                            tint = Color(0xFF0A3C3E),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Camera Scan",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            ),
                                            color = Color(0xFF0A3C3E)
                                        )
                                    }
                                }

                                // Upload Image Button (Dark pill with border)
                                Surface(
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onNavigateToGallery() }
                                        .testTag("button_upload_image"),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF145356),
                                    border = BorderStroke(1.dp, Color(0xFF23767B))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Upload Image",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 3 Feature Checklist Badges at the bottom of hero card (Clean aligned 2-line layout)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HeroTipChip(
                                    icon = Icons.Default.LightMode,
                                    line1 = "Good",
                                    line2 = "lighting",
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                HeroTipChip(
                                    icon = Icons.Default.CheckCircle,
                                    line1 = "Clear",
                                    line2 = "image",
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                HeroTipChip(
                                    icon = Icons.Default.CenterFocusStrong,
                                    line1 = "Focus on",
                                    line2 = "affected area",
                                    modifier = Modifier.weight(1.3f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // 4. RECENT SCREENINGS SECTION
            // ==========================================
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Screenings",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier
                            .clickable { onNavigateToHistory() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "See All",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            ),
                            color = Color(0xFF00897B)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "See All",
                            tint = Color(0xFF00897B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Horizontal List of Recent Screenings
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (allScans.isNotEmpty()) {
                        items(allScans.take(6)) { scan ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .width(200.dp)
                                    .clickable { onSelectScan(scan) }
                                    .testTag("recent_scan_${scan.id}")
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    // Image Thumbnail Box with Floating Match Badge
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(95.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                    ) {
                                        val diseaseClass = SkinDiseaseClass.fromIndex(scan.predictedClassIndex)
                                        if (scan.imageFilePath.isNotEmpty() && File(scan.imageFilePath).exists()) {
                                            AsyncImage(
                                                model = File(scan.imageFilePath),
                                                contentDescription = scan.predictedClassName,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            SkinConditionThumbnail(
                                                diseaseClass = diseaseClass,
                                                modifier = Modifier.fillMaxSize(),
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                        }

                                        // Match Badge Tag on Top Left
                                        val matchPct = (scan.confidence * 100).toInt()
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF00796B),
                                            modifier = Modifier
                                                .padding(6.dp)
                                                .align(Alignment.TopStart)
                                        ) {
                                            Text(
                                                text = "$matchPct% Match",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                ),
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Condition Name & Date Row with Chevron
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = scan.predictedClassName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            val formattedDate = remember(scan.timestamp) {
                                                try {
                                                    val sdf = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())
                                                    sdf.format(Date(scan.timestamp))
                                                } catch (e: Exception) {
                                                    "Recent"
                                                }
                                            }
                                            Text(
                                                text = formattedDate,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 10.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Details",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Sample items matching reference image
                        items(defaultSampleScreenings) { sample ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .width(200.dp)
                                    .clickable {
                                        val match = SkinDiseaseClass.entries.find {
                                            it.displayName.contains(sample.name, ignoreCase = true) ||
                                            it.shortName.contains(sample.name, ignoreCase = true)
                                        }
                                        selectedConditionForModal = match ?: sample.diseaseClass
                                    }
                                    .testTag("sample_scan_${sample.id}")
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    // Lesion Image Preview with Match Badge
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(95.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                    ) {
                                        SkinConditionThumbnail(
                                            diseaseClass = sample.diseaseClass,
                                            modifier = Modifier.fillMaxSize(),
                                            shape = RoundedCornerShape(14.dp)
                                        )

                                        // Floating Match Badge on Top Left
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF00796B),
                                            modifier = Modifier
                                                .padding(6.dp)
                                                .align(Alignment.TopStart)
                                        ) {
                                            Text(
                                                text = "${sample.matchPercentage}% Match",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                ),
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Condition Name & Date Row with Chevron
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = sample.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = sample.timestamp,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 10.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Details",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // 5. QUICK ACTION CARDS: MY REPORTS & SKIN GUIDES
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: My Reports
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToHistory() }
                        .testTag("card_my_reports")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Green Document Icon Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "My Reports",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "My Reports",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "View all your saved analysis reports and download them.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open Reports",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Card 2: Skin Guides
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSkinGuidesDialog = true }
                        .testTag("card_skin_guides")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Purple Book Icon Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEDE7F6),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = "Skin Guides",
                                    tint = Color(0xFF673AB7),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Skin Guides",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Learn about skin conditions, causes, symptoms and care tips.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open Guides",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // 6. SKIN HEALTH TIPS SECTION
            // ==========================================
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Skin Health Tips",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier
                            .clickable { selectedTipForModal = healthTips.first() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            ),
                            color = Color(0xFF00897B)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "View All",
                            tint = Color(0xFF00897B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Horizontal Row of 4 Tips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(healthTips) { tip ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .width(155.dp)
                                .height(125.dp)
                                .clickable { selectedTipForModal = tip }
                                .testTag("tip_${tip.title.lowercase().replace(" ", "_")}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = tip.iconBgColor,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = tip.icon,
                                                contentDescription = tip.title,
                                                tint = tip.iconColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = tip.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = tip.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.5.sp,
                                        lineHeight = 14.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // 7. LEARN ABOUT SKIN CONDITIONS SECTION
            // ==========================================
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Learn About Skin Conditions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier
                            .clickable { showSkinGuidesDialog = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            ),
                            color = Color(0xFF00897B)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "View All",
                            tint = Color(0xFF00897B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Horizontal Row of Condition Cards with Round Thumbnails
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(conditionChips) { chip ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .clickable {
                                    chip.diseaseClass?.let { selectedConditionForModal = it }
                                }
                                .testTag("condition_${chip.name.lowercase().replace(" ", "_")}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Real Condition Thumbnail with Curved Edges
                                SkinConditionThumbnail(
                                    diseaseClass = chip.diseaseClass,
                                    size = 28.dp,
                                    shape = RoundedCornerShape(8.dp),
                                    fallbackColor = chip.color
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = chip.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // 8. IMPORTANT MEDICAL DISCLAIMER BANNER
            // ==========================================
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFFFFBEB),
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Medical Shield",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Important: DermaVision provides automated screening for informational purposes and does not replace professional medical diagnosis.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        ),
                        color = Color(0xFF92400E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ==========================================
    // NOTIFICATIONS DIALOG
    // ==========================================
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF00897B).copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color(0xFF00897B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (notifications.isNotEmpty()) {
                            TextButton(
                                onClick = { onClearNotifications() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Clear All",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        IconButton(
                            onClick = { showNotificationsDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            },
            text = {
                if (notifications.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF00897B).copy(alpha = 0.1f),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = null,
                                    tint = Color(0xFF00897B),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Notifications",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Real-time updates for your scans, clinical reports, and risk assessments will appear here.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications) { notif ->
                            AppNotificationCard(
                                notification = notif,
                                onDismiss = { onMarkNotificationRead(notif.id) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNotificationsDialog = false },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00897B),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
            }
        )
    }

    // ==========================================
    // SKIN CONDITION DETAIL DIALOG
    // ==========================================
    selectedConditionForModal?.let { condition ->
        AlertDialog(
            onDismissRequest = { selectedConditionForModal = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF00897B).copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = Color(0xFF00897B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = condition.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )
                    }
                    IconButton(
                        onClick = { selectedConditionForModal = null },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Risk Level Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (condition.riskLevel) {
                            RiskLevel.LOW -> Color(0xFFE8F5E9)
                            RiskLevel.LOW_TO_MODERATE -> Color(0xFFFFF3E0)
                            RiskLevel.HIGH -> Color(0xFFFFEBEE)
                        }
                    ) {
                        Text(
                            text = "Risk Level: ${condition.riskLevel.label}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = when (condition.riskLevel) {
                                RiskLevel.LOW -> Color(0xFF2E7D32)
                                RiskLevel.LOW_TO_MODERATE -> Color(0xFFEF6C00)
                                RiskLevel.HIGH -> Color(0xFFC62828)
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    // Real Disease Image with Curved Edges
                    SkinConditionThumbnail(
                        diseaseClass = condition,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Text(
                        text = condition.detailedDescription,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Common Signs & Symptoms:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    condition.commonSymptoms.forEach { symptom ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00897B),
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = symptom,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF00897B).copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color(0xFF00897B).copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Care & Prevention Advice:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF00897B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = condition.educationalAdvice,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedConditionForModal = null },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00897B),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("Got it", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
            }
        )
    }

    // ==========================================
    // HEALTH TIP DETAIL DIALOG (Styled to match DermaVision App UI)
    // ==========================================
    selectedTipForModal?.let { tip ->
        AlertDialog(
            onDismissRequest = { selectedTipForModal = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = tip.iconBgColor,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = tip.icon,
                                    contentDescription = tip.title,
                                    tint = tip.iconColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF00897B).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "SKIN HEALTH TIP",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color(0xFF00897B),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tip.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    IconButton(
                        onClick = { selectedTipForModal = null },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Subtitle Banner Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = tip.iconBgColor.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, tip.iconColor.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = tip.subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    // Detailed Advice Text
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF00897B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Dermatologist Recommendation",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    ),
                                    color = Color(0xFF00897B)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = tip.detailedAdvice,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedTipForModal = null },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00897B),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "Understood",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
            }
        )
    }

    // ==========================================
    // SKIN GUIDES DIALOG
    // ==========================================
    if (showSkinGuidesDialog) {
        AlertDialog(
            onDismissRequest = { showSkinGuidesDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF00897B).copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = Color(0xFF00897B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Skin Guides",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { showSkinGuidesDialog = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkinDiseaseClass.entries.forEach { condition ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showSkinGuidesDialog = false
                                    selectedConditionForModal = condition
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SkinConditionThumbnail(
                                    diseaseClass = condition,
                                    size = 42.dp,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = condition.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = condition.shortDescription,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color(0xFF00897B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSkinGuidesDialog = false },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00897B),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
            }
        )
    }
}

@Composable
fun HeroTipChip(
    icon: ImageVector,
    line1: String,
    line2: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.14f),
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF64FFDA),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(5.dp))
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = line1,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 12.5.sp
                ),
                color = Color.White.copy(alpha = 0.95f),
                maxLines = 1
            )
            Text(
                text = line2,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 11.5.sp
                ),
                color = Color.White.copy(alpha = 0.75f),
                maxLines = 1
            )
        }
    }
}

private fun formatNotificationTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        diff < 60_000 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

@Composable
fun AppNotificationCard(
    notification: AppNotification,
    onDismiss: () -> Unit = {}
) {
    val icon = when (notification.type) {
        NotificationType.SCAN_COMPLETED -> Icons.Default.CheckCircle
        NotificationType.HIGH_RISK_ALERT -> Icons.Default.Warning
        NotificationType.REPORT_GENERATED -> Icons.Default.PictureAsPdf
        NotificationType.QUALITY_ADVICE -> Icons.Default.AutoAwesome
        NotificationType.SYSTEM_STATUS -> Icons.Default.Notifications
    }

    val iconTint = when (notification.type) {
        NotificationType.SCAN_COMPLETED -> Color(0xFF00897B)
        NotificationType.HIGH_RISK_ALERT -> Color(0xFFD32F2F)
        NotificationType.REPORT_GENERATED -> Color(0xFF1976D2)
        NotificationType.QUALITY_ADVICE -> Color(0xFFF57C00)
        NotificationType.SYSTEM_STATUS -> MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (notification.isRead) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        } else {
            Color(0xFF00897B).copy(alpha = 0.08f)
        },
        border = if (!notification.isRead) {
            BorderStroke(1.dp, Color(0xFF00897B).copy(alpha = 0.35f))
        } else {
            BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (notification.diseaseClass != null) {
                SkinConditionThumbnail(
                    diseaseClass = notification.diseaseClass,
                    size = 40.dp,
                    shape = RoundedCornerShape(10.dp),
                    fallbackColor = iconTint
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = iconTint.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatNotificationTime(notification.timestamp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!notification.isRead) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00897B))
                )
            }
        }
    }
}
