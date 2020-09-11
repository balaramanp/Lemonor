package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.pojo.SuppliersForCustomer
import `in`.inferon.msl.lemonor.view.activity.PlaceOrderActivity
import `in`.inferon.msl.lemonor.view.activity.SupplierInfoActivity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.categories_adapter.view.*
import kotlinx.android.synthetic.main.preferred_supplier_adapter.view.*

class PreferredSupplierAdapter(
    private val context: Context,
    private val preferredSupplierList: MutableList<SuppliersForCustomer>
) : RecyclerView.Adapter<PreferredSupplierAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.preferred_supplier_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return preferredSupplierList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imgUrl = Constants.SHOP_IMG_BASE_URL + preferredSupplierList[position].user_id + ".jpg"
        Log.e("TAG", "Image URL : $imgUrl")
        Picasso.get().load(imgUrl).into(holder.imageView)
        holder.shopNameTV.text = preferredSupplierList[position].shop_name
        holder.descriptionTV.text = preferredSupplierList[position].shop_description
        if (preferredSupplierList[position].supplier_rating != null && preferredSupplierList[position].supplier_rating != "0") {
            holder.overRatingLayout.visibility = View.GONE
            holder.overRatingTV.text = preferredSupplierList[position].supplier_rating
        } else {
            holder.overRatingLayout.visibility = View.GONE
        }

        holder.cardView.setOnClickListener {
            val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.card_click_anim)
            holder.cardView.startAnimation(animation)

            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    val intent = Intent(context, PlaceOrderActivity::class.java)
                    intent.putExtra("supplier_id", preferredSupplierList[position].user_id)
                    intent.putExtra("shop_name", preferredSupplierList[position].shop_name)
                    context.startActivity(intent)
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })
        }

        holder.shopDetailLayout.setOnClickListener {
            val intent = Intent(context, SupplierInfoActivity::class.java)
            intent.putExtra("supplier_id", preferredSupplierList[position].user_id)
            intent.putExtra("from", "supplier_list")
            context.startActivity(intent)
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val cardView = view.cardView!!
        val imageView = view.imageView!!
        val shopNameTV = view.shopNameTV!!
        val descriptionTV = view.descriptionTV!!
        val overRatingLayout = view.overRatingLayout!!
        val overRatingTV = view.overRatingTV!!
        val shopDetailLayout = view.shopDetailLayout!!
    }
}