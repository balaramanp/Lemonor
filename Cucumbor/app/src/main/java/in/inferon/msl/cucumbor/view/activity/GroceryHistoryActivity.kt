package `in`.inferon.msl.cucumbor.view.activity

import `in`.inferon.msl.cucumbor.R
import `in`.inferon.msl.cucumbor.model.pojo.GroceryHistory
import `in`.inferon.msl.cucumbor.repo.Repository
import `in`.inferon.msl.cucumbor.view.adapter.GroceryHistoryAdapter
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_grocery_history.*
import kotlinx.android.synthetic.main.activity_grocery_history.progressBar
import kotlinx.android.synthetic.main.grocery_fragment.*
import org.json.JSONObject

class GroceryHistoryActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = GroceryHistoryActivity::class.java.simpleName
    private var repo: Repository? = null
    private val PREF = "Pref"
    private var groceryHistoryList = mutableListOf<GroceryHistory>()
    private var groceryHistoryAdapter: GroceryHistoryAdapter? = null
    private var shared: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grocery_history)

        LocalBroadcastManager.getInstance(this).registerReceiver(groceryCancel, IntentFilter("GroceryCancel"))

        shared = getSharedPreferences(PREF, MODE_PRIVATE)
        repo = Repository()

        progressBar.visibility = View.VISIBLE
        val obj = JSONObject()
        obj.put("client_app_get_grocery_sales_list", "")
        obj.put("user_id", shared!!.getString("id", ""))
        repo!!.getGroceryHistory(obj.toString())

        repo!!.getGroceryHistory.observe(this, androidx.lifecycle.Observer {
            run {
                if (it.isNotEmpty()) {
                    val jobj = JSONObject(it)
                    progressBar.visibility = View.GONE
                    groceryHistoryList = Gson().fromJson<MutableList<GroceryHistory>>(
                        jobj.getString("data"),
                        object : TypeToken<MutableList<GroceryHistory>>() {}.type
                    )

                    groceryHistoryRV.layoutManager = LinearLayoutManager( this)
                    groceryHistoryAdapter = GroceryHistoryAdapter(this, groceryHistoryList, this@GroceryHistoryActivity)
                    groceryHistoryRV.adapter = groceryHistoryAdapter
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
                    "GroceryHistoryActivity : Back Button Clicked"
                )
                repo!!.eventLog(logObj.toString(), this@GroceryHistoryActivity)

                super.onBackPressed()
            }
        }
    }

    private val groceryCancel = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val logObj = JSONObject()
            logObj.put("what", "")
            logObj.put("who", shared!!.getString("id", ""))
            logObj.put("where", "cucumbor")
            logObj.put(
                "log_data",
                "GroceryHistoryActivity : Grocery Item Cancel Button Clicked"
            )
            repo!!.eventLog(logObj.toString(), this@GroceryHistoryActivity)

            progressBar.visibility = View.VISIBLE
            val obj = JSONObject()
            obj.put("client_app_get_grocery_sales_list", "")
            obj.put("user_id", shared!!.getString("id", ""))
            repo!!.getGroceryHistory(obj.toString())
        }
    }
}
