package `in`.inferon.msl.lemonor.model

import `in`.inferon.msl.lemonor.R
import `in`.inferon.msl.lemonor.view.activity.MainFragmentActivity
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import android.net.Uri
import android.content.ContentResolver
import android.media.AudioAttributes
import android.app.NotificationChannel


class FireBaseService : FirebaseMessagingService() {

    private val TAG = FireBaseService::class.java.simpleName
    private val PREF = "Pref"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.e(TAG, "Remote Message : ${remoteMessage.data}")

        val data = JSONObject(remoteMessage.data as Map<*, *>)
        showNotification(data)
    }

    override fun onMessageSent(p0: String) {
        super.onMessageSent(p0)
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    override fun onSendError(p0: String, p1: Exception) {
        super.onSendError(p0, p1)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e(TAG, "Token : $token")
        val editor = getSharedPreferences(PREF, AppCompatActivity.MODE_PRIVATE).edit()
        editor.putString("fcm_token", token)
        editor.apply()
    }

    private fun showNotification(data: JSONObject) {
        createNotificationChannel(data, this)
    }


    private fun createNotificationChannel(data: JSONObject, context: Context) {
        val tokid = data.getString("token_number").replace("-", "").replace("ORDR_", "")
        val NOTIFY_ID = tokid.toInt() // ID of notification
        val id = "my_channel_01" // default_channel_id
        val title = "my_channel_01" // Default Channel
        val intent: Intent
        val pendingIntent: PendingIntent
        val builder: NotificationCompat.Builder
        val sound =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/raw/just_saying.mp3")

        /*if (notificationManager == null) {
            notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }*/
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val attributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            var mChannel: NotificationChannel? = notificationManager!!.getNotificationChannel(id)
            if (mChannel == null) {
                mChannel = NotificationChannel(id, title, importance)
                mChannel.enableVibration(true)
                mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                mChannel.setSound(sound, attributes)
                notificationManager!!.createNotificationChannel(mChannel)
            }
            builder = NotificationCompat.Builder(context, id)
            intent = Intent(context, MainFragmentActivity::class.java)
            /*intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)*/
            intent.putExtra("from", "Notification")
            intent.putExtra("for", data.getString("for"))
            intent.putExtra("token_no", data.getString("token_number"))
            intent.putExtra("added_datetime", data.getString("added_datetime"))
            intent.putExtra("user_id", data.getString("user_id"))
            intent.putExtra("data", data.toString())
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            pendingIntent = PendingIntent.getActivity(
                this, NOTIFY_ID, intent,
                PendingIntent.FLAG_ONE_SHOT
            )
            var contentTitle = ""
            var content = ""
            if (data.getString("for") == "order") {
                contentTitle =
                    "New order from " + data.getString("user_name") + " (" + data.getString("token_number") + ")"
                content =
                    "New order received from " + data.getString("user_name") + " (" + data.getString("user_mobile_number") +
                            ") \nOrder value : Rs. " + data.getString("discounted_total")
            } else if (data.getString("for") == "supplier_accepted") {
                contentTitle = "Order Accepted (" + data.getString("token_number") + ")"
                content =
                    data.getString("shop_name") + " have accepted your order (" + data.getString("token_number") + ")  Order Value : Rs." +
                            data.getString("discounted_total")
            } else if (data.getString("for") == "supplier_rejected") {
                contentTitle = "Order Cancelled (" + data.getString("token_number") + ")"
                content =
                    "Sorry, " + data.getString("shop_name") + " have cancelled your order (" + data.getString("token_number") + ")  Order Value : Rs." +
                            data.getString("discounted_total")
            } else if (data.getString("for") == "order_completed") {
                contentTitle = "Order has been Packed and Out for Delivery (" + data.getString("token_number") + ")"
                content =
                    data.getString("shop_name") + " delivered your order (" + data.getString("token_number") + ")  Order Value : Rs." +
                            data.getString("discounted_total")
            } else if (data.getString("for") == "supplier_rejected_by_order_id") {
                contentTitle = "One of the Product Unavailable (" + data.getString("order_id") + ")"
                if (data.getString("product_name") == "Open Order") {
                    content =
                        data.getString("shop_name") + " (" + data.getString("supplier_mobile_number") + ") have rejected " +
                                data.getString("product_name") + "\n" + " Order Value : Rs." + data.getString("price")
                } else {
                    content =
                        data.getString("shop_name") + " (" + data.getString("supplier_mobile_number") + ") have rejected " +
                                data.getString("product_name") + " " + data.getString("product_qty") + " " + data.getString(
                            "product_unit"
                        ) + "\n" + " Order Value : Rs." + data.getString("price")
                }
            } else if (data.getString("for") == "chat_supplier") {
                contentTitle = "Item Description Update (" + data.getString("token_number") + ")"
                content = data.getString("product_name") + " : " + data.getString("chat_content")
            } else if (data.getString("for") == "chat_customer") {
                contentTitle = "Item Description Update (" + data.getString("token_number") + ")"
                content = data.getString("product_name") + " : " + data.getString("chat_content")
            } else if (data.getString("for") == "update_order_detail") {
                contentTitle = "One of the Product Updated (" + data.getString("token_number") + ")"
                if (data.getString("product_name") == "Open Order") {
                    content =
                        data.getString("shop_name") + " (" + data.getString("supplier_mobile_number") + ") have updated " +
                                data.getString("product_name") + "\n" + " Order Value : Rs." + data.getString("price")
                } else {
                    content =
                        data.getString("shop_name") + " (" + data.getString("supplier_mobile_number") + ") have updated " +
                                data.getString("product_name") + " " + data.getString("product_qty") + " " + data.getString(
                            "product_unit"
                        ) + "\n" + " Order Value : Rs." + data.getString("price")
                }
            }
//            val uri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.just_saying)
            builder.setContentTitle(contentTitle)                            // required
                .setSmallIcon(R.drawable.notificationmini)   // required
                //                .setContentText(content) // required
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(content)
                )
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setChannelId(id)
                .setContentIntent(pendingIntent)
                //                .setSound(uri)
                //                .setTicker(aMessage)
                .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
                .color = context.resources.getColor(R.color.buttonColor)
        } else {
            builder = NotificationCompat.Builder(context, id)
            intent = Intent(context, MainFragmentActivity::class.java)
            /*intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)*/
            intent.putExtra("from", "Notification")
            intent.putExtra("for", data.getString("for"))
            intent.putExtra("token_no", data.getString("token_number"))
            intent.putExtra("added_datetime", data.getString("added_datetime"))
            intent.putExtra("user_id", data.getString("user_id"))
            intent.putExtra("data", data.toString())
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            pendingIntent = PendingIntent.getActivity(
                this, NOTIFY_ID, intent,
                PendingIntent.FLAG_ONE_SHOT
            )
            var contentTitle = ""
            var content = ""
            if (data.getString("for") == "order") {
                contentTitle =
                    "New order from " + data.getString("user_name") + " (" + data.getString("token_number") + ")"
                content =
                    "New order received from " + data.getString("user_name") + " (" + data.getString("user_mobile_number") +
                            ") \nOrder Value : Rs." + data.getString("discounted_total")
            } else if (data.getString("for") == "supplier_accepted") {
                contentTitle = "Order Accepted (" + data.getString("token_number") + ")"
                content =
                    data.getString("shop_name") + " have accepted your order (" + data.getString("token_number") + ")  Order Value : Rs." +
                            data.getString("discounted_total")
            } else if (data.getString("for") == "supplier_rejected") {
                contentTitle = "Order Cancelled (" + data.getString("token_number") + ")"
                content =
                    "Sorry, " + data.getString("shop_name") + " have cancelled your order (" + data.getString("token_number") + ")  Order Value : Rs." +
                            data.getString("discounted_total")
            } else if (data.getString("for") == "order_completed") {
                contentTitle = "Order has been Packed and Out for Delivery (" + data.getString("token_number") + ")"
                content =
                    data.getString("shop_name") + " delivered your order (" + data.getString("token_number") + ")  Order Value : Rs." +
                            data.getString("discounted_total")
            } else if (data.getString("for") == "supplier_rejected_by_order_id") {
                contentTitle = "One of the Product Unavailable (" + data.getString("order_id") + ")"
                if (data.getString("product_name") == "Open Order") {
                    content =
                        data.getString("shop_name") + " (" + data.getString("supplier_mobile_number") + ") have rejected " +
                                data.getString("product_name") + "\n" + " Order Value : Rs." + data.getString("price")
                } else {
                    content =
                        data.getString("shop_name") + " (" + data.getString("supplier_mobile_number") + ") have rejected " +
                                data.getString("product_name") + " " + data.getString("product_qty") + " " + data.getString(
                            "product_unit"
                        ) + "\n" + " Order Value : Rs." + data.getString("price")
                }
            } else if (data.getString("for") == "chat_supplier") {
                contentTitle = "Item Description Update (" + data.getString("token_number") + ")"
                content = data.getString("product_name") + " : " + data.getString("chat_content")
            } else if (data.getString("for") == "chat_customer") {
                contentTitle = "Item Description Update (" + data.getString("token_number") + ")"
                content = data.getString("product_name") + " : " + data.getString("chat_content")
            } else if (data.getString("for") == "update_order_detail") {
                contentTitle = "One of the Product Updated (" + data.getString("token_number") + ")"
                if (data.getString("product_name") == "Open Order") {
                    content =
                        data.getString("shop_name") + " (" + data.getString("supplier_mobile_number") + ") have updated " +
                                data.getString("product_name") + "\n" + " Order Value : Rs." + data.getString("price")
                } else {
                    content =
                        data.getString("shop_name") + " (" + data.getString("supplier_mobile_number") + ") have updated " +
                                data.getString("product_name") + " " + data.getString("product_qty") + " " + data.getString(
                            "product_unit"
                        ) + "\n" + " Order Value : Rs." + data.getString("price")
                }
            }
//            val uri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.just_saying)
            builder.setContentTitle(contentTitle)                            // required
                .setSmallIcon(R.drawable.notificationmini)   // required
//                .setContentText(content) // required
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(content)
                )
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(sound)
//                .setTicker(aMessage)
                .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
                .color = context.resources.getColor(R.color.buttonColor)
        }
        val notification = builder.build()
        notificationManager!!.notify(NOTIFY_ID, notification)
    }

}
