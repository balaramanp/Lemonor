package `in`.inferon.msl.lemonor.model.pojo

import com.google.gson.annotations.SerializedName

data class Products(
    @SerializedName("id") val id: String,
    @SerializedName("product_id") val product_id: String,
    @SerializedName("product_name") var product_name: String,
    @SerializedName("supplier_id") val supplier_id: String,
    @SerializedName("name") var name: String,
    @SerializedName("local_name") var local_name: String,
    @SerializedName("language_name") var language_name: String,
    @SerializedName("unit") var unit: String,
    @SerializedName("available_status") val available_status: String,
    @SerializedName("timing") var timing: String,
    @SerializedName("description") var description: String,
    @SerializedName("min_qty") var min_qty: String,
    @SerializedName("max_qty") var max_qty: String,
    @SerializedName("increment_qty") var increment_qty: String,
    @SerializedName("limit_qty") var limit_qty: String,
    @SerializedName("supplier_products_buffer") var supplier_products_buffer: String,
    @SerializedName("added_datetime") var added_datetime: String,
    @SerializedName("category") var category: String,
    @SerializedName("stock_status") var stock_status: String,
    @SerializedName("rate") var rate: String,
    @SerializedName("qty") var qty: String,
    @SerializedName("chat") var chat: String,
    @SerializedName("price_changed") var price_changed: Boolean,
    @SerializedName("updated_rate") var updated_rate: String,
    @SerializedName("new_product") var new_product: Boolean,
    @SerializedName("is_supplier_product") var is_supplier_product: Boolean,
    @SerializedName("featured_product_flag") var featured_product_flag: Boolean,
    @SerializedName("unit_value_changed") var unit_value_changed: Boolean,
    @SerializedName("mrp") var mrp: String
) {

}