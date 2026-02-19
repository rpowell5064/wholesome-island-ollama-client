package com.wholesomeisland.ollamaclient.data.remote

import com.wholesomeisland.ollamaclient.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object OllamaServiceFactory {

    private var sharedClient: OkHttpClient? = null

    private fun getClient(): OkHttpClient {
        return sharedClient ?: synchronized(this) {
            sharedClient ?: OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    // Only log headers in debug mode. No logging in release for privacy/security.
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.HEADERS
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build().also { sharedClient = it }
        }
    }

    fun create(baseUrl: String): OllamaApi {
        val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(getClient())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OllamaApi::class.java)
    }
}
