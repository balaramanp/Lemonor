package `in`.inferon.msl.cucumbor.model.pojo

import `in`.inferon.msl.cucumbor.model.pojo.BillDetails
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class TotalSalesTable(
    @SerializedName("date") val date: String,
    @SerializedName("am") val am: ArrayList<BillDetails>,
    @SerializedName("pm") val pm: ArrayList<BillDetails>,
    @SerializedName("price_am") val price_am: String,
    @SerializedName("price_pm") val price_pm: String
) {

}