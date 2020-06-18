package `in`.inferon.msl.cucumbor.repo

import `in`.inferon.msl.cucumbor.model.Utils
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Repository {
    private val TAG = Repository::class.java.simpleName

    var getOTP = MutableLiveData<String>()
    fun getOTP(data: String) {
        Utils.getRetrofit().getOTP(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseString = response.body()!!.string()
                Log.e(TAG,"Get OTP Response : $responseString")
                getOTP.postValue(responseString)
            }
        })
    }

    var verifyOTP = MutableLiveData<String>()
    fun verifyOTP(data: String) {
        Utils.getRetrofit().verifyOTP(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseString = response.body()!!.string()
                Log.e(TAG,"Verify OTP Response : $responseString")
                verifyOTP.postValue(responseString)
            }
        })
    }

    var getUpcomingMilkList = MutableLiveData<String>()
    fun getUpcomingMilkList(data: String) {
        Utils.getRetrofit().getUpcomingMilkList(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseString = response.body()!!.string()
                Log.e(TAG,"Upcoming Milk List Response : $responseString")
                getUpcomingMilkList.postValue(responseString)
            }
        })
    }

    var getMilkSalesList = MutableLiveData<String>()
    fun getMilkSalesList(data: String) {
        Utils.getRetrofit().getMilkSalesList(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseString = response.body()!!.string()
                Log.e(TAG,"Milk Sales List Response : $responseString")
                getMilkSalesList.postValue(responseString)
            }
        })
    }

    var getGroceryList = MutableLiveData<String>()
    fun getGroceryList(data: String) {
        Utils.getRetrofit().getGroceryList(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseString = response.body()!!.string()
                Log.e(TAG,"Grocery List Response : $responseString")
                getGroceryList.postValue(responseString)
            }
        })
    }

    var updateMilkToProjection = MutableLiveData<String>()
    fun updateMilkToProjection(data: String) {
        Utils.getRetrofit().updateMilkToProjection(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseString = response.body()!!.string()
                Log.e(TAG,"Update Milk To Projection Response : $responseString")
                updateMilkToProjection.postValue(responseString)
            }
        })
    }

    var getGroceryHistory = MutableLiveData<String>()
    fun getGroceryHistory(data: String) {
        Utils.getRetrofit().getGroceryHistory(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseString = response.body()!!.string()
                Log.e(TAG,"Grocery History List Response : $responseString")
                getGroceryHistory.postValue(responseString)
            }
        })
    }

    var cancelGroceryItem = MutableLiveData<String>()
    fun cancelGroceryItem(data: String) {
        Utils.getRetrofit().cancelGroceryItem(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseString = response.body()!!.string()
                Log.e(TAG,"Cancel Grocery Item Response : $responseString")
                cancelGroceryItem.postValue(responseString)
            }
        })
    }

    var updateGroceryToProjection = MutableLiveData<String>()
    fun updateGroceryToProjection(data: String) {
        Utils.getRetrofit().updateGroceryToProjection(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseString = response.body()!!.string()
                Log.e(TAG,"Update Grocery To Projection Response : $responseString")
                updateGroceryToProjection.postValue(responseString)
            }
        })
    }

    var getTotalSalesReport = MutableLiveData<String>()
    fun getTotalSalesReport(data: String) {
        Utils.getRetrofit().getTotalSalesReport(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseString = response.body()!!.string()
                Log.e(TAG,"Get Total Sales Report List Response : $responseString")
                getTotalSalesReport.postValue(responseString)
            }
        })
    }

    fun eventLog(data: String, applicationContext: Context) {
        val elObj = JSONObject(data)
        try {
            val pInfo = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
            val version = pInfo.versionName
            elObj.put("app_version", version)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        Utils.getRetrofit().eventLog(elObj.toString()).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

            }
        })
    }
}