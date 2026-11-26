package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.model.BrightnessQuality
import com.example.data.model.ImageQualityReport
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object ImageQualityChecker {

    fun analyze(bitmap: Bitmap): ImageQualityReport {
        val width = bitmap.width
        val height = bitmap.height

        val isResolutionGood = width >= 224 && height >= 224
        val isResolutionOptimal = width >= 400 && height >= 400

        // Subsample for fast luminance, skin color & sharpness computation (max 150x150 samples)
        val sampleStepX = (width / 150).coerceAtLeast(1)
        val sampleStepY = (height / 150).coerceAtLeast(1)

        var totalLuminance = 0.0
        var pixelCount = 0
        var skinPixelCount = 0

        val sampledLuminances = mutableListOf<Double>()
        val hsvBuffer = FloatArray(3)

        for (y in 0 until height step sampleStepY) {
            for (x in 0 until width step sampleStepX) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                // Standard ITU-R BT.601 luminance
                val lum = 0.299 * r + 0.587 * g + 0.114 * b
                totalLuminance += lum
                sampledLuminances.add(lum)
                pixelCount++

                // Dermatological & Fitzpatrick Skin Tone Validation (YCbCr + HSV + RGB)
                if (isSkinColor(r, g, b, hsvBuffer)) {
                    skinPixelCount++
                }
            }
        }

        val avgLuminance = if (pixelCount > 0) totalLuminance / pixelCount else 128.0
        val skinRatio = if (pixelCount > 0) skinPixelCount.toFloat() / pixelCount else 0f

        // An image requires at least 15% cutaneous/skin color coverage to be recognized as a valid skin photo
        val isSkinDetected = skinRatio >= 0.15f

        val brightnessStatus = when {
            avgLuminance < 45.0 -> BrightnessQuality.TOO_DARK
            avgLuminance > 225.0 -> BrightnessQuality.TOO_BRIGHT
            else -> BrightnessQuality.OPTIMAL
        }

        // Compute local gradient variance (Laplacian approximation) to assess blur/sharpness
        var gradientSum = 0.0
        var gradientCount = 0
        val sampleGridCols = width / sampleStepX
        val sampleGridRows = sampledLuminances.size / sampleGridCols.coerceAtLeast(1)

        for (r in 1 until (sampleGridRows - 1)) {
            for (c in 1 until (sampleGridCols - 1)) {
                val idx = r * sampleGridCols + c
                if (idx < sampledLuminances.size && idx - 1 >= 0 && idx + 1 < sampledLuminances.size && idx - sampleGridCols >= 0 && idx + sampleGridCols < sampledLuminances.size) {
                    val center = sampledLuminances[idx]
                    val left = sampledLuminances[idx - 1]
                    val right = sampledLuminances[idx + 1]
                    val top = sampledLuminances[idx - sampleGridCols]
                    val bottom = sampledLuminances[idx + sampleGridCols]

                    val laplacian = abs(4 * center - left - right - top - bottom)
                    gradientSum += laplacian
                    gradientCount++
                }
            }
        }

        val blurScore = if (gradientCount > 0) gradientSum / gradientCount else 15.0
        val isSharpnessGood = blurScore >= 7.0

        val warnings = mutableListOf<String>()
        val tips = mutableListOf<String>()

        if (!isSkinDetected) {
            warnings.add("Photo does not appear to contain human skin or a skin lesion.")
            tips.add("Please capture or upload a close-up photo of the affected skin area.")
        }

        if (!isResolutionGood) {
            warnings.add("Image resolution is below model minimum (224x224 px).")
            tips.add("Select or capture a higher resolution skin photo.")
        } else if (!isResolutionOptimal) {
            tips.add("Higher resolution photos (>400px) improve feature extraction accuracy.")
        }

        when (brightnessStatus) {
            BrightnessQuality.TOO_DARK -> {
                warnings.add("Photo is under-exposed (too dark for reliable AI feature analysis).")
                tips.add("Increase ambient lighting or enable device camera flash.")
            }
            BrightnessQuality.TOO_BRIGHT -> {
                warnings.add("Photo has excessive glare or over-exposure.")
                tips.add("Avoid direct specular flash reflection and angle the light evenly.")
            }
            BrightnessQuality.OPTIMAL -> {}
        }

        if (!isSharpnessGood) {
            warnings.add("Lesion focus appears soft or blurred.")
            tips.add("Hold phone steady, tap to focus on the skin lesion, and clean the camera lens.")
        }

        val isOverallAcceptable = isSkinDetected && isResolutionGood && (brightnessStatus == BrightnessQuality.OPTIMAL) && isSharpnessGood

        return ImageQualityReport(
            width = width,
            height = height,
            isResolutionGood = isResolutionGood,
            averageLuminance = avgLuminance,
            brightnessStatus = brightnessStatus,
            blurScore = blurScore,
            isSharpnessGood = isSharpnessGood,
            isSkinDetected = isSkinDetected,
            skinPixelRatio = skinRatio,
            isOverallAcceptable = isOverallAcceptable,
            warnings = warnings,
            tips = tips
        )
    }

    /**
     * Multi-space cutaneous classifier supporting all Fitzpatrick skin phototypes (I to VI)
     * as well as erythematous/inflammatory lesions and hyperpigmented spots.
     */
    private fun isSkinColor(r: Int, g: Int, b: Int, hsv: FloatArray): Boolean {
        // YCbCr transformation
        val y = 0.299 * r + 0.587 * g + 0.114 * b
        val cb = -0.1687 * r - 0.3313 * g + 0.500 * b + 128
        val cr = 0.500 * r - 0.4187 * g - 0.0813 * b + 128

        // General YCbCr skin cluster
        val isYcbcrSkin = cb in 75.0..138.0 && cr in 128.0..182.0 && y in 30.0..248.0

        // RGB rule for fair to dark skin tones
        val maxVal = max(r, max(g, b))
        val minVal = min(r, min(g, b))
        val isRgbSkin = r > 40 && g > 25 && b > 15 &&
                r > g && r > b &&
                (r - g) >= 8 &&
                (maxVal - minVal) >= 10

        // Erythematous / inflammatory / vascular rash tones (redness on skin)
        val isErythemaSkin = r > 70 && r > (g * 1.2f) && r > (b * 1.2f) && (g > 20)

        // HSV color space check (Peach, Brown, Dark Brown, Olive)
        Color.RGBToHSV(r, g, b, hsv)
        val hue = hsv[0]
        val sat = hsv[1]
        val value = hsv[2]
        val isHsvSkin = ((hue in 0f..52f) || (hue in 330f..360f)) && sat in 0.10f..0.92f && value in 0.15f..0.99f

        return isYcbcrSkin || (isRgbSkin && isHsvSkin) || isErythemaSkin
    }
}

