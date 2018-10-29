package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MovieCredits {

    @SerializedName("cast")
    private Set<FilmByPerson> cast;

    @SerializedName("crew")
    private Set<FilmByPerson> crew;

    private Set<FilmByPerson> both;

    public Set<FilmByPerson> getCrew() {
        return filterCrew(crew);
    }

    public Set<FilmByPerson> getCast() {
        return cast;
    }

    public Set<FilmByPerson> bothLists() {
        if(both == null) {
            Set<FilmByPerson> filmByPersonSet = new HashSet<>(cast);
            filmByPersonSet.addAll(getCrew());
//            both = new ArrayList<>(filmByPersonSet);
            both = filmByPersonSet;
        }
        return both;
    }

    private Set<FilmByPerson> filterCrew(Set<FilmByPerson> unfiltered) {
        Set<FilmByPerson> filteredSet = new HashSet<>();
        for (FilmByPerson film : unfiltered) {
            if (film.getJob().equals("Director") || film.getJob().equals("Writer") || film.getJob().equals("Screenplay")) {
                filteredSet.add(film);
            }
        }
//        return new ArrayList<>(filteredSet);
        return filteredSet;
    }
}
