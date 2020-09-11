package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.adapter.AddPageAutoCompleteAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_support.*
import kotlinx.android.synthetic.main.activity_support.backIB
import kotlinx.android.synthetic.main.activity_support.cityET
import kotlinx.android.synthetic.main.activity_support.countryATV
import kotlinx.android.synthetic.main.activity_support.femaleIV
import kotlinx.android.synthetic.main.activity_support.firstNameET
import kotlinx.android.synthetic.main.activity_support.lastNameET
import kotlinx.android.synthetic.main.activity_support.maleIV
import kotlinx.android.synthetic.main.activity_support.number_picker
import org.json.JSONObject

class SupportActivity : AppCompatActivity(), View.OnClickListener, AdapterView.OnItemClickListener {
    private val TAG = SupportActivity::class.java.simpleName
    private val PREF = "Pref"
    private var repo: Repository? = null
    private var shared: SharedPreferences? = null
    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()
    private var selectedCountry = ""
    private var selectedGender = "male"
    private val requestCode = 201
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)

        repo = Repository()
        shared = getSharedPreferences(PREF, MODE_PRIVATE)
        loadInitData()
        loadFusedLocation()

        repo!!.supportByMail.observe(this, androidx.lifecycle.Observer {
            run {
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    progressLayout.visibility = View.GONE
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        })

        backIB.setOnClickListener(this)
        maleIV.setOnClickListener(this)
        femaleIV.setOnClickListener(this)
        submitBT.setOnClickListener(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            this.requestCode -> if (permissions.size == grantResults.size) {
                updateData()
            } else {
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backIB -> {
                super.onBackPressed()
            }
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
            R.id.submitBT -> {
                if (firstNameET.text!!.isNotEmpty() && mobileNoET.text!!.isNotEmpty() && addressOneET.text!!.isNotEmpty() && cityET.text!!.isNotEmpty() && countryATV.text!!.isNotEmpty() && symptomsET.text!!.isNotEmpty() && requirementsET.text!!.isNotEmpty()) {
                    ActivityCompat.requestPermissions(this, Constants.getPermissions()!!, requestCode)
                } else {
                    Toast.makeText(this, "Please Fill All Data!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateData() {
        loadFusedLocation()
        submitBT.visibility = View.GONE
        progressLayout.visibility = View.VISIBLE
        val editor = getSharedPreferences(PREF, MODE_PRIVATE).edit()
        editor.putString("mail_count", (shared!!.getString("mail_count", "")!!.toInt() + 1).toString())
        editor.apply()

        val obj = JSONObject()
        obj.put("user_id", Constants.user_id)
        obj.put("location", "$latitude,$longitude")
        obj.put("mail_count", shared!!.getString("mail_count", ""))
        obj.put("first_name", firstNameET.text.toString().trim())
        obj.put("last_name", lastNameET.text.toString().trim())
        obj.put("gender", selectedGender)
        obj.put("age", number_picker.value.toString())
        obj.put("address_line1", addressOneET.text.toString().trim())
        obj.put("address_line2", addressTwoET.text.toString().trim())
        obj.put("landmark", landmarkET.text.toString().trim())
        obj.put("city", cityET.text.toString().trim())
        obj.put("pincode", pincodeET.text.toString().trim())
        obj.put("country", selectedCountry)
        obj.put("symptoms", symptomsET.text.toString().trim())
        obj.put("requirements", requirementsET.text.toString().trim())
        obj.put("mobile_no", mobileNoET.text.toString().trim())
        obj.put("mobile_no_alternate", alterMobileNoET.text.toString().trim())

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        if (!isConnected) {
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
        } else {
            repo!!.supportByMail(obj.toString())
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

        /*val ageAdapter = ArrayAdapter<String>(this, R.layout.spinner_tv, age)
        ageSpinner.adapter = ageAdapter
        ageSpinner.setSelection(44)

        ageSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                selectedAge = age[position]
            }
        }*/

        val adapter = AddPageAutoCompleteAdapter<String>(this, country)
        countryATV.setAdapter(adapter)
        countryATV.threshold = 0
        countryATV.onItemClickListener = this
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
        selectedCountry = p0!!.getItemAtPosition(pos).toString()
    }
}
