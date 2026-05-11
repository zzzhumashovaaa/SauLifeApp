package com.example.saulifeapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.saulifeapp.data.local.dao.MedicineDao
import com.example.saulifeapp.data.local.dao.ScanHistoryDao
import com.example.saulifeapp.data.local.dao.ScanMedicineDao
import com.example.saulifeapp.data.local.entity.MedicineEntity
import com.example.saulifeapp.data.local.entity.ScanHistoryEntity
import com.example.saulifeapp.data.local.entity.ScanMedicineEntity

@Database(
    entities = [
        MedicineEntity::class,
        ScanHistoryEntity::class,
        ScanMedicineEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicineDao(): MedicineDao
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun scanMedicineDao(): ScanMedicineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "saulife_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}