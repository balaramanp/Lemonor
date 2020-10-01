package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.Utils
import `in`.inferon.msl.lemonor.model.pojo.Products
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.adapter.SupplierProductsAdapter
import `in`.inferon.msl.lemonor.view.adapter.UPSelectCategoriesAdapter
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_update_product.*
import kotlinx.android.synthetic.main.activity_update_product.categoryNameTV
import kotlinx.android.synthetic.main.activity_update_product.progressLayout
import org.json.JSONObject
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_order_confirmation.*
import kotlinx.android.synthetic.main.activity_place_order.*
import kotlinx.android.synthetic.main.activity_update_product.backIB
import kotlinx.android.synthetic.main.activity_view_supplier_product.*
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class UpdateProductActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = UpdateProductActivity::class.java.simpleName
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    private var productsList = mutableListOf<Products>()
    private var selectedCategoryList = mutableListOf<Products>()
    private var categories = mutableListOf<String>()
    private var totalItemCount = ""
    private var repo: Repository? = null
    private var productPosition = 0
    private var productPriceChanged = false
    private var globalProductPriceChanged = false
    private lateinit var categoryDialog: Dialog
    private val confirmArray = JSONArray()
    private var backPressClicked = false
    private var updatePriceFirstTime = true
    private var currentPage = 0
    private var isFirstTimeData = true
    private var unSavedItems = false
    private var categoryChangeFirstTime = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_product)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(selectedProductUpdate, IntentFilter("SelectedProductUpdate"))

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(priceUpdateChange, IntentFilter("PriceUpdateChange"))

        repo = Repository()
        shared = getSharedPreferences(PREF, MODE_PRIVATE)
        layout.visibility = View.INVISIBLE

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        Log.e(TAG, "Network Connectivity : $isConnected")
        if (!isConnected) {
            val intent = Intent(this, NoInternetActivity::class.java)
            startActivity(intent)
        } else {
            loadMainAPI()
        }

        repo!!.getProductsBySupplierID.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout!!.visibility = View.GONE
                layout.visibility = View.VISIBLE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    productsList =
                        Gson().fromJson(
                            jsonObject.getString("supplier_products"),
                            object : TypeToken<MutableList<Products>>() {}.type
                        )

                    selectedCategoryList.addAll(productsList)

                    if (isFirstTimeData) {
                        categories =
                            Gson().fromJson(
                                jsonObject.getString("categories"),
                                object : TypeToken<MutableList<String>>() {}.type
                            )
                        categories.reverse()
                        categories.add("All")
                        categories.reverse()
                        categoryNameTV.visibility = View.VISIBLE
                        categoryNameTV.text = categories[0]

                        totalItemCount = jsonObject.getString("total_products_count")
                        totalItemTV.text = totalItemCount

                        isFirstTimeData = false
                    }


                    if (selectedCategoryList.size > 0) {
                        layout.visibility = View.VISIBLE
                        loadCategoryData()
                    } else {
                        layout.visibility = View.GONE
                    }
                }

                if (categoryChangeFirstTime){
                    previousIV.visibility = View.INVISIBLE
                    categoryChangeFirstTime = false
                }
            }
        })

        repo!!.getCategoryFilteredProductsList.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                Log.e(TAG, "Category Filter Product List : $it")
                val jsonObject = JSONObject(it)
                Log.e(TAG, "Category Filter Product List : " + jsonObject.length())
                if (jsonObject.getString("status") == "ok") {
                    productsList =
                        Gson().fromJson(
                            jsonObject.getString("supplier_products_list"),
                            object : TypeToken<MutableList<Products>>() {}.type
                        )
                    totalItemCount = jsonObject.getString("total_products_count")

                    selectedCategoryList.addAll(productsList)

                    if (selectedCategoryList.size > 0) {
                        layout.visibility = View.VISIBLE
                        loadCategoryData()
                    } else {
                        layout.visibility = View.GONE
                    }
                } else if (jsonObject.getString("status") == "error") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                }

                if (categoryChangeFirstTime){
                    previousIV.visibility = View.INVISIBLE
                    categoryChangeFirstTime = false
                }
            }
        })


        repo!!.updateAllSupplierProductRate.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout!!.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {

                    if (backPressClicked) {
                        super.onBackPressed()
                    } else {
                        selectedCategoryList.forEachIndexed { i, products ->
                            if (selectedCategoryList[i].price_changed) {
                                selectedCategoryList[i].rate = selectedCategoryList[i].updated_rate
                                selectedCategoryList[i].updated_rate = ""
                                selectedCategoryList[i].price_changed = false
                            }
                        }
                        productPriceChanged = false
                        globalProductPriceChanged = false
                        showSaveButton()
                        updatePriceFirstTime = true
                    }
                    Toast.makeText(this, "Product Price Updated Successfully!", Toast.LENGTH_SHORT).show()

                } else if (jsonObject.getString("status") == "error") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                } else if (jsonObject.getString("status") == "no_product") {
                    Toast.makeText(this, "No Product Available!", Toast.LENGTH_SHORT).show()
                }
            }
        })

        productNameTV.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (selectedCategoryList[productPosition].name != productNameTV.text.toString().trim()) {
                    Log.e(TAG, "Product Name Changed")
                    productPriceChanged = true
                    globalProductPriceChanged = true
                    showSaveButton()
                }
            }
        })

        productAliasNameTV.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (selectedCategoryList[productPosition].local_name != productAliasNameTV.text.toString().trim()) {
                    Log.e(TAG, "Product Local Name Changed")
                    productPriceChanged = true
                    globalProductPriceChanged = true
                    showSaveButton()
                }
            }
        })

        descriptionTV.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (selectedCategoryList[productPosition].description != descriptionTV.text.toString().trim()) {
                    Log.e(TAG, "Product Description Changed")
                    productPriceChanged = true
                    globalProductPriceChanged = true
                    showSaveButton()
                }
            }
        })


        productMRPTV.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (selectedCategoryList[productPosition].mrp != productMRPTV.text.toString().trim()) {
                    Log.e(TAG, "Product MRP Changed")
                    productPriceChanged = true
                    globalProductPriceChanged = true

                    if (productMRPTV.text.toString().trim() != "" && productMRPTV.text.toString().trim() != "0" && productMRPTV.text.toString().trim().toFloat() > updateProductPriceET.text.toString().trim().toFloat()) {
                        val pre =
                            productMRPTV.text.toString().trim().toFloat() - updateProductPriceET.text.toString().trim().toFloat()
                        val div: Float = (pre / productMRPTV.text.toString().trim().toFloat())
                        val percent: Int = (div * 100).toInt()
                        percentageOffTV.text = "$percent% OFF"
                        mrpTV.text = productMRPTV.text.toString().trim()
                        mrpLayout.visibility = View.VISIBLE
                    } else {
                        mrpLayout.visibility = View.GONE
                    }

                    showSaveButton()
                }
            }
        })

        backIB.setOnClickListener(this)
        categoryNameTV.setOnClickListener(this)
        oneBT.setOnClickListener(this)
        twoBT.setOnClickListener(this)
        threeBT.setOnClickListener(this)
        fourBT.setOnClickListener(this)
        fiveBT.setOnClickListener(this)
        sixBT.setOnClickListener(this)
        sevenBT.setOnClickListener(this)
        eightBT.setOnClickListener(this)
        nineBT.setOnClickListener(this)
        zeroBT.setOnClickListener(this)
        clearBT.setOnClickListener(this)
        dotBT.setOnClickListener(this)
        previousIV.setOnClickListener(this)
        nextIV.setOnClickListener(this)
        saveBT.setOnClickListener(this)
        currentItemLayout.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backIB -> {
                backPress()
            }
            R.id.categoryNameTV -> {
                categoryDialog = Dialog(this)
                categoryDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                categoryDialog.setContentView(R.layout.category_dialog)

                val categoriesRV = categoryDialog.findViewById(R.id.categoriesRV) as RecyclerView
                categoriesRV.layoutManager = LinearLayoutManager(this)
                val upselectCategoryAdapter = UPSelectCategoriesAdapter(this, categories, this)
                categoriesRV.adapter = upselectCategoryAdapter

                categoryDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                categoryDialog.setCanceledOnTouchOutside(false)
                categoryDialog.show()
                val window = categoryDialog.window!!
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            R.id.oneBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "1"
                    priceTV.text = updateProductPriceET.text.toString().trim()
                    updatePriceFirstTime = false
                    productPriceChanged = true
                    globalProductPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 7) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "1"
                        } else {
                            updateProductPriceET.text.toString() + "1"
                        }
                        updateProductPriceET.text = price
                        priceTV.text = updateProductPriceET.text.toString().trim()
                        productPriceChanged = true
                        globalProductPriceChanged = true
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }

                if (productMRPTV.text.toString().trim() != "" && productMRPTV.text.toString().trim() != "0" && productMRPTV.text.toString().trim().toFloat() > updateProductPriceET.text.toString().trim().toFloat()) {
                    val pre =
                        productMRPTV.text.toString().trim().toFloat() - updateProductPriceET.text.toString().trim().toFloat()
                    val div: Float = (pre / productMRPTV.text.toString().trim().toFloat())
                    val percent: Int = (div * 100).toInt()
                    percentageOffTV.text = "$percent% OFF"
                    mrpTV.text = productMRPTV.text.toString().trim()
                    mrpLayout.visibility = View.VISIBLE
                } else {
                    mrpLayout.visibility = View.GONE
                }


                showSaveButton()
            }
            R.id.twoBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "2"
                    priceTV.text = updateProductPriceET.text.toString().trim()
                    updatePriceFirstTime = false
                    productPriceChanged = true
                    globalProductPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 7) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "2"
                        } else {
                            updateProductPriceET.text.toString() + "2"
                        }
                        updateProductPriceET.text = price
                        priceTV.text = updateProductPriceET.text.toString().trim()
                        productPriceChanged = true
                        globalProductPriceChanged = true
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }

                if (productMRPTV.text.toString().trim() != "" && productMRPTV.text.toString().trim() != "0" && productMRPTV.text.toString().trim().toFloat() > updateProductPriceET.text.toString().trim().toFloat()) {
                    val pre =
                        productMRPTV.text.toString().trim().toFloat() - updateProductPriceET.text.toString().trim().toFloat()
                    val div: Float = (pre / productMRPTV.text.toString().trim().toFloat())
                    val percent: Int = (div * 100).toInt()
                    percentageOffTV.text = "$percent% OFF"
                    mrpTV.text = productMRPTV.text.toString().trim()
                    mrpLayout.visibility = View.VISIBLE
                } else {
                    mrpLayout.visibility = View.GONE
                }


                showSaveButton()
            }
            R.id.threeBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "3"
                    priceTV.text = updateProductPriceET.text.toString().trim()
                    updatePriceFirstTime = false
                    productPriceChanged = true
                    globalProductPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 7) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "3"
                        } else {
                            updateProductPriceET.text.toString() + "3"
                        }
                        updateProductPriceET.text = price
                        priceTV.text = updateProductPriceET.text.toString().trim()
                        productPriceChanged = true
                        globalProductPriceChanged = true
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }

                if (productMRPTV.text.toString().trim() != "" && productMRPTV.text.toString().trim() != "0" && productMRPTV.text.toString().trim().toFloat() > updateProductPriceET.text.toString().trim().toFloat()) {
                    val pre =
                        productMRPTV.text.toString().trim().toFloat() - updateProductPriceET.text.toString().trim().toFloat()
                    val div: Float = (pre / productMRPTV.text.toString().trim().toFloat())
                    val percent: Int = (div * 100).toInt()
                    percentageOffTV.text = "$percent% OFF"
                    mrpTV.text = productMRPTV.text.toString().trim()
                    mrpLayout.visibility = View.VISIBLE
                } else {
                    mrpLayout.visibility = View.GONE
                }


                showSaveButton()
            }
            R.id.fourBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "4"
                    priceTV.text = updateProductPriceET.text.toString().trim()
                    updatePriceFirstTime = false
                    productPriceChanged = true
                    globalProductPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 7) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "4"
                        } else {
                            updateProductPriceET.text.toString() + "4"
                        }
                        updateProductPriceET.text = price
                        priceTV.text = updateProductPriceET.text.toString().trim()
                        productPriceChanged = true
                        globalProductPriceChanged = true
                        showSaveButton()
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }

                if (productMRPTV.text.toString().trim() != "" && productMRPTV.text.toString().trim() != "0" && productMRPTV.text.toString().trim().toFloat() > updateProductPriceET.text.toString().trim().toFloat()) {
                    val pre =
                        productMRPTV.text.toString().trim().toFloat() - updateProductPriceET.text.toString().trim().toFloat()
                    val div: Float = (pre / productMRPTV.text.toString().trim().toFloat())
                    val percent: Int = (div * 100).toInt()
                    percentageOffTV.text = "$percent% OFF"
                    mrpTV.text = productMRPTV.text.toString().trim()
                    mrpLayout.visibility = View.VISIBLE
                } else {
                    mrpLayout.visibility = View.GONE
                }


                showSaveButton()
            }
            R.id.fiveBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "5"
                    priceTV.text = updateProductPriceET.text.toString().trim()
                    updatePriceFirstTime = false
                    productPriceChanged = true
                    globalProductPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 7) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "5"
                        } else {
                            updateProductPriceET.text.toString() + "5"
                        }
                        updateProductPriceET.text = price
                        priceTV.text = updateProductPriceET.text.toString().trim()
                        productPriceChanged = true
                        globalProductPriceChanged = true
                        showSaveButton()
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }

                if (productMRPTV.text.toString().trim() != "" && productMRPTV.text.toString().trim() != "0" && productMRPTV.text.toString().trim().toFloat() > updateProductPriceET.text.toString().trim().toFloat()) {
                    val pre =
                        productMRPTV.text.toString().trim().toFloat() - updateProductPriceET.text.toString().trim().toFloat()
                    val div: Float = (pre / productMRPTV.text.toString().trim().toFloat())
                    val percent: Int = (div * 100).toInt()
                    percentageOffTV.text = "$percent% OFF"
                    mrpTV.text = productMRPTV.text.toString().trim()
                    mrpLayout.visibility = View.VISIBLE
                } else {
                    mrpLayout.visibility = View.GONE
                }


                showSaveButton()
            }
            R.id.sixBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "6"
                    priceTV.text = updateProductPriceET.text.toString().trim()
                    updatePriceFirstTime = false
                    productPriceChanged = true
                    globalProductPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 7) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "6"
                        } else {
                            updateProductPriceET.text.toString() + "6"
                        }
                        updateProductPriceET.text = price
                        priceTV.text = updateProductPriceET.text.toString().trim()
                        productPriceChanged = true
                        globalProductPriceChanged = true
                        showSaveButton()
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }

                if (productMRPTV.text.toString().trim() != "" && productMRPTV.text.toString().trim() != "0" && productMRPTV.text.toString().trim().toFloat() > updateProductPriceET.text.toString().trim().toFloat()) {
                    val pre =
                        productMRPTV.text.toString().trim().toFloat() - updateProductPriceET.text.toString().trim().toFloat()
                    val div: Float = (pre / productMRPTV.text.toString().trim().toFloat())
                    val percent: Int = (div * 100).toInt()
                    percentageOffTV.text = "$percent% OFF"
                    mrpTV.text = productMRPTV.text.toString().trim()
                    mrpLayout.visibility = View.VISIBLE
                } else {
                    mrpLayout.visibility = View.GONE
                }


                showSaveButton()
            }
            R.id.sevenBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "7"
                    priceTV.text = updateProductPriceET.text.toString().trim()
                    updatePriceFirstTime = false
                    productPriceChanged = true
                    globalProductPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 7) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "7"
                        } else {
                            updateProductPriceET.text.toString() + "7"
                        }
                        updateProductPriceET.text = price
                        priceTV.text = updateProductPriceET.text.toString().trim()
                        productPriceChanged = true
                        globalProductPriceChanged = true
                        showSaveButton()
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }

                if (productMRPTV.text.toString().trim() != "" && productMRPTV.text.toString().trim() != "0" && productMRPTV.text.toString().trim().toFloat() > updateProductPriceET.text.toString().trim().toFloat()) {
                    val pre =
                        productMRPTV.text.toString().trim().toFloat() - updateProductPriceET.text.toString().trim().toFloat()
                    val div: Float = (pre / productMRPTV.text.toString().trim().toFloat())
                    val percent: Int = (div * 100).toInt()
                    percentageOffTV.text = "$percent% OFF"
                    mrpTV.text = productMRPTV.text.toString().trim()
                    mrpLayout.visibility = View.VISIBLE
                } else {
                    mrpLayout.visibility = View.GONE
                }


                showSaveButton()
            }
            R.id.eightBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "8"
                    priceTV.text = updateProductPriceET.text.toString().trim()
                    updatePriceFirstTime = false
                    productPriceChanged = true
                    globalProductPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 7) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "8"
                        } else {
                            updateProductPriceET.text.toString() + "8"
                        }
                        updateProductPriceET.text = price
                        priceTV.text = updateProductPriceET.text.toString().trim()
                        productPriceChanged = true
                        globalProductPriceChanged = true
                        showSaveButton()
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }

                if (productMRPTV.text.toString().trim() != "" && productMRPTV.text.toString().trim() != "0" && productMRPTV.text.toString().trim().toFloat() > updateProductPriceET.text.toString().trim().toFloat()) {
                    val pre =
                        productMRPTV.text.toString().trim().toFloat() - updateProductPriceET.text.toString().trim().toFloat()
                    val div: Float = (pre / productMRPTV.text.toString().trim().toFloat())
                    val percent: Int = (div * 100).toInt()
                    percentageOffTV.text = "$percent% OFF"
                    mrpTV.text = productMRPTV.text.toString().trim()
                    mrpLayout.visibility = View.VISIBLE
                } else {
                    mrpLayout.visibility = View.GONE
                }


                showSaveButton()
            }
            R.id.nineBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "9"
                    priceTV.text = updateProductPriceET.text.toString().trim()
                    updatePriceFirstTime = false
                    productPriceChanged = true
                    globalProductPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 7) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "9"
                        } else {
                            updateProductPriceET.text.toString() + "9"
                        }
                        updateProductPriceET.text = price
                        priceTV.text = updateProductPriceET.text.toString().trim()
                        productPriceChanged = true
                        globalProductPriceChanged = true
                        showSaveButton()
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }

                if (productMRPTV.text.toString().trim() != "" && productMRPTV.text.toString().trim() != "0" && productMRPTV.text.toString().trim().toFloat() > updateProductPriceET.text.toString().trim().toFloat()) {
                    val pre =
                        productMRPTV.text.toString().trim().toFloat() - updateProductPriceET.text.toString().trim().toFloat()
                    val div: Float = (pre / productMRPTV.text.toString().trim().toFloat())
                    val percent: Int = (div * 100).toInt()
                    percentageOffTV.text = "$percent% OFF"
                    mrpTV.text = productMRPTV.text.toString().trim()
                    mrpLayout.visibility = View.VISIBLE
                } else {
                    mrpLayout.visibility = View.GONE
                }


                showSaveButton()
            }
            R.id.zeroBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "0"
                    priceTV.text = updateProductPriceET.text.toString().trim()
                    updatePriceFirstTime = false
                    productPriceChanged = true
                    globalProductPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 7) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "0"
                        } else {
                            updateProductPriceET.text.toString() + "0"
                        }
                        updateProductPriceET.text = price
                        priceTV.text = updateProductPriceET.text.toString().trim()
                        productPriceChanged = true
                        globalProductPriceChanged = true
                        showSaveButton()
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }

                if (productMRPTV.text.toString().trim() != "" && productMRPTV.text.toString().trim() != "0" && productMRPTV.text.toString().trim().toFloat() > updateProductPriceET.text.toString().trim().toFloat()) {
                    val pre =
                        productMRPTV.text.toString().trim().toFloat() - updateProductPriceET.text.toString().trim().toFloat()
                    val div: Float = (pre / productMRPTV.text.toString().trim().toFloat())
                    val percent: Int = (div * 100).toInt()
                    percentageOffTV.text = "$percent% OFF"
                    mrpTV.text = productMRPTV.text.toString().trim()
                    mrpLayout.visibility = View.VISIBLE
                } else {
                    mrpLayout.visibility = View.GONE
                }


                showSaveButton()
            }
            R.id.clearBT -> {
                if (updateProductPriceET.text.isNotEmpty()) {
                    val price = updateProductPriceET.text.toString()
                    updateProductPriceET.text = price.substring(0, price.length - 1)
                    if (price.length - 1 == 0) {
                        updateProductPriceET.text = "0"
                    }
                    priceTV.text = updateProductPriceET.text.toString().trim()
                    productPriceChanged = true
                    globalProductPriceChanged = true
                    updatePriceFirstTime = false

                    if (productMRPTV.text.toString().trim() != "" && productMRPTV.text.toString().trim() != "0" && productMRPTV.text.toString().trim().toFloat() > updateProductPriceET.text.toString().trim().toFloat()) {
                        val pre =
                            productMRPTV.text.toString().trim().toFloat() - updateProductPriceET.text.toString().trim().toFloat()
                        val div: Float = (pre / productMRPTV.text.toString().trim().toFloat())
                        val percent: Int = (div * 100).toInt()
                        percentageOffTV.text = "$percent% OFF"
                        mrpTV.text = productMRPTV.text.toString().trim()
                        mrpLayout.visibility = View.VISIBLE
                    } else {
                        mrpLayout.visibility = View.GONE
                    }


                    showSaveButton()
                }
            }
            R.id.dotBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "0."
                    priceTV.text = updateProductPriceET.text.toString().trim()
                    updatePriceFirstTime = false
                    productPriceChanged = true
                    globalProductPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 5 && !updateProductPriceET.text.toString().contains(
                            "."
                        )
                    ) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "0."
                        } else {
                            updateProductPriceET.text.toString() + "."
                        }
                        updateProductPriceET.text = price
                        priceTV.text = updateProductPriceET.text.toString().trim()
                        productPriceChanged = true
                        globalProductPriceChanged = true
                        showSaveButton()
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }

                if (productMRPTV.text.toString().trim() != "" && productMRPTV.text.toString().trim() != "0" && productMRPTV.text.toString().trim().toFloat() > updateProductPriceET.text.toString().trim().toFloat()) {
                    val pre =
                        productMRPTV.text.toString().trim().toFloat() - updateProductPriceET.text.toString().trim().toFloat()
                    val div: Float = (pre / productMRPTV.text.toString().trim().toFloat())
                    val percent: Int = (div * 100).toInt()
                    percentageOffTV.text = "$percent% OFF"
                    mrpTV.text = productMRPTV.text.toString().trim()
                    mrpLayout.visibility = View.VISIBLE
                } else {
                    mrpLayout.visibility = View.GONE
                }

                showSaveButton()
            }
            R.id.previousIV -> {
                updatePriceFirstTime = true
                if (productPriceChanged) {
                    selectedCategoryList[productPosition].updated_rate = updateProductPriceET.text.toString().trim()
                    selectedCategoryList[productPosition].name = productNameTV.text.toString().trim()
                    selectedCategoryList[productPosition].local_name = productAliasNameTV.text.toString().trim()
                    selectedCategoryList[productPosition].description = descriptionTV.text.toString().trim()
                    selectedCategoryList[productPosition].mrp = productMRPTV.text.toString().trim()
                    selectedCategoryList[productPosition].price_changed = true
                    productPriceChanged = false
//                    globalProductPriceChanged = false
                    loadPreviousProduct()
                } else {
                    loadPreviousProduct()
                }
            }
            R.id.nextIV -> {
                updatePriceFirstTime = true
                if (productPriceChanged) {
                    selectedCategoryList[productPosition].updated_rate = updateProductPriceET.text.toString().trim()
                    selectedCategoryList[productPosition].name = productNameTV.text.toString().trim()
                    selectedCategoryList[productPosition].local_name = productAliasNameTV.text.toString().trim()
                    selectedCategoryList[productPosition].description = descriptionTV.text.toString().trim()
                    selectedCategoryList[productPosition].mrp = productMRPTV.text.toString().trim()
                    selectedCategoryList[productPosition].price_changed = true
                    productPriceChanged = false
//                    globalProductPriceChanged = false
                    loadNextProduct()
                } else {
                    loadNextProduct()
                }
            }
            R.id.saveBT -> {
                if (productPriceChanged) {
                    selectedCategoryList[productPosition].updated_rate = updateProductPriceET.text.toString().trim()
                    selectedCategoryList[productPosition].name = productNameTV.text.toString().trim()
                    selectedCategoryList[productPosition].local_name = productAliasNameTV.text.toString().trim()
                    selectedCategoryList[productPosition].description = descriptionTV.text.toString().trim()
                    selectedCategoryList[productPosition].mrp = productMRPTV.text.toString().trim()
                    selectedCategoryList[productPosition].price_changed = true
                }
                saveChanges()
            }
            R.id.currentItemLayout -> {
                if (productPriceChanged) {
                    selectedCategoryList[productPosition].updated_rate = updateProductPriceET.text.toString().trim()
                    selectedCategoryList[productPosition].price_changed = true
                }
                val intent = Intent(this, UpdateProductSearchActivity::class.java)
                intent.putExtra("product", Gson().toJson(selectedCategoryList))
                startActivity(intent)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadCategoryData() {
        if (selectedCategoryList.size > 0) {
            if (selectedCategoryList.size > 1 && productPosition != selectedCategoryList.size - 1) {
                nextIV.visibility = View.VISIBLE
            } else {
                nextIV.visibility = View.INVISIBLE
            }
            if (selectedCategoryList.size == 1) {
                nextIV.visibility = View.INVISIBLE
                previousIV.visibility = View.INVISIBLE
            }
            productDetailsLayout.visibility = View.VISIBLE
            productNameTV.setText(selectedCategoryList[productPosition].name)
            productAliasNameTV.setText(selectedCategoryList[productPosition].local_name)

            /*if (selectedCategoryList[productPosition].local_name.trim().length > 0) {
                productAliasNameTV.visibility = View.VISIBLE
            } else {
                productAliasNameTV.visibility = View.INVISIBLE
            }*/
            productMRPTV.setText(selectedCategoryList[productPosition].mrp)
            unitTV.text = "( " + selectedCategoryList[productPosition].unit + " )"
            descriptionTV.setText(selectedCategoryList[productPosition].description)

            if (selectedCategoryList[productPosition].mrp != "" && selectedCategoryList[productPosition].mrp != "0" && selectedCategoryList[productPosition].mrp.toFloat() > selectedCategoryList[productPosition].rate.toFloat()) {
                val pre =
                    selectedCategoryList[productPosition].mrp.toFloat() - selectedCategoryList[productPosition].rate.toFloat()
                val div: Float = (pre / selectedCategoryList[productPosition].mrp.toFloat())
                val percent: Int = (div * 100).toInt()
                percentageOffTV.text = "$percent% OFF"
                mrpTV.text = productMRPTV.text.toString().trim()
                mrpLayout.visibility = View.VISIBLE
            } else {
                mrpLayout.visibility = View.GONE
            }

            /*if (selectedCategoryList[productPosition].description.trim().length > 0) {
                descriptionTV.visibility = View.VISIBLE
            } else {
                descriptionTV.visibility = View.INVISIBLE
            }*/

            Log.e(TAG, "Selected Category List Updated Rate : " + selectedCategoryList[productPosition].updated_rate)
            if (selectedCategoryList[productPosition].updated_rate != null && selectedCategoryList[productPosition].updated_rate != "") {
                Log.e(TAG, "Entered IF Condition")
                updateProductPriceET.text = selectedCategoryList[productPosition].updated_rate
            } else {
                Log.e(TAG, "Entered Else Condition")
                updateProductPriceET.text = selectedCategoryList[productPosition].rate
            }
            priceTV.text = updateProductPriceET.text.toString().trim()
            currentItemTV.text = (productPosition + 1).toString()
//            previousIV.visibility = View.INVISIBLE
        } else {
            productDetailsLayout.visibility = View.GONE
            Toast.makeText(this, "No Products Available!", Toast.LENGTH_SHORT).show()
        }

        showSaveButton()
    }

    @SuppressLint("SetTextI18n")
    private fun loadNextProduct() {
        previousIV.visibility = View.VISIBLE
        if (productPosition < selectedCategoryList.size - 1) {
            productPosition += 1
            nextIV.visibility = View.VISIBLE
            productDetailsLayout.visibility = View.VISIBLE
            productNameTV.setText(selectedCategoryList[productPosition].name)
            productAliasNameTV.setText(selectedCategoryList[productPosition].local_name)

            /*if (selectedCategoryList[productPosition].local_name.trim().length > 0) {
                productAliasNameTV.visibility = View.VISIBLE
            } else {
                productAliasNameTV.visibility = View.INVISIBLE
            }*/
            productMRPTV.setText(selectedCategoryList[productPosition].mrp)
            unitTV.text = "( " + selectedCategoryList[productPosition].unit + " )"
            descriptionTV.setText(selectedCategoryList[productPosition].description)

            if (selectedCategoryList[productPosition].mrp != "" && selectedCategoryList[productPosition].mrp != "0" && selectedCategoryList[productPosition].mrp.toFloat() > selectedCategoryList[productPosition].rate.toFloat()) {
                val pre =
                    selectedCategoryList[productPosition].mrp.toFloat() - selectedCategoryList[productPosition].rate.toFloat()
                val div: Float = (pre / selectedCategoryList[productPosition].mrp.toFloat())
                val percent: Int = (div * 100).toInt()
                percentageOffTV.text = "$percent% OFF"
                mrpTV.text = productMRPTV.text.toString().trim()
                mrpLayout.visibility = View.VISIBLE
            } else {
                mrpLayout.visibility = View.GONE
            }

            /*if (selectedCategoryList[productPosition].description.trim().length > 0) {
                descriptionTV.visibility = View.VISIBLE
            } else {
                descriptionTV.visibility = View.INVISIBLE
            }*/

            if (selectedCategoryList[productPosition].updated_rate != null && selectedCategoryList[productPosition].updated_rate != "") {
                updateProductPriceET.text = selectedCategoryList[productPosition].updated_rate
            } else {
                updateProductPriceET.text = selectedCategoryList[productPosition].rate
            }
            priceTV.text = updateProductPriceET.text.toString().trim()
            currentItemTV.text = (productPosition + 1).toString()
//            totalItemTV.text = selectedCategoryList.size.toString()

            if (productPosition == selectedCategoryList.size - 1) {
                nextIV.visibility = View.INVISIBLE
            }

            if (productPosition == selectedCategoryList.size - 2) {
                if (categoryNameTV.text.toString().trim() == "All") {
                    loadMainAPI()
                } else {
                    loadCategoryAPI(categoryNameTV.text.toString().trim())
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadPreviousProduct() {
        nextIV.visibility = View.VISIBLE
        if (productPosition != 0) {
            productPosition -= 1
            previousIV.visibility = View.VISIBLE
            productDetailsLayout.visibility = View.VISIBLE
            productNameTV.setText(selectedCategoryList[productPosition].name)
            productAliasNameTV.setText(selectedCategoryList[productPosition].local_name)

            /*if (selectedCategoryList[productPosition].local_name.trim().length > 0) {
                productAliasNameTV.visibility = View.VISIBLE
            } else {
                productAliasNameTV.visibility = View.INVISIBLE
            }*/
            productMRPTV.setText(selectedCategoryList[productPosition].mrp)
            unitTV.text = "( " + selectedCategoryList[productPosition].unit + " )"
            descriptionTV.setText(selectedCategoryList[productPosition].description)

            if (selectedCategoryList[productPosition].mrp != "" && selectedCategoryList[productPosition].mrp != "0" && selectedCategoryList[productPosition].mrp.toFloat() > selectedCategoryList[productPosition].rate.toFloat()) {
                val pre =
                    selectedCategoryList[productPosition].mrp.toFloat() - selectedCategoryList[productPosition].rate.toFloat()
                val div: Float = (pre / selectedCategoryList[productPosition].mrp.toFloat())
                val percent: Int = (div * 100).toInt()
                percentageOffTV.text = "$percent% OFF"
                mrpTV.text = productMRPTV.text.toString().trim()
                mrpLayout.visibility = View.VISIBLE
            } else {
                mrpLayout.visibility = View.GONE
            }

            /*if (selectedCategoryList[productPosition].description.trim().length > 0) {
                descriptionTV.visibility = View.VISIBLE
            } else {
                descriptionTV.visibility = View.INVISIBLE
            }*/

            Log.e(TAG, "Selected Category List Updated Rate : " + selectedCategoryList[productPosition].updated_rate)
            if (selectedCategoryList[productPosition].updated_rate != null && selectedCategoryList[productPosition].updated_rate != "") {
                updateProductPriceET.text = selectedCategoryList[productPosition].updated_rate
            } else {
                updateProductPriceET.text = selectedCategoryList[productPosition].rate
            }
            priceTV.text = updateProductPriceET.text.toString().trim()
            currentItemTV.text = (productPosition + 1).toString()
//            totalItemTV.text = selectedCategoryList.size.toString()

            if (productPosition == 0) {
                previousIV.visibility = View.INVISIBLE
            }
        }
    }

    override fun onBackPressed() {
        backPress()
    }

    private fun backPress() {
        if (unSavedItems) {
            selectedCategoryList[productPosition].updated_rate = updateProductPriceET.text.toString().trim()
            selectedCategoryList[productPosition].price_changed = true

            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.unsaved_product_dialog)

            val cancelBT = dialog.findViewById(R.id.cancelBT) as Button
            val discardBT = dialog.findViewById(R.id.discardBT) as Button
            val saveBT = dialog.findViewById(R.id.saveBT) as Button

            cancelBT.setOnClickListener {
                dialog.dismiss()
            }

            discardBT.setOnClickListener {
                super.onBackPressed()
            }

            saveBT.setOnClickListener {
                dialog.dismiss()
                backPressClicked = true
                saveChanges()
            }
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            val window = dialog.window!!
            window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        } else {
            super.onBackPressed()
        }
    }

    fun receiveClickListener(category: String) {
        updatePriceFirstTime = true
        categoryChangeFirstTime = true
        categoryDialog.dismiss()

        if (category != "All") {
            productsList.clear()
            selectedCategoryList.clear()
            productPosition = 0
            currentPage = 0
            categoryNameTV.text = category

            loadCategoryAPI(category)

        } else {
            productsList.clear()
            selectedCategoryList.clear()
            productPosition = 0
            currentPage = 0
            categoryNameTV.text = category

            loadMainAPI()
        }
    }

    private fun saveChanges() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        if (!isConnected) {
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
        } else {
            progressLayout!!.visibility = View.VISIBLE

            for (i in selectedCategoryList) {
                if (i.price_changed) {
                    val obj = JSONObject()
                    obj.put("product_id", i.product_id)
                    obj.put("product_name", i.name)
                    obj.put("local_name", i.local_name)
                    obj.put("description", i.description)
                    obj.put("rate", i.updated_rate)
                    obj.put("mrp", i.mrp)
                    confirmArray.put(obj)
                }
            }

            Log.e(TAG, "Price Changed Confirm Array List : $confirmArray")
            val fObj = JSONObject()
            fObj.put("supplier_id", shared!!.getString("id", ""))
            fObj.put("products_list", confirmArray)
            repo!!.updateAllSupplierProductRate(fObj.toString())
        }
    }

    private fun showSaveButton() {
        if (globalProductPriceChanged) {
            saveBT.setTextColor(ContextCompat.getColor(this, R.color.white))
            saveBT.setBackgroundResource(R.drawable.button_bg)
            saveBT.isClickable = true
            unSavedItems = true
        } else {
            saveBT.setTextColor(ContextCompat.getColor(this, R.color.light_gray))
            saveBT.setBackgroundResource(R.drawable.price_save_disable_bt_bg)
            saveBT.isClickable = false
            unSavedItems = false
        }
    }

    private val selectedProductUpdate = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            productPosition = intent.getIntExtra("position", 0)
            Log.e(TAG, "Received Product Position : $productPosition")
            loadSelectedProduct()
        }
    }

    private val priceUpdateChange = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            for (i in selectedCategoryList) {
                if (i.product_id == intent.getStringExtra("product_id")) {
                    i.rate = intent.getStringExtra("rate")
                    i.name = intent.getStringExtra("product_name")
                    i.local_name = intent.getStringExtra("local_name")
                    i.description = intent.getStringExtra("description")
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadSelectedProduct() {
        updatePriceFirstTime = true
        if (productPosition == 0) {
            previousIV.visibility = View.INVISIBLE
        } else {
            previousIV.visibility = View.VISIBLE
        }
        if (productPosition < selectedCategoryList.size) {
            nextIV.visibility = View.VISIBLE
            productDetailsLayout.visibility = View.VISIBLE
            productNameTV.setText(selectedCategoryList[productPosition].name)
            productAliasNameTV.setText(selectedCategoryList[productPosition].local_name)

            if (selectedCategoryList[productPosition].local_name.trim().length > 0) {
                productAliasNameTV.visibility = View.VISIBLE
            } else {
                productAliasNameTV.visibility = View.INVISIBLE
            }
            productMRPTV.setText(selectedCategoryList[productPosition].mrp)
            unitTV.text = selectedCategoryList[productPosition].unit
            descriptionTV.setText(selectedCategoryList[productPosition].description)

            if (selectedCategoryList[productPosition].mrp != "" && selectedCategoryList[productPosition].mrp != "0" && selectedCategoryList[productPosition].mrp.toFloat() > selectedCategoryList[productPosition].rate.toFloat()) {
                val pre =
                    selectedCategoryList[productPosition].mrp.toFloat() - selectedCategoryList[productPosition].rate.toFloat()
                val div: Float = (pre / selectedCategoryList[productPosition].mrp.toFloat())
                val percent: Int = (div * 100).toInt()
                percentageOffTV.text = "$percent% OFF"
                mrpTV.text = productMRPTV.text.toString().trim()
                mrpLayout.visibility = View.VISIBLE
            } else {
                mrpLayout.visibility = View.GONE
            }

            /*if (selectedCategoryList[productPosition].description.trim().length > 0) {
                descriptionTV.visibility = View.VISIBLE
            } else {
                descriptionTV.visibility = View.INVISIBLE
            }*/

            if (selectedCategoryList[productPosition].updated_rate != null && selectedCategoryList[productPosition].updated_rate != "") {
                updateProductPriceET.text = selectedCategoryList[productPosition].updated_rate
            } else {
                updateProductPriceET.text = selectedCategoryList[productPosition].rate
            }
            priceTV.text = updateProductPriceET.text.toString().trim()
            currentItemTV.text = (productPosition + 1).toString()
//            totalItemTV.text = selectedCategoryList.size.toString()

            if (productPosition == selectedCategoryList.size - 1) {
                nextIV.visibility = View.INVISIBLE
            }
        }
    }

    private fun loadMainAPI() {
        progressLayout.visibility = View.VISIBLE
        val obj = JSONObject()
        obj.put("supplier_id", shared!!.getString("id", ""))
        obj.put("page_no", currentPage++)
        repo!!.getProductsBySupplierID(obj.toString())
    }

    private fun loadCategoryAPI(category: String) {
        progressLayout.visibility = View.VISIBLE
        val obj = JSONObject()
        obj.put("supplier_id", shared!!.getString("id", ""))
        obj.put("category", category)
        obj.put("page_no", currentPage++)
        repo!!.getCategoryFilteredProductsList(obj.toString())
    }
}
