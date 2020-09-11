package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.view.activity.ViewSupplierProductActivity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.select_categories_adapter.view.*

class VSPSelectCategoriesAdapter(
    private val context: Context,
    private val list: MutableList<String>,
    private val viewSupplierProductActivity: ViewSupplierProductActivity
) : RecyclerView.Adapter<VSPSelectCategoriesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.select_categories_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.categoryRB.text = list[position]

        holder.categoryRB.setOnClickListener {
//            loadData(list[position], position)
            viewSupplierProductActivity.receiveClickListener(list[position])
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val categoryRB = view.categoryRB!!
    }

    private fun loadData(category: String, position: Int) {
        val intent = Intent("SelectCategory")
        intent.putExtra("position", position)
        intent.putExtra("selectCategory", category)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}