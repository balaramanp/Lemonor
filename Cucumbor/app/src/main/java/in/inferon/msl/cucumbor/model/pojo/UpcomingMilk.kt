package `in`.inferon.msl.cucumbor.model.pojo

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class UpcomingMilk(
    @SerializedName("qty") val qty: String,
    @SerializedName("date") val date: String,
    @SerializedName("noon") val noon: String,
    @SerializedName("from") val from: String,
    @SerializedName("price") val price: Float,
    @SerializedName("quarter") val quarter: String,
    @SerializedName("half") val half: String,
    @SerializedName("one") val one: String,
    @SerializedName("status") val status: String,
    @SerializedName("edit") val edit: String
) : Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readFloat(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(qty)
        parcel.writeString(date)
        parcel.writeString(noon)
        parcel.writeString(from)
        parcel.writeFloat(price)
        parcel.writeString(quarter)
        parcel.writeString(half)
        parcel.writeString(one)
        parcel.writeString(status)
        parcel.writeString(edit)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UpcomingMilk> {
        override fun createFromParcel(parcel: Parcel): UpcomingMilk {
            return UpcomingMilk(parcel)
        }

        override fun newArray(size: Int): Array<UpcomingMilk?> {
            return arrayOfNulls(size)
        }
    }
}