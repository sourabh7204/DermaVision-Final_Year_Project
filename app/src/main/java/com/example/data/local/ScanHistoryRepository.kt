package com.example.data.local

import android.content.Context
import android.graphics.Bitmap
import com.example.data.firestore.FirestoreSyncService
import com.example.data.model.PredictionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ScanHistoryRepository(private val dao: ScanHistoryDao, private val context: Context) {

    private val firestoreSync = FirestoreSyncService(context)

    val allScans: Flow<List<ScanHistoryEntity>> = dao.getAllScans()
    val scanCount: Flow<Int> = dao.getScanCount()
    val latestScan: Flow<ScanHistoryEntity?> = dao.getLatestScan()

    fun getRecentScans(limit: Int = 5): Flow<List<ScanHistoryEntity>> = dao.getRecentScans(limit)

    fun getScansByClass(classIndex: Int): Flow<List<ScanHistoryEntity>> = dao.getScansByClass(classIndex)

    suspend fun getScanById(id: Long): ScanHistoryEntity? = dao.getScanById(id)

    suspend fun saveScan(result: PredictionResult, bitmap: Bitmap?, notes: String = "", currentUid: String = ""): Long = withContext(Dispatchers.IO) {
        val savedImagePath = if (bitmap != null) {
            saveBitmapToInternalStorage(bitmap)
        } else {
            result.imagePath ?: ""
        }

        val entity = ScanHistoryEntity.fromPredictionResult(
            result = result.copy(imagePath = savedImagePath),
            imagePath = savedImagePath,
            notes = notes
        )
        val id = dao.insertScan(entity)

        if (currentUid.isNotBlank()) {
            firestoreSync.syncScanRecord(currentUid, entity.copy(id = id))
        }
        id
    }

    suspend fun deleteScan(scan: ScanHistoryEntity, currentUid: String = "") = withContext(Dispatchers.IO) {
        if (scan.imageFilePath.isNotEmpty()) {
            try {
                val file = File(scan.imageFilePath)
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                // ignore deletion error for image file
            }
        }
        dao.deleteScanById(scan.id)
        if (currentUid.isNotBlank()) {
            firestoreSync.deleteScanRecord(currentUid, scan.id)
        }
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.clearAll()
    }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap): String {
        val filename = "scan_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
        val imagesDir = File(context.filesDir, "scans")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val file = File(imagesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }
}
