package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.pojo.Products
import `in`.inferon.msl.lemonor.view.activity.SearchUpdatePriceActivity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.update_product_search_adapter.view.*

class UpdateProductSearchAdapter(
    private val context: Context,
    private val productsList: MutableList<Products>,
    private val activity: AppCompatActivity
) : RecyclerView.Adapter<UpdateProductSearchAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.update_product_search_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return productsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.productNameTV.text = productsList[position].name
        if (productsList[position].local_name != "") {
            holder.productAliasNameTV.visibility = View.VISIBLE
            holder.productAliasNameTV.text = productsList[position].local_name
        } else {
            holder.productAliasNameTV.visibility = View.GONE
        }
        holder.priceTV.text = productsList[position].rate
        holder.unitTV.text = productsList[position].unit
        holder.descriptionTV.text = productsList[position].description

        holder.layout.setOnClickListener {
            //            loadData(productsList[position].product_id)

            val intent = Intent(context, SearchUpdatePriceActivity::class.java)
            intent.putExtra("product", Gson().toJson(productsList[position]))
            intent.putExtra("position", position)
            context.startActivity(intent)
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val productNameTV = view.productNameTV!!
        val productAliasNameTV = view.productAliasNameTV!!
        val priceTV = view.priceTV!!
        val unitTV = view.unitTV!!
        val descriptionTV = view.descriptionTV!!
        val layout = view.layout!!
    }

    private fun loadData(productID: String) {
        val intent = Intent("SelectedProduct")
        intent.putExtra("product_id", productID)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}