package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PopularPOJO {

    @SerializedName("results")
    private List<FilmByPerson> results;

    public List<FilmByPerson> getResults() {
        return results;
    }
}
