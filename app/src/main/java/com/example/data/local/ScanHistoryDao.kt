package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {

    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentScans(limit: Int): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history WHERE id = :id LIMIT 1")
    suspend fun getScanById(id: Long): ScanHistoryEntity?

    @Query("SELECT COUNT(*) FROM scan_history")
    fun getScanCount(): Flow<Int>

    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC LIMIT 1")
    fun getLatestScan(): Flow<ScanHistoryEntity?>

    @Query("SELECT * FROM scan_history WHERE predictedClassIndex = :classIndex ORDER BY timestamp DESC")
    fun getScansByClass(classIndex: Int): Flow<List<ScanHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanHistoryEntity): Long

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteScanById(id: Long)

    @Query("DELETE FROM scan_history")
    suspend fun clearAll()
}
