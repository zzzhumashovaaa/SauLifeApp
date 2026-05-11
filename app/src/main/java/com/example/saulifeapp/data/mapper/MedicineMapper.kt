package com.example.saulifeapp.data.mapper

import com.example.saulifeapp.data.local.entity.MedicineEntity
import com.example.saulifeapp.data.remote.dto.EgovMedicineDto

object MedicineMapper {

    fun fromDto(dto: EgovMedicineDto): MedicineEntity? {
        val rawName = dto.tradeName ?: dto.name ?: return null
        val cleanName = rawName.trim()

        if (cleanName.isBlank()) return null

        return MedicineEntity(
            id = dto.id ?: cleanName.lowercase().replace(" ", "_"),
            nameOfficial = cleanName,
            normalizedName = normalize(cleanName),
            dosage = dto.dosage?.trim(),
            form = dto.form?.trim(),
            manufacturer = dto.manufacturer?.trim(),
            source = "egov"
        )
    }

    private fun normalize(text: String): String {
        return text
            .trim()
            .lowercase()
            .replace(Regex("[^\\p{L}\\p{N}\\s\\-]"), "")
            .replace(Regex("\\s+"), " ")
    }
}