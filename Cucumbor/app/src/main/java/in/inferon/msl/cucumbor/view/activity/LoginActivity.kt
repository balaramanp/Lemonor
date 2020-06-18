package `in`.inferon.msl.cucumbor.view.activity

import `in`.inferon.msl.cucumbor.R
import `in`.inferon.msl.cucumbor.repo.Repository
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = LoginActivity::class.java.simpleName
    private var repo: Repository? = null
    private val PREF = "Pref"
    private var shared: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        repo = Repository()
        shared = getSharedPreferences(PREF, MODE_PRIVATE)

        repo!!.getOTP.observe(this, androidx.lifecycle.Observer {
            run {
                if (it.isNotEmpty()) {
                    val jobj = JSONObject(it)
                    when (jobj.getString("status")) {
                        "ok" -> {
                            val logObj = JSONObject()
                            logObj.put("what", "")
                            logObj.put("who", shared!!.getString("id", ""))
                            logObj.put("where", "cucumbor")
                            logObj.put(
                                "log_data",
                                "LoginActivity : Showing OTP Dialog"
                            )
                            repo!!.eventLog(logObj.toString(), this@LoginActivity)

                            showOTPDialog()
                        }
                        "no_user" -> {
                            val logObj = JSONObject()
                            logObj.put("what", "")
                            logObj.put("who", shared!!.getString("id", ""))
                            logObj.put("where", "cucumbor")
                            logObj.put(
                                "log_data",
                                "LoginActivity : Entered Mobile Number is not a User"
                            )
                            repo!!.eventLog(logObj.toString(), this@LoginActivity)

                            Toast.makeText(this, "You are Not a User!", Toast.LENGTH_SHORT).show()
                        }
                        else -> {

                        }
                    }
                } else {
                    Log.e(TAG, "Get OTP Response is Empty")
                }
            }
        })

        login_bt.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.login_bt -> {
                if (mobile_no_et.text.toString().trim().length == 10) {
                    val logObj = JSONObject()
                    logObj.put("what", "")
                    logObj.put("who", shared!!.getString("id", ""))
                    logObj.put("where", "cucumbor")
                    logObj.put(
                        "log_data",
                        "LoginActivity : Showing OTP Dialog"
                    )
                    repo!!.eventLog(logObj.toString(), this@LoginActivity)

                    progress_bar.visibility = View.VISIBLE
                    val obj = JSONObject()
                    obj.put("client_app_get_otp", "")
                    obj.put("mobile_number", mobile_no_et.text.toString().trim())
                    repo!!.getOTP(obj.toString())
                } else {
                    Toast.makeText(this, "Please Enter Valid Mobile Number!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showOTPDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.otp_dialog)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val otpd_otp_et = dialog.findViewById(R.id.otpd_otp_et) as EditText
        val loadingLayout = dialog.findViewById(R.id.loadingLayout) as LinearLayout
        val otpd_done_bt = dialog.findViewById(R.id.otpd_done_bt) as Button

        otpd_done_bt.setOnClickListener(View.OnClickListener {
            if (otpd_otp_et.text.isNotEmpty()) {
                val logObj = JSONObject()
                logObj.put("what", "")
                logObj.put("who", shared!!.getString("id", ""))
                logObj.put("where", "cucumbor")
                logObj.put(
                    "log_data",
                    "LoginActivity : OTP Done Button Clicked"
                )
                repo!!.eventLog(logObj.toString(), this@LoginActivity)

                loadingLayout.visibility = View.VISIBLE
                otpd_done_bt.visibility = View.GONE

                val obj = JSONObject()
                obj.put("client_app_verify_otp", "")
                obj.put("mobile_number", mobile_no_et.text.toString().trim())
                obj.put("otp", otpd_otp_et.text.toString().trim())
                repo!!.verifyOTP(obj.toString())
            } else {
                Toast.makeText(this, "Please Enter Valid OTP!", Toast.LENGTH_SHORT).show()
            }
        })

        repo!!.verifyOTP.observe(this, androidx.lifecycle.Observer {
            run {
                if (it.isNotEmpty()) {
                    val jobj = JSONObject(it)
                    when (jobj.getString("status")) {
                        "ok" -> {
                            val editor = getSharedPreferences(PREF, MODE_PRIVATE).edit()
                            editor.putString("name", jobj.getString("name"))
                            editor.putString("id", jobj.getString("id"))
                            editor.putString("pincode", jobj.getString("pincode"))
                            editor.apply()

                            val logObj = JSONObject()
                            logObj.put("what", "")
                            logObj.put("who", shared!!.getString("id", ""))
                            logObj.put("where", "cucumbor")
                            logObj.put(
                                "log_data",
                                "LoginActivity : OTP Verified - Navigate to MainActivity"
                            )
                            repo!!.eventLog(logObj.toString(), this@LoginActivity)

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        "wrong_otp" -> {

                            val logObj = JSONObject()
                            logObj.put("what", "")
                            logObj.put("who", shared!!.getString("id", ""))
                            logObj.put("where", "cucumbor")
                            logObj.put(
                                "log_data",
                                "LoginActivity : Wrong OTP"
                            )
                            repo!!.eventLog(logObj.toString(), this@LoginActivity)

                            loadingLayout.visibility = View.GONE
                            otpd_done_bt.visibility = View.VISIBLE
                            otpd_otp_et.setText("")
                        }
                        "no_user" -> {
                            Toast.makeText(this, "You are Not a User!", Toast.LENGTH_SHORT).show()
                        }
                        else -> {

                        }
                    }
                } else {
                    Log.e(TAG, "Get OTP Response is Empty")
                }
            }
        })
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
        val window = dialog.window
        window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }
}
