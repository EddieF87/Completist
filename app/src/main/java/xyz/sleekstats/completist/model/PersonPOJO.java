package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

public class PersonPOJO {

    public PersonPOJO(String name, String biography, String known_for_department, String profile_path, String id) {
        this.name = name;
        this.biography = biography;
        this.known_for_department = known_for_department;
        this.profile_path = profile_path;
        this.id = id;
    }

    @SerializedName("movie_credits")
    private MovieCredits movieCredits;

    @SerializedName("name")
    private String name;

    @SerializedName("biography")
    private final String biography;

    @SerializedName("known_for_department")
    private final String known_for_department;

    @SerializedName("profile_path")
    private final String profile_path;

    @SerializedName("id")
    private final String id;

    public String getId() {
        return id;
    }

    public MovieCredits getMovieCredits() {
        return movieCredits;
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

    public String getKnown_for_department() {
        return known_for_department;
    }

    public String getProfile_path() {
        return profile_path;
    }
}
