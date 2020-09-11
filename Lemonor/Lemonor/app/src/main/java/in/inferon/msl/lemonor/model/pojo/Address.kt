package `in`.inferon.msl.lemonor.model.pojo

import com.google.gson.annotations.SerializedName

data class Address(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") var user_id: String,
    @SerializedName("first_name") var first_name: String,
    @SerializedName("last_name") var last_name: String,
    @SerializedName("address_line_1") var address_line_1: String,
    @SerializedName("address_line_2") var address_line_2: String,
    @SerializedName("city") var city: String,
    @SerializedName("district") var district: String,
    @SerializedName("state") var state: String,
    @SerializedName("country") var country: String,
    @SerializedName("zip_code") var zip_code: String,
    @SerializedName("phone_no1") var phone_no1: String,
    @SerializedName("phone_no2") var phone_no2: String,
    @SerializedName("landmark") var landmark: String,
    @SerializedName("lat_lng") var lat_lng: String,
    @SerializedName("default") var default: String,
    @SerializedName("disabled") var disabled: String
) {

}