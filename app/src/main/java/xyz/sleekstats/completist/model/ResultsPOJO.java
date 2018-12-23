package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResultsPOJO {

    @SerializedName("results")
    private List<MediaByPerson> results;

    public List<MediaByPerson> getResults() {
        return results;
    }
}
