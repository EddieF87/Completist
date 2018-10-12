package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResultsPOJO {

    @SerializedName("results")
    private List<FilmByPerson> results;

    public List<FilmByPerson> getResults() {
        return results;
    }
}
