package me.reezy.cosmo.httpapi

import retrofit2.Retrofit

object Api {
    private lateinit var default: ApiRetrofit

    private var defaultErrorHandler: ((Throwable) -> Unit)? = null

    fun setRetrofitProvider(provider: () -> Retrofit) {
        default = ApiRetrofit(provider())
    }

    fun setErrorHandler(handler: (Throwable) -> Unit) {
        defaultErrorHandler = handler
    }

    val errorHandler: ((Throwable) -> Unit)? get() = defaultErrorHandler

    val baseUrl: String get() = default.baseUrl

    fun <T : Any> getService(clazz: Class<T>): T = default.getService(clazz)
}

