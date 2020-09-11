package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Utils
import `in`.inferon.msl.lemonor.model.pojo.Address
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.activity.AddAddressActivity
import `in`.inferon.msl.lemonor.view.activity.EditAddressActivity
import `in`.inferon.msl.lemonor.view.activity.UpdateProfileActivity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.addresses_adapter.view.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddressesAdapter(
    private val context: Context,
    private val address: MutableList<Address>,
    private val activity: AppCompatActivity
) : RecyclerView.Adapter<AddressesAdapter.ViewHolder>() {
    private var repo: Repository? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        repo = Repository()
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.addresses_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return address.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.firstNameTV.text = address[position].first_name
        if (address[position].last_name != "") {
            holder.lastNameTV.visibility = View.VISIBLE
            holder.lastNameTV.text = address[position].last_name
        } else {
            holder.lastNameTV.visibility = View.GONE
        }
        holder.phone1TV.text = address[position].phone_no1
        if (address[position].phone_no2 != "") {
            holder.phone2TV.visibility = View.VISIBLE
            holder.phone2TV.text = address[position].phone_no2
        } else {
            holder.phone2TV.visibility = View.GONE
        }
        holder.address1TV.text = address[position].address_line_1
        if (address[position].address_line_2 != "") {
            holder.address2TV.visibility = View.VISIBLE
            holder.address2TV.text = address[position].address_line_2
        } else {
            holder.address2TV.visibility = View.GONE
        }
        if (address[position].landmark != "") {
            holder.landmarkTV.visibility = View.VISIBLE
            holder.landmarkTV.text = address[position].landmark
        } else {
            holder.landmarkTV.visibility = View.GONE
        }
        holder.pincodeTV.text = address[position].zip_code
        holder.cityTV.text = address[position].city
        holder.districtTV.text = address[position].district
        holder.stateTV.text = address[position].state
        holder.countryTV.text = address[position].country
        if (address[position].default == "1") {
            holder.defaultAddressTV.visibility = View.VISIBLE
        } else {
            holder.defaultAddressTV.visibility = View.GONE
        }

        holder.deleteBT.setOnClickListener {

            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.delete_address_dialog)

            val cancelBT = dialog.findViewById(R.id.cancelBT) as Button
            val okBT = dialog.findViewById(R.id.okBT) as Button

            cancelBT.setOnClickListener {
                dialog.dismiss()
            }

            okBT.setOnClickListener {
                dialog.dismiss()
                showProgressBar()
                val obj = JSONObject()
                obj.put("address_id", address[position].id)

                Utils.getRetrofit().deleteAddress(obj.toString()).enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        t.printStackTrace()
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            hideProgressBar()
                            val responseString = response.body()!!.string()
                            val jsonObject = JSONObject(responseString)
                            if (jsonObject.getString("status") == "ok") {
                                address.removeAt(position)
                                notifyDataSetChanged()
                            } else if (jsonObject.getString("status") == "error") {
                                Toast.makeText(context, "Something went Wrong!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            }
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            val window = dialog.window!!
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        holder.editBT.setOnClickListener {
            val intent = Intent(context, EditAddressActivity::class.java)
            intent.putExtra("address", Gson().toJson(address[position]))
            context.startActivity(intent)
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val firstNameTV = view.firstNameTV!!
        val lastNameTV = view.lastNameTV!!
        val phone1TV = view.phone1TV!!
        val phone2TV = view.phone2TV!!
        val address1TV = view.address1TV!!
        val address2TV = view.address2TV!!
        val landmarkTV = view.landmarkTV!!
        val pincodeTV = view.pincodeTV!!
        val cityTV = view.cityTV!!
        val districtTV = view.districtTV!!
        val stateTV = view.stateTV!!
        val countryTV = view.countryTV!!
        val defaultAddressTV = view.defaultAddressTV!!
        val deleteBT = view.deleteBT!!
        val editBT = view.editBT!!
    }

    private fun showProgressBar() {
        val intent = Intent("ShowProgressBar")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun hideProgressBar() {
        val intent = Intent("HideProgressBar")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}