package xyz.sleekstats.completist.service

import androidx.room.*
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import xyz.sleekstats.completist.model.MediaByPerson
import xyz.sleekstats.completist.model.MyList

@Dao
interface MovieDao {
    //Movies Table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMovie(myMovie: MediaByPerson?)

    @Update
    fun updateMovie(film: MediaByPerson?)

    @Query("SELECT * from movie_table WHERE isFilm == :isFilmRankings ORDER BY ranking ASC")
    fun getSavedForRankings(isFilmRankings: Int): Single<List<MediaByPerson>>

    @get:Query("SELECT * from movie_table WHERE isWatched == 1")
    val savedWatchedMovies: Single<List<MediaByPerson>>

    @get:Query("SELECT * from movie_table WHERE isQueued == 1")
    val savedQueuedMovies: Single<List<MediaByPerson>>

    @Query("DELETE FROM movie_table WHERE id LIKE :id")
    fun removeMovie(id: String?)

    @Query("SELECT * FROM movie_table WHERE id LIKE :id")
    fun checkIfMovieExists(id: String?): Single<MediaByPerson>

    @Query("UPDATE movie_table SET ranking = ranking + 1 WHERE ranking >= :newRank AND ranking < :oldRank AND isFilm == :isFilmRankings")
    fun updateOtherRankingsDown(oldRank: Int, newRank: Int, isFilmRankings: Int)

    @Query("UPDATE movie_table SET ranking = ranking + 1 WHERE ranking >= :newRank AND isFilm == :isFilmRankings")
    fun updateOtherRankingsDownAfterNew(newRank: Int, isFilmRankings: Int)

    @Query("UPDATE movie_table SET ranking = ranking - 1 WHERE ranking <= :newRank AND ranking > :oldRank AND isFilm == :isFilmRankings")
    fun updateOtherRankingsUp(oldRank: Int, newRank: Int, isFilmRankings: Int)

    @Query("UPDATE movie_table SET ranking = ranking - 1 WHERE ranking > :oldRank AND isFilm == :isFilmRankings")
    fun updateOtherRankingsUpAfterRemoval(oldRank: Int, isFilmRankings: Int)

    @Query("UPDATE movie_table SET ranking = :newRank WHERE id LIKE :id AND isFilm == :isFilmRankings")
    fun updateRanking(id: String?, newRank: Int, isFilmRankings: Int)

    //Lists Table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(myList: MyList?)

    @get:Query("SELECT * from list_table")
    val savedLists: Flowable<List<MyList>>

    @get:Query("SELECT * from list_table")
    val savedListsToUpdate: Single<List<MyList>>

    @Query("DELETE FROM list_table WHERE list_id LIKE :id")
    fun removeList(id: String?)

    @Query("SELECT * FROM list_table WHERE list_id LIKE :id")
    fun checkIfListExists(id: String?): Maybe<MyList>

    @Query("UPDATE list_table SET watched_films=:watched, total_films=:total WHERE list_id = :id")
    fun updateListWatched(watched: Int, total: Int, id: String?)

    @Query("UPDATE list_table SET watched_films = watched_films + 1 WHERE list_id = :id")
    fun addWatchedMovie(id: String?)

    @Query("UPDATE list_table SET watched_films = watched_films - 1 WHERE list_id = :id")
    fun removeWatchedMovie(id: String?)
}