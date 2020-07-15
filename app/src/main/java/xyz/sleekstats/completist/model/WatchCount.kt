package xyz.sleekstats.completist.model

class WatchCount(var watched: Int, var total: Int) {

    val watchedPct: Int
        get() = if (total == 0) {
            0
        } else watched * 100 / total

    override fun toString(): String {
        return "Watched: $watched/$total ($watchedPct%)"
    }

}