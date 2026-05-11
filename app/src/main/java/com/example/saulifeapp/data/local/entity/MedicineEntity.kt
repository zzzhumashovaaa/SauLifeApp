package com.example.saulifeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey
    val id: String,
    val nameOfficial: String,
    val normalizedName: String,
    val dosage: String? = null,
    val form: String? = null,
    val manufacturer: String? = null,
    val source: String = "egov"
)