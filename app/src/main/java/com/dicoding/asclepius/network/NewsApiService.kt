package com.dicoding.asclepius.network

import com.dicoding.asclepius.model.NewsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("v2/top-headlines")
    fun getCancerNews(
        @Query("q") query: String = "cancer",
        @Query("category") category: String = "health",
        @Query("language") language: String = "en",
        @Query("apiKey") apiKey: String
    ): Call<NewsResponse>
}
