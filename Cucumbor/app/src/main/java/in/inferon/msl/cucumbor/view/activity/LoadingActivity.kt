package `in`.inferon.msl.cucumbor.view.activity

import `in`.inferon.msl.cucumbor.R
import `in`.inferon.msl.cucumbor.model.Constants
import `in`.inferon.msl.cucumbor.repo.Repository
import android.animation.Animator
import android.animation.AnimatorInflater
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_loading.*
import org.json.JSONObject

class LoadingActivity : AppCompatActivity() {
    private var isConnected: Boolean = false
    private val requestCode = 200
    private val PREF = "Pref"
    private var repo: Repository? = null

    override fun onStart() {
        super.onStart()
        checkInternet()
        if (isConnected) {
            checkGPS()
        } else {
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        repo = Repository()
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            this.requestCode -> if (permissions.size == grantResults.size) {
                val animator = AnimatorInflater.loadAnimator(this@LoadingActivity, R.animator.rotate_y)
                animator.setTarget(logo_iv)
                animator.start()
                val animatorListener = object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {

                    }

                    override fun onAnimationEnd(animator: Animator) {
                        val shared = getSharedPreferences(PREF, MODE_PRIVATE)
                        if (shared.getString("name", "") != ""){
                            val logObj = JSONObject()
                            logObj.put("what", "")
                            logObj.put("who", shared.getString("id", ""))
                            logObj.put("where", "cucumbor")
                            logObj.put(
                                "log_data",
                                "LoadingActivity : Checking User Login - Navigation to MainActivity"
                            )
                            repo!!.eventLog(logObj.toString(), this@LoadingActivity)

                            val intent = Intent(this@LoadingActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }else{
                            val logObj = JSONObject()
                            logObj.put("what", "")
                            logObj.put("who", shared.getString("id", ""))
                            logObj.put("where", "cucumbor")
                            logObj.put(
                                "log_data",
                                "LoadingActivity : Checking User Login - Navigation to LoginActivity"
                            )
                            repo!!.eventLog(logObj.toString(), this@LoadingActivity)

                            val intent = Intent(this@LoadingActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }

                    override fun onAnimationCancel(animator: Animator) {

                    }

                    override fun onAnimationRepeat(animator: Animator) {

                    }
                }
                animator.addListener(animatorListener)
            } else {
                Toast.makeText(this, "Please grant all permissions!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkInternet() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        isConnected = activeNetwork?.isConnected == true
    }

    private fun checkGPS() {
        val lm: LocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder(this)
                .setMessage("GPS Network Not Enabled")
                .setPositiveButton("Open Location Settings", DialogInterface.OnClickListener { dialogInterface, i ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                })
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            ActivityCompat.requestPermissions(this, Constants.getPermissions(), requestCode)
        }
    }
}
