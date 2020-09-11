package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.pojo.Products
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.adapter.UpdateProductSearchAdapter
import android.content.*
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_place_order.*
import kotlinx.android.synthetic.main.activity_update_product.*
import kotlinx.android.synthetic.main.activity_update_product_search.*
import kotlinx.android.synthetic.main.activity_update_product_search.backIB
import kotlinx.android.synthetic.main.activity_update_product_search.closeIB
import kotlinx.android.synthetic.main.activity_update_product_search.progressLayout
import kotlinx.android.synthetic.main.activity_update_product_search.recyclerView
import kotlinx.android.synthetic.main.activity_update_product_search.searchET
import org.json.JSONObject

class UpdateProductSearchActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = UpdateProductSearchActivity::class.java.simpleName
    private var productsListData = ""
    private var currentPage = 0
    private var isLoading = false
    private var productsList = mutableListOf<Products>()
    private var mainProductsList = mutableListOf<Products>()
    private var updateProductSearchAdapter: UpdateProductSearchAdapter? = null
    private var shared: SharedPreferences? = null
    private val PREF = "Pref"
    private var repo: Repository? = null
    private var updatedProductID = ""
    private var updatedProductRate = ""
    private var updatedProductName = ""
    private var updatedProductLocalName = ""
    private var updatedProductDescription = ""
    private var loadDataFor = "main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_product_search)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(selectedProduct, IntentFilter("SelectedProduct"))

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(priceChanged, IntentFilter("PriceChanged"))

        repo = Repository()
        shared = getSharedPreferences(PREF, MODE_PRIVATE)

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        Log.e(TAG, "Network Connectivity : $isConnected")
        if (!isConnected) {
            val intent = Intent(this, NoInternetActivity::class.java)
            startActivity(intent)
        } else {
            recyclerView.layoutManager = LinearLayoutManager(this)
            updateProductSearchAdapter =
                UpdateProductSearchAdapter(this, mainProductsList, this)
            recyclerView.adapter = updateProductSearchAdapter

            loadMainAPI()
        }

        repo!!.getProductsBySupplierID.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout!!.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    productsList =
                        Gson().fromJson(
                            jsonObject.getString("supplier_products"),
                            object : TypeToken<MutableList<Products>>() {}.type
                        )

                    mainProductsList.addAll(productsList)

                    updateProductSearchAdapter!!.notifyDataSetChanged()
                    isLoading = false
                } else if (jsonObject.getString("status") == "error") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                }
            }
        })

        repo!!.getSearchFilteredProductsList.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    if (currentPage == 1) {
                        productsList.clear()
                        mainProductsList.clear()
                    }
                    productsList =
                        Gson().fromJson(
                            jsonObject.getString("supplier_products_list"),
                            object : TypeToken<MutableList<Products>>() {}.type
                        )
                    mainProductsList.addAll(productsList)

                    updateProductSearchAdapter!!.notifyDataSetChanged()
                    isLoading = false

                } else if (jsonObject.getString("status") == "error") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                }
            }
        })

        setOnScrollListener(recyclerView)

        var animateFirstTime = true
        searchET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (searchET.text.toString().trim().isNotEmpty()) {
                    closeIB.visibility = View.VISIBLE
                    if (animateFirstTime) {
                        val slideIn: Animation =
                            AnimationUtils.loadAnimation(this@UpdateProductSearchActivity, R.anim.slide_in_left)
                        closeIB.startAnimation(slideIn)
                        animateFirstTime = false
                    }
                } else {
                    val slideOut: Animation =
                        AnimationUtils.loadAnimation(this@UpdateProductSearchActivity, R.anim.slide_out_left)
                    closeIB.startAnimation(slideOut)
                    closeIB.visibility = View.GONE
                    animateFirstTime = true
                }
                if (searchET.text.toString().trim().length > 2) {
                    productsList.clear()
                    mainProductsList.clear()
                    currentPage = 0
                    loadDataFor = "search"
                    loadSearchAPI()
                } else if (searchET.text.toString().trim().isEmpty()) {
                    productsList.clear()
                    mainProductsList.clear()
                    currentPage = 0
                    loadDataFor = "main"
                    loadMainAPI()
                }

                updateProductSearchAdapter!!.notifyDataSetChanged()
                /*if (mainProductsList.size == 0) {
                    Toast.makeText(this@UpdateProductSearchActivity, "No Products Available!", Toast.LENGTH_SHORT)
                        .show()
                }*/
            }
        })

        backIB.setOnClickListener(this)
        closeIB.setOnClickListener(this)
    }

    private fun loadMainAPI() {
        progressLayout.visibility = View.VISIBLE
        val obj = JSONObject()
        obj.put("supplier_id", shared!!.getString("id", ""))
        obj.put("page_no", currentPage++)
        repo!!.getProductsBySupplierID(obj.toString())
    }

    private fun loadSearchAPI() {
        progressLayout.visibility = View.VISIBLE
        val obj = JSONObject()
        obj.put("supplier_id", shared!!.getString("id", ""))
        obj.put("search_term", searchET.text.toString().trim())
        obj.put("page_no", currentPage++)
        repo!!.getSearchFilteredProductsList(obj.toString())
    }

    override fun onBackPressed() {
        if (updatedProductID != "") {
            loadPriceUpdateData()
            super.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backIB -> {
                if (updatedProductID != "") {
                    loadPriceUpdateData()
                    super.onBackPressed()
                } else {
                    super.onBackPressed()
                }
            }
            R.id.closeIB -> {
                searchET.setText("")
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchET.windowToken, 0)

                productsList.clear()
                mainProductsList.clear()
                currentPage = 0
                updateProductSearchAdapter!!.notifyDataSetChanged()


                val slideOut: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_out_right)
                closeIB.startAnimation(slideOut)
                closeIB.visibility = View.GONE
            }
        }
    }

    private fun setOnScrollListener(recyclerView: RecyclerView) {
        Log.e(TAG, "Entered ScrollListener")
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager
                if (layoutManager is LinearLayoutManager) {
                    val mLayoutManager = layoutManager as LinearLayoutManager?
                    val visibleItemCount = mLayoutManager!!.childCount
                    val totalItemCount = mLayoutManager.itemCount
                    val pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition()
                    /*Log.i(
                        TAG,
                        "onScrolled: " + isLoading + " " + isLastPage + " " + (totalItemCount >= Constants.PAGE_SIZE) + " " + (pastVisibleItems >= 0) + " " + (visibleItemCount + pastVisibleItems >= totalItemCount)
                    )*/
                    if (!isLoading) {
                        if (visibleItemCount + pastVisibleItems >= totalItemCount
                            && pastVisibleItems >= 0
                            && totalItemCount >= Constants.PAGE_SIZE
                        ) {
                            isLoading = true
                            Log.i(TAG, "onScrolled: REQUESTING FOR PAGE $currentPage")
                            when (loadDataFor) {
                                "main" -> loadMainAPI()
                                "search" -> loadSearchAPI()
                            }
                        }
                    }
                }
            }
        })
    }

    private val selectedProduct = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val selectedProduct = intent.getStringExtra("product_id")

            var pos = 0
            productsList.forEachIndexed { index, products ->
                if (products.product_id == selectedProduct) {
                    pos = index
                }
            }
            loadData(pos)
            finish()
        }
    }

    private val priceChanged = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updatedProductID = intent.getStringExtra("product_id")
            updatedProductRate = intent.getStringExtra("rate")
            updatedProductName = intent.getStringExtra("product_name")
            updatedProductLocalName = intent.getStringExtra("local_name")
            updatedProductDescription = intent.getStringExtra("description")

            val pos = intent.getIntExtra("position", 0)
            mainProductsList[pos].rate = intent.getStringExtra("rate")
            mainProductsList[pos].name = intent.getStringExtra("product_name")
            mainProductsList[pos].local_name = intent.getStringExtra("local_name")
            mainProductsList[pos].description = intent.getStringExtra("description")
            updateProductSearchAdapter!!.notifyItemChanged(pos)

        }
    }

    private fun loadData(position: Int) {
        val intent = Intent("SelectedProductUpdate")
        intent.putExtra("position", position)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun loadPriceUpdateData() {
        val intent = Intent("PriceUpdateChange")
        intent.putExtra("rate", updatedProductRate)
        intent.putExtra("product_id", updatedProductID)
        intent.putExtra("product_name", updatedProductName)
        intent.putExtra("local_name", updatedProductLocalName)
        intent.putExtra("description", updatedProductDescription)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
