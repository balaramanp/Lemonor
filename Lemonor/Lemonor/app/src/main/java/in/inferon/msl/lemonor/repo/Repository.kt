package `in`.inferon.msl.lemonor.repo

import `in`.inferon.msl.lemonor.BuildConfig
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.Utils
import `in`.inferon.msl.lemonor.view.activity.NoInternetActivity
import `in`.inferon.msl.lemonor.view.activity.SupplierInfoActivity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import androidx.lifecycle.MutableLiveData
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException


class Repository {
    private val TAG = Repository::class.java.simpleName

    var checkEmailExist = MutableLiveData<String>()
    fun checkEmailExist(data: String) {
        Utils.getRetrofit().checkEmailExist(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Check Email Exist Response : $responseString")
                    checkEmailExist.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("checkEmailExist", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }

            }
        })
    }

    var register = MutableLiveData<String>()
    fun register(data: String) {
        Utils.getRetrofit().register(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Register Response : $responseString")
                    register.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("register", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var updateProfile = MutableLiveData<String>()
    fun updateProfile(data: String) {
        Utils.getRetrofit().updateProfile(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Update Profile Response : $responseString")
                    updateProfile.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("updateProfile", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var supportByMail = MutableLiveData<String>()
    fun supportByMail(data: String) {
        Utils.getRetrofit().supportByMail(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Support By Mail Response : $responseString")
                    supportByMail.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("supportByMail", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var enableSupplier = MutableLiveData<String>()
    fun enableSupplier(data: String) {
        Utils.getRetrofit().enableSupplier(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Enable Supplier Response : $responseString")
                    enableSupplier.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("enableSupplier", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var disableSupplier = MutableLiveData<String>()
    fun disableSupplier(data: String) {
        Utils.getRetrofit().disableSupplier(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Disable Supplier Response : $responseString")
                    disableSupplier.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("disableSupplier", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var saveSupplierData = MutableLiveData<String>()
    fun saveSupplierData(data: String) {
        Utils.getRetrofit().saveSupplierData(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Save Supplier Response : $responseString")
                    saveSupplierData.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("saveSupplierData", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var getGoodsList = MutableLiveData<String>()
    fun getGoodsList(data: String) {
        Utils.getRetrofit().getGoodsList(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Goods List Response : $responseString")
                    getGoodsList.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("getGoodsList", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }

            }
        })
    }


    var saveSupplierGoodsList = MutableLiveData<String>()
    fun saveSupplierGoodsList(data: String) {
        Utils.getRetrofit().saveSupplierGoodsList(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Save Supplier Goods List Response : $responseString")
                    saveSupplierGoodsList.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("saveSupplierGoodsList", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var getProductsBySupplierID = MutableLiveData<String>()
    fun getProductsBySupplierID(data: String) {
        Utils.getRetrofit().getProductsBySupplierID(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Products By Supplier ID Response : $responseString")
                    getProductsBySupplierID.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("getProductsBySupplierID", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var getProductsListForSupplierToEdit = MutableLiveData<String>()
    fun getProductsListForSupplierToEdit(data: String) {
        Utils.getRetrofit().getProductsListForSupplierToEdit(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Products List For Supplier To Edit Response : $responseString")
                    getProductsListForSupplierToEdit.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getProductsListForSupplierToEdit",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var enableSupplierProduct = MutableLiveData<String>()
    fun enableSupplierProduct(data: String) {
        Utils.getRetrofit().enableSupplierProduct(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Enable Supplier Product Response : $responseString")
                    enableSupplierProduct.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("enableSupplierProduct", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var disableSupplierProduct = MutableLiveData<String>()
    fun disableSupplierProduct(data: String) {
        Utils.getRetrofit().disableSupplierProduct(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Disable Supplier Product Response : $responseString")
                    disableSupplierProduct.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("disableSupplierProduct", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var saveSupplierItem = MutableLiveData<String>()
    fun saveSupplierItem(data: String) {
        Utils.getRetrofit().saveSupplierItem(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Save Supplier Item Response : $responseString")
                    saveSupplierItem.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("saveSupplierItem", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var getSupplierProfileDataById = MutableLiveData<String>()
    fun getSupplierProfileDataById(data: String) {
        Utils.getRetrofit().getSupplierProfileDataById(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Supplier Profile Data By ID Response : $responseString")
                    getSupplierProfileDataById.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getSupplierProfileDataById",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var updateSupplierData = MutableLiveData<String>()
    fun updateSupplierData(data: String) {
        Utils.getRetrofit().updateSupplierData(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Update Supplier Data Response : $responseString")
                    updateSupplierData.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("updateSupplierData", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var getSupplierListForCustomer = MutableLiveData<String>()
    fun getSupplierListForCustomer(data: String) {
        Utils.getRetrofit().getSupplierListForCustomer(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Supplier List For Customer Response : $responseString")
                    getSupplierListForCustomer.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getSupplierListForCustomer",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var getSupplierListForCustomerWithoutShopTiming = MutableLiveData<String>()
    fun getSupplierListForCustomerWithoutShopTiming(data: String) {
        Utils.getRetrofit().getSupplierListForCustomerWithoutShopTiming(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Supplier List For Customer Without Shop Timing Response : $responseString")
                    getSupplierListForCustomerWithoutShopTiming.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getSupplierListForCustomerWithoutShopTiming",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var getProductsListBySupplierIdForCustomer = MutableLiveData<String>()
    fun getProductsListBySupplierIdForCustomer(data: String) {
        Utils.getRetrofit().getProductsListBySupplierIdForCustomer(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Products List By Supplier Id For Customer Response : $responseString")
                    getProductsListBySupplierIdForCustomer.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getProductsListBySupplierIdForCustomer",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var placeAnOrder = MutableLiveData<String>()
    fun placeAnOrder(data: String) {
        Utils.getRetrofit().placeAnOrder(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Place An Order Response : $responseString")
                    placeAnOrder.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("placeAnOrder", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var getOrderListBySupplierID = MutableLiveData<String>()
    fun getOrderListBySupplierID(data: String) {
        Utils.getRetrofit().getOrderListBySupplierID(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Order List By Supplier ID Response : $responseString")
                    getOrderListBySupplierID.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("getOrderListBySupplierID", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var getOrderListByUserIdForCustomer = MutableLiveData<String>()
    fun getOrderListByUserIdForCustomer(data: String) {
        Utils.getRetrofit().getOrderListByUserIdForCustomer(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Order List By User Id For Customer Response : $responseString")
                    getOrderListByUserIdForCustomer.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getOrderListByUserIdForCustomer",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var removeSupplierProduct = MutableLiveData<String>()
    fun removeSupplierProduct(data: String) {
        Utils.getRetrofit().removeSupplierProduct(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Remove Supplier Product Response : $responseString")
                    removeSupplierProduct.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("removeSupplierProduct", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var getInitData = MutableLiveData<String>()
    fun getInitData(data: String) {
        Utils.getRetrofit().getInitData(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Init Data Response : $responseString")
                    getInitData.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("getInitData", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var saveSupplierGoodsData = MutableLiveData<String>()
    fun saveSupplierGoodsData(data: String) {
        Utils.getRetrofit().saveSupplierGoodsData(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Save Supplier Goods Data Response : $responseString")
                    saveSupplierGoodsData.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("saveSupplierGoodsData", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var getStateDistrictList = MutableLiveData<String>()
    fun getStateDistrictList(data: String) {
        Utils.getRetrofit().getStateDistrictList(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get State District List Response : $responseString")
                    getStateDistrictList.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("getStateDistrictList", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var getAboutUs = MutableLiveData<String>()
    fun getAboutUs() {
        Utils.getRetrofitText().getAboutUs().enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get About Us Response : $responseString")
                    getAboutUs.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("getAboutUs", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var updateUserCancelled = MutableLiveData<String>()
    fun updateUserCancelled(data: String) {
        Utils.getRetrofit().updateUserCancelled(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Update User Cancelled Response : $responseString")
                    updateUserCancelled.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("updateUserCancelled", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var updateSupplierAccepted = MutableLiveData<String>()
    fun updateSupplierAccepted(data: String) {
        Utils.getRetrofit().updateSupplierAccepted(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Update Supplier Accepted Response : $responseString")
                    updateSupplierAccepted.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("updateSupplierAccepted", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var updateSupplierRejected = MutableLiveData<String>()
    fun updateSupplierRejected(data: String) {
        Utils.getRetrofit().updateSupplierRejected(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Update Supplier Rejected Response : $responseString")
                    updateSupplierRejected.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("updateSupplierRejected", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var orderCompleted = MutableLiveData<String>()
    fun orderCompleted(data: String) {
        Utils.getRetrofit().orderCompleted(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Order Completed Response : $responseString")
                    orderCompleted.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("orderCompleted", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }
            }
        })
    }

    var updateSupplierAcceptedByTokenNumber = MutableLiveData<String>()
    fun updateSupplierAcceptedByTokenNumber(data: String) {
        Utils.getRetrofit().updateSupplierAcceptedByTokenNumber(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Update Supplier Accepted By Token Number Response : $responseString")
                    updateSupplierAcceptedByTokenNumber.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "updateSupplierAcceptedByTokenNumber",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var updateSupplierRejectedByTokenNumber = MutableLiveData<String>()
    fun updateSupplierRejectedByTokenNumber(data: String) {
        Utils.getRetrofit().updateSupplierRejectedByTokenNumber(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Update Supplier Rejected By Token Number Response : $responseString")
                    updateSupplierRejectedByTokenNumber.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "updateSupplierRejectedByTokenNumber",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var updateO2PriceFromSupplierInOrder = MutableLiveData<String>()
    fun updateO2PriceFromSupplierInOrder(data: String) {
        Utils.getRetrofit().updateO2PriceFromSupplierInOrder(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Update O2 Price From Supplier In Order Response : $responseString")
                    updateO2PriceFromSupplierInOrder.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "updateO2PriceFromSupplierInOrder",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var updateSupplierCompletedByTokenNumber = MutableLiveData<String>()
    fun updateSupplierCompletedByTokenNumber(data: String) {
        Utils.getRetrofit().updateSupplierCompletedByTokenNumber(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Update Supplier Completed By Token Number Response : $responseString")
                    updateSupplierCompletedByTokenNumber.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "updateSupplierCompletedByTokenNumber",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var getOrderHistoryList = MutableLiveData<String>()
    fun getOrderHistoryList(data: String) {
        Utils.getRetrofit().getOrderHistoryList(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Order History List Response : $responseString")
                    getOrderHistoryList.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging("getOrderHistoryList", errorBody!!.string() + " Error Raw : ${response.raw()}")
                }

            }
        })
    }


    var getOrderByTokenId = MutableLiveData<String>()
    fun getOrderByTokenId(data: String) {
        Utils.getRetrofit().getOrderByTokenId(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Order By Token ID Response : $responseString")
                    getOrderByTokenId.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getOrderByTokenId",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }


    var getIsSupplierReachableForCustomer = MutableLiveData<String>()
    fun getIsSupplierReachableForCustomer(data: String) {
        Utils.getRetrofit().getIsSupplierReachableForCustomer(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Is Supplier Reachable For Customer Response : $responseString")
                    getIsSupplierReachableForCustomer.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getIsSupplierReachableForCustomer",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var chat = MutableLiveData<String>()
    fun chat(data: String) {
        Utils.getRetrofit().chat(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Chat Response : $responseString")
                    chat.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "chat",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var getPublicKey = MutableLiveData<String>()
    fun getPublicKey(data: String) {
        Utils.getRetrofit().getPublicKey(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Public Key Response : $responseString")
                    getPublicKey.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getPublicKey",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var getAppContent = MutableLiveData<String>()
    fun getAppContent() {
        Utils.getRetrofit().getAppContent(JSONObject().toString()).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get App Content Response : $responseString")
                    getAppContent.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getAppContent",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var updateSupplierProductRate = MutableLiveData<String>()
    fun updateSupplierProductRate(data: String) {
        Utils.getRetrofit().updateSupplierProductRate(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Update Supplier Product Rate Response : $responseString")
                    updateSupplierProductRate.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "updateSupplierProductRate",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var updateAllSupplierProductRate = MutableLiveData<String>()
    fun updateAllSupplierProductRate(data: String) {
        Utils.getRetrofit().updateAllSupplierProductRate(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Update All Supplier Product Rate Response : $responseString")
                    updateAllSupplierProductRate.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "updateAllSupplierProductRate",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var getOrderOpenedByTokenNumber = MutableLiveData<String>()
    fun getOrderOpenedByTokenNumber(data: String) {
        Utils.getRetrofit().getOrderOpenedByTokenNumber(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Order Opened By Token Number Response : $responseString")
                    getOrderOpenedByTokenNumber.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getOrderOpenedByTokenNumber",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var updateOrderDetailsFromSupplier = MutableLiveData<String>()
    fun updateOrderDetailsFromSupplier(data: String) {
        Utils.getRetrofit().updateOrderDetailsFromSupplier(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Update Order Details From Supplier Response : $responseString")
                    updateOrderDetailsFromSupplier.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "updateOrderDetailsFromSupplier",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var getCategoryFilteredProductsList = MutableLiveData<String>()
    fun getCategoryFilteredProductsList(data: String) {
        Utils.getRetrofit().getCategoryFilteredProductsList(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Category Filtered Products List Response : $responseString")
                    getCategoryFilteredProductsList.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getCategoryFilteredProductsList",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var getSearchFilteredProductsList = MutableLiveData<String>()
    fun getSearchFilteredProductsList(data: String) {
        Utils.getRetrofit().getSearchFilteredProductsList(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Search Filtered Products List Response : $responseString")
                    getSearchFilteredProductsList.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getSearchFilteredProductsList",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var getGoodsListBySearchTerm = MutableLiveData<String>()
    fun getGoodsListBySearchTerm(data: String) {
        Utils.getRetrofit().getGoodsListBySearchTerm(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Goods List By Search Term Response : $responseString")
                    getGoodsListBySearchTerm.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getGoodsListBySearchTerm",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var getGoodsListByCategoryFilter = MutableLiveData<String>()
    fun getGoodsListByCategoryFilter(data: String) {
        Utils.getRetrofit().getGoodsListByCategoryFilter(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Goods List By Category Filter Response : $responseString")
                    getGoodsListByCategoryFilter.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getGoodsListByCategoryFilter",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }


    var getProductsListByCategoryFilterForSupplierToEdit = MutableLiveData<String>()
    fun getProductsListByCategoryFilterForSupplierToEdit(data: String) {
        Utils.getRetrofit().getProductsListByCategoryFilterForSupplierToEdit(data)
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.printStackTrace()
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val responseString = response.body()!!.string()
                        Log.e(
                            TAG,
                            "Get Products List By Category Filter For Supplier To Edit Response : $responseString"
                        )
                        getProductsListByCategoryFilterForSupplierToEdit.postValue(responseString)
                    } else {
                        val errorBody = response.errorBody()
                        activityLogging(
                            "getProductsListByCategoryFilterForSupplierToEdit",
                            errorBody!!.string() + " Error Raw : ${response.raw()}"
                        )
                    }
                }
            })
    }


    var getProductsListBySearchTermForSupplierToEdit = MutableLiveData<String>()
    fun getProductsListBySearchTermForSupplierToEdit(data: String) {
        Utils.getRetrofit().getProductsListBySearchTermForSupplierToEdit(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Products List By Search Term For Supplier To Edit Response : $responseString")
                    getProductsListBySearchTermForSupplierToEdit.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getProductsListBySearchTermForSupplierToEdit",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var checkSupplierExists = MutableLiveData<String>()
    fun checkSupplierExists(data: String) {
        Utils.getRetrofit().checkSupplierExists(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Check Supplier Exists Response : $responseString")
                    checkSupplierExists.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "checkSupplierExists",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var safetyMeasurement = MutableLiveData<String>()
    fun safetyMeasurement() {
        Utils.getRetrofitText().safetyMeasurement().enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Safety Measurement Response : $responseString")
                    safetyMeasurement.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "safetyMeasurement",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var updateFCMToken = MutableLiveData<String>()
    fun updateFCMToken(data: String) {
        Utils.getRetrofitText().updateFCMToken(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Update FCM Token Response : $responseString")
                    updateFCMToken.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "updateFCMToken",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }


    var getUserProfileByUserID = MutableLiveData<String>()
    fun getUserProfileByUserID(data: String) {
        Utils.getRetrofit().getUserProfileByUserID(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get User Profile By User ID Response : $responseString")
                    getUserProfileByUserID.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getUserProfileByUserID",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    var addAddress = MutableLiveData<String>()
    fun addAddress(data: String) {
        Utils.getRetrofit().addAddress(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Add Address Response : $responseString")
                    addAddress.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "addAddress",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }


    var editAddress = MutableLiveData<String>()
    fun editAddress(data: String) {
        Utils.getRetrofit().editAddress(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Edit Address Response : $responseString")
                    editAddress.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "editAddress",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }


    var deleteAddress = MutableLiveData<String>()
    fun deleteAddress(data: String) {
        Utils.getRetrofit().deleteAddress(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Delete Address Response : $responseString")
                    deleteAddress.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "deleteAddress",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }


    var getSupplierIdForStaff = MutableLiveData<String>()
    fun getSupplierIdForStaff(data: String) {
        Utils.getRetrofit().getSupplierIdForStaff(data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseString = response.body()!!.string()
                    Log.e(TAG, "Get Supplier ID For Staff Response : $responseString")
                    getSupplierIdForStaff.postValue(responseString)
                } else {
                    val errorBody = response.errorBody()
                    activityLogging(
                        "getSupplierIdForStaff",
                        errorBody!!.string() + " Error Raw : ${response.raw()}"
                    )
                }
            }
        })
    }

    fun activityLogging(module: String, data: String) {
        val versionName = BuildConfig.VERSION_NAME
        Utils.getRetrofitText().activityLogging(versionName, module, data).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

            }
        })
    }
}