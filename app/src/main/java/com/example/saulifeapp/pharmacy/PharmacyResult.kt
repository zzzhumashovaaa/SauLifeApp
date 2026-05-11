package com.example.saulifeapp.pharmacy

data class PharmacyResult(
    val medicineName: String,
    val dosage: String,
    val pharmacyName: String,
    val address: String,
    val price: Double,
    val distance: String,
    val available: Boolean
)