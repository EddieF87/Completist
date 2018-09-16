package xyz.sleekstats.completist.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface MovieDao {

    //Movies Table

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMovie(MyMovie myMovie);

    @Query("SELECT * from movie_table")
    Flowable<List<MyMovie>> getSavedMovies();

    @Query("DELETE FROM movie_table WHERE movie_id LIKE :id")
    void removeMovie(String id);

    @Query("SELECT * FROM movie_table WHERE movie_id LIKE :id")
    Single<MyMovie> checkIfMovieExists(String id);

    @Query("SELECT * FROM movie_table WHERE movie_id IN (:ids)")
    Flowable<List<MyMovie>> getMoviesWatched(List<String> ids);



    //Lists Table

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(MyList myList);

    @Query("SELECT * from list_table")
    Flowable<List<MyList>> getSavedLists();

    @Query("DELETE FROM list_table WHERE list_id LIKE :id")
    void removeList(String id);

    @Query("SELECT * FROM list_table WHERE list_id LIKE :id")
    Single<MyList> checkIfListExists(String id);
}