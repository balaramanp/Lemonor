package `in`.inferon.msl.lemonor.model.pojo

import com.google.gson.annotations.SerializedName

data class SuppliersForCustomer(
    @SerializedName("location") val location: String,
    @SerializedName("user_id") val user_id: String,
    @SerializedName("shop_name") val shop_name: String,
    @SerializedName("shop_description") val shop_description: String,
    @SerializedName("mobile_number") val mobile_number: String,
    @SerializedName("address") val address: String,
    @SerializedName("state") val state: String,
    @SerializedName("district") val district: String,
    @SerializedName("distance") val distance: String,
    @SerializedName("supplier_rating") val supplier_rating: String
) {

}