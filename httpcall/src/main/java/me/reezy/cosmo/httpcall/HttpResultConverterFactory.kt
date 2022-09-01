package me.reezy.cosmo.httpcall

import com.squareup.moshi.Moshi
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class HttpResultConverterFactory private constructor(private val factory: MoshiConverterFactory) : Converter.Factory() {


    companion object {
        fun create(moshi: Moshi = Moshi.Builder().build()): HttpResultConverterFactory {
            return HttpResultConverterFactory(MoshiConverterFactory.create(moshi))
        }
    }

    fun asLenient(): HttpResultConverterFactory {
        return HttpResultConverterFactory(factory.asLenient())
    }

    fun failOnUnknown(): HttpResultConverterFactory {
        return HttpResultConverterFactory(factory.failOnUnknown())
    }

    fun withNullSerialization(): HttpResultConverterFactory {
        return HttpResultConverterFactory(factory.withNullSerialization())
    }

    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, *>? {
        if (type is ParameterizedType && (type.rawType == HttpResult::class.java)) {
            return factory.responseBodyConverter(getParameterUpperBound(0, type), annotations, retrofit)
        }
        return factory.responseBodyConverter(type, annotations, retrofit)
    }

    override fun requestBodyConverter(type: Type, parameterAnnotations: Array<Annotation>, methodAnnotations: Array<Annotation>, retrofit: Retrofit): Converter<*, RequestBody>? {
        return factory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
    }
}