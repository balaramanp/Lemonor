package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.pojo.Address
import `in`.inferon.msl.lemonor.model.pojo.Products
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.adapter.AddressesSelectionAdapter
import `in`.inferon.msl.lemonor.view.adapter.PlaceOrderAdapter
import `in`.inferon.msl.lemonor.view.adapter.SelectCategoriesAdapter
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.FileProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_place_order.*
import kotlinx.android.synthetic.main.activity_place_order.backIB
import kotlinx.android.synthetic.main.activity_place_order.progressLayout
import kotlinx.android.synthetic.main.activity_place_order.recyclerView
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PlaceOrderActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = PlaceOrderActivity::class.java.simpleName
    private var repo: Repository? = null
    private var supplier_id = ""
    private var shopName = ""
    private var from = ""
    private var productsList = mutableListOf<Products>()
    private var mainProductsList = mutableListOf<Products>()
    private var orderedProductsList = mutableListOf<Products>()
    private var categoryList = mutableListOf<String>()
    private var placeOrderAdapter: PlaceOrderAdapter? = null
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var currentPhotoPath: String
    private var totalPrice = 0f
    private lateinit var categoryDialog: Dialog
    private var currentPage = 0
    private var isLoading = false
    private var isLastPage = false
    private var isFirstTimeData = true
    private var loadDataFor = "main"
    private var addresses = mutableListOf<Address>()
    private var addressSelectionAdapter: AddressesSelectionAdapter? = null
    private var addressSelectionDialogVisible = false
    private var addressesRV: RecyclerView? = null
    private var selectedAddressID = ""
    private var selectedAddressPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_order)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(plusReceiver, IntentFilter("PlusReceiver"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(minusReceiver, IntentFilter("MinusReceiver"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(orderPlacedSuccessfully, IntentFilter("OrderPlacedSuccessfully"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(addEditAddress, IntentFilter("AddEditAddress"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(selectedAddress, IntentFilter("SelectedAddress"))

        repo = Repository()
        shared = getSharedPreferences(PREF, MODE_PRIVATE)

        val animation: Animation = AnimationUtils.loadAnimation(this, R.anim.zoom_in_out)
        editIV.startAnimation(animation)

        supplier_id = intent.getStringExtra("supplier_id")!!
        shopName = intent.getStringExtra("shop_name")!!
        try {
            from = intent.getStringExtra("from")!!
        } catch (e: Exception) {

        }

        titleTV.text = shopName
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        if (!isConnected) {
//            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, NoInternetActivity::class.java)
            startActivity(intent)
        } else {
            val obj = JSONObject()
            obj.put("user_id", shared!!.getString("id", ""))
            repo!!.getUserProfileByUserID(obj.toString())

            loadCategoryData()
            progressLayout.visibility = View.VISIBLE
            loadMainAPI()
        }

        repo!!.getUserProfileByUserID.observe(this, androidx.lifecycle.Observer {
            progressLayout.visibility = View.GONE
            val jsonObject = JSONObject(it)
            addresses =
                Gson().fromJson(
                    jsonObject.getString("addresses"),
                    object : TypeToken<MutableList<Address>>() {}.type
                )

            if (addressSelectionDialogVisible) {
                addressesRV!!.layoutManager = LinearLayoutManager(this)
                addressSelectionAdapter = AddressesSelectionAdapter(this, addresses, this)
                addressesRV!!.adapter = addressSelectionAdapter
            }
        })

        repo!!.getProductsListBySupplierIdForCustomer.observe(this, androidx.lifecycle.Observer {
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

                    if (isFirstTimeData) {
                        categoryList =
                            Gson().fromJson(
                                jsonObject.getString("categories"),
                                object : TypeToken<MutableList<String>>() {}.type
                            )
                        categoryList.reverse()
                        categoryList.add("All")
                        categoryList.reverse()
                        categoryNameTV.visibility = View.VISIBLE
                        categoryNameTV.text = categoryList[0]
                        isFirstTimeData = false
                    }

                    if (shopName == "") {
                        if (jsonObject.getString("shop_name") != "null") {
                            titleTV.text = jsonObject.getString("shop_name")
                        }
                    }

                    if (shared!!.getBoolean("typing_animation", false)) {
                        o2ET.isEnabled = false
                        orderBT.isClickable = false
                        loadTypingAnimation()
                    }

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

                    placeOrderAdapter!!.notifyDataSetChanged()
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
                            AnimationUtils.loadAnimation(this@PlaceOrderActivity, R.anim.slide_in_left)
                        closeIB.startAnimation(slideIn)
                        animateFirstTime = false
                    }
                } else {
                    val slideOut: Animation =
                        AnimationUtils.loadAnimation(this@PlaceOrderActivity, R.anim.slide_out_left)
                    closeIB.startAnimation(slideOut)
                    closeIB.visibility = View.GONE
                    animateFirstTime = true
                }

                if (categoryList.size > 0) {
                    categoryNameTV.visibility = View.VISIBLE
                    categoryNameTV.text = categoryList[0]
                } else {
                    categoryNameTV.visibility = View.GONE
                }

                if (searchET.text.toString().trim().length > 2) {
                    productsList.clear()
                    mainProductsList.clear()
                    currentPage = 0

                    loadDataFor = "search"

                    progressLayout.visibility = View.VISIBLE
                    loadSearchAPI()
                } else if (searchET.text.toString().trim().isEmpty()) {
                    productsList.clear()
                    mainProductsList.clear()
                    currentPage = 0

                    loadDataFor = "main"

                    progressLayout.visibility = View.VISIBLE
                    loadMainAPI()
                }

            }
        })

        backIB.setOnClickListener(this)
        imageIB.setOnClickListener(this)
        orderBT.setOnClickListener(this)
        categoryNameTV.setOnClickListener(this)
        closeIB.setOnClickListener(this)
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            /*val imageBitmap = data!!.extras.get("data") as Bitmap
            imageView.visibility = View.VISIBLE
            imageView.setImageBitmap(imageBitmap)*/
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backIB -> {
                backButtonClick()
            }
            R.id.imageIB -> {
                dispatchTakePictureIntent()
            }
            R.id.categoryNameTV -> {
                categoryDialog = Dialog(this)
                categoryDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                categoryDialog.setContentView(R.layout.category_dialog)

                val categoriesRV = categoryDialog.findViewById(R.id.categoriesRV) as RecyclerView
                categoriesRV.layoutManager = LinearLayoutManager(this)
                val selectCategoryAdapter = SelectCategoriesAdapter(this, categoryList, this)
                categoriesRV.adapter = selectCategoryAdapter

                categoryDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                categoryDialog.setCanceledOnTouchOutside(false)
                categoryDialog.show()
                val window = categoryDialog.window!!
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            R.id.orderBT -> {
                val connectivityManager =
                    getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                val isConnected: Boolean = activeNetwork?.isConnected == true
                if (!isConnected) {
                    Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                } else {
                    var itemAvailable = false
                    var itemListAvailable = false
                    var itemShort = false
                    var zeroPriceItem = false

                    if (o2ET.text.toString().trim().length >= 3) {
                        itemAvailable = true
                    } else if (o2ET.text.toString().trim().length in 1..2) {
                        itemShort = true
                    }

                    if (orderedProductsList.size > 0) {
//                        itemAvailable = true
                        itemListAvailable = true
                    }

                    for (i in orderedProductsList) {
                        if (i.rate == "0") {
                            zeroPriceItem = true
                        }
                    }


                    if (!itemShort) {
                        if (itemAvailable || itemListAvailable) {
                            if (totalPrice >= 100f) {
                                /*val intent = Intent(this, OrderConfirmationActivity::class.java)
                                intent.putExtra("supplier_id", supplier_id)
                                intent.putExtra("shop_name", shopName)
                                intent.putExtra("o2", o2ET.text.toString().trim())
                                intent.putExtra("productsList", Gson().toJson(orderedProductsList))
                                startActivity(intent)*/

                                showAddressSelectionDialog()

                                /*val imageViewPair = Pair.create<View?, String?>(totalPriceTV, "totalPrice")
                                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, imageViewPair)
                                startActivity(intent, options.toBundle())*/
                            } else {
                                showOrderValueDialog(itemAvailable, itemListAvailable, zeroPriceItem)
                            }
                        } else {
                            Toast.makeText(this, "Please select any item to place order!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Open Order text is too short!", Toast.LENGTH_SHORT).show()
                    }
                }

            }

            R.id.closeIB -> {
                searchET.setText("")
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchET.windowToken, 0)

                productsList.clear()
                mainProductsList.clear()
                currentPage = 0

                loadDataFor = "main"

                progressLayout.visibility = View.VISIBLE
                loadMainAPI()

                val slideOut: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_out_right)
                closeIB.startAnimation(slideOut)
                closeIB.visibility = View.GONE
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "in.inferon.msl.lemonor.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private val plusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            var inOrderProductsList = false
            for (i in mainProductsList) {
                if (i.product_id == intent.getStringExtra("product_id")) {
                    for (j in orderedProductsList) {
                        if (j.product_id == i.product_id) {
                            inOrderProductsList = true
                            j.qty = intent.getStringExtra("qty")
                        }
                    }

                    if (!inOrderProductsList) {
                        orderedProductsList.add(i)
                    }
                }
            }
            totalPrice += intent.getFloatExtra("value", 0f)
            totalPriceTV.text = doubleToStringNoDecimal(totalPrice.toDouble())
        }
    }

    private val minusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getStringExtra("qty") == "0") {
                for (i in mainProductsList) {
                    if (i.product_id == intent.getStringExtra("product_id")) {
                        orderedProductsList.remove(i)
                    }
                }
            } else {
                for (i in mainProductsList) {
                    if (i.product_id == intent.getStringExtra("product_id")) {
                        for (j in orderedProductsList) {
                            if (j.product_id == i.product_id) {
                                j.qty = intent.getStringExtra("qty")
                            }
                        }
                    }
                }
            }
            totalPrice -= intent.getFloatExtra("value", 0f)
            totalPriceTV.text = doubleToStringNoDecimal(totalPrice.toDouble())
        }
    }

    private val orderPlacedSuccessfully = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish()
        }
    }

    fun receiveClickListener(category: String) {
        categoryDialog.dismiss()
        categoryNameTV.text = category

        if (categoryList.size > 1) {
            if (category != "All") {
                productsList.clear()
                mainProductsList.clear()
                currentPage = 0

                loadDataFor = "category"

                progressLayout.visibility = View.VISIBLE
                loadCategoryAPI(category)
            } else {
                productsList.clear()
                mainProductsList.clear()
                currentPage = 0

                loadDataFor = "main"

                progressLayout.visibility = View.VISIBLE
                loadMainAPI()
            }
        }
    }

    private fun doubleToStringNoDecimal(d: Double): String? {
        val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,##,###.##")
        return formatter.format(d)
    }

    override fun onBackPressed() {
        backButtonClick()
    }

    private fun backButtonClick() {
        var itemSelected = false
        for (i in productsList) {
            if (i.qty != null && i.qty.toInt() > 0) {
                itemSelected = true
            }
        }

        if (itemSelected) {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.cancel_order_dialog)

            val cancelBT = dialog.findViewById(R.id.cancelBT) as Button
            val okBT = dialog.findViewById(R.id.okBT) as Button

            cancelBT.setOnClickListener {
                dialog.dismiss()
            }

            okBT.setOnClickListener {
                super.onBackPressed()
            }
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            val window = dialog.window!!
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        } else {
            if (from == "login_page") {
                val intent = Intent(this, MainFragmentActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun loadCategoryData() {
        recyclerView.layoutManager = LinearLayoutManager(this@PlaceOrderActivity)
        placeOrderAdapter =
            PlaceOrderAdapter(this@PlaceOrderActivity, mainProductsList, this@PlaceOrderActivity, orderedProductsList)
        recyclerView.adapter = placeOrderAdapter
    }

    //    private val typeText = "Apple 1KG ( Good Quality, Bigger Size ) \n\nஎலும்பிச்சை பழம் - 10 ( நன்கு பழுத்தது )"
//    private val typeText = "Type anything to Place your Order \n\nஇங்கு தேவையான பொருட்களை உள்ளீடு செய்யவும்"
    private val typeText = "Type here to request Order by plain text"
    private var pos = 0
    private fun loadTypingAnimation() {
        Handler().postDelayed({
            if (pos < typeText.length) {
                val newText = o2ET.text.toString() + typeText[pos]
                o2ET.setText(newText)
                o2ET.isCursorVisible = true
                pos += 1
                loadTypingAnimation()
            } else {
                Handler().postDelayed({
                    o2ET.setText("")
                    o2ET.isEnabled = true
                    orderBT.isClickable = true
                    o2ET.requestFocus()
                    val editor = getSharedPreferences(PREF, MODE_PRIVATE).edit()
                    editor.putBoolean("typing_animation", false)
                    editor.apply()
                }, 500)
            }
        }, 50)
    }

    private fun loadMainAPI() {
        val obj = JSONObject()
        obj.put("supplier_id", supplier_id)
        obj.put("page_no", currentPage++)
        repo!!.getProductsListBySupplierIdForCustomer(obj.toString())
    }

    private fun loadSearchAPI() {
        val obj = JSONObject()
        obj.put("supplier_id", supplier_id)
        obj.put("search_term", searchET.text.toString().trim())
        obj.put("page_no", currentPage++)
        repo!!.getSearchFilteredProductsList(obj.toString())
    }

    private fun loadCategoryAPI(category: String) {
        val obj = JSONObject()
        obj.put("supplier_id", supplier_id)
        obj.put("category", category)
        obj.put("page_no", currentPage++)
        repo!!.getCategoryFilteredProductsList(obj.toString())
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

    private fun showOrderValueDialog(
        itemAvailable: Boolean,
        itemListAvailable: Boolean,
        zeroPriceItem: Boolean
    ) {
        val orderValueDialog = Dialog(this)
        orderValueDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        orderValueDialog.setContentView(R.layout.order_value_dialog)

        val contentTV = orderValueDialog.findViewById(R.id.contentTV) as TextView
        val cancelBT = orderValueDialog.findViewById(R.id.cancelBT) as Button
        val okBT = orderValueDialog.findViewById(R.id.okBT) as Button
        val continueBT = orderValueDialog.findViewById(R.id.continueBT) as Button

        if (itemAvailable || zeroPriceItem) {
            contentTV.text = resources.getString(R.string.continue_txt)
            okBT.visibility = View.GONE
            cancelBT.visibility = View.VISIBLE
            continueBT.visibility = View.VISIBLE
        } else if (!itemAvailable && !zeroPriceItem) {
            contentTV.text = resources.getString(R.string.minimum_value_txt)
            cancelBT.visibility = View.GONE
            continueBT.visibility = View.GONE
            okBT.visibility = View.VISIBLE
        }

        okBT.setOnClickListener {
            orderValueDialog.dismiss()
        }

        cancelBT.setOnClickListener {
            orderValueDialog.dismiss()
        }

        continueBT.setOnClickListener {
            /*val intent = Intent(this, OrderConfirmationActivity::class.java)
            intent.putExtra("supplier_id", supplier_id)
            intent.putExtra("shop_name", shopName)
            intent.putExtra("o2", o2ET.text.toString().trim())
            intent.putExtra("productsList", Gson().toJson(orderedProductsList))
            intent.putExtra("selectedAddressID", "")
            startActivity(intent)*/
            showAddressSelectionDialog()
            orderValueDialog.dismiss()
        }

        orderValueDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        orderValueDialog.setCanceledOnTouchOutside(false)
        orderValueDialog.show()
        val window = orderValueDialog.window!!
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun showAddressSelectionDialog() {
        addressSelectionDialogVisible = true
        selectedAddressID = ""

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.address_selection_dialog)

        val addNewAddressLayout = dialog.findViewById(R.id.addNewAddressLayout) as TextView
        val cancelBT = dialog.findViewById(R.id.cancelBT) as Button
        val okBT = dialog.findViewById(R.id.okBT) as Button
        addressesRV = dialog.findViewById(R.id.addressesRV) as RecyclerView

        addressesRV!!.layoutManager = LinearLayoutManager(this)
        addressSelectionAdapter = AddressesSelectionAdapter(this, addresses, this)
        addressesRV!!.adapter = addressSelectionAdapter

        addNewAddressLayout.setOnClickListener {
            addressSelectionDialogVisible = true
            val intent = Intent(this@PlaceOrderActivity, AddAddressActivity::class.java)
            intent.putExtra("from", "add")
            startActivity(intent)
        }

        cancelBT.setOnClickListener {
            selectedAddressID = ""
            addressSelectionDialogVisible = false
            dialog.dismiss()
        }

        okBT.setOnClickListener {
            Log.e(TAG, "Selected Address ID : $selectedAddressID")
            if (selectedAddressID != "") {
                addressSelectionDialogVisible = false
//                dialog.dismiss()

                val intent = Intent(this, OrderConfirmationActivity::class.java)
                intent.putExtra("supplier_id", supplier_id)
                intent.putExtra("shop_name", shopName)
                intent.putExtra("o2", o2ET.text.toString().trim())
                intent.putExtra("productsList", Gson().toJson(orderedProductsList))
                intent.putExtra("selectedAddressID", selectedAddressID)
                intent.putExtra("selectedAddress", Gson().toJson(addresses[selectedAddressPosition]))
                startActivity(intent)
            } else {
                Toast.makeText(this@PlaceOrderActivity, "Please Select Address!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val window = dialog.window!!
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private val addEditAddress = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            progressLayout.visibility = View.VISIBLE
            val obj = JSONObject()
            obj.put("user_id", shared!!.getString("id", ""))
            repo!!.getUserProfileByUserID(obj.toString())
        }
    }

    private val selectedAddress = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            selectedAddressPosition = intent.getIntExtra("position", 0)
            selectedAddressID = addresses[intent.getIntExtra("position", 0)].id
        }
    }


}
