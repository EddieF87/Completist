package xyz.sleekstats.completist.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "list_table")
public class MyList {

    @PrimaryKey
    private int list_id;
    private String list_name;
    private int list_pct;
    private String list_img;

    public MyList(int list_id, String list_name, int list_pct, String list_img) {
        this.list_id = list_id;
        this.list_name = list_name;
        this.list_pct = list_pct;
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

    public int getList_pct() {
        return list_pct;
    }

    public void setList_pct(int list_pct) {
        this.list_pct = list_pct;
    }

    public String getList_img() {
        return list_img;
    }

    public void setList_img(String list_img) {
        this.list_img = list_img;
    }
}
