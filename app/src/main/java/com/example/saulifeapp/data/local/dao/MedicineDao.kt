package com.example.saulifeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.saulifeapp.data.local.entity.MedicineEntity

@Dao
interface MedicineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medicines: List<MedicineEntity>)

    @Query("SELECT * FROM medicines ORDER BY nameOfficial ASC")
    suspend fun getAllMedicines(): List<MedicineEntity>

    @Query("SELECT COUNT(*) FROM medicines")
    suspend fun getCount(): Int

    @Query("""
        SELECT * FROM medicines
        WHERE normalizedName LIKE '%' || :query || '%'
           OR nameOfficial LIKE '%' || :originalQuery || '%'
        ORDER BY nameOfficial ASC
        LIMIT 20
    """)
    suspend fun findSimilar(query: String, originalQuery: String): List<MedicineEntity>

    @Query("DELETE FROM medicines")
    suspend fun clearAll()
}