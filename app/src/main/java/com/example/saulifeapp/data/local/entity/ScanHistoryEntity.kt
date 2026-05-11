package com.example.saulifeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val scanId: Int = 0,
    val rawText: String,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)