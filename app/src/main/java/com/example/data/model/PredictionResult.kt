package com.example.data.model

enum class SeverityLevel(
    val label: String,
    val title: String,
    val description: String,
    val recommendation: String
) {
    MILD(
        label = "Mild",
        title = "Mild Severity",
        description = "Superficial epidermal involvement with localized presentation and low progression risk.",
        recommendation = "Maintain regular skincare, keep the area clean and moisturized, and monitor for changes."
    ),
    MODERATE(
        label = "Moderate",
        title = "Moderate Severity",
        description = "Active inflammatory markers, persistent lesions, or distinct epidermal scaling present.",
        recommendation = "Non-urgent clinical consultation recommended if symptoms persist, cause discomfort, or expand."
    ),
    SEVERE(
        label = "Severe",
        title = "Severe Severity",
        description = "High clinical priority indicators detected (marked cellular atypia, ulceration, or high-risk category).",
        recommendation = "Prompt dermatological or clinical evaluation strongly advised for definitive examination."
    );

    companion object {
        fun compute(diseaseClass: SkinDiseaseClass, confidence: Float = 0.9f): SeverityLevel {
            return when (diseaseClass.category) {
                DiseaseCategory.MALIGNANT -> SEVERE
                DiseaseCategory.INFLAMMATORY -> {
                    if (diseaseClass == SkinDiseaseClass.ATOPIC_DERMATITIS || confidence > 0.92f) MODERATE else MILD
                }
                DiseaseCategory.VIRAL, DiseaseCategory.FUNGAL -> {
                    if (confidence > 0.92f) MODERATE else MILD
                }
                DiseaseCategory.BENIGN -> MILD
            }
        }
    }
}

data class PredictionResult(
    val primaryClass: SkinDiseaseClass,
    val confidence: Float, // 0.0 to 1.0
    val topPredictions: List<ClassProbability>,
    val timestamp: Long = System.currentTimeMillis(),
    val modelVersion: String = "DermaVision V2 (224x224 RGB)",
    val severityLevel: SeverityLevel = SeverityLevel.compute(primaryClass, confidence),
    val severityStatus: String = "${SeverityLevel.compute(primaryClass, confidence).label} — ${SeverityLevel.compute(primaryClass, confidence).description}",
    val imagePath: String? = null,
    val engineName: String = "DermaVision Clinical AI",
    val isOfflineInference: Boolean = true,
    val inferenceTimeMs: Long = 45L,
    val isInvalidSkinImage: Boolean = false,
    val invalidImageReason: String? = null
) {
    val confidencePercentage: Int
        get() = (confidence * 100).toInt().coerceIn(0, 100)

    val isLowConfidence: Boolean
        get() = !isInvalidSkinImage && (confidence < 0.60f || (topPredictions.size >= 2 && (topPredictions[0].probability - topPredictions[1].probability < 0.08f) && confidence < 0.72f))

    val lowConfidenceMessage: String
        get() = if (topPredictions.size >= 2 && topPredictions[0].probability - topPredictions[1].probability < 0.08f) {
            "Ambiguous overlapping visual characteristics detected between ${topPredictions[0].diseaseClass.displayName} (${topPredictions[0].percentage}%) and ${topPredictions[1].diseaseClass.displayName} (${topPredictions[1].percentage}%)."
        } else {
            "Match probability is below optimal clinical threshold ($confidencePercentage%)."
        }

    companion object {
        fun createInvalidSkinResult(
            imagePath: String? = null,
            reason: String = "The uploaded photo does not appear to contain human skin or a dermatological lesion. Please capture or upload a clear, close-up photo of the affected skin area."
        ): PredictionResult {
            return PredictionResult(
                primaryClass = SkinDiseaseClass.ECZEMA,
                confidence = 0.0f,
                topPredictions = emptyList(),
                imagePath = imagePath,
                isInvalidSkinImage = true,
                invalidImageReason = reason,
                severityStatus = "Non-skin photo detected"
            )
        }
    }
}

data class ClassProbability(
    val diseaseClass: SkinDiseaseClass,
    val probability: Float // 0.0 to 1.0
) {
    val percentage: Int
        get() = (probability * 100).toInt().coerceIn(0, 100)
}

sealed class ClassificationResponse {
    data class Success(val result: PredictionResult) : ClassificationResponse()
    data class InvalidSkinImage(
        val message: String = "The image provided does not appear to be a skin or dermatological photo. Please capture or upload a clear, well-lit photo of the skin lesion."
    ) : ClassificationResponse()
    data class ModelNotAvailable(
        val message: String = "Classification model 'dermavision_v2.tflite' is not available in app assets yet. Place your trained model in app/src/main/assets/ to run predictions."
    ) : ClassificationResponse()
    data class Error(val errorMessage: String) : ClassificationResponse()
}

