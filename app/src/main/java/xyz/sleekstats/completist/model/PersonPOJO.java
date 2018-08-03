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

    public String getName() {
        return name;
    }

    public String getBiography() {
        return biography;
    }

    public String getKnown_for_department() {
        return known_for_department;
    }

    public String getProfile_path() {
        return profile_path;
    }
}
