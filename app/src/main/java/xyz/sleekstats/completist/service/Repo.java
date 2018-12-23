package xyz.sleekstats.completist.service;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.sleekstats.completist.model.CastCredits;
import xyz.sleekstats.completist.model.CastInfo;
import xyz.sleekstats.completist.model.FilmByPerson;
import xyz.sleekstats.completist.model.FilmPOJO;
import xyz.sleekstats.completist.model.MediaPOJO;
import xyz.sleekstats.completist.model.FilmResultsPOJO;
import xyz.sleekstats.completist.model.GenreList;
import xyz.sleekstats.completist.model.MediaByPerson;
import xyz.sleekstats.completist.model.MediaQueryPOJO;
import xyz.sleekstats.completist.model.MovieRoomDB;
import xyz.sleekstats.completist.model.MyList;
import xyz.sleekstats.completist.model.PersonPOJO;
import xyz.sleekstats.completist.model.PersonQueryPOJO;
import xyz.sleekstats.completist.model.ShowByPerson;
import xyz.sleekstats.completist.model.ShowPOJO;
import xyz.sleekstats.completist.model.ShowResultsPOJO;

public class Repo {

    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private TmdbAPI tmdbAPI;
    private MovieDao mMovieDao;
    private static final String TAG_RXERROR = "rxprob Repo";

    public Repo(Application application) {

        if (tmdbAPI == null) {
            tmdbAPI = getData();
        }

        if (mMovieDao == null) {
            MovieRoomDB db = MovieRoomDB.getDatabase(application);
            this.mMovieDao = db.movieDao();
        }
    }

    //Build Retrofit API service
    private TmdbAPI getData() {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

        Retrofit retrofit = builder.build();
        return retrofit.create(TmdbAPI.class);
    }

