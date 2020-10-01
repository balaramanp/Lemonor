package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.fragment.ConsumerFragment
import `in`.inferon.msl.lemonor.view.fragment.ConsumerNestedFragment
import `in`.inferon.msl.lemonor.view.fragment.SupplierFragment
import android.app.Dialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.activity_main_fragment.*
import kotlinx.android.synthetic.main.container_main_fragment.*
import kotlinx.android.synthetic.main.container_main_fragment.progressLayout
import kotlinx.android.synthetic.main.navigation_layout.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Looper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.LinkProperties


class MainFragmentActivity : AppCompatActivity(), View.OnClickListener, OnMapReadyCallback {

    private val TAG = MainFragmentActivity::class.java.simpleName
    private val PREF = "Pref"
    private var repo: Repository? = null
    private var shared: SharedPreferences? = null
    private var sdf: SimpleDateFormat? = null
    private var stateDistrictList = ""
    private val requestCode = 201
    private val REQUEST_LOCATION = 199
    private lateinit var mMap: GoogleMap
    internal var latitude: Double = 0.toDouble()
    internal var longitude: Double = 0.toDouble()
    internal var currentLocationMarker: Marker? = null
    internal var locationMarked = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isSubSupplier = false
    private var staffShopName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(loadSupplierFragment, IntentFilter("LoadSupplierFragment"))

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(checkSellerEnable, IntentFilter("CheckSellerEnable"))

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(orderRefreshData, IntentFilter("OrderRefreshData"))

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(locationChanged, IntentFilter("LocationChanged"))

        setContentView(R.layout.activity_main_fragment)

        loadGPS()
    }

    @SuppressLint("SimpleDateFormat")
    private fun init() {
        Constants.logger.logEvent("Home Activity : Entered Home Activity")
        Log.e(TAG, "Entered Init")
        Constants.context = this
        repo = Repository()
        shared = getSharedPreferences(PREF, MODE_PRIVATE)
        sdf = SimpleDateFormat("dd/M/yyyy")

        if (shared!!.getString("supplier_enable", "") == "true") {
            expandIV.visibility = View.VISIBLE
        }

        progressLayout.visibility = View.VISIBLE
        repo!!.getAboutUs()
        repo!!.getAboutUs.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE

            }
        })

        val obj = JSONObject()
        obj.put("user_id", shared!!.getString("id", ""))
        repo!!.getSupplierIdForStaff(obj.toString())

        repo!!.getSupplierIdForStaff.observe(this, androidx.lifecycle.Observer {
            progressLayout.visibility = View.GONE
            val jsonObject = JSONObject(it)
            if (jsonObject.getBoolean("is_staff")) {
                isSubSupplier = true
                staffShopName = jsonObject.getString("shop_name")
                expandIV.visibility = View.GONE
                navExpandLayout.visibility = View.GONE
                supplierMenuLayout.visibility = View.INVISIBLE
            } else {
                expandIV.visibility = View.VISIBLE
                navExpandLayout.visibility = View.VISIBLE
            }
        })

        loadStateDistrictList()

        Constants.user_id = shared!!.getString("id", "")
        Constants.country = shared!!.getString("country", "")

        userNameTV.text = shared!!.getString("user_name", "")
        mailIDTV.text = shared!!.getString("email_id", "")

        consumerToolbarLayout.visibility = View.VISIBLE
        supplierToolbarLayout.visibility = View.GONE
        ConsumerFragment.type = "all"
