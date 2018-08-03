package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CastCredits {

    @SerializedName("cast")
    private List<CastInfo> cast;

    @SerializedName("crew")
    private List<CastInfo> crew;

    public List<CastInfo> getCast() {
        return cast;
    }

    public List<CastInfo> getCrew() {
        return crew;
    }
}
