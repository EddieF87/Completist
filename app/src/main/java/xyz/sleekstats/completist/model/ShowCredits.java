package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Set;

public class ShowCredits {

    @SerializedName("cast")
    private Set<ShowByPerson> cast;

    @SerializedName("crew")
    private Set<ShowByPerson> crew;

    private Set<ShowByPerson> both;

    public Set<ShowByPerson> getCrew() {
        return filterCrew(crew);
    }

    public Set<ShowByPerson> getCast() {
        return cast;
    }

    public Set<ShowByPerson> bothLists() {
        if(both == null) {
            Set<ShowByPerson> ShowByPersonSet = new HashSet<>(cast);
            ShowByPersonSet.addAll(getCrew());
            both = ShowByPersonSet;
        }
        return both;
    }

    private Set<ShowByPerson> filterCrew(Set<ShowByPerson> unfiltered) {
        Set<ShowByPerson> filteredSet = new HashSet<>();
        for (ShowByPerson film : unfiltered) {
            if (film.getJob().equals("Director") || film.getJob().equals("Writer") || film.getJob().equals("Screenplay")) {
                filteredSet.add(film);
            }
        }
        return filteredSet;
    }
}