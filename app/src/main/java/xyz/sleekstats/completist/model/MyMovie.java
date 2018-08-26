package xyz.sleekstats.completist.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "movie_table")
public class MyMovie {

    @PrimaryKey
    private int movie_id;
    private String title;
    private int rating;
    private int watchType;
    // 0: not watched, 1: want to watch, 2: ignored, 3:watched


    public MyMovie(int movie_id, String title, int rating, int watchType) {
        this.movie_id = movie_id;
        this.title = title;
        this.rating = rating;
        this.watchType = watchType;
    }

    public int getMovie_id() {
        return movie_id;
    }

    public void setMovie_id(int movie_id) {
        this.movie_id = movie_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getWatchType() {
        return watchType;
    }

    public void setWatchType(int watchType) {
        this.watchType = watchType;
    }
}
