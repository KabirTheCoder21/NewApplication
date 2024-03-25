package com.example.application

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder().baseUrl("https://fatema.takatakind.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
     val apiInterface by lazy{
        retrofit.create(ApiInterface::class.java)
    }
}