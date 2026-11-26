package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.PredictionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    suspend fun generateReport(
        context: Context,
        result: PredictionResult,
        patientName: String = "Self Examination",
        bitmap: Bitmap? = null
    ): File = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 points (595x842)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(result.timestamp))

        // Background
        canvas.drawColor(Color.WHITE)

        // 1. Header Banner
        val headerPaint = Paint().apply {
            color = Color.rgb(0, 104, 122) // Medical Teal #00687A
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 75f, headerPaint)

        // DermaVision Official App Logo Emblem (Beside Name in Header)
        drawDermaVisionLogo(context, canvas, 26f, 16f, 70f, 60f)

        // DermaVision Title (Beside Logo)
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DermaVision", 78f, 42f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Skin Disease Analysis Report", 78f, 58f, paint)

        // Report ID / Date in Header
        val reportId = "DVR-${result.timestamp.toString().takeLast(6)}"
        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 10f
        canvas.drawText("Report ID: $reportId", 565f, 40f, paint)
        canvas.drawText("Date: $formattedDate", 565f, 56f, paint)
        paint.textAlign = Paint.Align.LEFT

        var currentY = 95f

        // Optional Low Confidence Banner
        if (result.isLowConfidence) {
            val lowConfBg = Paint().apply {
                color = Color.rgb(255, 248, 225)
                style = Paint.Style.FILL
            }
            val lowConfBorder = Paint().apply {
                color = Color.rgb(255, 179, 0)
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
            }
            val lowConfRect = RectF(30f, currentY, 565f, currentY + 38f)
            canvas.drawRoundRect(lowConfRect, 6f, 6f, lowConfBg)
            canvas.drawRoundRect(lowConfRect, 6f, 6f, lowConfBorder)

            paint.color = Color.rgb(180, 80, 0)
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("⚠️ LOW CONFIDENCE SCREENING NOTICE", 45f, currentY + 16f, paint)

            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val lowConfDesc = "Automated confidence is low (${result.confidencePercentage}%). Overlapping visual characteristics detected. In-person clinical exam strongly advised."
            canvas.drawText(lowConfDesc, 45f, currentY + 29f, paint)

            currentY += 48f
        }

        // 2. Subject Info Box
        val infoBgPaint = Paint().apply {
            color = Color.rgb(244, 248, 250)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + 48f), 8f, 8f, infoBgPaint)

        paint.color = Color.rgb(0, 50, 60)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Applicant Name: $patientName", 45f, currentY + 20f, paint)
        canvas.drawText("Assessment: Skin Disease Screening", 45f, currentY + 36f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(100, 110, 115)
        canvas.drawText("Privacy: On-Device Analysis", 330f, currentY + 20f, paint)
        canvas.drawText("Application: DermaVision", 330f, currentY + 36f, paint)

        currentY += 62f

        // 3. Image and Primary Prediction Side-by-Side
        val imageBitmap = bitmap ?: result.imagePath?.let { path ->
            try { BitmapFactory.decodeFile(path) } catch (e: Exception) { null }
        }

        if (imageBitmap != null) {
            val destRect = RectF(30f, currentY, 170f, currentY + 135f)
            val srcRect = Rect(0, 0, imageBitmap.width, imageBitmap.height)
            canvas.drawBitmap(imageBitmap, srcRect, destRect, paint)

            // Border around image
            val borderPaint = Paint().apply {
                color = Color.rgb(200, 215, 220)
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas.drawRoundRect(destRect, 6f, 6f, borderPaint)
        } else {
            val placeholderPaint = Paint().apply {
                color = Color.rgb(235, 240, 242)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(RectF(30f, currentY, 170f, currentY + 135f), 6f, 6f, placeholderPaint)
            paint.color = Color.rgb(120, 130, 135)
            paint.textSize = 10f
            canvas.drawText("Image Analyzed", 55f, currentY + 70f, paint)
        }

        // Primary Prediction Card (Right of image)
        val predCardPaint = Paint().apply {
            color = Color.rgb(230, 245, 248)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(185f, currentY, 565f, currentY + 135f), 8f, 8f, predCardPaint)

        paint.color = Color.rgb(0, 104, 122)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PRIMARY DETECTED CONDITION", 200f, currentY + 24f, paint)

        paint.color = Color.rgb(15, 30, 35)
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val dispName = if (result.primaryClass.displayName.length > 34) result.primaryClass.displayName.take(32) + "..." else result.primaryClass.displayName
        canvas.drawText(dispName, 200f, currentY + 48f, paint)

        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(60, 75, 80)
        canvas.drawText("Category: ${result.primaryClass.category.label}", 200f, currentY + 68f, paint)
        canvas.drawText("Risk Indicator: ${result.primaryClass.riskLevel.label}", 200f, currentY + 85f, paint)

        // Confidence Pill
        val confPillPaint = Paint().apply {
            color = if (result.isLowConfidence) Color.rgb(230, 120, 20) else Color.rgb(0, 104, 122)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(200f, currentY + 98f, 360f, currentY + 122f), 12f, 12f, confPillPaint)
        paint.color = Color.WHITE
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val confLabel = if (result.isLowConfidence) "Match: ${result.confidencePercentage}% (Low Conf)" else "Match Likelihood: ${result.confidencePercentage}%"
        canvas.drawText(confLabel, 212f, currentY + 114f, paint)

        currentY += 148f

        // 4. Differential Class Probabilities Table
        paint.color = Color.rgb(0, 50, 60)
        paint.textSize = 11.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Top Differential Condition Probabilities", 30f, currentY, paint)

        currentY += 14f
        // Table Header
        val tableHeaderPaint = Paint().apply {
            color = Color.rgb(235, 240, 242)
            style = Paint.Style.FILL
        }
        canvas.drawRect(30f, currentY, 565f, currentY + 20f, tableHeaderPaint)

        paint.color = Color.rgb(60, 70, 75)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("RANK", 40f, currentY + 14f, paint)
        canvas.drawText("CONDITION CLASS", 90f, currentY + 14f, paint)
        canvas.drawText("CATEGORY", 350f, currentY + 14f, paint)
        canvas.drawText("PROBABILITY", 480f, currentY + 14f, paint)

        currentY += 20f

        // Table Rows
        result.topPredictions.take(3).forEachIndexed { index, item ->
            val rowBgPaint = Paint().apply {
                color = if (index % 2 == 0) Color.rgb(250, 252, 253) else Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawRect(30f, currentY, 565f, currentY + 20f, rowBgPaint)

            paint.color = Color.rgb(30, 40, 45)
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, if (index == 0) Typeface.BOLD else Typeface.NORMAL)

            canvas.drawText("#${index + 1}", 40f, currentY + 14f, paint)
            val nameDisplay = if (item.diseaseClass.displayName.length > 40) item.diseaseClass.displayName.take(38) + "..." else item.diseaseClass.displayName
            canvas.drawText(nameDisplay, 90f, currentY + 14f, paint)
            canvas.drawText(item.diseaseClass.category.label, 350f, currentY + 14f, paint)
            canvas.drawText("${item.percentage}%", 480f, currentY + 14f, paint)

            currentY += 20f
        }

        currentY += 12f

        // 5. Severity Status Section (Dynamically wrapped to avoid overflow)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        val wrappedSeverityLines = wrapText(result.severityStatus, 490f, paint)
        val severityBoxHeight = (28f + wrappedSeverityLines.size * 13f).coerceAtLeast(42f)

        val severityBgPaint = Paint().apply {
            color = Color.rgb(245, 245, 248)
            style = Paint.Style.FILL
        }
        val severityBorderPaint = Paint().apply {
            color = Color.rgb(220, 225, 230)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val severityRect = RectF(30f, currentY, 565f, currentY + severityBoxHeight)
        canvas.drawRoundRect(severityRect, 6f, 6f, severityBgPaint)
        canvas.drawRoundRect(severityRect, 6f, 6f, severityBorderPaint)

        paint.color = Color.rgb(70, 80, 90)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Severity Assessment Note:", 45f, currentY + 16f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(80, 90, 100)
        paint.textSize = 8.5f
        var sevTextY = currentY + 30f
        wrappedSeverityLines.forEach { sLine ->
            canvas.drawText(sLine, 45f, sevTextY, paint)
            sevTextY += 12f
        }

        currentY += severityBoxHeight + 14f

        // 6. Educational Information & Guidance
        paint.color = Color.rgb(0, 50, 60)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Educational Overview: ${result.primaryClass.shortName}", 30f, currentY, paint)

        currentY += 16f
        paint.color = Color.rgb(40, 50, 55)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        val descLines = wrapText(result.primaryClass.detailedDescription, 500f, paint)
        descLines.forEach { line ->
            canvas.drawText(line, 30f, currentY, paint)
            currentY += 13f
        }

        currentY += 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Precautionary & Consultation Advice:", 30f, currentY, paint)
        currentY += 14f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val adviceLines = wrapText(result.primaryClass.whenToSeeDoctor, 500f, paint)
        adviceLines.forEach { line ->
            canvas.drawText(line, 30f, currentY, paint)
            currentY += 13f
        }

        currentY += 15f

        // 7. Mandatory Medical Disclaimer Box
        val disclaimerBgPaint = Paint().apply {
            color = Color.rgb(255, 243, 224) // Warm amber warning tint
            style = Paint.Style.FILL
        }
        val disclaimerBorderPaint = Paint().apply {
            color = Color.rgb(255, 179, 0)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val disclaimerRect = RectF(30f, 720f, 565f, 805f)
        canvas.drawRoundRect(disclaimerRect, 6f, 6f, disclaimerBgPaint)
        canvas.drawRoundRect(disclaimerRect, 6f, 6f, disclaimerBorderPaint)

        paint.color = Color.rgb(191, 54, 12)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("IMPORTANT MEDICAL DISCLAIMER", 45f, 738f, paint)

        paint.color = Color.rgb(90, 50, 20)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        val disclaimerText = "DermaVision is a clinical dermatology screening and educational application. It does not provide a medical diagnosis and should not replace consultation with a qualified healthcare professional. Never ignore professional medical advice or delay seeking it because of something you read on this report."
        val discLines = wrapText(disclaimerText, 490f, paint)
        var dy = 754f
        discLines.forEach { line ->
            canvas.drawText(line, 45f, dy, paint)
            dy += 12f
        }

        // Clean Footer with DermaVision Logo
        drawDermaVisionLogo(context, canvas, 186f, 814f, 202f, 830f)

        paint.color = Color.rgb(120, 135, 140)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("DermaVision • Skin Disease Analysis Report", 208f, 825f, paint)

        pdfDocument.finishPage(page)

        // Save PDF to documents directory
        val docsDir = File(context.cacheDir, "reports")
        if (!docsDir.exists()) docsDir.mkdirs()

        val pdfFile = File(docsDir, "DermaVision_Report_${result.timestamp}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        pdfFile
    }

    private fun drawDermaVisionLogo(context: Context, canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        try {
            // Draw background container rounded rect
            val width = right - left
            val cornerRadius = width * 0.22f
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(0, 106, 106)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(RectF(left, top, right, bottom), cornerRadius, cornerRadius, bgPaint)

            // Try to load and draw launcher foreground or full launcher drawable
            val foregroundDrawable = androidx.core.content.ContextCompat.getDrawable(context, com.example.R.drawable.ic_launcher_foreground)
            if (foregroundDrawable != null) {
                foregroundDrawable.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
                foregroundDrawable.draw(canvas)
                return
            }
        } catch (e: Exception) {
            // Fallback drawing if needed
        }

        // Geometric fallback logo if vector loading is unavailable
        val cx = (left + right) / 2f
        val cy = (top + bottom) / 2f
        val radius = (right - left) / 2.2f
        val crossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(cx - radius * 0.7f, cy - radius * 0.22f, cx + radius * 0.7f, cy + radius * 0.22f), 1.5f, 1.5f, crossPaint)
        canvas.drawRoundRect(RectF(cx - radius * 0.22f, cy - radius * 0.7f, cx + radius * 0.22f, cy + radius * 0.7f), 1.5f, 1.5f, crossPaint)
    }

    fun sharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "DermaVision Skin Disease Analysis Report")
            putExtra(Intent.EXTRA_TEXT, "Attached is the DermaVision skin disease analysis report.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share DermaVision PDF Report")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun wrapText(text: String, maxWidth: Float, paint: Paint): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return lines
    }
}
