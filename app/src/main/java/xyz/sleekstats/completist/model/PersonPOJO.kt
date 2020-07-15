package xyz.sleekstats.completist.model

import com.google.gson.annotations.SerializedName

class PersonPOJO(@field:SerializedName("name") var name: String, @field:SerializedName("biography") val biography: String, @field:SerializedName("known_for_department") val known_for_department: String, @field:SerializedName("profile_path") val profile_path: String, @field:SerializedName("id") val id: String) {
    @SerializedName("movie_credits")
    val movieCredits: MovieCredits? = null

    @SerializedName("tv_credits")
    val tvCredits: ShowCredits? = null

    @SerializedName("imdb_id")
    val imdb_id: String = ""

}