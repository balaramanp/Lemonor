package `in`.inferon.msl.lemonor.model.pojo

import com.google.gson.annotations.SerializedName

data class Order(
    @SerializedName("order_id") val order_id: String,
    @SerializedName("user_id") val user_id: String,
    @SerializedName("product_id") val product_id: String,
    @SerializedName("delivery_boy_id") val delivery_boy_id: String,
    @SerializedName("supplier_id") var supplier_id: String,
    @SerializedName("bill_complete") val bill_complete: String,
    @SerializedName("qty") var qty: String,
    @SerializedName("product_name") var product_name: String,
    @SerializedName("delivery_datetime") var delivery_datetime: String,
    @SerializedName("item_count") var item_count: String,
    @SerializedName("price") var price: String,
    @SerializedName("net_price") var net_price: String,
    @SerializedName("tax1") var tax1: String,
    @SerializedName("tax2") var tax2: String,
    @SerializedName("tax3") var tax3: String,
    @SerializedName("discounts") var discounts: String,
    @SerializedName("added_datetime") var added_datetime: String,
    @SerializedName("token_number") var token_number: String,
    @SerializedName("user_cancelled") var user_cancelled: String,
    @SerializedName("supplier_accepted") var supplier_accepted: String,
    @SerializedName("packed") var packed: String,
    @SerializedName("out_for_delivery") var out_for_delivery: String,
    @SerializedName("delivered") var delivered: String,
    @SerializedName("cash_received") var cash_received: String,
    @SerializedName("user_received") var user_received: String,
    @SerializedName("user_cancelled_datetime") var user_cancelled_datetime: String,
    @SerializedName("supplier_accepted_datetime") var supplier_accepted_datetime: String,
    @SerializedName("packed_datetime") var packed_datetime: String,
    @SerializedName("out_for_delivery_datetime") var out_for_delivery_datetime: String,
    @SerializedName("delivered_datetime") var delivered_datetime: String,
    @SerializedName("cash_received_datetime") var cash_received_datetime: String,
    @SerializedName("user_received_datetime") var user_received_datetime: String,
    @SerializedName("user_name") var user_name: String,
    @SerializedName("mobile_number") var mobile_number: String,
    @SerializedName("contact_person_name") var contact_person_name: String,
    @SerializedName("shop_name") var shop_name: String,
    @SerializedName("shop_mobile_number") var shop_mobile_number: String,
    @SerializedName("unit") var unit: String,
    @SerializedName("rate") var rate: String,
    @SerializedName("formatted_date") var formatted_date: String,
    @SerializedName("formatted_time") var formatted_time: String,
    @SerializedName("order_status") var order_status: String,
    @SerializedName("description") var description: String,
    @SerializedName("chat") var chat: String,
    @SerializedName("local_name") var local_name: String,
    @SerializedName("order_modified") var order_modified: Boolean,
    @SerializedName("order_modification_saved") var order_modification_saved: String,
    @SerializedName("is_rated") var is_rated: String
) {

}