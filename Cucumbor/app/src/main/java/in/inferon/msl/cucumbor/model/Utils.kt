package `in`.inferon.msl.cucumbor.model

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Utils{
    fun getRetrofit(): API {
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(40, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder().client(okHttpClient).baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(
            API::class.java)
    }
}