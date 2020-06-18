package `in`.inferon.msl.cucumbor.view.adapter

import `in`.inferon.msl.cucumbor.R
import `in`.inferon.msl.cucumbor.model.pojo.GroceryHistory
import `in`.inferon.msl.cucumbor.view.activity.GroceryHistoryActivity
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.grocery_history_adapter.view.*
import java.text.SimpleDateFormat

class GroceryHistoryAdapter(
    private val context: Context,
    private val groceryHistory: MutableList<GroceryHistory>,
    private val groceryHistoryActivity: GroceryHistoryActivity
) : RecyclerView.Adapter<GroceryHistoryAdapter.ViewHolder>() {
    private var groceryItemHistoryAdapter: GroceryItemHistoryAdapter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.grocery_history_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return groceryHistory.size
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date = SimpleDateFormat("yyyy-MM-dd").parse(groceryHistory[position].date)
        val sdf = SimpleDateFormat("dd MMM yy")
        holder.dateTV.text = sdf.format(date)
        holder.totalBillTV.text = groceryHistory[position].total_price
        holder.groceryItemHistoryRV.layoutManager = LinearLayoutManager(context)
        groceryItemHistoryAdapter =
            GroceryItemHistoryAdapter(context, groceryHistory[position].items, groceryHistoryActivity)
        holder.groceryItemHistoryRV.adapter = groceryItemHistoryAdapter
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val dateTV = view.dateTV!!
        val totalBillTV = view.totalBillTV!!
        val groceryItemHistoryRV = view.groceryItemHistoryRV!!
    }
}