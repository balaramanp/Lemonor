package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Utils
import `in`.inferon.msl.lemonor.model.pojo.Order
import `in`.inferon.msl.lemonor.model.pojo.Products
import `in`.inferon.msl.lemonor.repo.Repository
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_place_order.*
import kotlinx.android.synthetic.main.supplier_orders_item_with_reject_adapter.view.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class SupplierOrdersItemWithRejectAdapter(
    private val context: Context,
    private val items: MutableList<Order>,
    private val activity: AppCompatActivity
) : RecyclerView.Adapter<SupplierOrdersItemWithRejectAdapter.ViewHolder>() {
    private var repo: Repository? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        repo = Repository()
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.supplier_orders_item_with_reject_adapter,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (items[position].product_name != "O2") {
            holder.itemLayout.visibility = View.VISIBLE
            holder.o2Layout.visibility = View.GONE
            holder.productNameTV.text = items[position].product_name
            holder.productQtyTV.text = items[position].qty
            holder.productUnitTV.text = items[position].unit
            holder.productRateTV.text = "(" + doubleToStringNoDecimal(items[position].rate.toDouble()) + ")"
            holder.productPriceTV.text = doubleToStringNoDecimal(items[position].price.toDouble())

            if (items[position].order_status == "open") {
                holder.statusTV.text = "Waiting for Seller apporval."
                holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productNameTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.productQtyTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.productUnitTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.productPriceTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.productPriceTV.visibility = View.VISIBLE
                holder.supplierRejectIV.visibility = View.VISIBLE
            } else if (items[position].order_status == "user_cancelled") {
                holder.statusTV.text = "User cancelled the item."
                holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productNameTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productQtyTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productUnitTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productPriceTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.supplierRejectIV.visibility = View.INVISIBLE
                holder.productPriceTV.visibility = View.INVISIBLE
            } else if (items[position].order_status == "supplier_accepted") {
                holder.statusTV.text = "Seller accepted. Waiting for delivery."
                holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.buttonOnClickColor))
                holder.productNameTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.productQtyTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.productUnitTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.productPriceTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.productPriceTV.visibility = View.VISIBLE
                holder.supplierRejectIV.visibility = View.VISIBLE
            } else if (items[position].order_status == "supplier_rejected") {
                holder.statusTV.text = "Seller Rejected."
                holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.brightColor))
                holder.productNameTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productQtyTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productUnitTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productPriceTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.supplierRejectIV.visibility = View.INVISIBLE
                holder.productPriceTV.visibility = View.INVISIBLE
            } else if (items[position].order_status == "completed") {
                holder.statusTV.text = "Order Delivered."
                holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.buttonColor))
                holder.productNameTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.productQtyTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.productUnitTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.productPriceTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.productPriceTV.visibility = View.VISIBLE
                holder.supplierRejectIV.visibility = View.INVISIBLE
            }
        } else if (items[position].product_name == "O2") {
            holder.o2Layout.visibility = View.VISIBLE
            holder.itemLayout.visibility = View.GONE
            holder.o2ProductTV.text = items[position].description
            holder.o2ProductPriceTV.text = doubleToStringNoDecimal(items[position].price.toDouble())

            if (items[position].order_status == "open") {
                holder.statusTV.text = "Waiting for Seller apporval."
                holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.o2ProductTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.o2ProductPriceTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.o2ProductPriceTV.visibility = View.VISIBLE
                holder.supplierRejectIV.visibility = View.VISIBLE
            } else if (items[position].order_status == "user_cancelled") {
                holder.statusTV.text = "User cancelled the item."
                holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.o2ProductTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.o2ProductPriceTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.supplierRejectIV.visibility = View.INVISIBLE
                holder.o2ProductPriceTV.visibility = View.INVISIBLE
            } else if (items[position].order_status == "supplier_accepted") {
                holder.statusTV.text = "Seller accepted. Waiting for delivery."
                holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.buttonOnClickColor))
                holder.o2ProductTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.o2ProductPriceTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.o2ProductPriceTV.visibility = View.VISIBLE
                holder.supplierRejectIV.visibility = View.VISIBLE
            } else if (items[position].order_status == "supplier_rejected") {
                holder.statusTV.text = "Seller Rejected."
                holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.brightColor))
                holder.o2ProductTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.o2ProductPriceTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.supplierRejectIV.visibility = View.INVISIBLE
                holder.o2ProductPriceTV.visibility = View.INVISIBLE
            } else if (items[position].order_status == "completed") {
                holder.statusTV.text = "Order Delivered."
                holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.buttonColor))
                holder.o2ProductTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.o2ProductPriceTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                holder.o2ProductPriceTV.visibility = View.VISIBLE
                holder.supplierRejectIV.visibility = View.INVISIBLE
            }
        }

        if (items[position].order_status == "open" || items[position].order_status == "supplier_accepted") {
            if (items[position].chat != "" && items[position].chat != null) {
                holder.chatTV.visibility = View.VISIBLE
                holder.chatTV.text = items[position].chat
                holder.replyIV.visibility = View.VISIBLE
                holder.messageIV.visibility = View.GONE
            } else {
                holder.chatTV.visibility = View.GONE
                holder.replyIV.visibility = View.GONE
                holder.messageIV.visibility = View.VISIBLE
            }
        } else if (items[position].order_status == "user_cancelled" || items[position].order_status == "supplier_rejected"
            || items[position].order_status == "completed"
        ) {
            if (items[position].chat != "" && items[position].chat != null) {
                holder.chatTV.visibility = View.VISIBLE
                holder.chatTV.text = items[position].chat
            } else {
                holder.chatTV.visibility = View.GONE
            }
            holder.replyIV.visibility = View.GONE
            holder.messageIV.visibility = View.GONE
        }



        holder.supplierRejectIV.setOnClickListener {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.cancel_individual_order_dialog)

            val productNameTV = dialog.findViewById(R.id.productNameTV) as TextView
            val rejectReasonET = dialog.findViewById(R.id.rejectReasonET) as EditText
            val diaCancelBT = dialog.findViewById(R.id.diaCancelBT) as Button
            val diaOKBT = dialog.findViewById(R.id.diaOKBT) as Button

            if (items[position].product_name == "O2") {
                productNameTV.text = "You are about to reject the order for " + items[position].description
            } else {
                productNameTV.text = "You are about to reject the order for " + items[position].product_name +
                        " " + items[position].qty + items[position].unit
            }


            rejectReasonET.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (rejectReasonET.text.toString().trim().length > 0) {
                        diaOKBT.text = "OK"
                    } else {
                        diaOKBT.text = "SKIP"
                    }
                }
            })

            diaCancelBT.setOnClickListener {
                dialog.dismiss()
            }

            diaOKBT.setOnClickListener {
                showProgressBar()
                val obj = JSONObject()
                obj.put("order_id", items[position].order_id)
                if (rejectReasonET.text.toString().trim().length > 0) {
                    obj.put("reject_reason", rejectReasonET.text.toString().trim())
                } else {
                    obj.put("reject_reason", "")
                }


                Utils.getRetrofit().updateSupplierRejected(obj.toString()).enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        t.printStackTrace()
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val responseString = response.body()!!.string()
                            Log.e("TAG", "Update Supplier Rejected Response : $responseString")
                            val jsonObject = JSONObject(responseString)
                            dialog.dismiss()
                            hideProgressBar()
                            if (jsonObject.getString("status") == "ok") {
                                items[position].order_status = "supplier_rejected"
                                holder.statusTV.text = "Seller Rejected."
                                holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.brightColor))
                                holder.productNameTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                                holder.productQtyTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                                holder.productUnitTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                                holder.productPriceTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                                holder.o2ProductTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                                holder.o2ProductPriceTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                                holder.supplierRejectIV.visibility = View.INVISIBLE
                                holder.productPriceTV.visibility = View.INVISIBLE
                                holder.o2ProductPriceTV.visibility = View.INVISIBLE

                                if (jsonObject.getString("chat_content") != "") {
                                    holder.chatTV.visibility = View.VISIBLE
                                    holder.replyIV.visibility = View.GONE
                                    holder.messageIV.visibility = View.GONE
                                    holder.chatTV.text = jsonObject.getString("chat_content")
                                    items[position].chat = jsonObject.getString("chat_content")
                                } else {
                                    holder.chatTV.visibility = View.GONE
                                    holder.replyIV.visibility = View.GONE
                                    holder.messageIV.visibility = View.GONE
                                }


                                updateTotalPrice()
                            } else if (jsonObject.getString("status") == "error") {
                                Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                            } else if (jsonObject.getString("status") == "user_cancelled") {
                                items[position].order_status = "user_cancelled"
                                holder.statusTV.text = "User cancelled the item."
                                holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                                holder.productNameTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                                holder.productQtyTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                                holder.productUnitTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                                holder.productPriceTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                                holder.o2ProductTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                                holder.o2ProductPriceTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                                holder.supplierRejectIV.visibility = View.INVISIBLE
                                holder.productPriceTV.visibility = View.INVISIBLE
                                holder.o2ProductPriceTV.visibility = View.INVISIBLE

                                holder.replyIV.visibility = View.GONE
                                holder.messageIV.visibility = View.GONE

                                Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                            } else if (jsonObject.getString("status") == "order_completed") {
                                items[position].order_status = "completed"
                                holder.statusTV.text = "Order Delivered."
                                holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.buttonColor))
                                holder.productNameTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                                holder.productQtyTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                                holder.productUnitTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                                holder.productPriceTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                                holder.o2ProductTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                                holder.o2ProductPriceTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
                                holder.productPriceTV.visibility = View.VISIBLE
                                holder.o2ProductPriceTV.visibility = View.VISIBLE
                                holder.supplierRejectIV.visibility = View.INVISIBLE

                                holder.replyIV.visibility = View.GONE
                                holder.messageIV.visibility = View.GONE

                                Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            val window = dialog.window!!
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        }

        holder.replyIV.setOnClickListener {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.order_description_dialog)

            val titleTV = dialog.findViewById(R.id.titleTV) as TextView
            val productDescriptionTV = dialog.findViewById(R.id.productDescriptionTV) as TextView
            val orderDescriptionET = dialog.findViewById(R.id.orderDescriptionET) as TextView
            val buttonLayout = dialog.findViewById(R.id.buttonLayout) as LinearLayout
            val loadingLayout = dialog.findViewById(R.id.loadingLayout) as LinearLayout
            val cancelBT = dialog.findViewById(R.id.cancelBT) as Button
            val okBT = dialog.findViewById(R.id.okBT) as Button

            if (items[position].product_name == "O2") {
                titleTV.text = "Open Order"
            } else {
                titleTV.text = items[position].product_name
            }
            /*if (items[position].description != "" || items[position].description != null) {
                productDescriptionTV.text = items[position].description
            } else {
                productDescriptionTV.visibility = View.GONE
            }*/
            productDescriptionTV.visibility = View.GONE

            cancelBT.setOnClickListener {
                dialog.dismiss()
            }

            okBT.setOnClickListener {
                if (orderDescriptionET.text.toString().trim().length > 0) {

                    buttonLayout.visibility = View.GONE
                    loadingLayout.visibility = View.VISIBLE

                    val jobj = JSONObject()
                    jobj.put("order_id", items[position].order_id)
                    jobj.put("from", "supplier")
                    jobj.put("chat_content", orderDescriptionET.text.toString().trim())


                    Utils.getRetrofit().chat(jobj.toString()).enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            t.printStackTrace()
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.isSuccessful) {
                                loadingLayout.visibility = View.GONE
                                val responseString = response.body()!!.string()
                                Log.e("TAG", "Chat Response : $responseString")
                                val jsonObject = JSONObject(responseString)
                                if (jsonObject.getString("status") == "ok") {
                                    holder.chatTV.visibility = View.VISIBLE
                                    holder.replyIV.visibility = View.VISIBLE
                                    holder.chatTV.text =
                                        jsonObject.getString("chat_content") + holder.chatTV.text.toString().trim()
                                    items[position].chat = jsonObject.getString("chat_content") + items[position].chat
                                    dialog.dismiss()
                                    chat()
                                } else if (jsonObject.getString("status") == "error") {

                                } else if (jsonObject.getString("status") == "invalid_from") {

                                }
                            }
                        }
                    })
                } else {
                    Toast.makeText(context, "Please enter additional product description!", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            val window = dialog.window!!
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        holder.messageIV.setOnClickListener {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.order_description_dialog)

            val titleTV = dialog.findViewById(R.id.titleTV) as TextView
            val productDescriptionTV = dialog.findViewById(R.id.productDescriptionTV) as TextView
            val orderDescriptionET = dialog.findViewById(R.id.orderDescriptionET) as TextView
            val buttonLayout = dialog.findViewById(R.id.buttonLayout) as LinearLayout
            val loadingLayout = dialog.findViewById(R.id.loadingLayout) as LinearLayout
            val cancelBT = dialog.findViewById(R.id.cancelBT) as Button
            val okBT = dialog.findViewById(R.id.okBT) as Button

            if (items[position].product_name == "O2") {
                titleTV.text = "Open Order"
            } else {
                titleTV.text = items[position].product_name
            }
            /*if (items[position].description != "" || items[position].description != null) {
                productDescriptionTV.text = items[position].description
            } else {
                productDescriptionTV.visibility = View.GONE
            }*/
            productDescriptionTV.visibility = View.GONE

            cancelBT.setOnClickListener {
                dialog.dismiss()
            }

            okBT.setOnClickListener {
                if (orderDescriptionET.text.toString().trim().length > 0) {
                    buttonLayout.visibility = View.GONE
                    loadingLayout.visibility = View.VISIBLE

                    val jobj = JSONObject()
                    jobj.put("order_id", items[position].order_id)
                    jobj.put("from", "supplier")
                    jobj.put("chat_content", orderDescriptionET.text.toString().trim())


                    Utils.getRetrofit().chat(jobj.toString()).enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            t.printStackTrace()
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.isSuccessful) {
                                val responseString = response.body()!!.string()
                                Log.e("TAG", "Chat Response : $responseString")
                                val jsonObject = JSONObject(responseString)
                                if (jsonObject.getString("status") == "ok") {
                                    holder.chatTV.visibility = View.VISIBLE
                                    holder.messageIV.visibility = View.GONE
                                    holder.replyIV.visibility = View.VISIBLE
                                    holder.chatTV.text =
                                        jsonObject.getString("chat_content") + holder.chatTV.text.toString().trim()
                                    items[position].chat = jsonObject.getString("chat_content") + items[position].chat
                                    dialog.dismiss()
                                    chat()
                                } else if (jsonObject.getString("status") == "error") {
                                    Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                } else if (jsonObject.getString("status") == "invalid_from") {
                                    Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                } else if (jsonObject.getString("status") == "order_closed") {
                                    Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    })
                } else {
                    Toast.makeText(context, "Please enter additional product description!", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            val window = dialog.window!!
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val productNameTV = view.productNameTV!!
        val productQtyTV = view.productQtyTV!!
        val productUnitTV = view.productUnitTV!!
        val productRateTV = view.productRateTV!!
        val productPriceTV = view.productPriceTV!!
        val itemLayout = view.itemLayout!!
        val o2Layout = view.o2Layout!!
        val o2ProductTV = view.o2ProductTV!!
        val o2ProductPriceTV = view.o2ProductPriceTV!!
        val supplierRejectIV = view.supplierRejectIV!!
        val statusTV = view.statusTV!!
        val replyIV = view.replyIV!!
        val messageIV = view.messageIV!!
        val chatTV = view.chatTV!!
    }

    private fun doubleToStringNoDecimal(d: Double): String? {
        val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,##,###.##")
        return formatter.format(d)
    }

    private fun showProgressBar() {
        val intent = Intent("ShowProgressBar")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun hideProgressBar() {
        val intent = Intent("HideProgressBar")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun updateTotalPrice() {
        val intent = Intent("UpdateTotalPrice")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun chat() {
        val intent = Intent("Chat")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}