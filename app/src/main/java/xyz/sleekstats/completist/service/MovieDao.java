package xyz.sleekstats.completist.service;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import xyz.sleekstats.completist.model.MyList;
import xyz.sleekstats.completist.model.MyMovie;

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

    @Query("SELECT * from list_table")
    Single<List<MyList>> getSavedListsToUpdate();

    @Query("DELETE FROM list_table WHERE list_id LIKE :id")
    void removeList(String id);

    @Query("SELECT * FROM list_table WHERE list_id LIKE :id")
    Maybe<MyList> checkIfListExists(String id);

//    @Update()
//    void updateList(MyList myList);

    @Query("UPDATE list_table SET watched_films=:watched, total_films=:total WHERE list_id = :id")
    void updateListWatched(int watched, int total, String id);

    @Query("UPDATE list_table SET watched_films = watched_films + 1 WHERE list_id = :id")
    void addWatchedMovie(String id);

    @Query("UPDATE list_table SET watched_films = watched_films - 1 WHERE list_id = :id")
    void removeWatchedMovie(String id);
}