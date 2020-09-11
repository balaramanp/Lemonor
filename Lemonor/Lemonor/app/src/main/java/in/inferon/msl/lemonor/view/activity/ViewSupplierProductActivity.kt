package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.pojo.Category
import `in`.inferon.msl.lemonor.model.pojo.Products
import `in`.inferon.msl.lemonor.model.pojo.Unit
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.adapter.SupplierProductsAdapter
import `in`.inferon.msl.lemonor.view.adapter.VSPSelectCategoriesAdapter
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
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_view_supplier_product.*
import kotlinx.android.synthetic.main.activity_view_supplier_product.backIB
import kotlinx.android.synthetic.main.activity_view_supplier_product.categoryNameTV
import kotlinx.android.synthetic.main.activity_view_supplier_product.closeIB
import kotlinx.android.synthetic.main.activity_view_supplier_product.progressLayout
import kotlinx.android.synthetic.main.activity_view_supplier_product.recyclerView
import kotlinx.android.synthetic.main.activity_view_supplier_product.searchET
import org.json.JSONObject

class ViewSupplierProductActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = ViewSupplierProductActivity::class.java.simpleName
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    private var productsList = mutableListOf<Products>()
    private var mainProductsList = mutableListOf<Products>()
    private var supplierProductsAdapter: SupplierProductsAdapter? = null
    private var unitList = mutableListOf<Unit>()
    private var categoryList = mutableListOf<Category>()
    private lateinit var categoryDialog: Dialog
    private var categories = mutableListOf<String>()
    private var currentPage = 0
    private var isLoading = false
    private var isLastPage = false
    private var isFirstTimeData = true
    private var loadDataFor = "main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_supplier_product)
        Constants.repository = Repository()
        shared = getSharedPreferences(PREF, MODE_PRIVATE)

        progressLayout.visibility = View.VISIBLE

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        Log.e(TAG, "Network Connectivity : $isConnected")
        if (!isConnected) {
//            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, NoInternetActivity::class.java)
            startActivity(intent)
        } else {
            val initObj = JSONObject()
            Constants.repository!!.getInitData(initObj.toString())
        }


        Constants.repository!!.getInitData.observe(this, androidx.lifecycle.Observer {
            run {
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    unitList =
                        Gson().fromJson(
                            jsonObject.getString("unit_list"),
                            object : TypeToken<MutableList<Unit>>() {}.type
                        )

                    Log.e(TAG, "Unit Item : " + unitList[0])

                    categoryList =
                        Gson().fromJson(
                            jsonObject.getString("category_list"),
                            object : TypeToken<MutableList<Category>>() {}.type
                        )

                    loadMainAPI()
                }
            }
        })


        Constants.repository!!.getProductsBySupplierID.observe(this, androidx.lifecycle.Observer {
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

                    if (isFirstTimeData) {
                        recyclerView.layoutManager = LinearLayoutManager(this)
                        supplierProductsAdapter =
                            SupplierProductsAdapter(this, mainProductsList, categoryList, unitList, this)
                        recyclerView.adapter = supplierProductsAdapter

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
                        isFirstTimeData = false
                    }

                    if (mainProductsList.size > 0) {
                        noProductsLayout.visibility = View.GONE
                        supplierProductsAdapter!!.notifyDataSetChanged()
                        isLoading = false
                    } else {
                        noProductsLayout.visibility = View.VISIBLE
                    }
                }
            }
        })


        Constants.repository!!.getSearchFilteredProductsList.observe(this, androidx.lifecycle.Observer {
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

                    noProductsLayout.visibility = View.GONE
                    supplierProductsAdapter!!.notifyDataSetChanged()
                    isLoading = false

                } else if (jsonObject.getString("status") == "error") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                }
            }
        })

        Constants.repository!!.getCategoryFilteredProductsList.observe(this, androidx.lifecycle.Observer {
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

                    noProductsLayout.visibility = View.GONE
                    supplierProductsAdapter!!.notifyDataSetChanged()
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
                if (searchET.text.toString().trim().length > 0) {
                    closeIB.visibility = View.VISIBLE
                    if (animateFirstTime) {
                        val slideIn: Animation =
                            AnimationUtils.loadAnimation(this@ViewSupplierProductActivity, R.anim.slide_in_left)
                        closeIB.startAnimation(slideIn)
                        animateFirstTime = false
                    }
                } else {
                    val slideOut: Animation =
                        AnimationUtils.loadAnimation(this@ViewSupplierProductActivity, R.anim.slide_out_left)
                    closeIB.startAnimation(slideOut)
                    closeIB.visibility = View.GONE
                    animateFirstTime = true
                }

                if (categories.size > 0) {
                    categoryNameTV.visibility = View.VISIBLE
                    categoryNameTV.text = categories[0]
                } else {
                    categoryNameTV.visibility = View.GONE
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
            }
        })

        backIB.setOnClickListener(this)
        categoryNameTV.setOnClickListener(this)
        closeIB.setOnClickListener(this)
        addProductLayout.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backIB -> {
                super.onBackPressed()
            }
            R.id.categoryNameTV -> {
                categoryDialog = Dialog(this)
                categoryDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                categoryDialog.setContentView(R.layout.category_dialog)

                val categoriesRV = categoryDialog.findViewById(R.id.categoriesRV) as RecyclerView
                categoriesRV.layoutManager = LinearLayoutManager(this)
                val vspselectCategoryAdapter = VSPSelectCategoriesAdapter(this, categories, this)
                categoriesRV.adapter = vspselectCategoryAdapter

                categoryDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                categoryDialog.setCanceledOnTouchOutside(false)
                categoryDialog.show()
                val window = categoryDialog.window!!
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            R.id.closeIB -> {
                searchET.setText("")
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchET.windowToken, 0)

                productsList.clear()
                mainProductsList.clear()
                currentPage = 0

                loadDataFor = "main"
                loadMainAPI()

                val slideOut: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_out_right)
                closeIB.startAnimation(slideOut)
                closeIB.visibility = View.GONE
            }
            R.id.addProductLayout -> {
                val intent = Intent(this, ProductSelectionActivity::class.java)
                intent.putExtra("from", "edit")
                startActivity(intent)
                finish()
            }
        }
    }

    fun receiveClickListener(category: String) {
        categoryDialog.dismiss()
        categoryNameTV.text = category

        if (categoryList.size > 1) {
            if (category != "All") {
                noProductsLayout.visibility = View.GONE
                productsList.clear()
                mainProductsList.clear()
                currentPage = 0

                loadDataFor = "category"
                loadCategoryAPI(category)

            } else {
                productsList.clear()
                mainProductsList.clear()
                currentPage = 0

                loadDataFor = "main"
                loadMainAPI()
            }
        }
    }

    private fun loadMainAPI() {
        progressLayout!!.visibility = View.VISIBLE
        val obj = JSONObject()
        obj.put("supplier_id", shared!!.getString("id", ""))
        obj.put("page_no", currentPage++)
        Constants.repository!!.getProductsBySupplierID(obj.toString())
    }

    private fun loadSearchAPI() {
        progressLayout.visibility = View.VISIBLE
        val obj = JSONObject()
        obj.put("supplier_id", shared!!.getString("id", ""))
        obj.put("search_term", searchET.text.toString().trim())
        obj.put("page_no", currentPage++)
        Constants.repository!!.getSearchFilteredProductsList(obj.toString())
    }

    private fun loadCategoryAPI(category: String) {
        progressLayout.visibility = View.VISIBLE
        val obj = JSONObject()
        obj.put("supplier_id", shared!!.getString("id", ""))
        obj.put("category", category)
        obj.put("page_no", currentPage++)
        Constants.repository!!.getCategoryFilteredProductsList(obj.toString())
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
                                "category" -> loadCategoryAPI(categoryNameTV.text.toString().trim())
                                "search" -> loadSearchAPI()
                            }
                        }
                    }
                }
            }
        })
    }
}
