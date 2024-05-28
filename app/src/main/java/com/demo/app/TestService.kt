package com.demo.app

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface TestService {

    @GET("https://httpbin.org/get")
    fun call(): Call<HttpBin>

    // 禁止在主线程调用，需要try catch处理异常
    @GET("https://httpbin.org/get")
    fun response(): Response<HttpBin>

}