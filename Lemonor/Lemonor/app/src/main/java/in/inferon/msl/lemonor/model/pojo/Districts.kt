package `in`.inferon.msl.lemonor.model.pojo

import com.google.gson.annotations.SerializedName

data class Districts(
    @SerializedName("state") val state: String,
    @SerializedName("district") val district: String
) {

}