package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.util.LocalizedStrings

enum class BottomNavTab(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME(
        route = Screen.Dashboard.route,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_bottom_home"
    ),
    HISTORY(
        route = Screen.History.route,
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History,
        testTag = "nav_bottom_history"
    ),
    SCAN(
        route = Screen.Camera.route,
        selectedIcon = Icons.Filled.PhotoCamera,
        unselectedIcon = Icons.Outlined.PhotoCamera,
        testTag = "nav_bottom_scan"
    ),
    STATS(
        route = Screen.Analytics.route,
        selectedIcon = Icons.Filled.Analytics,
        unselectedIcon = Icons.Outlined.Analytics,
        testTag = "nav_bottom_stats"
    ),
    PROFILE(
        route = Screen.Profile.route,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "nav_bottom_profile"
    )
}

/**
 * Custom Arched Mound Shape for Bottom Navigation Bar
 * Creates a smooth elevated dome/arch in the center that rises higher for the Scan button
 */
private fun createArchedBottomBarShape(
    archHeightPx: Float,
    archRadiusPx: Float
): GenericShape {
    return GenericShape { size: Size, _ ->
        val centerX = size.width / 2f
        val baseTop = archHeightPx
        val moundWidth = archRadiusPx * 2.2f

        reset()
        // Top-left straight line up to the arch base
        moveTo(0f, baseTop)
        lineTo(centerX - moundWidth / 2f, baseTop)

        // Smooth cubic curve up to the crest of the arch
        cubicTo(
            centerX - moundWidth / 3.2f, baseTop,
            centerX - moundWidth / 4f, 0f,
            centerX, 0f
        )

        // Smooth cubic curve down from the crest to the right base line
        cubicTo(
            centerX + moundWidth / 4f, 0f,
            centerX + moundWidth / 3.2f, baseTop,
            centerX + moundWidth / 2f, baseTop
        )

        // Straight line to the right edge
        lineTo(size.width, baseTop)

        // Down right edge to bottom
        lineTo(size.width, size.height)

        // Bottom edge to bottom-left
        lineTo(0f, size.height)

        close()
    }
}

@Composable
fun DermaVisionBottomBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedLanguage: String = "English"
) {
    val strings = remember(selectedLanguage) { LocalizedStrings.get(selectedLanguage) }
    val density = LocalDensity.current
    val archHeightDp = 20.dp
    val archRadiusDp = 42.dp

    val archHeightPx = with(density) { archHeightDp.toPx() }
    val archRadiusPx = with(density) { archRadiusDp.toPx() }

    val archedShape = remember(archHeightPx, archRadiusPx) {
        createArchedBottomBarShape(archHeightPx, archRadiusPx)
    }

    val isScanSelected = currentRoute == BottomNavTab.SCAN.route

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Arched Bottom Surface Background with elevated center border
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = archedShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    ambientColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ),
            shape = archedShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
            )
        ) {
            // Horizontal Items Container positioned in the main row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(top = archHeightDp + 2.dp, start = 6.dp, end = 6.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Tabs: Home & History
                BottomNavItem(
                    tab = BottomNavTab.HOME,
                    title = strings.navHome,
                    isSelected = currentRoute == BottomNavTab.HOME.route,
                    onNavigateToRoute = onNavigateToRoute
                )

                BottomNavItem(
                    tab = BottomNavTab.HISTORY,
                    title = strings.navHistory,
                    isSelected = currentRoute == BottomNavTab.HISTORY.route,
                    onNavigateToRoute = onNavigateToRoute
                )

                // Center Spacer for the elevated Scan button
                Spacer(modifier = Modifier.width(68.dp))

                // Right Tabs: Stats & Profile
                BottomNavItem(
                    tab = BottomNavTab.STATS,
                    title = strings.navStats,
                    isSelected = currentRoute == BottomNavTab.STATS.route,
                    onNavigateToRoute = onNavigateToRoute
                )

                BottomNavItem(
                    tab = BottomNavTab.PROFILE,
                    title = strings.navProfile,
                    isSelected = currentRoute == BottomNavTab.PROFILE.route,
                    onNavigateToRoute = onNavigateToRoute
                )
            }
        }

        // Prominent Elevated Center Scan Button (Rises higher than the rest of the navigation bar)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-4).dp),
            contentAlignment = Alignment.Center
        ) {
            val scaleAnim by animateFloatAsState(
                targetValue = if (isScanSelected) 1.06f else 1.0f,
                animationSpec = tween(250),
                label = "scan_scale"
            )

            // Outer decorative concentric halo border
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                border = BorderStroke(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .size(62.dp)
                    .scale(scaleAnim)
                    .testTag(BottomNavTab.SCAN.testTag)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onNavigateToRoute(BottomNavTab.SCAN.route)
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF00897B),
                                    Color(0xFF00695C)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = "Scan Skin",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = strings.navScan,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: BottomNavTab,
    title: String,
    isSelected: Boolean,
    onNavigateToRoute: (String) -> Unit
) {
    val activeTeal = Color(0xFF00897B)
    val inactiveGray = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    Column(
        modifier = Modifier
            .size(width = 56.dp, height = 50.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                if (!isSelected) {
                    onNavigateToRoute(tab.route)
                }
            }
            .testTag(tab.testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
            contentDescription = title,
            tint = if (isSelected) activeTeal else inactiveGray,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.5.sp
            ),
            color = if (isSelected) activeTeal else inactiveGray,
            maxLines = 1
        )
    }
}
