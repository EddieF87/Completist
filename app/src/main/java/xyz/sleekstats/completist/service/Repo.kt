package xyz.sleekstats.completist.service

import android.app.Application
import android.util.Log
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import xyz.sleekstats.completist.model.*
import java.util.*

class Repo(application: Application) {

    private val mMovieDao: MovieDao = MovieRoomDB.getDatabase(application).movieDao()

    private val tmdbAPI: TmdbAPI by lazy {
        val builder = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        val retrofit = builder.build()
        retrofit.create(TmdbAPI::class.java)
    }


    //Get list of films matching search query
    fun queryMedia(movieQuery: String?): Observable<MediaQueryPOJO> = tmdbAPI.queryMedia(movieQuery)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    //Get list of films matching search query
    fun queryForRankings(movieQuery: String?, isFilmRankings: Boolean): Observable<MediaQueryPOJO> =
            if (isFilmRankings) {
                tmdbAPI.queryFilmsForRankings(movieQuery)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
            } else {
                tmdbAPI.queryShowsForRankings(movieQuery)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
            }

    //Get specific film details based  Tmdb film id
    fun getFilm(movie_id: String?): Single<FilmPOJO> = tmdbAPI.retrieveFilm(movie_id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    //Get list of films by a specific actor/director based on Tmdb actor/director id
    fun getFilmsByPerson(person_id: String?): Observable<PersonPOJO> =
            tmdbAPI.retrievePersonInfo(person_id)
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    //Get specific film details based  Tmdb film id
    fun getShow(show_id: String?): Single<ShowPOJO> = tmdbAPI.retrieveShow(show_id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    //Get list of most popular film-people
    fun getPopularActors(pageNumber: Int): Observable<PersonQueryPOJO> =
            tmdbAPI.retrievePopularActors(pageNumber)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    //Get list of most popular films
    fun getPopularFilms(pageNumber: Int): Observable<List<FilmByPerson>> = singleToFilmObservable(
            tmdbAPI.retrievePopularMovies(pageNumber)
                    .map { obj: FilmResultsPOJO -> obj.results }
    )

    //Get list of most popular shows
    fun getPopularShows(pageNumber: Int): Observable<List<ShowByPerson>> = singleToShowObservable(
            tmdbAPI.retrievePopularShows(pageNumber)
                    .map { obj: ShowResultsPOJO -> obj.results }
    )

    //Get list of films currently playing in theatres
    fun getNowPlaying(pageNumber: Int): Observable<List<FilmByPerson>> = singleToFilmObservable(
            tmdbAPI.retrieveNowPlaying(pageNumber)
                    .map { obj: FilmResultsPOJO -> obj.results }
    )

    //Get list of top-rated films
    fun getTopRated(pageNumber: Int): Observable<List<FilmByPerson>> = singleToFilmObservable(
            tmdbAPI.retrieveTopRated(pageNumber)
                    .map { obj: FilmResultsPOJO -> obj.results }
    )

    //Get list of popular films/shows by genre
    fun getMoviesByGenre(tvOrMovie: String?, genre: String?, pageNumber: Int): Observable<List<FilmByPerson>> =
            singleToFilmObservable(
                    tmdbAPI.retrieveByGenre(tvOrMovie, genre, pageNumber)
                            .map { obj: FilmResultsPOJO -> obj.results }
            )

    //Get list of top-rated films
    val myWatchedMovies: Observable<List<MediaByPerson>>
        get() = singleToObservable(mMovieDao.savedWatchedMovies)

    //Get list of top-rated films
    val myQueuedMovies: Observable<List<MediaByPerson>>
        get() = singleToObservable(mMovieDao.savedQueuedMovies)

    private fun singleToObservable(single: Single<List<MediaByPerson>>): Observable<List<MediaByPerson>> =
            single.toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    private fun singleToFilmObservable(single: Single<List<FilmByPerson>>): Observable<List<FilmByPerson>> =
            single.toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    private fun singleToShowObservable(single: Single<List<ShowByPerson>>): Observable<List<ShowByPerson>> =
            single.toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    fun getGenreList(tvOrMovie: String?): Single<GenreList> = tmdbAPI.getGenreList(tvOrMovie)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun insertMovie(movie: MediaByPerson): Disposable {
        mMovieDao.insertMovie(movie)
        return getListIDs(movie.id).subscribe({ ids: List<String?> ->
            for (id in ids) {
                mMovieDao.addWatchedMovie(id)
            }
        }
        ) { e: Throwable -> Log.e(TAG_RXERROR, "getListIDs e=" + e.message) }
    }

    fun insertQueuedMovie(movie: MediaByPerson?) = mMovieDao.insertMovie(movie)

    fun removeMovie(movieID: String): Disposable {
        mMovieDao.removeMovie(movieID)
        return getListIDs(movieID).subscribe({ ids: List<String?> ->
            for (id in ids) {
                mMovieDao.removeWatchedMovie(id)
            }
        }
        ) { e: Throwable -> Log.e(TAG_RXERROR, "getListIDs e=" + e.message) }
    }

    fun removeQueuedMovie(movieID: String?) = mMovieDao.removeMovie(movieID)

    fun insertList(list: MyList?) = mMovieDao.insertList(list)

    fun removeList(id: String?) = mMovieDao.removeList(id)

    fun updateList(numberSeen: Int, numberOfMovies: Int, id: String?) = mMovieDao.updateListWatched(numberSeen, numberOfMovies, id)

    fun checkIfMovieExists(id: String?): Single<MediaByPerson> = mMovieDao.checkIfMovieExists(id)

    fun updateFilm(film: MediaByPerson): Disposable {
        mMovieDao.updateMovie(film)
        return if (film.isWatched) {
            getListIDs(film.id).subscribe({ ids: List<String?> ->
                for (id in ids) {
                    mMovieDao.addWatchedMovie(id)
                }
            }
            ) { e: Throwable -> Log.e(TAG_RXERROR, "getListIDs e=" + e.message) }
        } else {
            getListIDs(film.id).subscribe({ ids: List<String?> ->
                for (id in ids) {
                    mMovieDao.removeWatchedMovie(id)
                }
            }
            ) { e: Throwable -> Log.e(TAG_RXERROR, "getListIDs e=" + e.message) }
        }
    }

    fun updateQueuedFilm(film: MediaByPerson?) = mMovieDao.updateMovie(film)

    fun checkIfListExists(id: String?): Maybe<MyList> = mMovieDao.checkIfListExists(id)

    fun getWatchedMovies(): Single<List<MediaByPerson>> = mMovieDao.savedWatchedMovies

    val savedLists: Flowable<List<MyList>>
        get() = mMovieDao.savedLists

    private fun getListIDs(movieID: String): Single<List<String?>> {
        val listSingle = mMovieDao.savedListsToUpdate
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
        val castSingle = tmdbAPI.retrieveFilm(movieID)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map { obj: FilmPOJO -> obj.castCredits }
                .map { obj: CastCredits -> obj.bothLists() }
                .toObservable()
                .flatMapIterable { list: Set<CastInfo>? -> list }
                .map { obj: CastInfo -> obj.id }
                .toList()
        return Single.zip(listSingle, castSingle, BiFunction<List<MyList>, List<String>, List<String?>> { x: List<MyList>, y: List<String> ->
            val ids: MutableList<String?> = ArrayList()
            for (myList in x) {
                val id = myList.list_id.toString()
                if (y.contains(id)) {
                    ids.add(id)
                }
            }
            ids
        }
        )
    }

    fun getSavedForRankings(isFilm: Boolean): Single<List<MediaByPerson>> =
            mMovieDao.getSavedForRankings(if (isFilm) 1 else 0)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    fun updateRankingNew(id: String, newRank: Int, isFilm: Boolean): Disposable = Single.just(id)
            .subscribeOn(Schedulers.io())
            .subscribe({ x: String? ->
                val isFilmRankings = if (isFilm) 1 else 0
                mMovieDao.updateOtherRankingsDownAfterNew(newRank, isFilmRankings)
                mMovieDao.updateRanking(x, newRank, isFilmRankings)
            }
            ) { e: Throwable -> Log.e(TAG_RXERROR, "updateRankingNew e=${e.message}") }

    fun updateRankingRemove(id: String, oldRank: Int, isFilm: Boolean): Disposable = Single.just(id)
            .subscribeOn(Schedulers.io())
            .subscribe({ x: String? ->
                val isFilmRankings = if (isFilm) 1 else 0
                mMovieDao.updateRanking(x, -1, isFilmRankings)
                mMovieDao.updateOtherRankingsUpAfterRemoval(oldRank, isFilmRankings)
            }
            ) { e: Throwable -> Log.e(TAG_RXERROR, "updateRankingRemove e=${e.message}") }

    fun updateRankingUp(id: String, oldRank: Int, newRank: Int, isFilm: Boolean): Disposable = Single.just(id)
            .subscribeOn(Schedulers.io())
            .subscribe({ x: String? ->
                val isFilmRankings = if (isFilm) 1 else 0
                mMovieDao.updateOtherRankingsDown(oldRank, newRank, isFilmRankings)
                mMovieDao.updateRanking(x, newRank, isFilmRankings)
            }
            ) { e: Throwable -> Log.e(TAG_RXERROR, "updateRankingUp e=${e.message}") }

    fun updateRankingDown(id: String, oldRank: Int, newRank: Int, isFilm: Boolean): Disposable =
            Single.just(id)
                    .subscribeOn(Schedulers.io())
                    .subscribe({ x: String? ->
                        val isFilmRankings = if (isFilm) 1 else 0
                        mMovieDao.updateOtherRankingsUp(oldRank, newRank, isFilmRankings)
                        mMovieDao.updateRanking(x, newRank, isFilmRankings)
                    }
                    ) { e: Throwable -> Log.e(TAG_RXERROR, "updateRankingDown e=${e.message}") }


    //    private void rankCheck(boolean isf) {

    //        getSavedForRankings(isf)
    //                .subscribeOn(Schedulers.io())
    //                .observeOn(AndroidSchedulers.mainThread())
    //                .subscribe(
    //                        films -> {
    //                            for (MediaByPerson film : films) {
    //                                if(film.getRanking() < 0) {continue;}
    //                                Log.d("rankingslischeck", film.getRanking() + " " + film.getTitle());
    //                            }
    //                        }
    //                        , e ->
    //                                Log.e(TAG_RXERROR, "rankingslis e=" + e.getMessage())
    //                );
    //    }

    companion object {
        private const val BASE_URL = "https://api.themoviedb.org/3/"
        private const val TAG_RXERROR = "rxprob Repo"
    }
}