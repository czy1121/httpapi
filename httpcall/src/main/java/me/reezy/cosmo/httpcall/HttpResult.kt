@file:Suppress("NOTHING_TO_INLINE")

package me.reezy.cosmo.httpcall


sealed class HttpResult<T> {

    data class Success<T>(val value: T) : HttpResult<T>()
    data class Failure<T>(val exception: Throwable) : HttpResult<T>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
}


inline fun <T> HttpResult<T>.onSuccess(action: (T) -> Unit): HttpResult<T> {
    if (this is HttpResult.Success) {
        action(value)
    }
    return this
}

inline fun <T> HttpResult<T>.onFailure(action: (Throwable) -> Unit): HttpResult<T> {
    if (this is HttpResult.Failure) {
        action(exception)
    }
    return this
}

inline fun <T> HttpResult<T>.getOrNull(): T? = if (this is HttpResult.Success) value else null
inline fun <T> HttpResult<T>.getOrElse(transformOnFailure: (Throwable) -> T): T = when (this) {
    is HttpResult.Success -> value
    is HttpResult.Failure -> transformOnFailure(exception)
}
