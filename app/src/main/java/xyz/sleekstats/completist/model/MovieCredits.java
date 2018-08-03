package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieCredits {

    @SerializedName("cast")
    private List<FilmByPerson> cast;

    @SerializedName("crew")
    private List<FilmByPerson> crew;

    public List<FilmByPerson> getCrew() {
        return crew;
    }

    public List<FilmByPerson> getCast() {
        return cast;
    }
}
