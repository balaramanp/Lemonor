package `in`.inferon.msl.cucumbor.model.pojo

import com.google.gson.annotations.SerializedName

data class GroceryHistory (
    @SerializedName("date") val date: String,
    @SerializedName("items") val items: ArrayList<GroceryHistoryItem>,
    @SerializedName("total_price") val total_price: String,
    @SerializedName("show_remove_btn") val show_remove_btn: Boolean
){}