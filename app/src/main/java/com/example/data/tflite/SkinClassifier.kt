package com.example.data.tflite

import android.content.Context
import android.graphics.Bitmap
import com.example.data.model.ClassificationResponse

/**
 * Backward compatibility wrapper delegating to dedicated [SkinDiseaseClassifier].
 */
class SkinClassifier(context: Context) {

    private val delegate = SkinDiseaseClassifier(context)

    fun isModelAvailable(): Boolean = delegate.isModelAvailable()

    suspend fun classifyImage(bitmap: Bitmap, imagePath: String? = null): ClassificationResponse {
        return delegate.classifyImage(bitmap, imagePath)
    }

    fun close() {
        delegate.close()
    }
}
