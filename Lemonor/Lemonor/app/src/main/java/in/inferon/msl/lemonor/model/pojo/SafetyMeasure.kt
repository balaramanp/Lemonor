package `in`.inferon.msl.lemonor.model.pojo

import com.google.gson.annotations.SerializedName

data class SafetyMeasure(
    @SerializedName("title") val title: String,
    @SerializedName("content") var content: String,
    @SerializedName("img") var img: String
) {

}