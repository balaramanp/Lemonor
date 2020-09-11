package `in`.inferon.msl.lemonor.view.activity

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.repo.Repository
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.LinkProperties
import kotlinx.android.synthetic.main.activity_about_us.*
import kotlinx.android.synthetic.main.activity_about_us.backIB
import kotlinx.android.synthetic.main.activity_about_us.progressLayout
import kotlinx.android.synthetic.main.activity_about_us.shareIB
import org.json.JSONObject

class AboutUsActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = AboutUsActivity::class.java.simpleName
    private var shared: SharedPreferences? = null
    private val PREF = "Pref"
    private var repo: Repository? = null
    private var shareEnglish = ""
    private var shareTamil = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        repo = Repository()
        shared = getSharedPreferences(PREF, MODE_PRIVATE)

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        if (!isConnected) {
            val intent = Intent(this, NoInternetActivity::class.java)
            startActivity(intent)
        } else {
            progressLayout.visibility = View.VISIBLE
            repo!!.getAppContent()
        }

        repo!!.getAppContent.observe(this, androidx.lifecycle.Observer {
            run {
                progressLayout.visibility = View.GONE
                shareIB.visibility = View.VISIBLE
                shareBT.visibility = View.VISIBLE
                val slideIn: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
                shareIB.startAnimation(slideIn)

                val jsonObject = JSONObject(it)
                englishAboutUsTv.text = jsonObject.getString("about_us_eng_content")
                tamilAboutUsTv.text = jsonObject.getString("about_us_tam_content")
                shareEnglish = jsonObject.getString("share_eng_content")
                shareTamil = jsonObject.getString("share_tam_content")
            }
        })

        backIB.setOnClickListener(this)
        shareIB.setOnClickListener(this)
        shareBT.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backIB -> {
                super.onBackPressed()
            }
            R.id.shareIB->{
                loadDialog()
            }
            R.id.shareBT -> {
                loadDialog()
            }
        }
    }

    private fun loadDialog(){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.supplier_share_dialog)

        val shareTamilBT = dialog.findViewById(R.id.shareTamilBT) as Button
        val shareEnglishBT = dialog.findViewById(R.id.shareEnglishBT) as Button

        shareTamilBT.setOnClickListener {
            dialog.dismiss()
            generateBranchTamil()
        }

        shareEnglishBT.setOnClickListener {
            dialog.dismiss()
            generateBranchEnglish()
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val window = dialog.window!!
        window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun generateBranchEnglish() {
        val contentMetadata = ContentMetadata()
        contentMetadata.addCustomMetadata("user_id", shared!!.getString("id", ""))
        contentMetadata.addCustomMetadata("seller_id", "")
        val branchUniversalObject = BranchUniversalObject()
            .setCanonicalIdentifier(shared!!.getString("id", "")!!)
            .setTitle("Lemonor")
            .setContentDescription("Lemonor - Online Neighborhood Grocery Shopping")
//            .setContentImageUrl((R.drawable.logoplain))
            .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
            .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
            .setContentMetadata(contentMetadata)
        val linkProperties = LinkProperties()
            .setChannel("facebook")
            .setFeature("sharing")
        branchUniversalObject.generateShortUrl(
            this, linkProperties
        ) { url, error ->
            if (error == null) {
                Log.i("MyApp", "got my Branch link to share: $url")
                val shareContent = "\n\n" + shareEnglish

                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, "Lemonor")
                intent.putExtra(Intent.EXTRA_TEXT, url + shareContent)
                startActivity(intent)
            } else {
                Log.i(TAG, "onLinkCreate: " + error.message)
            }
        }
    }

    private fun generateBranchTamil() {
        val contentMetadata = ContentMetadata()
        contentMetadata.addCustomMetadata("user_id", shared!!.getString("id", ""))
        contentMetadata.addCustomMetadata("seller_id", "")
        val branchUniversalObject = BranchUniversalObject()
            .setCanonicalIdentifier(shared!!.getString("id", "")!!)
            .setTitle("Lemonor")
            .setContentDescription("Lemonor - Online Neighborhood Grocery Shopping")
//            .setContentImageUrl((R.drawable.logoplain))
            .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
            .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
            .setContentMetadata(contentMetadata)
        val linkProperties = LinkProperties()
            .setChannel("facebook")
            .setFeature("sharing")
        branchUniversalObject.generateShortUrl(
            this, linkProperties
        ) { url, error ->
            if (error == null) {
                Log.i("MyApp", "got my Branch link to share: $url")
                val shareContent = "\n\n" + shareTamil
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, "Lemonor")
                intent.putExtra(Intent.EXTRA_TEXT, url + shareContent)
                startActivity(intent)
            } else {
                Log.i(TAG, "onLinkCreate: " + error.message)
            }
        }
    }
}
