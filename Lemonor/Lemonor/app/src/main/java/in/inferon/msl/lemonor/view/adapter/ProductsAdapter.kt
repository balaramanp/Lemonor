package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Utils
import `in`.inferon.msl.lemonor.model.pojo.Products
import `in`.inferon.msl.lemonor.model.pojo.Unit
import `in`.inferon.msl.lemonor.repo.Repository
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.products_adapter.view.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class ProductsAdapter(
    private val context: Context,
    private val goodsList: MutableList<Products>,
    private val unitList: MutableList<Unit>,
    private val activity: AppCompatActivity
) : RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {
    private var repo: Repository? = null
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    private var unitCheckBox: CheckBox? = null
    private var unitNameET: EditText? = null
    private lateinit var unitDialog: Dialog
    private var selectedUnitTV: TextView? = null
    private var pos = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        repo = Repository()
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(selectedAdapterUnit, IntentFilter("SelectedAdapterUnit"))
        shared = context.getSharedPreferences(PREF, AppCompatActivity.MODE_PRIVATE)
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.products_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return goodsList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.productNameTV.text = goodsList[position].product_name
        if (goodsList[position].local_name != "") {
            holder.productAliasNameTV.text = goodsList[position].local_name
        } else {
            holder.productAliasNameTV.visibility = View.GONE
        }
        holder.categoryTV.text = goodsList[position].category
        holder.expandLayout.visibility = View.GONE

        if (goodsList[position].new_product || goodsList[position].is_supplier_product) {
            holder.unitTV.text = goodsList[position].unit
            holder.priceET.setText(goodsList[position].rate)
            holder.descriptionET.setText(goodsList[position].description)
            holder.featuredProduct.isChecked = goodsList[position].featured_product_flag

            holder.addedIV.visibility = View.VISIBLE
            holder.addIV.visibility = View.GONE
            holder.addProductBT.visibility = View.GONE
            holder.expandLayout.visibility = View.GONE
        } else {
            holder.unitTV.text = "Kg"
            holder.priceET.setText("")
            holder.descriptionET.setText("")
            holder.featuredProduct.isChecked = false

            holder.addedIV.visibility = View.GONE
            holder.addIV.visibility = View.VISIBLE
            holder.addProductBT.visibility = View.VISIBLE
            holder.expandLayout.visibility = View.GONE
        }

        if (goodsList[position].rate != null && goodsList[position].rate != "" ||
            goodsList[position].unit_value_changed || goodsList[position].description != null
            && goodsList[position].description != "" || goodsList[position].featured_product_flag
        ) {
            holder.expandLayout.visibility = View.VISIBLE
            holder.unitTV.text = goodsList[position].unit
            holder.priceET.setText(goodsList[position].rate)
            holder.descriptionET.setText(goodsList[position].description)
            holder.featuredProduct.isChecked = goodsList[position].featured_product_flag

        } else {
            holder.expandLayout.visibility = View.GONE
            holder.unitTV.text = "Kg"
            holder.priceET.setText("")
            holder.descriptionET.setText("")
            holder.featuredProduct.isChecked = false
        }

        holder.layout.setOnClickListener {
            if (holder.expandLayout.visibility == View.VISIBLE) {
                holder.expandLayout.visibility = View.GONE
            } else {
                holder.expandLayout.visibility = View.VISIBLE
            }
        }

        holder.unitTV.setOnClickListener {
            pos = position
            unitDialog = Dialog(context)
            unitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            unitDialog.setContentView(R.layout.units_dialog)

            unitCheckBox = unitDialog.findViewById(R.id.unitCheckBox) as CheckBox
            unitNameET = unitDialog.findViewById(R.id.unitNameET) as EditText
            selectedUnitTV = unitDialog.findViewById(R.id.selectedUnitTV) as TextView
            val unitsRV = unitDialog.findViewById(R.id.unitsRV) as RecyclerView
            val doneTV = unitDialog.findViewById(R.id.doneTV) as TextView

            Log.e("TAG", "Received Unit List : $unitList")
            unitsRV.layoutManager = LinearLayoutManager(context)
            var unitsAdapter = UnitsAdapter(context, unitList, "adapter", "not_custom")
            unitsRV.adapter = unitsAdapter
            unitsRV.setHasFixedSize(true)
            unitsRV.isNestedScrollingEnabled = false

            selectedUnitTV!!.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    holder.unitTV.text = selectedUnitTV!!.text.toString().trim()
                    goodsList[position].unit = selectedUnitTV!!.text.toString().trim()
                    goodsList[position].unit_value_changed = true
                    holder.addProductBT.visibility = View.VISIBLE
                }
            })

            unitCheckBox!!.setOnClickListener {
                if (unitCheckBox!!.isChecked) {
                    unitNameET!!.visibility = View.VISIBLE
                    doneTV.visibility = View.VISIBLE
                } else {
                    unitNameET!!.visibility = View.GONE
                    doneTV.visibility = View.GONE
                }
                unitsAdapter = UnitsAdapter(context, unitList, "adapter", "custom")
                unitsRV.adapter = unitsAdapter
                unitsRV.setHasFixedSize(true)
                unitsRV.isNestedScrollingEnabled = false
            }

            doneTV.setOnClickListener {
                if (unitCheckBox!!.isChecked && unitNameET!!.text.isNotEmpty()) {
                    holder.unitTV.text = unitNameET!!.text.toString().trim()
                    goodsList[position].unit = unitNameET!!.text.toString().trim()
                    goodsList[position].unit_value_changed = true
                    unitNameET!!.setText("")
                }
                holder.addProductBT.visibility = View.VISIBLE
                unitDialog.dismiss()
            }

            unitDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            unitDialog.setCanceledOnTouchOutside(false)
            unitDialog.show()
            val window = unitDialog.window!!
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        holder.addProductBT.setOnClickListener {
            if (holder.unitTV.text!!.isNotEmpty() && holder.priceET.text!!.isNotEmpty()) {
                holder.addProductBT.isClickable = false
                val obj = JSONObject()
                obj.put("product_id", goodsList[position].id)
                obj.put("product_name", goodsList[position].product_name)
                obj.put("local_name", goodsList[position].local_name)
                obj.put("unit", holder.unitTV.text.toString().trim())
                obj.put("description", holder.descriptionET.text.toString().trim())
                obj.put("category", goodsList[position].category)
                obj.put("rate", holder.priceET.text.toString().trim())
                obj.put("new_product", false)
                if (holder.featuredProduct.isChecked) {
                    obj.put("featured_product_flag", true)
                } else {
                    obj.put("featured_product_flag", false)
                }

                val fObj = JSONObject()
                fObj.put("user_id", shared!!.getString("id", ""))
                fObj.put("goods_data", obj)

                Utils.getRetrofit().saveSupplierGoodsData(fObj.toString()).enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        t.printStackTrace()
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val jsonObject = JSONObject(response.body()!!.string())
                            if (jsonObject.getString("status") == "ok") {
                                goodsList[position].unit = holder.unitTV.text.toString().trim()
                                goodsList[position].rate = holder.priceET.text.toString().trim()
                                goodsList[position].description = holder.descriptionET.text.toString().trim()
                                goodsList[position].is_supplier_product = true

                                holder.addedIV.visibility = View.VISIBLE
                                holder.addIV.visibility = View.GONE
                                holder.expandLayout.visibility = View.GONE
                                holder.addProductBT.visibility = View.GONE
                                holder.addProductBT.isClickable = true
                                Toast.makeText(context, "Product Added Successfully!", Toast.LENGTH_SHORT).show()
                            } else if (jsonObject.getString("status") == "error") {
                                Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                holder.addProductBT.isClickable = true
                            } else if (jsonObject.getString("status") == "already_exists") {
                                Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                holder.addProductBT.isClickable = true
                            }
                        }
                    }
                })
            } else {
                Toast.makeText(context, "Please Enter All Fields!", Toast.LENGTH_SHORT).show()
            }
        }

        holder.priceET.setOnKeyListener { view, i, keyEvent ->
            Log.e("TAG", "Price key event called")
            goodsList[position].rate = holder.priceET.text.toString().trim()
            goodsList[position].description = holder.descriptionET.text.toString().trim()
            holder.addProductBT.visibility = View.VISIBLE
            false
        }

        holder.descriptionET.setOnKeyListener { view, i, keyEvent ->
            Log.e("TAG", "Price Description key event called")
            goodsList[position].description = holder.descriptionET.text.toString().trim()
            holder.addProductBT.visibility = View.VISIBLE
            false
        }

        holder.featuredProduct.setOnClickListener {
            goodsList[position].featured_product_flag = holder.featuredProduct.isChecked
            goodsList[position].description = holder.descriptionET.text.toString().trim()
        }


        holder.addIV.setOnClickListener {
            if (holder.expandLayout.visibility == View.VISIBLE) {
                holder.expandLayout.visibility = View.GONE
            } else {
                holder.expandLayout.visibility = View.VISIBLE
            }
        }

        holder.addedIV.setOnClickListener {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.product_remove_dialog)

            val cancelBT = dialog.findViewById(R.id.cancelBT) as Button
            val okBT = dialog.findViewById(R.id.okBT) as Button

            cancelBT.setOnClickListener {
                dialog.dismiss()
            }

            okBT.setOnClickListener {
                val obj = JSONObject()
                obj.put("supplier_id", shared!!.getString("id", ""))
                obj.put("product_id", goodsList[position].id)
//                repo!!.removeSupplierProduct(obj.toString())

                Utils.getRetrofit().removeSupplierProduct(obj.toString()).enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        t.printStackTrace()
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val responseString = response.body()!!.string()
                            val decodedJSONObject = JSONObject(responseString)
                            if (decodedJSONObject.getString("status") == "ok") {
                                holder.addedIV.visibility = View.GONE
                                holder.addIV.visibility = View.VISIBLE
                                goodsList[position].unit = ""
                                goodsList[position].rate = ""
                                goodsList[position].description = ""
                                goodsList[position].is_supplier_product = false
                                goodsList[position].featured_product_flag = false

                                holder.unitTV.text = "Kg"
                                holder.priceET.setText("")
                                holder.descriptionET.setText("")
                                holder.featuredProduct.isChecked = false
                            } else if (decodedJSONObject.getString("status") == "error") {
                                Toast.makeText(context, decodedJSONObject.getString("msg"), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })

                dialog.dismiss()
            }
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            val window = dialog.window!!
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val layout = view.layout!!
        val productNameTV = view.productNameTV!!
        val productAliasNameTV = view.productAliasNameTV!!
        val categoryTV = view.categoryTV!!
        val expandLayout = view.expandLayout!!
        val unitTV = view.unitTV!!
        val priceET = view.priceET!!
        val descriptionET = view.descriptionET!!
        val featuredProduct = view.featuredProduct!!
        val addProductBT = view.addProductBT!!
        val addedIV = view.addedIV!!
        val addIV = view.addIV!!
    }

    private val selectedAdapterUnit = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                unitCheckBox!!.isChecked = false
                unitNameET!!.setText("")
                unitNameET!!.visibility = View.GONE
                selectedUnitTV!!.text = intent.getStringExtra("selectedUnit")
                unitDialog.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}