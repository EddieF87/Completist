package xyz.sleekstats.completist.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.util.*

class GenreList private constructor(`in`: Parcel) : Parcelable {
    @SerializedName("genres")
    var genres: List<Genre?> = emptyList()

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte(0x01.toByte())
        dest.writeList(genres)
    }

    companion object {
        val CREATOR: Parcelable.Creator<GenreList?> = object : Parcelable.Creator<GenreList?> {
            override fun createFromParcel(`in`: Parcel): GenreList? {
                return GenreList(`in`)
            }

            override fun newArray(size: Int): Array<GenreList?> {
                return arrayOfNulls(size)
            }
        }
    }

    init {
        if (`in`.readByte().toInt() == 0x01) {
            genres = ArrayList()
            `in`.readList(genres, Genre::class.java.classLoader)
        }
    }
}