package me.reezy.cosmo.httpapi

import retrofit2.Retrofit

class ApiRetrofit(private val retrofit: Retrofit) {

    val baseUrl: String get() = retrofit.baseUrl().toString()

    private var services = mutableMapOf<Class<*>, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getService(clazz: Class<T>): T = services.getOrPut(clazz) { retrofit.create(clazz) } as T
}

