# httpapi
 
使用 Coroutine + Retrofit 打造的最简单HTTP请求库，支持 `kotlin.Result`

## Gradle

``` groovy
repositories {
    maven { url "https://gitee.com/ezy/repo/raw/cosmo/"}
}
dependencies {
    implementation "me.reezy.cosmo:httpapi:0.8.0"
}
```

## 使用

```kotlin
data class HttpBin(
    val origin: String,
    val url: String,
)

interface TestService {
    @GET("https://httpbin.org/get")
    suspend fun suspendHttpResult(): Result<HttpBin>

    @GET("https://httpbin.org/get")
    fun call(): Call<HttpBin>
}
```

初始化

```kotlin
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
        .addCallAdapterFactory(ResultCallAdapterFactory {
            it.printStackTrace()
        })
        .build() 
}


// 提供 Retrofit 实例
Api.setRetrofitProvider {
    retrofit
}

// 设置全局错误处理
Api.setErrorHandler {
    it.printStackTrace()
}
```

普通请求

```kotlin
// 普通请求
http<TestService>().call().onSuccess(this) {
    Log.e("OoO", "call => $it")
}.onFailure {
    Log.e("OoO", "call onFailure => $it")
}.onFinally {
    Log.e("OoO", "call onFinally")
}

```

在协程上下文中发起请求

```kotlin
// 在协程上下文中发起请求
lifecycleScope.launch {

    // 通过回调处理结果
    http<TestService>().suspendKotlinResult().onSuccess {
        Log.e("OoO", "suspendKotlinResult => $it")
    }.onFailure {
        Log.e("OoO", "suspendKotlinResult onFailure => $it")
    }

    // 直接返回结果
    val result = http<TestService>().suspendKotlinResult().getOrNull() ?: return@launch
}
```


## LICENSE

The Component is open-sourced software licensed under the [Apache license](LICENSE).
