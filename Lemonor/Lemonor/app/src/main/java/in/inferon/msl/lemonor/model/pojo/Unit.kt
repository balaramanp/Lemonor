package `in`.inferon.msl.lemonor.model.pojo

import com.google.gson.annotations.SerializedName

data class Unit(
    @SerializedName("unit") val unit: String,
    @SerializedName("selected") var selected: Boolean = false,
    @SerializedName("name") var name: String
) {

}