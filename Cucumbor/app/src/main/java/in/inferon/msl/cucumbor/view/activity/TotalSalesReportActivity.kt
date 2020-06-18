package `in`.inferon.msl.cucumbor.view.activity

import `in`.inferon.msl.cucumbor.R
import `in`.inferon.msl.cucumbor.model.pojo.GroceryHistory
import `in`.inferon.msl.cucumbor.model.pojo.TotalSalesTable
import `in`.inferon.msl.cucumbor.repo.Repository
import `in`.inferon.msl.cucumbor.view.adapter.GroceryHistoryAdapter
import `in`.inferon.msl.cucumbor.view.adapter.TotalSalesReportAdapter
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_grocery_history.*
import kotlinx.android.synthetic.main.activity_total_sales_report.*
import kotlinx.android.synthetic.main.activity_total_sales_report.backIB
import kotlinx.android.synthetic.main.activity_total_sales_report.progressBar
import org.json.JSONObject

class TotalSalesReportActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = GroceryHistoryActivity::class.java.simpleName
    private var repo: Repository? = null
    private val PREF = "Pref"
    private var totalSalesReportAdapter: TotalSalesReportAdapter? = null
    private var shared: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_total_sales_report)

        shared = getSharedPreferences(PREF, MODE_PRIVATE)
        repo = Repository()

        progressBar.visibility = View.VISIBLE
        val obj = JSONObject()
        obj.put("client_app_all_multiple_products_from_to_user_wise", "")
        obj.put("user_id", shared!!.getString("id", ""))
        repo!!.getTotalSalesReport(obj.toString())

        repo!!.getTotalSalesReport.observe(this, androidx.lifecycle.Observer {
            run {
                if (it.isNotEmpty()) {
                    val jobj = JSONObject(it)
                    progressBar.visibility = View.GONE
                    val totalSalesReportList: MutableList<TotalSalesTable> =
                        Gson().fromJson(jobj.getString("data"), object : TypeToken<MutableList<TotalSalesTable>>() {}.type)

                    totalSalesReportRV.layoutManager = LinearLayoutManager( this)
                    totalSalesReportAdapter = TotalSalesReportAdapter(this, totalSalesReportList)
                    totalSalesReportRV.adapter = totalSalesReportAdapter
                } else {
                    Log.e(TAG, "Get Upcoming Milk List Response is Empty")
                }
            }
        })

        backIB.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backIB -> {
                val logObj = JSONObject()
                logObj.put("what", "")
                logObj.put("who", shared!!.getString("id", ""))
                logObj.put("where", "cucumbor")
                logObj.put(
                    "log_data",
                    "TotalSalesReportActivity : Back Button Clicked"
                )
                repo!!.eventLog(logObj.toString(), this@TotalSalesReportActivity)

                super.onBackPressed()
            }
        }
    }
}
