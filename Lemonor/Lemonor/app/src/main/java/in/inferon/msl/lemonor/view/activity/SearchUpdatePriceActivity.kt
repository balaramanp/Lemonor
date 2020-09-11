package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.pojo.Products
import `in`.inferon.msl.lemonor.repo.Repository
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_search_update_price.*
import kotlinx.android.synthetic.main.activity_search_update_price.backIB
import kotlinx.android.synthetic.main.activity_search_update_price.clearBT
import kotlinx.android.synthetic.main.activity_search_update_price.descriptionTV
import kotlinx.android.synthetic.main.activity_search_update_price.dotBT
import kotlinx.android.synthetic.main.activity_search_update_price.eightBT
import kotlinx.android.synthetic.main.activity_search_update_price.fiveBT
import kotlinx.android.synthetic.main.activity_search_update_price.fourBT
import kotlinx.android.synthetic.main.activity_search_update_price.nineBT
import kotlinx.android.synthetic.main.activity_search_update_price.oneBT
import kotlinx.android.synthetic.main.activity_search_update_price.priceTV
import kotlinx.android.synthetic.main.activity_search_update_price.productAliasNameTV
import kotlinx.android.synthetic.main.activity_search_update_price.productDetailsLayout
import kotlinx.android.synthetic.main.activity_search_update_price.productNameTV
import kotlinx.android.synthetic.main.activity_search_update_price.progressLayout
import kotlinx.android.synthetic.main.activity_search_update_price.saveBT
import kotlinx.android.synthetic.main.activity_search_update_price.sevenBT
import kotlinx.android.synthetic.main.activity_search_update_price.sixBT
import kotlinx.android.synthetic.main.activity_search_update_price.threeBT
import kotlinx.android.synthetic.main.activity_search_update_price.twoBT
import kotlinx.android.synthetic.main.activity_search_update_price.unitTV
import kotlinx.android.synthetic.main.activity_search_update_price.updateProductPriceET
import kotlinx.android.synthetic.main.activity_search_update_price.zeroBT
import kotlinx.android.synthetic.main.activity_update_product.*
import org.json.JSONArray
import org.json.JSONObject

class SearchUpdatePriceActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = SearchUpdatePriceActivity::class.java.simpleName
    private var product: Products? = null
    private var position = 0
    private var productPriceChanged = false
    private var updatePriceFirstTime = true
    private var priceChanged = false
    private val confirmArray = JSONArray()
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    private var repo: Repository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_update_price)

        repo = Repository()
        shared = getSharedPreferences(PREF, MODE_PRIVATE)

        product = Gson().fromJson(intent.getStringExtra("product"), object : TypeToken<Products>() {}.type)
        position = intent.getIntExtra("position", 0)
        loadCategoryData()


        repo!!.updateAllSupplierProductRate.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout!!.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    if (product!!.price_changed) {
                        product!!.rate = product!!.updated_rate
                        product!!.updated_rate = ""
                        product!!.price_changed = false
                    }
                    productPriceChanged = false
                    showSaveButton()
                    updatePriceFirstTime = true
                    Toast.makeText(this, "Product Price Updated Successfully!", Toast.LENGTH_SHORT).show()

                    priceChanged = true

                    if (priceChanged) {
                        loadData()
                        super.onBackPressed()
                    } else {
                        super.onBackPressed()
                    }

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
                if (product!!.name != productNameTV.text.toString().trim()) {
                    Log.e(TAG, "Product Name Changed")
                    productPriceChanged = true
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
                if (product!!.local_name != productAliasNameTV.text.toString().trim()) {
                    Log.e(TAG, "Product Local Name Changed")
                    productPriceChanged = true
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
                if (product!!.description != descriptionTV.text.toString().trim()) {
                    Log.e(TAG, "Product Description Changed")
                    productPriceChanged = true
                    showSaveButton()
                }
            }
        })


        backIB.setOnClickListener(this)
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
        saveBT.setOnClickListener(this)
    }

    private fun loadCategoryData() {
        productDetailsLayout.visibility = View.VISIBLE
        productNameTV.setText(product!!.name)
        productAliasNameTV.setText(product!!.local_name)

        priceTV.text = product!!.rate
        unitTV.text = product!!.unit
        descriptionTV.setText(product!!.description)

        Log.e(TAG, "Selected Category List Updated Rate : " + product!!.updated_rate)
        if (product!!.updated_rate != null && product!!.updated_rate != "") {
            Log.e(TAG, "Entered IF Condition")
            updateProductPriceET.text = product!!.updated_rate
        } else {
            Log.e(TAG, "Entered Else Condition")
            updateProductPriceET.text = product!!.rate
        }

        showSaveButton()
    }

    override fun onBackPressed() {
        if (priceChanged) {
            loadData()
            super.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backIB -> {
                if (priceChanged) {
                    loadData()
                    super.onBackPressed()
                } else {
                    super.onBackPressed()
                }
            }
            R.id.oneBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "1"
                    updatePriceFirstTime = false
                    productPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 5) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "1"
                        } else {
                            updateProductPriceET.text.toString() + "1"
                        }
                        updateProductPriceET.text = price
                        productPriceChanged = true
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }
                showSaveButton()
            }
            R.id.twoBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "2"
                    updatePriceFirstTime = false
                    productPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 5) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "2"
                        } else {
                            updateProductPriceET.text.toString() + "2"
                        }
                        updateProductPriceET.text = price
                        productPriceChanged = true
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }
                showSaveButton()
            }
            R.id.threeBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "3"
                    updatePriceFirstTime = false
                    productPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 5) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "3"
                        } else {
                            updateProductPriceET.text.toString() + "3"
                        }
                        updateProductPriceET.text = price
                        productPriceChanged = true
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }
                showSaveButton()
            }
            R.id.fourBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "4"
                    updatePriceFirstTime = false
                    productPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 5) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "4"
                        } else {
                            updateProductPriceET.text.toString() + "4"
                        }
                        updateProductPriceET.text = price
                        productPriceChanged = true
                        showSaveButton()
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }
                showSaveButton()
            }
            R.id.fiveBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "5"
                    updatePriceFirstTime = false
                    productPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 5) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "5"
                        } else {
                            updateProductPriceET.text.toString() + "5"
                        }
                        updateProductPriceET.text = price
                        productPriceChanged = true
                        showSaveButton()
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }
                showSaveButton()
            }
            R.id.sixBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "6"
                    updatePriceFirstTime = false
                    productPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 5) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "6"
                        } else {
                            updateProductPriceET.text.toString() + "6"
                        }
                        updateProductPriceET.text = price
                        productPriceChanged = true
                        showSaveButton()
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }
                showSaveButton()
            }
            R.id.sevenBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "7"
                    updatePriceFirstTime = false
                    productPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 5) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "7"
                        } else {
                            updateProductPriceET.text.toString() + "7"
                        }
                        updateProductPriceET.text = price
                        productPriceChanged = true
                        showSaveButton()
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }
                showSaveButton()
            }
            R.id.eightBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "8"
                    updatePriceFirstTime = false
                    productPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 5) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "8"
                        } else {
                            updateProductPriceET.text.toString() + "8"
                        }
                        updateProductPriceET.text = price
                        productPriceChanged = true
                        showSaveButton()
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }
                showSaveButton()
            }
            R.id.nineBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "9"
                    updatePriceFirstTime = false
                    productPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 5) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "9"
                        } else {
                            updateProductPriceET.text.toString() + "9"
                        }
                        updateProductPriceET.text = price
                        productPriceChanged = true
                        showSaveButton()
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
                }
                showSaveButton()
            }
            R.id.zeroBT -> {
                if (updatePriceFirstTime) {
                    updateProductPriceET.text = "0"
                    updatePriceFirstTime = false
                    productPriceChanged = true
                } else {
                    if (updateProductPriceET.text.toString().trim().length < 5) {
                        val price: String = if (updateProductPriceET.text == "0") {
                            "0"
                        } else {
                            updateProductPriceET.text.toString() + "0"
                        }
                        updateProductPriceET.text = price
                        productPriceChanged = true
                        showSaveButton()
                    } else {
                        Toast.makeText(this, "Reached Maximum Limit!", Toast.LENGTH_SHORT).show()
                    }
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
                    productPriceChanged = true
                    showSaveButton()
                }
            }
            R.id.dotBT -> {

            }
            R.id.saveBT -> {
                if (productPriceChanged) {
                    product!!.updated_rate = updateProductPriceET.text.toString().trim()
                    product!!.name = productNameTV.text.toString().trim()
                    product!!.local_name = productAliasNameTV.text.toString().trim()
                    product!!.description = descriptionTV.text.toString().trim()
                    product!!.price_changed = true
                    saveChanges()
                }
            }
        }
    }

    private fun showSaveButton() {
        if (productPriceChanged) {
            saveBT.setTextColor(ContextCompat.getColor(this, R.color.white))
            saveBT.setBackgroundResource(R.drawable.button_bg)
            saveBT.isClickable = true
        } else {
            saveBT.setTextColor(ContextCompat.getColor(this, R.color.light_gray))
            saveBT.setBackgroundResource(R.drawable.price_save_disable_bt_bg)
            saveBT.isClickable = false
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

            if (product!!.price_changed) {
                val obj = JSONObject()
                obj.put("product_id", product!!.product_id)
                obj.put("product_name", product!!.name)
                obj.put("local_name", product!!.local_name)
                obj.put("description", product!!.description)
                obj.put("rate", product!!.updated_rate)
                confirmArray.put(obj)
            }

            Log.e(TAG, "Confirm Array : $confirmArray")
            val fObj = JSONObject()
            fObj.put("supplier_id", shared!!.getString("id", ""))
            fObj.put("products_list", confirmArray)
            repo!!.updateAllSupplierProductRate(fObj.toString())
        }
    }

    private fun loadData() {
        val intent = Intent("PriceChanged")
        intent.putExtra("position", position)
        intent.putExtra("rate", product!!.rate)
        intent.putExtra("product_name", product!!.name)
        intent.putExtra("local_name", product!!.local_name)
        intent.putExtra("description", product!!.description)
        intent.putExtra("product_id", product!!.product_id)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
