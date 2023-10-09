package me.reezy.cosmo.httpapi

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ResultCallAdapterFactory(var errorHandler: ((Throwable) -> Unit)? = null) : CallAdapter.Factory() {

    override fun get(returnType: Type, annotations: Array<out Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java || returnType !is ParameterizedType) {
            return null
        }
        val responseType = getParameterUpperBound(0, returnType)

        return if (responseType is ParameterizedType && responseType.rawType == Result::class.java) {
            object : CallAdapter<Any, Call<Result<*>>> {
                override fun responseType(): Type = getParameterUpperBound(0, responseType)

                override fun adapt(call: Call<Any>): Call<Result<*>> = CallbackCall(call, errorHandler)
            }
        } else {
            null
        }

    }

    private class CallbackCall(private val delegate: Call<Any>, var errorHandler: ((Throwable) -> Unit)? = null) : Call<Result<*>> {

        override fun enqueue(callback: Callback<Result<*>>): Unit = delegate.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                try {
                    if (!response.isSuccessful) {
                        throw HttpException(response)
                    }
                    val body = response.body() ?: throw KotlinNullPointerException("response body was null")
                    callback.onResponse(this@CallbackCall, Response.success(Result.success(body), response.raw()))
                } catch (throwable: Throwable) {
                    errorHandler?.invoke(throwable)
                    callback.onResponse(this@CallbackCall, Response.success(Result.failure<Result<*>>(throwable), response.raw()))
                }
            }

            override fun onFailure(call: Call<Any>, throwable: Throwable) {
                if (call.isCanceled) {
                    return
                }
                errorHandler?.invoke(throwable)
                callback.onResponse(this@CallbackCall, Response.success(Result.failure<Result<*>>(throwable)))
            }
        })

        override fun clone(): Call<Result<*>> = CallbackCall(delegate)

        override fun execute(): Response<Result<*>> = throw UnsupportedOperationException("${this.javaClass.name} doesn't support execute")

        override fun isExecuted(): Boolean = delegate.isExecuted
        override fun cancel(): Unit = delegate.cancel()
        override fun isCanceled(): Boolean = delegate.isCanceled
        override fun request(): Request = delegate.request()
        override fun timeout(): Timeout = delegate.timeout()

    }
}