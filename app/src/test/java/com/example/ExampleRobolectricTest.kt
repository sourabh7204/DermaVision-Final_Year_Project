package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SkinDiseaseClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DermaVision", appName)
  }

  @Test
  fun `verify ten disease classes defined for V2`() {
    val classes = SkinDiseaseClass.getAllClasses()
    assertEquals(10, classes.size)
    assertEquals("Eczema", SkinDiseaseClass.fromIndex(0)?.displayName)
    assertEquals("Warts, Molluscum and other Viral Infections", SkinDiseaseClass.fromIndex(1)?.displayName)
    assertEquals("Melanoma", SkinDiseaseClass.fromIndex(2)?.displayName)
    assertEquals("Atopic Dermatitis", SkinDiseaseClass.fromIndex(3)?.displayName)
    assertEquals("Basal Cell Carcinoma (BCC)", SkinDiseaseClass.fromIndex(4)?.displayName)
    assertEquals("Melanocytic Nevi (NV)", SkinDiseaseClass.fromIndex(5)?.displayName)
    assertEquals("Benign Keratosis-like Lesions (BKL)", SkinDiseaseClass.fromIndex(6)?.displayName)
    assertEquals("Psoriasis, Lichen Planus and related diseases", SkinDiseaseClass.fromIndex(7)?.displayName)
    assertEquals("Seborrheic Keratoses and other Benign Tumors", SkinDiseaseClass.fromIndex(8)?.displayName)
    assertEquals("Tinea, Ringworm, Candidiasis and other Fungal Infections", SkinDiseaseClass.fromIndex(9)?.displayName)
  }

  @Test
  fun `verify skin disease classifier mapping and top 3 predictions`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val classifier = com.example.data.tflite.SkinDiseaseClassifier(context)
    
    // Verify default class mapping
    assertEquals("Eczema", classifier.getClassName(0))
    assertEquals("Melanoma", classifier.getClassName(2))
    assertEquals("Basal Cell Carcinoma (BCC)", classifier.getClassName(4))

    // Verify model availability
    org.junit.Assert.assertTrue(classifier.isModelAvailable())

    // Test inference on annular ring lesion (Ringworm pattern)
    val sampleBitmap = android.graphics.Bitmap.createBitmap(224, 224, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(sampleBitmap)
    // Draw background normal skin tone
    canvas.drawColor(android.graphics.Color.rgb(235, 205, 185))
    // Draw active erythematous scaly ring border with central clearing (Ringworm hallmark)
    val ringPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(210, 60, 50)
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 24f
    }
    canvas.drawCircle(112f, 112f, 65f, ringPaint)

    val response = classifier.classifyImage(sampleBitmap)
    assertNotNull(response)
    if (response is com.example.data.model.ClassificationResponse.Success) {
        assertNotNull(response.result.primaryClass)
        assertEquals(3, response.result.topPredictions.size)
        org.junit.Assert.assertTrue(response.result.confidence in 0.0f..1.0f)
        // Verify Tinea / Ringworm is elevated in predictions
        val isRingwormInTop = response.result.topPredictions.any { 
            it.diseaseClass == SkinDiseaseClass.FUNGAL_INFECTIONS 
        }
        org.junit.Assert.assertTrue(isRingwormInTop)
    } else if (response is com.example.data.model.ClassificationResponse.Error) {
        org.junit.Assert.assertTrue(response.errorMessage.isNotEmpty())
    }

    // Verify classifier handles recycled bitmap gracefully
    val testBitmap = android.graphics.Bitmap.createBitmap(224, 224, android.graphics.Bitmap.Config.ARGB_8888)
    testBitmap.recycle()
    val errorResponse = classifier.classifyImage(testBitmap)
    org.junit.Assert.assertTrue(errorResponse is com.example.data.model.ClassificationResponse.Error)
  }
}
