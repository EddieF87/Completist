package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

public class MediaPOJO {

    @SerializedName("media_type")
    private String media_type;

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("title")
    private String title;

    public String getMedia_type() {
        return media_type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }
}
