package `in`.inferon.msl.cucumbor.view.adapter

import `in`.inferon.msl.cucumbor.R
import `in`.inferon.msl.cucumbor.model.pojo.GroceryHistoryItem
import `in`.inferon.msl.cucumbor.repo.Repository
import `in`.inferon.msl.cucumbor.view.activity.GroceryHistoryActivity
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.grocery_item_history_adapter.view.*
import org.json.JSONObject

class GroceryItemHistoryAdapter(
    private val context: Context,
    private val groceryHistoryItem: ArrayList<GroceryHistoryItem>,
    private val groceryHistoryActivity: GroceryHistoryActivity
) : RecyclerView.Adapter<GroceryItemHistoryAdapter.ViewHolder>() {
    private var repo: Repository? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        repo = Repository()
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.grocery_item_history_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return groceryHistoryItem.size
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.productNameTV.text = groceryHistoryItem[position].product_name
        holder.productPriceTV.text = groceryHistoryItem[position].qty + " " + groceryHistoryItem[position].unit +
                " ( " + context.getString(R.string.Rs) + " " + groceryHistoryItem[position].price + "/- )"

        Log.e("TAG", "Projection ID : " + groceryHistoryItem[position].projection_id)
        if (groceryHistoryItem[position].projection_id != null) {
            holder.cancelLayout.visibility = View.VISIBLE
            holder.cancelTV.visibility = View.VISIBLE
        } else {
            holder.cancelLayout.visibility = View.GONE
        }

        holder.cancelLayout.setOnClickListener {
            holder.cancelTV.visibility = View.GONE
            holder.updatingLayout.visibility = View.VISIBLE
            val obj = JSONObject()
            obj.put("client_app_update_skip_for_grocery_by_projection_id", "")
            obj.put("projection_id", groceryHistoryItem[position].projection_id)
            repo!!.cancelGroceryItem(obj.toString())

            repo!!.cancelGroceryItem.observe(groceryHistoryActivity, androidx.lifecycle.Observer {
                run {
                    if (it.isNotEmpty()) {
                        val jobj = JSONObject(it)
                        if (jobj.getString("status") == "ok") {
                            sendGroceryCancel()
                            Toast.makeText(context, "Item Removed Successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("TAG", "Something Went Wrong!")
                        }
                    } else {
                        Log.e("TAG", "Get Milk Sales List Response is Empty")
                    }
                }
            })
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.imageView!!
        val productNameTV = view.productNameTV!!
        val productPriceTV = view.productPriceTV!!
        val cancelLayout = view.cancelLayout!!
        val cancelTV = view.cancelTV!!
        val updatingLayout = view.updatingLayout!!
    }

    private fun sendGroceryCancel() {
        val intent = Intent("GroceryCancel")
        intent.putExtra("groceryCancel", "done")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}