package `in`.inferon.msl.cucumbor.view.adapter

import `in`.inferon.msl.cucumbor.R
import `in`.inferon.msl.cucumbor.model.Constants
import `in`.inferon.msl.cucumbor.model.pojo.Grocery
import `in`.inferon.msl.cucumbor.repo.Repository
import `in`.inferon.msl.cucumbor.view.activity.MainActivity
import android.annotation.SuppressLint
import android.content.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.grocery_list_adapter.view.*
import java.lang.Exception
import android.view.animation.Animation
import android.view.animation.AnimationUtils


class GroceryListAdapter(
    private val context: Context,
    private val groceryList: List<Grocery>,
    private val mainActivity: MainActivity
) : RecyclerView.Adapter<GroceryListAdapter.ViewHolder>() {
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    private var repo: Repository? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        repo = Repository()
        shared = context.getSharedPreferences(PREF, AppCompatActivity.MODE_PRIVATE)
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.grocery_list_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return groceryList.size
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.productNameTV.text = groceryList[position].name
        val sp =
            (groceryList[position].price_range.toFloat() * groceryList[position].cf.toFloat() * groceryList[position].mq.toFloat()).toInt()
        holder.productPriceTV.text =
            context.getString(R.string.Rs) + " " + sp + "/- " + groceryList[position].mq + " " + groceryList[position].show_unit
        holder.productUnitTV.text =
            context.getString(R.string.Rs) + " " + sp + "/- for " + groceryList[position].mq + " " + groceryList[position].show_unit

        val imgURL = Constants.IMAGE_BASE_URL + groceryList[position].id.substring(0, 6) + ".jpg"
        Log.e("TAG", "Grocery image URL : $imgURL")

        Picasso.get()
            .load(imgURL)
            .into(holder.imageView, object : com.squareup.picasso.Callback {
                override fun onError(e: Exception?) {
                    Picasso.get().load(R.drawable.default_img).into(holder.imageView)
                }

                override fun onSuccess() {

                }
            })

        Picasso.get()
            .load(imgURL)
            .into(holder.imageViewTemp, object : com.squareup.picasso.Callback {
                override fun onError(e: Exception?) {
                    Picasso.get().load(R.drawable.default_img).into(holder.imageViewTemp)
                }

                override fun onSuccess() {

                }
            })


        holder.layout.setOnClickListener {
            holder.imageViewTemp.visibility = View.VISIBLE
            val animation: Animation = AnimationUtils.loadAnimation(
                context,
                R.anim.translate_right
            )
            holder.imageViewTemp.startAnimation(animation)

            if (holder.qtyET.text.toString().trim().toInt() == 0) {
                holder.qtyET.text = (groceryList[position].mq.toInt()).toString()
                groceryList[position].qty =
                    (holder.qtyET.text.toString().trim().toInt()).toString()
                groceryList[position].calculated_price = sp
                sendPlusClicked(sp)
            } else if (holder.qtyET.text.toString().trim().toInt() > 0) {
                val q = holder.qtyET.text.toString().trim().toInt() + groceryList[position].iq.toInt()
                holder.qtyET.text = q.toString()
                groceryList[position].qty =
                    (q).toString()

                val pu =
                    (groceryList[position].price_range.toFloat() * groceryList[position].cf.toFloat() * holder.qtyET.text.toString().trim().toInt()).toInt()
                holder.productUnitTV.text =
                    context.getString(R.string.Rs) + " " + pu + "/- for " + holder.qtyET.text.toString().trim().toInt() + " " + groceryList[position].show_unit
                groceryList[position].calculated_price = pu
                sendPlusClicked(pu)
            }
        }

        holder.qtyET.setOnClickListener {
            if (holder.qtyET.text.toString() != "0"){
                holder.imageViewTemp.visibility = View.VISIBLE
                val animation: Animation = AnimationUtils.loadAnimation(
                    context,
                    R.anim.translate_left
                )
                holder.imageViewTemp.startAnimation(animation)
            }

            if (holder.qtyET.text.toString().trim().toInt() > groceryList[position].mq.toFloat()) {
                val q = holder.qtyET.text.toString().trim().toInt() - groceryList[position].iq.toInt()
                holder.qtyET.text = q.toString()
                groceryList[position].qty =
                    (q).toString()

                val pu =
                    (groceryList[position].price_range.toFloat() * groceryList[position].cf.toFloat() * holder.qtyET.text.toString().trim().toInt()).toInt()
                holder.productUnitTV.text =
                    context.getString(R.string.Rs) + " " + pu + "/- for " + holder.qtyET.text.toString().trim().toInt() + " " + groceryList[position].show_unit
                groceryList[position].calculated_price = pu
                sendMinusClicked(pu)
            } else if (holder.qtyET.text.toString().trim().toFloat() == groceryList[position].mq.toFloat()) {
                holder.qtyET.text = "0"
                groceryList[position].qty =
                    "0"
                holder.productUnitTV.text =
                    context.getString(R.string.Rs) + " " + sp + "/- for " + groceryList[position].mq + " " + groceryList[position].show_unit
                groceryList[position].calculated_price = 0
                sendMinusClicked(sp)
            }
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val layout = view.layout!!
        val imageView = view.imageView!!
        val imageViewTemp = view.imageViewTemp!!
        val productNameTV = view.productNameTV!!
        val productPriceTV = view.productPriceTV!!
        val productUnitTV = view.productUnitTV!!
        val qtyET = view.qtyET!!
    }

    private fun sendPlusClicked(tot: Int) {
        val intent = Intent("GoodsPlusQtyClick")
        intent.putExtra("goodsPlusQtyClick", tot)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun sendMinusClicked(tot: Int) {
        val intent = Intent("GoodsMinusQtyClick")
        intent.putExtra("goodsMinusQtyClick", tot)
        Log.e("TAG", "Minus Value : $tot")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}