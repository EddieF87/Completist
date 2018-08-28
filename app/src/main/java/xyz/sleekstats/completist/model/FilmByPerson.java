package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

public class FilmByPerson {

    @SerializedName("title")
    private String title;

    @SerializedName("popularity")
    private String popularity;

    @SerializedName("id")
    private String id;

    @SerializedName("poster_path")
    private String poster_path;

    @SerializedName("job")
    private String job;


    public String getTitle() {
        return title;
    }

    public String getPopularity() {
        return popularity;
    }

    public String getId() {
        return id;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public String getJob() {
        return job;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
