package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.pojo.Districts
import `in`.inferon.msl.lemonor.repo.Repository
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.*
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_register_supplier.*
import kotlinx.android.synthetic.main.activity_register_supplier.saveBT
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RegisterSupplierActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = RegisterSupplierActivity::class.java.simpleName
    private var repo: Repository? = null
    private var initData = ""
    private var selectedState = ""
    private var selectedDistrict = ""
    private var districtList = mutableListOf<Districts>()
    private var stateList = ArrayList<String>()
    private var districtBasedSelectedStateList = ArrayList<String>()
    private var latitude = ""
    private var longitude = ""
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    private var firstTime = true
    private var openTime = ""
    private var closeTime = ""
    private var location = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_supplier)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(locationMarker, IntentFilter("LocationMarker"))

        repo = Repository()
        shared = getSharedPreferences(PREF, MODE_PRIVATE)

        initData = intent.getStringExtra("init_data")
        Log.e(TAG, "Received Init Data : $initData")

        val jsonObject = JSONObject(initData)
        districtList =
            Gson().fromJson(
                jsonObject.getString("state_district_list"),
                object : TypeToken<MutableList<Districts>>() {}.type
            )
        Log.e(TAG, "District Item : " + districtList[0].district)

        stateList = Gson().fromJson(
            jsonObject.getString("state_list"),
            object : TypeToken<ArrayList<String>>() {}.type
        )

        val stateAdapter = ArrayAdapter<String>(this, R.layout.add_page_spinner_tv, stateList)
        stateSpinner.adapter = stateAdapter

        stateList.forEachIndexed { index, s ->
            if (s == "Tamil Nadu") {
                selectedState = stateList[index]
                stateSpinner.setSelection(index)
            }
        }

        stateSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (!firstTime) {
                    selectedState = stateList[position]

                    districtBasedSelectedStateList.clear()
                    for (i in districtList) {
                        if (i.state == selectedState) {
                            districtBasedSelectedStateList.add(i.district)
                        }
                    }

                    val districtAdapter = ArrayAdapter<String>(
                        this@RegisterSupplierActivity,
                        R.layout.add_page_spinner_tv,
                        districtBasedSelectedStateList
                    )
                    districtSpinner.adapter = districtAdapter
                }
            }
        }

        districtBasedSelectedStateList.clear()
        for (i in districtList) {
            if (i.state == selectedState) {
                districtBasedSelectedStateList.add(i.district)
            }
        }

        val districtAdapter = ArrayAdapter<String>(
            this@RegisterSupplierActivity,
            R.layout.add_page_spinner_tv,
            districtBasedSelectedStateList
        )
        districtSpinner.adapter = districtAdapter

        districtBasedSelectedStateList.forEachIndexed { index, s ->
            if (s == "Erode") {
                selectedDistrict = stateList[index]
                districtSpinner.setSelection(index)
            }
        }

        districtSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                selectedDistrict = districtBasedSelectedStateList[position]
                firstTime = false
            }
        }


        repo!!.saveSupplierData.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    checkSellerEnable()

                    val editor = getSharedPreferences(PREF, MODE_PRIVATE).edit()
                    editor.putString("shop_name", shopNameET.text.toString().trim())
                    editor.putString("shop_description", shopDescriptionEt.text.toString().trim())
                    editor.putString("shop_address", addressET.text.toString().trim())
                    editor.putString("shop_city", cityATV.text.toString().trim())
                    editor.putString("shop_district", selectedDistrict)
                    editor.putString("shop_state", selectedState)
                    editor.putString("shop_pincode", pincodeET.text.toString().trim())
                    editor.putString("shop_location", "$latitude, $longitude")
                    editor.putString("shop_mobile_no", mobileNoET.text.toString().trim())
                    editor.putString("shop_alternative_no", alterMobileNoET.text.toString().trim())
                    editor.putString("shop_contact_person_name", contactPersonNameEt.text.toString().trim())
                    editor.putString("shop_opening_time", openingTimeTV.text.toString().trim() + ":00")
                    editor.putString("shop_closing_time", closingTimeTV.text.toString().trim() + ":00")
                    editor.putString("service_radius", serviceRadiusTV.text.toString().trim())
                    editor.putBoolean("inter_district_flag", true)
                    /*if (interDistrictRB.isChecked) {
                        editor.putBoolean("inter_district_flag", true)
                    } else {
                        editor.putBoolean("inter_district_flag", false)
                    }*/
                    editor.putString("supplier_enable", "true")
                    editor.apply()

                    val intent = Intent(this, ProductSelectionActivity::class.java)
                    intent.putExtra("from", "register")
                    startActivity(intent)
                    finish()
                } else if (jsonObject.getString("status") == "error") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                } else if (jsonObject.getString("status") == "duplicate_mobile_no") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                }
            }
        })

        backIB.setOnClickListener(this)
        openingTimeTV.setOnClickListener(this)
        closingTimeTV.setOnClickListener(this)
        minusTV.setOnClickListener(this)
        plusTV.setOnClickListener(this)
        markLocationBT.setOnClickListener(this)
        updateLocationBT.setOnClickListener(this)
        saveBT.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backIB -> {
                super.onBackPressed()
            }
            R.id.openingTimeTV -> {
                showTimerPicker("openingTime")
            }
            R.id.closingTimeTV -> {
                showTimerPicker("closingTime")
            }
            R.id.minusTV -> {
                if (serviceRadiusTV.text.toString().trim().toInt() > 5) {
                    val serviceRadius = serviceRadiusTV.text.toString().toInt()
                    serviceRadiusTV.text = (serviceRadius - 1).toString()
                } else {
                    Toast.makeText(this, "You reached minimum Service Radius", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.plusTV -> {
                if (serviceRadiusTV.text.toString().trim().toInt() in 5..98) {
                    val serviceRadius = serviceRadiusTV.text.toString().toInt()
                    serviceRadiusTV.text = (serviceRadius + 1).toString()
                } else {
                    Toast.makeText(this, "You reached maximum Service Radius", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.markLocationBT -> {
                val intent = Intent(this@RegisterSupplierActivity, ShopMarkerMapActivity::class.java)
                intent.putExtra("from", "register")
                intent.putExtra("location", "")
                startActivity(intent)
            }
            R.id.updateLocationBT -> {
                val intent = Intent(this@RegisterSupplierActivity, ShopMarkerMapActivity::class.java)
                intent.putExtra("from", "edit")
                intent.putExtra("location", location)
                startActivity(intent)
            }
            R.id.saveBT -> {
                val connectivityManager =
                    getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                val isConnected: Boolean = activeNetwork?.isConnected == true
                if (!isConnected) {
                    Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                } else {
                    if (mobileNoET.text!!.isNotEmpty() && mobileNoET.text!!.length == 10) {
                        if (alterMobileNoET.text!!.isEmpty() || alterMobileNoET.text!!.length >= 10) {
                            if (mobileNoET.text.toString().trim() != alterMobileNoET.text.toString().trim()) {
                                if (latitude != "" && longitude != "") {
                                    if (shopNameET.text!!.isNotEmpty() && contactPersonNameEt.text!!.isNotEmpty() && addressET.text!!.isNotEmpty() &&
                                        cityATV.text!!.isNotEmpty() && selectedDistrict != "" && selectedState != ""
                                        && serviceRadiusTV.text.toString().trim() != ""
                                    ) {
                                        progressLayout.visibility = View.VISIBLE
                                        val obj = JSONObject()
                                        obj.put("user_id", shared!!.getString("id", ""))
                                        obj.put("shop_name", shopNameET.text.toString().trim())
                                        obj.put("shop_description", shopDescriptionEt.text.toString().trim())
                                        obj.put("address", addressET.text.toString().trim())
                                        obj.put("city", cityATV.text.toString().trim())
                                        obj.put("district", selectedDistrict)
                                        obj.put("state", selectedState)
                                        obj.put("country", "india")
                                        obj.put("pincode", pincodeET.text.toString().trim())
                                        obj.put("location", "$latitude, $longitude")
                                        obj.put("mobile_number", mobileNoET.text.toString().trim())
                                        obj.put("alternate_mobile_number", alterMobileNoET.text.toString().trim())
                                        obj.put("contact_person_name", contactPersonNameEt.text.toString().trim())
                                        obj.put("opening_time", "$openTime:00")
                                        obj.put("closing_time", "$closeTime:00")
                                        obj.put("service_radius", serviceRadiusTV.text.toString().trim())
                                        obj.put("inter_district_flag", true)
                                        /*if (interDistrictRB.isChecked) {
                                            obj.put("inter_district_flag", true)
                                        } else {
                                            obj.put("inter_district_flag", false)
                                        }*/

                                        repo!!.saveSupplierData(obj.toString())

                                    } else {
                                        Toast.makeText(this, "Please Enter All Data!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(this, "Please Mark Your Shop Location!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Mobile Number and Alternative Mobile Number must not same!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(this, "Please Enter Valid Alternative Mobile Number!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(this, "Please Enter Valid Mobile Number!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun showTimerPicker(from: String) {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            if (from == "openingTime") {
                openTime = SimpleDateFormat("HH:mm").format(cal.time)
                openingTimeTV.text = SimpleDateFormat("hh:mm aa").format(cal.time)
            } else {
                closeTime = SimpleDateFormat("HH:mm").format(cal.time)
                closingTimeTV.text = SimpleDateFormat("hh:mm aa").format(cal.time)
            }
        }
        TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
    }

    private val locationMarker = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.e(TAG, "Location Marker Called")
            Log.e(TAG, "Latitude : " + intent.getStringExtra("latitude"))
            Log.e(TAG, "Longitude : " + intent.getStringExtra("longitude"))
            latitude = intent.getStringExtra("latitude")
            longitude = intent.getStringExtra("longitude")
            location = "$latitude, $longitude"
            markLocationBT.visibility = View.GONE
            updateLocationBT.visibility = View.VISIBLE
        }
    }

    private fun checkSellerEnable() {
        val intent = Intent("CheckSellerEnable")
        intent.putExtra("checkSellerEnable", "done")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
