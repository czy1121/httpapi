@file:Suppress("NOTHING_TO_INLINE")

package me.reezy.cosmo.httpapi

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.await
import retrofit2.awaitResponse

inline fun <reified T : Any> api(): T = Api.getService(T::class.java)


inline fun <T> Call<T>.onSuccess(fragment: Fragment, noinline callback: (T) -> Unit = {}) = onSuccess(fragment.lifecycleScope, callback)

inline fun <T> Call<T>.onSuccess(activity: FragmentActivity, noinline callback: (T) -> Unit = {}) = onSuccess(activity.lifecycleScope, callback)

fun <T> Call<T>.onSuccess(scope: CoroutineScope, callback: (T) -> Unit = {}) = ApiCall(this, scope, Api.errorHandler) {
    if (!it.isSuccessful) {
        throw HttpException(it)
    }
    callback(it.body() ?: throw KotlinNullPointerException("response body was null"))
}

inline fun <T> Call<T>.onResponse(fragment: Fragment, noinline callback: ((Response<T>) -> Unit)? = null) = ApiCall(this, fragment.lifecycleScope, Api.errorHandler, callback)

inline fun <T> Call<T>.onResponse(activity: FragmentActivity, noinline callback: ((Response<T>) -> Unit)? = null) = ApiCall(this, activity.lifecycleScope, Api.errorHandler, callback)

inline fun <T> Call<T>.onResponse(scope: CoroutineScope, noinline callback: ((Response<T>) -> Unit)? = null) = ApiCall(this, scope, Api.errorHandler, callback)



suspend fun <T: Any> Call<T>.getOrNull(): T? {
    return try {
        await()
    } catch (throwable: Throwable) {
        Api.errorHandler?.invoke(throwable)
        null
    }
}


