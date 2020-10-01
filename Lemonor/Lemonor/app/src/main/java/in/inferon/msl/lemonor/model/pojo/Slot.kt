package `in`.inferon.msl.lemonor.model.pojo

import com.google.gson.annotations.SerializedName

data class Slot(
    @SerializedName("value") val value: String,
    @SerializedName("date_text") var date_text: String,
    @SerializedName("is_selected") var is_selected: Boolean,
    @SerializedName("date") var date: String
) {

}