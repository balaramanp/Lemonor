package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.pojo.Address
import `in`.inferon.msl.lemonor.model.pojo.Products
import `in`.inferon.msl.lemonor.view.adapter.ConfirmOrdersItemAdapter
import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_order_summary.*
import kotlinx.android.synthetic.main.activity_order_summary.afterDiscountValueTV
import kotlinx.android.synthetic.main.activity_order_summary.backIB
import kotlinx.android.synthetic.main.activity_order_summary.discountLayout
import kotlinx.android.synthetic.main.activity_order_summary.discountTotalLayout
import kotlinx.android.synthetic.main.activity_order_summary.discountTxtTV
import kotlinx.android.synthetic.main.activity_order_summary.itemCountTV
import kotlinx.android.synthetic.main.activity_order_summary.itemTV
import kotlinx.android.synthetic.main.activity_order_summary.o2DescriptionTV
import kotlinx.android.synthetic.main.activity_order_summary.okBT
import kotlinx.android.synthetic.main.activity_order_summary.recyclerView
import kotlinx.android.synthetic.main.activity_order_summary.titleTV
import kotlinx.android.synthetic.main.activity_order_summary.totalTV
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class OrderSummaryActivity : AppCompatActivity() {
    private val TAG = OrderSummaryActivity::class.java.simpleName
    private var shopName = ""
    private var o2 = ""
    private var products = ""
    private var supplierDiscount = ""
    private var productsList = mutableListOf<Products>()
    private var itemCount = 0
    private var total = 0f
    private lateinit var address: Address
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    private val confirmArray = JSONArray()
    private val confirmList = mutableListOf<Products>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_summary)

        shared = getSharedPreferences(PREF, MODE_PRIVATE)

        shopName = intent.getStringExtra("shop_name")!!
        o2 = intent.getStringExtra("o2")!!
        products = intent.getStringExtra("products")!!
        supplierDiscount = intent.getStringExtra("supplierDiscount")!!
        productsList = Gson().fromJson(products, object : TypeToken<MutableList<Products>>() {}.type)
        address = Gson().fromJson(
            intent.getStringExtra("address"),
            object : TypeToken<Address>() {}.type
        )

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

            afterDiscountValueTV.text = getString(R.string.Rs) + " " + doubleToStringNoDecimal(ourPrice.toDouble() + 9)
        } else {
            discountLayout.visibility = View.GONE
            discountTotalLayout.visibility = View.GONE
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        val confirmOrdersItemAdapter = ConfirmOrdersItemAdapter(this, confirmList)
        recyclerView.adapter = confirmOrdersItemAdapter
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.setHasFixedSize(true)


        firstNameTV.text = address.first_name + " " + address.last_name
        /*if (address.last_name != "") {
            lastNameTV.visibility = View.VISIBLE
            lastNameTV.text = address.last_name
        } else {
            lastNameTV.visibility = View.GONE
        }*/
        phone1TV.text = address.phone_no1
        if (address.phone_no2 != "") {
            phone2TV.visibility = View.VISIBLE
            phone2TV.text = address.phone_no2
        } else {
            phone2TV.visibility = View.GONE
        }
        address1TV.text = address.address_line_1
        if (address.address_line_2 != "") {
            address2TV.visibility = View.VISIBLE
            address2TV.text = address.address_line_2
        } else {
            address2TV.visibility = View.GONE
        }
        if (address.landmark != "") {
            landmarkTV.visibility = View.VISIBLE
            landmarkTV.text = address.landmark
        } else {
            landmarkTV.visibility = View.GONE
        }
        pincodeTV.text = address.zip_code
        cityTV.text = address.city
        districtTV.text = address.district
        stateTV.text = address.state
        countryTV.text = address.country


        okBT.setOnClickListener {
            super.onBackPressed()
        }

        backIB.setOnClickListener {
            super.onBackPressed()
        }
    }

    private fun doubleToStringNoDecimal(d: Double): String? {
        val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,##,###.00")
        return formatter.format(d)
    }
}
