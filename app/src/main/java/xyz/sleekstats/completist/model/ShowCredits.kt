package xyz.sleekstats.completist.model

import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.collections.HashSet

class ShowCredits {
    @SerializedName("cast")
    val cast: Set<ShowByPerson> = emptySet()

    @SerializedName("crew")
    private val crew: Set<ShowByPerson> = emptySet()
        get() = filterCrew(field)

    private var both: Set<ShowByPerson>? = null

    fun bothLists(): Set<ShowByPerson>? {
        if (both == null) {
            val showByPersonSet: MutableSet<ShowByPerson> = HashSet(cast)
            showByPersonSet.addAll(crew)
            both = showByPersonSet
        }
        return both
    }

    private fun filterCrew(unfiltered: Set<ShowByPerson>): Set<ShowByPerson> = unfiltered.filterTo(HashSet()) { filteredCrewJobs.contains(it.job) }

    companion object {
        private val filteredCrewJobs = arrayOf("Director", "Writer", "Screenplay")
    }
}