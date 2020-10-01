package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.Constants.context
import `in`.inferon.msl.lemonor.model.Utils
import `in`.inferon.msl.lemonor.model.pojo.Order
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.adapter.ClientOrdersAdapter
import `in`.inferon.msl.lemonor.view.adapter.SupplierOrdersItemAdapter
import `in`.inferon.msl.lemonor.view.adapter.SupplierOrdersItemWithRejectAdapter
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
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
import kotlinx.android.synthetic.main.activity_my_orders.*
import kotlinx.android.synthetic.main.activity_order_info.*
import kotlinx.android.synthetic.main.activity_order_info.backIB
import kotlinx.android.synthetic.main.activity_order_info.progressLayout
import kotlinx.android.synthetic.main.activity_order_info.recyclerView
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class OrderInfoActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = OrderInfoActivity::class.java.simpleName
    private var orderData = ""
    private var supplierDiscount = ""
    private var order = mutableListOf<Order>()
    private var totPrice = 0f
    private var itemCount = 0
    private var repo: Repository? = null
    private var changesDone = false
    private var supplierOrdersItemWithRejectAdapter: SupplierOrdersItemWithRejectAdapter? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_info)
        repo = Repository()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(showProgressBar, IntentFilter("ShowProgressBar"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(hideProgressBar, IntentFilter("HideProgressBar"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(updateTotalPrice, IntentFilter("UpdateTotalPrice"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(chat, IntentFilter("Chat"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(orderModificationDone, IntentFilter("OrderModificationDone"))

        val from = intent.getStringExtra("from")
        if (from == "history") {
            val orderStatus = intent.getStringExtra("order_status")
            if (orderStatus == "true") {
                acceptCancelLayout.visibility = View.GONE
            } else {
                acceptCancelLayout.visibility = View.VISIBLE
            }

            orderData = intent.getStringExtra("data")!!
            Log.e(TAG, "Received Data : $orderData")
            supplierDiscount = intent.getStringExtra("supplierDiscount")!!

            order = Gson().fromJson(orderData, object : TypeToken<MutableList<Order>>() {}.type)
            Log.e(TAG, "Received Order Data : " + order[0].token_number)
            init()
        } else if (from == "notification") {
            val token_no = intent.getStringExtra("token_no")
            val added_datetime = intent.getStringExtra("added_datetime")
            val user_id = intent.getStringExtra("user_id")
            Log.e(TAG, "Received Token No : $token_no")

            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnected == true
            if (!isConnected) {
                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
            } else {
                progressLayout.visibility = View.VISIBLE
                val jobj = JSONObject()
                jobj.put("token_number", token_no)
                jobj.put("added_datetime", added_datetime)
                jobj.put("user_id", user_id)
                repo!!.getOrderByTokenId(jobj.toString())
            }

        } else if (from == "live") {
            orderData = intent.getStringExtra("data")!!
            Log.e(TAG, "Received Data : $orderData")
            Log.e(TAG,"Received Supplier Discount : " + intent.getStringExtra("supplierDiscount")!!)
            supplierDiscount = intent.getStringExtra("supplierDiscount")!!

            order = Gson().fromJson(JSONObject(orderData).getString("productsList"), object : TypeToken<MutableList<Order>>() {}.type)
            Log.e(TAG, "Received Order Data : " + order[0].token_number)
            init()
        }

        repo!!.getOrderByTokenId.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    order = Gson().fromJson(
                        jsonObject.getString("order_list"),
                        object : TypeToken<MutableList<Order>>() {}.type
                    )
                    supplierDiscount = jsonObject.getString("supplier_discount")
                    if (order.size > 0) {
                        if (jsonObject.getBoolean("is_completed")) {
                            acceptCancelLayout.visibility = View.GONE
                        } else {
                            acceptCancelLayout.visibility = View.VISIBLE
                        }
                        init()
                    }
                } else if (jsonObject.getString("status") == "invalid_token") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                } else if (jsonObject.getString("status") == "error") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                }
            }
        })

    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        totPrice = 0f
        itemCount = 0

        tokenTV.text = order[0].token_number
        userNameTV.text = order[0].user_name
        mobileNoTV.text = order[0].mobile_number
        orderDateTV.text = order[0].formatted_date + "   " + order[0].formatted_time

        for (i in order) {
            if (i.order_status == "open" || i.order_status == "supplier_accepted" || i.order_status == "completed") {
                totPrice += i.price.toFloat()
                itemCount += 1
            }
        }

        totalPriceTV.text = getString(R.string.Rs) + " " + doubleToStringTwoDecimal(totPrice.toDouble())
        itemCountTV.text = itemCount.toString()
        if (itemCount > 1) {
            itemTV.text = "Items"
        } else {
            itemTV.text = "Item"
        }

        if (totPrice > 0) {
            totalPriceLayout.visibility = View.VISIBLE
        } else {
            totalPriceLayout.visibility = View.GONE
        }

        Log.e("TAG", "Live Order Supplier Discount : $supplierDiscount")
        if (supplierDiscount != "" && supplierDiscount != "0" && supplierDiscount != null) {
            discountLayout.visibility = View.VISIBLE
            discountTotalLayout.visibility = View.VISIBLE
            discountTxtTV.text = "Extra Discount $supplierDiscount%"

            val pre = totPrice / 100
            val percentAmount: Float = (pre * supplierDiscount.toFloat())
            val ourPrice: Float = (totPrice - percentAmount)
            discountValueTV.text = "- " + doubleToStringTwoDecimal(percentAmount.toDouble())

            afterDiscountValueTV.text =
                context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(ourPrice.toDouble())
        } else {
            discountLayout.visibility = View.GONE
            discountTotalLayout.visibility = View.GONE
        }

        for (i in order) {
            if (i.order_status == "open") {
                acceptBT.visibility = View.VISIBLE
                editBT.visibility = View.VISIBLE
                orderCompleteBT.visibility = View.GONE
            } else if (i.order_status == "supplier_accepted") {
                acceptBT.visibility = View.GONE
                editBT.visibility = View.VISIBLE
                orderCompleteBT.visibility = View.VISIBLE
            } else if (i.order_status == "completed") {
                acceptCancelLayout.visibility = View.GONE
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        supplierOrdersItemWithRejectAdapter = SupplierOrdersItemWithRejectAdapter(this, order, this)
        recyclerView.adapter = supplierOrdersItemWithRejectAdapter
        recyclerView.isNestedScrollingEnabled = false


        backIB.setOnClickListener(this)
        callLayout.setOnClickListener(this)
        cancelBT.setOnClickListener(this)
        acceptBT.setOnClickListener(this)
        editBT.setOnClickListener(this)
        orderCompleteBT.setOnClickListener(this)
    }

    override fun onBackPressed() {
        if (changesDone) {
            super.onBackPressed()
            refreshData()
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backIB -> {
                if (changesDone) {
                    super.onBackPressed()
                    refreshData()
                } else {
                    super.onBackPressed()
                }
            }
            R.id.callLayout -> {
                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.make_call_dialog)

                val diaUserNameTV = dialog.findViewById(R.id.diaUserNameTV) as TextView
                val diaMobileNoTV = dialog.findViewById(R.id.diaMobileNoTV) as TextView
                val diaCancelBT = dialog.findViewById(R.id.diaCancelBT) as Button
                val diaOKBT = dialog.findViewById(R.id.diaOKBT) as Button

                diaUserNameTV.text = order[0].user_name
                diaMobileNoTV.text = order[0].mobile_number

                diaCancelBT.setOnClickListener {
                    dialog.dismiss()
                }

                diaOKBT.setOnClickListener {
                    val callIntent = Intent(Intent.ACTION_DIAL)
                    callIntent.data = Uri.parse("tel:" + order[0].mobile_number)
                    startActivity(callIntent)
                }

                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
                val window = dialog.window!!
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            R.id.cancelBT -> {
                cancelBT.isClickable = false
                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.cancel_order_status_dialog)

                val titleTV = dialog.findViewById<TextView>(R.id.titleTV)
                val userNameTV = dialog.findViewById<TextView>(R.id.userNameTV)
                val mobileNoTV = dialog.findViewById<TextView>(R.id.mobileNoTV)
                val recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerView)
                val itemTV = dialog.findViewById<TextView>(R.id.itemTV)
                val itemCountTV = dialog.findViewById<TextView>(R.id.itemCountTV)
                val totalPriceTV = dialog.findViewById<TextView>(R.id.totalPriceTV)
                val discountLayout = dialog.findViewById<LinearLayout>(R.id.discountLayout)
                val discountTxtTV = dialog.findViewById<TextView>(R.id.discountTxtTV)
                val discountValueTV = dialog.findViewById<TextView>(R.id.discountValueTV)
                val discountTotalLayout = dialog.findViewById<LinearLayout>(R.id.discountTotalLayout)
                val afterDiscountValueTV = dialog.findViewById<TextView>(R.id.afterDiscountValueTV)
                val rejectReasonET = dialog.findViewById<EditText>(R.id.rejectReasonET)
                val ccancelBT = dialog.findViewById<Button>(R.id.cancelBT)
                val okBT = dialog.findViewById<Button>(R.id.okBT)
                val closeIB = dialog.findViewById<ImageButton>(R.id.closeIB)

                titleTV.text = "Reject Order"
                userNameTV.text = order[0].user_name
                mobileNoTV.text = order[0].mobile_number

                totPrice = 0f
                itemCount = 0
                for (i in order) {
                    if (i.order_status == "open" || i.order_status == "supplier_accepted") {
                        totPrice += i.price.toFloat()
                        itemCount += 1
                    }
                }
                itemCountTV.text = itemCount.toString()
                if (itemCount > 1) {
                    itemTV.text = "Items"
                } else {
                    itemTV.text = "Item"
                }
                totalPriceTV.text = getString(R.string.Rs) + " " + doubleToStringTwoDecimal(totPrice.toDouble())
                if (supplierDiscount != "" && supplierDiscount != "0" && supplierDiscount != null) {
                    discountLayout.visibility = View.VISIBLE
                    discountTotalLayout.visibility = View.VISIBLE
                    discountTxtTV.text = "Extra Discount $supplierDiscount%"

                    val pre = totPrice / 100
                    val percentAmount: Float = (pre * supplierDiscount.toFloat())
                    val ourPrice: Float = (totPrice - percentAmount)
                    discountValueTV.text = "- " + doubleToStringTwoDecimal(percentAmount.toDouble())

                    afterDiscountValueTV.text =
                        context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(ourPrice.toDouble())
                } else {
                    discountLayout.visibility = View.GONE
                    discountTotalLayout.visibility = View.GONE
                }

                val dialogOrder = mutableListOf<Order>()
                for (i in order) {
                    if (i.order_status == "open" || i.order_status == "supplier_accepted") {
                        dialogOrder.add(i)
                    }
                }
                recyclerView.layoutManager = LinearLayoutManager(this)
                val supplierOrdersItemAdapter = SupplierOrdersItemAdapter(this, dialogOrder, this)
                recyclerView.adapter = supplierOrdersItemAdapter

                closeIB.setOnClickListener {
                    dialog.dismiss()
                    cancelBT.isClickable = true
                }

                ccancelBT.setOnClickListener {
                    dialog.dismiss()
                    cancelBT.isClickable = true
                }

                okBT.setOnClickListener {
                    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                    val isConnected: Boolean = activeNetwork?.isConnected == true
                    if (!isConnected) {
                        Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                    } else {
                        val obj = JSONObject()
                        obj.put("token_number", order[0].token_number)
                        obj.put("added_datetime", order[0].added_datetime)
                        obj.put("user_id", order[0].user_id)
                        if (rejectReasonET.text.toString().trim().length > 0) {
                            obj.put("reject_reason", rejectReasonET.text.toString().trim())
                        } else {
                            obj.put("reject_reason", "")
                        }
                        repo!!.updateSupplierRejectedByTokenNumber(obj.toString())
                        cancelBT.isClickable = false
                    }

                    repo!!.updateSupplierRejectedByTokenNumber.observe(this, androidx.lifecycle.Observer {
                        run {
                            val jsonObject = JSONObject(it)
                            if (jsonObject.getString("status") == "ok") {
                                changesDone = true
                                /*for (i in order) {
                                    if (i.order_status == "open" || i.order_status == "supplier_accepted") {
                                        i.order_status = "supplier_rejected"
                                    }
                                }
                                supplierOrdersItemWithRejectAdapter!!.notifyDataSetChanged()
                                acceptCancelLayout.visibility = View.GONE
                                cancelBT.isClickable = true*/
                                progressLayout.visibility = View.VISIBLE
                                val obj = JSONObject()
                                obj.put("token_number", order[0].token_number)
                                obj.put("added_datetime", order[0].added_datetime)
                                obj.put("user_id", order[0].user_id)
                                repo!!.getOrderByTokenId(obj.toString())

                                Toast.makeText(this, "Order Rejected Successfully!", Toast.LENGTH_SHORT).show()
                            } else if (jsonObject.getString("status") == "error") {
                                Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                cancelBT.isClickable = true
                            } else if (jsonObject.getString("status") == "invalid_token") {
                                Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                cancelBT.isClickable = true
                            } else if (jsonObject.getString("status") == "cannot_be_done") {
                                changesDone = true
                                progressLayout.visibility = View.VISIBLE
                                val obj = JSONObject()
                                obj.put("token_number", order[0].token_number)
                                obj.put("added_datetime", order[0].added_datetime)
                                obj.put("user_id", order[0].user_id)
                                repo!!.getOrderByTokenId(obj.toString())
                            }

                            dialog.dismiss()
                        }
                    })
                }

                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.setCanceledOnTouchOutside(false)
                dialog.setCancelable(false)
                dialog.show()
                val window = dialog.window!!
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            R.id.acceptBT -> {
                if (itemCount > 0) {
                    acceptBT.isClickable = false
                    val dialog = Dialog(this)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setContentView(R.layout.order_status_dialog)

                    val titleTV = dialog.findViewById<TextView>(R.id.titleTV)
                    val userNameTV = dialog.findViewById<TextView>(R.id.userNameTV)
                    val mobileNoTV = dialog.findViewById<TextView>(R.id.mobileNoTV)
                    val recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerView)
                    val itemCountTV = dialog.findViewById<TextView>(R.id.itemCountTV)
                    val itemTV = dialog.findViewById<TextView>(R.id.itemTV)
                    val totalPriceTV = dialog.findViewById<TextView>(R.id.totalPriceTV)
                    val discountLayout = dialog.findViewById<LinearLayout>(R.id.discountLayout)
                    val discountTxtTV = dialog.findViewById<TextView>(R.id.discountTxtTV)
                    val discountValueTV = dialog.findViewById<TextView>(R.id.discountValueTV)
                    val discountTotalLayout = dialog.findViewById<LinearLayout>(R.id.discountTotalLayout)
                    val afterDiscountValueTV = dialog.findViewById<TextView>(R.id.afterDiscountValueTV)
                    val cancelBT = dialog.findViewById<Button>(R.id.cancelBT)
                    val okBT = dialog.findViewById<Button>(R.id.okBT)
                    val closeIB = dialog.findViewById<ImageButton>(R.id.closeIB)

                    titleTV.text = "Accept Order"
                    userNameTV.text = order[0].user_name
                    mobileNoTV.text = order[0].mobile_number

                    totPrice = 0f
                    itemCount = 0
                    for (i in order) {
                        if (i.order_status == "open" || i.order_status == "supplier_accepted") {
                            totPrice += i.price.toFloat()
                            itemCount += 1
                        }
                    }
                    itemCountTV.text = itemCount.toString()
                    if (itemCount > 1) {
                        itemTV.text = "Items"
                    } else {
                        itemTV.text = "Item"
                    }
                    totalPriceTV.text = getString(R.string.Rs) + " " + doubleToStringTwoDecimal(totPrice.toDouble())
                    if (supplierDiscount != "" && supplierDiscount != "0" && supplierDiscount != null) {
                        discountLayout.visibility = View.VISIBLE
                        discountTotalLayout.visibility = View.VISIBLE
                        discountTxtTV.text = "Extra Discount $supplierDiscount%"

                        val pre = totPrice / 100
                        val percentAmount: Float = (pre * supplierDiscount.toFloat())
                        val ourPrice: Float = (totPrice - percentAmount)
                        discountValueTV.text = "- " + doubleToStringTwoDecimal(percentAmount.toDouble())

                        afterDiscountValueTV.text =
                            context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(ourPrice.toDouble())
                    } else {
                        discountLayout.visibility = View.GONE
                        discountTotalLayout.visibility = View.GONE
                    }

                    val dialogOrder = mutableListOf<Order>()
                    for (i in order) {
                        if (i.order_status == "open") {
                            dialogOrder.add(i)
                        }
                    }
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    val supplierOrdersItemAdapter = SupplierOrdersItemAdapter(this, dialogOrder, this)
                    recyclerView.adapter = supplierOrdersItemAdapter

                    closeIB.setOnClickListener {
                        dialog.dismiss()
                        acceptBT.isClickable = true
                    }

                    cancelBT.setOnClickListener {
                        dialog.dismiss()
                        acceptBT.isClickable = true
                    }

                    okBT.setOnClickListener {
                        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                        val isConnected: Boolean = activeNetwork?.isConnected == true
                        if (!isConnected) {
                            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                        } else {
                            acceptBT.isClickable = false
                            val obj = JSONObject()
                            obj.put("token_number", order[0].token_number)
                            obj.put("added_datetime", order[0].added_datetime)
                            obj.put("user_id", order[0].user_id)
                            repo!!.updateSupplierAcceptedByTokenNumber(obj.toString())
                        }

                        repo!!.updateSupplierAcceptedByTokenNumber.observe(this, androidx.lifecycle.Observer {
                            run {
                                val jsonObject = JSONObject(it)
                                if (jsonObject.getString("status") == "ok") {
                                    changesDone = true
                                    /*acceptBT.visibility = View.GONE
                                    orderCompleteBT.visibility = View.VISIBLE
                                    for (i in order) {
                                        if (i.order_status == "open") {
                                            i.order_status = "supplier_accepted"
                                        }
                                    }
                                    supplierOrdersItemWithRejectAdapter!!.notifyDataSetChanged()
                                    acceptBT.isClickable = true*/

                                    progressLayout.visibility = View.VISIBLE
                                    val obj = JSONObject()
                                    obj.put("token_number", order[0].token_number)
                                    obj.put("added_datetime", order[0].added_datetime)
                                    obj.put("user_id", order[0].user_id)
                                    repo!!.getOrderByTokenId(obj.toString())

                                    Toast.makeText(this, "Order Accepted Successfully!", Toast.LENGTH_SHORT).show()
                                } else if (jsonObject.getString("status") == "error") {
                                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                    acceptBT.isClickable = true
                                } else if (jsonObject.getString("status") == "invalid_token") {
                                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                    acceptBT.isClickable = true
                                } else if (jsonObject.getString("status") == "cannot_be_done") {
                                    changesDone = true
                                    progressLayout.visibility = View.VISIBLE
                                    val obj = JSONObject()
                                    obj.put("token_number", order[0].token_number)
                                    obj.put("added_datetime", order[0].added_datetime)
                                    obj.put("user_id", order[0].user_id)
                                    repo!!.getOrderByTokenId(obj.toString())
                                }

                                dialog.dismiss()
                            }
                        })
                    }

                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.setCancelable(false)
                    dialog.show()
                    val window = dialog.window!!
                    window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                }
            }
            R.id.orderCompleteBT -> {
                var o2Exist = false
                var des = ""
                var ordID = ""
                var pos = 0
                var o2Open = true
                order.forEachIndexed { index, o ->
                    if (o.product_name == "O2") {
                        des = o.description
                        ordID = o.order_id
                        pos = index
                        o2Exist = true
                        if (o.order_status != "supplier_accepted") {
                            o2Open = false
                        }
                    }
                }


                if (o2Exist && o2Open) {
                    orderCompleteBT.isClickable = false
                    val dialog = Dialog(this)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setContentView(R.layout.o2_order_status_dialog)

                    val o2DescriptionTV = dialog.findViewById<TextView>(R.id.o2DescriptionTV)
                    val o2PriceET = dialog.findViewById<EditText>(R.id.o2PriceET)
                    val loadingLayout = dialog.findViewById<LinearLayout>(R.id.loadingLayout)
                    val o2AcceptCancelLayout = dialog.findViewById<LinearLayout>(R.id.o2AcceptCancelLayout)
                    val cancelBT = dialog.findViewById<Button>(R.id.cancelBT)
                    val okBT = dialog.findViewById<Button>(R.id.okBT)
                    val closeIB = dialog.findViewById<ImageButton>(R.id.closeIB)

                    o2DescriptionTV.text = des

                    closeIB.setOnClickListener {
                        dialog.dismiss()
                        orderCompleteBT.isClickable = true
                    }

                    cancelBT.setOnClickListener {
                        dialog.dismiss()
                        orderCompleteBT.isClickable = true
                    }

                    okBT.setOnClickListener {
                        if (o2PriceET.text.toString().trim().length > 0) {
                            val obj = JSONObject()
                            obj.put("order_id", ordID)
                            obj.put("price", o2PriceET.text.toString().trim())
//                        repo!!.updateO2PriceFromSupplierInOrder(obj.toString())
                            loadingLayout.visibility = View.VISIBLE
                            o2AcceptCancelLayout.visibility = View.GONE

                            Utils.getRetrofit().updateO2PriceFromSupplierInOrder(obj.toString()).enqueue(object :
                                Callback<ResponseBody> {
                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    t.printStackTrace()
                                }

                                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                    if (response.isSuccessful) {
                                        val responseString = response.body()!!.string()
                                        Log.e(
                                            "TAG",
                                            "Update O2 Price From Supplier In Order Response : $responseString"
                                        )
                                        val jsonObject = JSONObject(responseString)
                                        if (jsonObject.getString("status") == "ok") {
                                            changesDone = true
                                            order[pos].price = o2PriceET.text.toString().trim()
                                            supplierOrdersItemWithRejectAdapter!!.notifyDataSetChanged()

                                            totPrice = 0f
                                            itemCount = 0
                                            for (i in order) {
                                                if (i.order_status == "open" || i.order_status == "supplier_accepted") {
                                                    totPrice += i.price.toFloat()
                                                    itemCount += 1
                                                }
                                            }
                                            totalPriceTV.text =
                                                getString(R.string.Rs) + " " + doubleToStringTwoDecimal(totPrice.toDouble())
                                            if (supplierDiscount != "" && supplierDiscount != "0" && supplierDiscount != null) {
                                                discountLayout.visibility = View.VISIBLE
                                                discountTotalLayout.visibility = View.VISIBLE
                                                discountTxtTV.text = "Extra Discount $supplierDiscount%"

                                                val pre = totPrice / 100
                                                val percentAmount: Float = (pre * supplierDiscount.toFloat())
                                                val ourPrice: Float = (totPrice - percentAmount)
                                                discountValueTV.text = "- " + doubleToStringTwoDecimal(percentAmount.toDouble())

                                                afterDiscountValueTV.text =
                                                    context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(ourPrice.toDouble())
                                            } else {
                                                discountLayout.visibility = View.GONE
                                                discountTotalLayout.visibility = View.GONE
                                            }
                                            itemCountTV.text = itemCount.toString()
                                            if (itemCount > 1) {
                                                itemTV.text = "Items"
                                            } else {
                                                itemTV.text = "Item"
                                            }
                                            dialog.dismiss()

                                            val cDialog = Dialog(this@OrderInfoActivity)
                                            cDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                            cDialog.setContentView(R.layout.order_status_dialog)

                                            val titleTV = cDialog.findViewById<TextView>(R.id.titleTV)
                                            val userNameTV = cDialog.findViewById<TextView>(R.id.userNameTV)
                                            val mobileNoTV = cDialog.findViewById<TextView>(R.id.mobileNoTV)
                                            val recyclerView = cDialog.findViewById<RecyclerView>(R.id.recyclerView)
                                            val itemCountTV = cDialog.findViewById<TextView>(R.id.itemCountTV)
                                            val itemTV = cDialog.findViewById<TextView>(R.id.itemTV)
                                            val totalPriceTV = cDialog.findViewById<TextView>(R.id.totalPriceTV)
                                            val discountLayout = cDialog.findViewById<LinearLayout>(R.id.discountLayout)
                                            val discountTxtTV = cDialog.findViewById<TextView>(R.id.discountTxtTV)
                                            val discountValueTV = cDialog.findViewById<TextView>(R.id.discountValueTV)
                                            val discountTotalLayout = cDialog.findViewById<LinearLayout>(R.id.discountTotalLayout)
                                            val afterDiscountValueTV = cDialog.findViewById<TextView>(R.id.afterDiscountValueTV)
                                            val ccancelBT = cDialog.findViewById<Button>(R.id.cancelBT)
                                            val cokBT = cDialog.findViewById<Button>(R.id.okBT)
                                            val ccloseIB = cDialog.findViewById<ImageButton>(R.id.closeIB)

                                            titleTV.text = "Complete Order"
                                            userNameTV.text = order[0].user_name
                                            mobileNoTV.text = order[0].mobile_number
                                            totPrice = 0f
                                            itemCount = 0
                                            for (i in order) {
                                                if (i.order_status == "open" || i.order_status == "supplier_accepted") {
                                                    totPrice += i.price.toFloat()
                                                    itemCount += 1
                                                }
                                            }
                                            itemCountTV.text = itemCount.toString()
                                            if (itemCount > 1) {
                                                itemTV.text = "Items"
                                            } else {
                                                itemTV.text = "Item"
                                            }

                                            totalPriceTV.text =
                                                getString(R.string.Rs) + " " + doubleToStringTwoDecimal(totPrice.toDouble())
                                            if (supplierDiscount != "" && supplierDiscount != "0" && supplierDiscount != null) {
                                                discountLayout.visibility = View.VISIBLE
                                                discountTotalLayout.visibility = View.VISIBLE
                                                discountTxtTV.text = "Extra Discount $supplierDiscount%"

                                                val pre = totPrice / 100
                                                val percentAmount: Float = (pre * supplierDiscount.toFloat())
                                                val ourPrice: Float = (totPrice - percentAmount)
                                                discountValueTV.text = "- " + doubleToStringTwoDecimal(percentAmount.toDouble())

                                                afterDiscountValueTV.text =
                                                    context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(ourPrice.toDouble())
                                            } else {
                                                discountLayout.visibility = View.GONE
                                                discountTotalLayout.visibility = View.GONE
                                            }

                                            val dialogOrder = mutableListOf<Order>()
                                            for (i in order) {
                                                if (i.order_status == "supplier_accepted") {
                                                    dialogOrder.add(i)
                                                }
                                            }
                                            recyclerView.layoutManager = LinearLayoutManager(this@OrderInfoActivity)
                                            val csupplierOrdersItemAdapter =
                                                SupplierOrdersItemAdapter(
                                                    this@OrderInfoActivity,
                                                    dialogOrder,
                                                    this@OrderInfoActivity
                                                )
                                            recyclerView.adapter = csupplierOrdersItemAdapter

                                            ccloseIB.setOnClickListener {
                                                cDialog.dismiss()
                                                orderCompleteBT.isClickable = true
                                            }

                                            ccancelBT.setOnClickListener {
                                                cDialog.dismiss()
                                                orderCompleteBT.isClickable = true
                                            }

                                            cokBT.setOnClickListener {
                                                val connectivityManager =
                                                    getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                                                val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                                                val isConnected: Boolean = activeNetwork?.isConnected == true
                                                if (!isConnected) {
                                                    Toast.makeText(
                                                        this@OrderInfoActivity,
                                                        "No Internet Connection!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    orderCompleteBT.isClickable = false
                                                    val cobj = JSONObject()
                                                    cobj.put("token_number", order[0].token_number)
                                                    cobj.put("added_datetime", order[0].added_datetime)
                                                    cobj.put("user_id", order[0].user_id)
                                                    repo!!.updateSupplierCompletedByTokenNumber(cobj.toString())
                                                }

                                                repo!!.updateSupplierCompletedByTokenNumber.observe(
                                                    this@OrderInfoActivity,
                                                    androidx.lifecycle.Observer {
                                                        run {
                                                            val cjsonObject = JSONObject(it)
                                                            if (cjsonObject.getString("status") == "ok") {
                                                                changesDone = true
                                                                /*for (i in order) {
                                                                    if (i.order_status == "supplier_accepted") {
                                                                        i.order_status = "completed"
                                                                    }
                                                                }
                                                                acceptCancelLayout.visibility = View.GONE
                                                                orderCompleteBT.isClickable = true
                                                                supplierOrdersItemWithRejectAdapter!!.notifyDataSetChanged()*/

                                                                progressLayout.visibility = View.VISIBLE
                                                                val obj = JSONObject()
                                                                obj.put("token_number", order[0].token_number)
                                                                obj.put("added_datetime", order[0].added_datetime)
                                                                obj.put("user_id", order[0].user_id)
                                                                repo!!.getOrderByTokenId(obj.toString())

                                                                Toast.makeText(
                                                                    this@OrderInfoActivity,
                                                                    "Order Completed Successfully!",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            } else if (cjsonObject.getString("status") == "error") {
                                                                Toast.makeText(
                                                                    this@OrderInfoActivity,
                                                                    cjsonObject.getString("msg"),
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                orderCompleteBT.isClickable = true
                                                            } else if (cjsonObject.getString("status") == "invalid_token") {
                                                                Toast.makeText(
                                                                    this@OrderInfoActivity,
                                                                    cjsonObject.getString("msg"),
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                orderCompleteBT.isClickable = true
                                                            } else if (cjsonObject.getString("status") == "cannot_be_done") {
                                                                changesDone = true
                                                                progressLayout.visibility = View.VISIBLE
                                                                val obj = JSONObject()
                                                                obj.put("token_number", order[0].token_number)
                                                                obj.put("added_datetime", order[0].added_datetime)
                                                                obj.put("user_id", order[0].user_id)
                                                                repo!!.getOrderByTokenId(obj.toString())
                                                            }

                                                            cDialog.dismiss()

                                                        }
                                                    })
                                            }

                                            cDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                            cDialog.setCanceledOnTouchOutside(false)
                                            cDialog.setCancelable(false)
                                            cDialog.show()
                                            val window = cDialog.window!!
                                            window.setLayout(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                            )
                                        } else if (jsonObject.getString("status") == "error") {
                                            Toast.makeText(
                                                this@OrderInfoActivity,
                                                jsonObject.getString("msg"),
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        }
                                    } else {
                                        val errorBody = response.errorBody()
                                    }
                                }
                            })
                        } else {
                            Toast.makeText(this, "Please enter valid price!", Toast.LENGTH_SHORT).show()
                        }

                    }

                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.setCancelable(false)
                    dialog.show()
                    val window = dialog.window!!
                    window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                } else {
                    orderCompleteBT.isClickable = false
                    val cDialog = Dialog(this)
                    cDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    cDialog.setContentView(R.layout.order_status_dialog)

                    val titleTV = cDialog.findViewById<TextView>(R.id.titleTV)
                    val userNameTV = cDialog.findViewById<TextView>(R.id.userNameTV)
                    val mobileNoTV = cDialog.findViewById<TextView>(R.id.mobileNoTV)
                    val recyclerView = cDialog.findViewById<RecyclerView>(R.id.recyclerView)
                    val itemCountTV = cDialog.findViewById<TextView>(R.id.itemCountTV)
                    val itemTV = cDialog.findViewById<TextView>(R.id.itemTV)
                    val totalPriceTV = cDialog.findViewById<TextView>(R.id.totalPriceTV)
                    val discountLayout = cDialog.findViewById<LinearLayout>(R.id.discountLayout)
                    val discountTxtTV = cDialog.findViewById<TextView>(R.id.discountTxtTV)
                    val discountValueTV = cDialog.findViewById<TextView>(R.id.discountValueTV)
                    val discountTotalLayout = cDialog.findViewById<LinearLayout>(R.id.discountTotalLayout)
                    val afterDiscountValueTV = cDialog.findViewById<TextView>(R.id.afterDiscountValueTV)
                    val ccancelBT = cDialog.findViewById<Button>(R.id.cancelBT)
                    val cokBT = cDialog.findViewById<Button>(R.id.okBT)
                    val ccloseIB = cDialog.findViewById<ImageButton>(R.id.closeIB)

                    titleTV.text = "Complete Order"
                    userNameTV.text = order[0].user_name
                    mobileNoTV.text = order[0].mobile_number

                    totPrice = 0f
                    itemCount = 0
                    for (i in order) {
                        if (i.order_status == "open" || i.order_status == "supplier_accepted") {
                            totPrice += i.price.toFloat()
                            itemCount += 1
                        }
                    }
                    itemCountTV.text = itemCount.toString()
                    if (itemCount > 1) {
                        itemTV.text = "Items"
                    } else {
                        itemTV.text = "Item"
                    }
                    totalPriceTV.text =
                        this.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(totPrice.toDouble())
                    if (supplierDiscount != "" && supplierDiscount != "0" && supplierDiscount != null) {
                        discountLayout.visibility = View.VISIBLE
                        discountTotalLayout.visibility = View.VISIBLE
                        discountTxtTV.text = "Extra Discount $supplierDiscount%"

                        val pre = totPrice / 100
                        val percentAmount: Float = (pre * supplierDiscount.toFloat())
                        val ourPrice: Float = (totPrice - percentAmount)
                        discountValueTV.text = "- " + doubleToStringTwoDecimal(percentAmount.toDouble())

                        afterDiscountValueTV.text =
                            context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(ourPrice.toDouble())
                    } else {
                        discountLayout.visibility = View.GONE
                        discountTotalLayout.visibility = View.GONE
                    }


                    val dialogOrder = mutableListOf<Order>()
                    for (i in order) {
                        if (i.order_status == "supplier_accepted") {
                            dialogOrder.add(i)
                        }
                    }

                    recyclerView.layoutManager = LinearLayoutManager(this)
                    val csupplierOrdersItemAdapter = SupplierOrdersItemAdapter(this, dialogOrder, this)
                    recyclerView.adapter = csupplierOrdersItemAdapter

                    ccloseIB.setOnClickListener {
                        cDialog.dismiss()
                        orderCompleteBT.isClickable = true
                    }

                    ccancelBT.setOnClickListener {
                        cDialog.dismiss()
                        orderCompleteBT.isClickable = true
                    }

                    cokBT.setOnClickListener {
                        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                        val isConnected: Boolean = activeNetwork?.isConnected == true
                        if (!isConnected) {
                            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                        } else {
                            orderCompleteBT.isClickable = false
                            val cobj = JSONObject()
                            cobj.put("token_number", order[0].token_number)
                            cobj.put("added_datetime", order[0].added_datetime)
                            cobj.put("user_id", order[0].user_id)
                            repo!!.updateSupplierCompletedByTokenNumber(cobj.toString())
                        }

                        repo!!.updateSupplierCompletedByTokenNumber.observe(
                            this,
                            androidx.lifecycle.Observer {
                                run {
                                    val cjsonObject = JSONObject(it)
                                    if (cjsonObject.getString("status") == "ok") {
                                        changesDone = true
                                        /*for (i in order) {
                                            if (i.order_status == "supplier_accepted") {
                                                i.order_status = "completed"
                                            }
                                        }
                                        supplierOrdersItemWithRejectAdapter!!.notifyDataSetChanged()
                                        acceptCancelLayout.visibility = View.GONE
                                        orderCompleteBT.isClickable = true*/

                                        progressLayout.visibility = View.VISIBLE
                                        val obj = JSONObject()
                                        obj.put("token_number", order[0].token_number)
                                        obj.put("added_datetime", order[0].added_datetime)
                                        obj.put("user_id", order[0].user_id)
                                        repo!!.getOrderByTokenId(obj.toString())

                                        Toast.makeText(
                                            this,
                                            "Order Completed Successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else if (cjsonObject.getString("status") == "error") {
                                        Toast.makeText(
                                            this,
                                            cjsonObject.getString("msg"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        orderCompleteBT.isClickable = true
                                    } else if (cjsonObject.getString("status") == "invalid_token") {
                                        Toast.makeText(this, cjsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                        orderCompleteBT.isClickable = true
                                    } else if (cjsonObject.getString("status") == "cannot_be_done") {
                                        changesDone = true
                                        progressLayout.visibility = View.VISIBLE
                                        val obj = JSONObject()
                                        obj.put("token_number", order[0].token_number)
                                        obj.put("added_datetime", order[0].added_datetime)
                                        obj.put("user_id", order[0].user_id)
                                        repo!!.getOrderByTokenId(obj.toString())
                                    }

                                    cDialog.dismiss()

                                }
                            })
                    }

                    cDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    cDialog.setCanceledOnTouchOutside(false)
                    cDialog.setCancelable(false)
                    cDialog.show()
                    val window = cDialog.window!!
                    window.setLayout(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
            }
            R.id.editBT -> {
                val intent = Intent(this, OrderModificationActivity::class.java)
                intent.putExtra("token_number", order[0].token_number)
                intent.putExtra("added_datetime", order[0].added_datetime)
                intent.putExtra("user_id", order[0].user_id)
                intent.putExtra("total", totPrice.toString())
                startActivity(intent)
            }
        }
    }

    private fun doubleToStringNoDecimal(d: Double): String? {
        val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,##,###.##")
        return formatter.format(d)
    }

    private fun doubleToStringTwoDecimal(d: Double): String? {
        val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,##,###.00")
        return formatter.format(d)
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

    private val updateTotalPrice = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            changesDone = true
            totPrice = 0f
            itemCount = 0
            for (i in order) {
                if (i.order_status == "open" || i.order_status == "supplier_accepted") {
                    totPrice += i.price.toFloat()
                    itemCount += 1
                }
            }

            totalPriceTV.text = getString(R.string.Rs) + " " + doubleToStringTwoDecimal(totPrice.toDouble())
            itemCountTV.text = itemCount.toString()
            if (itemCount > 1) {
                itemTV.text = "Items"
            } else {
                itemTV.text = "Item"
            }

            if (supplierDiscount != "" && supplierDiscount != "0" && supplierDiscount != null) {
                discountLayout.visibility = View.VISIBLE
                discountTotalLayout.visibility = View.VISIBLE
                discountTxtTV.text = "Extra Discount $supplierDiscount%"

                val pre = totPrice / 100
                val percentAmount: Float = (pre * supplierDiscount.toFloat())
                val ourPrice: Float = (totPrice - percentAmount)
                discountValueTV.text = "- " + doubleToStringTwoDecimal(percentAmount.toDouble())

                afterDiscountValueTV.text =
                    Constants.context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(ourPrice.toDouble())
            } else {
                discountLayout.visibility = View.GONE
                discountTotalLayout.visibility = View.GONE
            }

            if (itemCount > 0) {
                acceptCancelLayout.visibility = View.VISIBLE
            } else {
                acceptCancelLayout.visibility = View.GONE
            }

            if (totPrice > 0) {
                totalPriceLayout.visibility = View.VISIBLE
            } else {
                totalPriceLayout.visibility = View.GONE
            }
        }
    }

    private val chat = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            progressLayout.visibility = View.GONE
            changesDone = true
        }
    }

    private val orderModificationDone = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            changesDone = true

            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnected == true
            if (!isConnected) {
                Toast.makeText(this@OrderInfoActivity, "No Internet Connection!", Toast.LENGTH_SHORT).show()
            } else {
                progressLayout.visibility = View.VISIBLE
                val obj = JSONObject()
                obj.put("token_number", order[0].token_number)
                obj.put("added_datetime", order[0].added_datetime)
                obj.put("user_id", order[0].user_id)
                repo!!.getOrderByTokenId(obj.toString())
            }
        }
    }

    private fun refreshData() {
        val intent = Intent("RefreshData")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
