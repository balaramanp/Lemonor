package `in`.inferon.msl.cucumbor.view.adapter

import `in`.inferon.msl.cucumbor.R
import `in`.inferon.msl.cucumbor.model.pojo.BillDetails
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bill_details_adapter.view.*

class BillDetailsAdapter(
    private val context: Context,
    private val billDetails: ArrayList<BillDetails>
) : RecyclerView.Adapter<BillDetailsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.bill_details_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return billDetails.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.productNameTV.text = billDetails[position].product_name
        holder.qtyTV.text = billDetails[position].qty
        holder.priceTV.text = context.getString(R.string.Rs) + " " + billDetails[position].price
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val productNameTV = view.productNameTV!!
        val qtyTV = view.qtyTV!!
        val priceTV = view.priceTV!!
    }
}