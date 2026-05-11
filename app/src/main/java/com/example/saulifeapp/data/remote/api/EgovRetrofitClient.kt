package com.example.saulifeapp.data.remote.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object EgovRetrofitClient {

    private const val BASE_URL = "https://data.egov.kz/api/v4/"

    val api: EgovApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EgovApiService::class.java)
    }
}