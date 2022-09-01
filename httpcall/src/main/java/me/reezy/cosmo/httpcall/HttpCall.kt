@file:Suppress("NOTHING_TO_INLINE")

package me.reezy.cosmo.httpcall

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.awaitResponse

class HttpCall<T>(call: Call<T>, scope: CoroutineScope, callback: ((Response<T>) -> Unit)? = null) {
    private var job: Job? = null
    private var failure: ((Throwable) -> Unit)? = null
    private val finally: MutableList<() -> Unit> = mutableListOf()

    init {

        job = scope.launch(context = Dispatchers.Main) {
            try {
                val response = call.awaitResponse()
                callback?.invoke(response)
            } catch (throwable: Throwable) {
                globalErrorHandler?.invoke(throwable)
                failure?.invoke(throwable)
            } finally {
                finally.forEach {
                    try {
                        it.invoke()
                    } catch (ex: Throwable) {
                        ex.printStackTrace()
                    }
                }
            }
        }
    }

    fun onFailure(block: (Throwable) -> Unit): HttpCall<T> {
        this.failure = block
        return this
    }

    fun onFinally(block: () -> Unit): HttpCall<T> {
        this.finally.add(block)
        return this
    }

    fun cancel() {
        job?.cancel()
    }
}


inline fun <T> Call<T>.onSuccess(fragment: Fragment, noinline callback: (T) -> Unit) = onSuccess(fragment.lifecycleScope, callback)
inline fun <T> Call<T>.onSuccess(activity: FragmentActivity, noinline callback: (T) -> Unit) = onSuccess(activity.lifecycleScope, callback)

fun <T> Call<T>.onSuccess(scope: CoroutineScope, callback: (T) -> Unit) = HttpCall(this, scope) {
    if (!it.isSuccessful) {
        throw HttpException(it)
    }
    callback(it.body() ?: throw KotlinNullPointerException("response body was null"))
}

inline fun <T> Call<T>.onResponse(fragment: Fragment, noinline callback: (Response<T>) -> Unit) = HttpCall(this, fragment.lifecycleScope, callback)
inline fun <T> Call<T>.onResponse(activity: FragmentActivity, noinline callback: (Response<T>) -> Unit) = HttpCall(this, activity.lifecycleScope, callback)
inline fun <T> Call<T>.onResponse(scope: CoroutineScope, noinline callback: ((Response<T>) -> Unit)? = null) = HttpCall(this, scope, callback)