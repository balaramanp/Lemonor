package `in`.inferon.msl.lemonor.model.pojo

import com.google.gson.annotations.SerializedName

data class OrderList(
    @SerializedName("productsList") val productsList: MutableList<Order>,
    @SerializedName("total") val total: String,
    @SerializedName("supplier_discount") val supplier_discount: String
) {

}