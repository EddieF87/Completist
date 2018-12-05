package xyz.sleekstats.completist.service;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import xyz.sleekstats.completist.model.FilmByPerson;
import xyz.sleekstats.completist.model.MyList;

@Dao
public interface MovieDao {

    //Movies Table

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMovie(FilmByPerson myMovie);

    @Update
    void updateMovie(FilmByPerson film);

    @Query("SELECT * from movie_table ORDER BY ranking ASC")
    Single<List<FilmByPerson>> getSavedMovies();

    @Query("SELECT * from movie_table WHERE isWatched == 1")
    Single<List<FilmByPerson>> getSavedWatchedMovies();

    @Query("SELECT * from movie_table WHERE isQueued == 1")
    Single<List<FilmByPerson>> getSavedQueuedMovies();

    @Query("DELETE FROM movie_table WHERE id LIKE :id")
    void removeMovie(String id);

    @Query("SELECT * FROM movie_table WHERE id LIKE :id")
    Single<FilmByPerson> checkIfMovieExists(String id);

    @Query("SELECT * FROM movie_table WHERE id IN (:ids)")
    Single<List<FilmByPerson>> getAllMoviesInList(List<String> ids);

    @Query("SELECT * FROM movie_table WHERE id IN (:ids) AND isWatched == 1")
    Single<List<FilmByPerson>> getWatchedMoviesInList(List<String> ids);

    @Query("SELECT * FROM movie_table WHERE id IN (:ids) AND isQueued == 1")
    Single<List<FilmByPerson>> getQueuedMoviesInList(List<String> ids);

    @Query("UPDATE movie_table SET ranking = ranking + 1 WHERE ranking >= :newRank AND ranking < :oldRank")
    void updateOtherRankingsDown(int oldRank, int newRank);

    @Query("UPDATE movie_table SET ranking = ranking + 1 WHERE ranking >= :newRank")
    void updateOtherRankingsDownAfterNew(int newRank);

    @Query("UPDATE movie_table SET ranking = ranking - 1 WHERE ranking <= :newRank AND ranking > :oldRank")
    void updateOtherRankingsUp(int oldRank, int newRank);

    @Query("UPDATE movie_table SET ranking = ranking - 1 WHERE ranking > :oldRank")
    void updateOtherRankingsUpAfterRemoval(int oldRank);

    @Query("UPDATE movie_table SET ranking = :newRank WHERE id LIKE :id")
    void updateRanking(String id, int newRank);


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