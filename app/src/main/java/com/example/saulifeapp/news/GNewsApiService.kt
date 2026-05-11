package com.example.saulifeapp.news

import retrofit2.http.GET
import retrofit2.http.Query

interface GNewsApiService {

    @GET("search")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("lang") lang: String = "ru",
        @Query("country") country: String? = null,
        @Query("max") max: Int = 10,
        @Query("sortby") sortBy: String = "publishedAt",
        @Query("apikey") apiKey: String
    ): GNewsResponse
}