package com.example.saulifeapp.ui.medicine

import java.io.Serializable

data class Medicine(
    val id: Int,
    val name: String,
    val type: String,
    val dosage: String,
    val forWhom: String,
    val purpose: String,
    val whenToTake: String,
    val instruction: String,
    val contraindications: String,
    val price: Double
) : Serializable