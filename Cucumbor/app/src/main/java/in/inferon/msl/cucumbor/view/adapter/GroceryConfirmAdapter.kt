package `in`.inferon.msl.cucumbor.view.adapter

import `in`.inferon.msl.cucumbor.R
import `in`.inferon.msl.cucumbor.model.Constants
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.grocery_confirm_adapter.view.*
import org.json.JSONArray
import java.lang.Exception

class GroceryConfirmAdapter(
    private val context: Context,
    private val confirmArray: JSONArray
) : RecyclerView.Adapter<GroceryConfirmAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.grocery_confirm_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return confirmArray.length()
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imgURL = Constants.IMAGE_BASE_URL + confirmArray.getJSONObject(position).getString("milk_type_id").substring(0, 6) + ".jpg"
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

        holder.productNameTV.text = confirmArray.getJSONObject(position).getString("milk_type_name")
        holder.productUnitTV.text =  context.getString(R.string.Rs) + " " + confirmArray.getJSONObject(position).getString("price").toInt() + "/- for " +
                confirmArray.getJSONObject(position).getString("qty") + " " + confirmArray.getJSONObject(position).getString("show_unit")
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.imageView!!
        val productNameTV = view.productNameTV!!
        val productUnitTV = view.productUnitTV!!
    }
}