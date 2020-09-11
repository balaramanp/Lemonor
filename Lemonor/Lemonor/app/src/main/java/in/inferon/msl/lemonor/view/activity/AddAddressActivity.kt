package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.pojo.Districts
import `in`.inferon.msl.lemonor.repo.Repository
import android.annotation.SuppressLint
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
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_add_edit_address.*
import kotlinx.android.synthetic.main.activity_add_edit_address.backIB
import kotlinx.android.synthetic.main.activity_add_edit_address.districtSpinner
import kotlinx.android.synthetic.main.activity_add_edit_address.markLocationBT
import kotlinx.android.synthetic.main.activity_add_edit_address.progressLayout
import kotlinx.android.synthetic.main.activity_add_edit_address.saveBT
import kotlinx.android.synthetic.main.activity_add_edit_address.stateSpinner
import kotlinx.android.synthetic.main.activity_add_edit_address.toolbarTV
import org.json.JSONObject

class AddAddressActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = AddAddressActivity::class.java.simpleName
    private var repo: Repository? = null
    private var from = ""
    private var districtList = mutableListOf<Districts>()
    private var stateList = ArrayList<String>()
    private var districtBasedSelectedStateList = ArrayList<String>()
    private var selectedState = ""
    private var selectedDistrict = ""
    private var districtSelectionFirstTime = true
    private var latitude = ""
    private var longitude = ""
    private var location = ""
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_address)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(locationMarker, IntentFilter("LocationMarker"))

        from = intent.getStringExtra("from")!!
        if (from == "add") {
            toolbarTV.text = "Add Address"
        }
        progressLayout.visibility = View.VISIBLE
        layout.visibility = View.GONE
        repo = Repository()
        shared = getSharedPreferences(PREF, MODE_PRIVATE)
        loadStateDistrictList()


        repo!!.addAddress.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    Toast.makeText(this, "Address Added Successfully!", Toast.LENGTH_SHORT).show()
                    addEditAddress()
                    finish()
                } else if (jsonObject.getString("status") == "error") {
                    Toast.makeText(this, "Something went Wrong!", Toast.LENGTH_SHORT).show()
                }
            }
        })

        backIB.setOnClickListener(this)
        updateLocationBT.setOnClickListener(this)
        markLocationBT.setOnClickListener(this)
        saveBT.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backIB -> {
                super.onBackPressed()
            }
            R.id.updateLocationBT -> {
                val intent = Intent(this@AddAddressActivity, AddressMarkerMapActivity::class.java)
                intent.putExtra("from", "edit")
                intent.putExtra("location", location)
                startActivity(intent)
            }
            R.id.markLocationBT -> {
                val intent = Intent(this@AddAddressActivity, AddressMarkerMapActivity::class.java)
                intent.putExtra("from", "add")
                startActivity(intent)
            }
            R.id.saveBT -> {
                val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                val isConnected: Boolean = activeNetwork?.isConnected == true
                if (!isConnected) {
                    Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                } else {
                    if (firstNameET.text!!.isNotEmpty()) {
                        firstNameET.background =
                            ContextCompat.getDrawable(this, R.drawable.main_edit_tx_bg)

                        if (mobileNoET.text!!.isNotEmpty() && mobileNoET.text!!.length == 10) {
                            mobileNoLayout.background =
                                ContextCompat.getDrawable(this, R.drawable.main_edit_tx_bg)

                            if (alterMobileNoET.text!!.isEmpty() || alterMobileNoET.text!!.length >= 10) {
                                alterMobileNoLayout.background =
                                    ContextCompat.getDrawable(this, R.drawable.main_edit_tx_bg)

                                if (address1ET.text!!.isNotEmpty()) {
                                    address1ET.background =
                                        ContextCompat.getDrawable(this, R.drawable.main_edit_tx_bg)

                                    if (cityATV.text!!.isNotEmpty()) {
                                        cityATV.background =
                                            ContextCompat.getDrawable(this, R.drawable.main_edit_tx_bg)

                                        if (pincodeET.text!!.isNotEmpty() && pincodeET.text!!.length == 6) {
                                            pincodeET.background =
                                                ContextCompat.getDrawable(this, R.drawable.main_edit_tx_bg)

                                            if (selectedState != stateList[0]) {
                                                stateSpinner.background =
                                                    ContextCompat.getDrawable(this, R.drawable.main_edit_tx_bg)
                                                selectDistrictTV.visibility = View.GONE

                                                if (mobileNoET.text.toString().trim() != alterMobileNoET.text.toString().trim()) {
                                                    alterMobileNoLayout.background =
                                                        ContextCompat.getDrawable(this, R.drawable.main_edit_tx_bg)

                                                    if (firstNameET.text!!.isNotEmpty() && address1ET.text!!.isNotEmpty()
                                                        && pincodeET.text!!.isNotEmpty() && cityATV.text!!.isNotEmpty()
                                                    ) {
                                                        progressLayout.visibility = View.VISIBLE
                                                        val obj = JSONObject()
                                                        obj.put("user_id", shared!!.getString("id", ""))
                                                        obj.put("first_name", firstNameET.text.toString().trim())
                                                        obj.put("last_name", lastNameET.text.toString().trim())
                                                        obj.put("address1", address1ET.text.toString().trim())
                                                        obj.put("address2", address2ET.text.toString().trim())
                                                        obj.put("landmark", landmarkET.text.toString().trim())
                                                        obj.put("country", "india")
                                                        obj.put("state", selectedState)
                                                        obj.put("district", selectedDistrict)
                                                        obj.put("city", cityATV.text.toString().trim())
                                                        obj.put("zipcode", pincodeET.text.toString().trim())
                                                        obj.put("lat_lng", location)
                                                        obj.put("phone_no1", mobileNoET.text.toString().trim())
                                                        obj.put("phone_no2", alterMobileNoET.text.toString().trim())
                                                        if (defaultAddressCB.isChecked) {
                                                            obj.put("deafult", true)
                                                        } else {
                                                            obj.put("deafult", false)
                                                        }

                                                        repo!!.addAddress(obj.toString())
                                                        saveBT.isClickable = false
                                                    } else {
                                                        if (firstNameET.text!!.isEmpty()) {
                                                            firstNameET.background =
                                                                ContextCompat.getDrawable(
                                                                    this,
                                                                    R.drawable.un_filled_edit_tx_bg
                                                                )
                                                        }

                                                        if (address1ET.text!!.isEmpty()) {
                                                            address1ET.background =
                                                                ContextCompat.getDrawable(
                                                                    this,
                                                                    R.drawable.un_filled_edit_tx_bg
                                                                )
                                                        }

                                                        if (cityATV.text!!.isEmpty()) {
                                                            cityATV.background =
                                                                ContextCompat.getDrawable(
                                                                    this,
                                                                    R.drawable.un_filled_edit_tx_bg
                                                                )
                                                        }
                                                        Toast.makeText(
                                                            this,
                                                            "Please enter all Data!",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                            .show()
                                                    }
                                                } else {
                                                    alterMobileNoLayout.background =
                                                        ContextCompat.getDrawable(this, R.drawable.un_filled_edit_tx_bg)

                                                    Toast.makeText(
                                                        this,
                                                        "Mobile Number and Alternative Mobile Number must not same!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                stateSpinner.background =
                                                    ContextCompat.getDrawable(this, R.drawable.un_filled_edit_tx_bg)
                                                selectDistrictTV.background =
                                                    ContextCompat.getDrawable(this, R.drawable.un_filled_edit_tx_bg)

                                                Toast.makeText(
                                                    this,
                                                    "Please selected valid State and District!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            pincodeET.background =
                                                ContextCompat.getDrawable(this, R.drawable.un_filled_edit_tx_bg)

                                            Toast.makeText(
                                                this,
                                                "Please enter valid Pincode!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        cityATV.background =
                                            ContextCompat.getDrawable(this, R.drawable.un_filled_edit_tx_bg)
                                        Toast.makeText(this, "Please enter valid City!", Toast.LENGTH_SHORT).show()

                                    }
                                } else {
                                    address1ET.background =
                                        ContextCompat.getDrawable(this, R.drawable.un_filled_edit_tx_bg)
                                    Toast.makeText(this, "Please enter valid Address!", Toast.LENGTH_SHORT).show()

                                }
                            } else {
                                alterMobileNoLayout.background =
                                    ContextCompat.getDrawable(this, R.drawable.un_filled_edit_tx_bg)

                                Toast.makeText(
                                    this,
                                    "Please enter valid Alternative Mobile Number!", Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            mobileNoLayout.background =
                                ContextCompat.getDrawable(this, R.drawable.un_filled_edit_tx_bg)
                            Toast.makeText(this, "Please enter valid Mobile Number!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        firstNameET.background =
                            ContextCompat.getDrawable(this, R.drawable.un_filled_edit_tx_bg)
                        Toast.makeText(this, "Please enter valid First Name!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadStateDistrictList() {
        val obj = JSONObject()
        repo!!.getStateDistrictList(obj.toString())

        repo!!.getStateDistrictList.observe(this, androidx.lifecycle.Observer {
            progressLayout.visibility = View.GONE
            val jsonObject = JSONObject(it)
            districtList =
                Gson().fromJson(
                    jsonObject.getString("state_district_list"),
                    object : TypeToken<MutableList<Districts>>() {}.type
                )


            stateList = Gson().fromJson(
                jsonObject.getString("state_list"),
                object : TypeToken<ArrayList<String>>() {}.type
            )
            stateList.reverse()
            stateList.add("Select State")
            stateList.reverse()

            loadInitData()
            layout.visibility = View.VISIBLE
        })
    }

    private fun loadInitData() {
        val selectedCountry = "India"
        val stateAdapter = ArrayAdapter<String>(this, R.layout.register_page_spinner_tv, stateList)
        stateSpinner.adapter = stateAdapter

        stateSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                selectedState = stateList[position]
                if (selectedState != stateList[0]) {
                    selectDistrictTV.visibility = View.GONE
                    districtSpinner.visibility = View.VISIBLE
                    districtBasedSelectedStateList.clear()
                    for (i in districtList) {
                        if (i.state == selectedState) {
                            districtBasedSelectedStateList.add(i.district)
                        }
                    }

                    val districtAdapter = ArrayAdapter<String>(
                        this@AddAddressActivity,
                        R.layout.register_page_spinner_tv,
                        districtBasedSelectedStateList
                    )
                    districtSpinner.adapter = districtAdapter
                } else {
                    selectDistrictTV.visibility = View.VISIBLE
                    districtSpinner.visibility = View.GONE
                }
            }
        }

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

        val districtAdapter = ArrayAdapter<String>(this, R.layout.add_page_spinner_tv, districtBasedSelectedStateList)
        districtSpinner.adapter = districtAdapter

        districtSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                Log.e(TAG, "Distict Spinner Called")
                selectedDistrict = districtBasedSelectedStateList[position]
                districtSelectionFirstTime = false
            }
        }

        districtBasedSelectedStateList.forEachIndexed { index, s ->
            if (s == selectedDistrict) {
                districtSpinner.setSelection(index)
            }
        }
    }

    private val locationMarker = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.e(TAG, "Location Marker Called")
            Log.e(TAG, "Latitude : " + intent.getStringExtra("latitude"))
            Log.e(TAG, "Longitude : " + intent.getStringExtra("longitude"))
            latitude = intent.getStringExtra("latitude")!!
            longitude = intent.getStringExtra("longitude")!!
            location = "$latitude, $longitude"
            markLocationBT.visibility = View.GONE
            updateLocationBT.visibility = View.VISIBLE
        }
    }

    private fun addEditAddress() {
        val intent = Intent("AddEditAddress")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
