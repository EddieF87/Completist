package xyz.sleekstats.completist.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "movie_table")
open class MediaByPerson(
        @SerializedName(value = "title", alternate = ["name"]) var title: String? = "",
        @NonNull @PrimaryKey @SerializedName("id") var id: String,
        @SerializedName("poster_path") var poster_path: String?,
        @SerializedName("job") var job: String? = null
) {

    open var isFilm = true
    var isWatched = false
    var isQueued = false
    var ranking = -1

    fun reverseWatched() {
        isWatched = !isWatched
    }

    fun reverseQueued() {
        isQueued = !isQueued
    }

    override fun equals(other: Any?): Boolean {
        if (other !is MediaByPerson) {
            return false
        }
        return other.title == title
    }

    override fun hashCode(): Int {
        return Objects.hash(title)
    }
}