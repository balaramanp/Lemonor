package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.pojo.Unit
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.categories_adapter.view.*

class UnitsAdapter(
    private val context: Context,
    private val list: MutableList<Unit>,
    private val from: String,
    private val isCustom: String
) : RecyclerView.Adapter<UnitsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.categories_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.checkBox.text = list[position].unit
        if (isCustom != "custom"){
            if (list[position].unit == "Kg") {
                holder.checkBox.isChecked = true
            }
        }

        holder.checkBox.setOnClickListener {
            if (holder.checkBox.isChecked) {
                if (from == "adapter") {
                    loadAdapterData(list[position].unit, position)
                } else {
                    loadData(list[position].unit, position)
                }
            }
        }


    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val checkBox = view.checkBox!!
        val categoryNameET = view.categoryNameET!!
    }

    private fun loadAdapterData(unit: String, position: Int) {
        val intent = Intent("SelectedAdapterUnit")
        intent.putExtra("position", position)
        intent.putExtra("selectedUnit", unit)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun loadData(unit: String, position: Int) {
        val intent = Intent("SelectedUnit")
        intent.putExtra("position", position)
        intent.putExtra("selectedUnit", unit)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}