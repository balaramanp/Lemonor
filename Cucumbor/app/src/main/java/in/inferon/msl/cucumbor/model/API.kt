package `in`.inferon.msl.cucumbor.model

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface API {

    @FormUrlEncoded
    @POST("client_app.php")
    fun getOTP(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("client_app.php")
    fun verifyOTP(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("client_app.php")
    fun getUpcomingMilkList(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("client_app.php")
    fun getMilkSalesList(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("client_app.php")
    fun getGroceryList(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("client_app.php")
    fun updateMilkToProjection(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("client_app.php")
    fun getGroceryHistory(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("client_app.php")
    fun cancelGroceryItem(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("client_app.php")
    fun updateGroceryToProjection(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("client_app.php")
    fun getTotalSalesReport(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("app_logger.php")
    fun eventLog(@Field("data") data: String): Call<ResponseBody>
}