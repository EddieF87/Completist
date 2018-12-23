package xyz.sleekstats.completist.service;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import xyz.sleekstats.completist.model.MediaByPerson;
import xyz.sleekstats.completist.model.MyList;

@Dao
public interface MovieDao {

    //Movies Table

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMovie(MediaByPerson myMovie);

    @Update
    void updateMovie(MediaByPerson film);

    @Query("SELECT * from movie_table WHERE isFilm == :isFilmRankings ORDER BY ranking ASC")
    Single<List<MediaByPerson>> getSavedForRankings(int isFilmRankings);

    @Query("SELECT * from movie_table WHERE isWatched == 1")
    Single<List<MediaByPerson>> getSavedWatchedMovies();

    @Query("SELECT * from movie_table WHERE isQueued == 1")
    Single<List<MediaByPerson>> getSavedQueuedMovies();

    @Query("DELETE FROM movie_table WHERE id LIKE :id")
    void removeMovie(String id);

    @Query("SELECT * FROM movie_table WHERE id LIKE :id")
    Single<MediaByPerson> checkIfMovieExists(String id);

    @Query("SELECT * FROM movie_table WHERE id IN (:ids)")
    Single<List<MediaByPerson>> getAllMoviesInList(List<String> ids);

    @Query("SELECT * FROM movie_table WHERE id IN (:ids) AND isWatched == 1")
    Single<List<MediaByPerson>> getWatchedMoviesInList(List<String> ids);

    @Query("SELECT * FROM movie_table WHERE id IN (:ids) AND isQueued == 1")
    Single<List<MediaByPerson>> getQueuedMoviesInList(List<String> ids);

    @Query("UPDATE movie_table SET ranking = ranking + 1 WHERE ranking >= :newRank AND ranking < :oldRank AND isFilm == :isFilmRankings")
    void updateOtherRankingsDown(int oldRank, int newRank, int isFilmRankings);

    @Query("UPDATE movie_table SET ranking = ranking + 1 WHERE ranking >= :newRank AND isFilm == :isFilmRankings")
    void updateOtherRankingsDownAfterNew(int newRank, int isFilmRankings);

    @Query("UPDATE movie_table SET ranking = ranking - 1 WHERE ranking <= :newRank AND ranking > :oldRank AND isFilm == :isFilmRankings")
    void updateOtherRankingsUp(int oldRank, int newRank, int isFilmRankings);

    @Query("UPDATE movie_table SET ranking = ranking - 1 WHERE ranking > :oldRank AND isFilm == :isFilmRankings")
    void updateOtherRankingsUpAfterRemoval(int oldRank, int isFilmRankings);

    @Query("UPDATE movie_table SET ranking = :newRank WHERE id LIKE :id AND isFilm == :isFilmRankings")
    void updateRanking(String id, int newRank, int isFilmRankings);


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