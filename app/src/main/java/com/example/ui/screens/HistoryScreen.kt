package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ScanHistoryEntity
import com.example.data.model.DiseaseCategory
import com.example.data.model.SeverityLevel
import com.example.data.model.SkinDiseaseClass
import com.example.ui.components.DermaVisionBottomBar
import com.example.ui.navigation.Screen
import com.example.util.LocalizedStrings
import com.example.ui.theme.StatusAmberBorder
import com.example.ui.theme.StatusAmberContainer
import com.example.ui.theme.StatusAmberText
import com.example.ui.theme.StatusGreenBorder
import com.example.ui.theme.StatusGreenContainer
import com.example.ui.theme.StatusGreenText
import com.example.ui.theme.StatusRedBorder
import com.example.ui.theme.StatusRedContainer
import com.example.ui.theme.StatusRedText
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class HistorySortOption(val label: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    HIGHEST_CONFIDENCE("Highest Match"),
    LOWEST_CONFIDENCE("Lowest Match")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    scans: List<ScanHistoryEntity>,
    onSelectScan: (ScanHistoryEntity) -> Unit,
    onDeleteScan: (ScanHistoryEntity) -> Unit,
    onClearAll: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit = {},
    selectedLanguage: String = "English"
) {
    val strings = remember(selectedLanguage) { LocalizedStrings.get(selectedLanguage) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableIntStateOf(-1) } // -1 = All
    var selectedSortOption by remember { mutableStateOf(HistorySortOption.NEWEST) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var scanToDelete by remember { mutableStateOf<ScanHistoryEntity?>(null) }

    // Summary statistics for the history view
    val totalCount = scans.size
    val averageConfidence = remember(scans) {
        if (scans.isNotEmpty()) (scans.map { it.confidence }.average() * 100).toInt() else 0
    }
    val topDetectedCondition = remember(scans) {
        if (scans.isNotEmpty()) {
            scans.groupBy { it.predictedClassName }
                .maxByOrNull { it.value.size }?.key ?: "None"
        } else "None"
    }

    val filteredScans = remember(scans, searchQuery, selectedCategoryFilter, selectedSortOption) {
        val filtered = scans.filter { scan ->
            val matchesSearch = searchQuery.isBlank() ||
                    scan.predictedClassName.contains(searchQuery, ignoreCase = true) ||
                    scan.severityStatus.contains(searchQuery, ignoreCase = true)

            val matchesCategory = if (selectedCategoryFilter == -1) {
                true
            } else {
                val diseaseClass = SkinDiseaseClass.fromIndex(scan.predictedClassIndex)
                diseaseClass?.classIndex == selectedCategoryFilter
            }
            matchesSearch && matchesCategory
        }

        when (selectedSortOption) {
            HistorySortOption.NEWEST -> filtered.sortedByDescending { it.timestamp }
            HistorySortOption.OLDEST -> filtered.sortedBy { it.timestamp }
            HistorySortOption.HIGHEST_CONFIDENCE -> filtered.sortedByDescending { it.confidence }
            HistorySortOption.LOWEST_CONFIDENCE -> filtered.sortedBy { it.confidence }
        }
    }

    Scaffold(
        bottomBar = {
            DermaVisionBottomBar(
                currentRoute = Screen.History.route,
                onNavigateToRoute = onNavigateToRoute,
                selectedLanguage = selectedLanguage
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = strings.scanHistoryTitle,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (totalCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(top = 1.dp)
                                ) {
                                    Text(
                                        text = "$totalCount",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = strings.scanHistorySubtitle,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("button_back_history")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Sort dropdown button
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("button_sort_history")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort records",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            HistorySortOption.entries.forEach { option ->
                                val sortLabel = when (option) {
                                    HistorySortOption.NEWEST -> strings.newestFirst
                                    HistorySortOption.OLDEST -> strings.oldestFirst
                                    HistorySortOption.HIGHEST_CONFIDENCE -> strings.highestMatch
                                    HistorySortOption.LOWEST_CONFIDENCE -> strings.lowestMatch
                                }
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = sortLabel,
                                            fontWeight = if (selectedSortOption == option) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedSortOption == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        selectedSortOption = option
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    if (scans.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearAllDialog = true },
                            modifier = Modifier.testTag("button_clear_all_history")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All History",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Header Stats Ribbon (if records exist)
            if (scans.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HistoryStatItem(label = "TOTAL SCANS", value = "$totalCount")
                        HistoryStatItem(label = "AVG CONFIDENCE", value = "$averageConfidence%")
                        HistoryStatItem(label = "TOP FINDING", value = topDetectedCondition, isWide = true)
                    }
                }
            }

            // Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_search_history"),
                        placeholder = {
                            Text(
                                "Search conditions or severities...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Category Filter Horizontal Scroll
            val allClasses = remember { SkinDiseaseClass.getAllClasses() }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                // All Chip
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == -1,
                        onClick = { selectedCategoryFilter = -1 },
                        label = {
                            Text(
                                "All (${scans.size})",
                                fontWeight = if (selectedCategoryFilter == -1) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Disease Classes Chips
                items(allClasses) { cls ->
                    val count = scans.count { it.predictedClassIndex == cls.classIndex }
                    FilterChip(
                        selected = selectedCategoryFilter == cls.classIndex,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == cls.classIndex) -1 else cls.classIndex
                        },
                        label = {
                            Text(
                                text = "${cls.shortName} ($count)",
                                fontWeight = if (selectedCategoryFilter == cls.classIndex) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // History Records Count / Sort Summary
            if (filteredScans.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Showing ${filteredScans.size} ${if (filteredScans.size == 1) "record" else "records"}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Sorted: ${selectedSortOption.label}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // History List
            if (filteredScans.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredScans, key = { it.id }) { scan ->
                        EnhancedHistoryScanCard(
                            scan = scan,
                            onClick = { onSelectScan(scan) },
                            onDelete = { scanToDelete = scan }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } else {
                // Empty / Zero Result State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty() || selectedCategoryFilter != -1) {
                                    "No matching scan records"
                                } else {
                                    "No scan history yet"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty() || selectedCategoryFilter != -1) {
                                    "No records match your active search terms or category filters. Try clearing filters."
                                } else {
                                    "Perform your first clinical skin analysis from the Home tab. Your results, confidence ratings, and AI observations will appear here."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            if (searchQuery.isNotEmpty() || selectedCategoryFilter != -1) {
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedButton(
                                    onClick = {
                                        searchQuery = ""
                                        selectedCategoryFilter = -1
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Reset Filters")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Single Item Confirmation Dialog
    if (scanToDelete != null) {
        val scan = scanToDelete!!
        AlertDialog(
            onDismissRequest = { scanToDelete = null },
            title = {
                Text(
                    "Delete Scan Record",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete the clinical record for '${scan.predictedClassName}'? This action cannot be reversed."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteScan(scan)
                        scanToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Record", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { scanToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All History Confirmation Dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = {
                Text(
                    "Clear Entire History",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to permanently delete all $totalCount scan records from local database storage?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EnhancedHistoryScanCard(
    scan: ScanHistoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val diseaseClass = remember(scan.predictedClassIndex) {
        SkinDiseaseClass.fromIndex(scan.predictedClassIndex)
    }
    val severity = remember(diseaseClass, scan.confidence) {
        if (diseaseClass != null) {
            SeverityLevel.compute(diseaseClass, scan.confidence)
        } else null
    }

    val (sevBg, sevBorder, sevText) = when (severity) {
        SeverityLevel.MILD -> Triple(StatusGreenContainer, StatusGreenBorder, StatusGreenText)
        SeverityLevel.MODERATE -> Triple(StatusAmberContainer, StatusAmberBorder, StatusAmberText)
        SeverityLevel.SEVERE -> Triple(StatusRedContainer, StatusRedBorder, StatusRedText)
        null -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.outline,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    val categoryColor = when (diseaseClass?.category) {
        DiseaseCategory.MALIGNANT -> Color(0xFFD32F2F)
        DiseaseCategory.INFLAMMATORY -> Color(0xFF0288D1)
        DiseaseCategory.VIRAL -> Color(0xFF7B1FA2)
        DiseaseCategory.BENIGN -> Color(0xFF388E3C)
        DiseaseCategory.FUNGAL -> Color(0xFFE65100)
        null -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("card_history_item_${scan.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image Thumbnail in small box with curved edge (Shadow removed)
                Surface(
                    modifier = Modifier.size(76.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp,
                    border = BorderStroke(1.2.dp, Color(0xFF009688).copy(alpha = 0.4f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (scan.imageFilePath.isNotEmpty() && File(scan.imageFilePath).exists()) {
                            AsyncImage(
                                model = File(scan.imageFilePath),
                                contentDescription = scan.predictedClassName,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Condition Title & Metadata
                Column(modifier = Modifier.weight(1f)) {
                    // Category Tag
                    if (diseaseClass != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = categoryColor.copy(alpha = 0.12f),
                            border = BorderStroke(0.75.dp, categoryColor.copy(alpha = 0.35f)),
                            modifier = Modifier.padding(bottom = 3.dp)
                        ) {
                            Text(
                                text = diseaseClass.category.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.4.sp
                                ),
                                color = categoryColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Main Condition Name
                    Text(
                        text = scan.predictedClassName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Match Confidence and Severity Badges
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Confidence Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        ) {
                            Text(
                                text = "${(scan.confidence * 100).toInt()}% Match",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                            )
                        }

                        // Severity Badge
                        if (severity != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = sevBg,
                                border = BorderStroke(0.75.dp, sevBorder)
                            ) {
                                Text(
                                    text = severity.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = sevText,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Date & Time
                    Text(
                        text = formatHumanFriendlyTimestamp(scan.timestamp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Delete Action Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Mini Confidence Bar
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { scan.confidence },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun HistoryStatItem(
    label: String,
    value: String,
    isWide: Boolean = false
) {
    Column(
        modifier = if (isWide) Modifier.width(130.dp) else Modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 0.4.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun formatHumanFriendlyTimestamp(timestamp: Long): String {
    val now = Calendar.getInstance()
    val scanTime = Calendar.getInstance().apply { timeInMillis = timestamp }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(timestamp))

    return when {
        now.get(Calendar.YEAR) == scanTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == scanTime.get(Calendar.DAY_OF_YEAR) -> {
            "Today • $formattedTime"
        }
        now.get(Calendar.YEAR) == scanTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - scanTime.get(Calendar.DAY_OF_YEAR) == 1 -> {
            "Yesterday • $formattedTime"
        }
        else -> {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}
