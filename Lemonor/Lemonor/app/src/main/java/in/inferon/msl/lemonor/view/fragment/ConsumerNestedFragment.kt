package `in`.inferon.msl.lemonor.view.fragment

import `in`.inferon.msl.lemonor.BuildConfig
import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.pojo.SuppliersForCustomer
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.activity.ForceUpdateActivity
import `in`.inferon.msl.lemonor.view.activity.MainFragmentActivity
import `in`.inferon.msl.lemonor.view.adapter.CNFPreferredSupplierAdapter
import `in`.inferon.msl.lemonor.view.adapter.CNFSuppliersForCustomerAdapter
import `in`.inferon.msl.lemonor.view.adapter.SuppliersForCustomerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.consumer_fragment.*
import org.json.JSONObject

class ConsumerNestedFragment : Fragment() {
    private val TAG = ConsumerNestedFragment::class.java.simpleName
    private var repo: Repository? = null
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    private var suppliersForCustomerList = mutableListOf<SuppliersForCustomer>()
    private var prefferedSupplierList = mutableListOf<SuppliersForCustomer>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    internal var latitude: Double = 0.toDouble()
    internal var longitude: Double = 0.toDouble()
    private var dataLayout: LinearLayout? = null
    private var progressLayout: LinearLayout? = null
    private var featuredRV: RecyclerView? = null
    private var nearByShopsRV: RecyclerView? = null
    private var cnfPreferredSupplierAdapter: CNFPreferredSupplierAdapter? = null
    private var cnfSuppliersForCustomerAdapter: CNFSuppliersForCustomerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.consumer_nested_fragment, null)
        Log.e(TAG, "Entered CNFragment")
        init(view)
        return view
    }

    private fun init(view: View) {
        repo = Repository()
        shared = context!!.getSharedPreferences(PREF, AppCompatActivity.MODE_PRIVATE)

        dataLayout = view.findViewById(R.id.dataLayout) as LinearLayout
        featuredRV = view.findViewById(R.id.featuredRV) as RecyclerView
        nearByShopsRV = view.findViewById(R.id.nearByShopsRV) as RecyclerView
        progressLayout = view.findViewById(R.id.progressLayout) as LinearLayout

        dataLayout!!.visibility = View.GONE


        val connectivityManager = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        Log.e(TAG, "Network Connectivity : $isConnected")
        if (!isConnected) {
            Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show()
        } else {
            if (Constants.changedLatitude != 0.0) {
                if (type == "all") {
                    progressLayout!!.visibility = View.VISIBLE

                    suppliersForCustomerList.clear()
                    val obj = JSONObject()
                    obj.put("user_id", shared!!.getString("id", ""))
                    obj.put("location", "${Constants.changedLatitude}, ${Constants.changedLongitude}")
                    obj.put("version_code", BuildConfig.VERSION_CODE)
                    obj.put("page_no", 0)
                    repo!!.getSupplierListForCustomerWithoutShopTiming(obj.toString())
                } else if (type == "open") {
                    progressLayout!!.visibility = View.VISIBLE

                    suppliersForCustomerList.clear()
                    val obj = JSONObject()
                    obj.put("user_id", shared!!.getString("id", ""))
                    obj.put("location", "${Constants.changedLatitude}, ${Constants.changedLongitude}")
                    obj.put("version_code", BuildConfig.VERSION_CODE)
                    obj.put("page_no", 0)
                    repo!!.getSupplierListForCustomer(obj.toString())
                }
            } else {
                loadFusedLocation()
            }
        }


        repo!!.getSupplierListForCustomer.observe(this, androidx.lifecycle.Observer {
            run {
                loadData(it)
            }
        })

        repo!!.getSupplierListForCustomerWithoutShopTiming.observe(this, androidx.lifecycle.Observer {
            run {
                loadData(it)
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun loadFusedLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                try {
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        Log.e(TAG, "Location : " + location.latitude + ", " + location.longitude)

                        latitude = location.latitude
                        longitude = location.longitude
                        Log.e(TAG, "Current Location : $latitude, $longitude")

                        if (type == "all") {
                            progressLayout!!.visibility = View.VISIBLE

                            suppliersForCustomerList.clear()
                            val obj = JSONObject()
                            obj.put("user_id", shared!!.getString("id", ""))
                            obj.put("location", "$latitude, $longitude")
                            obj.put("version_code", BuildConfig.VERSION_CODE)
                            obj.put("page_no", 0)
                            repo!!.getSupplierListForCustomerWithoutShopTiming(obj.toString())
                        } else if (type == "open") {
                            progressLayout!!.visibility = View.VISIBLE

                            suppliersForCustomerList.clear()
                            val obj = JSONObject()
                            obj.put("user_id", shared!!.getString("id", ""))
                            obj.put("location", "$latitude, $longitude")
                            obj.put("version_code", BuildConfig.VERSION_CODE)
                            obj.put("page_no", 0)
                            repo!!.getSupplierListForCustomer(obj.toString())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        fusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            latitude = mLastLocation.latitude
            longitude = mLastLocation.longitude
            Log.e(TAG, "Current Location : $latitude, $longitude")
            if (mLastLocation != null) {
                if (type == "all") {
                    progressLayout!!.visibility = View.VISIBLE

                    suppliersForCustomerList.clear()
                    val obj = JSONObject()
                    obj.put("user_id", shared!!.getString("id", ""))
                    obj.put("location", "$latitude, $longitude")
                    obj.put("version_code", BuildConfig.VERSION_CODE)
                    obj.put("page_no", 0)
                    repo!!.getSupplierListForCustomerWithoutShopTiming(obj.toString())
                } else if (type == "open") {
                    progressLayout!!.visibility = View.VISIBLE

                    suppliersForCustomerList.clear()
                    val obj = JSONObject()
                    obj.put("user_id", shared!!.getString("id", ""))
                    obj.put("location", "$latitude, $longitude")
                    obj.put("version_code", BuildConfig.VERSION_CODE)
                    obj.put("page_no", 0)
                    repo!!.getSupplierListForCustomer(obj.toString())
                }
            }
        }
    }

    companion object {
        var type = ""
    }


    private fun loadData(it: String) {
        try {
            progressLayout!!.visibility = View.GONE
            dataLayout!!.visibility = View.VISIBLE
            val jsonObject = JSONObject(it)
            if (jsonObject.getString("status") == "ok") {
                if (!jsonObject.getBoolean("app_update_status")) {
                    suppliersForCustomerList =
                        Gson().fromJson(
                            jsonObject.getString("supplier_list_for_customer"),
                            object : TypeToken<MutableList<SuppliersForCustomer>>() {}.type
                        )

                    prefferedSupplierList =
                        Gson().fromJson(
                            jsonObject.getString("preffered_supplier_list"),
                            object : TypeToken<MutableList<SuppliersForCustomer>>() {}.type
                        )

                    val editor = context!!.getSharedPreferences(PREF, AppCompatActivity.MODE_PRIVATE).edit()
                    editor.putString("o2_id", jsonObject.getString("o2_id"))
                    editor.putString("o2_name", jsonObject.getString("o2_name"))
                    editor.apply()

                    if (prefferedSupplierList.size > 0) {
                        featuredRV!!.layoutManager = GridLayoutManager(context, 2)
                        cnfPreferredSupplierAdapter = CNFPreferredSupplierAdapter(
                            context!!,
                            prefferedSupplierList
                        )
                        featuredRV!!.adapter = cnfPreferredSupplierAdapter
                    }

                    if (suppliersForCustomerList.size > 0) {
                        nearByShopsRV!!.layoutManager = LinearLayoutManager(context)
                        cnfSuppliersForCustomerAdapter = CNFSuppliersForCustomerAdapter(
                            context!!,
                            suppliersForCustomerList
                        )
                        nearByShopsRV!!.adapter = cnfSuppliersForCustomerAdapter
                    }
                } else {
                    val intent = Intent(context, ForceUpdateActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}