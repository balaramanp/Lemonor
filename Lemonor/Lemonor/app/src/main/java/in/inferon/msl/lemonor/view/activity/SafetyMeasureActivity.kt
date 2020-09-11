package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.pojo.SafetyMeasure
import `in`.inferon.msl.lemonor.repo.Repository
import `in`.inferon.msl.lemonor.view.adapter.SafetyMeasureAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_safety_measure.*

class SafetyMeasureActivity : AppCompatActivity() {
    private val TAG = SafetyMeasureActivity::class.java.simpleName
    private var repo: Repository? = null
    private var safetyList = mutableListOf<SafetyMeasure>()
    private var safetyMeasureAdapter: SafetyMeasureAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safety_measure)

        repo = Repository()
        repo!!.safetyMeasurement()

        repo!!.safetyMeasurement.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                safetyList = Gson().fromJson(it, object : TypeToken<MutableList<SafetyMeasure>>() {}.type)

                recyclerView.layoutManager = LinearLayoutManager(this)
                safetyMeasureAdapter = SafetyMeasureAdapter(this, safetyList)
                recyclerView.adapter = safetyMeasureAdapter
                recyclerView.setHasFixedSize(true)
                recyclerView.isNestedScrollingEnabled = false
            }
        })

        backIB.setOnClickListener {
            super.onBackPressed()
        }

        backBT.setOnClickListener {
            super.onBackPressed()
        }

        helpDeskBT.setOnClickListener {
            val intent = Intent(this, SupportActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
