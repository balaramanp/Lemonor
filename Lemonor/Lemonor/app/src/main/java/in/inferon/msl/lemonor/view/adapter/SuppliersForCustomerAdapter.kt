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
import kotlinx.android.synthetic.main.supplier_for_customer_adapter.view.*
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.*
import kotlin.collections.ArrayList


class SuppliersForCustomerAdapter(
    private val context: Context,
    private val supplierForCustomerList: MutableList<SuppliersForCustomer>,
    private val activity: AppCompatActivity,
    private val imagesList: ArrayList<String>,
    private val prefferedSupplierList: MutableList<SuppliersForCustomer>
) : RecyclerView.Adapter<SuppliersForCustomerAdapter.ViewHolder>() {
    private var shared: SharedPreferences? = null
    private val PREF = "Pref"
    private var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        shared = context.getSharedPreferences(PREF, AppCompatActivity.MODE_PRIVATE)
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.supplier_for_customer_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return supplierForCustomerList.size + 2
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.e("TAG", "Images List Size : " + imagesList.size)
        if (position == 0) {
            if (imagesList.size > 0) {
                holder.imageLayout.visibility = View.VISIBLE
                holder.viewPager.visibility = View.VISIBLE
                holder.cardView.visibility = View.GONE
                holder.preferredSupplierLayout.visibility = View.GONE
                holder.positionOneLayout.visibility = View.GONE
                val imageViewPagerAdapter = ImageViewPagerAdapter(context, imagesList)
                holder.viewPager.adapter = imageViewPagerAdapter
                holder.tabLayout.setupWithViewPager(holder.viewPager, true)

                var page = 0
                val handler = Handler()
                val update = Runnable {
                    holder.viewPager.setCurrentItem(page, true)
                    if (page == imagesList.size) {
                        page = 0
                    } else {
                        ++page
                    }
                }

                val timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        handler.post(update)
                    }
                }, 1000, 6000)
            } else {
                holder.imageLayout.visibility = View.GONE
                holder.cardView.visibility = View.GONE
                holder.preferredSupplierLayout.visibility = View.GONE
            }
        } else if (position == 1) {
            holder.positionOneLayout.visibility = View.VISIBLE
            if (prefferedSupplierList.size > 0) {
                holder.preferredSupplierLayout.visibility = View.VISIBLE
                holder.cardView.visibility = View.GONE
                holder.imageLayout.visibility = View.GONE

                holder.preferredSupplierRV.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                val preferredSupplierAdapter = PreferredSupplierAdapter(context, prefferedSupplierList)
                holder.preferredSupplierRV.adapter = preferredSupplierAdapter
            } else {
                holder.preferredSupplierLayout.visibility = View.GONE
                holder.cardView.visibility = View.GONE
                holder.imageLayout.visibility = View.GONE
            }
        } else {
            holder.positionOneLayout.visibility = View.GONE
            holder.preferredSupplierLayout.visibility = View.GONE
            holder.imageLayout.visibility = View.GONE
            holder.cardView.visibility = View.VISIBLE

            holder.shopNameTV.text = supplierForCustomerList[position - 2].shop_name
            if (supplierForCustomerList[position - 2].supplier_rating != null && supplierForCustomerList[position - 2].supplier_rating != "0") {
                holder.overRatingLayout.visibility = View.VISIBLE
                holder.overRatingTV.text = supplierForCustomerList[position - 2].supplier_rating
                holder.ratingLayout.visibility = View.INVISIBLE
                holder.ratingTV.text = supplierForCustomerList[position - 2].supplier_rating
            } else {
                holder.overRatingLayout.visibility = View.GONE
                holder.ratingLayout.visibility = View.GONE
            }
            if (supplierForCustomerList[position - 2].shop_description != null && supplierForCustomerList[position - 2].shop_description != "") {
                holder.shopDescriptionTV.visibility = View.VISIBLE
                holder.shopDescriptionTV.text = supplierForCustomerList[position - 2].shop_description
            } else {
                holder.shopDescriptionTV.visibility = View.GONE
            }
            holder.addressTV.text = supplierForCustomerList[position - 2].address
            holder.distanceTV.text = supplierForCustomerList[position - 2].distance + " +"
            holder.districtTV.text = supplierForCustomerList[position - 2].district + ","
            holder.stateTV.text = supplierForCustomerList[position - 2].state

            holder.distanceLayout.setOnClickListener {
                val from = shared!!.getString("location", "")
                val to = supplierForCustomerList[position - 2].location
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
                        intent.putExtra("supplier_id", supplierForCustomerList[position - 2].user_id)
                        intent.putExtra("shop_name", supplierForCustomerList[position - 2].shop_name)
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
                intent.putExtra("supplier_id", supplierForCustomerList[position - 2].user_id)
                intent.putExtra("from", "supplier_list")
                context.startActivity(intent)
            }

//        setAnimation(holder.cardView, position-2)
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
        val imageLayout = view.imageLayout!!
        val viewPager = view.viewPager!!
        val tabLayout = view.tabLayout!!
        val positionOneLayout = view.positionOneLayout!!
        val preferredSupplierLayout = view.preferredSupplierLayout!!
        val preferredSupplierRV = view.preferredSupplierRV!!
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