package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.pojo.Address
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.addresses_selection_adapter.view.*

class AddressesSelectionAdapter(
    private val context: Context,
    private val address: MutableList<Address>,
    private val activity: AppCompatActivity
) : RecyclerView.Adapter<AddressesSelectionAdapter.ViewHolder>() {
    private var pos = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.addresses_selection_adapter, parent, false))
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

        if (pos == position) {
            holder.checkBox.isChecked = true
            holder.cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.lightCP))
        } else {
            holder.checkBox.isChecked = false
            holder.cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        }

        holder.cardView.setOnClickListener {
            pos = position
            selectedAddress(position)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val cardView = view.cardView!!
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
        val checkBox = view.checkBox!!
    }

    private fun selectedAddress(position: Int) {
        val intent = Intent("SelectedAddress")
        intent.putExtra("position", position)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}