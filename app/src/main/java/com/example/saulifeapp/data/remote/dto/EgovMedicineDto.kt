package com.example.saulifeapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EgovMedicineDto(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("trade_name")
    val tradeName: String? = null,

    @SerializedName("dosage")
    val dosage: String? = null,

    @SerializedName("form")
    val form: String? = null,

    @SerializedName("manufacturer")
    val manufacturer: String? = null
)