//        ConsumerNestedFragment.type = "all"
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ConsumerFragment())
            .commitAllowingStateLoss()
        bottom_navigation.selectedItemId = R.id.action_item1
        repo!!.enableSupplier.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    if (jsonObject.getBoolean("collect_data")) {
                        val intent = Intent(this, RegisterSupplierActivity::class.java)
                        intent.putExtra("init_data", jsonObject.getString("init_data"))
                        startActivity(intent)
                    } else {
                        val editor = getSharedPreferences(PREF, MODE_PRIVATE).edit()
                        editor.putString("supplier_enable", "true")
                        editor.apply()
                        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, SupplierFragment())
                            .commit()
                        expandIV.visibility = View.VISIBLE
                        shareIB.visibility = View.VISIBLE
                        supplierMenuLayout.visibility = View.VISIBLE
                        supplierSwitch.isChecked = true
                        supplierEnableLayout.visibility = View.GONE
                    }
                }
            }
        })

        repo!!.disableSupplier.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    supplierSwitch.isChecked = false
                    val editor = getSharedPreferences(PREF, MODE_PRIVATE).edit()
                    editor.putString("supplier_enable", "false")
                    editor.apply()
                }
            }
        })

        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_item1 -> {
                    consumerToolbarLayout.visibility = View.VISIBLE
                    supplierToolbarLayout.visibility = View.GONE
                    supplierEnableLayout.visibility = View.GONE
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ConsumerFragment())
                        .commitAllowingStateLoss()
                    fragment_container.visibility = View.VISIBLE
                }
                R.id.action_item2 -> {
                    supplierToolbarLayout.visibility = View.VISIBLE
                    consumerToolbarLayout.visibility = View.GONE

                    if (isSubSupplier) {
                        shopNameTV.text = staffShopName
                        supplierSwitch.visibility = View.INVISIBLE
                        shareIB.visibility = View.INVISIBLE
                        supplierMenuLayout.visibility = View.INVISIBLE
                        expandIV.visibility = View.GONE
                        supplierEnableLayout.visibility = View.GONE
                    } else {
                        if (shared!!.getString("supplier_enable", "") == "true") {
                            shopNameTV.text = shared!!.getString("shop_name", "")
                            supplierSwitch.isChecked = true
                            shareIB.visibility = View.VISIBLE
                            supplierMenuLayout.visibility = View.VISIBLE
                            expandIV.visibility = View.VISIBLE
                        }

                        if (shared!!.getString("shop_name", "") == "") {
                            supplierEnableLayout.visibility = View.VISIBLE
                        } else {
                            shareIB.visibility = View.VISIBLE
                            supplierMenuLayout.visibility = View.VISIBLE
                            expandIV.visibility = View.VISIBLE
                        }
                    }

                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, SupplierFragment())
                        .commitAllowingStateLoss()
                }
            }
            return@setOnNavigationItemSelectedListener true
        }

        try {
            if (intent.getStringExtra("from") == "Notification") {
                val tokenNo = intent.getStringExtra("token_no")
                val addedDateTime = intent.getStringExtra("added_datetime")
                val user_id = intent.getStringExtra("user_id")
                Log.e(TAG, "Received for : " + intent.getStringExtra("for"))
                if (intent.getStringExtra("for") == "order" || intent.getStringExtra("for") == "chat_supplier") {
                    bottom_navigation.selectedItemId = R.id.action_item2
                    val intent = Intent(this, OrderInfoActivity::class.java)
                    intent.putExtra("from", "notification")
                    intent.putExtra("token_no", tokenNo)
                    intent.putExtra("added_datetime", addedDateTime)
                    intent.putExtra("user_id", user_id)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, MyOrdersActivity::class.java)
                    startActivity(intent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        menuLayout.setOnClickListener(this)
        clientMenuLayout.setOnClickListener(this)
        supplierMenuLayout.setOnClickListener(this)
        profileLayout.setOnClickListener(this)
        consumerTV.setOnClickListener(this)
        supplierTV.setOnClickListener(this)
        editProfileTV.setOnClickListener(this)
        myOrdersTV.setOnClickListener(this)
        iamDeliveringTV.setOnClickListener(this)
        aboutUsTV.setOnClickListener(this)
        addProductTV.setOnClickListener(this)
        viewProductsTV.setOnClickListener(this)
        quickPriceUpdateTV.setOnClickListener(this)
        orderHistoryTV.setOnClickListener(this)
        editSellerProfileTV.setOnClickListener(this)
        supportTV.setOnClickListener(this)
        expandIV.setOnClickListener(this)
        switchLayout.setOnClickListener(this)
        shopsToggleTV.setOnClickListener(this)
        shareIB.setOnClickListener(this)
        safetyMeasurementTV.setOnClickListener(this)
        locationChangeIV.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.menuLayout -> {
                drawer.openDrawer(GravityCompat.START)
            }
            R.id.clientMenuLayout -> {
                showClientOptionsDialog()
            }
            R.id.supplierMenuLayout -> {
                showSupplierEditDialog()
            }
            R.id.profileLayout -> {
                drawer.closeDrawer(GravityCompat.START)
                val intent = Intent(this@MainFragmentActivity, UpdateProfileActivity::class.java)
                intent.putExtra("init_data", stateDistrictList)
                startActivity(intent)
            }
            R.id.supportTV -> {
                drawer.closeDrawer(GravityCompat.START)
                val intent = Intent(this@MainFragmentActivity, SupportActivity::class.java)
                startActivity(intent)
            }
            R.id.consumerTV -> {
                drawer.closeDrawer(GravityCompat.START)
                consumerToolbarLayout.visibility = View.VISIBLE
                supplierToolbarLayout.visibility = View.GONE
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ConsumerFragment())
                    .commit()
                bottom_navigation.selectedItemId = R.id.action_item1
            }
            R.id.editProfileTV -> {
                drawer.closeDrawer(GravityCompat.START)
                val intent = Intent(this@MainFragmentActivity, UpdateProfileActivity::class.java)
                intent.putExtra("init_data", stateDistrictList)
                startActivity(intent)
            }
            R.id.myOrdersTV -> {
                drawer.closeDrawer(GravityCompat.START)
                val intent = Intent(this, MyOrdersActivity::class.java)
                startActivity(intent)
            }
            R.id.supplierTV -> {
                drawer.closeDrawer(GravityCompat.START)
                supplierToolbarLayout.visibility = View.VISIBLE
                consumerToolbarLayout.visibility = View.GONE
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, SupplierFragment())
                    .commit()
                bottom_navigation.selectedItemId = R.id.action_item2
            }
            R.id.iamDeliveringTV -> {
                drawer.closeDrawer(GravityCompat.START)
            }
            R.id.aboutUsTV -> {
                val intent = Intent(this, AboutUsActivity::class.java)
                startActivity(intent)
                drawer.closeDrawer(GravityCompat.START)
            }
            R.id.addProductTV -> {
                val intent = Intent(this, ProductSelectionActivity::class.java)
                intent.putExtra("from", "edit")
                startActivity(intent)
                drawer.closeDrawer(GravityCompat.START)
            }
            R.id.viewProductsTV -> {
                val intent = Intent(this, ViewSupplierProductActivity::class.java)
                startActivity(intent)
                drawer.closeDrawer(GravityCompat.START)
            }
            R.id.quickPriceUpdateTV -> {
                val intent = Intent(this, UpdateProductActivity::class.java)
                startActivity(intent)
                drawer.closeDrawer(GravityCompat.START)
            }
            R.id.orderHistoryTV -> {
                val intent = Intent(this, SupplierOrderHistoryActivity::class.java)
                startActivity(intent)
                drawer.closeDrawer(GravityCompat.START)
            }
            R.id.editSellerProfileTV -> {
                val intent = Intent(this, EditSupplierActivity::class.java)
                startActivity(intent)
                drawer.closeDrawer(GravityCompat.START)
            }
            R.id.expandIV -> {
                if (navExpandLayout.visibility == View.GONE) {
                    val anim = ObjectAnimator.ofFloat(expandIV, "rotation", 0f, 180f)
                    anim.duration = 500
                    anim.start()

                    navExpandLayout.visibility = View.VISIBLE
                } else {
                    val anim = ObjectAnimator.ofFloat(expandIV, "rotation", 180f, 0f)
                    anim.duration = 500
                    anim.start()

                    navExpandLayout.visibility = View.GONE
                }
            }
            R.id.switchLayout -> {
                if (!supplierSwitch.isChecked) {
                    if (shared!!.getString("shop_name", "") != "") {
                        showSellerShopOpenDialog()
                    } else {
                        showSupplierEnableDialog()
                    }
                } else {
                    showSupplierDisableDialog()
                }
            }
            R.id.shopsToggleTV -> {
                if (shopsToggleTV.text == "All") {
                    shopsToggleTV.text = "Open"
                    ConsumerFragment.type = "open"
//                    ConsumerNestedFragment.type = "open"
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ConsumerFragment())
                        .commitAllowingStateLoss()
                } else if (shopsToggleTV.text == "Open") {
                    shopsToggleTV.text = "All"
                    ConsumerFragment.type = "all"
//                    ConsumerNestedFragment.type = "all"
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ConsumerFragment())
                        .commitAllowingStateLoss()
                }
            }
            R.id.shareIB -> {
                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.supplier_share_dialog)

                val shareTamilBT = dialog.findViewById(R.id.shareTamilBT) as Button
                val shareEnglishBT = dialog.findViewById(R.id.shareEnglishBT) as Button

                shareTamilBT.setOnClickListener {
                    dialog.dismiss()
                    generateBranchTamil()
                }

                shareEnglishBT.setOnClickListener {
                    dialog.dismiss()
                    generateBranchEnglish()
                }
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
                val window = dialog.window!!
                window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            R.id.safetyMeasurementTV -> {
                val intent = Intent(this, SafetyMeasureActivity::class.java)
                startActivity(intent)
                drawer.closeDrawer(GravityCompat.START)
            }
            R.id.locationChangeIV -> {
                val intent = Intent(this, AddressChangeMapActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun showSupplierEnableDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.supplier_enable_dialog)

        val cancelBT = dialog.findViewById(R.id.cancelBT) as Button
        val okBT = dialog.findViewById(R.id.okBT) as Button

        cancelBT.setOnClickListener {
            supplierSwitch.isChecked = false
            dialog.dismiss()
        }

        okBT.setOnClickListener {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnected == true
            if (!isConnected) {
                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
            } else {
                progressLayout.visibility = View.VISIBLE
                val obj = JSONObject()
                obj.put("user_id", shared!!.getString("id", ""))
                repo!!.enableSupplier(obj.toString())
                dialog.dismiss()
            }
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val window = dialog.window!!
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun showSellerShopOpenDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.seller_shop_open_dialog)

        val cancelBT = dialog.findViewById(R.id.cancelBT) as Button
        val okBT = dialog.findViewById(R.id.okBT) as Button

        cancelBT.setOnClickListener {
            supplierSwitch.isChecked = false
            dialog.dismiss()
        }

        okBT.setOnClickListener {
            progressLayout.visibility = View.VISIBLE
            val obj = JSONObject()
            obj.put("user_id", shared!!.getString("id", ""))
            repo!!.enableSupplier(obj.toString())
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val window = dialog.window!!
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun showSupplierDisableDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.supplier_disable_dialog)

        val cancelBT = dialog.findViewById(R.id.cancelBT) as Button
        val okBT = dialog.findViewById(R.id.okBT) as Button

        cancelBT.setOnClickListener {
            dialog.dismiss()
        }

        okBT.setOnClickListener {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnected == true
            if (!isConnected) {
                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
            } else {
                progressLayout.visibility = View.VISIBLE
                val obj = JSONObject()
                obj.put("user_id", shared!!.getString("id", ""))
                repo!!.disableSupplier(obj.toString())
                dialog.dismiss()
            }
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val window = dialog.window!!
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun showSupplierEditDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.supplier_edit_dialog)

        val storeNameTV = dialog.findViewById(R.id.storeNameTV) as TextView
        val quickPriceBT = dialog.findViewById(R.id.quickPriceBT) as LinearLayout
        val addProductBT = dialog.findViewById(R.id.addProductBT) as LinearLayout
        val viewProductsBT = dialog.findViewById(R.id.viewProductsBT) as LinearLayout
        val quickPriceUpdateBT = dialog.findViewById(R.id.quickPriceUpdateBT) as LinearLayout
        val orderHistoryBT = dialog.findViewById(R.id.orderHistoryBT) as LinearLayout
        val editProfileBT = dialog.findViewById(R.id.editProfileBT) as LinearLayout

        storeNameTV.text = shared!!.getString("shop_name", "")

        addProductBT.setOnClickListener {
            val intent = Intent(this, ProductSelectionActivity::class.java)
            intent.putExtra("from", "edit")
            startActivity(intent)
            dialog.dismiss()
        }

        viewProductsBT.setOnClickListener {
            val intent = Intent(this, ViewSupplierProductActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        quickPriceUpdateBT.setOnClickListener {
            val intent = Intent(this, UpdateProductActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        editProfileBT.setOnClickListener {
            val intent = Intent(this, EditSupplierActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        orderHistoryBT.setOnClickListener {
            val intent = Intent(this, SupplierOrderHistoryActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        quickPriceBT.setOnClickListener {
            val intent = Intent(this, ProductSelectionActivity::class.java)
            intent.putExtra("from", "edit")
            startActivity(intent)
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val window = dialog.window!!
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun showClientOptionsDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.client_options_dialog)

        val userNameTV = dialog.findViewById(R.id.userNameTV) as TextView
        val myOrdersBT = dialog.findViewById(R.id.myOrdersBT) as LinearLayout
        val myProfileBT = dialog.findViewById(R.id.myProfileBT) as LinearLayout

        userNameTV.text = shared!!.getString("user_name", "")

        myOrdersBT.setOnClickListener {
            val intent = Intent(this, MyOrdersActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        myProfileBT.setOnClickListener {
            val intent = Intent(this, UpdateProfileActivity::class.java)
            intent.putExtra("init_data", stateDistrictList)
            startActivity(intent)
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val window = dialog.window!!
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e(TAG, "Request code : $requestCode")
        Log.e(TAG, "Result code : $resultCode")
        if (requestCode == 199) {
            Log.e("TAG WIFI Condition : ", (resultCode == Activity.RESULT_OK).toString())
            ActivityCompat.requestPermissions(this, Constants.getLocationPermissions()!!, requestCode)
            /*if (resultCode == Activity.RESULT_OK) {
            } else {
                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.deny_gps_dialog)

                val okBT = dialog.findViewById(R.id.okBT) as Button

                okBT.setOnClickListener {
                    loadGPS()
                    dialog.dismiss()
                }
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
                val window = dialog.window!!
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }*/
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.contains(-1)) {
            Log.e(TAG, "Permission Deny")
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.deny_permission_dialog)

            val okBT = dialog.findViewById(R.id.okBT) as Button

            okBT.setOnClickListener {
                dialog.dismiss()
                ActivityCompat.requestPermissions(this, Constants.getLocationPermissions()!!, requestCode)
            }
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            val window = dialog.window!!
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        } else if (!grantResults.contains(-1)) {
            Log.e(TAG, "Permission Granted")
//            init()
            loadMap()
        }
    }

    private fun loadStateDistrictList() {
        val obj = JSONObject()
        repo!!.getStateDistrictList(obj.toString())

        repo!!.getStateDistrictList.observe(this, androidx.lifecycle.Observer {
            run {
                stateDistrictList = it
            }
        })
    }


    private val loadSupplierFragment = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                if (shared!!.getString("supplier_enable", "") == "true") {
                    supplierSwitch.isChecked = true
                    shareIB.visibility = View.VISIBLE
                    supplierMenuLayout.visibility = View.VISIBLE
                    expandIV.visibility = View.VISIBLE
                    supplierEnableLayout.visibility = View.GONE
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, SupplierFragment())
                        .commitAllowingStateLoss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val checkSellerEnable = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                if (shared!!.getString("supplier_enable", "") == "true") {
                    supplierSwitch.isChecked = true
                    shareIB.visibility = View.VISIBLE
                    supplierMenuLayout.visibility = View.VISIBLE
                    expandIV.visibility = View.VISIBLE
                    supplierEnableLayout.visibility = View.GONE
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, SupplierFragment())
                        .commitAllowingStateLoss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val orderRefreshData = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            supplierToolbarLayout.visibility = View.VISIBLE
            consumerToolbarLayout.visibility = View.GONE

            bottom_navigation.selectedItemId = R.id.action_item2

            if (shared!!.getString("supplier_enable", "") == "true") {
                supplierSwitch.isChecked = true
                shareIB.visibility = View.VISIBLE
                supplierMenuLayout.visibility = View.VISIBLE
                expandIV.visibility = View.VISIBLE
            }

            if (shared!!.getString("shop_name", "") == "") {
                supplierEnableLayout.visibility = View.VISIBLE
            } else {
                shareIB.visibility = View.VISIBLE
                supplierMenuLayout.visibility = View.VISIBLE
                expandIV.visibility = View.VISIBLE
            }
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, SupplierFragment())
                .commitAllowingStateLoss()
        }
    }


    private val locationChanged = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            supplierToolbarLayout.visibility = View.GONE
            consumerToolbarLayout.visibility = View.VISIBLE

            bottom_navigation.selectedItemId = R.id.action_item1

            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ConsumerFragment())
                .commitAllowingStateLoss()
        }
    }

    override fun onBackPressed() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.close_app_dialog)

        val cancelBT = dialog.findViewById(R.id.cancelBT) as Button
        val okBT = dialog.findViewById(R.id.okBT) as Button

        cancelBT.setOnClickListener {
            dialog.dismiss()
        }

        okBT.setOnClickListener {
            Constants.changedLatitude = 0.0
            Constants.changedLongitude = 0.0
            super.onBackPressed()
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val window = dialog.window!!
        window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun loadMap() {
        Log.e(TAG, "Entered Load Map")
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        Log.e(TAG, "Entered On Map Ready")
        mMap = googleMap!!
        loadFusedLocation()
//        loadCurrentLocation()
    }

    @SuppressLint("MissingPermission")
    private fun loadFusedLocation() {
        Log.e(TAG, "Entered Fused Location")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                try {
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        Log.e(TAG, "Location : " + location.latitude + ", " + location.longitude)

                        latitude = location.latitude
                        longitude = location.longitude
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        Log.e(TAG, "Current Location : $latitude, $longitude")

                        currentLocationMarker = mMap.addMarker(
                            MarkerOptions().position(currentLocation).title("Current Location").icon(
                                BitmapDescriptorFactory.fromResource(R.drawable.current_location_icon)
                            )
                        )
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f))
                        init()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        Log.e(TAG, "Entered New Location")
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.e(TAG, "Entered New Location Result")
            val mLastLocation: Location = locationResult.lastLocation
            latitude = mLastLocation.latitude
            longitude = mLastLocation.longitude
            val currentLocation = LatLng(mLastLocation.latitude, mLastLocation.longitude)
            Log.e(TAG, "Current Location : $latitude, $longitude")

            currentLocationMarker = mMap.addMarker(
                MarkerOptions().position(currentLocation).title("Current Location").icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.current_location_icon)
                )
            )
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f))
            init()
        }
    }


    private fun generateBranchEnglish() {
        val contentMetadata = ContentMetadata()
        contentMetadata.addCustomMetadata("shop_name", shared!!.getString("shop_name", ""))
        contentMetadata.addCustomMetadata("seller_id", shared!!.getString("id", ""))
        val branchUniversalObject = BranchUniversalObject()
            .setCanonicalIdentifier(shared!!.getString("id", "")!!)
            .setTitle(shared!!.getString("shop_name", "")!!)
            .setContentDescription("Lemonor - Online Neighborhood Grocery Shopping")
//            .setContentImageUrl((R.drawable.logoplain))
            .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
            .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
            .setContentMetadata(contentMetadata)
        val linkProperties = LinkProperties()
            .setChannel("facebook")
            .setFeature("sharing")
        branchUniversalObject.generateShortUrl(
            this, linkProperties
        ) { url, error ->
            if (error == null) {
                Log.i("MyApp", "got my Branch link to share: $url")
                val shareContent = "\n\n" + shared!!.getString("shop_name", "") +
                        "\n" + shared!!.getString("shop_address", "") + "," +
                        "\n" + shared!!.getString("shop_district", "") +
                        ", " + shared!!.getString("shop_state", "") +
                        "\n\nWe are in Lemonor now !! \uD83C\uDF8A\uD83C\uDF89\n" +
                        "\n" +
                        "Install the app, order online and get the Groceries Safely Delivered at your Doorstep FREE ! \n" +
                        "\n" +
                        "Lets practice social distancing and stay safe."
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, "Lemonor")
                intent.putExtra(Intent.EXTRA_TEXT, url + shareContent)
                startActivity(intent)
            } else {
                Log.i(TAG, "onLinkCreate: " + error.message)
            }
        }
    }

    private fun generateBranchTamil() {
        val contentMetadata = ContentMetadata()
        contentMetadata.addCustomMetadata("shop_name", shared!!.getString("shop_name", ""))
        contentMetadata.addCustomMetadata("seller_id", shared!!.getString("id", ""))
        val branchUniversalObject = BranchUniversalObject()
            .setCanonicalIdentifier(shared!!.getString("id", "")!!)
            .setTitle(shared!!.getString("shop_name", "")!!)
            .setContentDescription("Lemonor - Online Neighborhood Grocery Shopping")
//            .setContentImageUrl((R.drawable.logoplain))
            .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
            .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
            .setContentMetadata(contentMetadata)
        val linkProperties = LinkProperties()
            .setChannel("facebook")
            .setFeature("sharing")
        branchUniversalObject.generateShortUrl(
            this, linkProperties
        ) { url, error ->
            if (error == null) {
                Log.i("MyApp", "got my Branch link to share: $url")
                val shareContent = "\n\n" + shared!!.getString("shop_name", "") +
                        "\n" + shared!!.getString("shop_address", "") + "," +
                        "\n" + shared!!.getString("shop_district", "") +
                        ", " + shared!!.getString("shop_state", "") +
                        "\n\nநாங்கள் இப்பொழுது லெமனர் சேவையுடன் இணைந்துள்ளோம்!! \uD83C\uDF8A\uD83C\uDF89\n" +
                        "\n" +
                        "செயலியை (App)  பதிவிறக்கம் செய்து, கட்டணமில்லா வீட்டு விநியோகத்தை பெற்றிடுங்கள். FREE  டோர் டெலிவரி. \n" +
                        "\n" +
                        "சமூக இடைவெளியை பின்பற்றி பாதுகாப்பாக இருப்போம்."
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, "Lemonor")
                intent.putExtra(Intent.EXTRA_TEXT, url + shareContent)
                startActivity(intent)
            } else {
                Log.i(TAG, "onLinkCreate: " + error.message)
            }
        }
    }

    private fun loadGPS() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = locationRequest?.let {
            LocationSettingsRequest.Builder()
                .addLocationRequest(it)
        }
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder?.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            Log.e("Popup", "SUCCESS")
            ActivityCompat.requestPermissions(this, Constants.getLocationPermissions()!!, requestCode)
        }
        task.addOnFailureListener { exception ->
            Log.e("Popup", "FAILURE")
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        this,
                        REQUEST_LOCATION
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.e("Popup", "EXCEPTION")
                }
            }
        }
    }
}
