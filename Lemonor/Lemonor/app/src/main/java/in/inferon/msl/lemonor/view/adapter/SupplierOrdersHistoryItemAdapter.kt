package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.pojo.Order
import `in`.inferon.msl.lemonor.repo.Repository
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.supplier_orders_history_item_adapter.view.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class SupplierOrdersHistoryItemAdapter(
    private val context: Context,
    private val items: MutableList<Order>,
    private val activity: AppCompatActivity
) : RecyclerView.Adapter<SupplierOrdersHistoryItemAdapter.ViewHolder>() {
    private var repo: Repository? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        repo = Repository()
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.supplier_orders_history_item_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (items[position].product_name != "O2"){
            holder.itemLayout.visibility = View.VISIBLE
            holder.o2Layout.visibility = View.GONE
            holder.productNameTV.text = items[position].product_name
            holder.productQtyTV.text = items[position].qty
            holder.productUnitTV.text = items[position].unit
            holder.productRateTV.text = "(" + doubleToStringNoDecimal(items[position].rate.toDouble()) + ")"
            holder.productPriceTV.text = doubleToStringNoDecimal(items[position].price.toDouble())
        }else {
            holder.o2Layout.visibility = View.VISIBLE
            holder.itemLayout.visibility = View.GONE
            holder.o2ProductTV.text = items[position].description
            holder.o2ProductPriceTV.text = doubleToStringNoDecimal(items[position].price.toDouble())
        }


        if (items[position].order_status == "open") {
            holder.statusTV.text = "Waiting for Seller apporval."
            holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
        } else if (items[position].order_status == "user_cancelled") {
            holder.statusTV.text = "User cancelled the item."
            holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
            holder.productNameTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
            holder.productQtyTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
            holder.productUnitTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
            holder.productPriceTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
            holder.o2ProductTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
            holder.o2ProductPriceTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
            holder.productPriceTV.visibility = View.INVISIBLE
            holder.o2ProductPriceTV.visibility = View.INVISIBLE
        } else if (items[position].order_status == "supplier_accepted") {
            holder.statusTV.text = "Seller accepted. Waiting for delivery."
            holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.buttonOnClickColor))
        } else if (items[position].order_status == "supplier_rejected") {
            holder.statusTV.text = "Seller Rejected."
            holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.brightColor))
            holder.productNameTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
            holder.productQtyTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
            holder.productUnitTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
            holder.productPriceTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
            holder.o2ProductTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
            holder.o2ProductPriceTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
            holder.productPriceTV.visibility = View.INVISIBLE
            holder.o2ProductPriceTV.visibility = View.INVISIBLE
        } else if (items[position].order_status == "completed") {
            holder.statusTV.text = "Order Delivered."
            holder.statusTV.setTextColor(ContextCompat.getColor(context, R.color.buttonColor))
        }

    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val productNameTV = view.productNameTV!!
        val productQtyTV = view.productQtyTV!!
        val productUnitTV = view.productUnitTV!!
        val productRateTV = view.productRateTV!!
        val productPriceTV = view.productPriceTV!!
        val statusTV = view.statusTV!!
        val itemLayout = view.itemLayout!!
        val o2Layout = view.o2Layout!!
        val o2ProductTV = view.o2ProductTV!!
        val o2ProductPriceTV = view.o2ProductPriceTV!!
    }

    private fun doubleToStringNoDecimal(d: Double): String? {
        val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,##,###.##")
        return formatter.format(d)
    }
}