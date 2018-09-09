package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PersonQueryPOJO {

    @SerializedName("results")
    private List<PersonPOJO> results;

    public List<PersonPOJO> getResults() {
        return results;
    }
}
