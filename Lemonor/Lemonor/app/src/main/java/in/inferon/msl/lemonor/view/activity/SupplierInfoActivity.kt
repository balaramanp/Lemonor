package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.repo.Repository
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_shop_marker_map.*
import kotlinx.android.synthetic.main.activity_supplier_info.*
import org.json.JSONObject

class SupplierInfoActivity : AppCompatActivity(), View.OnClickListener, OnMapReadyCallback {

    private val TAG = SupplierInfoActivity::class.java.simpleName
    private var repo: Repository? = null
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    internal var latitude: Double = 0.toDouble()
    internal var longitude: Double = 0.toDouble()
    private var from = ""
    private var supplierID = ""
    private var shopName = ""
    private var mobileNo = ""
    private var alterMobileNo = ""
    private var location = ""
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supplier_info)

        repo = Repository()
        shared = getSharedPreferences(PREF, MODE_PRIVATE)
        from = intent.getStringExtra("from")
        supplierID = intent.getStringExtra("supplier_id")
        infoLayout.visibility = View.GONE

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        if (!isConnected) {
//            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, NoInternetActivity::class.java)
            startActivity(intent)
        } else {
            loadFusedLocation()

            progressLayout.visibility = View.VISIBLE
            val obj = JSONObject()
            obj.put("supplier_id", supplierID)
            repo!!.getSupplierProfileDataById(obj.toString())
        }


        repo!!.getSupplierProfileDataById.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    shopNameTV.text = jsonObject.getJSONObject("supplier_data").getString("shop_name")
                    shopDescriptionTV.text = jsonObject.getJSONObject("supplier_data").getString("shop_description")
                    contactPersonNameTV.text =
                        jsonObject.getJSONObject("supplier_data").getString("contact_person_name")
                    mobileNoTV.text = jsonObject.getJSONObject("supplier_data").getString("mobile_number")
                    if (jsonObject.getJSONObject("supplier_data").getString("alternate_number") == "") {
                        alterMobileNoTV.visibility = View.GONE
                    } else {
                        alterMobileNoTV.text = jsonObject.getJSONObject("supplier_data").getString("alternate_number")
                    }
                    addressTV.text = jsonObject.getJSONObject("supplier_data").getString("address")

                    shopName = jsonObject.getJSONObject("supplier_data").getString("shop_name")
                    mobileNo = jsonObject.getJSONObject("supplier_data").getString("mobile_number")
                    alterMobileNo = jsonObject.getJSONObject("supplier_data").getString("alternate_number")
                    location = jsonObject.getJSONObject("supplier_data").getString("location")
                    loadMap()
                    infoLayout.visibility = View.VISIBLE
                }
            }
        })

        repo!!.getIsSupplierReachableForCustomer.observe(this, androidx.lifecycle.Observer {
            run {
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    if (jsonObject.getBoolean("is_reachable")) {
                        orderBT.visibility = View.VISIBLE
                    } else {
                        orderBT.visibility = View.GONE
                    }
                }
            }
        })


        backIB.setOnClickListener(this)
        mobileNoTV.setOnClickListener(this)
        alterMobileNoTV.setOnClickListener(this)
        orderBT.setOnClickListener(this)
        navigateBT.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backIB -> {
                if (from == "supplier_list") {
                    super.onBackPressed()
                } else {
                    val intent = Intent(this@SupplierInfoActivity, MainFragmentActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            R.id.mobileNoTV -> {
                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.make_call_dialog)

                val diaUserNameTV = dialog.findViewById(R.id.diaUserNameTV) as TextView
                val diaMobileNoTV = dialog.findViewById(R.id.diaMobileNoTV) as TextView
                val diaCancelBT = dialog.findViewById(R.id.diaCancelBT) as Button
                val diaOKBT = dialog.findViewById(R.id.diaOKBT) as Button

                diaUserNameTV.text = shopName
                diaMobileNoTV.text = mobileNo

                diaCancelBT.setOnClickListener {
                    dialog.dismiss()
                }

                diaOKBT.setOnClickListener {
                    val callIntent = Intent(Intent.ACTION_DIAL)
                    callIntent.data = Uri.parse("tel:" + mobileNo)
                    startActivity(callIntent)
                }

                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
                val window = dialog.window!!
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            R.id.alterMobileNoTV -> {
                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.make_call_dialog)

                val diaUserNameTV = dialog.findViewById(R.id.diaUserNameTV) as TextView
                val diaMobileNoTV = dialog.findViewById(R.id.diaMobileNoTV) as TextView
                val diaCancelBT = dialog.findViewById(R.id.diaCancelBT) as Button
                val diaOKBT = dialog.findViewById(R.id.diaOKBT) as Button

                diaUserNameTV.text = shopName
                diaMobileNoTV.text = alterMobileNo

                diaCancelBT.setOnClickListener {
                    dialog.dismiss()
                }

                diaOKBT.setOnClickListener {
                    val callIntent = Intent(Intent.ACTION_DIAL)
                    callIntent.data = Uri.parse("tel:" + alterMobileNo)
                    startActivity(callIntent)
                }

                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
                val window = dialog.window!!
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            R.id.orderBT -> {
                val intent = Intent(this, PlaceOrderActivity::class.java)
                intent.putExtra("supplier_id", supplierID)
                intent.putExtra("shop_name", shopName)
                startActivity(intent)
            }
            R.id.navigateBT -> {
                val from = shared!!.getString("location", "")
                val to = location
                val intent = Intent(
                    android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?saddr=$from&daddr=$to")
                )
                startActivity(intent)
            }
        }
    }

    override fun onBackPressed() {
        if (from == "supplier_list") {
            super.onBackPressed()
        } else {
            val intent = Intent(this@SupplierInfoActivity, MainFragmentActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadFusedLocation() {

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
                        Log.e(TAG, "Current Location : $latitude, $longitude")

                        val jobj = JSONObject()
                        jobj.put("user_id", shared!!.getString("id", ""))
                        jobj.put("location", "$latitude, $longitude")
                        jobj.put("supplier_id", supplierID)
                        repo!!.getIsSupplierReachableForCustomer(jobj.toString())
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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

            val jobj = JSONObject()
            jobj.put("user_id", shared!!.getString("id", ""))
            jobj.put("location", "$latitude, $longitude")
            jobj.put("supplier_id", supplierID)
            repo!!.getIsSupplierReachableForCustomer(jobj.toString())
        }
    }

    private fun loadMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val locArray = location.split(",")
        val mlatitude = locArray[0].trim().toDouble()
        val mlongitude = locArray[1].trim().toDouble()
        val markedShopLocation = LatLng(mlatitude, mlongitude)
        mMap.addMarker(
            MarkerOptions().position(markedShopLocation).title(shopName).icon(
                BitmapDescriptorFactory.fromResource(R.drawable.ic_place_red_500_24dp)
            )
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(markedShopLocation))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markedShopLocation, 10f))
    }
}
