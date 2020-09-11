package `in`.inferon.msl.lemonor.model.pojo

import com.google.gson.annotations.SerializedName

data class Goods(
    @SerializedName("id") val id: String,
    @SerializedName("added_datetime") val added_datetime: String,
    @SerializedName("product_name") val product_name: String,
    @SerializedName("local_name") val local_name: String,
    @SerializedName("alias_name") val alias_name: String,
    @SerializedName("other_language_names") var other_language_names: String,
    @SerializedName("category") val category: String,
    @SerializedName("img_url") val img_url: String,
    @SerializedName("unit") var unit: String,
    @SerializedName("rate") var rate: String,
    @SerializedName("description") var description: String,
    @SerializedName("stock_status") var stock_status: String,
    @SerializedName("new_product") var new_product: Boolean,
    @SerializedName("is_supplier_product") var is_supplier_product: Boolean
) {

}