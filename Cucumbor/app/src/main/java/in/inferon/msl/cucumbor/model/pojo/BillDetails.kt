package `in`.inferon.msl.cucumbor.model.pojo

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class BillDetails(
    @SerializedName("qty") val qty: String,
    @SerializedName("product_name") val product_name: String,
    @SerializedName("price") val price: String,
    @SerializedName("net_price") val net_price: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(qty)
        parcel.writeString(product_name)
        parcel.writeString(price)
        parcel.writeString(net_price)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BillDetails> {
        override fun createFromParcel(parcel: Parcel): BillDetails {
            return BillDetails(parcel)
        }

        override fun newArray(size: Int): Array<BillDetails?> {
            return arrayOfNulls(size)
        }
    }
}