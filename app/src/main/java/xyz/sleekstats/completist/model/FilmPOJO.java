package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FilmPOJO {

    @SerializedName(value = "title", alternate = "name")
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

    @SerializedName("vote_average")
    private float vote_average;

    @SerializedName("media_type")
    private String media_type;

    public String getMedia_type() { return media_type; }

    public void setMedia_type(String media_type) {
        this.media_type = media_type;
    }

    private boolean isFilm;

    public boolean isFilm() {
        return isFilm;
    }

    public void setIsFilm(boolean film) {
        isFilm = film;
    }

    private boolean isWatched;
    private boolean isQueued;

    public boolean isWatched() {
        return isWatched;
    }

    public void setWatched(boolean watched) {
        isWatched = watched;
    }

    public boolean isQueued() {
        return isQueued;
    }

    public void setQueued(boolean queued) {
        isQueued = queued;
    }

    public void reverseWatched() {
        isWatched = !isWatched;
    }

    public void reverseQueued() {
        isQueued = !isQueued;
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

    public void setId(String id) {
        this.id = id;
    }
}
