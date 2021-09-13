package com.example.axxtransdemo.data.api

import com.example.axxtransdemo.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


object RetrofitBuilder {

    private const val BASE_URL = "https://api.airtable.com/v0/"
    private const val TOKEN = BuildConfig.MY_API_KEY



//    var client: OkHttpClient = Builder().addInterceptor(object : Interceptor() {
//        @Throws(IOException::class)
//        fun intercept(chain: Interceptor.Chain): Response? {
//            val newRequest: Request = chain.request().newBuilder()
//                .addHeader("Authorization", "Bearer $TOKEN")
//                .build()
//            return chain.proceed(newRequest)
//        }
//    }).build()


    var okHttpClient = OkHttpClient.Builder().apply {
        addInterceptor(
            Interceptor { chain ->
                val builder = chain.request().newBuilder()
                builder.addHeader("Authorization", "Bearer $TOKEN").build()
//                builder.header("X-App-Version", "1.23")
//                builder.header("X-Platform", "Android")
//                builder.header("X-Auth-Token", "sgsrager32524542afg3423")
                return@Interceptor chain.proceed(builder.build())
            }
        )
    }.build()

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build() //Doesn't require the adapter
    }

    val apiService: ApiService = getRetrofit().create(ApiService::class.java)
}