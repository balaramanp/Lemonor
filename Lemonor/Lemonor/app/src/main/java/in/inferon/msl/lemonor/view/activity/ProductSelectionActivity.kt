package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.pojo.Category
import `in`.inferon.msl.lemonor.model.pojo.Products
import `in`.inferon.msl.lemonor.model.pojo.Unit
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.adapter.*
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
import android.widget.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_place_order.*
import kotlinx.android.synthetic.main.activity_product_selection.*
import kotlinx.android.synthetic.main.activity_product_selection.backIB
import kotlinx.android.synthetic.main.activity_product_selection.categoryNameTV
import kotlinx.android.synthetic.main.activity_product_selection.closeIB
import kotlinx.android.synthetic.main.activity_product_selection.progressLayout
import kotlinx.android.synthetic.main.activity_product_selection.recyclerView
import kotlinx.android.synthetic.main.activity_product_selection.searchET
import org.json.JSONObject
import java.lang.Exception

class ProductSelectionActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = ProductSelectionActivity::class.java.simpleName
    private var repo: Repository? = null
    private var goodsList = mutableListOf<Products>()
    private var mainGoodsList = mutableListOf<Products>()
    private var unitList = mutableListOf<Unit>()
    private var categoryList = mutableListOf<Category>()
    private var productsAdapter: ProductsAdapter? = null
    private var supplierEditProductsAdapter: SupplierEditProductsAdapter? = null
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    private var from = ""
    private var categoryTV: TextView? = null
    private var checkBox: CheckBox? = null
    private var categoryNameET: EditText? = null
    private var unitTV: TextView? = null
    private var unitCheckBox: CheckBox? = null
    private var unitNameET: EditText? = null
    private lateinit var unitDialog: Dialog
    private lateinit var categoryDialog: Dialog
    private var categoryName = mutableListOf<String>()
    private var dialog: Dialog? = null
    private var currentPage = 0
    private var isLoading = false
    private var isLastPage = false
    private var isFirstTimeData = true
    private var loadDataFor = "main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_selection)


        LocalBroadcastManager.getInstance(this)
            .registerReceiver(selectedCategory, IntentFilter("SelectedCategory"))

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(selectedUnit, IntentFilter("SelectedUnit"))

        /*LocalBroadcastManager.getInstance(this)
            .registerReceiver(selectCategory, IntentFilter("SelectCategory"))*/

        from = intent.getStringExtra("from")

        repo = Repository()
        shared = getSharedPreferences(PREF, MODE_PRIVATE)

        searchET.isEnabled = false
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        if (!isConnected) {
//            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, NoInternetActivity::class.java)
            startActivity(intent)
        } else {
            Log.e(TAG, "Supplier ID : " + shared!!.getString("id", ""))
            progressLayout!!.visibility = View.VISIBLE
            val initObj = JSONObject()
            repo!!.getInitData(initObj.toString())
        }


        repo!!.getInitData.observe(this, androidx.lifecycle.Observer {
            run {
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {

                    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                    val isConnected: Boolean = activeNetwork?.isConnected == true
                    Log.e(TAG, "Network Connectivity : $isConnected")
                    if (!isConnected) {
                        Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                    } else {
                        if (from == "edit") {
                            loadEditMainAPI()
                        } else {
                            loadRegisterMainAPI()
                        }
                    }

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
                }
            }
        })


        repo!!.getProductsListForSupplierToEdit.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout!!.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    goodsList =
                        Gson().fromJson(
                            jsonObject.getString("products_list"),
                            object : TypeToken<MutableList<Products>>() {}.type
                        )

                    mainGoodsList.addAll(goodsList)

                    if (isFirstTimeData) {
                        categoryName = Gson().fromJson(
                            jsonObject.getString("categories"),
                            object : TypeToken<MutableList<String>>() {}.type
                        )
                        categoryName.reverse()
                        categoryName.add("All")
                        categoryName.reverse()
                        categoryNameTV.visibility = View.VISIBLE
                        categoryNameTV.text = categoryName[0]

                        if (from == "edit") {
                            recyclerView.layoutManager = LinearLayoutManager(this)
                            supplierEditProductsAdapter =
                                SupplierEditProductsAdapter(this, mainGoodsList, unitList, this)
                            recyclerView.adapter = supplierEditProductsAdapter
                        } else {
                            recyclerView.layoutManager = LinearLayoutManager(this)
                            productsAdapter = ProductsAdapter(this, mainGoodsList, unitList, this)
                            recyclerView.adapter = productsAdapter
                        }

                        searchET.isEnabled = true
                        isFirstTimeData = false
                    }


                    if (mainGoodsList.size > 0) {
                        supplierEditProductsAdapter!!.notifyDataSetChanged()
                        isLoading = false
                    } else {
                        Toast.makeText(this, "No Products Available!", Toast.LENGTH_SHORT).show()
                    }

                }
            }
        })

        repo!!.getGoodsList.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout!!.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    goodsList =
                        Gson().fromJson(
                            jsonObject.getString("products_list"),
                            object : TypeToken<MutableList<Products>>() {}.type
                        )
                    mainGoodsList.addAll(goodsList)

                    if (isFirstTimeData) {
                        categoryName = Gson().fromJson(
                            jsonObject.getString("categories"),
                            object : TypeToken<MutableList<String>>() {}.type
                        )
                        categoryName.reverse()
                        categoryName.add("All")
                        categoryName.reverse()
                        categoryNameTV.visibility = View.VISIBLE
                        categoryNameTV.text = categoryName[0]

                        if (from == "edit") {
                            recyclerView.layoutManager = LinearLayoutManager(this)
                            supplierEditProductsAdapter =
                                SupplierEditProductsAdapter(this, mainGoodsList, unitList, this)
                            recyclerView.adapter = supplierEditProductsAdapter
                        } else {
                            recyclerView.layoutManager = LinearLayoutManager(this)
                            productsAdapter = ProductsAdapter(this, mainGoodsList, unitList, this)
                            recyclerView.adapter = productsAdapter
                        }

                        isFirstTimeData = false
                    }

                    if (mainGoodsList.size > 0) {
                        productsAdapter!!.notifyDataSetChanged()
                        isLoading = false
                    } else {
                        Toast.makeText(this, "No Products Available!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })


        repo!!.getProductsListBySearchTermForSupplierToEdit.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    if (currentPage == 1) {
                        goodsList.clear()
                        mainGoodsList.clear()
                    }
                    goodsList =
                        Gson().fromJson(
                            jsonObject.getString("products_list"),
                            object : TypeToken<MutableList<Products>>() {}.type
                        )
                    mainGoodsList.addAll(goodsList)

                    supplierEditProductsAdapter!!.notifyDataSetChanged()
                    isLoading = false

                } else if (jsonObject.getString("status") == "error") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                }
            }
        })

        repo!!.getGoodsListBySearchTerm.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    if (currentPage == 1) {
                        goodsList.clear()
                        mainGoodsList.clear()
                    }
                    goodsList =
                        Gson().fromJson(
                            jsonObject.getString("products_list"),
                            object : TypeToken<MutableList<Products>>() {}.type
                        )
                    mainGoodsList.addAll(goodsList)

                    productsAdapter!!.notifyDataSetChanged()
                    isLoading = false

                } else if (jsonObject.getString("status") == "error") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                }
            }
        })

        repo!!.getProductsListByCategoryFilterForSupplierToEdit.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    goodsList =
                        Gson().fromJson(
                            jsonObject.getString("products_list"),
                            object : TypeToken<MutableList<Products>>() {}.type
                        )
                    mainGoodsList.addAll(goodsList)

                    supplierEditProductsAdapter!!.notifyDataSetChanged()
                    isLoading = false

                } else if (jsonObject.getString("status") == "error") {
                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                }
            }
        })

        repo!!.getGoodsListByCategoryFilter.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    goodsList =
                        Gson().fromJson(
                            jsonObject.getString("products_list"),
                            object : TypeToken<MutableList<Products>>() {}.type
                        )
                    mainGoodsList.addAll(goodsList)

                    productsAdapter!!.notifyDataSetChanged()
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
                            AnimationUtils.loadAnimation(this@ProductSelectionActivity, R.anim.slide_in_left)
                        closeIB.startAnimation(slideIn)
                        animateFirstTime = false
                    }
                } else {
                    val slideOut: Animation =
                        AnimationUtils.loadAnimation(this@ProductSelectionActivity, R.anim.slide_out_left)
                    closeIB.startAnimation(slideOut)
                    closeIB.visibility = View.GONE
                    animateFirstTime = true
                }

                if (categoryList.size > 0) {
                    categoryNameTV.visibility = View.VISIBLE
                    categoryNameTV.text = categoryName[0]
                } else {
                    categoryNameTV.visibility = View.GONE
                }

                if (from == "edit") {
                    if (searchET.text.toString().trim().length > 2) {
                        goodsList.clear()
                        mainGoodsList.clear()
                        currentPage = 0

                        loadDataFor = "search"
                        loadEditSearchAPI()
                    } else if (searchET.text.toString().trim().isEmpty()) {
                        goodsList.clear()
                        mainGoodsList.clear()
                        currentPage = 0

                        loadDataFor = "main"
                        loadEditMainAPI()
                    }
                } else if (from == "register") {
                    if (searchET.text.toString().trim().length > 2) {
                        goodsList.clear()
                        mainGoodsList.clear()
                        currentPage = 0

                        loadDataFor = "search"
                        loadRegisterSearchAPI()
                    } else if (searchET.text.toString().trim().isEmpty()) {
                        goodsList.clear()
                        mainGoodsList.clear()
                        currentPage = 0

                        loadDataFor = "main"
                        loadRegisterMainAPI()
                    }
                }
            }
        })

        backIB.setOnClickListener(this)
        categoryNameTV.setOnClickListener(this)
        fabIB.setOnClickListener(this)
        closeIB.setOnClickListener(this)
    }

    private fun loadEditMainAPI() {
        progressLayout.visibility = View.VISIBLE
        val obj = JSONObject()
        obj.put("supplier_id", shared!!.getString("id", ""))
        obj.put("page_no", currentPage++)
        repo!!.getProductsListForSupplierToEdit(obj.toString())
    }

    private fun loadRegisterMainAPI() {
        progressLayout.visibility = View.VISIBLE
        val obj = JSONObject()
        obj.put("page_no", currentPage++)
        repo!!.getGoodsList(obj.toString())
    }

    private fun loadEditSearchAPI() {
        progressLayout.visibility = View.VISIBLE
        val obj = JSONObject()
        obj.put("supplier_id", shared!!.getString("id", ""))
        obj.put("search_term", searchET.text.toString().trim())
        obj.put("page_no", currentPage++)
        repo!!.getProductsListBySearchTermForSupplierToEdit(obj.toString())
    }

    private fun loadRegisterSearchAPI() {
        progressLayout.visibility = View.VISIBLE
        val obj = JSONObject()
        obj.put("search_term", searchET.text.toString().trim())
        obj.put("page_no", currentPage++)
        repo!!.getGoodsListBySearchTerm(obj.toString())
    }

    private fun loadEditCategoryAPI(category: String) {
        progressLayout.visibility = View.VISIBLE
        val obj = JSONObject()
        obj.put("supplier_id", shared!!.getString("id", ""))
        obj.put("category", category)
        obj.put("page_no", currentPage++)
        repo!!.getProductsListByCategoryFilterForSupplierToEdit(obj.toString())
    }

    private fun loadRegisterCategoryAPI(category: String) {
        progressLayout.visibility = View.VISIBLE
        val obj = JSONObject()
        obj.put("category", category)
        obj.put("page_no", currentPage++)
        repo!!.getGoodsListByCategoryFilter(obj.toString())
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backIB -> {
                if (from == "register") {
                    loadSupplierFragment()
                }
                super.onBackPressed()
            }
            R.id.categoryNameTV -> {
                dialog = Dialog(this)
                dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog!!.setContentView(R.layout.category_dialog)

                val categoriesRV = dialog!!.findViewById(R.id.categoriesRV) as RecyclerView
                categoriesRV.layoutManager = LinearLayoutManager(this)
                val psaselectCategoryAdapter = PSASelectCategoriesAdapter(this, categoryName, this)
                categoriesRV.adapter = psaselectCategoryAdapter

                dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog!!.setCanceledOnTouchOutside(false)
                dialog!!.show()
                val window = dialog!!.window!!
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            R.id.fabIB -> {
                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.add_new_product_dialog)

                val productNameET = dialog.findViewById(R.id.productNameET) as EditText
                val aliasNameET = dialog.findViewById(R.id.aliasNameET) as EditText
                categoryTV = dialog.findViewById(R.id.categoryTV) as TextView
                unitTV = dialog.findViewById(R.id.unitTV) as TextView
                val priceET = dialog.findViewById(R.id.priceET) as EditText
                val descriptionET = dialog.findViewById(R.id.descriptionET) as EditText
                val featuredProduct = dialog.findViewById(R.id.featuredProduct) as CheckBox
                val addTV = dialog.findViewById(R.id.addTV) as TextView

                categoryTV!!.setOnClickListener {
                    categoryDialog = Dialog(this)
                    categoryDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    categoryDialog.setContentView(R.layout.categories_dialog)
                    checkBox = categoryDialog.findViewById(R.id.checkBox) as CheckBox
                    categoryNameET = categoryDialog.findViewById(R.id.categoryNameET) as EditText
                    val categoryRV = categoryDialog.findViewById(R.id.categoryRV) as RecyclerView
                    val doneTV = categoryDialog.findViewById(R.id.doneTV) as TextView

                    categoryRV.layoutManager = LinearLayoutManager(this)
                    var categoriesAdapter = CategoriesAdapter(this, categoryList)
                    categoryRV.adapter = categoriesAdapter
                    categoryRV.setHasFixedSize(true)
                    categoryRV.isNestedScrollingEnabled = false

                    checkBox!!.setOnClickListener {
                        if (checkBox!!.isChecked) {
                            categoryNameET!!.visibility = View.VISIBLE
                            doneTV.visibility = View.VISIBLE
                        } else {
                            categoryNameET!!.visibility = View.GONE
                            doneTV.visibility = View.GONE
                        }

                        categoriesAdapter = CategoriesAdapter(this, categoryList)
                        categoryRV.adapter = categoriesAdapter
                        categoryRV.setHasFixedSize(true)
                        categoryRV.isNestedScrollingEnabled = false
                    }

                    doneTV.setOnClickListener {
                        if (checkBox!!.isChecked && categoryNameET!!.text.isNotEmpty()) {
                            categoryTV!!.text = categoryNameET!!.text.toString().trim()
                            categoryNameET!!.setText("")
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
                    unitDialog = Dialog(this)
                    unitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    unitDialog.setContentView(R.layout.units_dialog)

                    unitCheckBox = unitDialog.findViewById(R.id.unitCheckBox) as CheckBox
                    unitNameET = unitDialog.findViewById(R.id.unitNameET) as EditText
                    val unitsRV = unitDialog.findViewById(R.id.unitsRV) as RecyclerView
                    val doneTV = unitDialog.findViewById(R.id.doneTV) as TextView

                    unitsRV.layoutManager = LinearLayoutManager(this)
                    var unitsAdapter = UnitsAdapter(this, unitList, "activity", "not_custom")
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
                        unitsAdapter = UnitsAdapter(this, unitList, "activity", "custom")
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

                addTV.setOnClickListener {
                    if (productNameET.text.isNotEmpty() && categoryTV!!.text.isNotEmpty() && unitTV!!.text.isNotEmpty() &&
                        priceET.text.isNotEmpty()
                    ) {
                        progressLayout.visibility = View.VISIBLE
                        val obj = JSONObject()
                        obj.put("product_id", "")
                        obj.put("product_name", productNameET.text.toString().trim())
                        obj.put("local_name", aliasNameET.text.toString().trim())
                        obj.put("unit", unitTV!!.text.toString().trim())
                        obj.put("description", descriptionET.text.toString().trim())
                        obj.put("category", categoryTV!!.text.toString().trim())
                        obj.put("rate", priceET.text.toString().trim())
                        obj.put("new_product", true)
                        if (featuredProduct.isChecked) {
                            obj.put("featured_product_flag", true)
                        } else {
                            obj.put("featured_product_flag", false)
                        }

                        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                        val isConnected: Boolean = activeNetwork?.isConnected == true
                        if (!isConnected) {
                            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                        } else {
                            val fObj = JSONObject()
                            fObj.put("user_id", shared!!.getString("id", ""))
                            fObj.put("goods_data", obj)
                            repo!!.saveSupplierGoodsData(fObj.toString())
                        }

                        repo!!.saveSupplierGoodsData.observe(this, androidx.lifecycle.Observer {
                            run {
                                progressLayout!!.visibility = View.GONE
                                val jsonObject = JSONObject(it)
                                if (jsonObject.getString("status") == "ok") {
                                    if (from == "edit") {
                                        Toast.makeText(
                                            this,
                                            "Product Added Successfully! Please check View / Edit Products to modify your product.",
                                            Toast.LENGTH_SHORT
                                        ).show()
//                                        supplierEditProductsAdapter!!.notifyDataSetChanged()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Product Added Successfully! Please check View / Edit Products to modify your product.",
                                            Toast.LENGTH_SHORT
                                        ).show()
//                                        productsAdapter!!.notifyDataSetChanged()
                                    }
                                    progressLayout.visibility = View.GONE
                                } else if (jsonObject.getString("status") == "already_exists") {
                                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                } else if (jsonObject.getString("status") == "error") {
                                    Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show()
                                }
                            }
                        })


                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "Please enter all fields!", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
                val window = dialog.window!!
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            R.id.closeIB -> {
                searchET.setText("")
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchET.windowToken, 0)

                loadCategoryData()

                val slideOut: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_out_right)
                closeIB.startAnimation(slideOut)
                closeIB.visibility = View.GONE
            }
        }
    }

    override fun onBackPressed() {
        if (from == "register") {
            loadSupplierFragment()
        }
        super.onBackPressed()
    }

    private fun loadSupplierFragment() {
        val intent = Intent("LoadSupplierFragment")
        intent.putExtra("loadSupplierFragment", "done")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private val selectedCategory = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                checkBox!!.isChecked = false
                categoryNameET!!.setText("")
                categoryNameET!!.visibility = View.GONE


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

    fun receiveClickListener(category: String) {
        dialog!!.dismiss()
        categoryNameTV.text = category

        if (categoryList.size > 1) {
            if (from == "edit") {
                if (category != "All") {
                    goodsList.clear()
                    mainGoodsList.clear()
                    currentPage = 0

                    loadDataFor = "category"
                    loadEditCategoryAPI(category)
                } else {
                    goodsList.clear()
                    mainGoodsList.clear()
                    currentPage = 0

                    loadDataFor = "main"
                    loadEditMainAPI()
                }
            } else {
                if (category != "All") {
                    goodsList.clear()
                    mainGoodsList.clear()
                    currentPage = 0

                    loadDataFor = "category"
                    loadRegisterCategoryAPI(category)
                } else {
                    goodsList.clear()
                    mainGoodsList.clear()
                    currentPage = 0

                    loadDataFor = "main"
                    loadRegisterMainAPI()
                }
            }

        }
    }

    private fun loadCategoryData() {
        if (from == "edit") {
            if (categoryName.size >= 1) {
                categoryNameTV.text = categoryName[0]

                goodsList.clear()
                mainGoodsList.clear()
                currentPage = 0

                loadDataFor = "main"
                loadEditMainAPI()
            }
        } else if (from == "register") {
            if (categoryName.size >= 1) {
                categoryNameTV.text = categoryName[0]

                goodsList.clear()
                mainGoodsList.clear()
                currentPage = 0

                loadDataFor = "main"
                loadRegisterMainAPI()
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
                                "main" -> if (from == "edit") {
                                    loadEditMainAPI()
                                } else {
                                    loadRegisterMainAPI()
                                }
                                "category" -> if (from == "edit") {
                                    loadEditCategoryAPI(categoryNameTV.text.toString().trim())
                                } else {
                                    loadRegisterCategoryAPI(categoryNameTV.text.toString().trim())
                                }
                                "search" -> if (from == "edit") {
                                    loadEditSearchAPI()
                                } else {
                                    loadRegisterSearchAPI()
                                }
                            }
                        }
                    }
                }
            }
        })
    }
}
