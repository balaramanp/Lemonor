package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.pojo.Order
import `in`.inferon.msl.lemonor.model.pojo.OrderList
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.adapter.ClientOrdersAdapter
import android.annotation.SuppressLint
import android.content.*
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_my_orders.*
import kotlinx.android.synthetic.main.activity_my_orders.backIB
import kotlinx.android.synthetic.main.activity_my_orders.progressLayout
import kotlinx.android.synthetic.main.activity_my_orders.recyclerView
import org.json.JSONArray
import org.json.JSONObject

class MyOrdersActivity : AppCompatActivity() {
    private val TAG = MyOrdersActivity::class.java.simpleName
    private var repo: Repository? = null
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    private var ordersList = mutableListOf<OrderList>()
    private var mainOrdersList = mutableListOf<OrderList>()
    private var clientOrdersAdapter: ClientOrdersAdapter? = null
    private var currentPage = 0
    private var isLoading = false
    private var isLastPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(showProgressBar, IntentFilter("ShowProgressBar"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(hideProgressBar, IntentFilter("HideProgressBar"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(updateTotalPrice, IntentFilter("UpdateTotalPrice"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(chat, IntentFilter("Chat"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(updateRating, IntentFilter("UpdateRating"))

        repo = Repository()
        shared = getSharedPreferences(PREF, MODE_PRIVATE)

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        if (!isConnected) {
//            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, NoInternetActivity::class.java)
            startActivity(intent)
        } else {
            recyclerView.layoutManager = LinearLayoutManager(this)
            clientOrdersAdapter = ClientOrdersAdapter(this, mainOrdersList, this)
            recyclerView.adapter = clientOrdersAdapter

            progressLayout!!.visibility = View.VISIBLE
            val obj = JSONObject()
            obj.put("user_id", shared!!.getString("id", ""))
            obj.put("page_no", currentPage++)
            repo!!.getOrderListByUserIdForCustomer(obj.toString())
        }



        repo!!.getOrderListByUserIdForCustomer.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout!!.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    val ordersArray = JSONArray(jsonObject.getString("order_list"))

                    ordersList =
                        Gson().fromJson(
                            ordersArray.toString(),
                            object : TypeToken<MutableList<OrderList>>() {}.type
                        )
                    mainOrdersList.addAll(ordersList)

                    if (mainOrdersList.size > 0) {
                        Log.e(TAG, "Orders List : $ordersList")

                        clientOrdersAdapter!!.notifyDataSetChanged()
                        noOrdersTV.visibility = View.GONE
                        isLoading = false
                    } else {
                        noOrdersTV.visibility = View.VISIBLE
                    }
                }
            }
        })

        setOnScrollListener(recyclerView)

        swipeRefreshLayout.setOnRefreshListener {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnected == true
            if (!isConnected) {
//                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, NoInternetActivity::class.java)
                startActivity(intent)
            } else {
                ordersList.clear()
                mainOrdersList.clear()
                currentPage = 0

                progressLayout!!.visibility = View.VISIBLE
                val obj = JSONObject()
                obj.put("user_id", shared!!.getString("id", ""))
                obj.put("page_no", currentPage++)
                repo!!.getOrderListByUserIdForCustomer(obj.toString())
            }
        }

        backIB.setOnClickListener {
            super.onBackPressed()
        }
    }

    private val showProgressBar = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            progressLayout!!.visibility = View.VISIBLE
        }
    }

    private val hideProgressBar = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            progressLayout!!.visibility = View.GONE
        }
    }

    private val updateTotalPrice = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            clientOrdersAdapter!!.notifyDataSetChanged()
        }
    }

    private val chat = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            clientOrdersAdapter!!.notifyDataSetChanged()
        }
    }

    private val updateRating = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            clientOrdersAdapter!!.notifyDataSetChanged()
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
                        if (visibleItemCount + pastVisibleItems >= totalItemCount
                            && pastVisibleItems >= 0
                            && totalItemCount >= Constants.PAGE_SIZE
                        ) {
                            isLoading = true
                            Log.i(TAG, "onScrolled: REQUESTING FOR PAGE $currentPage")

                            progressLayout!!.visibility = View.VISIBLE
                            val obj = JSONObject()
                            obj.put("user_id", shared!!.getString("id", ""))
                            obj.put("page_no", currentPage++)
                            repo!!.getOrderListByUserIdForCustomer(obj.toString())
                        }
                    }
                }
            }
        })
    }
}
