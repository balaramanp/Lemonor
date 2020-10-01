package `in`.inferon.msl.lemonor.model.pojo

import com.google.gson.annotations.SerializedName

data class OrderHistory(
    @SerializedName("is_completed") val is_completed: String,
    @SerializedName("supplier_discount") val supplier_discount: String,
    @SerializedName("products_list") var products_list: MutableList<Order>
) {

}