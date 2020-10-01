package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.pojo.Products
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.adapter.PlaceOrderAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_product_listing.*
import kotlinx.android.synthetic.main.activity_product_listing.progressLayout
import kotlinx.android.synthetic.main.activity_product_listing.recyclerView
import org.json.JSONObject

class ProductListingActivity : AppCompatActivity() {
    private val TAG = ProductListingActivity::class.java.simpleName
    private var repo: Repository? = null
    private var currentPage = 0
    private var supplier_id = ""
    private var shopName = ""
    private var category = ""
    private var from = ""
    private var products = ""
    private var supplierDiscount = ""
    private var productsList = mutableListOf<Products>()
    private var mainProductsList = mutableListOf<Products>()
    private var orderedProductsList = mutableListOf<Products>()
    private var placeOrderAdapter: PlaceOrderAdapter? = null
    private var isLoading = false
    private var placeOrderActivity = PlaceOrderActivity()
    private var isLastPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_listing)
        repo = Repository()

        supplier_id = intent.getStringExtra("supplier_id")!!
        shopName = intent.getStringExtra("shop_name")!!
        category = intent.getStringExtra("category")!!
        from = intent.getStringExtra("from")!!
        products = intent.getStringExtra("orderedProductsList")!!
        orderedProductsList = Gson().fromJson(products, object : TypeToken<MutableList<Products>>() {}.type)

        shopNameTV.text = shopName
        categoryTV.text = category
        if (from == "category") {
            loadCategoryAPI(category)
        } else {
            loadMainAPI()
        }

        recyclerView.layoutManager = LinearLayoutManager(this@ProductListingActivity)
        placeOrderAdapter =
            PlaceOrderAdapter(
                this@ProductListingActivity,
                mainProductsList,
                placeOrderActivity,
                orderedProductsList,
                "productListingActivity"
            )
        recyclerView.adapter = placeOrderAdapter

        repo!!.getProductsListBySupplierIdForCustomer.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                Log.e(TAG, "Get Product List By Supplier ID For Customer : $jsonObject")
                if (jsonObject.getString("status") == "ok") {
                    supplierDiscount = jsonObject.getString("supplier_discount")
                    productsList =
                        Gson().fromJson(
                            jsonObject.getString("supplier_products_list"),
                            object : TypeToken<MutableList<Products>>() {}.type
                        )
                    mainProductsList.addAll(productsList)

                    placeOrderAdapter!!.notifyDataSetChanged()
                    isLoading = false

                } else if (jsonObject.getString("status") == "error") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                }
            }
        })


        repo!!.getCategoryFilteredProductsList.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    productsList =
                        Gson().fromJson(
                            jsonObject.getString("supplier_products_list"),
                            object : TypeToken<MutableList<Products>>() {}.type
                        )
                    mainProductsList.addAll(productsList)

                    placeOrderAdapter!!.notifyDataSetChanged()
                    isLoading = false

                } else if (jsonObject.getString("status") == "error") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                }
            }
        })

        setOnScrollListener(recyclerView)

        backIB.setOnClickListener {
//            placeOrderActivity.backFromProductListingActivity()
            super.onBackPressed()
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
                    Log.i(
                        TAG,
                        "onScrolled: " + isLoading + " " + isLastPage + " " + (totalItemCount >= Constants.PAGE_SIZE) + " " + (pastVisibleItems >= 0) + " " + (visibleItemCount + pastVisibleItems >= totalItemCount)
                    )
                    if (!isLoading) {
                        if (visibleItemCount + pastVisibleItems >= totalItemCount - 15
                            && pastVisibleItems >= 0
                            && totalItemCount >= Constants.PAGE_SIZE
                        ) {
                            isLoading = true
                            Log.i(TAG, "onScrolled: REQUESTING FOR PAGE $currentPage")
                            when (from) {
                                "main" -> loadMainAPI()
                                "category" -> loadCategoryAPI(category)
//                                "search" -> loadSearchAPI()
                            }
                        }
                    }
                }
            }
        })
    }


    private fun loadMainAPI() {
        val obj = JSONObject()
        obj.put("supplier_id", supplier_id)
        obj.put("page_no", currentPage++)
        repo!!.getProductsListBySupplierIdForCustomer(obj.toString())
    }

    private fun loadCategoryAPI(category: String) {
        val obj = JSONObject()
        obj.put("supplier_id", supplier_id)
        obj.put("category", category)
        obj.put("page_no", currentPage++)
        repo!!.getCategoryFilteredProductsList(obj.toString())
    }

    override fun onBackPressed() {
//        placeOrderActivity.backFromProductListingActivity()
        super.onBackPressed()
    }
}
