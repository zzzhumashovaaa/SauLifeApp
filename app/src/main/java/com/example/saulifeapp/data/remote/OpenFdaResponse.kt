package com.example.saulifeapp.data.remote

data class OpenFdaNdcResponse(
    val results: List<OpenFdaNdcResult>? = null
)

data class OpenFdaNdcResult(
    val brand_name: String? = null,
    val generic_name: String? = null,
    val dosage_form: String? = null,
    val active_ingredients: List<ActiveIngredient>? = null
)

data class ActiveIngredient(
    val name: String? = null,
    val strength: String? = null
)