package `in`.inferon.msl.lemonor.model.pojo

import com.google.gson.annotations.SerializedName

data class Supplier(
    @SerializedName("user_id") val user_id: String,
    @SerializedName("added_datetime") val added_datetime: String,
    @SerializedName("shop_name") val shop_name: String,
    @SerializedName("address") val address: String,
    @SerializedName("city") var city: String,
    @SerializedName("district") val district: String,
    @SerializedName("state") val state: String,
    @SerializedName("country") var country: String,
    @SerializedName("pincode") var pincode: String,
    @SerializedName("location") var location: String,
    @SerializedName("mobile_number") var mobile_number: String,
    @SerializedName("alternate_number") var alternate_number: String,
    @SerializedName("contact_person_name") var contact_person_name: String,
    @SerializedName("status") var status: String,
    @SerializedName("open_timing") var open_timing: String,
    @SerializedName("close_timing") var close_timing: String,
    @SerializedName("shop_details_buffer") var shop_details_buffer: String
) {

}