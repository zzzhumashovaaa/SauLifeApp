package com.example.saulifeapp.data.remote.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EgovApiService {

    @GET("mapping/{dataset}/{version}")
    suspend fun getMapping(
        @Path("dataset") dataset: String,
        @Path("version") version: String
    ): ResponseBody

    @GET("{dataset}/{version}")
    suspend fun getDatasetRaw(
        @Path("dataset") dataset: String,
        @Path("version") version: String,
        @Query("source") source: String
    ): ResponseBody
}