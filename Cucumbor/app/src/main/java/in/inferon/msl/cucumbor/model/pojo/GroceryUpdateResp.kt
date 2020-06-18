package `in`.inferon.msl.cucumbor.model.pojo

import com.google.gson.annotations.SerializedName

data class GroceryUpdateResp(
    @SerializedName("status") val status: String,
    @SerializedName("product_name") val product_name: String
) {

}