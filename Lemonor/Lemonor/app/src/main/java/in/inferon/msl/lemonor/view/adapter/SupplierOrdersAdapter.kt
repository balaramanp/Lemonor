package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.Utils
import `in`.inferon.msl.lemonor.model.pojo.Order
import `in`.inferon.msl.lemonor.model.pojo.OrderList
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.activity.OrderInfoActivity
import `in`.inferon.msl.lemonor.view.activity.ProductSelectionActivity
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_order_confirmation.*
import kotlinx.android.synthetic.main.suppier_orders_adapter.view.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class SupplierOrdersAdapter(
    private val context: Context,
    private val list: MutableList<OrderList>,
    private val activity: AppCompatActivity
) : RecyclerView.Adapter<SupplierOrdersAdapter.ViewHolder>() {
    private var repo: Repository? = null
    private var pos = -1
    private var lastPosition = -1
    private var removeStatus = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        repo = Repository()
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.suppier_orders_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = list[position]
        holder.tokenTV.text = order.productsList[0].token_number
        holder.userNameTV.text = order.productsList[0].user_name
        holder.mobileNoTV.text = order.productsList[0].mobile_number
        holder.orderDateTV.text = order.productsList[0].formatted_date + "   " + order.productsList[0].formatted_time

        var totPrice = 0f
        var itemCount = 0
        for (i in order.productsList) {
            totPrice += i.price.toFloat()
            itemCount += 1
        }

        holder.totalPriceTV.text = context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(totPrice.toDouble())
        holder.itemCountTV.text = itemCount.toString()
        if (itemCount > 1) {
            holder.itemTV.text = "Items"
        } else {
            holder.itemTV.text = "Item"
        }
        Log.e("TAG", "Live Order Supplier Discount : " + order.supplier_discount)
        if (order.supplier_discount != "" && order.supplier_discount != "0") {
            holder.discountLayout.visibility = View.VISIBLE
            holder.discountTotalLayout.visibility = View.VISIBLE
            holder.discountTxtTV.text = "Extra Discount ${order.supplier_discount}%"

            val pre = totPrice / 100
            val percentAmount: Float = (pre * order.supplier_discount.toFloat())
            val ourPrice: Float = (totPrice - percentAmount)
            holder.discountValueTV.text = "- " + doubleToStringTwoDecimal(percentAmount.toDouble())

            holder.afterDiscountValueTV.text =
                context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(ourPrice.toDouble())
        } else {
            holder.discountLayout.visibility = View.GONE
            holder.discountTotalLayout.visibility = View.GONE
        }


        if (order.productsList[0].order_status == "open") {
            holder.acceptBT.visibility = View.VISIBLE
            holder.orderCompleteBT.visibility = View.GONE
        } else if (order.productsList[0].order_status == "supplier_accepted") {
            holder.acceptBT.visibility = View.GONE
            holder.orderCompleteBT.visibility = View.VISIBLE
        }

        /*holder.recyclerView.layoutManager = LinearLayoutManager(context)
        val supplierOrdersItemAdapter = SupplierOrdersItemAdapter(context, order.productsList, activity)
        holder.recyclerView.adapter = supplierOrdersItemAdapter
        holder.recyclerView.isNestedScrollingEnabled = false*/

        if (order.productsList[0].product_name != "O2") {
            holder.itemLayout1.visibility = View.VISIBLE
            holder.o2Layout.visibility = View.GONE
            holder.productNameTV1.text = order.productsList[0].product_name
            holder.productQtyTV1.text = order.productsList[0].qty
            holder.productUnitTV1.text = order.productsList[0].unit
            holder.productRateTV1.text = "(" + doubleToStringNoDecimal(order.productsList[0].rate.toDouble()) + ")"
            holder.productPriceTV1.text = doubleToStringTwoDecimal(order.productsList[0].price.toDouble())
        } else {
            holder.o2Layout.visibility = View.VISIBLE
            holder.itemLayout1.visibility = View.GONE
            holder.o2DescriptionTV.text = order.productsList[0].description
            if (order.productsList[0].price.toFloat() == 0f) {
                holder.o2ProductPriceTV.visibility = View.INVISIBLE
            } else {
                holder.o2ProductPriceTV.visibility = View.VISIBLE
                holder.o2ProductPriceTV.text = doubleToStringTwoDecimal(order.productsList[0].price.toDouble())
            }
        }

        if (order.productsList.size >= 2) {
            holder.itemLayout2.visibility = View.VISIBLE
            holder.productNameTV2.text = order.productsList[1].product_name
            holder.productQtyTV2.text = order.productsList[1].qty
            holder.productUnitTV2.text = order.productsList[1].unit
            holder.productRateTV2.text = "(" + doubleToStringNoDecimal(order.productsList[1].rate.toDouble()) + ")"
            holder.productPriceTV2.text = doubleToStringTwoDecimal(order.productsList[1].price.toDouble())
        } else {
            holder.itemLayout2.visibility = View.GONE
        }

        if (order.productsList.size >= 3) {
            holder.itemLayout3.visibility = View.VISIBLE
            holder.productNameTV3.text = order.productsList[2].product_name
            holder.productQtyTV3.text = order.productsList[2].qty
            holder.productUnitTV3.text = order.productsList[2].unit
            holder.productRateTV3.text = "(" + doubleToStringNoDecimal(order.productsList[2].rate.toDouble()) + ")"
            holder.productPriceTV3.text = doubleToStringTwoDecimal(order.productsList[2].price.toDouble())
        } else {
            holder.itemLayout3.visibility = View.GONE
        }

        if (order.productsList.size > 3) {
            holder.moreItemCountTV.visibility = View.VISIBLE
            holder.moreItemCountTV.text = (itemCount - 3).toString() + " more Items"
        } else {
            holder.moreItemCountTV.visibility = View.GONE
        }



        holder.callLayout.setOnClickListener {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.make_call_dialog)

            val diaUserNameTV = dialog.findViewById(R.id.diaUserNameTV) as TextView
            val diaMobileNoTV = dialog.findViewById(R.id.diaMobileNoTV) as TextView
            val diaCancelBT = dialog.findViewById(R.id.diaCancelBT) as Button
            val diaOKBT = dialog.findViewById(R.id.diaOKBT) as Button

            diaUserNameTV.text = order.productsList[0].user_name
            diaMobileNoTV.text = order.productsList[0].mobile_number

            diaCancelBT.setOnClickListener {
                dialog.dismiss()
            }

            diaOKBT.setOnClickListener {
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.data = Uri.parse("tel:" + order.productsList[0].mobile_number)
                context.startActivity(callIntent)
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            val window = dialog.window!!
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        holder.cancelBT.setOnClickListener {
            holder.cancelBT.isClickable = false
            val dialog = Dialog(context)
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
            val cancelBT = dialog.findViewById<Button>(R.id.cancelBT)
            val okBT = dialog.findViewById<Button>(R.id.okBT)
            val closeIB = dialog.findViewById<ImageButton>(R.id.closeIB)

            titleTV.text = "Reject Order"
            userNameTV.text = order.productsList[0].user_name
            mobileNoTV.text = order.productsList[0].mobile_number
            itemCountTV.text = itemCount.toString()
            if (itemCount > 1) {
                itemTV.text = "Items"
            } else {
                itemTV.text = "Item"
            }
            totalPriceTV.text = context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(totPrice.toDouble())
            if (list[position].supplier_discount != "" && list[position].supplier_discount != "0" && list[position].supplier_discount != null) {
                discountLayout.visibility = View.VISIBLE
                discountTotalLayout.visibility = View.VISIBLE
                discountTxtTV.text = "Extra Discount ${list[position].supplier_discount}%"

                val pre = totPrice / 100
                val percentAmount: Float = (pre * list[position].supplier_discount.toFloat())
                val ourPrice: Float = (totPrice - percentAmount)
                discountValueTV.text = "- " + doubleToStringTwoDecimal(percentAmount.toDouble())

                afterDiscountValueTV.text =
                    Constants.context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(ourPrice.toDouble())
            } else {
                discountLayout.visibility = View.GONE
                discountTotalLayout.visibility = View.GONE
            }
            recyclerView.layoutManager = LinearLayoutManager(context)
            val supplierOrdersItemAdapter = SupplierOrdersItemAdapter(context, order.productsList, activity)
            recyclerView.adapter = supplierOrdersItemAdapter
            recyclerView.isNestedScrollingEnabled = false
            recyclerView.setHasFixedSize(true)

            closeIB.setOnClickListener {
                holder.cancelBT.isClickable = true
                dialog.dismiss()
            }

            cancelBT.setOnClickListener {
                holder.cancelBT.isClickable = true
                dialog.dismiss()
            }

            okBT.setOnClickListener {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                val isConnected: Boolean = activeNetwork?.isConnected == true
                if (!isConnected) {
                    Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                } else {
                    holder.cancelBT.isClickable = false
                    showProgressBar()
                    pos = position
                    removeStatus = true
                    val obj = JSONObject()
                    obj.put("token_number", order.productsList[0].token_number)
                    obj.put("added_datetime", order.productsList[0].added_datetime)
                    obj.put("user_id", order.productsList[0].user_id)
                    if (rejectReasonET.text.toString().trim().length > 0) {
                        obj.put("reject_reason", rejectReasonET.text.toString().trim())
                    } else {
                        obj.put("reject_reason", "")
                    }
                    repo!!.updateSupplierRejectedByTokenNumber(obj.toString())
                    dialog.dismiss()
                }

                repo!!.updateSupplierRejectedByTokenNumber.observe(activity, androidx.lifecycle.Observer {
                    run {
                        hideProgressBar()
                        val jsonObject = JSONObject(it)
                        if (jsonObject.getString("status") == "ok") {
                            if (removeStatus) {
                                removeStatus = false
                                for (i in order.productsList) {
                                    if (i.order_status == "open" || i.order_status == "supplier_accepted") {
                                        i.order_status = "supplier_rejected"
                                    }
                                }
                                list.removeAt(pos)
                                Toast.makeText(context, "Order Rejected Successfully!", Toast.LENGTH_SHORT).show()
                            }
                            if (list.size == 0) {
                                showNoOrdersLayout()
                            }
                            notifyDataSetChanged()
                            holder.cancelBT.isClickable = true
                        } else if (jsonObject.getString("status") == "error") {
                            Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                            holder.cancelBT.isClickable = true
                        } else if (jsonObject.getString("status") == "invalid_token") {
                            Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                            holder.cancelBT.isClickable = true
                        } else if (jsonObject.getString("status") == "cannot_be_done") {
                            if (removeStatus) {
                                removeStatus = false
                                for (i in order.productsList) {
                                    i.order_status = "completed"
                                }
                                list.removeAt(pos)
                            }
                            if (list.size == 0) {
                                showNoOrdersLayout()
                            }
                            notifyDataSetChanged()
                            holder.orderCompleteBT.isClickable = true
                        }
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

        holder.acceptBT.setOnClickListener {
            holder.acceptBT.isClickable = false
            val dialog = Dialog(context)
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
            userNameTV.text = order.productsList[0].user_name
            mobileNoTV.text = order.productsList[0].mobile_number
            itemCountTV.text = itemCount.toString()
            if (itemCount > 1) {
                itemTV.text = "Items"
            } else {
                itemTV.text = "Item"
            }
            totalPriceTV.text = context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(totPrice.toDouble())
            if (list[position].supplier_discount != "" && list[position].supplier_discount != "0" && list[position].supplier_discount != null) {
                discountLayout.visibility = View.VISIBLE
                discountTotalLayout.visibility = View.VISIBLE
                discountTxtTV.text = "Extra Discount ${list[position].supplier_discount}%"

                val pre = totPrice / 100
                val percentAmount: Float = (pre * list[position].supplier_discount.toFloat())
                val ourPrice: Float = (totPrice - percentAmount)
                discountValueTV.text = "- " + doubleToStringTwoDecimal(percentAmount.toDouble())

                afterDiscountValueTV.text =
                    Constants.context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(ourPrice.toDouble())
            } else {
                discountLayout.visibility = View.GONE
                discountTotalLayout.visibility = View.GONE
            }

            recyclerView.layoutManager = LinearLayoutManager(context)
            val supplierOrdersItemAdapter = SupplierOrdersItemAdapter(context, order.productsList, activity)
            recyclerView.adapter = supplierOrdersItemAdapter

            closeIB.setOnClickListener {
                dialog.dismiss()
                holder.acceptBT.isClickable = true
            }

            cancelBT.setOnClickListener {
                dialog.dismiss()
                holder.acceptBT.isClickable = true
            }

            okBT.setOnClickListener {
                holder.acceptBT.isClickable = false
                pos = position
                removeStatus = true
                showProgressBar()
                val obj = JSONObject()
                obj.put("token_number", order.productsList[0].token_number)
                obj.put("added_datetime", order.productsList[0].added_datetime)
                obj.put("user_id", order.productsList[0].user_id)
                repo!!.updateSupplierAcceptedByTokenNumber(obj.toString())
                dialog.dismiss()

                repo!!.updateSupplierAcceptedByTokenNumber.observe(activity, androidx.lifecycle.Observer {
                    run {
                        hideProgressBar()
                        val jsonObject = JSONObject(it)
                        if (jsonObject.getString("status") == "ok") {
                            holder.acceptBT.visibility = View.GONE
                            holder.orderCompleteBT.visibility = View.VISIBLE
                            if (removeStatus) {
                                removeStatus = false
                                for (i in order.productsList) {
                                    if (i.order_status == "open") {
                                        i.order_status = "supplier_accepted"
                                    }
                                }
                                Toast.makeText(context, "Order Accepted Successfully!", Toast.LENGTH_SHORT).show()
                            }
                            notifyDataSetChanged()
                            holder.acceptBT.isClickable = true
                        } else if (jsonObject.getString("status") == "error") {
                            Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                            holder.acceptBT.isClickable = true
                        } else if (jsonObject.getString("status") == "invalid_token") {
                            Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                            holder.acceptBT.isClickable = true
                        } else if (jsonObject.getString("status") == "cannot_be_done") {
                            if (removeStatus) {
                                removeStatus = false
                                for (i in order.productsList) {
                                    i.order_status = "completed"
                                }
                                list.removeAt(pos)
                            }
                            if (list.size == 0) {
                                showNoOrdersLayout()
                            }
                            notifyDataSetChanged()
                            holder.orderCompleteBT.isClickable = true
                        }
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


        holder.orderCompleteBT.setOnClickListener {
            var o2Exist = false
            var des = ""
            var ordID = ""
            var pos = 0
            order.productsList.forEachIndexed { index, o ->
                if (o.product_name == "O2") {
                    des = o.description
                    ordID = o.order_id
                    pos = index
                    o2Exist = true
                }
            }


            if (o2Exist) {
                holder.orderCompleteBT.isClickable = false
                val dialog = Dialog(context)
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
                    holder.orderCompleteBT.isClickable = true
                }

                cancelBT.setOnClickListener {
                    dialog.dismiss()
                    holder.orderCompleteBT.isClickable = true
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
                                    hideProgressBar()
                                    val responseString = response.body()!!.string()
                                    Log.e("TAG", "Update O2 Price From Supplier In Order Response : $responseString")
                                    val jsonObject = JSONObject(responseString)
                                    if (jsonObject.getString("status") == "ok") {
                                        order.productsList[pos].price = o2PriceET.text.toString().trim()
                                        dialog.dismiss()

                                        val cDialog = Dialog(context)
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
                                        val discountTotalLayout =
                                            cDialog.findViewById<LinearLayout>(R.id.discountTotalLayout)
                                        val afterDiscountValueTV =
                                            cDialog.findViewById<TextView>(R.id.afterDiscountValueTV)
                                        val ccancelBT = cDialog.findViewById<Button>(R.id.cancelBT)
                                        val cokBT = cDialog.findViewById<Button>(R.id.okBT)
                                        val ccloseIB = cDialog.findViewById<ImageButton>(R.id.closeIB)

                                        titleTV.text = "Complete Order"
                                        userNameTV.text = order.productsList[0].user_name
                                        mobileNoTV.text = order.productsList[0].mobile_number
                                        itemCountTV.text = itemCount.toString()
                                        if (itemCount > 1) {
                                            itemTV.text = "Items"
                                        } else {
                                            itemTV.text = "Item"
                                        }

                                        var tPrice = 0f
                                        for (i in order.productsList) {
                                            tPrice += i.price.toFloat()
                                        }

                                        totalPriceTV.text =
                                            context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(tPrice.toDouble())
                                        if (list[position].supplier_discount != "" && list[position].supplier_discount != "0" && list[position].supplier_discount != null) {
                                            discountLayout.visibility = View.VISIBLE
                                            discountTotalLayout.visibility = View.VISIBLE
                                            discountTxtTV.text = "Extra Discount ${list[position].supplier_discount}%"

                                            val pre = totPrice / 100
                                            val percentAmount: Float =
                                                (pre * list[position].supplier_discount.toFloat())
                                            val ourPrice: Float = (totPrice - percentAmount)
                                            discountValueTV.text =
                                                "- " + doubleToStringTwoDecimal(percentAmount.toDouble())

                                            afterDiscountValueTV.text =
                                                Constants.context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(
                                                    ourPrice.toDouble()
                                                )
                                        } else {
                                            discountLayout.visibility = View.GONE
                                            discountTotalLayout.visibility = View.GONE
                                        }

                                        recyclerView.layoutManager = LinearLayoutManager(context)
                                        val csupplierOrdersItemAdapter =
                                            SupplierOrdersItemAdapter(context, order.productsList, activity)
                                        recyclerView.adapter = csupplierOrdersItemAdapter

                                        ccloseIB.setOnClickListener {
                                            cDialog.dismiss()
                                            holder.orderCompleteBT.isClickable = true
                                        }

                                        ccancelBT.setOnClickListener {
                                            repo!!.updateO2PriceFromSupplierInOrder.removeObserver {}
                                            notifyDataSetChanged()
                                            cDialog.dismiss()
                                            holder.orderCompleteBT.isClickable = true
                                        }

                                        cokBT.setOnClickListener {
                                            val connectivityManager =
                                                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                                            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                                            val isConnected: Boolean = activeNetwork?.isConnected == true
                                            if (!isConnected) {
                                                Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT)
                                                    .show()
                                            } else {
                                                holder.orderCompleteBT.isClickable = false
                                                showProgressBar()
                                                pos = position
                                                removeStatus = true
                                                val cobj = JSONObject()
                                                cobj.put("token_number", order.productsList[0].token_number)
                                                cobj.put("added_datetime", order.productsList[0].added_datetime)
                                                cobj.put("user_id", order.productsList[0].user_id)
                                                repo!!.updateSupplierCompletedByTokenNumber(cobj.toString())
                                                cDialog.dismiss()
                                                holder.orderCompleteBT.isClickable = true
                                            }

                                            repo!!.updateSupplierCompletedByTokenNumber.observe(
                                                activity,
                                                androidx.lifecycle.Observer {
                                                    run {
                                                        val cjsonObject = JSONObject(it)
                                                        if (cjsonObject.getString("status") == "ok") {
                                                            Log.e("TAG", "Position : $position")
                                                            Log.e("TAG", "Global Position : $pos")
                                                            if (removeStatus) {
                                                                removeStatus = false
                                                                for (i in order.productsList) {
                                                                    if (i.order_status == "supplier_accepted") {
                                                                        i.order_status = "completed"
                                                                    }
                                                                }
                                                                list.removeAt(pos)
                                                                Toast.makeText(
                                                                    context,
                                                                    "Order Completed Successfully!",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                            if (list.size == 0) {
                                                                showNoOrdersLayout()
                                                            }
                                                            notifyDataSetChanged()
                                                            hideProgressBar()
                                                            holder.orderCompleteBT.isClickable = true
                                                        } else if (cjsonObject.getString("status") == "error") {
                                                            Toast.makeText(
                                                                context,
                                                                cjsonObject.getString("msg"),
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            holder.orderCompleteBT.isClickable = true
                                                        } else if (cjsonObject.getString("status") == "invalid_token") {
                                                            Toast.makeText(
                                                                context,
                                                                cjsonObject.getString("msg"),
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            holder.orderCompleteBT.isClickable = true
                                                        } else if (jsonObject.getString("status") == "cannot_be_done") {
                                                            if (removeStatus) {
                                                                removeStatus = false
                                                                for (i in order.productsList) {
                                                                    i.order_status = "completed"
                                                                }
                                                                list.removeAt(pos)
                                                            }
                                                            if (list.size == 0) {
                                                                showNoOrdersLayout()
                                                            }
                                                            notifyDataSetChanged()
                                                            holder.orderCompleteBT.isClickable = true
                                                        }
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
                                        Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val errorBody = response.errorBody()
                                }
                            }
                        })
                    } else {
                        Toast.makeText(context, "Please enter valid price!", Toast.LENGTH_SHORT).show()
                    }

                }

                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.setCanceledOnTouchOutside(false)
                dialog.setCancelable(false)
                dialog.show()
                val window = dialog.window!!
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            } else {

                val cDialog = Dialog(context)
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
                userNameTV.text = order.productsList[0].user_name
                mobileNoTV.text = order.productsList[0].mobile_number
                itemCountTV.text = itemCount.toString()
                if (itemCount > 1) {
                    itemTV.text = "Items"
                } else {
                    itemTV.text = "Item"
                }
                totalPriceTV.text = context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(totPrice.toDouble())
                if (list[position].supplier_discount != "" && list[position].supplier_discount != "0" && list[position].supplier_discount != null) {
                    discountLayout.visibility = View.VISIBLE
                    discountTotalLayout.visibility = View.VISIBLE
                    discountTxtTV.text = "Extra Discount ${list[position].supplier_discount}%"

                    val pre = totPrice / 100
                    val percentAmount: Float = (pre * list[position].supplier_discount.toFloat())
                    val ourPrice: Float = (totPrice - percentAmount)
                    discountValueTV.text = "- " + doubleToStringTwoDecimal(percentAmount.toDouble())

                    afterDiscountValueTV.text =
                        Constants.context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(ourPrice.toDouble())
                } else {
                    discountLayout.visibility = View.GONE
                    discountTotalLayout.visibility = View.GONE
                }

                recyclerView.layoutManager = LinearLayoutManager(context)
                val csupplierOrdersItemAdapter = SupplierOrdersItemAdapter(context, order.productsList, activity)
                recyclerView.adapter = csupplierOrdersItemAdapter

                ccloseIB.setOnClickListener {
                    holder.orderCompleteBT.isClickable = true
                    cDialog.dismiss()
                }

                ccancelBT.setOnClickListener {
                    holder.orderCompleteBT.isClickable = true
                    cDialog.dismiss()
                }

                cokBT.setOnClickListener {
                    val connectivityManager =
                        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                    val isConnected: Boolean = activeNetwork?.isConnected == true
                    if (!isConnected) {
                        Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                    } else {
                        holder.orderCompleteBT.isClickable = false
                        showProgressBar()
                        pos = position
                        removeStatus = true
                        val cobj = JSONObject()
                        cobj.put("token_number", order.productsList[0].token_number)
                        cobj.put("added_datetime", order.productsList[0].added_datetime)
                        cobj.put("user_id", order.productsList[0].user_id)
                        repo!!.updateSupplierCompletedByTokenNumber(cobj.toString())
                        cDialog.dismiss()
                        holder.orderCompleteBT.isClickable = true
                    }

                    repo!!.updateSupplierCompletedByTokenNumber.observe(
                        activity,
                        androidx.lifecycle.Observer {
                            run {
                                hideProgressBar()
                                val cjsonObject = JSONObject(it)
                                if (cjsonObject.getString("status") == "ok") {
                                    Log.e("TAG", "Position : $position")
                                    Log.e("TAG", "Global Position : $pos")
                                    if (removeStatus) {
                                        removeStatus = false
                                        for (i in order.productsList) {
                                            if (i.order_status == "supplier_accepted") {
                                                i.order_status = "completed"
                                            }
                                        }
                                        list.removeAt(pos)
                                        Toast.makeText(
                                            context,
                                            "Order Completed Successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    if (list.size == 0) {
                                        showNoOrdersLayout()
                                    }
                                    notifyDataSetChanged()
                                    holder.orderCompleteBT.isClickable = true
                                } else if (cjsonObject.getString("status") == "error") {
                                    Toast.makeText(
                                        context,
                                        cjsonObject.getString("msg"),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    holder.orderCompleteBT.isClickable = true
                                } else if (cjsonObject.getString("status") == "invalid_token") {
                                    Toast.makeText(context, cjsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                    holder.orderCompleteBT.isClickable = true
                                } else if (cjsonObject.getString("status") == "cannot_be_done") {
                                    if (removeStatus) {
                                        removeStatus = false
                                        for (i in order.productsList) {
                                            i.order_status = "completed"
                                        }
                                        list.removeAt(pos)
                                    }
                                    if (list.size == 0) {
                                        showNoOrdersLayout()
                                    }
                                    notifyDataSetChanged()
                                    holder.orderCompleteBT.isClickable = true
                                }
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

        holder.cardView.setOnClickListener {
            val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.card_click_anim)
            holder.cardView.startAnimation(animation)

            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    Log.e("TAG", "Sending Data : " + Gson().toJson(order))
                    val intent = Intent(context, OrderInfoActivity::class.java)
                    intent.putExtra("from", "live")
                    intent.putExtra("data", Gson().toJson(order))
                    intent.putExtra("supplierDiscount", order.supplier_discount)
                    context.startActivity(intent)
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })

            /*val imageViewPair = Pair.create<View?, String?>(holder.orderDateTV, "orderDate")
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imageViewPair)
            context.startActivity(intent, options.toBundle())*/
        }


        /*holder.recyclerView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.card_click_anim)
                holder.cardView.startAnimation(animation)

                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        val intent = Intent(context, OrderInfoActivity::class.java)
                        intent.putExtra("from", "live")
                        intent.putExtra("data", Gson().toJson(order))
                        intent.putExtra("supplierDiscount", order.supplier_discount)
                        context.startActivity(intent)
                    }

                    override fun onAnimationStart(animation: Animation?) {
                    }
                })
                *//*val imageViewPair = Pair.create<View?, String?>(holder.orderDateTV, "orderDate")
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imageViewPair)
                context.startActivity(intent, options.toBundle())*//*
            }
            false
        }*/

//        setAnimation(holder.cardView, position)
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        val animation = AnimationUtils.loadAnimation(
            context,
            if (position > lastPosition)
                R.anim.up_from_bottom
            else
                R.anim.down_from_top
        )
        viewToAnimate.startAnimation(animation)
        lastPosition = position
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tokenTV = view.tokenTV!!
        val orderDateTV = view.orderDateTV!!
        val callLayout = view.callLayout!!
        val userNameTV = view.userNameTV!!
        val mobileNoTV = view.mobileNoTV!!
        val itemCountTV = view.itemCountTV!!
        val itemTV = view.itemTV!!
        val totalPriceTV = view.totalPriceTV!!
        val cancelBT = view.cancelBT!!
        val acceptBT = view.acceptBT!!
        val orderCompleteBT = view.orderCompleteBT!!
        val cardView = view.cardView!!
        val discountLayout = view.discountLayout!!
        val discountTxtTV = view.discountTxtTV!!
        val discountValueTV = view.discountValueTV!!
        val discountTotalLayout = view.discountTotalLayout!!
        val afterDiscountValueTV = view.afterDiscountValueTV!!
//        val recyclerView = view.recyclerView!!

        val o2Layout = view.o2Layout!!
        val o2DescriptionTV = view.o2DescriptionTV!!
        val o2ProductPriceTV = view.o2ProductPriceTV!!
        val itemLayout1 = view.itemLayout1!!
        val productNameTV1 = view.productNameTV1!!
        val productQtyTV1 = view.productQtyTV1!!
        val productUnitTV1 = view.productUnitTV1!!
        val productRateTV1 = view.productRateTV1!!
        val productPriceTV1 = view.productPriceTV1!!
        val itemLayout2 = view.itemLayout2!!
        val productNameTV2 = view.productNameTV2!!
        val productQtyTV2 = view.productQtyTV2!!
        val productUnitTV2 = view.productUnitTV2!!
        val productRateTV2 = view.productRateTV2!!
        val productPriceTV2 = view.productPriceTV2!!
        val itemLayout3 = view.itemLayout3!!
        val productNameTV3 = view.productNameTV3!!
        val productQtyTV3 = view.productQtyTV3!!
        val productUnitTV3 = view.productUnitTV3!!
        val productRateTV3 = view.productRateTV3!!
        val productPriceTV3 = view.productPriceTV3!!
        val moreItemCountTV = view.moreItemCountTV!!
    }

    private fun showProgressBar() {
        val intent = Intent("ShowProgressBar")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun hideProgressBar() {
        val intent = Intent("HideProgressBar")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun showNoOrdersLayout() {
        val intent = Intent("ShowNoOrdersLayout")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
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
}