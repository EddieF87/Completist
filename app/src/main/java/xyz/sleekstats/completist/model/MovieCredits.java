package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MovieCredits {

    @SerializedName("cast")
    private List<FilmByPerson> cast;

    @SerializedName("crew")
    private List<FilmByPerson> crew;

    private List<FilmByPerson> both;

    public List<FilmByPerson> getCrew() {
        return filterCrew(crew);
    }

    public List<FilmByPerson> getCast() {
        return cast;
    }

    public List<FilmByPerson> bothLists() {
        if(both == null) {
            both = new ArrayList<>(cast);
            both.addAll(getCrew());
        }
        return both;
    }

    private List<FilmByPerson> filterCrew(List<FilmByPerson> unfiltered) {
        Set<FilmByPerson> filteredSet = new HashSet<>();
        for (FilmByPerson film : unfiltered) {
            if (film.getJob().equals("Director") || film.getJob().equals("Writer") || film.getJob().equals("Screenplay")) {
                filteredSet.add(film);
            }
        }
        return new ArrayList<>(filteredSet);
    }
}
