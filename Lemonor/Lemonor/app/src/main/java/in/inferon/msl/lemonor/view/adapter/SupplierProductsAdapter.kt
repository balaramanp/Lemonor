package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.Utils
import `in`.inferon.msl.lemonor.model.pojo.Category
import `in`.inferon.msl.lemonor.model.pojo.Products
import `in`.inferon.msl.lemonor.model.pojo.Unit
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_view_supplier_product.*
import kotlinx.android.synthetic.main.supplier_products_adapter.view.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SupplierProductsAdapter(
    private val context: Context,
    private val productsList: MutableList<Products>,
    private val categoryList: MutableList<Category>,
    private val unitList: MutableList<Unit>,
    private val activity: AppCompatActivity
) : RecyclerView.Adapter<SupplierProductsAdapter.ViewHolder>() {
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    private var categoryTV: TextView? = null
    private lateinit var checkBox: CheckBox
    private lateinit var categoryNameET: EditText
    private var unitTV: TextView? = null
    private var unitCheckBox: CheckBox? = null
    private var unitNameET: EditText? = null
    private var holder: ViewHolder? = null
    private var position = -1
    private lateinit var categoryDialog: Dialog
    private lateinit var unitDialog: Dialog

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(selectedCategory, IntentFilter("SelectedCategory"))

        LocalBroadcastManager.getInstance(context)
            .registerReceiver(selectedUnit, IntentFilter("SelectedUnit"))
        shared = context.getSharedPreferences(PREF, AppCompatActivity.MODE_PRIVATE)


        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.supplier_products_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return productsList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        this.holder = holder

        holder.productNameTV.text = productsList[position].name
        if (productsList[position].local_name.trim().length > 0) {
            holder.productAliasNameTV.visibility = View.VISIBLE
            holder.productAliasNameTV.text = productsList[position].local_name
        } else {
            holder.productAliasNameTV.visibility = View.GONE
        }
        holder.priceTV.text = productsList[position].rate
        holder.unitTV.text = productsList[position].unit
        holder.descriptionTV.text = productsList[position].description

        if (productsList[position].stock_status == "1") {
            holder.productSwitch.isChecked = true
            holder.productStatusTV.text = "Disable"
        } else {
            holder.productSwitch.isChecked = false
            holder.productStatusTV.text = "Enable"
        }

        holder.switchLayout.setOnClickListener {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnected == true
            if (!isConnected) {
                Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show()
            } else {
                this.position = position
                if (!holder.productSwitch.isChecked) {
                    val obj = JSONObject()
                    obj.put("supplier_product_id", productsList[position].id)
//                    Constants.repository!!.enableSupplierProduct(obj.toString())

                    Utils.getRetrofit().enableSupplierProduct(obj.toString()).enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            t.printStackTrace()
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.isSuccessful) {
                                val responseString = response.body()!!.string()
                                Log.e("TAG", "Enable Supplier Product Response : $responseString")

                                val decodedJSONObject = JSONObject(responseString)
                                if (decodedJSONObject.getString("status") == "ok") {
//                                    progressLayout.visibility = View.VISIBLE
                                    productsList[position].stock_status = "1"
                                    holder.productSwitch.isChecked = true

                                    val toastTxt = if (productsList[position].local_name != "") {
                                        "${productsList[position].name} ( ${productsList[position].local_name} ) is Available Now!"
                                    } else {
                                        "${productsList[position].name} is Available Now!"
                                    }
                                    Toast.makeText(context, toastTxt, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    })
                } else {
                    val obj = JSONObject()
                    obj.put("supplier_product_id", productsList[position].id)
//                    Constants.repository!!.disableSupplierProduct(obj.toString())

                    Utils.getRetrofit().disableSupplierProduct(obj.toString()).enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            t.printStackTrace()
                        }

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.isSuccessful) {
                                val responseString = response.body()!!.string()
                                Log.e("TAG", "Disable Supplier Product Response : $responseString")

                                val decodedJSONObject = JSONObject(responseString)
                                if (decodedJSONObject.getString("status") == "ok") {
//                                    progressLayout.visibility = View.VISIBLE
                                    productsList[position].stock_status = "0"
                                    holder.productSwitch.isChecked = false

                                    val toastTxt = if (productsList[position].local_name != "") {
                                        "${productsList[position].name} ( ${productsList[position].local_name} ) is Unavailable / Out of Stock"
                                    } else {
                                        "${productsList[position].name} is Unavailable / Out of Stock"
                                    }
                                    Toast.makeText(context, toastTxt, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    })
                }
            }

        }

        holder.layout.setOnClickListener {
            val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.card_click_anim)
            holder.layout.startAnimation(animation)

            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    val dialog = Dialog(context)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setContentView(R.layout.edit_product_dialog)

                    val productNameET = dialog.findViewById(R.id.productNameET) as EditText
                    val aliasNameET = dialog.findViewById(R.id.aliasNameET) as EditText
                    categoryTV = dialog.findViewById(R.id.categoryTV) as TextView
                    unitTV = dialog.findViewById(R.id.unitTV) as TextView
                    val priceET = dialog.findViewById(R.id.priceET) as EditText
                    val descriptionET = dialog.findViewById(R.id.descriptionET) as EditText
                    val featuredProduct = dialog.findViewById(R.id.featuredProduct) as CheckBox
                    val updateTV = dialog.findViewById(R.id.updateTV) as TextView

                    productNameET.setText(productsList[position].name)
                    aliasNameET.setText(productsList[position].local_name)
                    categoryTV!!.text = productsList[position].category
                    unitTV!!.text = productsList[position].unit
                    priceET.setText(productsList[position].rate)
                    descriptionET.setText(productsList[position].description)
                    featuredProduct.isChecked = productsList[position].featured_product_flag

                    categoryTV!!.setOnClickListener {
                        categoryDialog = Dialog(context)
                        categoryDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        categoryDialog.setContentView(R.layout.categories_dialog)
                        checkBox = categoryDialog.findViewById(R.id.checkBox) as CheckBox
                        categoryNameET = categoryDialog.findViewById(R.id.categoryNameET) as EditText
                        val categoryRV = categoryDialog.findViewById(R.id.categoryRV) as RecyclerView
                        val doneTV = categoryDialog.findViewById(R.id.doneTV) as TextView

                        categoryRV.layoutManager = LinearLayoutManager(context)
                        var categoriesAdapter = CategoriesAdapter(context, categoryList)
                        categoryRV.adapter = categoriesAdapter
                        categoryRV.setHasFixedSize(true)
                        categoryRV.isNestedScrollingEnabled = false

                        checkBox.setOnClickListener {
                            if (checkBox.isChecked) {
                                categoryNameET.visibility = View.VISIBLE
                                doneTV.visibility = View.VISIBLE
                            } else {
                                categoryNameET.visibility = View.GONE
                                doneTV.visibility = View.GONE
                            }

                            categoriesAdapter = CategoriesAdapter(context, categoryList)
                            categoryRV.adapter = categoriesAdapter
                            categoryRV.setHasFixedSize(true)
                            categoryRV.isNestedScrollingEnabled = false
                        }

                        doneTV.setOnClickListener {
                            if (checkBox.isChecked && categoryNameET.text.isNotEmpty()) {
                                categoryTV!!.text = categoryNameET.text.toString().trim()
                                categoryNameET.setText("")
                            }
                            categoryDialog.dismiss()
                        }

                        categoryDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        categoryDialog.setCanceledOnTouchOutside(false)
                        categoryDialog.show()
                        val window = categoryDialog.window!!
                        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    }

                    unitTV!!.setOnClickListener {
                        unitDialog = Dialog(context)
                        unitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        unitDialog.setContentView(R.layout.units_dialog)

                        unitCheckBox = unitDialog.findViewById(R.id.unitCheckBox) as CheckBox
                        unitNameET = unitDialog.findViewById(R.id.unitNameET) as EditText
                        val unitsRV = unitDialog.findViewById(R.id.unitsRV) as RecyclerView
                        val doneTV = unitDialog.findViewById(R.id.doneTV) as TextView

                        unitsRV.layoutManager = LinearLayoutManager(context)
                        var unitsAdapter = UnitsAdapter(context, unitList, "activity", "not_custom")
                        unitsRV.adapter = unitsAdapter
                        unitsRV.setHasFixedSize(true)
                        unitsRV.isNestedScrollingEnabled = false


                        unitCheckBox!!.setOnClickListener {
                            if (unitCheckBox!!.isChecked) {
                                unitNameET!!.visibility = View.VISIBLE
                                doneTV.visibility = View.VISIBLE
                            } else {
                                unitNameET!!.visibility = View.GONE
                                doneTV.visibility = View.GONE
                            }
                            unitsAdapter = UnitsAdapter(context, unitList, "activity", "custom")
                            unitsRV.adapter = unitsAdapter
                            unitsRV.setHasFixedSize(true)
                            unitsRV.isNestedScrollingEnabled = false
                        }

                        doneTV.setOnClickListener {
                            if (unitCheckBox!!.isChecked && unitNameET!!.text.isNotEmpty()) {
                                unitTV!!.text = unitNameET!!.text.toString().trim()
                                unitNameET!!.setText("")
                            }
                            unitDialog.dismiss()
                        }

                        unitDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        unitDialog.setCanceledOnTouchOutside(false)
                        unitDialog.show()
                        val window = unitDialog.window!!
                        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    }

                    updateTV.setOnClickListener {
                        val connectivityManager =
                            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                        val isConnected: Boolean = activeNetwork?.isConnected == true
                        if (!isConnected) {
                            Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                        } else {
                            if (productNameET.text.isNotEmpty() && priceET.text.isNotEmpty()) {
                                val obj = JSONObject()
                                obj.put("product_id", productsList[position].product_id)
                                obj.put("supplier_id", shared!!.getString("id", ""))
                                obj.put("product_name", productNameET.text.toString().trim())
                                obj.put("local_name", aliasNameET.text.toString().trim())
                                obj.put("unit", unitTV!!.text.toString().trim())
                                obj.put("description", descriptionET.text.toString().trim())
                                obj.put("category", categoryTV!!.text.toString().trim())
                                obj.put("rate", priceET.text.toString().trim())
                                if (featuredProduct.isChecked) {
                                    obj.put("featured_product_flag", true)
                                } else {
                                    obj.put("featured_product_flag", false)
                                }
//                        Constants.repository!!.saveSupplierItem(obj.toString())
                                dialog.dismiss()

                                Utils.getRetrofit().saveSupplierItem(obj.toString())
                                    .enqueue(object : Callback<ResponseBody> {
                                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                            t.printStackTrace()
                                        }

                                        override fun onResponse(
                                            call: Call<ResponseBody>,
                                            response: Response<ResponseBody>
                                        ) {
                                            if (response.isSuccessful) {
                                                val responseString = response.body()!!.string()
                                                Log.e("TAG", "Save Supplier Item Response : $responseString")

                                                val decodedJSONObject = JSONObject(responseString)
                                                if (decodedJSONObject.getString("status") == "ok") {
//                                        progressLayout.visibility = View.VISIBLE

                                                    productsList[position].name = productNameET.text.toString().trim()
                                                    productsList[position].local_name =
                                                        aliasNameET.text.toString().trim()
                                                    productsList[position].unit = unitTV!!.text.toString().trim()
                                                    productsList[position].description =
                                                        descriptionET.text.toString().trim()
                                                    productsList[position].category =
                                                        categoryTV!!.text.toString().trim()
                                                    productsList[position].rate = priceET.text.toString().trim()
                                                    Toast.makeText(
                                                        context,
                                                        "Product Updated Successfully!",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                        .show()
                                                    notifyDataSetChanged()
                                                } else if (decodedJSONObject.getString("status") == "error") {
                                                    Toast.makeText(
                                                        context,
                                                        decodedJSONObject.getString("msg"),
                                                        Toast.LENGTH_SHORT
                                                    )
                                                        .show()
                                                }
                                            }
                                        }
                                    })
                            } else {
                                Toast.makeText(context, "Please Enter All Fields!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.show()
                    val window = dialog.window!!
                    window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })
        }


    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val productNameTV = view.productNameTV!!
        val productAliasNameTV = view.productAliasNameTV!!
        val priceTV = view.priceTV!!
        val unitTV = view.unitTV!!
        val descriptionTV = view.descriptionTV!!
        val productSwitch = view.productSwitch!!
        val productStatusTV = view.productStatusTV!!
        val layout = view.layout!!
        val switchLayout = view.switchLayout!!
    }

    private val selectedCategory = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                checkBox.isChecked = false
                categoryNameET.setText("")
                categoryNameET.visibility = View.GONE

                categoryTV!!.text = intent.getStringExtra("selectedCategory")
                categoryDialog.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val selectedUnit = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                unitCheckBox!!.isChecked = false
                unitNameET!!.setText("")
                unitNameET!!.visibility = View.GONE

                unitTV!!.text = intent.getStringExtra("selectedUnit")
                unitDialog.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}