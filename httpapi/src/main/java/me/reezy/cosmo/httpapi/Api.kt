package me.reezy.cosmo.httpapi

import retrofit2.Retrofit

object Api {
    private lateinit var default: Lazy<ApiRetrofit>

    private var defaultErrorHandler: ((Throwable) -> Unit)? = null

    fun setRetrofitProvider(provider: () -> Retrofit) {
        default = lazy { ApiRetrofit(provider()) }
    }

    fun setErrorHandler(handler: (Throwable) -> Unit) {
        defaultErrorHandler = handler
    }

    val errorHandler: ((Throwable) -> Unit)? get() = defaultErrorHandler

    val baseUrl: String get() = default.value.baseUrl

    fun <T : Any> getService(clazz: Class<T>): T = default.value.getService(clazz)
}

