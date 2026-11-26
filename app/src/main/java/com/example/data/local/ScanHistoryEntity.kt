package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ClassProbability
import com.example.data.model.PredictionResult
import com.example.data.model.SkinDiseaseClass
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imageFilePath: String,
    val predictedClassIndex: Int,
    val predictedClassName: String,
    val confidence: Float,
    val topPredictionsJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelVersion: String,
    val severityStatus: String,
    val notes: String = ""
) {
    fun toPredictionResult(): PredictionResult {
        val primary = SkinDiseaseClass.fromIndex(predictedClassIndex) ?: SkinDiseaseClass.ECZEMA
        val topList = mutableListOf<ClassProbability>()
        try {
            val jsonArray = JSONArray(topPredictionsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val idx = obj.getInt("index")
                val prob = obj.getDouble("prob").toFloat()
                val cls = SkinDiseaseClass.fromIndex(idx) ?: primary
                topList.add(ClassProbability(cls, prob))
            }
        } catch (e: Exception) {
            topList.add(ClassProbability(primary, confidence))
        }

        return PredictionResult(
            primaryClass = primary,
            confidence = confidence,
            topPredictions = if (topList.isNotEmpty()) topList else listOf(ClassProbability(primary, confidence)),
            timestamp = timestamp,
            modelVersion = modelVersion,
            severityStatus = severityStatus,
            imagePath = imageFilePath
        )
    }

    companion object {
        fun fromPredictionResult(result: PredictionResult, imagePath: String, notes: String = ""): ScanHistoryEntity {
            val jsonArray = JSONArray()
            result.topPredictions.forEach { item ->
                val obj = JSONObject()
                obj.put("index", item.diseaseClass.classIndex)
                obj.put("name", item.diseaseClass.displayName)
                obj.put("prob", item.probability)
                jsonArray.put(obj)
            }

            return ScanHistoryEntity(
                imageFilePath = imagePath,
                predictedClassIndex = result.primaryClass.classIndex,
                predictedClassName = result.primaryClass.displayName,
                confidence = result.confidence,
                topPredictionsJson = jsonArray.toString(),
                timestamp = result.timestamp,
                modelVersion = result.modelVersion,
                severityStatus = result.severityStatus,
                notes = notes
            )
        }
    }
}
