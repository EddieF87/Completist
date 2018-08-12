package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QueryPOJO {

    @SerializedName("results")
    private List<MediaPOJO> results;

    public List<MediaPOJO> getResults() {
        return results;
    }
}
