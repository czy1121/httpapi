package com.demo.app

import retrofit2.Call
import retrofit2.http.GET

interface TestApi {
    @GET("https://httpbin.org/get")
    fun call(): Call<HttpBin>
}