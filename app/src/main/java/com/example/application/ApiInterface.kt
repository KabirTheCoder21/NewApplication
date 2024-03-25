package com.example.application

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiInterface {
   @GET("app_api/index.php?p=showAllVideos")
   suspend fun getVideos(): Response<VideoResponse>
}
