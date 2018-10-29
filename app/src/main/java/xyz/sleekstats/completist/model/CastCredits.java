package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CastCredits {

    @SerializedName("cast")
    private List<CastInfo> cast;

    @SerializedName("crew")
    private List<CastInfo> crew;

    private List<CastInfo> both;

    public List<CastInfo> getCast() {
        return cast;
    }

    public List<CastInfo> getCrew() {
        return crew;
    }

    public List<CastInfo> bothLists() {
        if(both == null) {
            both = new ArrayList<>(cast);
            both.addAll(crew);
        }
        return both;
    }
}
