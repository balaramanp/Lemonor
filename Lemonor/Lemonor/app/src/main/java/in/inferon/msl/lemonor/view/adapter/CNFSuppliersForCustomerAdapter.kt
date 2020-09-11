package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.pojo.SuppliersForCustomer
import `in`.inferon.msl.lemonor.view.activity.PlaceOrderActivity
import `in`.inferon.msl.lemonor.view.activity.SupplierInfoActivity
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.cnf_supplier_for_customer_adapter.view.*
import java.util.*


class CNFSuppliersForCustomerAdapter(
    private val context: Context,
    private val supplierForCustomerList: MutableList<SuppliersForCustomer>
) : RecyclerView.Adapter<CNFSuppliersForCustomerAdapter.ViewHolder>() {
    private var shared: SharedPreferences? = null
    private val PREF = "Pref"
    private var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        shared = context.getSharedPreferences(PREF, AppCompatActivity.MODE_PRIVATE)
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.cnf_supplier_for_customer_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return supplierForCustomerList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.shopNameTV.text = supplierForCustomerList[position].shop_name
        if (supplierForCustomerList[position].supplier_rating != null && supplierForCustomerList[position].supplier_rating != "0") {
            holder.overRatingLayout.visibility = View.VISIBLE
            holder.overRatingTV.text = supplierForCustomerList[position].supplier_rating
            holder.ratingLayout.visibility = View.INVISIBLE
            holder.ratingTV.text = supplierForCustomerList[position].supplier_rating
        } else {
            holder.overRatingLayout.visibility = View.GONE
            holder.ratingLayout.visibility = View.GONE
        }
        if (supplierForCustomerList[position].shop_description != null && supplierForCustomerList[position].shop_description != "") {
            holder.shopDescriptionTV.visibility = View.VISIBLE
            holder.shopDescriptionTV.text = supplierForCustomerList[position].shop_description
        } else {
            holder.shopDescriptionTV.visibility = View.GONE
        }
        holder.addressTV.text = supplierForCustomerList[position].address
        holder.distanceTV.text = supplierForCustomerList[position].distance + " +"
        holder.districtTV.text = supplierForCustomerList[position].district + ","
        holder.stateTV.text = supplierForCustomerList[position].state

        holder.distanceLayout.setOnClickListener {
            val from = shared!!.getString("location", "")
            val to = supplierForCustomerList[position].location
            val intent = Intent(
                android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=$from&daddr=$to")
            )
            context.startActivity(intent)
        }

        holder.cardView.setOnClickListener {
            val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.card_click_anim)
            holder.cardView.startAnimation(animation)

            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    val intent = Intent(context, PlaceOrderActivity::class.java)
                    intent.putExtra("supplier_id", supplierForCustomerList[position].user_id)
                    intent.putExtra("shop_name", supplierForCustomerList[position].shop_name)
                    context.startActivity(intent)
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })

            /*val imageViewPair = Pair.create<View?, String?>(holder.shopNameTV, "shopName")
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imageViewPair)
            context.startActivity(intent, options.toBundle())*/
        }

        holder.shopLogoIV.setOnClickListener {
            val intent = Intent(context, SupplierInfoActivity::class.java)
            intent.putExtra("supplier_id", supplierForCustomerList[position].user_id)
            intent.putExtra("from", "supplier_list")
            context.startActivity(intent)
        }
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        val animation = AnimationUtils.loadAnimation(
            context,
            if (position > lastPosition)
                R.anim.up_from_bottom
            else
                R.anim.down_from_top
        )
        viewToAnimate.startAnimation(animation)
        lastPosition = position
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val cardView = view.cardView!!
        val shopNameTV = view.shopNameTV!!
        val shopDescriptionTV = view.shopDescriptionTV!!
        val addressTV = view.addressTV!!
        val distanceTV = view.distanceTV!!
        val districtTV = view.districtTV!!
        val stateTV = view.stateTV!!
        val shopLogoIV = view.shopLogoIV!!
        val distanceLayout = view.distanceLayout!!
        val ratingLayout = view.ratingLayout!!
        val ratingTV = view.ratingTV!!
        val overRatingLayout = view.overRatingLayout!!
        val overRatingTV = view.overRatingTV!!
    }
}