package xyz.sleekstats.completist.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MyMovie myMovie);

//    @Query("DELETE FROM movie_table")
//    void deleteAll();

    @Query("DELETE FROM movie_table WHERE movie_id LIKE :id")
    void removeMovie(String id);

    @Query("DELETE FROM list_table WHERE list_id LIKE :id")
    void removeList(String id);

    @Query("SELECT * from movie_table")
    List<MyMovie> getSavedImages();

    @Query("SELECT * FROM movie_table WHERE movie_id LIKE :id")
    List<MyMovie> checkIfExists(String id);

    @Query("SELECT * FROM movie_table WHERE movie_id IN (:ids)")
    List<MyMovie> checkIfListExists(List<String> ids);
}