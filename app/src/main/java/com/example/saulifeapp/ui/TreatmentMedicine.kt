package com.example.saulifeapp.ui.treatment

data class TreatmentMedicine(
    var id: String = "",
    val name: String = "",
    val dosage: String = "",
    val quantity: Int = 0,
    val expiryDate: String = "",
    val category: String = "",
    val isCurrent: Boolean = false,
    val time: String = "",
    val timesPerDay: Int = 0,
    val startDate: String = "",
    val durationDays: Int = 0,
    val notes: String = "",
    val takenToday: Boolean = false
)