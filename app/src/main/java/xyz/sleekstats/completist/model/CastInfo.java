package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

public class CastInfo {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("profile_path")
    private String profile_path;

    @SerializedName("order")
    private String order;

    @SerializedName("job")
    private String job;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProfile_path() {
        return profile_path;
    }

    public String getOrder() {
        return order;
    }

    public String getJob() {
        return job;
    }
}
