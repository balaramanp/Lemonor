package `in`.inferon.msl.cucumbor.model.pojo

import com.google.gson.annotations.SerializedName

data class GroceryHistoryItem (
    @SerializedName("product_name") val product_name: String,
    @SerializedName("product_id") val product_id: String,
    @SerializedName("qty") val qty: String,
    @SerializedName("price") val price: String,
    @SerializedName("net_price") val net_price: String,
    @SerializedName("category") val category: String,
    @SerializedName("unit") val unit: String,
    @SerializedName("projection_id") val projection_id: String
){}