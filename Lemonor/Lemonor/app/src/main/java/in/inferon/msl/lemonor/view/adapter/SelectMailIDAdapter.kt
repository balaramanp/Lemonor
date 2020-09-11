package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_list_adapter.view.*

class SelectMailIDAdapter(
    private val context: Context,
    private val list: ArrayList<String>
) : RecyclerView.Adapter<SelectMailIDAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_list_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.radioBT.text = list[position]

        holder.radioBT.setOnClickListener {
            loadData(position)
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val radioBT = view.radioBT!!
    }

    private fun loadData(position: Int) {
        val intent = Intent("LoadSelectedMailData")
        intent.putExtra("position", position)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}