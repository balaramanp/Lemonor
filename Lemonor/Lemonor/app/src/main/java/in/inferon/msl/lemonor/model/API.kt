package `in`.inferon.msl.lemonor.model

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface API {

    @FormUrlEncoded
    @POST("user/check_email_exists")
    fun checkEmailExist(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/new_register")
    fun register(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/update_profile")
    fun updateProfile(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("contents/support_by_mail")
    fun supportByMail(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/enable_supplier")
    fun enableSupplier(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/disable_supplier")
    fun disableSupplier(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/save_supplier_data")
    fun saveSupplierData(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/get_goods_list")
    fun getGoodsList(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/save_supplier_goods_list")
    fun saveSupplierGoodsList(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("products/get_products_by_supplier_id")
    fun getProductsBySupplierID(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/get_products_list_for_supplier_to_edit")
    fun getProductsListForSupplierToEdit(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/enable_supplier_product")
    fun enableSupplierProduct(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/disable_supplier_product")
    fun disableSupplierProduct(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/save_supplier_item")
    fun saveSupplierItem(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/get_supplier_profile_data_by_id")
    fun getSupplierProfileDataById(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/update_supplier_data")
    fun updateSupplierData(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/get_supplier_list_for_customer")
    fun getSupplierListForCustomer(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/get_supplier_list_for_customer_without_shop_timing")
    fun getSupplierListForCustomerWithoutShopTiming(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/get_products_list_by_supplier_id_for_customer")
    fun getProductsListBySupplierIdForCustomer(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/place_an_order")
    fun placeAnOrder(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/get_order_list_by_supplier_id")
    fun getOrderListBySupplierID(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/get_order_list_by_user_id_for_customer")
    fun getOrderListByUserIdForCustomer(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/remove_supplier_product")
    fun removeSupplierProduct(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/get_init_data")
    fun getInitData(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/save_supplier_goods_data")
    fun saveSupplierGoodsData(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/get_state_district_list")
    fun getStateDistrictList(@Field("data") data: String): Call<ResponseBody>

    @GET("about_us.json")
    fun getAboutUs(): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/update_user_cancelled")
    fun updateUserCancelled(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/update_supplier_accepted")
    fun updateSupplierAccepted(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/update_supplier_rejected")
    fun updateSupplierRejected(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/order_completed")
    fun orderCompleted(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/update_supplier_accepted_by_token_number")
    fun updateSupplierAcceptedByTokenNumber(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/update_supplier_rejected_by_token_number")
    fun updateSupplierRejectedByTokenNumber(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/update_order_completed_by_token_number")
    fun updateSupplierCompletedByTokenNumber(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/get_order_history_list")
    fun getOrderHistoryList(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("acra/activity_logging.php")
    fun activityLogging(@Field("app_version") app_version: String, @Field("module") module: String, @Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/update_o2_price_from_supplier_in_order")
    fun updateO2PriceFromSupplierInOrder(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/get_order_by_token_id")
    fun getOrderByTokenId(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/get_is_supplier_reachable_for_customer")
    fun getIsSupplierReachableForCustomer(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/chat")
    fun chat(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/get_public_key")
    fun getPublicKey(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/get_app_content")
    fun getAppContent(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/update_supplier_product_rate")
    fun updateSupplierProductRate(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/update_all_supplier_product_rate")
    fun updateAllSupplierProductRate(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/get_order_opened_by_token_number")
    fun getOrderOpenedByTokenNumber(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/update_order_details_from_supplier")
    fun updateOrderDetailsFromSupplier(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/get_category_filtered_products_list")
    fun getCategoryFilteredProductsList(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/get_search_filtered_products_list")
    fun getSearchFilteredProductsList(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/get_goods_list_by_search_term")
    fun getGoodsListBySearchTerm(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/get_goods_list_by_category_filter")
    fun getGoodsListByCategoryFilter(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/get_products_list_by_category_filter_for_supplier_to_edit")
    fun getProductsListByCategoryFilterForSupplierToEdit(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/get_products_list_by_search_term_for_supplier_to_edit")
    fun getProductsListBySearchTermForSupplierToEdit(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("supplier/check_supplier_exists")
    fun checkSupplierExists(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/update_fcm_token")
    fun updateFCMToken(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/update_bill_rating")
    fun updateBillRating(@Field("data") data: String): Call<ResponseBody>

    @GET("assets/sm/sm.json")
    fun safetyMeasurement(): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/get_user_profile_by_user_id")
    fun getUserProfileByUserID(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/add_address")
    fun addAddress(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/edit_address")
    fun editAddress(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("user/delete_address")
    fun deleteAddress(@Field("data") data: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("customer/get_supplier_id_for_staff")
    fun getSupplierIdForStaff(@Field("data") data: String): Call<ResponseBody>
}