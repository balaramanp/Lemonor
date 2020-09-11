package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.pojo.Category
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.categories_adapter.view.*

class CategoriesAdapter(
    private val context: Context,
    private val list: MutableList<Category>
) : RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {
//    private var selectedPosition = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.categories_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.checkBox.text = list[position].category
//        holder.checkBox.isChecked = selectedPosition == position

        /*for(i in list){
            list[position].selected = selectedPosition == position
        }*/

        holder.checkBox.setOnClickListener {
            if (holder.checkBox.isChecked) {
//                selectedPosition = position
                loadData(list[position].category, position)
//                notifyDataSetChanged()
            }
        }


    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val checkBox = view.checkBox!!
        val categoryNameET = view.categoryNameET!!
    }

    private fun loadData(category: String, position: Int) {
        val intent = Intent("SelectedCategory")
        intent.putExtra("position", position)
        intent.putExtra("selectedCategory", category)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}