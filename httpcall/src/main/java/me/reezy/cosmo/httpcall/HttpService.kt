package me.reezy.cosmo.httpcall

import retrofit2.Retrofit

inline fun <reified T : Any> http() = HttpService.getService(T::class.java)

internal var globalErrorHandler: ((Throwable) -> Unit)? = null

object HttpService {
    private val retrofit by lazy { retrofitProvider() }

    private var services = mutableMapOf<Class<*>, Any>()

    private lateinit var retrofitProvider: () -> Retrofit

    fun setRetrofitProvider(factory: () -> Retrofit) {
        retrofitProvider = factory
    }

    fun setErrorHandler(handler: (Throwable) -> Unit) {
        globalErrorHandler = handler
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getService(clazz: Class<T>): T = services.getOrPut(clazz) { retrofit.create(clazz) } as T

}
