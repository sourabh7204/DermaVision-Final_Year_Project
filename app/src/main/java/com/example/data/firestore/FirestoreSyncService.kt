package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.data.auth.UserSession
import com.example.data.local.ScanHistoryEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreSyncService(private val context: Context) {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("FirestoreSyncService", "Firestore initialization unavailable: ${e.localizedMessage}")
            null
        }
    }

    suspend fun syncUserProfile(user: UserSession): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        if (user.isGuest) return@withContext false

        try {
            val userMap = hashMapOf(
                "uid" to user.uid,
                "email" to user.email,
                "displayName" to user.displayName,
                "profilePictureUri" to (user.profilePictureUri ?: ""),
                "isGuest" to user.isGuest,
                "createdAt" to user.createdAt,
                "lastLoginAt" to System.currentTimeMillis()
            )
            db.collection("users").document(user.uid)
                .set(userMap, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.w("FirestoreSyncService", "Failed to sync user profile: ${e.localizedMessage}")
            false
        }
    }

    suspend fun syncScanRecord(uid: String, scan: ScanHistoryEntity): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        if (uid.isBlank() || uid.startsWith("guest_") || uid.startsWith("local_")) return@withContext false

        try {
            val scanMap = hashMapOf(
                "id" to scan.id,
                "predictedClassName" to scan.predictedClassName,
                "predictedClassIndex" to scan.predictedClassIndex,
                "confidence" to scan.confidence,
                "severityStatus" to scan.severityStatus,
                "topPredictionsJson" to scan.topPredictionsJson,
                "modelVersion" to scan.modelVersion,
                "timestamp" to scan.timestamp,
                "notes" to scan.notes,
                "isSynced" to true
            )
            db.collection("users").document(uid)
                .collection("scans").document(scan.id.toString())
                .set(scanMap, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.w("FirestoreSyncService", "Failed to sync scan record: ${e.localizedMessage}")
            false
        }
    }

    suspend fun deleteScanRecord(uid: String, scanId: Long): Boolean = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext false
        if (uid.isBlank() || uid.startsWith("guest_") || uid.startsWith("local_")) return@withContext false

        try {
            db.collection("users").document(uid)
                .collection("scans").document(scanId.toString())
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.w("FirestoreSyncService", "Failed to delete scan record from cloud: ${e.localizedMessage}")
            false
        }
    }
}
