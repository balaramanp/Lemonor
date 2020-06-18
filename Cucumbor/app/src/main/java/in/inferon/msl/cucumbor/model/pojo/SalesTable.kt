package `in`.inferon.msl.cucumbor.model.pojo

import com.google.gson.annotations.SerializedName

data class SalesTable(
    @SerializedName("date") val date: String,
    @SerializedName("am") val am: String,
    @SerializedName("pm") val pm: String,
    @SerializedName("price_am") val price_am: String,
    @SerializedName("price_pm") val price_pm: String
) {

}