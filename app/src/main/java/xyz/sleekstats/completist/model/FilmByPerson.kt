package xyz.sleekstats.completist.model

class FilmByPerson(title: String, id: String, poster_path: String, job: String) : MediaByPerson(title, id, poster_path, job) {
    override var isFilm: Boolean
        get() = true
        set(isFilm) {
            super.isFilm = isFilm
        }
}