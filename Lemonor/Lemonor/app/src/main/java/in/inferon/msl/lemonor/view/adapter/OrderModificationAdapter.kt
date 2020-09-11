package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Utils
import `in`.inferon.msl.lemonor.model.pojo.Order
import `in`.inferon.msl.lemonor.repo.Repository
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.order_modification_adapter.view.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt

class OrderModificationAdapter(
    private val context: Context,
    private val openOrder: MutableList<Order>,
    private val activity: AppCompatActivity
) : RecyclerView.Adapter<OrderModificationAdapter.ViewHolder>() {
    private var repo: Repository? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        repo = Repository()
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.order_modification_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return openOrder.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (openOrder[position].product_name == "O2") {
            holder.productNameTV.text = openOrder[position].description
            holder.qtyET.setText("1")
            holder.unitTV.text = ""
        } else {
            holder.productNameTV.text = openOrder[position].product_name
            holder.qtyET.setText(openOrder[position].qty)
            holder.unitTV.text = openOrder[position].unit
        }
        if (openOrder[position].local_name != null && openOrder[position].local_name.length > 0) {
            holder.localNameTV.visibility = View.VISIBLE
            holder.localNameTV.text = openOrder[position].local_name
        } else {
            holder.localNameTV.visibility = View.GONE
        }
        holder.rateET.setText(openOrder[position].rate)
        holder.priceTV.text = openOrder[position].price

        holder.qtyET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (holder.qtyET.text.toString().trim().isEmpty()) {
                    holder.priceTV.text = "0"
                    holder.saveBT.visibility = View.VISIBLE
                }

                if (holder.qtyET.text.toString().trim().length > 0 && holder.rateET.text.toString().trim().length > 0) {
                    holder.saveBT.visibility = View.VISIBLE
                    val totPrice =
                        holder.qtyET.text.toString().trim().toFloat() * holder.rateET.text.toString().trim().toFloat()
                    holder.priceTV.text = totPrice.toString()

                    openOrder[position].order_modified = true
                    openOrder[position].order_modification_saved = "false"
                }
            }
        })

        holder.rateET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (holder.rateET.text.toString().trim().isEmpty()) {
                    holder.priceTV.text = "0"
                    holder.saveBT.visibility = View.VISIBLE
                }

                if (holder.qtyET.text.toString().trim().length > 0 && holder.rateET.text.toString().trim().length > 0) {
                    holder.saveBT.visibility = View.VISIBLE
                    val totPrice =
                        holder.qtyET.text.toString().trim().toFloat() * holder.rateET.text.toString().trim().toFloat()
                    holder.priceTV.text = totPrice.toString()

                    openOrder[position].order_modified = true
                    openOrder[position].order_modification_saved = "false"
                }
            }
        })

        holder.saveBT.setOnClickListener {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnected == true
            if (!isConnected) {
                Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show()
            } else {
                if (holder.qtyET.text.toString().trim().length > 0 && holder.rateET.text.toString().trim().length > 0) {
                    holder.saveBT.visibility = View.GONE
                    holder.progressLayout.visibility = View.VISIBLE
                    val jobj = JSONObject()
                    jobj.put("order_id", openOrder[position].order_id)
                    jobj.put("qty", holder.qtyET.text.toString().trim())
                    jobj.put("rate", holder.rateET.text.toString().trim())
                    jobj.put("price", holder.priceTV.text.toString().trim())
                    jobj.put("unit", openOrder[position].unit)
//                repo!!.updateOrderDetailsFromSupplier(jobj.toString())

                    Utils.getRetrofit().updateOrderDetailsFromSupplier(jobj.toString())
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                t.printStackTrace()
                            }

                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                if (response.isSuccessful) {
                                    holder.progressLayout.visibility = View.GONE
                                    val responseString = response.body()!!.string()
                                    Log.e("TAG", "Update Order Details From Supplier Response : $responseString")
                                    val jsonObject = JSONObject(responseString)
                                    if (jsonObject.getString("status") == "ok") {
                                        Toast.makeText(context, "Price Updated Successfully!", Toast.LENGTH_SHORT)
                                            .show()
                                        holder.saveBT.visibility = View.GONE

                                        openOrder[position].order_modification_saved = "true"
                                        openOrder[position].qty = holder.qtyET.text.toString().trim()
                                        openOrder[position].rate = holder.rateET.text.toString().trim()
                                        openOrder[position].price = holder.priceTV.text.toString().trim()
                                        orderModified()
                                    } else if (jsonObject.getString("status") == "error") {
                                        Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                        holder.saveBT.visibility = View.VISIBLE
                                    } else if (jsonObject.getString("status") == "price_mismatch") {
                                        Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                        holder.saveBT.visibility = View.VISIBLE
                                    }
                                }
                            }
                        })
                } else {
                    Toast.makeText(context, "Please Enter Valid Data!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val productNameTV = view.productNameTV!!
        val localNameTV = view.localNameTV!!
        val qtyET = view.qtyET!!
        val unitTV = view.unitTV!!
        val rateET = view.rateET!!
        val priceTV = view.priceTV!!
        val saveBT = view.saveBT!!
        val progressLayout = view.progressLayout!!
    }

    private fun orderModified() {
        val intent = Intent("OrderModified")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}