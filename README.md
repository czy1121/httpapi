# httpapi
 
使用 Coroutine + Retrofit 打造的最简单HTTP请求库

## Gradle

``` groovy
repositories {
    maven { url "https://gitee.com/ezy/repo/raw/cosmo/"}
}
dependencies {
    implementation "me.reezy.cosmo:httpapi:0.10.0"
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
api<TestApi>().call().onSuccess(this) {
    Log.e("OoO", "call => $it")
}.onFailure {
    Log.e("OoO", "call onFailure  => $it")
}.onFinally {
    Log.e("OoO", "call onFinally")
}

```

在协程上下文中发起请求

```kotlin
// 在协程上下文中发起请求
lifecycleScope.launch {

    // 自己处理异常
    try {
        val data = api<TestApi>().call().await()
        Log.e("OoO", "call => $data")
    } catch (ex: Throwable) {
        Log.e("OoO", "call catch => $ex")
    }

    // 使用全局异常处理，可能返回 null
    val result = api<TestApi>().call().catch() 
    Log.e("OoO", "call => $result")

}
```


## LICENSE

The Component is open-sourced software licensed under the [Apache license](LICENSE).
