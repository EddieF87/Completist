package xyz.sleekstats.completist.model

import com.google.gson.annotations.SerializedName

open class MediaPOJO {
    @SerializedName(value = "title", alternate = ["name"])
    var title: String = ""

    @SerializedName("overview")
    val overview: String = ""

    @SerializedName("poster_path")
    val poster_path: String = ""

    @SerializedName("genres")
    val genres: List<Genre> = emptyList()

    @SerializedName("credits")
    val castCredits: CastCredits? = null

    @SerializedName("id")
    var id: String = ""

    @SerializedName("imdb_id")
    val imdb_id: String = ""

    @SerializedName("vote_average")
    val vote_average = 0f

    @SerializedName("media_type")
    val media_type: String = ""

    @SerializedName(value = "release_date", alternate = ["first_air_date"])
    val release_date: String = ""

    @SerializedName("last_air_date")
    val last_date: String = ""

    @SerializedName(value = "runtime")
    val runtime = 0

    @SerializedName("number_of_seasons")
    val number_of_seasons = 0

    @SerializedName("number_of_episodes")
    val number_of_episodes = 0

    @SerializedName("similar")
    val similar: ResultsPOJO? = null

    open var isFilm = false

    var isWatched = false
    var isQueued = false

    fun reverseWatched() {
        isWatched = !isWatched
    }

    fun reverseQueued() {
        isQueued = !isQueued
    }

}