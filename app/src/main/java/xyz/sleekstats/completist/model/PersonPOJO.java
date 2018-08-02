package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

public class PersonPOJO {

    @SerializedName("movie_credits")
    private MovieCredits movieCredits;

    @SerializedName("name")
    private String name;

    @SerializedName("biography")
    private String biography;

    @SerializedName("known_for_department")
    private String known_for_department;

    @SerializedName("profile_path")
    private String profile_path;

    public MovieCredits getMovieCredits() {
        return movieCredits;
    }

    public void setMovieCredits(MovieCredits movieCredits) {
        this.movieCredits = movieCredits;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getKnown_for_department() {
        return known_for_department;
    }

    public void setKnown_for_department(String known_for_department) {
        this.known_for_department = known_for_department;
    }

    public String getProfile_path() {
        return profile_path;
    }

    public void setProfile_path(String profile_path) {
        this.profile_path = profile_path;
    }
}
