package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.Utils
import `in`.inferon.msl.lemonor.view.activity.PlaceOrderActivity
import `in`.inferon.msl.lemonor.view.activity.SafetyMeasureActivity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class ImageViewPagerAdapter(
    private var context: Context,
    private var imagesList: ArrayList<String>
) : PagerAdapter() {

    override fun getCount(): Int {
        return imagesList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.image_view_pager_adapter, container, false)
        val imageView = view.findViewById(R.id.imageView) as ImageView
        Log.e("TAG", "Image Url : " + Constants.IMG_BASE_URL + imagesList[position])
        val imgUrl = Constants.IMG_BASE_URL + imagesList[position]
        Picasso.get().load(imgUrl).into(imageView)

        imageView.setOnClickListener {
            Log.e("TAG", "First Four Character : " + imagesList[position].substring(0, 4))
            if (imagesList[position].substring(0, 4) == "CCCU") {
                showProgressBar()
                val jobj = JSONObject()
                jobj.put("supplier_id", imagesList[position].substring(0, imagesList[position].indexOf(".")))

                Utils.getRetrofit().checkSupplierExists(jobj.toString()).enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        t.printStackTrace()
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val responseString = response.body()!!.string()
                            Log.e("TAG", "Response String : $responseString")
                            val jsonObject = JSONObject(responseString)
                            if (jsonObject.getBoolean("is_supplier_exists")) {
                                val intent = Intent(context, PlaceOrderActivity::class.java)
                                intent.putExtra(
                                    "supplier_id",
                                    imagesList[position].substring(0, imagesList[position].indexOf("."))
                                )
                                intent.putExtra("shop_name", "")
                                context.startActivity(intent)
                                hideProgressBar()
                            } else {
                                Toast.makeText(context, "Shop Not Available!", Toast.LENGTH_SHORT).show()
                                hideProgressBar()
                            }
                        }
                    }
                })
            } else if (imagesList[position].substring(0, 4) == "SFTY") {
                val intent = Intent(context, SafetyMeasureActivity::class.java)
                context.startActivity(intent)
            }
        }
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as CardView)
    }

    private fun showProgressBar() {
        val intent = Intent("ShowProgressBar")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun hideProgressBar() {
        val intent = Intent("HideProgressBar")
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}
