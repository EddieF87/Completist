package xyz.sleekstats.completist.model

import com.google.gson.annotations.SerializedName

class ResultsPOJO {
    @SerializedName("results")
    val results: List<MediaByPerson> = emptyList()
}