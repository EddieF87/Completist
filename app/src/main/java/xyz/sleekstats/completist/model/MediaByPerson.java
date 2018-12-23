package xyz.sleekstats.completist.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

@Entity(tableName = "movie_table")
public class MediaByPerson {

    public MediaByPerson(){ }

    public MediaByPerson(String title, @NonNull String id, String poster_path, boolean isItFilm) {
        this.title = title;
        this.id = id;
        this.poster_path = poster_path;
        this.isFilm = isItFilm;
        this.ranking = -1;
    }

    @SerializedName(value = "title", alternate = "name")
    private String title;

    @NonNull
    @PrimaryKey
    @SerializedName("id")
    private String id;

    @SerializedName("poster_path")
    private String poster_path;

    @SerializedName("job")
    private String job;

    private boolean isFilm;

    public boolean isFilm() {
        return isFilm;
    }

    public void setIsFilm(boolean film) {
        isFilm = film;
    }

    private boolean isWatched;

    private boolean isQueued;

    private int ranking;

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }


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
        if (!(obj instanceof MediaByPerson)) {
            return false;
        }
        MediaByPerson otherFilm = (MediaByPerson) obj;
        return otherFilm.getTitle().equals(this.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }
}
