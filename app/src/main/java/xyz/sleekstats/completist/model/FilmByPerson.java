package xyz.sleekstats.completist.model;

public class FilmByPerson extends MediaByPerson {

    @Override
    public boolean isFilm() {
        return true;
    }
}
