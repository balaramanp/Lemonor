package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Utils
import `in`.inferon.msl.lemonor.model.pojo.Order
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.client_orders_adapter.view.*
import kotlinx.android.synthetic.main.client_orders_adapter.view.mobileNoTV
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class ClientOrdersAdapter(
    private val context: Context,
    private val list: MutableList<MutableList<Order>>,
    private val activity: AppCompatActivity
) : RecyclerView.Adapter<ClientOrdersAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.client_orders_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = list[position]
        holder.tokenTV.text = order[0].token_number
        holder.shopNameTV.text = order[0].shop_name
        holder.mobileNoTV.text = order[0].shop_mobile_number
        holder.orderDateTV.text = order[0].formatted_date + "   " + order[0].formatted_time

        var totPrice = 0f
        var itemCount = 0
        var orderCompleted = false
        for (i in order) {
            if (i.order_status != "user_cancelled" && i.order_status != "supplier_rejected") {
                totPrice += i.price.toFloat()
                itemCount += 1
                orderCompleted = false
            }

            if (i.order_status == "completed") {
                orderCompleted = true
            }
        }

        holder.totalPriceTV.text = context.getString(R.string.Rs) + " " + doubleToStringNoDecimal(totPrice.toDouble())
        holder.itemCountTV.text = itemCount.toString()
        if (itemCount > 1) {
            holder.itemTV.text = "Items"
        } else {
            holder.itemTV.text = "Item"
        }

        if (orderCompleted && order[0].is_rated == "0") {
            holder.ratingLayout.visibility = View.VISIBLE
        } else {
            holder.ratingLayout.visibility = View.GONE
        }


        holder.recyclerView.layoutManager = LinearLayoutManager(context)
        val ordersItemAdapter = OrdersItemAdapter(context, order, activity)
        holder.recyclerView.adapter = ordersItemAdapter
        holder.recyclerView.setHasFixedSize(true)

        holder.callLayout.setOnClickListener {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.make_call_dialog)

            val diaUserNameTV = dialog.findViewById(R.id.diaUserNameTV) as TextView
            val diaMobileNoTV = dialog.findViewById(R.id.diaMobileNoTV) as TextView
            val diaCancelBT = dialog.findViewById(R.id.diaCancelBT) as Button
            val diaOKBT = dialog.findViewById(R.id.diaOKBT) as Button

            diaUserNameTV.text = order[0].shop_name
            diaMobileNoTV.text = order[0].shop_mobile_number

            diaCancelBT.setOnClickListener {
                dialog.dismiss()
            }

            diaOKBT.setOnClickListener {
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.data = Uri.parse("tel:" + order[0].shop_mobile_number)
                context.startActivity(callIntent)
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            val window = dialog.window!!
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }


        holder.ratingLayout.setOnClickListener {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.rating_dialog)

            val storeRatingBar = dialog.findViewById(R.id.storeRatingBar) as RatingBar
            val productRatingBar = dialog.findViewById(R.id.productRatingBar) as RatingBar
            val deliveryBoyRatingBar = dialog.findViewById(R.id.deliveryBoyRatingBar) as RatingBar
            val ratingCommentET = dialog.findViewById(R.id.ratingCommentET) as EditText
            val loadingLayout = dialog.findViewById(R.id.loadingLayout) as LinearLayout
            val buttonLayout = dialog.findViewById(R.id.buttonLayout) as LinearLayout
            val diaCancelBT = dialog.findViewById(R.id.diaCancelBT) as Button
            val diaOKBT = dialog.findViewById(R.id.diaOKBT) as Button

            diaCancelBT.setOnClickListener {
                dialog.dismiss()
            }

            diaOKBT.setOnClickListener {
                if (storeRatingBar.rating > 0f && productRatingBar.rating > 0f && deliveryBoyRatingBar.rating > 0f) {
                    loadingLayout.visibility = View.VISIBLE
                    buttonLayout.visibility = View.GONE
                    val obj = JSONObject()
                    obj.put("token_number", order[0].token_number)
                    obj.put("added_datetime", order[0].added_datetime)
                    obj.put("user_id", order[0].user_id)
                    obj.put("supplier_rating", storeRatingBar.rating.toString())
                    obj.put("product_rating", productRatingBar.rating.toString())
                    obj.put("delivery_boy_rating", deliveryBoyRatingBar.rating.toString())
                    obj.put("rating_comment", ratingCommentET.text.toString().trim())

                    Utils.getRetrofit().updateBillRating(obj.toString()).enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            t.printStackTrace()
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.isSuccessful) {
                                loadingLayout.visibility = View.GONE
                                val responseString = response.body()!!.string()
                                Log.e("TAG", "Update Bill Rating Response : $responseString")
                                val jsonObject = JSONObject(responseString)
                                if (jsonObject.getString("status") == "ok") {
                                    order[0].is_rated = "1"
                                    updateRating(position)
                                    dialog.dismiss()
                                    Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                                } else if (jsonObject.getString("status") == "error") {
                                    buttonLayout.visibility = View.VISIBLE
                                    Toast.makeText(context, "Something went Wrong!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    })
                } else {
                    Toast.makeText(context, "Please Provide All Rating!", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            val window = dialog.window!!
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tokenTV = view.tokenTV!!
        val orderDateTV = view.orderDateTV!!
        val callLayout = view.callLayout!!
        val shopNameTV = view.shopNameTV!!
        val mobileNoTV = view.mobileNoTV!!
        val recyclerView = view.recyclerView!!
        val itemCountTV = view.itemCountTV!!
        val itemTV = view.itemTV!!
        val totalPriceTV = view.totalPriceTV!!
        val ratingLayout = view.ratingLayout!!
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

    private fun updateRating(position: Int) {
        val intent = Intent("UpdateRating")
        intent.putExtra("position", position)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}