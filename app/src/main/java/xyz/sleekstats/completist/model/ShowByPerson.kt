package xyz.sleekstats.completist.model

class ShowByPerson(title: String, id: String, poster_path: String, job: String) : MediaByPerson(title, id, poster_path, job) {
    override var isFilm: Boolean
        get() = false
        set(isFilm) {
            super.isFilm = isFilm
        }
}