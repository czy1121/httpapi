package com.demo.app

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import me.reezy.cosmo.httpapi.Api
import me.reezy.cosmo.httpapi.api
import me.reezy.cosmo.httpapi.catch
import me.reezy.cosmo.httpapi.onSuccess
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.await
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File

class MainActivity : AppCompatActivity() {


    private val okhttp by lazy {
        OkHttpClient.Builder().cache(Cache(File(cacheDir, "okhttp"), 100 * 1024 * 1024)).build()
    }

    private val moshi by lazy {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }
    private val retrofit by lazy {
        Retrofit.Builder().client(okhttp)
            .baseUrl("http://httpbin.org")
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // 提供 Retrofit 实例
        Api.setRetrofitProvider {
            retrofit
        }

        // 设置全局错误处理
        Api.setErrorHandler {
            it.printStackTrace()
        }

        // 普通请求
        api<TestApi>().call().onSuccess(this) {
            findViewById<TextView>(R.id.text1).text = "普通请求:\n$it"
            Log.e("OoO", "call => $it")
        }.onFailure {
            Log.e("OoO", "call onFailure  => $it")
        }.onFinally {
            Log.e("OoO", "call onFinally")
        }

        // 在协程上下文中发起请求
        lifecycleScope.launch {

            // 自己处理异常
            try {
                val data = api<TestApi>().call().await()
                Log.e("OoO", "call() => $data")
            } catch (ex: Throwable) {
                Log.e("OoO", "call() catch => $ex")
            }

            // 使用全局异常处理
            val result = api<TestApi>().call().catch()
            findViewById<TextView>(R.id.text2).text = "call() => \n$result"
            Log.e("OoO", "call() => $result")


        }
    }
}