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
import java.text.ParseException


class EditSupplierActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = EditSupplierActivity::class.java.simpleName
    private var repo: Repository? = null
    private var selectedState = ""
    private var selectedDistrict = ""
    private var districtList = mutableListOf<Districts>()
    private var stateList = ArrayList<String>()
    private var districtBasedSelectedStateList = ArrayList<String>()
    private var latitude = ""
    private var longitude = ""
    private var location = ""
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    private var districtSelectionFirstTime = true
    private var openTimeHour = ""
    private var openTimeMinute = ""
    private var closeTimeHour = ""
    private var closeTimeMinute = ""
    private var openTime = ""
    private var closeTime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_supplier)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(locationMarker, IntentFilter("LocationMarker"))

        repo = Repository()
        shared = getSharedPreferences(PREF, MODE_PRIVATE)

        progressLayout.visibility = View.VISIBLE
        toolbarTV.text = "Edit Profile"
        saveBT.text = "Update"
        markLocationBT.visibility = View.GONE
        updateLocationBT.visibility = View.VISIBLE

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        if (!isConnected) {
//            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, NoInternetActivity::class.java)
            startActivity(intent)
        } else {
            val obj = JSONObject()
            obj.put("supplier_id", shared!!.getString("id", ""))
            repo!!.getSupplierProfileDataById(obj.toString())
        }


        repo!!.getSupplierProfileDataById.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    Log.e(TAG, "Data : " + jsonObject.getJSONObject("supplier_data").getString("user_id"))
                    shopNameET.setText(jsonObject.getJSONObject("supplier_data").getString("shop_name"))
                    shopDescriptionEt.setText(jsonObject.getJSONObject("supplier_data").getString("shop_description"))
                    contactPersonNameEt.setText(jsonObject.getJSONObject("supplier_data").getString("contact_person_name"))
                    addressET.setText(jsonObject.getJSONObject("supplier_data").getString("address"))
                    cityATV.setText(jsonObject.getJSONObject("supplier_data").getString("city"))
                    pincodeET.setText(jsonObject.getJSONObject("supplier_data").getString("pincode"))
                    mobileNoET.setText(jsonObject.getJSONObject("supplier_data").getString("mobile_number"))
                    alterMobileNoET.setText(jsonObject.getJSONObject("supplier_data").getString("alternate_number"))
