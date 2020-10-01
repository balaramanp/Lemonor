package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.BuildConfig
import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.pojo.Districts
import `in`.inferon.msl.lemonor.repo.Repository
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.common.AccountPicker
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.branch.referral.Branch
import io.branch.referral.BranchError
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.io.IOException
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = LoginActivity::class.java.simpleName
    private var repo: Repository? = null
    private val PREF = "Pref"
    private var selectedMailID = ""
    private var districtList = mutableListOf<Districts>()
    private var stateList = ArrayList<String>()
    private var districtBasedSelectedStateList = ArrayList<String>()
    private var selectedState = ""
    private var selectedDistrict = ""
    private var selectedGender = ""
    private var selectedCountry = ""
    internal var latitude: Double = 0.toDouble()
    internal var longitude: Double = 0.toDouble()
    private var firstTime = true
    private var shared: SharedPreferences? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var fcmToken: String = ""
    private var branchReferringParams: JSONObject? = null

    override fun onStart() {
        super.onStart()
        loadFusedLocation()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        // Branch reinit (in case Activity is already in foreground when Branch link is clicked)
        Branch.sessionBuilder(this).withCallback(branchListener).reInit()
    }

    object branchListener : Branch.BranchReferralInitListener {
        override fun onInitFinished(referringParams: JSONObject?, error: BranchError?) {
            if (error == null) {
                Log.i("BRANCH SDK", referringParams.toString())
                // Retrieve deeplink keys from 'referringParams' and evaluate the values to determine where to route the user
                // Check '+clicked_branch_link' before deciding whether to use your Branch routing logic
            } else {
                Log.e("BRANCH SDK", error.message)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        shared = getSharedPreferences(PREF, MODE_PRIVATE)
        MobileAds.initialize(this) {}
        Constants.logger = AppEventsLogger.newLogger(this)
        FacebookSdk.setAdvertiserIDCollectionEnabled(true)
        FacebookSdk.setAdvertiserIDCollectionEnabled(true)
        FacebookSdk.setAutoInitEnabled(true)
        FacebookSdk.fullyInitialize()
        Constants.logger.logEvent("Login Activity : Entered Login Activity")

        AsyncTask.execute {
            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(this)
                val adId = adInfo?.id
                Log.e("AdID", adId.toString())
            } catch (exception: IOException) {
            } catch (exception: GooglePlayServicesRepairableException) {
            } catch (exception: GooglePlayServicesNotAvailableException) {
            }
        }


        if (shared!!.getString("app_open_count", "") != "") {
            if (shared!!.getString("app_open_count", "") == "1") {
                val editor = getSharedPreferences(PREF, MODE_PRIVATE).edit()
                editor.putString("app_open_count", "2")
                editor.apply()
            } else if (shared!!.getString("app_open_count", "") == "2") {
                val editor = getSharedPreferences(PREF, MODE_PRIVATE).edit()
                editor.putString("app_open_count", "3")
                editor.apply()
            } else if (shared!!.getString("app_open_count", "") == "3") {
                val editor = getSharedPreferences(PREF, MODE_PRIVATE).edit()
                editor.putString("app_open_count", "")
                editor.apply()
            }
        } else {
            val editor = getSharedPreferences(PREF, MODE_PRIVATE).edit()
            editor.putString("app_open_count", "1")
            editor.apply()
        }

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        Log.e(TAG, "Network Connectivity : $isConnected")
        if (!isConnected) {
            splashLoadingLayout.visibility = View.GONE
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
        } else {
            if (intent.hasExtra("from_notification")) {
                val supid = intent.getStringExtra("supplier_id")
                val sopName = intent.getStringExtra("shop_name")

                val intent = Intent(this, PlaceOrderActivity::class.java)
                intent.putExtra("supplier_id", supid)
                intent.putExtra("shop_name", sopName)
                startActivity(intent)
                finish()
            } else {
                init()
            }
        }
    }

    private fun checkFCM() {
        loadingLayout.visibility = View.VISIBLE
        loginLayout.visibility = View.INVISIBLE
        registerLayout.visibility = View.INVISIBLE
        Handler().postDelayed({
            if (shared!!.getString("fcm_token", "") != "") {
                if (shared!!.getString("id", "") != "") {
                    splashLayout.visibility = View.VISIBLE
                    loadingLayout.visibility = View.GONE
                    loginLayout.visibility = View.INVISIBLE
                    registerLayout.visibility = View.INVISIBLE

                    Handler().postDelayed({
                        val intent = Intent(this@LoginActivity, MainFragmentActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }, 1000)

                } else {
                    loginLayout.visibility = View.VISIBLE
                    registerLayout.visibility = View.INVISIBLE
                    loadingLayout.visibility = View.GONE
                    splashLayout.visibility = View.GONE
                }
            } else {
                checkFCM()
            }
        }, 100)
    }

    private fun init() {
        repo = Repository()

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            fcmToken = it.token
            Log.e(TAG, "Newly Genered Token : $fcmToken")
        }

        val editor = getSharedPreferences(PREF, MODE_PRIVATE).edit()
        editor.putBoolean("typing_animation", true)
        editor.apply()


        Branch.sessionBuilder(this).withCallback { referringParams, error ->
            Constants.logger.logEvent("Login Activity : Checking App Opened Using Branch")
            Log.e(TAG, "Branch Referring Params : $referringParams")
            if (error == null) {
                if (referringParams!!.getBoolean("+clicked_branch_link")) {
                    Constants.logger.logEvent("Login Activity : App Opened Using Branch Link -> Existing User")
                    branchReferringParams = referringParams

                    if (shared!!.getString("id", "") != "") {
                        if (referringParams.getString("seller_id") == "") {
                            Constants.logger.logEvent("Login Activity : Branch With No Seller ID -> Navigated to Home Page")
                            val intent = Intent(this@LoginActivity, MainFragmentActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Constants.logger.logEvent("Login Activity : Branch With Seller ID -> Navigated to Supplier Place Order Page")
                            val intent = Intent(this@LoginActivity, PlaceOrderActivity::class.java)
                            intent.putExtra("supplier_id", referringParams.getString("seller_id"))
                            intent.putExtra("shop_name", referringParams.getString("shop_name"))
                            intent.putExtra("from", "login_page")
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Constants.logger.logEvent("Login Activity : From Branch -> Showing Login Button")
                        loginLayout.visibility = View.VISIBLE
                        registerLayout.visibility = View.INVISIBLE
                        loadingLayout.visibility = View.GONE
                        splashLayout.visibility = View.GONE
                    }
                } else {
                    loadWithoutBranch()
                }

            } else {
                Log.e("BRANCH Error SDK", error.message)
                loadWithoutBranch()
            }
        }.withData(this.intent.data).init()

        repo!!.getPublicKey.observe(this, androidx.lifecycle.Observer {
            run {
                val jsonObject = JSONObject(it)
                if (!jsonObject.getBoolean("app_update_status")) {
                    val key = jsonObject.getString("key")
                    val token = jsonObject.getString("token")
                    val encryptedEmail = encryptRSA(selectedMailID, key)

                    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                    val isConnected: Boolean = activeNetwork?.isConnected == true
                    Log.e(TAG, "Network Connectivity : $isConnected")
                    if (!isConnected) {
                        Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                    } else {
                        val obj = JSONObject()
                        obj.put("email_id", encryptedEmail)
                        obj.put("token", token)
                        obj.put("fcm_token", fcmToken)
                        repo!!.checkEmailExist(obj.toString())
                        Constants.logger.logEvent("Login Activity : Checking Mail ID Exist or Not")
                    }
                } else {
                    Constants.logger.logEvent("Login Activity : Naviaged to Force Update Page")
                    val intent = Intent(this@LoginActivity, ForceUpdateActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

            }
        })
        repo!!.checkEmailExist.observe(this, androidx.lifecycle.Observer {
            run {
                Log.e(TAG, "Check Mail Exist Response : $it")
                loadingLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "not_registered") {
                    Constants.logger.logEvent("Login Activity : Mail ID Not Registered")

                    loadRegisterLayout(jsonObject.getString("init_data"))

                } else if (jsonObject.getString("status") == "registered") {
                    Constants.logger.logEvent("Login Activity : Mail ID Already Exist")
                    val userData = jsonObject.getJSONObject("user_data")
                    Log.e(TAG, "User ID : " + userData.getString("id").toString())

                    Log.e(TAG, "Received User ID : " + userData.getString("id"))
                    val editor = getSharedPreferences(PREF, MODE_PRIVATE).edit()
                    editor.putString("id", userData.getString("id"))
                    editor.putString("user_name", userData.getString("user_name"))
                    editor.putString("first_name", userData.getString("first_name"))
                    editor.putString("last_name", userData.getString("last_name"))
                    editor.putString("mobile_number", userData.getString("mobile_number"))
                    editor.putString("email_id", userData.getString("email_id"))
                    editor.putString("country", userData.getString("country"))
                    editor.putString("state", userData.getString("state"))
                    editor.putString("district", userData.getString("district"))
                    editor.putString("gender", userData.getString("gender"))
                    editor.putString("age", userData.getString("age"))
                    editor.putString("city", userData.getString("city"))
                    editor.putString("zipcode", userData.getString("zipcode"))
                    editor.putString("app_open_count", "1")
                    editor.putString("mail_count", "0")

                    if (jsonObject.has("supplier_data")) {
                        Log.e(TAG, "Supplier Data : " + jsonObject.getString("supplier_data"))
                        val supplierData = jsonObject.getJSONObject("supplier_data")
                        editor.putString("shop_name", supplierData.getString("shop_name"))
                        editor.putString("shop_address", supplierData.getString("address"))
                        editor.putString("shop_city", supplierData.getString("city"))
                        editor.putString("shop_district", supplierData.getString("district"))
                        editor.putString("shop_state", supplierData.getString("state"))
                        editor.putString("shop_pincode", supplierData.getString("pincode"))
                        editor.putString("shop_location", supplierData.getString("location"))
                        editor.putString("shop_mobile_no", supplierData.getString("mobile_number"))
                        editor.putString("shop_alternative_no", supplierData.getString("alternate_number"))
                        editor.putString("shop_contact_person_name", supplierData.getString("contact_person_name"))
                        editor.putString("shop_opening_time", supplierData.getString("open_timing"))
                        editor.putString("shop_closing_time", supplierData.getString("close_timing"))
                        if (supplierData.getString("status") == "1") {
                            editor.putString("supplier_enable", "true")
                        } else {
                            editor.putString("supplier_enable", "false")
                        }
                    }
                    editor.apply()

                    /*val intent = Intent(this@LoginActivity, MainFragmentActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()*/


                    if (branchReferringParams != null) {
                        if (branchReferringParams!!.getBoolean("+clicked_branch_link")) {
                            Constants.logger.logEvent("Login Activity : Checking App Opened Using Branch")
                            if (userData.getString("id") != "") {
                                if (branchReferringParams!!.getString("seller_id") == "") {
                                    Constants.logger.logEvent("Login Activity : Branch With No Seller ID -> Navigated to Home Page")
                                    val intent = Intent(this@LoginActivity, MainFragmentActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Constants.logger.logEvent("Login Activity : Branch With Seller ID -> Navigated to Supplier Place Order Page")
                                    val intent = Intent(this@LoginActivity, PlaceOrderActivity::class.java)
                                    intent.putExtra("supplier_id", branchReferringParams!!.getString("seller_id"))
                                    intent.putExtra("shop_name", branchReferringParams!!.getString("shop_name"))
                                    intent.putExtra("from", "login_page")
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        } else {
                            Constants.logger.logEvent("Login Activity : Navigated to Home Page")
                            val intent = Intent(this@LoginActivity, MainFragmentActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Constants.logger.logEvent("Login Activity : Navigated to Home Page")
                        val intent = Intent(this@LoginActivity, MainFragmentActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }

                }
            }
        })

        loginLayout.setOnClickListener(this)
        maleIV.setOnClickListener(this)
        femaleIV.setOnClickListener(this)
        doneBT.setOnClickListener(this)
    }

    private fun loadWithoutBranch() {
        Constants.logger.logEvent("Login Activity : App Opened without Branch Link")
        if (shared!!.getString("id", "") != "") {
            splashLayout.visibility = View.VISIBLE
            loadingLayout.visibility = View.GONE
            loginLayout.visibility = View.INVISIBLE
            registerLayout.visibility = View.INVISIBLE

            Handler().postDelayed({
                Constants.logger.logEvent("Login Activity : Navigated to Home Page")
                val intent = Intent(this@LoginActivity, MainFragmentActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }, 1000)

        } else {
            Constants.logger.logEvent("Login Activity : Showing Login Button")
            loginLayout.visibility = View.VISIBLE
            registerLayout.visibility = View.INVISIBLE
            loadingLayout.visibility = View.GONE
            splashLayout.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.loginLayout -> {
                try {
                    val animation: Animation = AnimationUtils.loadAnimation(this, R.anim.login_bt)
                    loginLayout.startAnimation(animation)

                    animation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationRepeat(animation: Animation?) {
                        }

                        override fun onAnimationEnd(animation: Animation?) {
                            loadingLayout.visibility = View.VISIBLE
                            val googlePicker = AccountPicker.newChooseAccountIntent(
                                null,
                                null,
                                arrayOf(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE),
                                true,
                                null,
                                null,
                                null,
                                null
                            )
                            startActivityForResult(googlePicker, 101)
                            Constants.logger.logEvent("Login Activity : Login Button Clicked")
                        }

                        override fun onAnimationStart(animation: Animation?) {
                        }
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
            R.id.doneBT -> {
                if (mobileNoET.text.toString().trim().isNotEmpty() && mobileNoET.text.toString().trim().length == 10) {
                    if (userNameET.text.toString().trim().isNotEmpty() && selectedCountry != "") {
                        loadingLayout.visibility = View.VISIBLE

                        val editor = getSharedPreferences(PREF, MODE_PRIVATE).edit()
                        editor.putString("user_name", userNameET.text.toString().trim())
                        editor.putString("email_id", selectedMailID)
                        editor.putString("country", selectedCountry)
                        if (selectedCountry == "India") {
                            editor.putString("district", selectedDistrict)
                            editor.putString("state", selectedState)
                        } else {
                            editor.putString("district", "")
                            editor.putString("state", "")
                        }
                        editor.putString("gender", selectedGender)
                        editor.putString("age", number_picker.value.toString())
                        editor.putString("app_open_count", "1")
                        editor.putString("mail_count", "0")
                        editor.putString("location", "$latitude, $longitude")
                        editor.putString("mobile_number", mobileNoET.text.toString().trim())
                        editor.apply()

                        val obj = JSONObject()
                        obj.put("email_id", selectedMailID)
                        obj.put("user_name", userNameET.text.toString().trim())
                        obj.put("country", selectedCountry)
                        if (selectedCountry == "India") {
                            obj.put("district", selectedDistrict)
                            obj.put("state", selectedState)
                        } else {
                            obj.put("district", "")
                            obj.put("state", "")
                        }
                        obj.put("gender", "")
                        obj.put("age", "")
                        obj.put("location", "$latitude, $longitude")
                        obj.put("mobile_number", mobileNoET.text.toString().trim())
                        obj.put("fcm_token", fcmToken)

                        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                        val isConnected: Boolean = activeNetwork?.isConnected == true
                        Log.e(TAG, "Network Connectivity : $isConnected")
                        if (!isConnected) {
                            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                        } else {
                            repo!!.register(obj.toString())
                            doneBT.isClickable = false
                            repo!!.activityLogging("Register Test", obj.toString())
                            Constants.logger.logEvent("Login Activity : Registration Page -> Register Button Clicked")
                        }


                        repo!!.register.observe(this, androidx.lifecycle.Observer {
                            run {
                                Log.e(TAG, "Register Response : $it")
                                val jsonObject = JSONObject(it)
                                if (jsonObject.getString("status") == "ok") {
                                    loadingLayout.visibility = View.GONE

                                    val editor = getSharedPreferences(PREF, MODE_PRIVATE).edit()
                                    editor.putString("id", jsonObject.getString("user_id"))
                                    editor.apply()

                                    if (branchReferringParams!!.getBoolean("+clicked_branch_link")) {
                                        if (shared!!.getString("id", "") != "") {
                                            if (branchReferringParams!!.getString("seller_id") == "") {
                                                Constants.logger.logEvent("Login Activity : Registration Page -> Navigated to Home Page")
                                                val intent =
                                                    Intent(this@LoginActivity, MainFragmentActivity::class.java)
                                                intent.flags =
                                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                startActivity(intent)
                                                finish()
                                            } else {
                                                Constants.logger.logEvent("Login Activity : Registration Page -> Navigated to Place Order Page")
                                                val intent = Intent(this@LoginActivity, PlaceOrderActivity::class.java)
                                                intent.putExtra(
                                                    "supplier_id",
                                                    branchReferringParams!!.getString("seller_id")
                                                )
                                                intent.putExtra(
                                                    "shop_name",
                                                    branchReferringParams!!.getString("shop_name")
                                                )
                                                intent.putExtra("from", "login_page")
                                                startActivity(intent)
                                                finish()
                                            }
                                        }
                                    } else {
                                        Constants.logger.logEvent("Login Activity : Registration Page -> Navigated to Home Page")
                                        val intent = Intent(this@LoginActivity, MainFragmentActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        finish()
                                    }

                                } else if (jsonObject.getString("status") == "username_exists") {
                                    Constants.logger.logEvent("Login Activity : Registration Page -> User Name Already Exists")
                                    loadingLayout.visibility = View.GONE
                                    userNameTakenTV.visibility = View.VISIBLE
                                    doneBT.isClickable = true
                                } else if (jsonObject.getString("status") == "mobile_number_exists") {
                                    Constants.logger.logEvent("Login Activity : Registration Page -> Mobile Number Already Exists")
                                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                    doneBT.isClickable = true
                                } else if (jsonObject.getString("status") == "error") {
                                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                    doneBT.isClickable = true
                                }
                            }
                        })
                    } else {
                        Toast.makeText(this, "Please Enter All Data!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please Enter Valid Mobile Number!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e(TAG, "Request code : $requestCode")
        if (requestCode == 101) {
            try {
                Constants.logger.logEvent("Login Activity : Mail ID Selected")
                val accountName = data!!.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                Log.e("onActivityResult", accountName.toString())

                loginLayout.visibility = View.INVISIBLE

                selectedMailID = accountName.toString()


                val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                val isConnected: Boolean = activeNetwork?.isConnected == true
                Log.e(TAG, "Network Connectivity : $isConnected")
                if (!isConnected) {
                    Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                } else {
                    val obj = JSONObject()
                    obj.put("version_code", BuildConfig.VERSION_CODE)
                    repo!!.getPublicKey(obj.toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Log.e("TAG", (resultCode == Activity.RESULT_OK).toString())
        }

    }


    private fun loadRegisterLayout(initData: String) {
        Constants.logger.logEvent("Login Activity : Showing User Registration Page")
        loadFusedLocation()
        loginLayout.visibility = View.INVISIBLE
        registerLayout.visibility = View.VISIBLE
        val animation: Animation = AnimationUtils.loadAnimation(this, R.anim.translate_bottom_to_up)
        registerLayout.startAnimation(animation)

        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                emailTV.text = selectedMailID
                emailTV.visibility = View.VISIBLE
            }

            override fun onAnimationStart(animation: Animation?) {
            }

        })

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

        loadInitData()

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
        }*/

        selectedCountry = "India"
        countryTV.text = selectedCountry
        stateLayout.visibility = View.VISIBLE
        val stateAdapter = ArrayAdapter<String>(this, R.layout.register_page_spinner_tv, stateList)
        stateSpinner.adapter = stateAdapter

        stateList.forEachIndexed { index, s ->
            if (s == "Tamil Nadu") {
                stateSpinner.setSelection(index)
                selectedState = stateList[index]
            }
        }

        stateSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (!firstTime) {
                    Constants.logger.logEvent("Login Activity : Registration Page -> State Selection Changed")
                    selectedState = stateList[position]

                    districtBasedSelectedStateList.clear()
                    for (i in districtList) {
                        if (i.state == selectedState) {
                            districtBasedSelectedStateList.add(i.district)
                        }
                    }

                    val districtAdapter = ArrayAdapter<String>(
                        this@LoginActivity,
                        R.layout.register_page_spinner_tv,
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
            this@LoginActivity,
            R.layout.register_page_spinner_tv,
            districtBasedSelectedStateList
        )
        districtSpinner.adapter = districtAdapter

        districtBasedSelectedStateList.forEachIndexed { index, s ->
            if (s == "Erode") {
                districtSpinner.setSelection(index)
                selectedDistrict = districtBasedSelectedStateList[index]
            }
        }

        districtSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                Constants.logger.logEvent("Login Activity : Registration Page -> District Selection Changed")
                selectedDistrict = districtBasedSelectedStateList[position]
                firstTime = false
            }
        }




        country.forEachIndexed { index, s ->
            if (s == "India") {
                selectedCountry = country[index]
                countrySpinner.setSelection(index)
            }
        }


    }

    private fun encryptRSA(data: String, pubKey: String): String {
        var encoded = ""
        var encrypted: ByteArray? = null
        try {
            val publicBytes = Base64.decode(pubKey, Base64.DEFAULT)
            val keySpec = X509EncodedKeySpec(publicBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            val pubKey = keyFactory.generatePublic(keySpec)
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING") //or try with "RSA"
            cipher.init(Cipher.ENCRYPT_MODE, pubKey)
            encrypted = cipher.doFinal(data.toByteArray())
            encoded = Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return encoded
    }
}