    //Get list of films matching search query
    public Observable<MediaQueryPOJO> queryMedia(String movieQuery) {
        return tmdbAPI.queryMedia(movieQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //Get list of films matching search query
    public Observable<MediaQueryPOJO> queryForRankings(String movieQuery, boolean isFilmRankings) {
        if (isFilmRankings) {
            return tmdbAPI.queryFilmsForRankings(movieQuery)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        } else {
            return tmdbAPI.queryShowsForRankings(movieQuery)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    //Get specific film details based  Tmdb film id
    public Single<FilmPOJO> getFilm(String movie_id) {
        return tmdbAPI.retrieveFilm(movie_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    //Get list of films by a specific actor/director based on Tmdb actor/director id
    public Observable<PersonPOJO> getFilmsByPerson(String person_id) {
        return tmdbAPI.retrievePersonInfo(person_id)
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //Get specific film details based  Tmdb film id
    public Single<ShowPOJO> getShow(String show_id) {
        return tmdbAPI.retrieveShow(show_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    //Get list of most popular film-people
    public Observable<PersonQueryPOJO> getPopularActors(int pageNumber) {
        return tmdbAPI.retrievePopularActors(pageNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //Get list of most popular films
    public Observable<List<FilmByPerson>> getPopularFilms(int pageNumber) {
        return singleToFilmObservable(
                tmdbAPI.retrievePopularMovies(pageNumber)
                        .map(FilmResultsPOJO::getResults)
        );
    }

    //Get list of most popular shows
    public Observable<List<ShowByPerson>> getPopularShows(int pageNumber) {
        return singleToShowObservable(
                tmdbAPI.retrievePopularShows(pageNumber)
                        .map(ShowResultsPOJO::getResults)
        );
    }

    //Get list of films currently playing in theatres
    public Observable<List<FilmByPerson>> getNowPlaying(int pageNumber) {
        return singleToFilmObservable(
                tmdbAPI.retrieveNowPlaying(pageNumber)
                        .map(FilmResultsPOJO::getResults)
        );
    }

    //Get list of top-rated films
    public Observable<List<FilmByPerson>> getTopRated(int pageNumber) {
        return singleToFilmObservable(
                tmdbAPI.retrieveTopRated(pageNumber)
                        .map(FilmResultsPOJO::getResults)
        );
    }

    //Get list of popular films/shows by genre
    public Observable<List<FilmByPerson>> getMoviesByGenre(String tvOrMovie, String genre, int pageNumber) {
        return singleToFilmObservable(
                tmdbAPI.retrieveByGenre(tvOrMovie, genre, pageNumber)
                        .map(FilmResultsPOJO::getResults)
        );
    }


    //Get list of top-rated films
    public Observable<List<MediaByPerson>> getMyWatchedMovies() {
        return singleToObservable(mMovieDao.getSavedWatchedMovies());
    }

    //Get list of top-rated films
    public Observable<List<MediaByPerson>> getMyQueuedMovies() {
        return singleToObservable(mMovieDao.getSavedQueuedMovies());
    }

    private Observable<List<MediaByPerson>> singleToObservable(Single<List<MediaByPerson>> single) {
        return single.toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<List<FilmByPerson>> singleToFilmObservable(Single<List<FilmByPerson>> single) {
        return single.toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<List<ShowByPerson>> singleToShowObservable(Single<List<ShowByPerson>> single) {
        return single.toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<GenreList> getGenreList(String tvOrMovie) {
        return tmdbAPI.getGenreList(tvOrMovie)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public Disposable insertMovie(MediaByPerson movie) {
        mMovieDao.insertMovie(movie);
        return getListIDs(movie.getId()).subscribe(ids -> {
                    for (String id : ids) {
                        mMovieDao.addWatchedMovie(id);
                    }
                },
                e -> Log.e(TAG_RXERROR, "getListIDs e=" + e.getMessage()));
    }

    public void insertQueuedMovie(MediaByPerson movie) {
        mMovieDao.insertMovie(movie);
    }

    public Disposable removeMovie(String movieID) {
        mMovieDao.removeMovie(movieID);
        return getListIDs(movieID).subscribe(ids -> {
                    for (String id : ids) {
                        mMovieDao.removeWatchedMovie(id);
                    }
                },
                e -> Log.e(TAG_RXERROR, "getListIDs e=" + e.getMessage()));
    }

    public void removeQueuedMovie(String movieID) {
        mMovieDao.removeMovie(movieID);
    }

    public void insertList(MyList list) {
        mMovieDao.insertList(list);
    }

    public void removeList(String id) {
        mMovieDao.removeList(id);
    }

    public void updateList(int numberSeen, int numberOfMovies, String id) {
        mMovieDao.updateListWatched(numberSeen, numberOfMovies, id);
    }

    public Single<MediaByPerson> checkIfMovieExists(String id) {
        return mMovieDao.checkIfMovieExists(id);
    }

    public Disposable updateFilm(MediaByPerson film) {
        mMovieDao.updateMovie(film);
        if (film.isWatched()) {
            return getListIDs(film.getId()).subscribe(ids -> {
                        for (String id : ids) {
                            mMovieDao.addWatchedMovie(id);
                        }
                    },
                    e -> Log.e(TAG_RXERROR, "getListIDs e=" + e.getMessage()));
        } else {
            return getListIDs(film.getId()).subscribe(ids -> {
                        for (String id : ids) {
                            mMovieDao.removeWatchedMovie(id);
                        }
                    },
                    e -> Log.e(TAG_RXERROR, "getListIDs e=" + e.getMessage()));
        }
    }

    public void updateQueuedFilm(MediaByPerson film) {
        mMovieDao.updateMovie(film);
    }

    public Maybe<MyList> checkIfListExists(String id) {
        return mMovieDao.checkIfListExists(id);
    }

    public Single<List<MediaByPerson>> getAllMovies(List<String> ids) {
        return mMovieDao.getAllMoviesInList(ids);
    }

    public Single<List<MediaByPerson>> getWatchedMovies(List<String> ids) {
        return mMovieDao.getWatchedMoviesInList(ids);
    }

    public Single<List<MediaByPerson>> getQueuedMovies(List<String> ids) {
        return mMovieDao.getQueuedMoviesInList(ids);
    }

    public Flowable<List<MyList>> getSavedLists() {
        return mMovieDao.getSavedLists();
    }


    private Single<List<String>> getListIDs(String movieID) {

        Single<List<MyList>> listSingle = mMovieDao.getSavedListsToUpdate()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());

        Single<List<String>> castSingle = tmdbAPI.retrieveFilm(movieID)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(MediaPOJO::getCastCredits)
                .map(CastCredits::bothLists)
                .toObservable()
                .flatMapIterable(list -> list)
                .map(CastInfo::getId)
                .toList();

        return Single.zip(listSingle, castSingle, (x, y) -> {
                    List<String> ids = new ArrayList<>();
                    for (MyList myList : x) {
                        String id = String.valueOf(myList.getList_id());
                        if (y.contains(id)) {
                            ids.add(id);
                        }
                    }
                    return ids;
                }
        );
    }

    public Single<List<MediaByPerson>> getSavedForRankings(boolean isFilm) {
        int isFilmRankings = isFilm ? 1 : 0;
        return mMovieDao.getSavedForRankings(isFilmRankings)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Disposable updateRankingNew(String id, int newRank, boolean isFilm) {

        return Single.just(id)
                .subscribeOn(Schedulers.io())
                .subscribe(x -> {
                            int isFilmRankings = isFilm ? 1 : 0;
                            mMovieDao.updateOtherRankingsDownAfterNew(newRank, isFilmRankings);
                            mMovieDao.updateRanking(x, newRank, isFilmRankings);
                        },
                        e -> Log.e(TAG_RXERROR, "updateRankingNew e=" + e.getMessage())
                );
    }

    public Disposable updateRankingRemove(String id, int oldRank, boolean isFilm) {
        return Single.just(id)
                .subscribeOn(Schedulers.io())
                .subscribe(x -> {
                            int isFilmRankings = isFilm ? 1 : 0;
                            mMovieDao.updateRanking(x, -1, isFilmRankings);
                            mMovieDao.updateOtherRankingsUpAfterRemoval(oldRank, isFilmRankings);
                        },
                        e -> Log.e(TAG_RXERROR, "updateRankingRemove e=" + e.getMessage())
                );
    }

    public Disposable updateRankingUp(String id, int oldRank, int newRank, boolean isFilm) {
        return Single.just(id)
                .subscribeOn(Schedulers.io())
                .subscribe(x -> {
                            int isFilmRankings = isFilm ? 1 : 0;
                            mMovieDao.updateOtherRankingsDown(oldRank, newRank, isFilmRankings);
                            mMovieDao.updateRanking(x, newRank, isFilmRankings);
                        },
                        e -> Log.e(TAG_RXERROR, "updateRankingUp e=" + e.getMessage())
                );

    }

    public Disposable updateRankingDown(String id, int oldRank, int newRank, boolean isFilm) {
        return Single.just(id)
                .subscribeOn(Schedulers.io())
                .subscribe(x -> {
                            int isFilmRankings = isFilm ? 1 : 0;
                            mMovieDao.updateOtherRankingsUp(oldRank, newRank, isFilmRankings);
                            mMovieDao.updateRanking(x, newRank, isFilmRankings);
                        },
                        e -> Log.e(TAG_RXERROR, "updateRankingDown e=" + e.getMessage())
                );
    }

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
}
