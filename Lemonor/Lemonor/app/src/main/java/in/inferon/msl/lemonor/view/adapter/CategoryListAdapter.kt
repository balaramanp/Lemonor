package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.pojo.Category
import `in`.inferon.msl.lemonor.model.pojo.MajorCategory
import `in`.inferon.msl.lemonor.view.activity.PlaceOrderActivity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.category_list_adapter.view.*
import java.lang.Exception

class CategoryListAdapter(
    private val context: Context,
    private val categoryList: MutableList<MajorCategory>,
    private val placeOrderActivity: PlaceOrderActivity
) : RecyclerView.Adapter<CategoryListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.category_list_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return if (categoryList.size >= 3) {
            3
        } else {
            categoryList.size
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imgUrl =
            Constants.CATEGORY_IMG_BASE_URL + categoryList[position].categoryName + ".jpg"
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

        holder.categoryTV.text = categoryList[position].categoryName
        holder.cardView.setOnClickListener {
            placeOrderActivity.receiveClickListener(categoryList[position].categoryName)
        }
        Log.e("TAG", "Category Product Exists : " + categoryList[position].isProductExists)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val cardView = view.cardView!!
        val imageView = view.imageView!!
        val categoryTV = view.categoryTV!!
    }
}