package xyz.sleekstats.completist.model

import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.collections.HashSet

class MovieCredits {
    @SerializedName("cast")
    val cast: Set<FilmByPerson> = emptySet()

    @SerializedName("crew")
    val crew: Set<FilmByPerson> = emptySet()
        get() = filterCrew(field)

    private var both: Set<FilmByPerson>? = null

    fun bothLists(): Set<FilmByPerson>? {
        if (both == null) {
            val filmByPersonSet: MutableSet<FilmByPerson> = HashSet(cast)
            filmByPersonSet.addAll(crew)
            both = filmByPersonSet
        }
        return both
    }

    private fun filterCrew(unfiltered: Set<FilmByPerson>): Set<FilmByPerson> = unfiltered.filterTo(HashSet()) { filteredCrewJobs.contains(it.job) }

    companion object {
        private val filteredCrewJobs = arrayOf("Director", "Writer", "Screenplay")
    }
}