package `in`.inferon.msl.lemonor.view.fragment

import `in`.inferon.msl.lemonor.BuildConfig
import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.pojo.SuppliersForCustomer
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.activity.ForceUpdateActivity
import `in`.inferon.msl.lemonor.view.activity.MainFragmentActivity
import `in`.inferon.msl.lemonor.view.activity.MyOrdersActivity
import `in`.inferon.msl.lemonor.view.activity.SafetyMeasureActivity
import `in`.inferon.msl.lemonor.view.adapter.SuppliersForCustomerAdapter
import android.annotation.SuppressLint
import android.content.*
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.consumer_fragment.*
import org.json.JSONObject

class ConsumerFragment : Fragment(), View.OnClickListener {

    private val TAG = ConsumerFragment::class.java.simpleName
    private var repo: Repository? = null
    private var progressLayout: LinearLayout? = null
    private var recyclerView: RecyclerView? = null
    private var noLocationLayout: RelativeLayout? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var safetyMeasuresLayout: LinearLayout? = null
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    internal var latitude: Double = 0.toDouble()
    internal var longitude: Double = 0.toDouble()
    internal var locationMarked = false
    private var suppliersForCustomerList = mutableListOf<SuppliersForCustomer>()
    private var prefferedSupplierList = mutableListOf<SuppliersForCustomer>()
    private var imagesList = ArrayList<String>()
    private var mainImagesList = ArrayList<String>()
    private var mainSuppliersForCustomerList = mutableListOf<SuppliersForCustomer>()
    private var suppliersForCustomerAdapter: SuppliersForCustomerAdapter? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentPage = 0
    private var isLoading = false
    private var isLastPage = false
    private var isLoadingDataFirstTime = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.consumer_fragment, null)
        init(view)
        return view
    }

    private fun init(view: View) {
        LocalBroadcastManager.getInstance(context!!)
            .registerReceiver(showProgressBar, IntentFilter("ShowProgressBar"))
        LocalBroadcastManager.getInstance(context!!)
            .registerReceiver(hideProgressBar, IntentFilter("HideProgressBar"))
        LocalBroadcastManager.getInstance(context!!)

        Log.e(TAG, "Consumer Fragment Called")
        repo = Repository()
        shared = context!!.getSharedPreferences(PREF, AppCompatActivity.MODE_PRIVATE)
        noLocationLayout = view.findViewById(R.id.noLocationLayout) as RelativeLayout
        val retryTV = view.findViewById(R.id.retryTV) as TextView
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout) as SwipeRefreshLayout
        recyclerView = view.findViewById(R.id.recyclerView) as RecyclerView
        progressLayout = view.findViewById(R.id.progressLayout) as LinearLayout
        val fabIB = view.findViewById(R.id.fabIB) as ImageButton
        safetyMeasuresLayout = view.findViewById(R.id.safetyMeasuresLayout) as LinearLayout
        val safetyMeasureCloseIV = view.findViewById(R.id.safetyMeasureCloseIV) as ImageView

        if (shared!!.getString("app_open_count", "") == "1") {
            safetyMeasuresLayout!!.visibility = View.VISIBLE
        } else {
            safetyMeasuresLayout!!.visibility = View.GONE
        }

        noLocationLayout!!.visibility = View.GONE
        swipeRefreshLayout!!.visibility = View.VISIBLE

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
                    mainSuppliersForCustomerList.clear()
                    currentPage = 0
                    val obj = JSONObject()
                    obj.put("user_id", shared!!.getString("id", ""))
                    obj.put("location", "${Constants.changedLatitude}, ${Constants.changedLongitude}")
                    obj.put("version_code", BuildConfig.VERSION_CODE)
                    obj.put("page_no", currentPage++)
                    repo!!.getSupplierListForCustomerWithoutShopTiming(obj.toString())
                    isLoadingDataFirstTime = true
                } else if (type == "open") {
                    progressLayout!!.visibility = View.VISIBLE

                    suppliersForCustomerList.clear()
                    mainSuppliersForCustomerList.clear()
                    currentPage = 0
                    val obj = JSONObject()
                    obj.put("user_id", shared!!.getString("id", ""))
                    obj.put("location", "${Constants.changedLatitude}, ${Constants.changedLongitude}")
                    obj.put("version_code", BuildConfig.VERSION_CODE)
                    obj.put("page_no", currentPage++)
                    repo!!.getSupplierListForCustomer(obj.toString())
                    isLoadingDataFirstTime = true
                }
            } else {
                loadFusedLocation()
            }
        }

        repo!!.getSupplierListForCustomer.observe(this, androidx.lifecycle.Observer {
            run {
                swipeRefreshLayout!!.isRefreshing = false
                loadData(it)
            }
        })

        repo!!.getSupplierListForCustomerWithoutShopTiming.observe(this, androidx.lifecycle.Observer {
            run {
                swipeRefreshLayout!!.isRefreshing = false
                loadData(it)
            }
        })

        setOnScrollListener(recyclerView!!)

        swipeRefreshLayout!!.setOnRefreshListener {
            val connectivityManager = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnected == true
            if (!isConnected) {
                Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    if (type == "all") {
                        progressLayout!!.visibility = View.VISIBLE

                        suppliersForCustomerList.clear()
                        mainSuppliersForCustomerList.clear()
                        currentPage = 0
                        val obj = JSONObject()
                        obj.put("user_id", shared!!.getString("id", ""))
                        if (Constants.changedLatitude != 0.0) {
                            obj.put("location", "${Constants.changedLatitude}, ${Constants.changedLongitude}")
                        } else {
                            obj.put("location", "$latitude, $longitude")
                        }
                        obj.put("version_code", BuildConfig.VERSION_CODE)
                        obj.put("page_no", currentPage++)
                        repo!!.getSupplierListForCustomerWithoutShopTiming(obj.toString())
                        isLoadingDataFirstTime = true
                    } else if (type == "open") {
                        progressLayout!!.visibility = View.VISIBLE

                        suppliersForCustomerList.clear()
                        mainSuppliersForCustomerList.clear()
                        currentPage = 0
                        val obj = JSONObject()
                        obj.put("user_id", shared!!.getString("id", ""))
                        if (Constants.changedLatitude != 0.0) {
                            obj.put("location", "${Constants.changedLatitude}, ${Constants.changedLongitude}")
                        } else {
                            obj.put("location", "$latitude, $longitude")
                        }
                        obj.put("version_code", BuildConfig.VERSION_CODE)
                        obj.put("page_no", currentPage++)
                        repo!!.getSupplierListForCustomer(obj.toString())
                        isLoadingDataFirstTime = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    swipeRefreshLayout!!.isRefreshing = false
                }
            }
        }

        fabIB.setOnClickListener(this)
        retryTV.setOnClickListener(this)
        safetyMeasuresLayout!!.setOnClickListener(this)
        safetyMeasureCloseIV.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.safetyMeasuresLayout -> {
                val intent = Intent(context, SafetyMeasureActivity::class.java)
                startActivity(intent)
            }
            R.id.safetyMeasureCloseIV -> {
                safetyMeasuresLayout!!.visibility = View.GONE
            }
            R.id.fabIB -> {
                val intent = Intent(context, MyOrdersActivity::class.java)
                startActivity(intent)
            }
            R.id.retryTV -> {
                loadFusedLocation()
            }
        }
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
                        noLocationLayout!!.visibility = View.GONE
                        swipeRefreshLayout!!.visibility = View.VISIBLE

                        Log.e(TAG, "Location : " + location.latitude + ", " + location.longitude)

                        latitude = location.latitude
                        longitude = location.longitude
                        Log.e(TAG, "Current Location : $latitude, $longitude")

                        if (type == "all") {
                            progressLayout!!.visibility = View.VISIBLE

                            suppliersForCustomerList.clear()
                            mainSuppliersForCustomerList.clear()
                            currentPage = 0
                            val obj = JSONObject()
                            obj.put("user_id", shared!!.getString("id", ""))
                            obj.put("location", "$latitude, $longitude")
                            obj.put("version_code", BuildConfig.VERSION_CODE)
                            obj.put("page_no", currentPage++)
                            repo!!.getSupplierListForCustomerWithoutShopTiming(obj.toString())
                            isLoadingDataFirstTime = true
                        } else if (type == "open") {
                            progressLayout!!.visibility = View.VISIBLE

                            suppliersForCustomerList.clear()
                            mainSuppliersForCustomerList.clear()
                            currentPage = 0
                            val obj = JSONObject()
                            obj.put("user_id", shared!!.getString("id", ""))
                            obj.put("location", "$latitude, $longitude")
                            obj.put("version_code", BuildConfig.VERSION_CODE)
                            obj.put("page_no", currentPage++)
                            repo!!.getSupplierListForCustomer(obj.toString())
                            isLoadingDataFirstTime = true
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
                noLocationLayout!!.visibility = View.GONE
                swipeRefreshLayout!!.visibility = View.VISIBLE

                if (type == "all") {
                    progressLayout!!.visibility = View.VISIBLE

                    suppliersForCustomerList.clear()
                    mainSuppliersForCustomerList.clear()
                    currentPage = 0
                    val obj = JSONObject()
                    obj.put("user_id", shared!!.getString("id", ""))
                    obj.put("location", "$latitude, $longitude")
                    obj.put("version_code", BuildConfig.VERSION_CODE)
                    obj.put("page_no", currentPage++)
                    repo!!.getSupplierListForCustomerWithoutShopTiming(obj.toString())
                    isLoadingDataFirstTime = true
                } else if (type == "open") {
                    progressLayout!!.visibility = View.VISIBLE

                    suppliersForCustomerList.clear()
                    mainSuppliersForCustomerList.clear()
                    currentPage = 0
                    val obj = JSONObject()
                    obj.put("user_id", shared!!.getString("id", ""))
                    obj.put("location", "$latitude, $longitude")
                    obj.put("version_code", BuildConfig.VERSION_CODE)
                    obj.put("page_no", currentPage++)
                    repo!!.getSupplierListForCustomer(obj.toString())
                    isLoadingDataFirstTime = true
                }
            } else {
                noLocationLayout!!.visibility = View.VISIBLE
                swipeRefreshLayout!!.visibility = View.GONE
            }
        }
    }

    companion object {
        var type = ""
    }

    private fun loadData(it: String) {
        try {
            progressLayout!!.visibility = View.GONE
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

                    try {
                        imagesList = Gson().fromJson(
                            jsonObject.getString("imgs"),
                            object : TypeToken<ArrayList<String>>() {}.type
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    mainImagesList.addAll(imagesList)
                    mainSuppliersForCustomerList.addAll(suppliersForCustomerList)

                    val editor = context!!.getSharedPreferences(PREF, AppCompatActivity.MODE_PRIVATE).edit()
                    editor.putString("o2_id", jsonObject.getString("o2_id"))
                    editor.putString("o2_name", jsonObject.getString("o2_name"))
                    editor.apply()

                    if (mainSuppliersForCustomerList.size > 0 || mainImagesList.size > 0) {
                        noSupplierLayout.visibility = View.GONE
                        if (isLoadingDataFirstTime) {
                            recyclerView!!.layoutManager = LinearLayoutManager(context)
                            suppliersForCustomerAdapter = SuppliersForCustomerAdapter(
                                context!!,
                                mainSuppliersForCustomerList,
                                context!! as MainFragmentActivity,
                                imagesList,
                                prefferedSupplierList
                            )
                            recyclerView!!.adapter = suppliersForCustomerAdapter
                        } else {
                            suppliersForCustomerAdapter!!.notifyDataSetChanged()
                        }


                        isLoading = false
                    } else {
                        noSupplierLayout.visibility = View.VISIBLE
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

    private fun setOnScrollListener(recyclerView: RecyclerView) {
        Log.e(TAG, "Entered ScrollListener")
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager
                if (layoutManager is LinearLayoutManager) {
                    val mLayoutManager = layoutManager as LinearLayoutManager?
                    val visibleItemCount = mLayoutManager!!.childCount
                    val totalItemCount = mLayoutManager.itemCount
                    val pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition()
                    Log.i(
                        TAG,
                        "onScrolled: " + isLoading + " " + isLastPage + " " + (totalItemCount >= Constants.PAGE_SIZE) + " " + (pastVisibleItems >= 0) + " " + (visibleItemCount + pastVisibleItems >= totalItemCount)
                    )
                    if (!isLoading) {
                        if (visibleItemCount + pastVisibleItems >= totalItemCount
                            && pastVisibleItems >= 0
                            && totalItemCount >= Constants.PAGE_SIZE
                        ) {
                            isLoading = true
                            Log.i(TAG, "onScrolled: REQUESTING FOR PAGE $currentPage")
                            if (type == "all") {
                                progressLayout!!.visibility = View.VISIBLE

                                val obj = JSONObject()
                                obj.put("user_id", shared!!.getString("id", ""))
                                obj.put("location", "$latitude, $longitude")
                                obj.put("version_code", BuildConfig.VERSION_CODE)
                                obj.put("page_no", currentPage++)
                                repo!!.getSupplierListForCustomerWithoutShopTiming(obj.toString())
                                isLoadingDataFirstTime = false
                            } else if (type == "open") {
                                progressLayout!!.visibility = View.VISIBLE

                                val obj = JSONObject()
                                obj.put("user_id", shared!!.getString("id", ""))
                                obj.put("location", "$latitude, $longitude")
                                obj.put("version_code", BuildConfig.VERSION_CODE)
                                obj.put("page_no", currentPage++)
                                repo!!.getSupplierListForCustomer(obj.toString())
                                isLoadingDataFirstTime = false
                            }
                        }
                    }
                }
            }
        })
    }

    private val showProgressBar = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            progressLayout!!.visibility = View.VISIBLE
        }
    }

    private val hideProgressBar = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            progressLayout!!.visibility = View.GONE
        }
    }
}