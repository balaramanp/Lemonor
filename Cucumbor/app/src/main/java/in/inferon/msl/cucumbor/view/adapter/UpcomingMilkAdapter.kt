package `in`.inferon.msl.cucumbor.view.adapter

import `in`.inferon.msl.cucumbor.R
import `in`.inferon.msl.cucumbor.model.pojo.UpcomingMilk
import `in`.inferon.msl.cucumbor.repo.Repository
import `in`.inferon.msl.cucumbor.view.activity.MainActivity
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.upcoming_milk_adapter.view.*
import org.json.JSONObject
import java.text.SimpleDateFormat


class UpcomingMilkAdapter(
    private val context: Context,
    private val upcomingMilk: List<UpcomingMilk>,
    private val mainActivity: MainActivity
) : RecyclerView.Adapter<UpcomingMilkAdapter.ViewHolder>() {
    private var repo: Repository? = null
    private var holder: ViewHolder? = null
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        repo = Repository()
        shared = context.getSharedPreferences(PREF, AppCompatActivity.MODE_PRIVATE)
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.upcoming_milk_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return upcomingMilk.size
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date = SimpleDateFormat("dd/MM/yyyy").parse(upcomingMilk[position].date)
        val sdf = SimpleDateFormat("dd MMM")
        holder.dateTV.text = sdf.format(date)

        if (upcomingMilk[position].status == "skip") {
            holder.amountTV.text = "0" + "  ( " + "0" + " Ltr )"
        } else {
            holder.amountTV.text =
                upcomingMilk[position].qty + " Ltr  ( " + context.getString(R.string.Rs) + " " + upcomingMilk[position].price + " )"
        }

        if (upcomingMilk[position].noon == "AM") {
            holder.deliverByTV.text = "Delivered by 6am"
        } else {
            holder.deliverByTV.text = "Delivered by 6pm"
        }

        if (upcomingMilk[position].edit == "1") {
            holder.modifyTV.visibility = View.VISIBLE
        } else {
            holder.modifyTV.visibility = View.INVISIBLE
        }

        holder.modifyTV.setOnClickListener {
            showMilkModifyDialog(position)
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val dateTV = view.dateTV!!
        val amountTV = view.amountTV!!
        val deliverByTV = view.deliverByTV!!
        val modifyTV = view.modifyTV!!
    }

    private fun showMilkModifyDialog(position: Int) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.milk_modify_dialog)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val qtyMinusTV = dialog.findViewById(R.id.qtyMinusTV) as TextView
        val qty_et = dialog.findViewById(R.id.qty_et) as EditText
        val qtyPlusTV = dialog.findViewById(R.id.qtyPlusTV) as TextView
        val quaterET = dialog.findViewById(R.id.quaterET) as EditText
        val halfET = dialog.findViewById(R.id.halfET) as EditText
        val fullET = dialog.findViewById(R.id.fullET) as EditText
        val skipSwitch = dialog.findViewById(R.id.skipSwitch) as Switch
        val loadingLayout = dialog.findViewById(R.id.loadingLayout) as LinearLayout
        val cancel_bt = dialog.findViewById(R.id.cancel_bt) as Button
        val done_bt = dialog.findViewById(R.id.done_bt) as Button

        qty_et.setText(upcomingMilk[position].qty)
        quaterET.setText(upcomingMilk[position].quarter)
        halfET.setText(upcomingMilk[position].half)
        fullET.setText(upcomingMilk[position].one)

        qtyMinusTV.setOnClickListener {
            if (qty_et.text.toString().trim().toFloat() > 0) {
                val q = qty_et.text.toString().trim().toFloat() - 1
                qty_et.setText(q.toString())
            }
        }
        qtyPlusTV.setOnClickListener {
            if (qty_et.text.toString().trim().toFloat() >= 0) {
                val q = qty_et.text.toString().trim().toFloat() + 1
                qty_et.setText(q.toString())
            }
        }
        var skipClicked = false
        skipSwitch.setOnCheckedChangeListener { compoundButton, b ->
            skipClicked = b
        }
        cancel_bt.setOnClickListener {
            dialog.dismiss()
        }
        done_bt.setOnClickListener {
            loadingLayout.visibility = View.VISIBLE
            val obj = JSONObject()
            obj.put("client_app_update_milk_to_projection", "")
            obj.put("user_id", shared!!.getString("id", ""))
            obj.put("date", upcomingMilk[position].date)
            obj.put("noon", upcomingMilk[position].noon)
            obj.put("milk_qty", qty_et.text.toString())
            if (skipClicked) {
                obj.put("milk_status", "skip")
            } else {
                obj.put("milk_status", "start")
            }
            obj.put("packet_quarter", quaterET.text.toString())
            obj.put("packet_half", halfET.text.toString())
            obj.put("packet_one", fullET.text.toString())
            repo!!.updateMilkToProjection(obj.toString())

            repo!!.updateMilkToProjection.observe(mainActivity, androidx.lifecycle.Observer {
                run {
                    if (it.isNotEmpty()) {
                        val jobj = JSONObject(it)
                        if (jobj.getString("status") == "ok") {
                            sendMilkModification()
                            loadingLayout.visibility = View.GONE
                            dialog.dismiss()
                            Toast.makeText(context, "Milk Modification Done!", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("TAG", "Something Went Wrong!")
                        }
                    } else {
                        Log.e("TAG", "Get Milk Sales List Response is Empty")
                    }
                }
            })
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
        val window = dialog.window
        window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun sendMilkModification() {
        val intent = Intent("MilkModification")
        intent.putExtra("milkModification", "done")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}