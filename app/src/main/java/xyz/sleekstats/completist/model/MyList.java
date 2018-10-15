package xyz.sleekstats.completist.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "list_table")
public class MyList {

    @PrimaryKey
    private int list_id;
    private String list_name;
    private int watched_films;
    private int total_films;
    private String list_img;

    public MyList(int list_id, String list_name, int watched_films, int total_films, String list_img) {
        this.list_id = list_id;
        this.list_name = list_name;
        this.watched_films = watched_films;
        this.total_films = total_films;
        this.list_img = list_img;
    }

    public int getList_id() {
        return list_id;
    }

    public void setList_id(int list_id) {
        this.list_id = list_id;
    }

    public String getList_name() {
        return list_name;
    }

    public void setList_name(String list_name) {
        this.list_name = list_name;
    }

    public int getWatched_films() {
        return watched_films;
    }

    public void setWatched_films(int watched_films) {
        this.watched_films = watched_films;
    }

    public int getTotal_films() {
        return total_films;
    }

    public void setTotal_films(int total_films) {
        this.total_films = total_films;
    }

    public int getList_pct() {
        return watched_films * 100 /total_films;
    }

    public String getList_img() {
        return list_img;
    }

    public void setList_img(String list_img) {
        this.list_img = list_img;
    }
}
