package xyz.sleekstats.completist.model

class FilmPOJO : MediaPOJO() {
    override var isFilm: Boolean
        get() = true
        set(isFilm) {
            super.isFilm = isFilm
        }
}