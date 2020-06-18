package `in`.inferon.msl.cucumbor.view.adapter

import `in`.inferon.msl.cucumbor.R
import `in`.inferon.msl.cucumbor.model.pojo.SalesTable
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.sales_report_adapter.view.*
import java.text.SimpleDateFormat

class SalesReportAdapter(
    private val context: Context,
    private val salesTable: List<SalesTable>
) : RecyclerView.Adapter<SalesReportAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.sales_report_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return salesTable.size
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date = SimpleDateFormat("dd/MM/yyyy").parse(salesTable[position].date)
        val sdf = SimpleDateFormat("dd MMM yy")
        holder.dateTV.text = sdf.format(date)
        holder.amTV.text = context.getString(R.string.Rs) + " " + salesTable[position].price_am + " ( " + salesTable[position].am + " Ltr)"
        holder.pmTV.text = context.getString(R.string.Rs) + " " + salesTable[position].price_pm + " ( " + salesTable[position].pm + " Ltr)"
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val dateTV = view.dateTV!!
        val amTV = view.amTV!!
        val pmTV = view.pmTV!!
    }
}