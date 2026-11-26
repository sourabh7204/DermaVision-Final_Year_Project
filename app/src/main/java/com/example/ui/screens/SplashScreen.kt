package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.util.LocalizedStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    isLoggedIn: Boolean,
    selectedLanguage: String = "English",
    onNavigateNext: (Boolean) -> Unit
) {
    val isHindi = selectedLanguage.contains("Hindi", ignoreCase = true) || selectedLanguage.contains("हिन्दी")
    val titleText = if (isHindi) "डर्माविज़न" else "DermaVision"
    val subtitleText = if (isHindi) "त्वचा रोग जांच" else "Skin Disease Screening"

    // Multi-stage animation controllers
    val logoScale = remember { Animatable(0.2f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoRotation = remember { Animatable(-15f) }
    
    val textSlideOffset = remember { Animatable(35f) }
    val textAlpha = remember { Animatable(0f) }
    
    val pulseRing1 = remember { Animatable(0.7f) }
    val pulseRing2 = remember { Animatable(0.5f) }
    val pulseAlpha1 = remember { Animatable(0.8f) }
    val pulseAlpha2 = remember { Animatable(0.6f) }
    
    val radarRotation = remember { Animatable(0f) }
    val scanBeamOffset = remember { Animatable(-60f) }
    val progressValue = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Continuous radar & pulse loops
        launch {
            radarRotation.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
        
        launch {
            pulseRing1.animateTo(
                targetValue = 1.45f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
        launch {
            pulseAlpha1.animateTo(
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }

        launch {
            delay(400)
            pulseRing2.animateTo(
                targetValue = 1.6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
        launch {
            delay(400)
            pulseAlpha2.animateTo(
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2600, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }

        // Scanning beam loop across logo
        launch {
            scanBeamOffset.animateTo(
                targetValue = 60f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }

        // Stage 1: Spring logo entrance
        launch {
            logoAlpha.animateTo(1f, tween(400))
        }
        launch {
            logoRotation.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        }
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        // Stage 2: Staggered text entrance
        delay(150)
        launch {
            textSlideOffset.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        }
        launch {
            textAlpha.animateTo(1f, tween(450))
        }

        // Progress bar
        launch {
            progressValue.animateTo(0.5f, tween(450, easing = FastOutSlowInEasing))
            delay(200)
            progressValue.animateTo(1f, tween(400, easing = LinearEasing))
        }

        delay(1400)
        onNavigateNext(isLoggedIn)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val backgroundColor = MaterialTheme.colorScheme.background
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryContainer.copy(alpha = 0.45f),
                        backgroundColor,
                        backgroundColor
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            // Animated Hero Emblem with Radar Orbit & Halo Pulse Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(190.dp)
                    .scale(logoScale.value)
                    .rotate(logoRotation.value)
                    .alpha(logoAlpha.value)
            ) {
                // Radar Scan & Pulse Waves
                Canvas(modifier = Modifier.size(180.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val baseRadius = size.minDimension / 2.2f

                    // Pulse Ring 1
                    drawCircle(
                        color = primaryColor.copy(alpha = (pulseAlpha1.value * 0.4f).coerceIn(0f, 1f)),
                        radius = baseRadius * pulseRing1.value,
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // Pulse Ring 2
                    drawCircle(
                        color = Color(0xFFFF7043).copy(alpha = (pulseAlpha2.value * 0.35f).coerceIn(0f, 1f)),
                        radius = baseRadius * pulseRing2.value,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Orbiting radar satellite dots
                    val rad = Math.toRadians(radarRotation.value.toDouble())
                    val orbitX = center.x + (baseRadius * 1.05f) * cos(rad).toFloat()
                    val orbitY = center.y + (baseRadius * 1.05f) * sin(rad).toFloat()
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = Offset(orbitX, orbitY)
                    )

                    val oppositeRad = Math.toRadians((radarRotation.value + 180).toDouble())
                    val oppOrbitX = center.x + (baseRadius * 1.05f) * cos(oppositeRad).toFloat()
                    val oppOrbitY = center.y + (baseRadius * 1.05f) * sin(oppositeRad).toFloat()
                    drawCircle(
                        color = Color(0xFFFF7043),
                        radius = 3.5.dp.toPx(),
                        center = Offset(oppOrbitX, oppOrbitY)
                    )
                }

                // Decorative Ambient Glow Surface
                Surface(
                    modifier = Modifier.size(130.dp),
                    shape = RoundedCornerShape(38.dp),
                    color = primaryColor.copy(alpha = 0.15f)
                ) {}

                // Main Medical Badge Card matching App Launcher Icon
                Surface(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(30.dp),
                            spotColor = primaryColor.copy(alpha = 0.4f),
                            ambientColor = primaryColor.copy(alpha = 0.2f)
                        ),
                    shape = RoundedCornerShape(30.dp),
                    color = Color.Transparent,
                    border = BorderStroke(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF009688),
                                        Color(0xFF006A6A),
                                        Color(0xFF004D4D)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "DermaVision App Icon",
                            modifier = Modifier.size(100.dp)
                        )

                        // Animated scanning beam across the logo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .offset(y = scanBeamOffset.value.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xFFFF7043).copy(alpha = 0.85f),
                                            Color.White,
                                            Color(0xFFFF7043).copy(alpha = 0.85f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Title & Slogan - Clean, High Contrast with Vision in Teal
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = textSlideOffset.value.dp)
                    .alpha(textAlpha.value)
            ) {
                if (isHindi) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "डर्मा",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = onBackgroundColor
                        )
                        Text(
                            text = "विज़न",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color(0xFF009688)
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Derma",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = onBackgroundColor
                        )
                        Text(
                            text = "Vision",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color(0xFF009688)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp,
                            fontSize = 15.sp
                        ),
                        color = primaryColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Minimalist Progress Bar
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .alpha(textAlpha.value)
            ) {
                LinearProgressIndicator(
                    progress = { progressValue.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = primaryColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}
