package xyz.sleekstats.completist.model

class ShowPOJO : MediaPOJO() {
    override var isFilm: Boolean
        get() = false
        set(isFilm) {
            super.isFilm = isFilm
        }
}