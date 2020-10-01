package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.pojo.MajorCategory
import `in`.inferon.msl.lemonor.view.activity.PlaceOrderActivity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.select_categories_adapter.view.*
import java.lang.Exception

class SelectCategoriesAdapter(
    private val context: Context,
    private val list: MutableList<MajorCategory>,
    private val placeOrderActivity: PlaceOrderActivity
) : RecyclerView.Adapter<SelectCategoriesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.select_categories_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imgUrl =
            Constants.CATEGORY_IMG_BASE_URL + list[position].categoryName + ".jpg"
        Log.e("TAG", "Category Image URL : $imgUrl")
        try {
            Picasso.get().load(imgUrl).into(holder.imageView, object : Callback {
                override fun onSuccess() {
                }

                override fun onError(e: Exception?) {

                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

        holder.categoryTV.text = list[position].categoryName

        holder.cardView.setOnClickListener {
            //            loadData(list[position], position)
            placeOrderActivity.receiveClickListener(list[position].categoryName)
        }

        if (list[position].isProductExists){
            holder.cardView.alpha = 1.0f
            holder.cardView.isClickable = true
        }else{
            holder.cardView.alpha = 0.5f
            holder.cardView.isClickable = false
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val cardView = view.cardView!!
        val imageView = view.imageView!!
        val categoryTV = view.categoryTV!!
    }
}