package `in`.inferon.msl.cucumbor.view.activity

import `in`.inferon.msl.cucumbor.R
import `in`.inferon.msl.cucumbor.model.pojo.GroceryHistory
import `in`.inferon.msl.cucumbor.model.pojo.GroceryOrderList
import `in`.inferon.msl.cucumbor.model.pojo.GroceryUpdateResp
import `in`.inferon.msl.cucumbor.repo.Repository
import `in`.inferon.msl.cucumbor.view.adapter.GroceryConfirmAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_confirm.*
import org.json.JSONArray
import org.json.JSONObject

class ConfirmActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = ConfirmActivity::class.java.simpleName
    private var repo: Repository? = null
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm)

        shared = getSharedPreferences(PREF, MODE_PRIVATE)
        repo = Repository()

        val ca = intent.getStringExtra("confirm_array")
        val confirmArray = JSONArray(ca)
        Log.e(TAG, "Received Confirm Array : $confirmArray")
        val groceryOrderList = Gson().fromJson<MutableList<GroceryOrderList>>(
            confirmArray.toString(),
            object : TypeToken<MutableList<GroceryOrderList>>() {}.type
        )

        var tot = 0
        for(i in groceryOrderList){
            tot += i.price.toInt()
        }
        totalTV!!.text = getString(R.string.Rs) + " " + tot.toString()
        expectedTimeTV.text = intent.getStringExtra("delivery_time")

        confirmGroceryRV.layoutManager = LinearLayoutManager(this)
        val groceryConfirmAdapter = GroceryConfirmAdapter(this, confirmArray)
        confirmGroceryRV.adapter = groceryConfirmAdapter

        backIB.setOnClickListener(this)
        cancelBT.setOnClickListener(this)
        okBT.setOnClickListener(this)
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
                    "ConfirmActivity : Back Button Clicked"
                )
                repo!!.eventLog(logObj.toString(), this@ConfirmActivity)

                super.onBackPressed()
            }
            R.id.cancelBT -> {
                val logObj = JSONObject()
                logObj.put("what", "")
                logObj.put("who", shared!!.getString("id", ""))
                logObj.put("where", "cucumbor")
                logObj.put(
                    "log_data",
                    "ConfirmActivity : Cancel Button Clicked"
                )
                repo!!.eventLog(logObj.toString(), this@ConfirmActivity)

                super.onBackPressed()
            }
            R.id.okBT -> {
                val logObj = JSONObject()
                logObj.put("what", "")
                logObj.put("who", shared!!.getString("id", ""))
                logObj.put("where", "cucumbor")
                logObj.put(
                    "log_data",
                    "ConfirmActivity : Confirm Button Clicked"
                )
                repo!!.eventLog(logObj.toString(), this@ConfirmActivity)

                val i = Intent("GroceryAddingToProjection")
                i.putExtra("groceryAddingToProjection", "")
                LocalBroadcastManager.getInstance(this).sendBroadcast(i)

                super.onBackPressed()
            }
        }
    }
}
