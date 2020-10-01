package `in`.inferon.msl.lemonor.model.pojo

import com.google.gson.annotations.SerializedName

data class MajorCategory(
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("is_product_exists") val isProductExists: Boolean
) {

}