package com.example.saulifeapp.data.repository

import android.util.Log
import com.example.saulifeapp.data.local.dao.MedicineDao
import com.example.saulifeapp.data.local.dao.ScanHistoryDao
import com.example.saulifeapp.data.local.dao.ScanMedicineDao
import com.example.saulifeapp.data.local.entity.MedicineEntity
import com.example.saulifeapp.data.local.entity.ScanHistoryEntity
import com.example.saulifeapp.data.local.entity.ScanMedicineEntity

class MedicineRepository(
    private val medicineDao: MedicineDao,
    private val scanHistoryDao: ScanHistoryDao,
    private val scanMedicineDao: ScanMedicineDao
) {

    suspend fun syncMedicinesFromEgov(apiKey: String): Result<Int> {
        return try {
            Log.e("EGOV_SYNC", "syncMedicinesFromEgov called, apiKey = $apiKey")
            Result.success(0)
        } catch (e: Exception) {
            Log.e("EGOV_SYNC", "FAILED", e)
            Result.failure(e)
        }
    }

    suspend fun getAllMedicines(): List<MedicineEntity> =
        medicineDao.getAllMedicines()

    suspend fun getMedicineCount(): Int =
        medicineDao.getCount()

    suspend fun findMatches(query: String, originalQuery: String): List<MedicineEntity> =
        medicineDao.findSimilar(query, originalQuery)

    suspend fun saveScan(rawText: String, detectedMedicines: List<String>): Int {
        val scanId = scanHistoryDao.insertScan(
            ScanHistoryEntity(rawText = rawText)
        ).toInt()

        val scanItems = detectedMedicines.map {
            ScanMedicineEntity(
                scanId = scanId,
                detectedText = it,
                medicineName = it
            )
        }

        scanMedicineDao.insertAll(scanItems)
        return scanId
    }

    suspend fun getMedicinesForScan(scanId: Int): List<ScanMedicineEntity> =
        scanMedicineDao.getMedicinesForScan(scanId)
}