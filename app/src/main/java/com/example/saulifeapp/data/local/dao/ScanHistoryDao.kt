package com.example.saulifeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.saulifeapp.data.local.entity.ScanHistoryEntity

@Dao
interface ScanHistoryDao {

    @Insert
    suspend fun insertScan(scan: ScanHistoryEntity): Long

    @Query("SELECT * FROM scan_history ORDER BY createdAt DESC")
    suspend fun getAllScans(): List<ScanHistoryEntity>
}
