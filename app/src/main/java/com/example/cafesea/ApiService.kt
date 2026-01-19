package com.example.cafesea

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("food")
    fun getFoodItems(): Call<List<CafeModel>>
}