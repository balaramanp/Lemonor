package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.pojo.OrderHistory
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.adapter.SupplierOrdersHistoryAdapter
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
import kotlinx.android.synthetic.main.activity_supplier_order_history.*
import kotlinx.android.synthetic.main.activity_supplier_order_history.backIB
import kotlinx.android.synthetic.main.activity_supplier_order_history.progressLayout
import kotlinx.android.synthetic.main.activity_supplier_order_history.recyclerView
import kotlinx.android.synthetic.main.activity_supplier_order_history.swipeRefreshLayout
import org.json.JSONArray
import org.json.JSONObject

class SupplierOrderHistoryActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = SupplierOrderHistoryActivity::class.java.simpleName
    private var repo: Repository? = null
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    private var ordersList = mutableListOf<OrderHistory>()
    private var mainOrdersList = mutableListOf<OrderHistory>()
    private var supplierOrdersHistoryAdapter: SupplierOrdersHistoryAdapter? = null
    private var currentPage = 0
    private var isLoading = false
    private var isLastPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supplier_order_history)

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(refreshData, IntentFilter("RefreshData"))

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
            supplierOrdersHistoryAdapter = SupplierOrdersHistoryAdapter(this, mainOrdersList, this)
            recyclerView.adapter = supplierOrdersHistoryAdapter
            recyclerView.visibility = View.GONE

            progressLayout!!.visibility = View.VISIBLE
            val obj = JSONObject()
            obj.put("supplier_id", shared!!.getString("id", ""))
            obj.put("page_no", currentPage++)
            repo!!.getOrderHistoryList(obj.toString())
        }


        repo!!.getOrderHistoryList.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout!!.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
                val jsonObject = JSONObject(it)
                if (jsonObject.getString("status") == "ok") {
                    val ordersArray = JSONArray(jsonObject.getString("order_history_list"))

                    ordersList =
                        Gson().fromJson(
                            ordersArray.toString(),
                            object : TypeToken<MutableList<OrderHistory>>() {}.type
                        )
                    mainOrdersList.addAll(ordersList)

                    if (mainOrdersList.size > 0) {
                        supplierOrdersHistoryAdapter!!.notifyDataSetChanged()
                        recyclerView.visibility = View.VISIBLE
                        supplierStatusLayout.visibility = View.GONE
                        isLoading = false
                    } else {
                        supplierStatusLayout.visibility = View.VISIBLE
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
                obj.put("supplier_id", shared!!.getString("id", ""))
                obj.put("page_no", currentPage++)
                repo!!.getOrderHistoryList(obj.toString())
            }
        }

        backIB.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backIB -> {
                refreshData()
                super.onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        refreshData()
        super.onBackPressed()
    }

    private val refreshData = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            ordersList.clear()
            mainOrdersList.clear()
            currentPage = 0

            progressLayout!!.visibility = View.VISIBLE
            val obj = JSONObject()
            obj.put("supplier_id", shared!!.getString("id", ""))
            obj.put("page_no", currentPage++)
            repo!!.getOrderHistoryList(obj.toString())
        }
    }

    private fun refreshData() {
        val intent = Intent("OrderRefreshData")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
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
                            obj.put("supplier_id", shared!!.getString("id", ""))
                            obj.put("page_no", currentPage++)
                            repo!!.getOrderHistoryList(obj.toString())
                        }
                    }
                }
            }
        })
    }
}
