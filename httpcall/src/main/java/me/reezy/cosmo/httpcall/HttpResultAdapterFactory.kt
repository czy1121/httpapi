package me.reezy.cosmo.httpcall

import okhttp3.Request
import okio.Timeout
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class HttpResultAdapterFactory() : CallAdapter.Factory() {


    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {

        val rawType = getRawType(returnType)

        if (rawType != Call::class.java) return null

        check(returnType is ParameterizedType) {
            "return type must be parameterized as Result<Foo> or Result<out Foo>"
        }

        val responseType = getParameterUpperBound(0, returnType)

        if (getRawType(responseType) != HttpResult::class.java) return null

        return object : CallAdapter<Any, Call<*>?> {
            override fun responseType(): Type = responseType

            override fun adapt(call: Call<Any>): Call<Any> = CallbackCall(call)
        }
    }

    private class CallbackCall(private val delegate: Call<Any>) : Call<Any> {

        override fun enqueue(callback: Callback<Any>): Unit = delegate.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                try {
                    if (!response.isSuccessful) {
                        throw HttpException(response)
                    }
                    val body = response.body() ?: throw KotlinNullPointerException("response body was null")
                    callback.onResponse(this@CallbackCall, Response.success(HttpResult.Success(body), response.raw()))
                } catch (throwable: Throwable) {
                    globalErrorHandler?.invoke(throwable)
                    callback.onResponse(this@CallbackCall, Response.success(HttpResult.Failure<Any>(throwable), response.raw()))
                }
            }

            override fun onFailure(call: Call<Any>, throwable: Throwable) {
                if (call.isCanceled) {
                    return
                }
                globalErrorHandler?.invoke(throwable)
                callback.onResponse(this@CallbackCall, Response.success(HttpResult.Failure<Any>(throwable)))
            }
        })

        override fun clone(): Call<Any> = CallbackCall(delegate)

        override fun execute(): Response<Any> = throw UnsupportedOperationException("${this.javaClass.name} doesn't support execute")

        override fun isExecuted(): Boolean = delegate.isExecuted
        override fun cancel(): Unit = delegate.cancel()
        override fun isCanceled(): Boolean = delegate.isCanceled
        override fun request(): Request = delegate.request()
        override fun timeout(): Timeout = delegate.timeout()

    }
}