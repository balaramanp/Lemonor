package `in`.inferon.msl.lemonor.view.adapter

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.model.Constants
import `in`.inferon.msl.lemonor.model.pojo.Products
import `in`.inferon.msl.lemonor.view.activity.PlaceOrderActivity
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.place_order_adapter.view.*
import java.lang.Exception
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class PlaceOrderAdapter(
    private val context: Context,
    private val productsList: MutableList<Products>,
    private val placeOrderActivity: PlaceOrderActivity,
    private val orderedProductsList: MutableList<Products>,
    private val from: String
) : RecyclerView.Adapter<PlaceOrderAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.place_order_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return productsList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imgUrl =
            Constants.SUPPLIER_PRODUCTS_IMG_BASE_URL + productsList[position].supplier_id + "/" + productsList[position].product_id + ".jpg"
        Log.e("TAG", "Product Image URL : $imgUrl")
        try {
            Picasso.get().load(imgUrl).into(holder.productImageIV, object : Callback {
                override fun onSuccess() {
                }

                override fun onError(e: Exception?) {
                    try {
                        val imgUrls =
                            Constants.PRODUCTS_IMG_BASE_URL + productsList[position].product_id + ".jpg"
                        Log.e("TAG", "Secondary URL : $imgUrls")

                        Picasso.get().load(imgUrls).into(holder.productImageIV, object : Callback {
                            override fun onSuccess() {

                            }

                            override fun onError(e: Exception?) {

                            }

                        })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
        }


        holder.productImageIV.visibility = View.VISIBLE

        holder.productNameTV.text = productsList[position].name
        if (productsList[position].local_name != "") {
            holder.productAliasNameTV.visibility = View.VISIBLE
            holder.productAliasNameTV.text = productsList[position].local_name
        } else {
            holder.productAliasNameTV.visibility = View.INVISIBLE
        }
        if (productsList[position].mrp.toFloat() > 0 && productsList[position].mrp.toFloat() > productsList[position].rate.toFloat()) {
            holder.mrpLayout.visibility = View.VISIBLE
            holder.mrpTV.text = productsList[position].mrp
            val pre = productsList[position].mrp.toFloat() - productsList[position].rate.toFloat()
            val div: Float = (pre / productsList[position].mrp.toFloat())
            val percent: Int = (div * 100).toInt()
            holder.percentageOffTV.text = "$percent% OFF"
        } else {
            holder.mrpLayout.visibility = View.GONE
        }

        if (productsList[position].rate.toFloat() > 0) {
            holder.priceLayout.visibility = View.VISIBLE
            holder.onDemandLayout.visibility = View.GONE
            holder.priceTV.text = productsList[position].rate
            holder.unitTV.text = "( " + productsList[position].unit + " )"
        } else {
            holder.priceLayout.visibility = View.GONE
            holder.onDemandLayout.visibility = View.GONE
        }

        Log.e("TAG", "Product Discount : " + productsList[position].discount)
        if (productsList[position].discount != "" && productsList[position].discount != "0") {
            holder.ourPriceLayout.visibility = View.VISIBLE
            holder.mainPriceLayout.visibility = View.GONE
            holder.opPercentageOffTV.text = productsList[position].discount + "%"
            holder.opUnitTV.text = "( " + productsList[position].unit + " )"

            val pre = productsList[position].rate.toFloat() / 100
            val percentAmount: Float = (pre * productsList[position].discount.toFloat())
            val ourPrice: Float = (productsList[position].rate.toFloat() - percentAmount)
            holder.opPriceTV.text = doubleToStringNoDecimal(ourPrice.toDouble()).toString()

            if (productsList[position].mrp.toFloat() > 0 && productsList[position].mrp.toFloat() > productsList[position].rate.toFloat()) {
                val mrpSA = productsList[position].mrp.toFloat() - productsList[position].rate.toFloat()
                val savedAmount = mrpSA + percentAmount
                holder.savedAmountTV.text =
                    context.resources.getString(R.string.RsSymbol) + " " + doubleToStringNoDecimal(savedAmount.toDouble()).toString()
            } else {
                holder.savedAmountTV.text =
                    context.resources.getString(R.string.RsSymbol) + " " + doubleToStringNoDecimal(percentAmount.toDouble()).toString()
            }


            if (productsList[position].mrp.toFloat() > 0 && productsList[position].mrp.toFloat() > productsList[position].rate.toFloat()) {
                holder.oMrpLayout.visibility = View.VISIBLE
                holder.oMrpTV.text = productsList[position].mrp
                val pre = productsList[position].mrp.toFloat() - productsList[position].rate.toFloat()
                val div: Float = (pre / productsList[position].mrp.toFloat())
                val percent: Int = (div * 100).toInt()
                holder.oPercentageOffTV.text = "$percent% OFF"
            } else {
                holder.oMrpLayout.visibility = View.GONE
            }

            if (productsList[position].rate.toFloat() > 0) {
                holder.oPriceLayout.visibility = View.VISIBLE
                holder.oPriceTV.text = productsList[position].rate
                holder.oUnitTV.text = "( " + productsList[position].unit + " )"
            } else {
                holder.oPriceLayout.visibility = View.GONE
            }
        } else {
            holder.ourPriceLayout.visibility = View.GONE
            holder.mainPriceLayout.visibility = View.VISIBLE
        }

        if (productsList[position].description.isNotEmpty()) {
            holder.descriptionTV.visibility = View.VISIBLE
            holder.descriptionTV.text = productsList[position].description
        } else {
            holder.descriptionTV.visibility = View.INVISIBLE
        }

        if (productsList[position].featured_product_flag) {
            holder.featuredIV.visibility = View.VISIBLE
        } else {
            holder.featuredIV.visibility = View.GONE
        }


        if (productsList[position].chat != null || productsList[position].chat != "") {
            holder.orderDescriptionTV.visibility = View.VISIBLE
            holder.orderDescriptionTV.text = productsList[position].chat
        } else {
            holder.orderDescriptionTV.visibility = View.GONE
        }

        holder.cUnitTV.text = "( " + productsList[position].unit + " )"
        holder.cRateTV.text = productsList[position].rate
        if (productsList[position].qty == null || productsList[position].qty == "0") {
            holder.qtyTV.text = "0"
            holder.updatePriceLayout.visibility = View.INVISIBLE
            holder.orderDescriptionTV.visibility = View.GONE
            holder.qtyTV.setTextColor(ContextCompat.getColor(context, R.color.gray))
        } else {
            holder.qtyTV.text = productsList[position].qty
            holder.updatePriceLayout.visibility = View.VISIBLE
            holder.orderDescriptionTV.visibility = View.VISIBLE
            holder.qtyTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
        }

        for (i in orderedProductsList) {
            if (i.product_id == productsList[position].product_id) {
                holder.qtyTV.text = i.qty
                holder.cQtyTV.text = (i.qty)
                if (i.discount != "" && i.discount != "0") {
                    holder.cTotalTV.text =
                        doubleToStringTwoDecimal(((i.qty).toInt() * holder.opPriceTV.text.toString().toFloat()).toDouble())
                } else {
                    holder.cTotalTV.text =
                        doubleToStringTwoDecimal(((i.qty).toInt() * i.rate.toFloat()).toDouble())
                }


                val pre = i.rate.toFloat() / 100
                val percentAmount: Float = (pre * i.discount.toFloat())
                if (i.mrp.toFloat() > 0 && i.mrp.toFloat() > i.rate.toFloat()) {
                    val mrpSA = i.mrp.toFloat() - i.rate.toFloat()
                    val savedAmount =
                        (mrpSA * i.qty.toInt()) + (percentAmount * i.qty.toInt())
                    holder.savedAmountTV.text =
                        context.resources.getString(R.string.RsSymbol) + " " + doubleToStringNoDecimal(savedAmount.toDouble()).toString()
                } else {
                    holder.savedAmountTV.text =
                        context.resources.getString(R.string.RsSymbol) + " " + doubleToStringNoDecimal(percentAmount.toDouble()).toString()
                }
                holder.updatePriceLayout.visibility = View.VISIBLE
                holder.orderDescriptionTV.visibility = View.VISIBLE
                holder.qtyTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))
            }
        }


        holder.minusTV.setOnClickListener {
            if (holder.qtyTV.text != "0") {
                val qty = holder.qtyTV.text.toString().toInt()
                holder.qtyTV.text = (qty - 1).toString()
                val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.text_change_zoom_in_out)
                holder.qtyTV.startAnimation(animation)
                holder.cQtyTV.text = (qty - 1).toString()
                productsList[position].qty = holder.qtyTV.text.toString()
                holder.qtyTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))

                if (productsList[position].discount != "" && productsList[position].discount != "0") {
                    holder.cTotalTV.text =
                        doubleToStringTwoDecimal(((qty - 1) * holder.opPriceTV.text.toString().toFloat()).toDouble())
                    /*placeOrderActivity.minusReceiver(holder.opPriceTV.text.toString().toFloat(),
                        productsList[position].product_id,
                        holder.qtyTV.text.toString().trim())*/
                    minusReceiver(
                        holder.opPriceTV.text.toString().toFloat(),
                        productsList[position].product_id,
                        holder.qtyTV.text.toString().trim(),
                        position
                    )
                } else {
                    holder.cTotalTV.text =
                        doubleToStringTwoDecimal(((qty - 1) * productsList[position].rate.toFloat()).toDouble())
                    /*placeOrderActivity.minusReceiver(productsList[position].rate.toFloat(),
                        productsList[position].product_id,
                        holder.qtyTV.text.toString().trim())*/
                    minusReceiver(
                        productsList[position].rate.toFloat(),
                        productsList[position].product_id,
                        holder.qtyTV.text.toString().trim(),
                        position
                    )
                }
                val pre = productsList[position].rate.toFloat() / 100
                val percentAmount: Float = (pre * productsList[position].discount.toFloat())
                if (productsList[position].mrp.toFloat() > 0 && productsList[position].mrp.toFloat() > productsList[position].rate.toFloat()) {
                    val mrpSA = productsList[position].mrp.toFloat() - productsList[position].rate.toFloat()
                    val savedAmount =
                        (mrpSA * holder.qtyTV.text.toString().toInt()) + (percentAmount * holder.qtyTV.text.toString().toInt())
                    holder.savedAmountTV.text =
                        context.resources.getString(R.string.RsSymbol) + " " + doubleToStringNoDecimal(savedAmount.toDouble()).toString()
                } else {
                    holder.savedAmountTV.text =
                        context.resources.getString(R.string.RsSymbol) + " " + doubleToStringNoDecimal(percentAmount.toDouble()).toString()
                }




                if (holder.qtyTV.text == "0") {
                    holder.updatePriceLayout.visibility = View.GONE
                    holder.orderDescriptionTV.visibility = View.GONE
                    holder.qtyTV.text = "0"
                    holder.qtyTV.setTextColor(ContextCompat.getColor(context, R.color.gray))

                    val pre = productsList[position].rate.toFloat() / 100
                    val percentAmount: Float = (pre * productsList[position].discount.toFloat())
                    if (productsList[position].mrp.toFloat() > 0 && productsList[position].mrp.toFloat() > productsList[position].rate.toFloat()) {
                        val mrpSA = productsList[position].mrp.toFloat() - productsList[position].rate.toFloat()
                        val savedAmount = mrpSA + percentAmount
                        holder.savedAmountTV.text =
                            context.resources.getString(R.string.RsSymbol) + " " + doubleToStringNoDecimal(savedAmount.toDouble()).toString()
                    } else {
                        holder.savedAmountTV.text =
                            context.resources.getString(R.string.RsSymbol) + " " + doubleToStringNoDecimal(percentAmount.toDouble()).toString()
                    }
                }
            }
        }
        holder.plusTV.setOnClickListener {
            if (holder.qtyTV.text != "999") {
                val qty = holder.qtyTV.text.toString().toInt()
                holder.qtyTV.text = (qty + 1).toString()
                val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.text_change_zoom_in_out)
                holder.qtyTV.startAnimation(animation)
                holder.cQtyTV.text = (qty + 1).toString()
                productsList[position].qty = holder.qtyTV.text.toString()

                if (productsList[position].discount != "" && productsList[position].discount != "0") {
                    holder.cTotalTV.text =
                        doubleToStringTwoDecimal(((qty + 1) * holder.opPriceTV.text.toString().toFloat()).toDouble())
                    /*placeOrderActivity.plusReceiver(holder.opPriceTV.text.toString().toFloat(),
                        productsList[position].product_id,
                        holder.qtyTV.text.toString().trim())*/
                    plusReceiver(
                        holder.opPriceTV.text.toString().toFloat(),
                        productsList[position].product_id,
                        holder.qtyTV.text.toString().trim(),
                        position
                    )
                } else {
                    holder.cTotalTV.text =
                        doubleToStringTwoDecimal(((qty + 1) * productsList[position].rate.toFloat()).toDouble())
                    /*placeOrderActivity.plusReceiver(productsList[position].rate.toFloat(),
                        productsList[position].product_id,
                        holder.qtyTV.text.toString().trim())*/
                    plusReceiver(
                        productsList[position].rate.toFloat(),
                        productsList[position].product_id,
                        holder.qtyTV.text.toString().trim(),
                        position
                    )
                }
                holder.updatePriceLayout.visibility = View.VISIBLE
                holder.orderDescriptionTV.visibility = View.VISIBLE
                holder.qtyTV.setTextColor(ContextCompat.getColor(context, R.color.fontColor))

                if (productsList[position].rate.toFloat() == 0f) {
                    holder.priceLayout.visibility = View.GONE
                    holder.onDemandLayout.visibility = View.VISIBLE
                }

                val pre = productsList[position].rate.toFloat() / 100
                val percentAmount: Float = (pre * productsList[position].discount.toFloat())
                if (productsList[position].mrp.toFloat() > 0 && productsList[position].mrp.toFloat() > productsList[position].rate.toFloat()) {
                    val mrpSA = productsList[position].mrp.toFloat() - productsList[position].rate.toFloat()
                    val savedAmount =
                        (mrpSA * holder.qtyTV.text.toString().toInt()) + (percentAmount * holder.qtyTV.text.toString().toInt())
                    holder.savedAmountTV.text =
                        context.resources.getString(R.string.RsSymbol) + " " + doubleToStringNoDecimal(savedAmount.toDouble()).toString()
                } else {
                    holder.savedAmountTV.text =
                        context.resources.getString(R.string.RsSymbol) + " " + doubleToStringNoDecimal(percentAmount.toDouble()).toString()
                }
            }
        }

        holder.orderDescriptionTV.setOnClickListener {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.order_description_dialog)

            val titleTV = dialog.findViewById(R.id.titleTV) as TextView
            val productDescriptionTV = dialog.findViewById(R.id.productDescriptionTV) as TextView
            val orderDescriptionET = dialog.findViewById(R.id.orderDescriptionET) as TextView
            val cancelBT = dialog.findViewById(R.id.cancelBT) as Button
            val okBT = dialog.findViewById(R.id.okBT) as Button

            titleTV.text = productsList[position].name
            if (productsList[position].description != "" || productsList[position].description != null) {
                productDescriptionTV.visibility = View.VISIBLE
                productDescriptionTV.text = productsList[position].description
            } else {
                productDescriptionTV.visibility = View.GONE
            }

            if (productsList[position].chat != "" || productsList[position].chat != null) {
                orderDescriptionET.visibility = View.VISIBLE
                orderDescriptionET.text = productsList[position].chat
            } else {
                orderDescriptionET.visibility = View.GONE
            }

            cancelBT.setOnClickListener {
                dialog.dismiss()
            }

            okBT.setOnClickListener {
                if (orderDescriptionET.text.toString().trim().isNotEmpty()) {
                    holder.orderDescriptionTV.text = orderDescriptionET.text.toString().trim()
                    productsList[position].chat = orderDescriptionET.text.toString().trim()
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "Please enter additional product description!", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            val window = dialog.window!!
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val productImageIV = view.productImageIV!!
        val productNameTV = view.productNameTV!!
        val productAliasNameTV = view.productAliasNameTV!!
        val priceTV = view.priceTV!!
        val unitTV = view.unitTV!!
        val descriptionTV = view.descriptionTV!!
        val minusTV = view.minusTV!!
        val qtyTV = view.qtyTV!!
        val plusTV = view.plusTV!!
        val cQtyTV = view.cQtyTV!!
        val cUnitTV = view.cUnitTV!!
        val cRateTV = view.cRateTV!!
        val cTotalTV = view.cTotalTV!!
        val updatePriceLayout = view.updatePriceLayout!!
        val orderDescriptionTV = view.orderDescriptionTV!!
        val featuredIV = view.featuredIV!!
        val mrpLayout = view.mrpLayout!!
        val mrpTV = view.mrpTV!!
        val percentageOffTV = view.percentageOffTV!!
        val onDemandLayout = view.onDemandLayout!!
        val priceLayout = view.priceLayout!!
        val qtyLayout = view.qtyLayout!!
        val layout = view.layout!!
        val ourPriceLayout = view.ourPriceLayout!!
        val opPriceLayout = view.opPriceLayout!!
        val opPriceTV = view.opPriceTV!!
        val opUnitTV = view.opUnitTV!!
        val opPercentageOffTV = view.opPercentageOffTV!!
        val savedAmountTV = view.savedAmountTV!!
        val mainPriceLayout = view.mainPriceLayout!!
        val oPriceLayout = view.oPriceLayout!!
        val oPriceTV = view.oPriceTV!!
        val oUnitTV = view.oUnitTV!!
        val oMrpLayout = view.oMrpLayout!!
        val oMrpTV = view.oMrpTV!!
        val oPercentageOffTV = view.oPercentageOffTV!!
    }

    private fun plusReceiver(rate: Float, product_id: String, qty: String, position: Int) {
        val intent = Intent("PlusReceiver")
        intent.putExtra("value", rate)
        intent.putExtra("product_id", product_id)
        intent.putExtra("qty", qty)
        intent.putExtra("from", from)
        intent.putExtra("data", Gson().toJson(productsList[position]))
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun minusReceiver(rate: Float, product_id: String, qty: String, position: Int) {
        val intent = Intent("MinusReceiver")
        intent.putExtra("value", rate)
        intent.putExtra("product_id", product_id)
        intent.putExtra("qty", qty)
        intent.putExtra("from", from)
        intent.putExtra("data", Gson().toJson(productsList[position]))
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun doubleToStringNoDecimal(d: Double): String? {
        val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,##,###.##")
        return formatter.format(d)
    }

    private fun doubleToStringTwoDecimal(d: Double): String? {
        val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,##,###.00")
        return formatter.format(d)
    }
}