package xyz.sleekstats.completist.model;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class FilmByPerson {

    public FilmByPerson(String title, String id, String poster_path) {
        this.title = title;
        this.id = id;
        this.poster_path = poster_path;
    }

    @SerializedName("title")
    private String title;

    @SerializedName("id")
    private String id;

    @SerializedName("poster_path")
    private String poster_path;

    @SerializedName("job")
    private String job;


    public String getTitle() {
        return title;
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

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {return false;}
        FilmByPerson otherFilm = (FilmByPerson) obj;
        return otherFilm.getTitle().equals(this.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }
}
