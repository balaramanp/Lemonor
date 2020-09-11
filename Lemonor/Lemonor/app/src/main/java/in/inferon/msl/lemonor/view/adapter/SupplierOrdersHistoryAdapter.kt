package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.pojo.Order
import `in`.inferon.msl.lemonor.model.pojo.OrderHistory
import `in`.inferon.msl.lemonor.view.activity.OrderInfoActivity
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.supplier_orders_history_adapter.view.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class SupplierOrdersHistoryAdapter(
    private val context: Context,
    private val list: MutableList<OrderHistory>,
    private val activity: AppCompatActivity
) : RecyclerView.Adapter<SupplierOrdersHistoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.supplier_orders_history_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.e("TAG", "Order Status : " + list[position].is_completed)
        if (list[position].is_completed == "true") {
            holder.toolbarLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.toolbar_gray))
            holder.callIV.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_call_gray_24dp))
        } else {
            holder.toolbarLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
            holder.callIV.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_call_cp_24dp))
        }
        val order = list[position].products_list
        holder.tokenTV.text = order[0].token_number
        holder.userNameTV.text = order[0].user_name
        holder.mobileNoTV.text = order[0].mobile_number
        holder.orderDateTV.text = order[0].formatted_date + "   " + order[0].formatted_time

        var totPrice = 0f
        var itemCount = 0
        for (i in order) {
            Log.e("TAG", "Order Status : " + i.order_status)
            if (i.order_status != "user_cancelled" && i.order_status != "supplier_rejected") {
                totPrice += i.price.toFloat()
                itemCount += 1
            }
        }

        holder.totalPriceTV.text = context.getString(R.string.Rs) + " " + doubleToStringNoDecimal(totPrice.toDouble())
        holder.itemCountTV.text = itemCount.toString()
        if (itemCount > 1) {
            holder.itemTV.text = "Items"
        } else {
            holder.itemTV.text = "Item"
        }

        holder.recyclerView.layoutManager = LinearLayoutManager(context)
        val supplierOrdersHistoryItemAdapter = SupplierOrdersHistoryItemAdapter(context, order, activity)
        holder.recyclerView.adapter = supplierOrdersHistoryItemAdapter
        holder.recyclerView.setHasFixedSize(true)

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

        holder.cardView.setOnClickListener {
            val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.card_click_anim)
            holder.cardView.startAnimation(animation)

            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    val intent = Intent(context, OrderInfoActivity::class.java)
                    intent.putExtra("from", "history")
                    intent.putExtra("order_status", list[position].is_completed)
                    intent.putExtra("data", Gson().toJson(order))
                    context.startActivity(intent)
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })
        }

        holder.recyclerView.setOnTouchListener { v, event ->
            if(event.action == MotionEvent.ACTION_UP){
                val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.card_click_anim)
                holder.cardView.startAnimation(animation)

                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        val intent = Intent(context, OrderInfoActivity::class.java)
                        intent.putExtra("from", "history")
                        intent.putExtra("order_status", list[position].is_completed)
                        intent.putExtra("data", Gson().toJson(order))
                        context.startActivity(intent)
                    }

                    override fun onAnimationStart(animation: Animation?) {
                    }
                })
            }
            false
        }

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
        val toolbarLayout = view.toolbarLayout!!
        val callIV = view.callIV!!
        val cardView = view.cardView!!
    }

    private fun doubleToStringNoDecimal(d: Double): String? {
        val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,##,###.##")
        return formatter.format(d)
    }
}