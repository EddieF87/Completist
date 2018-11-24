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
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.sleekstats.completist.model.CastCredits;
import xyz.sleekstats.completist.model.CastInfo;
import xyz.sleekstats.completist.model.FilmByPerson;
import xyz.sleekstats.completist.model.FilmPOJO;
import xyz.sleekstats.completist.model.Genre;
import xyz.sleekstats.completist.model.GenreList;
import xyz.sleekstats.completist.model.MovieRoomDB;
import xyz.sleekstats.completist.model.MyList;
import xyz.sleekstats.completist.model.PersonPOJO;
import xyz.sleekstats.completist.model.MediaQueryPOJO;
import xyz.sleekstats.completist.model.PersonQueryPOJO;
import xyz.sleekstats.completist.model.ResultsPOJO;

public class Repo {

    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private TmdbAPI tmdbAPI;
    private MovieDao mMovieDao;
//    private final CompositeDisposable repoCompositeDisposable = new CompositeDisposable();

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
    public Observable<MediaQueryPOJO> queryFilms(String movieQuery) {
        return tmdbAPI.queryFilms(movieQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
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
    public Single<FilmPOJO> getShow(String show_id) {
        return tmdbAPI.retrieveShow(show_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    //Get list of most popular film-people
    public Observable<PersonQueryPOJO> getPopularActors() {
        return tmdbAPI.retrievePopularActors()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //Get list of most popular films
    public Observable<List<FilmByPerson>> getPopularFilms(int pageNumber) {
        return singleToObservable(
                tmdbAPI.retrievePopularMovies(pageNumber)
                        .map(ResultsPOJO::getResults)
        );
    }

    //Get list of films currently playing in theatres
    public Observable<List<FilmByPerson>> getNowPlaying(int pageNumber) {
        return singleToObservable(
                tmdbAPI.retrieveNowPlaying(pageNumber)
                        .map(ResultsPOJO::getResults)
        );
    }

    //Get list of top-rated films
    public Observable<List<FilmByPerson>> getTopRated(int pageNumber) {
        return singleToObservable(
                tmdbAPI.retrieveTopRated(pageNumber)
                        .map(ResultsPOJO::getResults)
        );
    }

    //Get list of popular films/shows by genre
    public Observable<List<FilmByPerson>> getMoviesByGenre(String tvOrMovie, String genre, int pageNumber) {
        return singleToObservable(
                tmdbAPI.retrieveByGenre(tvOrMovie, genre, pageNumber)
                        .map(ResultsPOJO::getResults)
        );
    }


    //Get list of top-rated films
    public Observable<List<FilmByPerson>> getMyWatchedMovies() {
        return singleToObservable(mMovieDao.getSavedWatchedMovies());
    }

    //Get list of top-rated films
    public Observable<List<FilmByPerson>> getMyQueuedMovies() {
        return singleToObservable(mMovieDao.getSavedQueuedMovies());
    }

    private Observable<List<FilmByPerson>> singleToObservable(Single<List<FilmByPerson>> single) {
        return single.toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<GenreList> getGenreList(String tvOrMovie) {
        return tmdbAPI.getGenreList(tvOrMovie)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public void insertMovie(FilmByPerson movie) {
        mMovieDao.insertMovie(movie);
        getListIDs(movie.getId()).subscribe(ids -> {
                    for (String id : ids) {
                        mMovieDao.addWatchedMovie(id);
                    }
                },
                e -> Log.e("rxprob", "getListIDs e=" + e.getMessage()));
    }

    public void insertQueuedMovie(FilmByPerson movie) {
        mMovieDao.insertMovie(movie);
    }

    public void removeMovie(String movieID) {
        mMovieDao.removeMovie(movieID);
        getListIDs(movieID).subscribe(ids -> {
                    for (String id : ids) {
                        mMovieDao.removeWatchedMovie(id);
                    }
                },
                e -> Log.e("rxprob", "getListIDs e=" + e.getMessage()));
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

    public Single<FilmByPerson> checkIfMovieExists(String id) {
        return mMovieDao.checkIfMovieExists(id);
    }

    public void updateFilm(FilmByPerson film) {
        mMovieDao.updateMovie(film);
        if (film.isWatched()) {
            getListIDs(film.getId()).subscribe(ids -> {
                        for (String id : ids) {
                            mMovieDao.addWatchedMovie(id);
                        }
                    },
                    e -> Log.e("rxprob", "getListIDs e=" + e.getMessage()));
        } else {
            getListIDs(film.getId()).subscribe(ids -> {
                        for (String id : ids) {
                            mMovieDao.removeWatchedMovie(id);
                        }
                    },
                    e -> Log.e("rxprob", "getListIDs e=" + e.getMessage()));
        }
    }

    public void updateQueuedFilm(FilmByPerson film) {
        mMovieDao.updateMovie(film);
    }

    public Maybe<MyList> checkIfListExists(String id) {
        return mMovieDao.checkIfListExists(id);
    }

    public Single<List<FilmByPerson>> getAllMovies(List<String> ids) {
        return mMovieDao.getAllMoviesInList(ids);
    }

    public Single<List<FilmByPerson>> getWatchedMovies(List<String> ids) {
        return mMovieDao.getWatchedMoviesInList(ids);
    }

    public Single<List<FilmByPerson>> getQueuedMovies(List<String> ids) {
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
                .map(FilmPOJO::getCastCredits)
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

}
