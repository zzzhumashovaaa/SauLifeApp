package com.example.saulifeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.saulifeapp.data.local.entity.ScanMedicineEntity

@Dao
interface ScanMedicineDao {

    @Insert
    suspend fun insertAll(items: List<ScanMedicineEntity>)

    @Query("SELECT * FROM scan_medicines WHERE scanId = :scanId")
    suspend fun getMedicinesForScan(scanId: Int): List<ScanMedicineEntity>
}