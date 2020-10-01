package `in`.inferon.msl.lemonor.view.fragment

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.pojo.Order
import `in`.inferon.msl.lemonor.model.pojo.OrderList
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.activity.MainFragmentActivity
import `in`.inferon.msl.lemonor.view.adapter.SupplierOrdersAdapter
import android.app.Dialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.container_main_fragment.*
import kotlinx.android.synthetic.main.navigation_layout.*
import kotlinx.android.synthetic.main.supplier_fragment.*
import org.json.JSONArray
import org.json.JSONObject

class SupplierFragment : Fragment() {
    private val TAG = SupplierFragment::class.java.simpleName
    private var repo: Repository? = null
    var supplierStatusLayout: RelativeLayout? = null
    var ordersLayout: LinearLayout? = null
    private var progressLayout: LinearLayout? = null
    private var rvOverView: View? = null
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null
    private var ordersList = mutableListOf<OrderList>()
    private var mainOrdersList = mutableListOf<OrderList>()
    private var supplierOrdersAdapter: SupplierOrdersAdapter? = null
    private var currentPage = 0
    private var isLoading = false
    private var isLastPage = false
    private var isSubSupplier = false
    private var subSupplierID = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.supplier_fragment, null)
        init(view)
        return view
    }

    private fun init(view: View) {
        repo = Repository()
        shared = context!!.getSharedPreferences(PREF, AppCompatActivity.MODE_PRIVATE)
        LocalBroadcastManager.getInstance(context!!)
            .registerReceiver(showProgressBar, IntentFilter("ShowProgressBar"))
        LocalBroadcastManager.getInstance(context!!)
            .registerReceiver(hideProgressBar, IntentFilter("HideProgressBar"))
        LocalBroadcastManager.getInstance(context!!)
            .registerReceiver(showNoOrdersLayout, IntentFilter("ShowNoOrdersLayout"))
        LocalBroadcastManager.getInstance(context!!)
            .registerReceiver(refreshData, IntentFilter("RefreshData"))

        val swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout) as SwipeRefreshLayout
        val recyclerView = view.findViewById(R.id.recyclerView) as RecyclerView
        supplierStatusLayout = view.findViewById(R.id.supplierStatusLayout) as RelativeLayout
        ordersLayout = view.findViewById(R.id.ordersLayout) as LinearLayout
        progressLayout = view.findViewById(R.id.progressLayout) as LinearLayout
        rvOverView = view.findViewById(R.id.rvOverView) as View

        val connectivityManager = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        if (!isConnected) {
            Toast.makeText(context, "No Internet Connection!", Toast.LENGTH_SHORT).show()
        } else {
            recyclerView.layoutManager = LinearLayoutManager(context)
            supplierOrdersAdapter =
                SupplierOrdersAdapter(context!!, mainOrdersList, context!! as MainFragmentActivity)
            recyclerView.adapter = supplierOrdersAdapter

            progressLayout!!.visibility = View.VISIBLE
            val obj = JSONObject()
            obj.put("user_id", shared!!.getString("id", ""))
            repo!!.getSupplierIdForStaff(obj.toString())
        }


        repo!!.getSupplierIdForStaff.observe(this, androidx.lifecycle.Observer {
            val jsonObject = JSONObject(it)
            if (jsonObject.getBoolean("is_staff")) {
                isSubSupplier = true
                subSupplierID = jsonObject.getString("supplier_id")

                val obj = JSONObject()
                obj.put("supplier_id", subSupplierID)
                obj.put("page_no", currentPage++)
                repo!!.getOrderListBySupplierID(obj.toString())
            } else {
                if (shared!!.getString("shop_name", "") != "") {
                    val obj = JSONObject()
                    obj.put("supplier_id", shared!!.getString("id", ""))
                    obj.put("page_no", currentPage++)
                    repo!!.getOrderListBySupplierID(obj.toString())
                }
            }
        })

        repo!!.getOrderListBySupplierID.observe(this, androidx.lifecycle.Observer {
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
                        ordersLayout!!.visibility = View.VISIBLE
                        supplierStatusLayout!!.visibility = View.GONE

                        Log.e(TAG, "Orders List : $ordersList")
                        supplierOrdersAdapter!!.notifyDataSetChanged()
                        isLoading = false
                        recyclerView.visibility = View.VISIBLE
                    } else {
                        ordersLayout!!.visibility = View.GONE
                        supplierStatusLayout!!.visibility = View.VISIBLE
                    }

                } else if (jsonObject.getString("status") == "not_approved") {
                    notApprovedLayout.visibility = View.VISIBLE
                    notApprovedTV.text = jsonObject.getString("msg")
                    notApprovedContactNoTV.text = jsonObject.getString("contact_no")


                    notApprovedCallIB.setOnClickListener {

                        val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.login_bt)
                        notApprovedCallIB.startAnimation(animation)

                        animation.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationRepeat(animation: Animation?) {
                            }

                            override fun onAnimationEnd(animation: Animation?) {
                                val dialog = Dialog(context!!)
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                dialog.setContentView(R.layout.make_call_dialog)

                                val diaUserNameTV = dialog.findViewById(R.id.diaUserNameTV) as TextView
                                val diaMobileNoTV = dialog.findViewById(R.id.diaMobileNoTV) as TextView
                                val diaCancelBT = dialog.findViewById(R.id.diaCancelBT) as Button
                                val diaOKBT = dialog.findViewById(R.id.diaOKBT) as Button

                                diaUserNameTV.text = "Make Call To Help Desk"
                                diaMobileNoTV.text = jsonObject.getString("contact_no")

                                diaCancelBT.setOnClickListener {
                                    dialog.dismiss()
                                }

                                diaOKBT.setOnClickListener {
                                    val callIntent = Intent(Intent.ACTION_DIAL)
                                    callIntent.data = Uri.parse("tel:" + jsonObject.getString("contact_no"))
                                    startActivity(callIntent)
                                    dialog.dismiss()
                                }

                                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                dialog.show()
                                val window = dialog.window!!
                                window.setLayout(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                            }

                            override fun onAnimationStart(animation: Animation?) {
                            }

                        })
                    }
                }
            }
        })

        setOnScrollListener(recyclerView)

        swipeRefreshLayout.setOnRefreshListener {
            ordersList.clear()
            mainOrdersList.clear()
            currentPage = 0
            if (shared!!.getString("shop_name", "") != "") {
                progressLayout!!.visibility = View.VISIBLE
                val obj = JSONObject()
                if (isSubSupplier) {
                    obj.put("supplier_id", subSupplierID)
                } else {
                    obj.put("supplier_id", shared!!.getString("id", ""))
                }
                obj.put("page_no", currentPage++)
                repo!!.getOrderListBySupplierID(obj.toString())
            }
        }
    }

    private val showProgressBar = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            progressLayout!!.visibility = View.VISIBLE
            rvOverView!!.visibility = View.VISIBLE
        }
    }

    private val hideProgressBar = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            progressLayout!!.visibility = View.GONE
            rvOverView!!.visibility = View.GONE
        }
    }

    private val showNoOrdersLayout = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            ordersLayout!!.visibility = View.GONE
            supplierStatusLayout!!.visibility = View.VISIBLE
        }
    }

    private val refreshData = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            ordersList.clear()
            mainOrdersList.clear()
            currentPage = 0
            if (shared!!.getString("shop_name", "") != "") {
                progressLayout!!.visibility = View.VISIBLE
                val obj = JSONObject()
                if (isSubSupplier) {
                    obj.put("supplier_id", subSupplierID)
                } else {
                    obj.put("supplier_id", shared!!.getString("id", ""))
                }
                obj.put("page_no", currentPage++)
                repo!!.getOrderListBySupplierID(obj.toString())
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
                            if (shared!!.getString("shop_name", "") != "") {
                                progressLayout!!.visibility = View.VISIBLE
                                val obj = JSONObject()
                                if (isSubSupplier) {
                                    obj.put("supplier_id", subSupplierID)
                                } else {
                                    obj.put("supplier_id", shared!!.getString("id", ""))
                                }
                                obj.put("page_no", currentPage++)
                                repo!!.getOrderListBySupplierID(obj.toString())
                            }
                        }
                    }
                }
            }
        })
    }
}