//                    openingTimeTV.text = jsonObject.getJSONObject("supplier_data").getString("open_timing")
//                    closingTimeTV.text = jsonObject.getJSONObject("supplier_data").getString("close_timing")
                    selectedState = jsonObject.getJSONObject("supplier_data").getString("state")
                    selectedDistrict = jsonObject.getJSONObject("supplier_data").getString("district")
                    location = jsonObject.getJSONObject("supplier_data").getString("location")
                    openTimeHour = jsonObject.getJSONObject("supplier_data").getString("open_timing").substring(0, 2)
                    openTimeMinute = jsonObject.getJSONObject("supplier_data").getString("open_timing").substring(3, 5)
                    closeTimeHour = jsonObject.getJSONObject("supplier_data").getString("close_timing").substring(0, 2)
                    closeTimeMinute =
                        jsonObject.getJSONObject("supplier_data").getString("close_timing").substring(3, 5)
                    openTime = "$openTimeHour:$openTimeMinute"
                    closeTime = "$closeTimeHour:$closeTimeMinute"
                    if (jsonObject.getJSONObject("supplier_data").getString("service_radius").toInt() >= 5) {
                        serviceRadiusTV.text = jsonObject.getJSONObject("supplier_data").getString("service_radius")
                    } else {
                        serviceRadiusTV.text = "5"
                    }
                    /*interDistrictRB.isChecked =
                        jsonObject.getJSONObject("supplier_data").getString("inter_district_flag") == "1"*/
                    Log.e(TAG, "Received Selected District : $selectedDistrict")
                    Log.e(
                        TAG,
                        "Received Opening Time : " + jsonObject.getJSONObject("supplier_data").getString("open_timing")
                    )
                    Log.e(
                        TAG,
                        "Received Closing Time : " + jsonObject.getJSONObject("supplier_data").getString("close_timing")
                    )

                    val sdf = SimpleDateFormat("HH:mm")
                    try {
                        val openTimeDate = sdf.parse("$openTimeHour:$openTimeMinute")
                        val closeTimeDate = sdf.parse("$closeTimeHour:$closeTimeMinute")
                        Log.e(TAG, "Open Time Date : $openTimeDate")
                        Log.e(TAG, "Close Time Date : $closeTimeDate")
                        val ocal = Calendar.getInstance()
                        ocal.time = openTimeDate
                        openingTimeTV.text = SimpleDateFormat("hh:mm aa").format(ocal.time)

                        val ccal = Calendar.getInstance()
                        ccal.time = closeTimeDate
                        closingTimeTV.text = SimpleDateFormat("hh:mm aa").format(ccal.time)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }

                    districtList =
                        Gson().fromJson(
                            jsonObject.getJSONObject("init_data").getString("state_district_list"),
                            object : TypeToken<MutableList<Districts>>() {}.type
                        )
                    Log.e(TAG, "District Item : " + districtList[0].district)

                    stateList = Gson().fromJson(
                        jsonObject.getJSONObject("init_data").getString("state_list"),
                        object : TypeToken<ArrayList<String>>() {}.type
                    )

                    val stateAdapter = ArrayAdapter<String>(this, R.layout.add_page_spinner_tv, stateList)
                    stateSpinner.adapter = stateAdapter

                    stateList.forEachIndexed { index, s ->
                        if (s == selectedState) {
                            stateSpinner.setSelection(index)
                        }
                    }


                    districtBasedSelectedStateList.clear()
                    for (i in districtList) {
                        if (i.state == selectedState) {
                            districtBasedSelectedStateList.add(i.district)
                        }
                    }

                    val districtAdapter =
                        ArrayAdapter<String>(this, R.layout.add_page_spinner_tv, districtBasedSelectedStateList)
                    districtSpinner.adapter = districtAdapter

                    districtBasedSelectedStateList.forEachIndexed { i, s ->
                        if (s == selectedDistrict) {
                            Log.e(TAG, "Enterd IF Condition in District Selection")
                            districtSpinner.setSelection(i)
                        }
                    }
                }
            }
        })


        stateSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {

                if (!districtSelectionFirstTime) {
                    selectedState = stateList[position]
                    districtBasedSelectedStateList.clear()
                    for (i in districtList) {
                        if (i.state == selectedState) {
                            districtBasedSelectedStateList.add(i.district)
                        }
                    }

                    val districtAdapter = ArrayAdapter<String>(
                        this@EditSupplierActivity,
                        R.layout.add_page_spinner_tv,
                        districtBasedSelectedStateList
                    )
                    districtSpinner.adapter = districtAdapter
                }
            }
        }



        districtSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                selectedDistrict = districtBasedSelectedStateList[position]
                districtSelectionFirstTime = false
            }
        }


        repo!!.updateSupplierData.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    val editor = getSharedPreferences(PREF, MODE_PRIVATE).edit()
                    editor.putString("shop_name", shopNameET.text.toString().trim())
                    editor.putString("shop_description", shopDescriptionEt.text.toString().trim())
                    editor.putString("shop_address", addressET.text.toString().trim())
                    editor.putString("shop_city", cityATV.text.toString().trim())
                    editor.putString("shop_district", selectedDistrict)
                    editor.putString("shop_state", selectedState)
                    editor.putString("shop_pincode", pincodeET.text.toString().trim())
                    editor.putString("shop_location", location)
                    editor.putString("shop_mobile_no", mobileNoET.text.toString().trim())
                    editor.putString("shop_alternative_no", alterMobileNoET.text.toString().trim())
                    editor.putString("shop_contact_person_name", contactPersonNameEt.text.toString().trim())
                    editor.putString("shop_opening_time", "$openTime:00")
                    editor.putString("shop_closing_time", "$closeTime:00")
                    editor.putString("service_radius", serviceRadiusTV.text.toString().trim())
                    editor.putBoolean("inter_district_flag", true)
                    /*if (interDistrictRB.isChecked) {
                        editor.putBoolean("inter_district_flag", true)
                    } else {
                        editor.putBoolean("inter_district_flag", false)
                    }*/
                    editor.apply()

                    Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                    saveBT.isClickable = true
                    finish()
                } else if (jsonObject.getString("status") == "error") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                    saveBT.isClickable = true
                } else if (jsonObject.getString("status") == "duplicate_mobile_no") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                    saveBT.isClickable = true
                }
            }
        })

        backIB.setOnClickListener(this)
        openingTimeTV.setOnClickListener(this)
        closingTimeTV.setOnClickListener(this)
        minusTV.setOnClickListener(this)
        plusTV.setOnClickListener(this)
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
            R.id.updateLocationBT -> {
                val intent = Intent(this@EditSupplierActivity, ShopMarkerMapActivity::class.java)
                intent.putExtra("from", "edit")
                intent.putExtra("location", location)
                startActivity(intent)
            }
            R.id.saveBT -> {
                val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                val isConnected: Boolean = activeNetwork?.isConnected == true
                if (!isConnected) {
                    Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                } else {
                    if (mobileNoET.text!!.isNotEmpty() && mobileNoET.text!!.length == 10) {
                        if (alterMobileNoET.text!!.isEmpty() || alterMobileNoET.text!!.length >= 10) {
                            if (mobileNoET.text.toString().trim() != alterMobileNoET.text.toString().trim()) {
                                if (shopNameET.text!!.isNotEmpty() && contactPersonNameEt.text!!.isNotEmpty()
                                    && addressET.text!!.isNotEmpty() && cityATV.text!!.isNotEmpty()
                                    && serviceRadiusTV.text.toString().trim() != ""
                                ) {
                                    progressLayout.visibility = View.VISIBLE
                                    val obj = JSONObject()
                                    obj.put("supplier_id", shared!!.getString("id", ""))
                                    obj.put("shop_name", shopNameET.text.toString().trim())
                                    obj.put("shop_description", shopDescriptionEt.text.toString().trim())
                                    obj.put("address", addressET.text.toString().trim())
                                    obj.put("city", cityATV.text.toString().trim())
                                    obj.put("district", selectedDistrict)
                                    obj.put("state", selectedState)
                                    obj.put("country", "india")
                                    obj.put("pincode", pincodeET.text.toString().trim())
                                    obj.put("location", location)
                                    obj.put("mobile_number", mobileNoET.text.toString().trim())
                                    obj.put("alternate_mobile_number", alterMobileNoET.text.toString().trim())
                                    obj.put("contact_person_name", contactPersonNameEt.text.toString().trim())
                                    if (openTime.length <= 5) {
                                        obj.put("opening_time", "$openTime:00")
                                    } else if (openingTimeTV.text.length > 5) {
                                        obj.put("opening_time", openTime)
                                    }

                                    if (closeTime.length <= 5) {
                                        obj.put("closing_time", "$closeTime:00")
                                    } else if (closingTimeTV.text.length > 5) {
                                        obj.put("closing_time", closeTime)
                                    }
                                    obj.put("service_radius", serviceRadiusTV.text.toString().trim())
                                    obj.put("inter_district_flag", true)
                                    /*if (interDistrictRB.isChecked) {
                                        obj.put("inter_district_flag", true)
                                    } else {
                                        obj.put("inter_district_flag", false)
                                    }*/

                                    repo!!.updateSupplierData(obj.toString())
                                    saveBT.isClickable = false
                                } else {
                                    Toast.makeText(this, "Please Enter All Data!", Toast.LENGTH_SHORT).show()
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
        val sdf = SimpleDateFormat("hh:mm")
        var date: Date? = null
        try {
            if (from == "openingTime") {
                date = sdf.parse("$openTimeHour:$openTimeMinute")
            } else {
                date = sdf.parse("$closeTimeHour:$closeTimeMinute")
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val cal = Calendar.getInstance()
        cal.time = date
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
        TimePickerDialog(
            this,
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
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
}
