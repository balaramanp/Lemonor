package `in`.inferon.msl.cucumbor.view.fragment

import `in`.inferon.msl.cucumbor.R
import `in`.inferon.msl.cucumbor.model.pojo.Grocery
import `in`.inferon.msl.cucumbor.model.pojo.GroceryUpdateResp
import `in`.inferon.msl.cucumbor.repo.Repository
import `in`.inferon.msl.cucumbor.view.activity.ConfirmActivity
import `in`.inferon.msl.cucumbor.view.activity.GroceryHistoryActivity
import `in`.inferon.msl.cucumbor.view.activity.MainActivity
import `in`.inferon.msl.cucumbor.view.adapter.GroceryListAdapter
import android.annotation.SuppressLint
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject

class GroceryFragment : Fragment(), View.OnClickListener {

    private val TAG = GroceryFragment::class.java.simpleName
    private var repo: Repository? = null
    private val PREF = "Pref"
    private var groceryList = mutableListOf<Grocery>()
    private var groceryListAdapter: GroceryListAdapter? = null
    private var shared: SharedPreferences? = null
    private var totalBillTV: TextView? = null
    private var expectedTime = ""

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.grocery_fragment, null)
        init(view)
        return view
    }

    private fun init(view: View) {

        demoMode(view)

        LocalBroadcastManager.getInstance(context!!)
            .registerReceiver(goodsPlusQtyClick, IntentFilter("GoodsPlusQtyClick"))
        LocalBroadcastManager.getInstance(context!!)
            .registerReceiver(goodsMinusQtyClick, IntentFilter("GoodsMinusQtyClick"))
        LocalBroadcastManager.getInstance(context!!)
            .registerReceiver(groceryAddingToProjection, IntentFilter("GroceryAddingToProjection"))

        shared = activity!!.getSharedPreferences(PREF, AppCompatActivity.MODE_PRIVATE)

        val callBT = view.findViewById(R.id.callBT) as Button
        val historyBT = view.findViewById(R.id.historyBT) as Button
        val groceryRV = view.findViewById(R.id.groceryRV) as RecyclerView
        val progressBar = view.findViewById(R.id.progressBar) as ProgressBar
        val nextBT = view.findViewById(R.id.nextBT) as Button
        totalBillTV = view.findViewById(R.id.totalBillTV) as TextView

        progressBar.visibility = View.VISIBLE
        repo = Repository()

        val obj = JSONObject()
        obj.put("client_app_get_init_data", "")
        obj.put("user_id", shared!!.getString("id", ""))
        obj.put("pincode", shared!!.getString("pincode", ""))
        repo!!.getGroceryList(obj.toString())

        repo!!.getGroceryList.observe(this, androidx.lifecycle.Observer {
            run {
                if (it.isNotEmpty()) {
                    val jobj = JSONObject(it)
                    progressBar.visibility = View.GONE
                    expectedTime = jobj.getString("delivery_time")
                    groceryList = Gson().fromJson<MutableList<Grocery>>(
                        jobj.getString("sales_price_list"),
                        object : TypeToken<MutableList<Grocery>>() {}.type
                    )

                    groceryRV.layoutManager = LinearLayoutManager(context)
                    groceryListAdapter = GroceryListAdapter(context!!, groceryList, context as MainActivity)
                    groceryRV.adapter = groceryListAdapter
                } else {
                    Log.e(TAG, "Get Upcoming Milk List Response is Empty")
                }
            }
        })



        callBT.setOnClickListener(this)
        historyBT.setOnClickListener(this)
        nextBT.setOnClickListener(this)
    }

    private val goodsPlusQtyClick = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            var tot = 0
            for(i in groceryList){
                Log.e("Plus Price",i.calculated_price.toString())
                tot += i.calculated_price
            }
            totalBillTV!!.text = tot.toString()
        }
    }

    private val goodsMinusQtyClick = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            var tot = 0
            for(i in groceryList){
                Log.e("Minus Price",i.calculated_price.toString())
                tot += i.calculated_price
            }
            totalBillTV!!.text = tot.toString()
        }
    }

    private val groceryAddingToProjection = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            addingToProjection()
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.callBT -> {
                val logObj = JSONObject()
                logObj.put("what", "")
                logObj.put("who", shared!!.getString("id", ""))
                logObj.put("where", "cucumbor")
                logObj.put(
                    "log_data",
                    "GroceryFragment : Call Button Clicked"
                )
                repo!!.eventLog(logObj.toString(), context!!)

                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:" + "9095590951")
                startActivity(callIntent)
            }
            R.id.historyBT -> {
                val logObj = JSONObject()
                logObj.put("what", "")
                logObj.put("who", shared!!.getString("id", ""))
                logObj.put("where", "cucumbor")
                logObj.put(
                    "log_data",
                    "GroceryFragment : History Button Clicked"
                )
                repo!!.eventLog(logObj.toString(), context!!)

                val intent = Intent(context, GroceryHistoryActivity::class.java)
                startActivity(intent)
            }
            R.id.nextBT -> {
                val logObj = JSONObject()
                logObj.put("what", "")
                logObj.put("who", shared!!.getString("id", ""))
                logObj.put("where", "cucumbor")
                logObj.put(
                    "log_data",
                    "GroceryFragment : Place Order Next Button Clicked"
                )
                repo!!.eventLog(logObj.toString(), context!!)

//                progressBar.visibility = View.VISIBLE

                val confirmArray = JSONArray()
                for (i in groceryList) {
                    if (i.qty != "0") {
                        val obj = JSONObject()
                        obj.put("milk_type_id", i.id)
                        obj.put("milk_type_name", i.name)
                        obj.put("qty", i.qty)
                        obj.put("show_unit", i.show_unit)
                        obj.put("price", i.cf.toFloat() * i.price_range.toFloat() * i.qty.toFloat())
                        confirmArray.put(obj)
                    }
                }
                val intent = Intent(context, ConfirmActivity::class.java)
                intent.putExtra("confirm_array", confirmArray.toString())
                intent.putExtra("delivery_time", expectedTime)
                startActivity(intent)
            }
        }
    }

    private fun addingToProjection() {
        val jobj = JSONObject()
        jobj.put("user_id", shared!!.getString("id", ""))
        val jarr = JSONArray()
        for (i in groceryList) {
            if (i.qty != "0") {
                val obj = JSONObject()
                obj.put("milk_type_id", i.id)
                obj.put("qty", i.qty)
                jarr.put(obj)
            }
        }
        jobj.put("products_list", jarr)

        jobj.put("client_app_update_grocery_to_projection", "")

        repo!!.updateGroceryToProjection(jobj.toString())


        repo!!.updateGroceryToProjection.observe(this, androidx.lifecycle.Observer {
            run {
                if (it.isNotEmpty()) {
                    val groceryUpdateResp = Gson().fromJson<MutableList<GroceryUpdateResp>>(
                        it.toString(),
                        object : TypeToken<MutableList<GroceryUpdateResp>>() {}.type
                    )
                    var allSuccess = true
                    for (i in groceryUpdateResp) {
                        if (i.status == "failed") {
                            Toast.makeText(
                                context,
                                "${i.product_name} Limited Stock. Try After Sometime. Please Contact Customer Care.",
                                Toast.LENGTH_SHORT
                            ).show()
                            allSuccess = false
                        }
                    }

                    val obj = JSONObject()
                    obj.put("client_app_get_init_data", "")
                    obj.put("user_id", shared!!.getString("id", ""))
                    obj.put("pincode", shared!!.getString("pincode", ""))
                    repo!!.getGroceryList(obj.toString())
                    totalBillTV!!.text = "0"
                    if(allSuccess){
                        Toast.makeText(context!!, "Your Order Placed Successfully!", Toast.LENGTH_SHORT).show()
                    }else {
                        Toast.makeText(context!!, "Your Order Placed Successfully! Some Products Limited Stock. " +
                                "Try After Sometime. Please Contact Customer Care.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun demoMode(view: View) {
        val clickingView = view.findViewById(R.id.clickingView) as RelativeLayout
        val imageViewTemp = view.findViewById(R.id.imageViewTemp) as ImageView


        val clickViewMoveAnimation: Animation = AnimationUtils.loadAnimation(context!!, R.anim.translate_click_view)
        clickingView.startAnimation(clickViewMoveAnimation)
        val clickViewZoomInOutAnimation: Animation = AnimationUtils.loadAnimation(context!!, R.anim.click_view_zoom_in_out)
        val itemToCountAnimation: Animation = AnimationUtils.loadAnimation(context!!, R.anim.translate_right)

        clickViewMoveAnimation.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationRepeat(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                clickingView.startAnimation(clickViewZoomInOutAnimation)
            }

            override fun onAnimationStart(p0: Animation?) {
            }
        })

        clickViewZoomInOutAnimation.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationRepeat(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                clickingView.visibility = View.GONE
                imageViewTemp.startAnimation(itemToCountAnimation)
            }

            override fun onAnimationStart(p0: Animation?) {
            }
        })

        clickViewZoomInOutAnimation.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationRepeat(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                clickingView.visibility = View.GONE
                imageViewTemp.startAnimation(itemToCountAnimation)
            }

            override fun onAnimationStart(p0: Animation?) {
            }
        })
    }
}