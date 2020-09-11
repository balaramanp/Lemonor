package `in`.inferon.msl.cucumbor.view.fragment

import `in`.inferon.msl.cucumbor.R
import `in`.inferon.msl.cucumbor.model.pojo.SalesTable
import `in`.inferon.msl.cucumbor.model.pojo.UpcomingMilk
import `in`.inferon.msl.cucumbor.repo.Repository
import `in`.inferon.msl.cucumbor.view.activity.MainActivity
import `in`.inferon.msl.cucumbor.view.adapter.SalesReportAdapter
import `in`.inferon.msl.cucumbor.view.adapter.UpcomingMilkAdapter
import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.milk_fragment.*
import org.json.JSONObject

class MilkFragment : Fragment() {
    private val TAG = MilkFragment::class.java.simpleName
    private var repo: Repository? = null
    private val PREF = "Pref"
    private var upcomingMilk = mutableListOf<UpcomingMilk>()
    private var milkSales = mutableListOf<SalesTable>()
    private var upcomingMilkAdapter: UpcomingMilkAdapter? = null
    private var salesReportAdapter: SalesReportAdapter? = null
    private var shared: SharedPreferences? = null

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.milk_fragment, null)
        init(view)
        return view
    }

    private fun init(view: View) {
        shared = activity!!.getSharedPreferences(PREF, AppCompatActivity.MODE_PRIVATE)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(milkModification, IntentFilter("MilkModification"))

        val upcomingRV = view.findViewById(R.id.upcomingRV) as RecyclerView
        val historyRV = view.findViewById(R.id.historyRV) as RecyclerView
        val progressBar = view.findViewById(R.id.progressBar) as ProgressBar

        progressBar.visibility = View.VISIBLE
        repo = Repository()

        val obj = JSONObject()
        obj.put("client_app_get_upcoming_list", "")
        obj.put("user_id", shared!!.getString("id", ""))
        repo!!.getUpcomingMilkList(obj.toString())

        val sobj = JSONObject()
        sobj.put("client_app_get_sales_list", "")
        sobj.put("user_id", shared!!.getString("id", ""))
        repo!!.getMilkSalesList(sobj.toString())

        /*repo!!.getUpcomingMilkList.observe(this, androidx.lifecycle.Observer {
            run {
                if (it.isNotEmpty()) {
                    progressBar.visibility = View.VISIBLE
                    upcomingMilk = Gson().fromJson<MutableList<UpcomingMilk>>(
                        it.toString(),
                        object : TypeToken<MutableList<UpcomingMilk>>() {}.type
                    )

                    upcomingRV.layoutManager = LinearLayoutManager(context)
                    upcomingMilkAdapter = UpcomingMilkAdapter(context!!, upcomingMilk, context as MainActivity)
                    upcomingRV.adapter = upcomingMilkAdapter
                    progressBar.visibility = View.GONE
                } else {
                    Log.e(TAG, "Get Upcoming Milk List Response is Empty")
                }
            }
        })*/

        repo!!.getMilkSalesList.observe(this, androidx.lifecycle.Observer {
            run {
                if (it.isNotEmpty()) {
                    milkSales = Gson().fromJson<MutableList<SalesTable>>(
                        it.toString(),
                        object : TypeToken<MutableList<SalesTable>>() {}.type
                    )

                    historyRV.layoutManager = LinearLayoutManager(context)
                    salesReportAdapter = SalesReportAdapter(context!!, milkSales)
                    historyRV.adapter = salesReportAdapter
                    progressBar.visibility = View.GONE
                } else {
                    Log.e(TAG, "Get Milk Sales List Response is Empty")
                }
            }
        })
    }

    private val milkModification = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val obj = JSONObject()
            obj.put("client_app_get_upcomming_list", "")
            obj.put("user_id", shared!!.getString("id", ""))
            repo!!.getUpcomingMilkList(obj.toString())
        }
    }
}