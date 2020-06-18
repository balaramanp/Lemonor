package `in`.inferon.msl.cucumbor.model.pojo

import com.google.gson.annotations.SerializedName

data class GroceryOrderList(
    @SerializedName("milk_type_id") val milk_type_id: String,
    @SerializedName("milk_type_name") val milk_type_name: String,
    @SerializedName("qty") val qty: String,
    @SerializedName("show_unit") val show_unit: String,
    @SerializedName("price") var price: String
) {

}