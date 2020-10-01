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

        holder.totalPriceTV.text = context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(totPrice.toDouble())
        holder.itemCountTV.text = itemCount.toString()
        if (itemCount > 1) {
            holder.itemTV.text = "Items"
        } else {
            holder.itemTV.text = "Item"
        }
        Log.e("TAG", "Live Order Supplier Discount : " + list[position].supplier_discount)
        if (list[position].supplier_discount != "" && list[position].supplier_discount != "0") {
            holder.discountLayout.visibility = View.VISIBLE
            holder.discountTotalLayout.visibility = View.VISIBLE
            holder.discountTxtTV.text = "Extra Discount ${list[position].supplier_discount}%"

            val pre = totPrice / 100
            val percentAmount: Float = (pre * list[position].supplier_discount.toFloat())
            val ourPrice: Float = (totPrice - percentAmount)
            holder.discountValueTV.text = "- " + doubleToStringTwoDecimal(percentAmount.toDouble())

            holder.afterDiscountValueTV.text =
                context.getString(R.string.Rs) + " " + doubleToStringTwoDecimal(ourPrice.toDouble())
        } else {
            holder.discountLayout.visibility = View.GONE
            holder.discountTotalLayout.visibility = View.GONE
        }

        /*holder.recyclerView.layoutManager = LinearLayoutManager(context)
        val supplierOrdersHistoryItemAdapter = SupplierOrdersHistoryItemAdapter(context, order, activity)
        holder.recyclerView.adapter = supplierOrdersHistoryItemAdapter
        holder.recyclerView.setHasFixedSize(true)*/

        if (order[0].product_name != "O2") {
            holder.itemLayout1.visibility = View.VISIBLE
            holder.statusTV1.visibility = View.VISIBLE
            holder.o2Layout.visibility = View.GONE
            holder.o2statusTV.visibility = View.GONE
            holder.productNameTV1.text = order[0].product_name
            holder.productQtyTV1.text = order[0].qty
            holder.productUnitTV1.text = order[0].unit
            holder.productRateTV1.text = "(" + doubleToStringNoDecimal(order[0].rate.toDouble()) + ")"
            holder.productPriceTV1.text = doubleToStringTwoDecimal(order[0].price.toDouble())



            if (order[0].order_status == "open") {
                holder.statusTV1.text = "Waiting for Seller apporval."
                holder.statusTV1.setTextColor(ContextCompat.getColor(context, R.color.gray))

                holder.productNameTV1.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productQtyTV1.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productUnitTV1.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV1.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV1.visibility = View.VISIBLE
            } else if (order[0].order_status == "user_cancelled") {
                holder.statusTV1.text = "User cancelled the item."
                holder.statusTV1.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productNameTV1.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productQtyTV1.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productUnitTV1.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productPriceTV1.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productPriceTV1.visibility = View.INVISIBLE
            } else if (order[0].order_status == "supplier_accepted") {
                holder.statusTV1.text = "Seller accepted. Waiting for delivery."
                holder.statusTV1.setTextColor(ContextCompat.getColor(context, R.color.buttonOnClickColor))

                holder.productNameTV1.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productQtyTV1.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productUnitTV1.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV1.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV1.visibility = View.VISIBLE
            } else if (order[0].order_status == "supplier_rejected") {
                holder.statusTV1.text = "Seller Rejected."
                holder.statusTV1.setTextColor(ContextCompat.getColor(context, R.color.brightColor))
                holder.productNameTV1.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productQtyTV1.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productUnitTV1.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productPriceTV1.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productPriceTV1.visibility = View.INVISIBLE
            } else if (order[0].order_status == "completed") {
                holder.statusTV1.text = "Order Delivered."
                holder.statusTV1.setTextColor(ContextCompat.getColor(context, R.color.buttonColor))

                holder.productNameTV1.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productQtyTV1.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productUnitTV1.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV1.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV1.visibility = View.VISIBLE
            }
        } else {
            holder.o2Layout.visibility = View.VISIBLE
            holder.o2statusTV.visibility = View.VISIBLE
            holder.itemLayout1.visibility = View.GONE
            holder.statusTV1.visibility = View.GONE
            holder.o2ProductTV.text = order[0].description
            if (order[0].price.toFloat() == 0f) {
                holder.o2ProductPriceTV.visibility = View.INVISIBLE
            } else {
                holder.o2ProductPriceTV.visibility = View.VISIBLE
                holder.o2ProductPriceTV.text = doubleToStringTwoDecimal(order[0].price.toDouble())
            }


            if (order[0].order_status == "open") {
                holder.o2statusTV.text = "Waiting for Seller apporval."
                holder.o2statusTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
            } else if (order[0].order_status == "user_cancelled") {
                holder.o2statusTV.text = "User cancelled the item."
                holder.o2statusTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.o2ProductTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.o2ProductPriceTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.o2ProductPriceTV.visibility = View.INVISIBLE
            } else if (order[0].order_status == "supplier_accepted") {
                holder.o2statusTV.text = "Seller accepted. Waiting for delivery."
                holder.o2statusTV.setTextColor(ContextCompat.getColor(context, R.color.buttonOnClickColor))
            } else if (order[0].order_status == "supplier_rejected") {
                holder.o2statusTV.text = "Seller Rejected."
                holder.o2statusTV.setTextColor(ContextCompat.getColor(context, R.color.brightColor))
                holder.o2ProductTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.o2ProductPriceTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.o2ProductPriceTV.visibility = View.INVISIBLE
            } else if (order[0].order_status == "completed") {
                holder.o2statusTV.text = "Order Delivered."
                holder.o2statusTV.setTextColor(ContextCompat.getColor(context, R.color.buttonColor))
            }
        }


        if (order.size >= 2) {
            holder.itemLayout2.visibility = View.VISIBLE
            holder.statusTV2.visibility = View.VISIBLE
            holder.productNameTV2.text = order[1].product_name
            holder.productQtyTV2.text = order[1].qty
            holder.productUnitTV2.text = order[1].unit
            holder.productRateTV2.text = "(" + doubleToStringNoDecimal(order[1].rate.toDouble()) + ")"
            holder.productPriceTV2.text = doubleToStringTwoDecimal(order[1].price.toDouble())



            if (order[1].order_status == "open") {
                holder.statusTV2.text = "Waiting for Seller apporval."
                holder.statusTV2.setTextColor(ContextCompat.getColor(context, R.color.gray))

                holder.productNameTV2.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productQtyTV2.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productUnitTV2.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV2.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV2.visibility = View.VISIBLE
            } else if (order[1].order_status == "user_cancelled") {
                holder.statusTV2.text = "User cancelled the item."
                holder.statusTV2.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productNameTV2.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productQtyTV2.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productUnitTV2.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productPriceTV2.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productPriceTV2.visibility = View.INVISIBLE
            } else if (order[1].order_status == "supplier_accepted") {
                holder.statusTV2.text = "Seller accepted. Waiting for delivery."
                holder.statusTV2.setTextColor(ContextCompat.getColor(context, R.color.buttonOnClickColor))

                holder.productNameTV2.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productQtyTV2.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productUnitTV2.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV2.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV2.visibility = View.VISIBLE
            } else if (order[1].order_status == "supplier_rejected") {
                holder.statusTV2.text = "Seller Rejected."
                holder.statusTV2.setTextColor(ContextCompat.getColor(context, R.color.brightColor))
                holder.productNameTV2.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productQtyTV2.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productUnitTV2.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productPriceTV2.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productPriceTV2.visibility = View.INVISIBLE
            } else if (order[1].order_status == "completed") {
                holder.statusTV2.text = "Order Delivered."
                holder.statusTV2.setTextColor(ContextCompat.getColor(context, R.color.buttonColor))

                holder.productNameTV2.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productQtyTV2.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productUnitTV2.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV2.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV2.visibility = View.VISIBLE
            }
        } else {
            holder.itemLayout2.visibility = View.GONE
            holder.statusTV2.visibility = View.GONE
        }


        if (order.size >= 3) {
            holder.itemLayout3.visibility = View.VISIBLE
            holder.statusTV3.visibility = View.VISIBLE
            holder.productNameTV3.text = order[2].product_name
            holder.productQtyTV3.text = order[2].qty
            holder.productUnitTV3.text = order[2].unit
            holder.productRateTV3.text = "(" + doubleToStringNoDecimal(order[2].rate.toDouble()) + ")"
            holder.productPriceTV3.text = doubleToStringTwoDecimal(order[2].price.toDouble())



            if (order[2].order_status == "open") {
                holder.statusTV3.text = "Waiting for Seller apporval."
                holder.statusTV3.setTextColor(ContextCompat.getColor(context, R.color.gray))

                holder.productNameTV3.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productQtyTV3.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productUnitTV3.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV3.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV3.visibility = View.VISIBLE
            } else if (order[2].order_status == "user_cancelled") {
                holder.statusTV3.text = "User cancelled the item."
                holder.statusTV3.setTextColor(ContextCompat.getColor(context, R.color.gray))

                holder.productNameTV3.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productQtyTV3.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productUnitTV3.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productPriceTV3.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productPriceTV3.visibility = View.INVISIBLE
            } else if (order[2].order_status == "supplier_accepted") {
                holder.statusTV3.text = "Seller accepted. Waiting for delivery."
                holder.statusTV3.setTextColor(ContextCompat.getColor(context, R.color.buttonOnClickColor))

                holder.productNameTV3.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productQtyTV3.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productUnitTV3.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV3.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV3.visibility = View.VISIBLE
            } else if (order[2].order_status == "supplier_rejected") {
                holder.statusTV3.text = "Seller Rejected."
                holder.statusTV3.setTextColor(ContextCompat.getColor(context, R.color.brightColor))

                holder.productNameTV3.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productQtyTV3.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productUnitTV3.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productPriceTV3.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.productPriceTV3.visibility = View.INVISIBLE
            } else if (order[2].order_status == "completed") {
                holder.statusTV3.text = "Order Delivered."
                holder.statusTV3.setTextColor(ContextCompat.getColor(context, R.color.buttonColor))

                holder.productNameTV3.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productQtyTV3.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productUnitTV3.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV3.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.productPriceTV3.visibility = View.VISIBLE
            }
        } else {
            holder.itemLayout3.visibility = View.GONE
            holder.statusTV3.visibility = View.GONE
        }

        if (order.size > 3) {
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
                    intent.putExtra("supplierDiscount", list[position].supplier_discount)
                    context.startActivity(intent)
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })
        }

        /*holder.recyclerView.setOnTouchListener { v, event ->
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
                        intent.putExtra("supplierDiscount", list[position].supplier_discount)
                        context.startActivity(intent)
                    }

                    override fun onAnimationStart(animation: Animation?) {
                    }
                })
            }
            false
        }*/

    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tokenTV = view.tokenTV!!
        val orderDateTV = view.orderDateTV!!
        val callLayout = view.callLayout!!
        val userNameTV = view.userNameTV!!
        val mobileNoTV = view.mobileNoTV!!
        //        val recyclerView = view.recyclerView!!
        val itemCountTV = view.itemCountTV!!
        val itemTV = view.itemTV!!
        val totalPriceTV = view.totalPriceTV!!
        val discountLayout = view.discountLayout!!
        val discountTxtTV = view.discountTxtTV!!
        val discountValueTV = view.discountValueTV!!
        val discountTotalLayout = view.discountTotalLayout!!
        val afterDiscountValueTV = view.afterDiscountValueTV!!
        val toolbarLayout = view.toolbarLayout!!
        val callIV = view.callIV!!
        val cardView = view.cardView!!


        val o2Layout = view.o2Layout!!
        val o2ProductTV = view.o2ProductTV!!
        val o2ProductPriceTV = view.o2ProductPriceTV!!
        val o2statusTV = view.o2statusTV!!
        val itemLayout1 = view.itemLayout1!!
        val productNameTV1 = view.productNameTV1!!
        val productQtyTV1 = view.productQtyTV1!!
        val productUnitTV1 = view.productUnitTV1!!
        val productRateTV1 = view.productRateTV1!!
        val productPriceTV1 = view.productPriceTV1!!
        val statusTV1 = view.statusTV1!!
        val itemLayout2 = view.itemLayout2!!
        val productNameTV2 = view.productNameTV2!!
        val productQtyTV2 = view.productQtyTV2!!
        val productUnitTV2 = view.productUnitTV2!!
        val productRateTV2 = view.productRateTV2!!
        val productPriceTV2 = view.productPriceTV2!!
        val statusTV2 = view.statusTV2!!
        val itemLayout3 = view.itemLayout3!!
        val productNameTV3 = view.productNameTV3!!
        val productQtyTV3 = view.productQtyTV3!!
        val productUnitTV3 = view.productUnitTV3!!
        val productRateTV3 = view.productRateTV3!!
        val productPriceTV3 = view.productPriceTV3!!
        val statusTV3 = view.statusTV3!!
        val moreItemCountTV = view.moreItemCountTV!!
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