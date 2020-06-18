package `in`.inferon.msl.cucumbor.view.activity

import `in`.inferon.msl.cucumbor.R
import `in`.inferon.msl.cucumbor.repo.Repository
import `in`.inferon.msl.cucumbor.view.fragment.GroceryFragment
import `in`.inferon.msl.cucumbor.view.fragment.MilkFragment
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.navigation_layout.*
import org.json.JSONObject

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var shared: SharedPreferences? = null
    private val PREF = "Pref"
    private var repo: Repository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        shared = getSharedPreferences(PREF, MODE_PRIVATE)
        repo = Repository()

        val logObj = JSONObject()
        logObj.put("what", "")
        logObj.put("who", shared!!.getString("id", ""))
        logObj.put("where", "cucumbor")
        logObj.put(
            "log_data",
            "MainActivity : Loading Milk Fragment"
        )
        repo!!.eventLog(logObj.toString(), this@MainActivity)

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MilkFragment()).commit()

        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_item1 -> {
                    val logObj = JSONObject()
                    logObj.put("what", "")
                    logObj.put("who", shared!!.getString("id", ""))
                    logObj.put("where", "cucumbor")
                    logObj.put(
                        "log_data",
                        "MainActivity : Loading Milk Fragment"
                    )
                    repo!!.eventLog(logObj.toString(), this@MainActivity)

                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MilkFragment()).commit()
                }
                R.id.action_item2 -> {
                    val logObj = JSONObject()
                    logObj.put("what", "")
                    logObj.put("who", shared!!.getString("id", ""))
                    logObj.put("where", "cucumbor")
                    logObj.put(
                        "log_data",
                        "MainActivity : Loading Grocery Fragment"
                    )
                    repo!!.eventLog(logObj.toString(), this@MainActivity)

                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, GroceryFragment())
                        .commit()
                }
            }
            return@setOnNavigationItemSelectedListener true
        }


        menu_layout.setOnClickListener(this)
        nav_order_bt.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.menu_layout -> {
                val logObj = JSONObject()
                logObj.put("what", "")
                logObj.put("who", shared!!.getString("id", ""))
                logObj.put("where", "cucumbor")
                logObj.put(
                    "log_data",
                    "MainActivity : Menu Button Clicked"
                )
                repo!!.eventLog(logObj.toString(), this@MainActivity)

                drawer_layout.openDrawer(GravityCompat.START)
            }
            R.id.nav_order_bt -> {
                val logObj = JSONObject()
                logObj.put("what", "")
                logObj.put("who", shared!!.getString("id", ""))
                logObj.put("where", "cucumbor")
                logObj.put(
                    "log_data",
                    "MainActivity : Menu Total Sales Report Button Clicked"
                )
                repo!!.eventLog(logObj.toString(), this@MainActivity)

                drawer_layout.closeDrawer(GravityCompat.START)
                val intent = Intent(this, TotalSalesReportActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
