package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MediaQueryPOJO {

    @SerializedName("results")
    private List<FilmPOJO> results;

    public List<FilmPOJO> getResults() {
        return results;
    }
}
