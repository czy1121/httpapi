package me.reezy.cosmo.httpapi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import retrofit2.awaitResponse

class ApiCall<T>(call: Call<T>, scope: CoroutineScope, errorHandler:((Throwable) -> Unit)? = null, callback: ((Response<T>) -> Unit)? = null) {
    private var job: Job? = null
    private var failure: ((Throwable) -> Unit)? = null
    private val finally: MutableList<() -> Unit> = mutableListOf()

    init {

        job = scope.launch(context = Dispatchers.Main) {
            try {
                val response = call.awaitResponse()
                callback?.invoke(response)
            } catch (throwable: Throwable) {
                errorHandler?.invoke(throwable)
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

    fun onFailure(block: (Throwable) -> Unit): ApiCall<T> {
        this.failure = block
        return this
    }

    fun onFinally(block: () -> Unit): ApiCall<T> {
        this.finally.add(block)
        return this
    }

    fun cancel() {
        job?.cancel()
    }
}

