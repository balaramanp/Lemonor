package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.pojo.Address
import `in`.inferon.msl.lemonor.model.pojo.MajorCategory
import `in`.inferon.msl.lemonor.model.pojo.Products
import `in`.inferon.msl.lemonor.model.pojo.Slot
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.adapter.AddressesSelectionAdapter
import `in`.inferon.msl.lemonor.view.adapter.ConfirmOrdersItemAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.android.synthetic.main.activity_order_confirmation.*
import kotlinx.android.synthetic.main.activity_order_confirmation.backIB
import kotlinx.android.synthetic.main.activity_order_confirmation.progressLayout
import kotlinx.android.synthetic.main.activity_order_confirmation.recyclerView
import kotlinx.android.synthetic.main.activity_order_confirmation.titleTV
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class OrderConfirmationActivity : AppCompatActivity(), View.OnClickListener, PaymentResultListener {

    private val TAG = OrderConfirmationActivity::class.java.simpleName
    private var supplierID = ""
    private var shopName = ""
    private var o2 = ""
    private var products = ""
    private var selectedAddressID = ""
    private var supplierDiscount = ""
    private var productsList = mutableListOf<Products>()
    private val confirmArray = JSONArray()
    private val confirmList = mutableListOf<Products>()
    private var itemCount = 0
    private var total = 0f
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    private var repo: Repository? = null
    private val UPI_PAYMENT = 0
    private var buttonLayout: LinearLayout? = null
    private var loadingLayout: LinearLayout? = null
    private lateinit var address: Address
    private lateinit var dialog: Dialog
    private var transactionRefID: String = ""
    private var addresses = mutableListOf<Address>()
    private var addressSelectionAdapter: AddressesSelectionAdapter? = null
    private var addressSelectionDialogVisible = false
    private var addressesRV: RecyclerView? = null
    private var selectedAddressPosition = 0
    private lateinit var addressDialog: Dialog
    private var slots = mutableListOf<Slot>()


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirmation)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(addEditAddress, IntentFilter("AddEditAddress"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(selectedAddress, IntentFilter("SelectedAddress"))

        repo = Repository()
        shared = getSharedPreferences(PREF, MODE_PRIVATE)

        progressLayout.visibility = View.VISIBLE
        val obj = JSONObject()
        obj.put("user_id", shared!!.getString("id", ""))
        repo!!.getUserProfileByUserID(obj.toString())

        val jobj = JSONObject()
        repo!!.getSlotForOrderDelivery(jobj.toString())

        var randString = ""
        for (i in 0..11) {
            randString += (0..10).random()
        }
        transactionRefID = shared!!.getString("mobile_number", "") +
                shared!!.getString("user_name", "") + randString
        Log.e(TAG, "Generated Transaction Ref ID : $transactionRefID")

        supplierID = intent.getStringExtra("supplier_id")!!
        shopName = intent.getStringExtra("shop_name")!!
        o2 = intent.getStringExtra("o2")!!
//        selectedAddressID = intent.getStringExtra("selectedAddressID")!!
        products = intent.getStringExtra("productsList")!!
        supplierDiscount = intent.getStringExtra("supplierDiscount")!!
        productsList = Gson().fromJson(products, object : TypeToken<MutableList<Products>>() {}.type)

        /*if (selectedAddressID != "") {
            address = Gson().fromJson(
                intent.getStringExtra("selectedAddress"),
                object : TypeToken<Address>() {}.type
            )
        }*/

        if (o2.length >= 3) {
            val obj = JSONObject()
            obj.put("product_id", shared!!.getString("o2_id", ""))
            obj.put("product_name", shared!!.getString("o2_name", ""))
            obj.put("qty", "0")
            obj.put("unit", "o2")
            obj.put("rate", "0")
            obj.put("description", o2)
            obj.put("is_o2_image_exists", "false")
            obj.put("chat", "")
            obj.put("local_name", "")
            confirmArray.put(obj)
            itemCount += 1
        }
        for (i in productsList) {
            Log.e(TAG, "Qty : ${i.qty}")
            if (i.qty != null && i.qty.toInt() > 0) {
                val obj = JSONObject()
                obj.put("product_id", i.product_id)
                obj.put("product_name", i.name)
                obj.put("local_name", i.local_name)
                obj.put("qty", i.qty)
                obj.put("unit", i.unit)
                obj.put("rate", i.rate)
                if (i.chat == null || i.chat.length == 0) {
                    obj.put("chat", "")
                } else {
                    obj.put("chat", i.chat)
                }
                confirmArray.put(obj)
                confirmList.add(i)

                if (i.discount != "" && i.discount != "0") {
                    val pre = i.rate.toFloat() / 100
                    val percentAmount: Float = (pre * i.discount.toFloat())
                    val ourPrice: Float = (i.rate.toFloat() - percentAmount)
                    total += i.qty.toInt() * ourPrice
                } else {
                    total += i.qty.toInt() * i.rate.toFloat()
                }
                itemCount += 1
            }
        }

        titleTV.text = shopName
        if (o2.length >= 3) {
            o2DescriptionTV.text = o2
        } else {
            o2DescriptionTV.visibility = View.GONE
        }
        itemCountTV.text = itemCount.toString()
        if (itemCount > 1) {
            itemTV.text = "Items"
        } else {
            itemTV.text = "Item"
        }
        totalTV.text = getString(R.string.Rs) + " " + doubleToStringNoDecimal(total.toDouble())
        if (supplierDiscount != "" && supplierDiscount != "0") {
            discountLayout.visibility = View.VISIBLE
            discountTotalLayout.visibility = View.VISIBLE
            discountTxtTV.text = "Extra Discount $supplierDiscount%"

            val pre = total / 100
            val percentAmount: Float = (pre * supplierDiscount.toFloat())
            val ourPrice: Float = (total - percentAmount)
            discountValueTV.text = "- " + doubleToStringNoDecimal(percentAmount.toDouble())

            afterDiscountValueTV.text = getString(R.string.Rs) + " " + doubleToStringNoDecimal(ourPrice.toDouble())
        } else {
            discountLayout.visibility = View.GONE
            discountTotalLayout.visibility = View.GONE
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        val confirmOrdersItemAdapter = ConfirmOrdersItemAdapter(this, confirmList)
        recyclerView.adapter = confirmOrdersItemAdapter
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.setHasFixedSize(true)

        repo!!.getUserProfileByUserID.observe(this, androidx.lifecycle.Observer {
            progressLayout.visibility = View.GONE
            val jsonObject = JSONObject(it)
            addresses =
                Gson().fromJson(
                    jsonObject.getString("addresses"),
                    object : TypeToken<MutableList<Address>>() {}.type
                )

            if (addressSelectionDialogVisible) {
                addressesRV!!.layoutManager = LinearLayoutManager(this)
                addressSelectionAdapter =
                    AddressesSelectionAdapter(this@OrderConfirmationActivity, addresses, this@OrderConfirmationActivity)
                addressesRV!!.adapter = addressSelectionAdapter
                /*addressDialog.dismiss()
                showAddressSelectionDialog()*/
            } else {
                if (addresses.size > 0) {
                    selectedAddressID = "0"
                    address = addresses[0]

                    addAddressBT.visibility = View.GONE
                    orderButtonLayout.visibility = View.VISIBLE
                    addressLayout.visibility = View.VISIBLE
                    firstNameTV.text = addresses[0].first_name + " " + addresses[0].last_name
                    /*if (addresses[0].last_name != "") {
                        lastNameTV.visibility = View.VISIBLE
                        lastNameTV.text = addresses[0].last_name
                    } else {
                        lastNameTV.visibility = View.GONE
                    }*/
                    phone1TV.text = addresses[0].phone_no1
                    if (addresses[0].phone_no2 != "") {
                        phone2TV.visibility = View.VISIBLE
                        phone2TV.text = addresses[0].phone_no2
                    } else {
                        phone2TV.visibility = View.GONE
                    }
                    address1TV.text = addresses[0].address_line_1
                    if (addresses[0].address_line_2 != "") {
                        address2TV.visibility = View.VISIBLE
                        address2TV.text = addresses[0].address_line_2
                    } else {
                        address2TV.visibility = View.GONE
                    }
                    if (addresses[0].landmark != "") {
                        landmarkTV.visibility = View.VISIBLE
                        landmarkTV.text = addresses[0].landmark
                    } else {
                        landmarkTV.visibility = View.GONE
                    }
                    pincodeTV.text = addresses[0].zip_code
                    cityTV.text = addresses[0].city
                    districtTV.text = addresses[0].district
                    stateTV.text = addresses[0].state
                    countryTV.text = addresses[0].country
                    if (addresses[0].default == "1") {
                        defaultAddressTV.visibility = View.VISIBLE
                    } else {
                        defaultAddressTV.visibility = View.GONE
                    }
                } else {
                    addressLayout.visibility = View.GONE
                    addAddressBT.visibility = View.VISIBLE
                    orderButtonLayout.visibility = View.GONE
                }
            }
        })

        repo!!.placeAnOrder.observe(this, androidx.lifecycle.Observer {
            run {
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    Toast.makeText(this, "Your Order Placed Successfully!", Toast.LENGTH_SHORT).show()
                    orderPlacedSuccessfully()
                    if (selectedAddressID != "") {
                        val intent = Intent(this, OrderSummaryActivity::class.java)
                        intent.putExtra("address", Gson().toJson(address))
                        intent.putExtra("products", products)
                        intent.putExtra("o2", o2)
                        intent.putExtra("shop_name", shopName)
                        intent.putExtra("supplierDiscount", supplierDiscount)
                        startActivity(intent)
                        finish()
                    } else {
                        finish()
                    }
                } else if (jsonObject.getString("status") == "error") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                    progressLayout.visibility = View.GONE
                }
            }
        })


        repo!!.getSlotForOrderDelivery.observe(this, androidx.lifecycle.Observer {
            val jsonObject = JSONObject(it)
            slots =
                Gson().fromJson(
                    jsonObject.getString("slots"),
                    object : TypeToken<MutableList<Slot>>() {}.type
                )
            /*for (i in slots){
                if (i.is_selected){
                    deliveryTimeTV.text = i.value + " " + i.date_text
                }
            }*/
        })

        deliveryTimeRG.setOnCheckedChangeListener { group, i ->
            Log.e(TAG, "Selected RB : $i")
        }

        backIB.setOnClickListener(this)
        changeNewAddressLayout.setOnClickListener(this)
        addAddressBT.setOnClickListener(this)
        cancelBT.setOnClickListener(this)
        okBT.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backIB -> {
                super.onBackPressed()
            }
            R.id.changeNewAddressLayout -> {
                showAddressSelectionDialog()
            }
            R.id.addAddressBT -> {
                val intent = Intent(this@OrderConfirmationActivity, AddAddressActivity::class.java)
                intent.putExtra("from", "add")
                startActivity(intent)
            }
            R.id.cancelBT -> {
                super.onBackPressed()
            }
            R.id.okBT -> {
                Log.e(TAG, "OK Button Clicked")
                val connectivityManager =
                    getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                val isConnected: Boolean = activeNetwork?.isConnected == true
                if (!isConnected) {
                    Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                } else {
                    if (selectedAddressID == "") {
                        progressLayout!!.visibility = View.VISIBLE
                        val fObj = JSONObject()
                        fObj.put("user_id", shared!!.getString("id", ""))
                        fObj.put("supplier_id", supplierID)
                        fObj.put("order_list", confirmArray)
                        fObj.put("payment_type", "CASH")
                        fObj.put("transaction_id", "")
                        fObj.put("reference_id", "")
                        fObj.put("address_id", "")
                        repo!!.placeAnOrder(fObj.toString())

                        okBT.isClickable = false
                    } else {
                        /*var selectedPaymentType = ""

                        dialog = Dialog(this)
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        dialog.setContentView(R.layout.select_payment_dialog)

                        val upiRB = dialog.findViewById(R.id.upiRB) as RadioButton
                        val netBankingRB = dialog.findViewById(R.id.netBankingRB) as RadioButton
                        val cashOnDeliveryRB = dialog.findViewById(R.id.cashOnDeliveryRB) as RadioButton
                        val cancelBT = dialog.findViewById(R.id.cancelBT) as Button
                        val proceedBT = dialog.findViewById(R.id.proceedBT) as Button
                        buttonLayout = dialog.findViewById(R.id.buttonLayout) as LinearLayout
                        loadingLayout = dialog.findViewById(R.id.loadingLayout) as LinearLayout

                        cancelBT.setOnClickListener {
                            dialog.dismiss()
                        }

                        proceedBT.setOnClickListener {
                            if (upiRB.isChecked) {
                                selectedPaymentType = "UPI"
                            } else if (cashOnDeliveryRB.isChecked) {
                                selectedPaymentType = "Cash On Delivery"
                            } else if (netBankingRB.isChecked) {
                                selectedPaymentType = "Net Banking"
                            }

                            if (selectedPaymentType != "") {
                                if (selectedPaymentType == "UPI") {
                                    buttonLayout!!.visibility = View.GONE
                                    loadingLayout!!.visibility = View.VISIBLE

                                    val uri = Uri.parse("upi://pay").buildUpon()
                                        .appendQueryParameter("pa", "9566433559@ybl")
                                        .appendQueryParameter("pn", "Praveen kumar")
                                        .appendQueryParameter("tn", "Testing")
                                        .appendQueryParameter("am", "1.00")
                                        .appendQueryParameter("cu", "INR")
                                        .appendQueryParameter("tr", transactionRefID)
                                        .appendQueryParameter(
                                            "url",
                                            Uri.parse("http://glancer.in/prototypes/flico/d117/projects_main/lemonor/dev_ops/online_payment_callback.php").path
                                        )
                                        .build()


                                    val upiPayIntent = Intent(Intent.ACTION_VIEW)
                                    upiPayIntent.data = uri

                                    // will always show a dialog to user to choose an app
                                    val chooser = Intent.createChooser(upiPayIntent, "Pay with")

                                    // check if intent resolves
                                    if (null != chooser.resolveActivity(packageManager)) {
                                        startActivityForResult(chooser, UPI_PAYMENT)
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "No UPI app found, please install one to continue",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else if (selectedPaymentType == "Net Banking") {
                                    startPayment()
                                } else if (selectedPaymentType == "Cash On Delivery") {
                                    progressLayout!!.visibility = View.VISIBLE
                                    val fObj = JSONObject()
                                    fObj.put("user_id", shared!!.getString("id", ""))
                                    fObj.put("supplier_id", supplierID)
                                    fObj.put("order_list", confirmArray)
                                    fObj.put("payment_type", "CASH")
                                    fObj.put("transaction_id", "")
                                    fObj.put("reference_id", "")
                                    repo!!.placeAnOrder(fObj.toString())

                                    okBT.isClickable = false
                                    dialog.dismiss()
                                }
                            } else {
                                Toast.makeText(this, "Please Select Payment Type!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        dialog.setCanceledOnTouchOutside(false)
                        dialog.show()
                        val window = dialog.window!!
                        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)*/


                        progressLayout!!.visibility = View.VISIBLE
                        val fObj = JSONObject()
                        fObj.put("user_id", shared!!.getString("id", ""))
                        fObj.put("supplier_id", supplierID)
                        fObj.put("order_list", confirmArray)
                        fObj.put("payment_type", "CASH")
                        fObj.put("transaction_id", "")
                        fObj.put("reference_id", "")
                        fObj.put("address_id", selectedAddressID)
                        repo!!.placeAnOrder(fObj.toString())

                        okBT.isClickable = false
                    }
                }
            }
        }
    }

    private fun orderPlacedSuccessfully() {
        val intent = Intent("OrderPlacedSuccessfully")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun doubleToStringNoDecimal(d: Double): String? {
        val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,##,###.00")
        return formatter.format(d)
    }


    //UPI Payment

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            UPI_PAYMENT -> if (Activity.RESULT_OK == resultCode || resultCode == 11) {
                if (data != null) {
                    val trxt = data.getStringExtra("response")
                    Log.e("UPI", "onActivityResult: $trxt")
                    val dataList = ArrayList<String>()
                    dataList.add(trxt)
                    upiPaymentDataOperation(dataList)
                } else {
                    Log.e("UPI", "onActivityResult: " + "Return data is null")
                    val dataList = ArrayList<String>()
                    dataList.add("nothing")
                    upiPaymentDataOperation(dataList)
                }
            } else {
                Log.d("UPI", "onActivityResult: " + "Return data is null") //when user simply back without payment
                val dataList = ArrayList<String>()
                dataList.add("nothing")
                upiPaymentDataOperation(dataList)
            }
        }
    }


    private fun upiPaymentDataOperation(data: ArrayList<String>) {
        if (isConnectionAvailable(this)) {
            var str: String? = data.toString()
            Log.e("UPIPAY", "upiPaymentDataOperation: " + str!!)
            var paymentCancel = ""
            if (str == null) str = "discard"
            var status = ""
            var approvalRefNo = ""
            val response = str.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in response.indices) {
                val equalStr = response[i].split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (equalStr.size >= 2) {
                    if (equalStr[0].toLowerCase() == "Status".toLowerCase()) {
                        status = equalStr[1].toLowerCase()
                    } else if (equalStr[0].toLowerCase() == "ApprovalRefNo".toLowerCase() || equalStr[0].toLowerCase() == "txnRef".toLowerCase()) {
                        approvalRefNo = equalStr[1]
                    }
                } else {
                    paymentCancel = "Payment cancelled by user."
                }
            }

            if (status == "success") {
                //Code to handle successful transaction here.
                Toast.makeText(this, "Transaction successful.", Toast.LENGTH_SHORT).show()
                Log.e("UPI", "responseStr: $approvalRefNo")

                progressLayout!!.visibility = View.VISIBLE
                val fObj = JSONObject()
                fObj.put("user_id", shared!!.getString("id", ""))
                fObj.put("supplier_id", supplierID)
                fObj.put("order_list", confirmArray)
                fObj.put("payment_type", "UPI")
                fObj.put("transaction_id", approvalRefNo)
                fObj.put("reference_id", transactionRefID)
                repo!!.placeAnOrder(fObj.toString())

                okBT.isClickable = false
//                dialog.dismiss()

            } else if ("Payment cancelled by user." == paymentCancel) {
                buttonLayout!!.visibility = View.VISIBLE
                loadingLayout!!.visibility = View.GONE
                Toast.makeText(this, "Payment cancelled.", Toast.LENGTH_SHORT).show()
            } else {
                buttonLayout!!.visibility = View.VISIBLE
                loadingLayout!!.visibility = View.GONE
                Toast.makeText(this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT)
                .show()
        }
    }


    companion object {

        fun isConnectionAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                val netInfo = connectivityManager.activeNetworkInfo
                if (netInfo != null && netInfo.isConnected
                    && netInfo.isConnectedOrConnecting
                    && netInfo.isAvailable
                ) {
                    return true
                }
            }
            return false
        }
    }


    //Razor Payment
    private fun startPayment() {
        /*
        *  You need to pass current activity in order to let Razorpay create CheckoutActivity
        * */
        val activity: Activity = this
        val co = Checkout()

        try {
            val options = JSONObject()
            options.put("name", "Lemonor")
            options.put("description", "Demoing Charges")
            //You can omit the image option to fetch the image from dashboard
            options.put("image", R.drawable.logo)
            options.put("currency", "INR")
            options.put("amount", "100")

            val prefill = JSONObject()
            prefill.put("email", "test@razorpay.com")
            prefill.put("contact", "9876543210")

            options.put("prefill", prefill)
            co.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }


    override fun onPaymentError(errorCode: Int, response: String?) {
        try {
            Toast.makeText(this, "Payment failed $errorCode \n $response", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentSuccess", e)
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        try {
            Toast.makeText(this, "Payment Successful $razorpayPaymentId", Toast.LENGTH_LONG).show()

            progressLayout!!.visibility = View.VISIBLE
            val fObj = JSONObject()
            fObj.put("user_id", shared!!.getString("id", ""))
            fObj.put("supplier_id", supplierID)
            fObj.put("order_list", confirmArray)
            fObj.put("payment_type", "UPI")
            fObj.put("transaction_id", razorpayPaymentId)
            fObj.put("reference_id", transactionRefID)
            repo!!.placeAnOrder(fObj.toString())

            okBT.isClickable = false

        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentSuccess", e)
        }
    }


    private fun showAddressSelectionDialog() {
        addressSelectionDialogVisible = true
        selectedAddressID = ""

        addressDialog = Dialog(this)
        addressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        addressDialog.setContentView(R.layout.address_selection_dialog)

        val addNewAddressLayout = addressDialog.findViewById(R.id.addNewAddressLayout) as TextView
        val cancelBT = addressDialog.findViewById(R.id.cancelBT) as Button
        val okBT = addressDialog.findViewById(R.id.okBT) as Button
        val closeIB = addressDialog.findViewById(R.id.closeIB) as ImageButton
        addressesRV = addressDialog.findViewById(R.id.addressesRV) as RecyclerView

        addressesRV!!.layoutManager = LinearLayoutManager(this)
        addressSelectionAdapter = AddressesSelectionAdapter(this, addresses, this)
        addressesRV!!.adapter = addressSelectionAdapter

        addNewAddressLayout.setOnClickListener {
            addressSelectionDialogVisible = true
            val intent = Intent(this@OrderConfirmationActivity, AddAddressActivity::class.java)
            intent.putExtra("from", "add")
            startActivity(intent)
        }

        closeIB.setOnClickListener {
            selectedAddressID = ""
            addressSelectionDialogVisible = false
            addressDialog.dismiss()
        }

        cancelBT.setOnClickListener {
            selectedAddressID = ""
            addressSelectionDialogVisible = false
            addressDialog.dismiss()
        }

        okBT.setOnClickListener {
            Log.e(TAG, "Selected Address ID : $selectedAddressID")
            if (selectedAddressID != "") {
                addressSelectionDialogVisible = false
            } else {
                Toast.makeText(this@OrderConfirmationActivity, "Please Select Address!", Toast.LENGTH_SHORT).show()
            }
        }

        addressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addressDialog.setCanceledOnTouchOutside(false)
        addressDialog.show()
        val window = addressDialog.window!!
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private val addEditAddress = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            progressLayout.visibility = View.VISIBLE
            val obj = JSONObject()
            obj.put("user_id", shared!!.getString("id", ""))
            repo!!.getUserProfileByUserID(obj.toString())
        }
    }

    private val selectedAddress = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            try {
                Log.e(TAG, "Address List Size  : " + addresses.size)
                Log.e(TAG, "Address Position  : " + intent.getIntExtra("position", 0))
                selectedAddressPosition = intent.getIntExtra("position", 0)
                selectedAddressID = addresses[intent.getIntExtra("position", 0)].id
                address = addresses[selectedAddressPosition]

                runOnUiThread {
                    addressDialog.dismiss()
                    addAddressBT.visibility = View.GONE
                    orderButtonLayout.visibility = View.VISIBLE
                    addressLayout.visibility = View.VISIBLE
                    firstNameTV.text =
                        addresses[selectedAddressPosition].first_name + " " + addresses[selectedAddressPosition].last_name
                    /*if (addresses[selectedAddressPosition].last_name != "") {
                        lastNameTV.visibility = View.VISIBLE
                        lastNameTV.text = addresses[selectedAddressPosition].last_name
                    } else {
                        lastNameTV.visibility = View.GONE
                    }*/
                    phone1TV.text = addresses[selectedAddressPosition].phone_no1
                    if (addresses[selectedAddressPosition].phone_no2 != "") {
                        phone2TV.visibility = View.VISIBLE
                        phone2TV.text = addresses[selectedAddressPosition].phone_no2
                    } else {
                        phone2TV.visibility = View.GONE
                    }
                    address1TV.text = addresses[selectedAddressPosition].address_line_1
                    if (addresses[selectedAddressPosition].address_line_2 != "") {
                        address2TV.visibility = View.VISIBLE
                        address2TV.text = addresses[selectedAddressPosition].address_line_2
                    } else {
                        address2TV.visibility = View.GONE
                    }
                    if (addresses[selectedAddressPosition].landmark != "") {
                        landmarkTV.visibility = View.VISIBLE
                        landmarkTV.text = addresses[selectedAddressPosition].landmark
                    } else {
                        landmarkTV.visibility = View.GONE
                    }
                    pincodeTV.text = addresses[selectedAddressPosition].zip_code
                    cityTV.text = addresses[selectedAddressPosition].city
                    districtTV.text = addresses[selectedAddressPosition].district
                    stateTV.text = addresses[selectedAddressPosition].state
                    countryTV.text = addresses[selectedAddressPosition].country
                    if (addresses[selectedAddressPosition].default == "1") {
                        defaultAddressTV.visibility = View.VISIBLE
                    } else {
                        defaultAddressTV.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
