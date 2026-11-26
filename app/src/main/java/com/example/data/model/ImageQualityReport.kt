package com.example.data.model

data class ImageQualityReport(
    val width: Int,
    val height: Int,
    val isResolutionGood: Boolean,
    val averageLuminance: Double, // 0 to 255
    val brightnessStatus: BrightnessQuality,
    val blurScore: Double,
    val isSharpnessGood: Boolean,
    val isSkinDetected: Boolean = true,
    val skinPixelRatio: Float = 1.0f,
    val isOverallAcceptable: Boolean,
    val warnings: List<String>,
    val tips: List<String>
)

enum class BrightnessQuality(val label: String) {
    TOO_DARK("Too Dark"),
    OPTIMAL("Optimal Lighting"),
    TOO_BRIGHT("Overexposed / Glare")
}
