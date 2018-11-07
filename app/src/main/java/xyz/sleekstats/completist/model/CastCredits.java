package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Set;

public class CastCredits {

    @SerializedName("cast")
    private Set<CastInfo> cast;

    @SerializedName("crew")
    private Set<CastInfo> crew;

    private Set<CastInfo> both;

    public Set<CastInfo> getCast() {
        return cast;
    }

    public Set<CastInfo> getCrew() {
        return crew;
    }

    public Set<CastInfo> bothLists() {
        if(both == null) {
            both = new HashSet<>(cast);
            both.addAll(crew);
        }
        return both;
    }
}
