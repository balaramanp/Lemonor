package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.pojo.Address
import `in`.inferon.msl.lemonor.model.pojo.Districts
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.adapter.AddressesAdapter
import android.content.*
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_update_profile.*
import kotlinx.android.synthetic.main.activity_update_profile.districtSpinner
import kotlinx.android.synthetic.main.activity_update_profile.doneBT
import kotlinx.android.synthetic.main.activity_update_profile.emailTV
import kotlinx.android.synthetic.main.activity_update_profile.femaleIV
import kotlinx.android.synthetic.main.activity_update_profile.maleIV
import kotlinx.android.synthetic.main.activity_update_profile.mobileNoET
import kotlinx.android.synthetic.main.activity_update_profile.number_picker
import kotlinx.android.synthetic.main.activity_update_profile.pincodeET
import kotlinx.android.synthetic.main.activity_update_profile.stateLayout
import kotlinx.android.synthetic.main.activity_update_profile.stateSpinner
import kotlinx.android.synthetic.main.activity_update_profile.userNameET
import org.json.JSONObject

class UpdateProfileActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = UpdateProfileActivity::class.java.simpleName
    private var selectedCountry = ""
    private var repo: Repository? = null
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    private var districtList = mutableListOf<Districts>()
    private var stateList = ArrayList<String>()
    private var districtBasedSelectedStateList = ArrayList<String>()
    private var initData = ""
    private var selectedState = ""
    private var selectedDistrict = ""
    private var selectedGender = ""
    private var districtSelectionFirstTime = true
    private var addresses = mutableListOf<Address>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(showProgressBar, IntentFilter("ShowProgressBar"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(hideProgressBar, IntentFilter("HideProgressBar"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(addEditAddress, IntentFilter("AddEditAddress"))

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

        progressLayout.visibility = View.VISIBLE
        val obj = JSONObject()
        obj.put("user_id", shared!!.getString("id", ""))
        repo!!.getUserProfileByUserID(obj.toString())

        emailTV.text = shared!!.getString("email_id", "")
        userNameET.text = shared!!.getString("user_name", "")
        mobileNoET.setText(shared!!.getString("mobile_number", ""))
        firstNameET.setText(shared!!.getString("first_name", ""))
        lastNameET.setText(shared!!.getString("last_name", ""))
        cityET.setText(shared!!.getString("city", ""))
        pincodeET.setText(shared!!.getString("pincode", ""))
        number_picker.value = shared!!.getString("age", "")!!.toInt()
        selectedCountry = shared!!.getString("country", "")!!
        selectedState = shared!!.getString("state", "")!!
        selectedDistrict = shared!!.getString("district", "")!!
        selectedGender = shared!!.getString("gender", "")!!

        Log.e(TAG, "Received Gender : $selectedGender")
        if (selectedGender == "male") {
            maleIV.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.malon))
            femaleIV.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.fimof))
        } else {
            maleIV.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.malof))
            femaleIV.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.fimon))
        }


        loadInitData()

        repo!!.getUserProfileByUserID.observe(this, androidx.lifecycle.Observer {
            progressLayout.visibility = View.GONE
            val jsonObject = JSONObject(it)
            addresses =
                    Gson().fromJson(
                        jsonObject.getString("addresses"),
                        object : TypeToken<MutableList<Address>>() {}.type
                    )
            addressesRV.layoutManager = LinearLayoutManager(this)
            val addressesAdapter = AddressesAdapter(this, addresses, this)
            addressesRV.adapter = addressesAdapter
        })

        repo!!.updateProfile.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                    doneBT.isClickable = true
                    finish()
                } else if (jsonObject.getString("status") == "mobile_number_exists") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                    doneBT.isClickable = true
                }
            }
        })

        maleIV.setOnClickListener(this)
        femaleIV.setOnClickListener(this)
        addNewAddressLayout.setOnClickListener(this)
        doneBT.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.maleIV -> {
                if (selectedGender != "male") {
                    maleIV.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.malon))
                    femaleIV.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.fimof))
                    selectedGender = "male"
                }
            }
            R.id.femaleIV -> {
                if (selectedGender == "male") {
                    maleIV.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.malof))
                    femaleIV.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.fimon))
                    selectedGender = "female"
                }
            }
            R.id.addNewAddressLayout -> {
                val intent = Intent(this@UpdateProfileActivity, AddAddressActivity::class.java)
                intent.putExtra("from", "add")
                startActivity(intent)
            }
            R.id.doneBT -> {
                if (mobileNoET.text.toString().isNotEmpty() && mobileNoET.text.toString().trim().length == 10) {
                    if (userNameET.text!!.isNotEmpty() && selectedCountry != "") {
                        progressLayout.visibility = View.VISIBLE

                        val editor = getSharedPreferences(PREF, MODE_PRIVATE).edit()
                        editor.putString("user_name", userNameET.text.toString().trim())
                        editor.putString("first_name", firstNameET.text.toString().trim())
                        editor.putString("last_name", lastNameET.text.toString().trim())
                        editor.putString("country", selectedCountry)
                        if (selectedCountry == "India") {
                            editor.putString("district", selectedDistrict)
                            editor.putString("state", selectedState)
                        } else {
                            editor.putString("district", "")
                            editor.putString("state", "")
                        }
                        editor.putString("gender", selectedGender)
                        editor.putString("city", cityET.text.toString().trim())
                        editor.putString("pincode", pincodeET.text.toString().trim())
                        editor.putString("age", number_picker.value.toString())
                        editor.putString("app_open_count", "1")
                        editor.putString("mail_count", "0")
                        editor.putString("mobile_number", mobileNoET.text.toString().trim())
                        editor.apply()

                        val obj = JSONObject()
                        obj.put("user_id", shared!!.getString("id", ""))
                        obj.put("first_name", firstNameET.text.toString().trim())
                        obj.put("last_name", lastNameET.text.toString().trim())
                        obj.put("country", selectedCountry)
                        obj.put("age", number_picker.value.toString())
                        obj.put("city", cityET.text.toString().trim())
                        obj.put("zipcode", pincodeET.text.toString().trim())
                        obj.put("state", selectedState)
                        obj.put("district", selectedDistrict)
                        obj.put("mobile_number", mobileNoET.text.toString().trim())
                        obj.put("gender", selectedGender)

                        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                        val isConnected: Boolean = activeNetwork?.isConnected == true
                        if (!isConnected) {
                            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                        } else {
                            repo!!.updateProfile(obj.toString())
                            doneBT.isClickable = false
                        }
                    } else {
                        Toast.makeText(this, "Please Enter All Data!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please Enter Valid Mobile Number!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadInitData() {
        val age = ArrayList<String>()
        for (i in 1..120) {
            age.add(i.toString())
        }

        val country = arrayListOf<String>(
            "Afghanistan",
            "Albania",
            "Algeria",
            "American Samoa",
            "Andorra",
            "Angola",
            "Anguilla",
            "Antarctica",
            "Antigua and Barbuda",
            "Argentina",
            "Armenia",
            "Aruba",
            "Australia",
            "Austria",
            "Azerbaijan",
            "Bahamas (the)",
            "Bahrain",
            "Bangladesh",
            "Barbados",
            "Belarus",
            "Belgium",
            "Belize",
            "Benin",
            "Bermuda",
            "Bhutan",
            "Bolivia (Plurinational State of)",
            "Bonaire, Sint Eustatius and Saba",
            "Bosnia and Herzegovina",
            "Botswana",
            "Bouvet Island",
            "Brazil",
            "British Indian Ocean Territory (the)",
            "Brunei Darussalam",
            "Bulgaria",
            "Burkina Faso",
            "Burundi",
            "Cabo Verde",
            "Cambodia",
            "Cameroon",
            "Canada",
            "Cayman Islands (the)",
            "Central African Republic (the)",
            "Chad",
            "Chile",
            "China",
            "Christmas Island",
            "Cocos (Keeling) Islands (the)",
            "Colombia",
            "Comoros (the)",
            "Congo (the Democratic Republic of the)",
            "Congo (the)",
            "Cook Islands (the)",
            "Costa Rica",
            "Croatia",
            "Cuba",
            "Curaçao",
            "Cyprus",
            "Czechia",
            "Côte d'Ivoire",
            "Denmark",
            "Djibouti",
            "Dominica",
            "Dominican Republic (the)",
            "Ecuador",
            "Egypt",
            "El Salvador",
            "Equatorial Guinea",
            "Eritrea",
            "Estonia",
            "Eswatini",
            "Ethiopia",
            "Falkland Islands (the) [Malvinas]",
            "Faroe Islands (the)",
            "Fiji",
            "Finland",
            "France",
            "French Guiana",
            "French Polynesia",
            "French Southern Territories (the)",
            "Gabon",
            "Gambia (the)",
            "Georgia",
            "Germany",
            "Ghana",
            "Gibraltar",
            "Greece",
            "Greenland",
            "Grenada",
            "Guadeloupe",
            "Guam",
            "Guatemala",
            "Guernsey",
            "Guinea",
            "Guinea-Bissau",
            "Guyana",
            "Haiti",
            "Heard Island and McDonald Islands",
            "Holy See (the)",
            "Honduras",
            "Hong Kong",
            "Hungary",
            "Iceland",
            "India",
            "Indonesia",
            "Iran (Islamic Republic of)",
            "Iraq",
            "Ireland",
            "Isle of Man",
            "Israel",
            "Italy",
            "Jamaica",
            "Japan",
            "Jersey",
            "Jordan",
            "Kazakhstan",
            "Kenya",
            "Kiribati",
            "Korea (the Democratic People's Republic of)",
            "Korea (the Republic of)",
            "Kuwait",
            "Kyrgyzstan",
            "Lao People's Democratic Republic (the)",
            "Latvia",
            "Lebanon",
            "Lesotho",
            "Liberia",
            "Libya",
            "Liechtenstein",
            "Lithuania",
            "Luxembourg",
            "Macao",
            "Madagascar",
            "Malawi",
            "Malaysia",
            "Maldives",
            "Mali",
            "Malta",
            "Marshall Islands (the)",
            "Martinique",
            "Mauritania",
            "Mauritius",
            "Mayotte",
            "Mexico",
            "Micronesia (Federated States of)",
            "Moldova (the Republic of)",
            "Monaco",
            "Mongolia",
            "Montenegro",
            "Montserrat",
            "Morocco",
            "Mozambique",
            "Myanmar",
            "Namibia",
            "Nauru",
            "Nepal",
            "Netherlands (the)",
            "New Caledonia",
            "New Zealand",
            "Nicaragua",
            "Niger (the)",
            "Nigeria",
            "Niue",
            "Norfolk Island",
            "Northern Mariana Islands (the)",
            "Norway",
            "Oman",
            "Pakistan",
            "Palau",
            "Palestine, State of",
            "Panama",
            "Papua New Guinea",
            "Paraguay",
            "Peru",
            "Philippines (the)",
            "Pitcairn",
            "Poland",
            "Portugal",
            "Puerto Rico",
            "Qatar",
            "Republic of North Macedonia",
            "Romania",
            "Russian Federation (the)",
            "Rwanda",
            "Réunion",
            "Saint Barthélemy",
            "Saint Helena, Ascension and Tristan da Cunha",
            "Saint Kitts and Nevis",
            "Saint Lucia",
            "Saint Martin (French part)",
            "Saint Pierre and Miquelon",
            "Saint Vincent and the Grenadines",
            "Samoa",
            "San Marino",
            "Sao Tome and Principe",
            "Saudi Arabia",
            "Senegal",
            "Serbia",
            "Seychelles",
            "Sierra Leone",
            "Singapore",
            "Sint Maarten (Dutch part)",
            "Slovakia",
            "Slovenia",
            "Solomon Islands",
            "Somalia",
            "South Africa",
            "South Georgia and the South Sandwich Islands",
            "South Sudan",
            "Spain",
            "Sri Lanka",
            "Sudan (the)",
            "Suriname",
            "Svalbard and Jan Mayen",
            "Sweden",
            "Switzerland",
            "Syrian Arab Republic",
            "Taiwan (Province of China)",
            "Tajikistan",
            "Tanzania, United Republic of",
            "Thailand",
            "Timor-Leste",
            "Togo",
            "Tokelau",
            "Tonga",
            "Trinidad and Tobago",
            "Tunisia",
            "Turkey",
            "Turkmenistan",
            "Turks and Caicos Islands (the)",
            "Tuvalu",
            "Uganda",
            "Ukraine",
            "United Arab Emirates (the)",
            "United Kingdom of Great Britain and Northern Ireland (the)",
            "United States Minor Outlying Islands (the)",
            "United States of America (the)",
            "Uruguay",
            "Uzbekistan",
            "Vanuatu",
            "Venezuela (Bolivarian Republic of)",
            "Viet Nam",
            "Virgin Islands (British)",
            "Virgin Islands (U.S.)",
            "Wallis and Futuna",
            "Western Sahara",
            "Yemen",
            "Zambia",
            "Zimbabwe",
            "Åland Islands"
        )

        /*val countryAdapter = ArrayAdapter<String>(this, R.layout.register_page_spinner_tv, country)
        countrySpinner.adapter = countryAdapter

        countrySpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                selectedCountry = country[position]
                if (selectedCountry == "India") {
                    stateLayout.visibility = View.VISIBLE
                } else {
                    stateLayout.visibility = View.GONE
                }
            }
        }

        country.forEachIndexed { index, s ->
            if (s == selectedCountry) {
                countrySpinner.setSelection(index)
            }
        }*/

        selectedCountry = "India"
        countryTV.text = selectedCountry
        stateLayout.visibility = View.VISIBLE
        val stateAdapter = ArrayAdapter<String>(this, R.layout.register_page_spinner_tv, stateList)
        stateSpinner.adapter = stateAdapter

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
                        this@UpdateProfileActivity,
                        R.layout.register_page_spinner_tv,
                        districtBasedSelectedStateList
                    )
                    districtSpinner.adapter = districtAdapter
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

    private val addEditAddress = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            progressLayout.visibility = View.VISIBLE
            val obj = JSONObject()
            obj.put("user_id", shared!!.getString("id", ""))
            repo!!.getUserProfileByUserID(obj.toString())
        }
    }
}
