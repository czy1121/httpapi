package com.demo.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import me.reezy.cosmo.httpcall.*
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
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
            .addConverterFactory(HttpResultConverterFactory.create(moshi).asLenient())
            .addCallAdapterFactory(HttpResultAdapterFactory())
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // 提供 Retrofit 实例
        HttpService.setRetrofitProvider {
            retrofit
        }

        // 设置全局错误处理
        HttpService.setErrorHandler {
            it.printStackTrace()
        }

        var text1 = ""
        var text2 = ""

        // 普通请求
        http<TestService>().call().onSuccess(this) {
            text1 = it.toString()
            findViewById<TextView>(R.id.hello).text = "call() \n==> $text1\n\nsuspendHttpResult() \n==> $text2"
            Log.e("OoO", "call => $it")
        }.onFailure {
            Log.e("OoO", "call onFailure  => $it")
        }.onFinally {
            Log.e("OoO", "call onFinally")
        }

        // 在协程上下文中发起请求
        lifecycleScope.launch {

            // 通过回调处理结果
            http<TestService>().suspendHttpResult().onSuccess {
                text2 = it.toString()
                findViewById<TextView>(R.id.hello).text = "call() \n==> $text1\n\nsuspendHttpResult() \n==> $text2"
                Log.e("OoO", "suspendHttpResult => $it")
            }.onFailure {
                Log.e("OoO", "suspendHttpResult onFailure => $it")
            }

            // 直接返回结果
            val result = http<TestService>().suspendHttpResult().getOrNull() ?: return@launch

        }
    }
}