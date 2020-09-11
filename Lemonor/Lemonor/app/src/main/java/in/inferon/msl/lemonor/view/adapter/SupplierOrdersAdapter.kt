package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Utils
import `in`.inferon.msl.lemonor.model.pojo.Order
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
    private val list: MutableList<MutableList<Order>>,
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
        holder.tokenTV.text = order[0].token_number
        holder.userNameTV.text = order[0].user_name
        holder.mobileNoTV.text = order[0].mobile_number
        holder.orderDateTV.text = order[0].formatted_date + "   " + order[0].formatted_time

        var totPrice = 0f
        var itemCount = 0
        for (i in order) {
            totPrice += i.price.toFloat()
            itemCount += 1
        }

        holder.totalPriceTV.text = context.getString(R.string.Rs) + " " + doubleToStringNoDecimal(totPrice.toDouble())
        holder.itemCountTV.text = itemCount.toString()
        if (itemCount > 1) {
            holder.itemTV.text = "Items"
        } else {
            holder.itemTV.text = "Item"
        }

        if (order[0].order_status == "open") {
            holder.acceptBT.visibility = View.VISIBLE
            holder.orderCompleteBT.visibility = View.GONE
        } else if (order[0].order_status == "supplier_accepted") {
            holder.acceptBT.visibility = View.GONE
            holder.orderCompleteBT.visibility = View.VISIBLE
        }

        holder.recyclerView.layoutManager = LinearLayoutManager(context)
        val supplierOrdersItemAdapter = SupplierOrdersItemAdapter(context, order, activity)
        holder.recyclerView.adapter = supplierOrdersItemAdapter
        holder.recyclerView.isNestedScrollingEnabled = false


        holder.callLayout.setOnClickListener {
            val dialog = Dialog(context)
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
                context.startActivity(callIntent)
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            val window = dialog.window!!
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        holder.cancelBT.setOnClickListener {
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
            val rejectReasonET = dialog.findViewById<EditText>(R.id.rejectReasonET)
            val cancelBT = dialog.findViewById<Button>(R.id.cancelBT)
            val okBT = dialog.findViewById<Button>(R.id.okBT)

            titleTV.text = "Reject Order"
            userNameTV.text = order[0].user_name
            mobileNoTV.text = order[0].mobile_number
            itemCountTV.text = itemCount.toString()
            if (itemCount > 1) {
                itemTV.text = "Items"
            } else {
                itemTV.text = "Item"
            }
            totalPriceTV.text = context.getString(R.string.Rs) + " " + doubleToStringNoDecimal(totPrice.toDouble())
            recyclerView.layoutManager = LinearLayoutManager(context)
            val supplierOrdersItemAdapter = SupplierOrdersItemAdapter(context, order, activity)
            recyclerView.adapter = supplierOrdersItemAdapter
            recyclerView.isNestedScrollingEnabled = false
            recyclerView.setHasFixedSize(true)

            cancelBT.setOnClickListener {
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
                    obj.put("token_number", order[0].token_number)
                    obj.put("added_datetime", order[0].added_datetime)
                    obj.put("user_id", order[0].user_id)
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
                                for (i in order) {
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
                                for (i in order) {
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
            dialog.show()
            val window = dialog.window!!
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        holder.acceptBT.setOnClickListener {
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
            val cancelBT = dialog.findViewById<Button>(R.id.cancelBT)
            val okBT = dialog.findViewById<Button>(R.id.okBT)

            titleTV.text = "Accept Order"
            userNameTV.text = order[0].user_name
            mobileNoTV.text = order[0].mobile_number
            itemCountTV.text = itemCount.toString()
            if (itemCount > 1) {
                itemTV.text = "Items"
            } else {
                itemTV.text = "Item"
            }
            totalPriceTV.text = context.getString(R.string.Rs) + " " + doubleToStringNoDecimal(totPrice.toDouble())
            recyclerView.layoutManager = LinearLayoutManager(context)
            val supplierOrdersItemAdapter = SupplierOrdersItemAdapter(context, order, activity)
            recyclerView.adapter = supplierOrdersItemAdapter

            cancelBT.setOnClickListener {
                dialog.dismiss()
            }

            okBT.setOnClickListener {
                holder.acceptBT.isClickable = false
                pos = position
                removeStatus = true
                showProgressBar()
                val obj = JSONObject()
                obj.put("token_number", order[0].token_number)
                obj.put("added_datetime", order[0].added_datetime)
                obj.put("user_id", order[0].user_id)
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
                                for (i in order) {
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
                                for (i in order) {
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
            dialog.show()
            val window = dialog.window!!
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }


        holder.orderCompleteBT.setOnClickListener {
            var o2Exist = false
            var des = ""
            var ordID = ""
            var pos = 0
            order.forEachIndexed { index, o ->
                if (o.product_name == "O2") {
                    des = o.description
                    ordID = o.order_id
                    pos = index
                    o2Exist = true
                }
            }


            if (o2Exist) {
                val dialog = Dialog(context)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.o2_order_status_dialog)

                val o2DescriptionTV = dialog.findViewById<TextView>(R.id.o2DescriptionTV)
                val o2PriceET = dialog.findViewById<EditText>(R.id.o2PriceET)
                val loadingLayout = dialog.findViewById<LinearLayout>(R.id.loadingLayout)
                val o2AcceptCancelLayout = dialog.findViewById<LinearLayout>(R.id.o2AcceptCancelLayout)
                val cancelBT = dialog.findViewById<Button>(R.id.cancelBT)
                val okBT = dialog.findViewById<Button>(R.id.okBT)

                o2DescriptionTV.text = des

                cancelBT.setOnClickListener {
                    dialog.dismiss()
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
                                        order[pos].price = o2PriceET.text.toString().trim()
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
                                        val ccancelBT = cDialog.findViewById<Button>(R.id.cancelBT)
                                        val cokBT = cDialog.findViewById<Button>(R.id.okBT)

                                        titleTV.text = "Complete Order"
                                        userNameTV.text = order[0].user_name
                                        mobileNoTV.text = order[0].mobile_number
                                        itemCountTV.text = itemCount.toString()
                                        if (itemCount > 1) {
                                            itemTV.text = "Items"
                                        } else {
                                            itemTV.text = "Item"
                                        }

                                        var tPrice = 0f
                                        for (i in order) {
                                            tPrice += i.price.toFloat()
                                        }

                                        totalPriceTV.text =
                                            context.getString(R.string.Rs) + " " + doubleToStringNoDecimal(tPrice.toDouble())
                                        recyclerView.layoutManager = LinearLayoutManager(context)
                                        val csupplierOrdersItemAdapter =
                                            SupplierOrdersItemAdapter(context, order, activity)
                                        recyclerView.adapter = csupplierOrdersItemAdapter

                                        ccancelBT.setOnClickListener {
                                            repo!!.updateO2PriceFromSupplierInOrder.removeObserver {}
                                            notifyDataSetChanged()
                                            cDialog.dismiss()
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
                                                cobj.put("token_number", order[0].token_number)
                                                cobj.put("added_datetime", order[0].added_datetime)
                                                cobj.put("user_id", order[0].user_id)
                                                repo!!.updateSupplierCompletedByTokenNumber(cobj.toString())
                                                cDialog.dismiss()
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
                                                                for (i in order) {
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
                                                                for (i in order) {
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
                val ccancelBT = cDialog.findViewById<Button>(R.id.cancelBT)
                val cokBT = cDialog.findViewById<Button>(R.id.okBT)

                titleTV.text = "Complete Order"
                userNameTV.text = order[0].user_name
                mobileNoTV.text = order[0].mobile_number
                itemCountTV.text = itemCount.toString()
                if (itemCount > 1) {
                    itemTV.text = "Items"
                } else {
                    itemTV.text = "Item"
                }
                totalPriceTV.text = context.getString(R.string.Rs) + " " + doubleToStringNoDecimal(totPrice.toDouble())
                recyclerView.layoutManager = LinearLayoutManager(context)
                val csupplierOrdersItemAdapter = SupplierOrdersItemAdapter(context, order, activity)
                recyclerView.adapter = csupplierOrdersItemAdapter

                ccancelBT.setOnClickListener {
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
                        cobj.put("token_number", order[0].token_number)
                        cobj.put("added_datetime", order[0].added_datetime)
                        cobj.put("user_id", order[0].user_id)
                        repo!!.updateSupplierCompletedByTokenNumber(cobj.toString())
                        cDialog.dismiss()
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
                                        for (i in order) {
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
                                        for (i in order) {
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
                    context.startActivity(intent)
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })

            /*val imageViewPair = Pair.create<View?, String?>(holder.orderDateTV, "orderDate")
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imageViewPair)
            context.startActivity(intent, options.toBundle())*/
        }

        holder.recyclerView.setOnTouchListener { v, event ->
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
                        context.startActivity(intent)
                    }

                    override fun onAnimationStart(animation: Animation?) {
                    }
                })
                /*val imageViewPair = Pair.create<View?, String?>(holder.orderDateTV, "orderDate")
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imageViewPair)
                context.startActivity(intent, options.toBundle())*/
            }
            false
        }

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
        val recyclerView = view.recyclerView!!
        val itemCountTV = view.itemCountTV!!
        val itemTV = view.itemTV!!
        val totalPriceTV = view.totalPriceTV!!
        val cancelBT = view.cancelBT!!
        val acceptBT = view.acceptBT!!
        val orderCompleteBT = view.orderCompleteBT!!
        val cardView = view.cardView!!
        val recyclerViewLayout = view.recyclerViewLayout!!
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
}