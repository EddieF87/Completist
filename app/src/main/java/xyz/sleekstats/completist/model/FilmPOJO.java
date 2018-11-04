package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FilmPOJO {

    @SerializedName("title")
    private String title;

    @SerializedName("overview")
    private String overview;

    @SerializedName("poster_path")
    private String poster_path;

    @SerializedName("genres")
    private List<Genre> genres;

    @SerializedName("credits")
    private CastCredits castCredits;

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("vote_average")
    private float vote_average;

    private int watchType;

    public int getWatchType() {
        return watchType;
    }

    public void setWatchType(int watchType) {
        this.watchType = watchType;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public CastCredits getCastCredits() {
        return castCredits;
    }

    public float getVote_average() {
        return vote_average;
    }
}
