package com.example.saulifeapp.ui.reminders

data class ReminderItem(
    val id: Int,
    val medicineName: String,
    val dosage: String,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true
)