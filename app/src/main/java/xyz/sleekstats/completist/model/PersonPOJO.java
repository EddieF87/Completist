package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PersonPOJO {

    @SerializedName("cast")
    private List<FilmByPerson> cast;

    @SerializedName("crew")
    private List<FilmByPerson> crew;

    public List<FilmByPerson> getCrew() {
        return crew;
    }

    public void setCrew(List<FilmByPerson> crew) {
        this.crew = crew;
    }

    public List<FilmByPerson> getCast() {
        return cast;
    }

    public void setCast(List<FilmByPerson> cast) {
        this.cast = cast;
    }
}
