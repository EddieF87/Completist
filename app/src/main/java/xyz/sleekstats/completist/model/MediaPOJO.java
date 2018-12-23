package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MediaPOJO {

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

    @SerializedName("imdb_id")
    private String imdb_id;

    @SerializedName("vote_average")
    private float vote_average;

    @SerializedName("media_type")
    private String media_type;

    @SerializedName(value = "release_date", alternate = "first_air_date")
    private String release_date;

    @SerializedName("last_air_date")
    private String last_date;

    @SerializedName(value = "runtime")
    private int runtime;

    @SerializedName("number_of_seasons")
    private int number_of_seasons;

    @SerializedName("number_of_episodes")
    private int number_of_episodes;

    @SerializedName("similar")
    private ResultsPOJO similar;

    public String getRelease_date() {
        return release_date;
    }

    public int getRuntime() {
        return runtime;
    }

    public int getNumber_of_seasons() {
        return number_of_seasons;
    }

    public int getNumber_of_episodes() {
        return number_of_episodes;
    }

    public String getLast_date() {
        return last_date;
    }

    public String getMedia_type() { return media_type; }

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

    public String getImdb_id() {
        return imdb_id;
    }

    public ResultsPOJO getSimilar() {
        return similar;
    }
}
