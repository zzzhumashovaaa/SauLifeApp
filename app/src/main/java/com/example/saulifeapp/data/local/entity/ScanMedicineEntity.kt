package com.example.saulifeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_medicines")
data class ScanMedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val scanId: Int,
    val detectedText: String,
    val medicineName: String,
    val isConfirmed: Boolean = false
)