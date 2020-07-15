package xyz.sleekstats.completist.model

import com.google.gson.annotations.SerializedName

class MediaQueryPOJO {
    @SerializedName("results")
    val results: List<MediaPOJO> = emptyList()
}