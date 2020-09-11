package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.pojo.SafetyMeasure
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.safety_measure_adapter.view.*

class SafetyMeasureAdapter(
    private val context: Context,
    private val list: MutableList<SafetyMeasure>
) : RecyclerView.Adapter<SafetyMeasureAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.safety_measure_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.titleTV.text = list[position].title
        holder.contentTv.text = list[position].content
        val imgUrl = Constants.SM_IMG_BASE_URL + list[position].img
        Picasso.get().load(imgUrl).into(holder.imageView)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val titleTV = view.titleTV!!
        val imageView = view.imageView!!
        val contentTv = view.contentTv!!
    }
}