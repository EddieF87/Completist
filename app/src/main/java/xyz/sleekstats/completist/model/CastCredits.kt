package xyz.sleekstats.completist.model

import com.google.gson.annotations.SerializedName
import java.util.*

class CastCredits {
    @SerializedName("cast")
    val cast: Set<CastInfo> = emptySet()

    @SerializedName("crew")
    val crew: Set<CastInfo> = emptySet()

    private var both: MutableSet<CastInfo>? = null

    fun bothLists(): Set<CastInfo> {
        if (both == null) {
            both = HashSet(cast)
            both?.addAll(crew)
        }
        return both ?: emptySet()
    }
}