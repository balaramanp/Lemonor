package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.pojo.Order
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.adapter.OrderModificationAdapter
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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_order_modification.*
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class OrderModificationActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = OrderModificationActivity::class.java.simpleName
    private var repo: Repository? = null
    private var order = mutableListOf<Order>()
    private var token_no = ""
    private var added_datetime = ""
    private var user_id = ""
    private var total = ""
    private var orderModificationAdapter: OrderModificationAdapter? = null
    private var changesDone = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_modification)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(orderModified, IntentFilter("OrderModified"))

        token_no = intent.getStringExtra("token_number")
        added_datetime = intent.getStringExtra("added_datetime")
        user_id = intent.getStringExtra("user_id")
        total = intent.getStringExtra("total")
        totalTV.text = getString(R.string.Rs) + " " + doubleToStringNoDecimal(total.toDouble())
        repo = Repository()

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

        repo!!.getOrderByTokenId.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    order = Gson().fromJson(
                        jsonObject.getString("order_list"),
                        object : TypeToken<MutableList<Order>>() {}.type
                    )

                    val openOrder = mutableListOf<Order>()
                    for (i in order) {
                        if (i.order_status == "open" || i.order_status == "supplier_accepted") {
                            openOrder.add(i)
                        }
                    }

                    recyclerView.layoutManager = LinearLayoutManager(this)
                    orderModificationAdapter = OrderModificationAdapter(this, openOrder, this)
                    recyclerView.adapter = orderModificationAdapter
                    recyclerView.isNestedScrollingEnabled = false

                } else if (jsonObject.getString("status") == "invalid_token") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                } else if (jsonObject.getString("status") == "error") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                }
            }
        })

        backIB.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backIB -> {
                checkBackPress()
            }
        }
    }

    override fun onBackPressed() {
        checkBackPress()
    }

    private val orderModified = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            Log.e(TAG, "Order Modified Called")
            changesDone = true

            var tot = 0f
            for (i in order) {
                tot += i.price.toFloat()
                Log.e(TAG, "Tot : $tot")
            }
            totalTV.text = getString(R.string.Rs) + " " + doubleToStringNoDecimal(tot.toDouble())
        }
    }

    private fun orderModificationDone() {
        val intent = Intent("OrderModificationDone")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun checkBackPress() {
        var unSavedItem = false
        for (i in order) {
            Log.e(TAG, "Order Modification Saved : " + i.order_modification_saved)
            if (i.order_modification_saved == "false") {
                unSavedItem = true
            }
        }

        if (unSavedItem) {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.unsaved_order_dialog)

            val cancelBT = dialog.findViewById(R.id.cancelBT) as Button
            val okBT = dialog.findViewById(R.id.okBT) as Button

            cancelBT.setOnClickListener {
                dialog.dismiss()
                orderModificationDone()
                super.onBackPressed()
            }

            okBT.setOnClickListener {
                dialog.dismiss()
            }
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        } else if (changesDone) {
            orderModificationDone()
            super.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    private fun doubleToStringNoDecimal(d: Double): String? {
        val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,##,###.##")
        return formatter.format(d)
    }
}
