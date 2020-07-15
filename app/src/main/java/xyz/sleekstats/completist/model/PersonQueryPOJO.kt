package xyz.sleekstats.completist.model

import com.google.gson.annotations.SerializedName

class PersonQueryPOJO {
    @SerializedName("results")
    val results: List<PersonPOJO>? = null
}