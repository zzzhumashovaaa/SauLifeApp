package com.example.saulifeapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFdaApi {

    @GET("drug/ndc.json")
    suspend fun searchMedicines(
        @Query("search") query: String,
        @Query("limit") limit: Int = 10
    ): OpenFdaNdcResponse
}