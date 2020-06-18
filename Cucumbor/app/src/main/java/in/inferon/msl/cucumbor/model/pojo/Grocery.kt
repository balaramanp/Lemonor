package `in`.inferon.msl.cucumbor.model.pojo

import com.google.gson.annotations.SerializedName

data class Grocery(
    @SerializedName("id") val id: String,
    @SerializedName("datetime") val datetime: String,
    @SerializedName("unit") val unit: String,
    @SerializedName("description") val description: String,
    @SerializedName("qty") var qty: String,
    @SerializedName("price_range") val price_range: String,
    @SerializedName("location") val location: String,
    @SerializedName("lp1") val lp1: String,
    @SerializedName("lp2") val lp2: String,
    @SerializedName("name") val name: String,
    @SerializedName("cf") val cf: String,
    @SerializedName("mq") val mq: String,
    @SerializedName("iq") val iq: String,
    @SerializedName("show_unit") val show_unit: String,
    @SerializedName("calculated_price") var calculated_price: Int,
    @SerializedName("category") val category: String
) {

}