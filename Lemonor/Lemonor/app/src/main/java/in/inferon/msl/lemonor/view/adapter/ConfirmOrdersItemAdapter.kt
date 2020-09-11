package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.pojo.Products
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.confirm_orders_item_adapter.view.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class ConfirmOrdersItemAdapter(
    private val context: Context,
    private val items: MutableList<Products>
) : RecyclerView.Adapter<ConfirmOrdersItemAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.confirm_orders_item_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.productNameTV.text = items[position].name
        holder.productQtyTV.text = items[position].qty
        holder.productUnitTV.text = items[position].unit
        holder.productRateTV.text = "(" + doubleToStringNoDecimal(items[position].rate.toDouble()) + ")"
        val price = items[position].qty.toInt() * items[position].rate.toFloat()
        holder.productPriceTV.text = doubleToStringNoDecimal(price.toDouble())
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val productNameTV = view.productNameTV!!
        val productQtyTV = view.productQtyTV!!
        val productUnitTV = view.productUnitTV!!
        val productRateTV = view.productRateTV!!
        val productPriceTV = view.productPriceTV!!
    }

    private fun doubleToStringNoDecimal(d: Double): String? {
        val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,##,###.##")
        return formatter.format(d)
    }
}