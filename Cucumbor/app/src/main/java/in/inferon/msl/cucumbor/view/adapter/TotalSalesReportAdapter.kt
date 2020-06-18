package `in`.inferon.msl.cucumbor.view.adapter

import `in`.inferon.msl.cucumbor.R
import `in`.inferon.msl.cucumbor.model.pojo.TotalSalesTable
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

class TotalSalesReportAdapter(
    private val context: Context,
    private val salesTable: List<TotalSalesTable>
) : RecyclerView.Adapter<TotalSalesReportAdapter.ViewHolder>() {
    private var billDetailsAdapter: BillDetailsAdapter? = null

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
        holder.amTV.text = context.getString(R.string.Rs) + " " + salesTable[position].price_am
        holder.pmTV.text = context.getString(R.string.Rs) + " " + salesTable[position].price_pm

        holder.amTV.setOnClickListener {
            if (salesTable[position].price_am != "0") {
                val dialog = Dialog(context)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.bill_details_dialog)

                val dialogTitleTV = dialog.findViewById(R.id.dialogTitleTV) as TextView
                val dialogRV = dialog.findViewById(R.id.dialogRV) as RecyclerView
                val totalPriceTV = dialog.findViewById(R.id.totalPriceTV) as TextView
                val diaOKBT = dialog.findViewById(R.id.diaOKBT) as Button

                dialogTitleTV.text = context.getString(R.string.am_bill_details)
                dialogRV.layoutManager = LinearLayoutManager(context)
                billDetailsAdapter = BillDetailsAdapter(context, salesTable[position].am)
                dialogRV.adapter = billDetailsAdapter

                var tot = 0f
                for (i in salesTable[position].am) {
                    tot += i.price.toFloat()
                }
                totalPriceTV.text = context.getString(R.string.Rs) + " " + tot.toString()

                diaOKBT.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
                val window = dialog.window!!
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            } else {
                Toast.makeText(context, "No Bill!", Toast.LENGTH_SHORT).show()
            }
        }

        holder.pmTV.setOnClickListener {
            if (salesTable[position].price_pm != "0") {
                val dialog = Dialog(context)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.bill_details_dialog)

                val dialogTitleTV = dialog.findViewById(R.id.dialogTitleTV) as TextView
                val dialogRV = dialog.findViewById(R.id.dialogRV) as RecyclerView
                val totalPriceTV = dialog.findViewById(R.id.totalPriceTV) as TextView
                val diaOKBT = dialog.findViewById(R.id.diaOKBT) as Button

                dialogTitleTV.text = context.getString(R.string.pm_bill_details)
                dialogRV.layoutManager = LinearLayoutManager(context)
                billDetailsAdapter = BillDetailsAdapter(context, salesTable[position].pm)
                dialogRV.adapter = billDetailsAdapter

                var tot = 0f
                for (i in salesTable[position].pm) {
                    tot += i.price.toFloat()
                }
                totalPriceTV.text = tot.toString()

                diaOKBT.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
                val window = dialog.window!!
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            } else {
                Toast.makeText(context, "No Bill!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val dateTV = view.dateTV!!
        val amTV = view.amTV!!
        val pmTV = view.pmTV!!
    }
}