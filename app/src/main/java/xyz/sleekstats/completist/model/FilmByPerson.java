package xyz.sleekstats.completist.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

@Entity(tableName = "movie_table")
public class FilmByPerson {

    public FilmByPerson(String title, @NonNull String id, String poster_path) {
        this.title = title;
        this.id = id;
        this.poster_path = poster_path;
    }

    @SerializedName("title")
    private String title;

    @NonNull
    @PrimaryKey
    @SerializedName("id")
    private String id;

    @SerializedName("poster_path")
    private String poster_path;

    @SerializedName("job")
    private String job;

    private boolean isWatched;

    private boolean isQueued;

    public String getTitle() {
        return title;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getJob() {
        return job;
    }

    public void setTitle(String title) {
        this.title = title;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FilmByPerson)) {
            return false;
        }
        FilmByPerson otherFilm = (FilmByPerson) obj;
        return otherFilm.getTitle().equals(this.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }
}
