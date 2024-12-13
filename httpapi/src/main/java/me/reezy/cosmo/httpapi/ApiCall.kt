package me.reezy.cosmo.httpapi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import retrofit2.awaitResponse

class ApiCall<T>(call: Call<T>, scope: CoroutineScope, errorHandler: ((Throwable) -> Unit)? = null, callback: ((Response<T>) -> Unit)? = null) {
    private var job: Job? = null
    private var intercept: Boolean = false
    private var failure: ((Throwable) -> Unit)? = null
    private val finally: MutableList<() -> Unit> = mutableListOf()

    init {

        job = scope.launch(Dispatchers.Main) {
            try {
                val response = call.awaitResponse()
                callback?.invoke(response)
            } catch (throwable: Throwable) {
                withContext(Dispatchers.Main.immediate) {
                    failure?.invoke(throwable)
                    if (!intercept) {
                        errorHandler?.invoke(throwable)
                    }
                }
            } finally {
                withContext(Dispatchers.Main.immediate) {
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
    }

    fun onFailure(intercept: Boolean = false, block: (Throwable) -> Unit): ApiCall<T> {
        this.intercept = intercept
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

