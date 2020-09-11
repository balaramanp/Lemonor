package `in`.inferon.msl.lemonor.model

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Utils{
    fun getRetrofit(): API {
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder().client(okHttpClient).baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(
            API::class.java)
    }

    fun getRetrofitText(): API {
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder().client(okHttpClient).baseUrl(Constants.TEXT_BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(
            API::class.java)
    }
}